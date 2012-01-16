package edu.cmu.ri.createlab.hummingbird.visualprogrammer;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.PropertyResourceBundle;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import edu.cmu.ri.createlab.device.CreateLabDevicePingFailureEventListener;
import edu.cmu.ri.createlab.expressionbuilder.ExpressionBuilderDevice;
import edu.cmu.ri.createlab.expressionbuilder.controlpanel.DeviceGUI;
import edu.cmu.ri.createlab.hummingbird.Hummingbird;
import edu.cmu.ri.createlab.hummingbird.HummingbirdFactory;
import edu.cmu.ri.createlab.hummingbird.HummingbirdHardwareType;
import edu.cmu.ri.createlab.hummingbird.expressionbuilder.controlpanel.HIDHummingbirdGUI;
import edu.cmu.ri.createlab.hummingbird.expressionbuilder.controlpanel.SerialHummingbirdGUI;
import edu.cmu.ri.createlab.hummingbird.services.HummingbirdServiceFactoryHelper;
import edu.cmu.ri.createlab.hummingbird.services.HummingbirdServiceManager;
import edu.cmu.ri.createlab.terk.TerkConstants;
import edu.cmu.ri.createlab.terk.services.Service;
import edu.cmu.ri.createlab.terk.services.ServiceManager;
import edu.cmu.ri.createlab.terk.services.analog.AnalogInputsService;
import edu.cmu.ri.createlab.util.thread.DaemonThreadFactory;
import edu.cmu.ri.createlab.visualprogrammer.Sensor;
import edu.cmu.ri.createlab.visualprogrammer.SensorImpl;
import edu.cmu.ri.createlab.visualprogrammer.VisualProgrammerConstants;
import edu.cmu.ri.createlab.visualprogrammer.VisualProgrammerDevice;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public final class HummingbirdVisualProgrammerDevice implements VisualProgrammerDevice
   {
   private static final Logger LOG = Logger.getLogger(HummingbirdVisualProgrammerDevice.class);

   private static final PropertyResourceBundle RESOURCES = (PropertyResourceBundle)PropertyResourceBundle.getBundle(HummingbirdVisualProgrammerDevice.class.getName());

   private Hummingbird hummingbird = null;
   private ServiceManager serviceManager = null;
   private ExpressionBuilderDevice expressionBuilderDevice;
   private final SortedMap<String, Sensor> sensorMap = new TreeMap<String, Sensor>();
   private final HummingbirdServiceFactoryHelper serviceFactoryHelper =
         new HummingbirdServiceFactoryHelper()
         {
         @Override
         public File getAudioDirectory()
            {
            return VisualProgrammerConstants.FilePaths.AUDIO_DIR;
            }
         };

   private final Lock lock = new ReentrantLock();

   private final Set<VisualProgrammerDevice.SensorListener> sensorListeners = new HashSet<VisualProgrammerDevice.SensorListener>();
   private ScheduledExecutorService sensorPollingService = null;
   private final ExecutorService sensorListenerNotificationService = Executors.newCachedThreadPool(new DaemonThreadFactory(this.getClass().getSimpleName() + "_sensorListenerNotificationService"));
   private Runnable sensorPollingRunnable =
         new Runnable()
         {
         @Override
         public void run()
            {
            try
               {
               if (!sensorListeners.isEmpty() && serviceManager != null)
                  {
                  final AnalogInputsService analogInputsService = (AnalogInputsService)serviceManager.getServiceByTypeId(AnalogInputsService.TYPE_ID);
                  if (analogInputsService != null)
                     {
                     final int[] analogInputValues = analogInputsService.getAnalogInputValues();
                     if (analogInputValues != null)
                        {
                        for (int i = 0; i < analogInputValues.length; i++)
                           {
                           final int portNumber = i;
                           final int rawValue = analogInputValues[portNumber];

                           sensorListenerNotificationService.submit(
                                 new Runnable()
                                 {
                                 @Override
                                 public void run()
                                    {
                                    if (LOG.isTraceEnabled())
                                       {
                                       LOG.trace("HummingbirdVisualProgrammerDevice.run(): notifying [" + sensorListeners.size() + "] listeners of (service, port, rawValue) = (" + AnalogInputsService.TYPE_ID + "," + portNumber + "," + rawValue + ")");
                                       }
                                    for (final SensorListener listener : sensorListeners)
                                       {
                                       listener.processSensorRawValue(AnalogInputsService.TYPE_ID, portNumber, rawValue);
                                       }
                                    }
                                 });
                           }
                        }
                     }
                  }
               }
            catch (Exception e)
               {
               LOG.error("Exception while trying to get the analog input values", e);
               }
            }
         };

   @Override
   public String getDeviceName()
      {
      return RESOURCES.getString("device.name");
      }

   @Override
   public void connect()
      {
      lock.lock();  // block until condition holds
      try
         {
         hummingbird = HummingbirdFactory.create();
         if (hummingbird != null)
            {
            hummingbird.addCreateLabDevicePingFailureEventListener(
                  new CreateLabDevicePingFailureEventListener()
                  {
                  @Override
                  public void handlePingFailureEvent()
                     {
                     lock.lock();  // block until condition holds
                     try
                        {
                        disconnectWorkhorse(false);
                        }
                     finally
                        {
                        lock.unlock();
                        }
                     }
                  }
            );
            serviceManager = new HummingbirdServiceManager(hummingbird, serviceFactoryHelper);

            // create an appropriate ExpressionBuilderDevice--it returns the proper DeviceGUI depending on whether we're connected to an HID or serial hummingbird
            expressionBuilderDevice =
                  new ExpressionBuilderDevice()
                  {
                  private final DeviceGUI deviceGUI = HummingbirdHardwareType.HID.equals(hummingbird.getHummingbirdProperties().getHardwareType()) ? new HIDHummingbirdGUI() : new SerialHummingbirdGUI();

                  @Override
                  public DeviceGUI getDeviceGUI()
                     {
                     return deviceGUI;
                     }
                  };

            // Build the map of sensor types.  First get the min and max allowed values from the AnalogInputsService
            sensorMap.clear();

            int defaultMinValue = 0;
            int defaultMaxValue = 0;
            int numPorts = 0;
            final Service analogInputsService = serviceManager.getServiceByTypeId(AnalogInputsService.TYPE_ID);
            if (analogInputsService != null)
               {
               final Integer minValueInteger = analogInputsService.getPropertyAsInteger(AnalogInputsService.PROPERTY_NAME_MIN_VALUE);
               final Integer maxValueInteger = analogInputsService.getPropertyAsInteger(AnalogInputsService.PROPERTY_NAME_MAX_VALUE);
               final Integer numPortsInteger = analogInputsService.getPropertyAsInteger(TerkConstants.PropertyKeys.DEVICE_COUNT);

               if (minValueInteger != null)
                  {
                  defaultMinValue = minValueInteger;
                  }
               if (maxValueInteger != null)
                  {
                  defaultMaxValue = maxValueInteger;
                  }
               if (numPortsInteger != null)
                  {
                  numPorts = numPortsInteger;
                  }
               }

            if (numPorts > 0)
               {
               // configure  the Light Sensor
               final AnalogInputSensor lightSensor = new AnalogInputSensor(RESOURCES.getString("sensor.light.name"),
                                                                           numPorts,
                                                                           readConfigValue("sensor.light.min-value", defaultMinValue),
                                                                           readConfigValue("sensor.light.max-value", defaultMaxValue),
                                                                           RESOURCES.getString("sensor.light.if-branch.label"),
                                                                           RESOURCES.getString("sensor.light.else-branch.label"));
               sensorMap.put(lightSensor.getMapKey(), lightSensor);

               // configure the Distance Sensor
               final AnalogInputSensor distanceSensor = new AnalogInputSensor(RESOURCES.getString("sensor.distance.name"),
                                                                              numPorts,
                                                                              readConfigValue("sensor.distance.min-value", defaultMinValue),
                                                                              readConfigValue("sensor.distance.max-value", defaultMaxValue),
                                                                              RESOURCES.getString("sensor.distance.if-branch.label"),
                                                                              RESOURCES.getString("sensor.distance.else-branch.label"));
               sensorMap.put(distanceSensor.getMapKey(), distanceSensor);

               // configure the Potentiometer
               final AnalogInputSensor potentiometer = new AnalogInputSensor(RESOURCES.getString("sensor.potentiometer.name"),
                                                                             numPorts,
                                                                             readConfigValue("sensor.potentiometer.min-value", defaultMinValue),
                                                                             readConfigValue("sensor.potentiometer.max-value", defaultMaxValue),
                                                                             RESOURCES.getString("sensor.potentiometer.if-branch.label"),
                                                                             RESOURCES.getString("sensor.potentiometer.else-branch.label"));
               sensorMap.put(potentiometer.getMapKey(), potentiometer);

               // configure the Temperature Sensor
               final AnalogInputSensor temperatureSensor = new AnalogInputSensor(RESOURCES.getString("sensor.temperature.name"),
                                                                                 numPorts,
                                                                                 readConfigValue("sensor.temperature.min-value", defaultMinValue),
                                                                                 readConfigValue("sensor.temperature.max-value", defaultMaxValue),
                                                                                 RESOURCES.getString("sensor.temperature.if-branch.label"),
                                                                                 RESOURCES.getString("sensor.temperature.else-branch.label"));
               sensorMap.put(temperatureSensor.getMapKey(), temperatureSensor);

               // configure the Raw Value Sensor
               final AnalogInputSensor rawValueSensor = new AnalogInputSensor(RESOURCES.getString("sensor.raw.name"),
                                                                              numPorts,
                                                                              readConfigValue("sensor.raw.min-value", defaultMinValue),
                                                                              readConfigValue("sensor.raw.max-value", defaultMaxValue),
                                                                              RESOURCES.getString("sensor.raw.if-branch.label"),
                                                                              RESOURCES.getString("sensor.raw.else-branch.label"));
               sensorMap.put(rawValueSensor.getMapKey(), rawValueSensor);

               // configure the Sound Sensor
               final AnalogInputSensor soundSensor = new AnalogInputSensor(RESOURCES.getString("sensor.sound.name"),
                                                                           numPorts,
                                                                           readConfigValue("sensor.sound.min-value", defaultMinValue),
                                                                           readConfigValue("sensor.sound.max-value", defaultMaxValue),
                                                                           RESOURCES.getString("sensor.sound.if-branch.label"),
                                                                           RESOURCES.getString("sensor.sound.else-branch.label"));
               sensorMap.put(soundSensor.getMapKey(), soundSensor);
               }

            // start the sensor poller
            sensorPollingService = Executors.newSingleThreadScheduledExecutor(new DaemonThreadFactory(this.getClass().getSimpleName() + "_sensorPollingService"));
            sensorPollingService.scheduleAtFixedRate(sensorPollingRunnable, 0, 500, TimeUnit.MILLISECONDS);
            }
         }
      catch (final Exception e)
         {
         LOG.error("Exception caught while trying to create the Hummingbird or the HummingbirdServiceManager.", e);
         disconnectWorkhorse(true);
         }
      finally
         {
         lock.unlock();
         }
      }

   private int readConfigValue(final String propertyKey, final int defaultValue)
      {
      int value = defaultValue;
      try
         {
         final String valueFromProperties = RESOURCES.getString(propertyKey);
         value = Integer.parseInt(valueFromProperties);
         }
      catch (Exception ignored)
         {
         if (LOG.isInfoEnabled())
            {
            LOG.info("Exception reading or converting '" + propertyKey + "' property value from properties file, using default [" + value + "]");
            }
         }
      return value;
      }

   @Override
   public boolean isConnected()
      {
      lock.lock();  // block until condition holds
      try
         {
         return hummingbird != null && serviceManager != null;
         }
      finally
         {
         lock.unlock();
         }
      }

   @Override
   public Hummingbird getDeviceProxy()
      {
      lock.lock();  // block until condition holds
      try
         {
         return hummingbird;
         }
      finally
         {
         lock.unlock();
         }
      }

   @Override
   public ServiceManager getServiceManager()
      {
      lock.lock();  // block until condition holds
      try
         {
         return serviceManager;
         }
      finally
         {
         lock.unlock();
         }
      }

   @Override
   public ExpressionBuilderDevice getExpressionBuilderDevice()
      {
      lock.lock();  // block until condition holds
      try
         {
         return expressionBuilderDevice;
         }
      finally
         {
         lock.unlock();
         }
      }

   @Override
   @NotNull
   public SortedSet<Sensor> getSensors()
      {
      lock.lock();  // block until condition holds
      try
         {
         return Collections.unmodifiableSortedSet(new TreeSet<Sensor>(sensorMap.values()));
         }
      finally
         {
         lock.unlock();
         }
      }

   @Override
   @Nullable
   public Sensor findSensor(@Nullable final String sensorName, @Nullable final String serviceTypeId)
      {
      lock.lock();  // block until condition holds
      try
         {
         return sensorMap.get(createMapKey(sensorName, serviceTypeId));
         }
      finally
         {
         lock.unlock();
         }
      }

   @Override
   public final void addSensorListener(@Nullable final SensorListener listener)
      {
      if (listener != null)
         {
         sensorListeners.add(listener);
         }
      }

   @Override
   public final void removeSensorListener(@Nullable final SensorListener listener)
      {
      if (listener != null)
         {
         sensorListeners.remove(listener);
         }
      }

   private static String createMapKey(final String sensorName, final String serviceTypeId)
      {
      return sensorName + "|" + serviceTypeId;
      }

   @Override
   public void disconnect()
      {
      lock.lock();  // block until condition holds
      try
         {
         disconnectWorkhorse(true);
         }
      finally
         {
         lock.unlock();
         }
      }

   // MUST be called from within a lock block!
   private void disconnectWorkhorse(final boolean willDisconnectFromHummingbird)
      {
      if (hummingbird != null && willDisconnectFromHummingbird)
         {
         hummingbird.disconnect();
         }

      shutdownSensorPollingService();

      hummingbird = null;
      serviceManager = null;
      expressionBuilderDevice = null;
      sensorPollingService = null;
      }

   // MUST be called from within a lock block!
   private void shutdownSensorPollingService()
      {
      if (sensorPollingService != null)
         {
         try
            {
            sensorPollingService.shutdownNow();
            }
         catch (Exception e)
            {
            LOG.debug("HummingbirdVisualProgrammerDevice.disconnectWorkhorse(): Exception while trying to shut down the sensor polling service.", e);
            }
         }
      }

   private static class AnalogInputSensor extends SensorImpl
      {
      private AnalogInputSensor(@NotNull final String name,
                                final int numPorts,
                                final int minValue,
                                final int maxValue,
                                @NotNull final String ifBranchValueLabel,
                                @NotNull final String elseBranchValueLabel)
         {
         super(name, AnalogInputsService.TYPE_ID, "getAnalogInputValue", numPorts, minValue, maxValue, ifBranchValueLabel, elseBranchValueLabel);
         }

      private String getMapKey()
         {
         return createMapKey(getName(), getServiceTypeId());
         }
      }
   }
