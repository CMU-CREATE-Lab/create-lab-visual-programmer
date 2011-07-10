package edu.cmu.ri.createlab.sequencebuilder;

import java.util.Collections;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import edu.cmu.ri.createlab.device.CreateLabDevicePingFailureEventListener;
import edu.cmu.ri.createlab.device.CreateLabDeviceProxy;
import edu.cmu.ri.createlab.expressionbuilder.ExpressionBuilderDevice;
import edu.cmu.ri.createlab.sequencebuilder.programelement.model.LoopableConditionalModel;
import edu.cmu.ri.createlab.terk.services.Service;
import edu.cmu.ri.createlab.terk.services.ServiceManager;
import edu.cmu.ri.createlab.visualprogrammer.VisualProgrammerDevice;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
final class FakeVisualProgrammerDevice implements VisualProgrammerDevice
   {
   private static final Logger LOG = Logger.getLogger(FakeVisualProgrammerDevice.class);

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
   private final SortedSet<LoopableConditionalModel.SensorType> sensorTypes = new TreeSet<LoopableConditionalModel.SensorType>();

   FakeVisualProgrammerDevice()
      {
      sensorTypes.add(new LoopableConditionalModel.SensorType("Fake Sensor",
                                                              "FakeSensorServiceTypeId",
                                                              5,
                                                              0,
                                                              255,
                                                              "Min",
                                                              "Max"));
      sensorTypes.add(new LoopableConditionalModel.SensorType("Bogus Sensor",
                                                              "FakeSensorServiceTypeId",
                                                              2,
                                                              0,
                                                              100,
                                                              "Low",
                                                              "High"));
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
   public SortedSet<LoopableConditionalModel.SensorType> getSensorTypes()
      {
      return Collections.unmodifiableSortedSet(sensorTypes);
      }

   @Override
   public void disconnect()
      {
      LOG.debug("FakeVisualProgrammerDevice.disconnect()");
      isConnected = false;
      }
   }
