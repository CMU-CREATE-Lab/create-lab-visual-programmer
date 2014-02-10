package edu.cmu.ri.createlab.sequencebuilder;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import javax.swing.ImageIcon;
import edu.cmu.ri.createlab.device.CreateLabDevicePingFailureEventListener;
import edu.cmu.ri.createlab.device.CreateLabDeviceProxy;
import edu.cmu.ri.createlab.expressionbuilder.ExpressionBuilderDevice;
import edu.cmu.ri.createlab.terk.TerkConstants;
import edu.cmu.ri.createlab.terk.properties.BasicPropertyManager;
import edu.cmu.ri.createlab.terk.services.Service;
import edu.cmu.ri.createlab.terk.services.ServiceManager;
import edu.cmu.ri.createlab.terk.services.analog.AnalogInputsService;
import edu.cmu.ri.createlab.terk.services.analog.BaseAnalogInputsServiceImpl;
import edu.cmu.ri.createlab.userinterface.util.ImageUtils;
import edu.cmu.ri.createlab.visualprogrammer.IntegralValueSensor;
import edu.cmu.ri.createlab.visualprogrammer.Sensor;
import edu.cmu.ri.createlab.visualprogrammer.VisualProgrammerDevice;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
final class FakeHummingbirdDevice implements VisualProgrammerDevice
   {
   private static final Logger LOG = Logger.getLogger(FakeHummingbirdDevice.class);

   private static String createMapKey(@NotNull final Sensor sensor)
      {
      return createMapKey(sensor.getName(), sensor.getServiceTypeId());
      }

   private static String createMapKey(final String sensorName, final String serviceTypeId)
      {
      return sensorName + "|" + serviceTypeId;
      }

   private boolean isConnected = false;
   private final CreateLabDeviceProxy fakeProxy =
         new CreateLabDeviceProxy()
         {
         @Override
         public String getPortName()
            {
            return "FakePort";
            }

         @Override
         public void disconnect()
            {
            LOG.debug("FakeHummingbirdDevice.disconnect()");
            }

         @Override
         public void addCreateLabDevicePingFailureEventListener(final CreateLabDevicePingFailureEventListener listener)
            {
            }

         @Override
         public void removeCreateLabDevicePingFailureEventListener(final CreateLabDevicePingFailureEventListener listener)
            {
            }
         };
   private final Set<String> supportedServicesSet = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(AnalogInputsService.TYPE_ID)));
   private final FakeAnalogInputsService fakeAnalogInputsService = FakeAnalogInputsService.create();
   private final ServiceManager fakeServiceManager =
         new ServiceManager()
         {
         @Override
         public boolean isServiceSupported(final String typeId)
            {
            return supportedServicesSet.contains(typeId);
            }

         @Override
         public Service getServiceByTypeId(final String typeId)
            {
            if (AnalogInputsService.TYPE_ID.equals(typeId))
               {
               return fakeAnalogInputsService;
               }
            return null;
            }

         @Override
         public Set<String> getTypeIdsOfSupportedServices()
            {
            return supportedServicesSet;
            }
         };
   private final SortedMap<String, Sensor> sensorMap = new TreeMap<String, Sensor>();

   FakeHummingbirdDevice()
      {
      final Sensor sensor1 = new IntegralValueSensor("Light Sensor",
                                                     AnalogInputsService.TYPE_ID,
                                                     "getAnalogInputValue",
                                                     2,
                                                     0,
                                                     255,
                                                     "Darker",
                                                     "Brighter");
      final Sensor sensor2 = new IntegralValueSensor("Distance Sensor",
                                                     AnalogInputsService.TYPE_ID,
                                                     "getAnalogInputValue",
                                                     2,
                                                     0,
                                                     255,
                                                     "Near",
                                                     "Far");
      final Sensor sensor3 = new IntegralValueSensor("Fake Sensor",
                                                     AnalogInputsService.TYPE_ID,
                                                     "getAnalogInputValue",
                                                     5,
                                                     0,
                                                     255,
                                                     "Min",
                                                     "Max");
      final Sensor sensor4 = new IntegralValueSensor("Bogus Sensor",
                                                     AnalogInputsService.TYPE_ID,
                                                     "getAnalogInputValue",
                                                     2,
                                                     0,
                                                     100,
                                                     "Low",
                                                     "High");
      sensorMap.put(createMapKey(sensor1), sensor1);
      sensorMap.put(createMapKey(sensor2), sensor2);
      sensorMap.put(createMapKey(sensor3), sensor3);
      sensorMap.put(createMapKey(sensor4), sensor4);
      }

   @Override
   public String getDeviceName()
      {
      return "Fake Hummingbird";
      }

   @Nullable
   @Override
   public String getDeviceVersion()
      {
      return null;
      }

   @Override
   public ImageIcon getConnectingImage()
      {
      return ImageUtils.createImageIcon("/edu/cmu/ri/createlab/hummingbird/visualprogrammer/images/connection_animation.gif");
      }

   @Override
   public ImageIcon getConnectionTipsImage()
      {
      return ImageUtils.createImageIcon("/edu/cmu/ri/createlab/hummingbird/visualprogrammer/images/connection_tips.gif");
      }

   @Override
   public void connect()
      {
      LOG.debug("FakeHummingbirdDevice.connect()");
      isConnected = true;
      }

   @Override
   public boolean isConnected()
      {
      return isConnected;
      }

   @Override
   public CreateLabDeviceProxy getDeviceProxy()
      {
      return fakeProxy;
      }

   @Override
   @Nullable
   public ServiceManager getServiceManager()
      {
      return fakeServiceManager;
      }

   @Override
   @NotNull
   public ExpressionBuilderDevice getExpressionBuilderDevice()
      {
      throw new UnsupportedOperationException();
      }

   @Override
   @NotNull
   public SequenceBuilderDevice getSequenceBuilderDevice()
      {
      throw new UnsupportedOperationException();
      }

   @NotNull
   @Override
   public SortedSet<Sensor> getSensors()
      {
      return Collections.unmodifiableSortedSet(new TreeSet<Sensor>(sensorMap.values()));
      }

   @Override
   @Nullable
   public Sensor findSensor(@Nullable final String sensorName, @Nullable final String serviceTypeId)
      {
      return sensorMap.get(createMapKey(sensorName, serviceTypeId));
      }

   @Override
   public final void addSensorListener(@Nullable final SensorListener listener)
      {
      // TODO: implement me
      }

   @Override
   public final void removeSensorListener(@Nullable final SensorListener listener)
      {
      // TODO: implement me
      }

   @Override
   public void disconnect()
      {
      LOG.debug("FakeHummingbirdDevice.disconnect()");
      isConnected = false;
      }

   private static final class FakeAnalogInputsService extends BaseAnalogInputsServiceImpl
      {
      private static FakeAnalogInputsService create()
         {
         final BasicPropertyManager basicPropertyManager = new BasicPropertyManager();
         final int deviceCount = 2;

         basicPropertyManager.setReadOnlyProperty(TerkConstants.PropertyKeys.DEVICE_COUNT, deviceCount);
         basicPropertyManager.setReadOnlyProperty(AnalogInputsService.PROPERTY_NAME_MIN_VALUE, 0);
         basicPropertyManager.setReadOnlyProperty(AnalogInputsService.PROPERTY_NAME_MAX_VALUE, 255);

         return new FakeAnalogInputsService(basicPropertyManager,
                                            deviceCount);
         }

      private Random random = new Random(System.currentTimeMillis());

      private FakeAnalogInputsService(final BasicPropertyManager basicPropertyManager, final int deviceCount)
         {
         super(basicPropertyManager, deviceCount);
         }

      @Override
      public Integer getAnalogInputValue(final int analogInputPortId)
         {
         return getRandomValue();
         }

      @Override
      public int[] getAnalogInputValues()
         {
         return new int[]{getRandomValue(), getRandomValue()};
         }

      private int getRandomValue()
         {
         return random.nextInt(255);
         }
      }
   }
