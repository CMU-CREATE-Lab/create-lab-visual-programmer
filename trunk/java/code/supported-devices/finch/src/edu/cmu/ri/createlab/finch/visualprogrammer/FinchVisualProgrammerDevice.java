package edu.cmu.ri.createlab.finch.visualprogrammer;

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
import edu.cmu.ri.createlab.device.CreateLabDevicePingFailureEventListener;
import edu.cmu.ri.createlab.device.CreateLabDeviceProxy;
import edu.cmu.ri.createlab.expressionbuilder.ExpressionBuilderDevice;
import edu.cmu.ri.createlab.finch.expressionbuilder.FinchExpressionBuilderDevice;
import edu.cmu.ri.createlab.finch.sequencebuilder.FinchSequenceBuilderDevice;
import edu.cmu.ri.createlab.sequencebuilder.SequenceBuilderDevice;
import edu.cmu.ri.createlab.terk.robot.finch.DefaultFinchController;
import edu.cmu.ri.createlab.terk.robot.finch.FinchController;
import edu.cmu.ri.createlab.terk.robot.finch.services.FinchServiceFactoryHelper;
import edu.cmu.ri.createlab.terk.robot.finch.services.FinchServiceManager;
import edu.cmu.ri.createlab.terk.services.ServiceManager;
import edu.cmu.ri.createlab.terk.services.photoresistor.PhotoresistorService;
import edu.cmu.ri.createlab.terk.services.thermistor.ThermistorService;
import edu.cmu.ri.createlab.util.thread.DaemonThreadFactory;
import edu.cmu.ri.createlab.visualprogrammer.BaseVisualProgrammerDevice;
import edu.cmu.ri.createlab.visualprogrammer.Sensor;
import edu.cmu.ri.createlab.visualprogrammer.SensorImpl;
import edu.cmu.ri.createlab.visualprogrammer.VisualProgrammerConstants;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public final class FinchVisualProgrammerDevice extends BaseVisualProgrammerDevice
   {
   private static final Logger LOG = Logger.getLogger(FinchVisualProgrammerDevice.class);

   private static final PropertyResourceBundle RESOURCES = (PropertyResourceBundle)PropertyResourceBundle.getBundle(FinchVisualProgrammerDevice.class.getName());

   private FinchController finch = null;
   private ServiceManager serviceManager = null;
   private final ExpressionBuilderDevice expressionBuilderDevice = new FinchExpressionBuilderDevice();
   private final SequenceBuilderDevice sequenceBuilderDevice = new FinchSequenceBuilderDevice();
   private final FinchServiceFactoryHelper serviceFactoryHelper =
         new FinchServiceFactoryHelper()
         {
         @Override
         public File getAudioDirectory()
            {
            return VisualProgrammerConstants.FilePaths.AUDIO_DIR;
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
                  final ThermistorService thermistorService = (ThermistorService)serviceManager.getServiceByTypeId(ThermistorService.TYPE_ID);
                  if (thermistorService != null)
                     {
                     for (int portNumber = 0; portNumber < thermistorService.getDeviceCount(); portNumber++)
                        {
                        final Integer rawValue = thermistorService.getThermistorValue(portNumber);
                        if (rawValue != null)
                           {
                           publishSensorValueToListeners(ThermistorService.TYPE_ID, portNumber, rawValue);
                           }
                        }
                     }
                  final PhotoresistorService photoresistorService = (PhotoresistorService)serviceManager.getServiceByTypeId(PhotoresistorService.TYPE_ID);
                  if (photoresistorService != null)
                     {
                     final int[] photoresistorValues = photoresistorService.getPhotoresistorValues();
                     if (photoresistorValues != null && photoresistorValues.length > 0)
                        {
                        for (int portNumber = 0; portNumber < photoresistorValues.length; portNumber++)
                           {
                           publishSensorValueToListeners(PhotoresistorService.TYPE_ID, portNumber, photoresistorValues[portNumber]);
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

         private void publishSensorValueToListeners(final String serviceTypeId, final int portNumber, final int rawValue)
            {
            sensorListenerNotificationService.submit(
                  new Runnable()
                  {
                  @Override
                  public void run()
                     {
                     if (LOG.isTraceEnabled())
                        {
                        LOG.trace("FinchVisualProgrammerDevice.run(): notifying [" + sensorListeners.size() + "] listeners of (service, port, rawValue) = (" + serviceTypeId + "," + portNumber + "," + rawValue + ")");
                        }
                     for (final SensorListener listener : sensorListeners)
                        {
                        listener.processSensorRawValue(serviceTypeId, portNumber, rawValue);
                        }
                     }
                  });
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

   public FinchVisualProgrammerDevice()
      {
      super(RESOURCES);
      }

   @Override
   public void connect()
      {
      lock.lock();  // block until condition holds
      try
         {
         finch = DefaultFinchController.create();
         if (finch != null)
            {
            finch.addCreateLabDevicePingFailureEventListener(pingFailureEventListener);
            serviceManager = new FinchServiceManager(finch, serviceFactoryHelper);

            // Unregister all sensors, and then recreate them
            unregisterAllSensors();

            final ThermistorService thermistorService = (ThermistorService)serviceManager.getServiceByTypeId(ThermistorService.TYPE_ID);
            if (thermistorService != null)
               {
               // configure the Temperature Sensor
               registerSensor(new TemperatureSensor(RESOURCES.getString("sensor.temperature.name"),
                                                    thermistorService.getDeviceCount(),
                                                    readConfigValueAsInt("sensor.temperature.min-value", 0),      // TODO: get min value from the service
                                                    readConfigValueAsInt("sensor.temperature.max-value", 255),    // TODO: get max value from the service
                                                    RESOURCES.getString("sensor.temperature.if-branch.label"),
                                                    RESOURCES.getString("sensor.temperature.else-branch.label")));
               }

            final PhotoresistorService photoresistorService = (PhotoresistorService)serviceManager.getServiceByTypeId(PhotoresistorService.TYPE_ID);
            if (photoresistorService != null)
               {
               // configure the Light Sensor
               registerSensor(new LightSensor(RESOURCES.getString("sensor.light.name"),
                                              photoresistorService.getDeviceCount(),
                                              readConfigValueAsInt("sensor.light.min-value", 0),            // TODO: get min value from the service
                                              readConfigValueAsInt("sensor.light.max-value", 255),          // TODO: get max value from the service
                                              RESOURCES.getString("sensor.light.if-branch.label"),
                                              RESOURCES.getString("sensor.light.else-branch.label")));
               }

            // start the sensor poller
            sensorPollingService = Executors.newSingleThreadScheduledExecutor(new DaemonThreadFactory(this.getClass().getSimpleName() + "_sensorPollingService"));
            sensorPollingService.scheduleAtFixedRate(sensorPollingRunnable, 0, 500, TimeUnit.MILLISECONDS);
            }
         }
      catch (final Exception e)
         {
         LOG.error("Exception caught while trying to create the Finch or the FinchServiceManager.", e);
         disconnectWorkhorse(true);
         }
      finally
         {
         lock.unlock();
         }
      }

   private static final class TemperatureSensor extends SensorImpl
      {
      private TemperatureSensor(@NotNull final String name,
                                final int numPorts,
                                final int minValue,
                                final int maxValue,
                                @NotNull final String ifBranchValueLabel,
                                @NotNull final String elseBranchValueLabel)
         {
         // TODO: replace "getThermistorValue" with a psf constant from the ThermistorService
         super(name, ThermistorService.TYPE_ID, "getThermistorValue", numPorts, minValue, maxValue, ifBranchValueLabel, elseBranchValueLabel);
         }
      }

   private static final class LightSensor extends SensorImpl
      {
      private LightSensor(@NotNull final String name,
                          final int numPorts,
                          final int minValue,
                          final int maxValue,
                          @NotNull final String ifBranchValueLabel,
                          @NotNull final String elseBranchValueLabel)
         {
         // TODO: replace "getPhotoresistorValue" with a psf constant from the PhotoresistorService
         super(name, PhotoresistorService.TYPE_ID, "getPhotoresistorValue", numPorts, minValue, maxValue, ifBranchValueLabel, elseBranchValueLabel);
         }
      }

   @Override
   public boolean isConnected()
      {
      lock.lock();  // block until condition holds
      try
         {
         return finch != null && serviceManager != null;
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
         return finch;
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
      return expressionBuilderDevice;
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
      if (finch != null && willDisconnectFromDevice)
         {
         finch.disconnect();
         }

      if (sensorPollingService != null)
         {
         try
            {
            sensorPollingService.shutdownNow();
            }
         catch (Exception e)
            {
            LOG.debug("FinchVisualProgrammerDevice.disconnectWorkhorse(): Exception while trying to shut down the sensor polling service.", e);
            }
         sensorPollingService = null;
         }

      finch = null;
      serviceManager = null;
      }
   }
