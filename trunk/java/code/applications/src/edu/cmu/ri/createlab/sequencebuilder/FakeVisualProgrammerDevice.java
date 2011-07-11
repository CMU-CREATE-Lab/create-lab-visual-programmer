package edu.cmu.ri.createlab.sequencebuilder;

import java.util.Collections;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import edu.cmu.ri.createlab.device.CreateLabDevicePingFailureEventListener;
import edu.cmu.ri.createlab.device.CreateLabDeviceProxy;
import edu.cmu.ri.createlab.expressionbuilder.ExpressionBuilderDevice;
import edu.cmu.ri.createlab.terk.services.Service;
import edu.cmu.ri.createlab.terk.services.ServiceManager;
import edu.cmu.ri.createlab.visualprogrammer.Sensor;
import edu.cmu.ri.createlab.visualprogrammer.SensorImpl;
import edu.cmu.ri.createlab.visualprogrammer.VisualProgrammerDevice;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
final class FakeVisualProgrammerDevice implements VisualProgrammerDevice
   {
   private static final Logger LOG = Logger.getLogger(FakeVisualProgrammerDevice.class);

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
            LOG.debug("FakeVisualProgrammerDevice.disconnect()");
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
   private final ServiceManager fakeServiceManager =
         new ServiceManager()
         {
         @Override
         public boolean isServiceSupported(final String typeId)
            {
            return false;
            }

         @Override
         public Service getServiceByTypeId(final String typeId)
            {
            return null;
            }

         @Override
         public Set<String> getTypeIdsOfSupportedServices()
            {
            return null;
            }
         };
   private final SortedMap<String, Sensor> sensorMap = new TreeMap<String, Sensor>();

   FakeVisualProgrammerDevice()
      {
      final SensorImpl sensor1 = new SensorImpl("Fake Sensor",
                                                "FakeSensorServiceTypeId",
                                                "fakeOperation",
                                                5,
                                                0,
                                                255,
                                                "Min",
                                                "Max");
      final SensorImpl sensor2 = new SensorImpl("Bogus Sensor",
                                                "FakeSensorServiceTypeId",
                                                "fakeOperation",
                                                2,
                                                0,
                                                100,
                                                "Low",
                                                "High");
      sensorMap.put(createMapKey(sensor1), sensor1);
      sensorMap.put(createMapKey(sensor2), sensor2);
      }

   @Override
   public String getDeviceName()
      {
      return "Fake Device";
      }

   @Override
   public void connect()
      {
      LOG.debug("FakeVisualProgrammerDevice.connect()");
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
   public ServiceManager getServiceManager()
      {
      return fakeServiceManager;
      }

   @Override
   public ExpressionBuilderDevice getExpressionBuilderDevice()
      {
      throw new UnsupportedOperationException("");
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
   public void disconnect()
      {
      LOG.debug("FakeVisualProgrammerDevice.disconnect()");
      isConnected = false;
      }
   }
