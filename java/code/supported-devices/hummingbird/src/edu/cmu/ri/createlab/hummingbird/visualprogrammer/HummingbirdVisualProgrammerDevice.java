package edu.cmu.ri.createlab.hummingbird.visualprogrammer;

import java.io.File;
import java.util.HashSet;
import java.util.PropertyResourceBundle;
import java.util.Set;
import java.util.SortedSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import javax.swing.ImageIcon;
import edu.cmu.ri.createlab.device.CreateLabDevicePingFailureEventListener;
import edu.cmu.ri.createlab.device.CreateLabDeviceProxy;
import edu.cmu.ri.createlab.expressionbuilder.ExpressionBuilderDevice;
import edu.cmu.ri.createlab.expressionbuilder.controlpanel.DeviceGUI;
import edu.cmu.ri.createlab.hummingbird.Hummingbird;
import edu.cmu.ri.createlab.hummingbird.HummingbirdFactory;
import edu.cmu.ri.createlab.hummingbird.HummingbirdHardwareType;
import edu.cmu.ri.createlab.hummingbird.expressionbuilder.controlpanel.HIDHummingbirdGUI;
import edu.cmu.ri.createlab.hummingbird.expressionbuilder.controlpanel.SerialHummingbirdGUI;
import edu.cmu.ri.createlab.hummingbird.sequencebuilder.HummingbirdSequenceBuilderDevice;
import edu.cmu.ri.createlab.hummingbird.services.HummingbirdServiceFactoryHelper;
import edu.cmu.ri.createlab.hummingbird.services.HummingbirdServiceManager;
import edu.cmu.ri.createlab.sequencebuilder.SequenceBuilderDevice;
import edu.cmu.ri.createlab.terk.services.ServiceManager;
import edu.cmu.ri.createlab.terk.services.analog.AnalogInputsService;
import edu.cmu.ri.createlab.userinterface.util.ImageUtils;
import edu.cmu.ri.createlab.util.thread.DaemonThreadFactory;
import edu.cmu.ri.createlab.visualprogrammer.BaseVisualProgrammerDevice;
import edu.cmu.ri.createlab.visualprogrammer.IntegralValueSensor;
import edu.cmu.ri.createlab.visualprogrammer.PathManager;
import edu.cmu.ri.createlab.visualprogrammer.Sensor;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public final class HummingbirdVisualProgrammerDevice extends BaseVisualProgrammerDevice
   {
   private static final Logger LOG = Logger.getLogger(HummingbirdVisualProgrammerDevice.class);

   private static final PropertyResourceBundle RESOURCES = (PropertyResourceBundle)PropertyResourceBundle.getBundle(HummingbirdVisualProgrammerDevice.class.getName());

   private static final class AnalogInputSensor extends IntegralValueSensor
      {
      private AnalogInputSensor(@NotNull final String name,
                                final int numPorts,
                                final int minValue,
                                final int maxValue,
                                @NotNull final String ifBranchValueLabel,
                                @NotNull final String elseBranchValueLabel)
         {
         super(name, AnalogInputsService.TYPE_ID, AnalogInputsService.OPERATION_NAME_GET_ANALOG_INPUT_VALUE, numPorts, minValue, maxValue, ifBranchValueLabel, elseBranchValueLabel);
         }
      }

   private Hummingbird hummingbird = null;
   private ServiceManager serviceManager = null;
   private ExpressionBuilderDevice expressionBuilderDevice;
   private SequenceBuilderDevice sequenceBuilderDevice = new HummingbirdSequenceBuilderDevice();
   private final HummingbirdServiceFactoryHelper serviceFactoryHelper =
         new HummingbirdServiceFactoryHelper()
         {
         @Override
         public File getAudioDirectory()
            {
            return PathManager.getInstance().getAudioDirectory();
            }
         };

   private final Lock lock = new ReentrantLock();

   private final Set<SensorListener> sensorListeners = new HashSet<SensorListener>();
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

   private final CreateLabDevicePingFailureEventListener pingFailureEventListener =
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
         };

   public HummingbirdVisualProgrammerDevice()
      {
      super(RESOURCES);
      }

   @Nullable
   @Override
   public String getDeviceVersion()
      {
      if (hummingbird != null)
         {
         return hummingbird.getHardwareVersion() + "," + hummingbird.getFirmwareVersion();
         }
      return null;
      }

   @Override
   public ImageIcon getConnectingImage()
      {
      return ImageUtils.createImageIcon(RESOURCES.getString("image.connecting"));
      }

   @Override
   public ImageIcon getConnectionTipsImage()
      {
      return ImageUtils.createImageIcon(RESOURCES.getString("image.connection-tips"));
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
            hummingbird.addCreateLabDevicePingFailureEventListener(pingFailureEventListener);
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

            // Unregister all sensors, and then recreate them
            unregisterAllSensors();

            // Get the min and max allowed values from the AnalogInputsService
            final AnalogInputsService analogInputsService = (AnalogInputsService)getServiceManager().getServiceByTypeId(AnalogInputsService.TYPE_ID);
            int defaultMinValue = 0;
            int defaultMaxValue = 255;
            int numPorts = 0;
            if (analogInputsService != null)
               {
               numPorts = analogInputsService.getDeviceCount();
               final Integer minValueInteger = analogInputsService.getPropertyAsInteger(AnalogInputsService.PROPERTY_NAME_MIN_VALUE);
               final Integer maxValueInteger = analogInputsService.getPropertyAsInteger(AnalogInputsService.PROPERTY_NAME_MAX_VALUE);

               if (minValueInteger != null)
                  {
                  defaultMinValue = minValueInteger;
                  }
               if (maxValueInteger != null)
                  {
                  defaultMaxValue = maxValueInteger;
                  }
               }

            if (numPorts > 0)
               {
               // configure  the Light Sensor
               registerSensor(new AnalogInputSensor(RESOURCES.getString("sensor.light.name"),
                                                    numPorts,
                                                    readConfigValueAsInt("sensor.light.min-value", defaultMinValue),
                                                    readConfigValueAsInt("sensor.light.max-value", defaultMaxValue),
                                                    RESOURCES.getString("sensor.light.if-branch.label"),
                                                    RESOURCES.getString("sensor.light.else-branch.label")));

               // configure the Distance Sensor
               registerSensor(new AnalogInputSensor(RESOURCES.getString("sensor.distance.name"),
                                                    numPorts,
                                                    readConfigValueAsInt("sensor.distance.min-value", defaultMinValue),
                                                    readConfigValueAsInt("sensor.distance.max-value", defaultMaxValue),
                                                    RESOURCES.getString("sensor.distance.if-branch.label"),
                                                    RESOURCES.getString("sensor.distance.else-branch.label")));

               // configure the Potentiometer
               registerSensor(new AnalogInputSensor(RESOURCES.getString("sensor.potentiometer.name"),
                                                    numPorts,
                                                    readConfigValueAsInt("sensor.potentiometer.min-value", defaultMinValue),
                                                    readConfigValueAsInt("sensor.potentiometer.max-value", defaultMaxValue),
                                                    RESOURCES.getString("sensor.potentiometer.if-branch.label"),
                                                    RESOURCES.getString("sensor.potentiometer.else-branch.label")));

               // configure the Temperature Sensor
               registerSensor(new AnalogInputSensor(RESOURCES.getString("sensor.temperature.name"),
                                                    numPorts,
                                                    readConfigValueAsInt("sensor.temperature.min-value", defaultMinValue),
                                                    readConfigValueAsInt("sensor.temperature.max-value", defaultMaxValue),
                                                    RESOURCES.getString("sensor.temperature.if-branch.label"),
                                                    RESOURCES.getString("sensor.temperature.else-branch.label")));

               // configure the Raw Value Sensor
               registerSensor(new AnalogInputSensor(RESOURCES.getString("sensor.raw.name"),
                                                    numPorts,
                                                    readConfigValueAsInt("sensor.raw.min-value", defaultMinValue),
                                                    readConfigValueAsInt("sensor.raw.max-value", defaultMaxValue),
                                                    RESOURCES.getString("sensor.raw.if-branch.label"),
                                                    RESOURCES.getString("sensor.raw.else-branch.label")));

               // configure the Sound Sensor
               registerSensor(new AnalogInputSensor(RESOURCES.getString("sensor.sound.name"),
                                                    numPorts,
                                                    readConfigValueAsInt("sensor.sound.min-value", defaultMinValue),
                                                    readConfigValueAsInt("sensor.sound.max-value", defaultMaxValue),
                                                    RESOURCES.getString("sensor.sound.if-branch.label"),
                                                    RESOURCES.getString("sensor.sound.else-branch.label")));
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
   @Nullable
   public CreateLabDeviceProxy getDeviceProxy()
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
   @Nullable
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
   @NotNull
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
   public SequenceBuilderDevice getSequenceBuilderDevice()
      {
      return sequenceBuilderDevice;
      }

   @Override
   @NotNull
   public SortedSet<Sensor> getSensors()
      {
      lock.lock();  // block until condition holds
      try
         {
         return getSensorsAsSortedSet();
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

   @Override
   @Nullable
   public Sensor findSensor(@Nullable final String sensorName, @Nullable final String serviceTypeId)
      {
      lock.lock();  // block until condition holds
      try
         {
         return getRegisteredSensor(sensorName, serviceTypeId);
         }
      finally
         {
         lock.unlock();
         }
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
   private void disconnectWorkhorse(final boolean willDisconnectFromDevice)
      {
      if (hummingbird != null && willDisconnectFromDevice)
         {
         hummingbird.disconnect();
         }

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
         sensorPollingService = null;
         }

      hummingbird = null;
      serviceManager = null;
      expressionBuilderDevice = null;
      }
   }
