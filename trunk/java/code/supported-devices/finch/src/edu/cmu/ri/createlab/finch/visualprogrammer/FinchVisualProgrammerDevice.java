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
import javax.swing.ImageIcon;
import edu.cmu.ri.createlab.device.CreateLabDevicePingFailureEventListener;
import edu.cmu.ri.createlab.device.CreateLabDeviceProxy;
import edu.cmu.ri.createlab.device.connectivity.FinchConnectivityManager;
import edu.cmu.ri.createlab.expressionbuilder.ExpressionBuilderDevice;
import edu.cmu.ri.createlab.finch.expressionbuilder.FinchExpressionBuilderDevice;
import edu.cmu.ri.createlab.finch.sequencebuilder.FinchSequenceBuilderDevice;
import edu.cmu.ri.createlab.sequencebuilder.SequenceBuilderDevice;
import edu.cmu.ri.createlab.terk.robot.finch.FinchController;
import edu.cmu.ri.createlab.terk.robot.finch.services.FinchServiceFactoryHelper;
import edu.cmu.ri.createlab.terk.robot.finch.services.FinchServiceManager;
import edu.cmu.ri.createlab.terk.services.ServiceManager;
import edu.cmu.ri.createlab.terk.services.accelerometer.AccelerometerGs;
import edu.cmu.ri.createlab.terk.services.accelerometer.AccelerometerService;
import edu.cmu.ri.createlab.terk.services.obstacle.SimpleObstacleDetectorService;
import edu.cmu.ri.createlab.terk.services.photoresistor.PhotoresistorService;
import edu.cmu.ri.createlab.terk.services.thermistor.ThermistorService;
import edu.cmu.ri.createlab.userinterface.util.ImageUtils;
import edu.cmu.ri.createlab.util.thread.DaemonThreadFactory;
import edu.cmu.ri.createlab.visualprogrammer.BaseVisualProgrammerDevice;
import edu.cmu.ri.createlab.visualprogrammer.BooleanValueSensor;
import edu.cmu.ri.createlab.visualprogrammer.DoubleValueSensor;
import edu.cmu.ri.createlab.visualprogrammer.IntegralValueSensor;
import edu.cmu.ri.createlab.visualprogrammer.Sensor;
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
                  final SimpleObstacleDetectorService obstacleDetectorService = (SimpleObstacleDetectorService)serviceManager.getServiceByTypeId(SimpleObstacleDetectorService.TYPE_ID);
                  if (obstacleDetectorService != null)
                     {
                     final boolean[] areDetected = obstacleDetectorService.areObstaclesDetected();
                     if (areDetected != null && areDetected.length > 0)
                        {
                        for (int portNumber = 0; portNumber < areDetected.length; portNumber++)
                           {
                           publishSensorValueToListeners(SimpleObstacleDetectorService.TYPE_ID, portNumber, areDetected[portNumber]);
                           }
                        }
                     }
                  final AccelerometerService accelerometerService = (AccelerometerService)serviceManager.getServiceByTypeId(AccelerometerService.TYPE_ID);
                  if (accelerometerService != null)
                     {
                     for (int portNumber = 0; portNumber < accelerometerService.getDeviceCount(); portNumber++)
                        {
                        final AccelerometerGs rawValue = accelerometerService.getAccelerometerGs(portNumber);
                        if (rawValue != null)
                           {
                           publishSensorValueToListeners(AccelerometerService.TYPE_ID, portNumber, rawValue);
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

         private void publishSensorValueToListeners(final String serviceTypeId, final int portNumber, final Object rawValue)
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
         finch = new FinchConnectivityManager().connect();
         if (finch != null)
            {
            finch.addCreateLabDevicePingFailureEventListener(pingFailureEventListener);
            serviceManager = new FinchServiceManager(finch, serviceFactoryHelper);

            // Unregister all sensors, and then recreate them
            unregisterAllSensors();

            final ThermistorService thermistorService = (ThermistorService)serviceManager.getServiceByTypeId(ThermistorService.TYPE_ID);
            if (thermistorService != null)
               {
               final Integer minValueInteger = thermistorService.getPropertyAsInteger(ThermistorService.PROPERTY_NAME_MIN_VALUE);
               final Integer maxValueInteger = thermistorService.getPropertyAsInteger(ThermistorService.PROPERTY_NAME_MAX_VALUE);
               final int defaultMinValue = (minValueInteger == null) ? 0 : minValueInteger;
               final int defaultMaxValue = (maxValueInteger == null) ? 255 : maxValueInteger;

               // configure the Temperature Sensor
               registerSensor(new TemperatureSensor(RESOURCES.getString("sensor.temperature.name"),
                                                    thermistorService.getDeviceCount(),
                                                    readConfigValueAsInt("sensor.temperature.min-value", defaultMinValue),
                                                    readConfigValueAsInt("sensor.temperature.max-value", defaultMaxValue),
                                                    RESOURCES.getString("sensor.temperature.if-branch.label"),
                                                    RESOURCES.getString("sensor.temperature.else-branch.label")));
               }

            final PhotoresistorService photoresistorService = (PhotoresistorService)serviceManager.getServiceByTypeId(PhotoresistorService.TYPE_ID);
            if (photoresistorService != null)
               {
               final Integer minValueInteger = photoresistorService.getPropertyAsInteger(PhotoresistorService.PROPERTY_NAME_MIN_VALUE);
               final Integer maxValueInteger = photoresistorService.getPropertyAsInteger(PhotoresistorService.PROPERTY_NAME_MAX_VALUE);
               final int defaultMinValue = (minValueInteger == null) ? 0 : minValueInteger;
               final int defaultMaxValue = (maxValueInteger == null) ? 255 : maxValueInteger;

               // configure the Light Sensor
               registerSensor(new LightSensor(RESOURCES.getString("sensor.light.name"),
                                              photoresistorService.getDeviceCount(),
                                              readConfigValueAsInt("sensor.light.min-value", defaultMinValue),
                                              readConfigValueAsInt("sensor.light.max-value", defaultMaxValue),
                                              RESOURCES.getString("sensor.light.if-branch.label"),
                                              RESOURCES.getString("sensor.light.else-branch.label")));
               }

            final SimpleObstacleDetectorService obstacleDetectorService = (SimpleObstacleDetectorService)serviceManager.getServiceByTypeId(SimpleObstacleDetectorService.TYPE_ID);
            if (obstacleDetectorService != null)
               {
               // configure the Obstact Dectors
               registerSensor(new ObstacleSensor(RESOURCES.getString("sensor.obstactle.name"),
                                                 obstacleDetectorService.getDeviceCount(),
                                                 RESOURCES.getString("sensor.obstactle.if-branch.label"),
                                                 RESOURCES.getString("sensor.obstactle.else-branch.label")));
               }

            final AccelerometerService accelerometerService = (AccelerometerService)serviceManager.getServiceByTypeId(AccelerometerService.TYPE_ID);
            if (accelerometerService != null)
               {
               registerSensor(
                     new AccelerometerOrientationSensor(RESOURCES.getString("sensor.accelerometer-orientation-is-beak-up.name"),
                                                        accelerometerService.getDeviceCount(),
                                                        RESOURCES.getString("sensor.accelerometer-orientation.if-branch.label"),
                                                        RESOURCES.getString("sensor.accelerometer-orientation.else-branch.label"))
                     {
                     @Override
                     protected boolean checkOrientation(@NotNull final AccelerometerGs gs)
                        {
                        return gs.getX() < -0.8 && gs.getX() > -1.5 && gs.getY() > -0.3 && gs.getY() < 0.3 && gs.getZ() > -0.3 && gs.getZ() < 0.3;
                        }
                     });
               registerSensor(
                     new AccelerometerOrientationSensor(RESOURCES.getString("sensor.accelerometer-orientation-is-beak-down.name"),
                                                        accelerometerService.getDeviceCount(),
                                                        RESOURCES.getString("sensor.accelerometer-orientation.if-branch.label"),
                                                        RESOURCES.getString("sensor.accelerometer-orientation.else-branch.label"))
                     {
                     @Override
                     protected boolean checkOrientation(@NotNull final AccelerometerGs gs)
                        {
                        return gs.getX() < 1.5 && gs.getX() > 0.8 && gs.getY() > -0.3 && gs.getY() < 0.3 && gs.getZ() > -0.3 && gs.getZ() < 0.3;
                        }
                     });
               registerSensor(
                     new AccelerometerOrientationSensor(RESOURCES.getString("sensor.accelerometer-orientation-is-level.name"),
                                                        accelerometerService.getDeviceCount(),
                                                        RESOURCES.getString("sensor.accelerometer-orientation.if-branch.label"),
                                                        RESOURCES.getString("sensor.accelerometer-orientation.else-branch.label"))
                     {
                     @Override
                     protected boolean checkOrientation(@NotNull final AccelerometerGs gs)
                        {
                        return gs.getX() > -0.5 && gs.getX() < 0.5 && gs.getY() > -0.5 && gs.getY() < 0.5 && gs.getZ() > 0.65 && gs.getZ() < 1.5;
                        }
                     });
               registerSensor(
                     new AccelerometerOrientationSensor(RESOURCES.getString("sensor.accelerometer-orientation-is-upside-down.name"),
                                                        accelerometerService.getDeviceCount(),
                                                        RESOURCES.getString("sensor.accelerometer-orientation.if-branch.label"),
                                                        RESOURCES.getString("sensor.accelerometer-orientation.else-branch.label"))
                     {
                     @Override
                     protected boolean checkOrientation(@NotNull final AccelerometerGs gs)
                        {
                        return gs.getX() > -0.5 && gs.getX() < 0.5 && gs.getY() > -0.5 && gs.getY() < 0.5 && gs.getZ() > -1.5 && gs.getZ() < -0.65;
                        }
                     });
               registerSensor(
                     new AccelerometerOrientationSensor(RESOURCES.getString("sensor.accelerometer-orientation-is-left-wing-up.name"),
                                                        accelerometerService.getDeviceCount(),
                                                        RESOURCES.getString("sensor.accelerometer-orientation.if-branch.label"),
                                                        RESOURCES.getString("sensor.accelerometer-orientation.else-branch.label"))
                     {
                     @Override
                     protected boolean checkOrientation(@NotNull final AccelerometerGs gs)
                        {
                        return gs.getX() > -0.5 && gs.getX() < 0.5 && gs.getY() > -1.5 && gs.getY() < -0.7 && gs.getZ() > -0.5 && gs.getZ() < 0.5;
                        }
                     });
               registerSensor(
                     new AccelerometerOrientationSensor(RESOURCES.getString("sensor.accelerometer-orientation-is-right-wing-up.name"),
                                                        accelerometerService.getDeviceCount(),
                                                        RESOURCES.getString("sensor.accelerometer-orientation.if-branch.label"),
                                                        RESOURCES.getString("sensor.accelerometer-orientation.else-branch.label"))
                     {
                     @Override
                     protected boolean checkOrientation(@NotNull final AccelerometerGs gs)
                        {
                        return gs.getX() > -0.5 && gs.getX() < 0.5 && gs.getY() > 0.7 && gs.getY() < 1.5 && gs.getZ() > -0.5 && gs.getZ() < 0.5;
                        }
                     });
               registerSensor(
                     new AccelerometerSensor(RESOURCES.getString("sensor.accelerometer-roll.name"),
                                             accelerometerService.getDeviceCount(),
                                             readConfigValueAsInt("sensor.accelerometer-roll.min-value", 90),
                                             readConfigValueAsInt("sensor.accelerometer-roll.max-value", -90),
                                             RESOURCES.getString("sensor.accelerometer-roll.if-branch.label"),
                                             RESOURCES.getString("sensor.accelerometer-roll.else-branch.label"))
                     {
                     @Override
                     protected double getOrientationInDegrees(final double x, final double y, final double z)
                        {
                        // compute roll
                        return Math.atan(y / Math.sqrt(x * x + z * z)) * AccelerometerSensor.RADIANS_TO_DEGREES;
                        }
                     });
               registerSensor(
                     new AccelerometerSensor(RESOURCES.getString("sensor.accelerometer-pitch.name"),
                                             accelerometerService.getDeviceCount(),
                                             readConfigValueAsInt("sensor.accelerometer-pitch.min-value", 90),
                                             readConfigValueAsInt("sensor.accelerometer-pitch.max-value", -90),
                                             RESOURCES.getString("sensor.accelerometer-pitch.if-branch.label"),
                                             RESOURCES.getString("sensor.accelerometer-pitch.else-branch.label"))
                     {
                     @Override
                     protected double getOrientationInDegrees(final double x, final double y, final double z)
                        {
                        // compute pitch
                        return Math.atan(x / Math.sqrt(y * y + z * z)) * AccelerometerSensor.RADIANS_TO_DEGREES;
                        }
                     });
               registerSensor(
                     new AccelerometerSensor(RESOURCES.getString("sensor.accelerometer-level.name"),
                                             accelerometerService.getDeviceCount(),
                                             readConfigValueAsInt("sensor.accelerometer-level.min-value", -90),
                                             readConfigValueAsInt("sensor.accelerometer-level.max-value", 90),
                                             RESOURCES.getString("sensor.accelerometer-level.if-branch.label"),
                                             RESOURCES.getString("sensor.accelerometer-level.else-branch.label"))
                     {
                     @Override
                     protected double getOrientationInDegrees(final double x, final double y, final double z)
                        {
                        // compute levelness
                        return Math.atan(z / Math.sqrt(x * x + y * y)) * AccelerometerSensor.RADIANS_TO_DEGREES;
                        }
                     });
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

   private static final class TemperatureSensor extends IntegralValueSensor
      {
      private TemperatureSensor(@NotNull final String name,
                                final int numPorts,
                                final int minValue,
                                final int maxValue,
                                @NotNull final String ifBranchValueLabel,
                                @NotNull final String elseBranchValueLabel)
         {
         super(name, ThermistorService.TYPE_ID, ThermistorService.OPERATION_NAME_GET_THERMISTOR_VALUE, numPorts, minValue, maxValue, ifBranchValueLabel, elseBranchValueLabel);
         }
      }

   private static final class LightSensor extends IntegralValueSensor
      {
      private LightSensor(@NotNull final String name,
                          final int numPorts,
                          final int minValue,
                          final int maxValue,
                          @NotNull final String ifBranchValueLabel,
                          @NotNull final String elseBranchValueLabel)
         {
         super(name, PhotoresistorService.TYPE_ID, PhotoresistorService.OPERATION_NAME_GET_PHOTORESISTOR_VALUE, numPorts, minValue, maxValue, ifBranchValueLabel, elseBranchValueLabel);
         }
      }

   private static final class ObstacleSensor extends BooleanValueSensor
      {
      private ObstacleSensor(@NotNull final String name,
                             final int numPorts,
                             @NotNull final String ifBranchValueLabel,
                             @NotNull final String elseBranchValueLabel)
         {
         super(name, SimpleObstacleDetectorService.TYPE_ID, SimpleObstacleDetectorService.OPERATION_NAME_IS_OBSTACLE_DETECTED, numPorts, ifBranchValueLabel, elseBranchValueLabel);
         }
      }

   private abstract static class AccelerometerSensor extends DoubleValueSensor
      {
      // NOTE: I got the roll and pitch formulas from http://www.st.com/internet/com/TECHNICAL_RESOURCES/TECHNICAL_LITERATURE/APPLICATION_NOTE/CD00268887.pdf

      private static final double RADIANS_TO_DEGREES = 180 / Math.PI;

      protected AccelerometerSensor(@NotNull final String name,
                                    final int numPorts,
                                    final int minValue,
                                    final int maxValue,
                                    @NotNull final String ifBranchValueLabel,
                                    @NotNull final String elseBranchValueLabel)
         {
         super(name,
               AccelerometerService.TYPE_ID,
               AccelerometerService.OPERATION_NAME_GET_ACCELEROMETER_GS,
               numPorts,
               minValue,
               maxValue,
               ifBranchValueLabel,
               elseBranchValueLabel);
         }

      @Override
      @Nullable
      protected final Double convertRawValueToDouble(@NotNull final Object rawValue)
         {
         if (rawValue instanceof AccelerometerGs)
            {
            final AccelerometerGs gs = (AccelerometerGs)rawValue;
            return getOrientationInDegrees(gs.getX(), gs.getY(), gs.getZ());
            }
         return null;
         }

      protected abstract double getOrientationInDegrees(final double x, final double y, final double z);
      }

   private abstract static class AccelerometerOrientationSensor extends BooleanValueSensor
      {
      protected AccelerometerOrientationSensor(@NotNull final String name,
                                               final int numPorts,
                                               @NotNull final String ifBranchValueLabel,
                                               @NotNull final String elseBranchValueLabel)
         {
         super(name,
               AccelerometerService.TYPE_ID,
               AccelerometerService.OPERATION_NAME_GET_ACCELEROMETER_GS,
               numPorts,
               ifBranchValueLabel,
               elseBranchValueLabel);
         }

      @Override
      protected Boolean convertRawValueToBoolean(@NotNull final Object rawValue)
         {
         return checkOrientation((AccelerometerGs)rawValue);
         }

      protected abstract boolean checkOrientation(@NotNull final AccelerometerGs gs);
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
