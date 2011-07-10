package edu.cmu.ri.createlab.hummingbird.visualprogrammer;

import java.util.Collections;
import java.util.PropertyResourceBundle;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import edu.cmu.ri.createlab.device.CreateLabDevicePingFailureEventListener;
import edu.cmu.ri.createlab.expressionbuilder.ExpressionBuilderDevice;
import edu.cmu.ri.createlab.hummingbird.Hummingbird;
import edu.cmu.ri.createlab.hummingbird.HummingbirdFactory;
import edu.cmu.ri.createlab.hummingbird.expressionbuilder.HummingbirdExpressionBuilderDevice;
import edu.cmu.ri.createlab.hummingbird.services.HummingbirdServiceManager;
import edu.cmu.ri.createlab.terk.TerkConstants;
import edu.cmu.ri.createlab.terk.services.Service;
import edu.cmu.ri.createlab.terk.services.ServiceManager;
import edu.cmu.ri.createlab.terk.services.analog.AnalogInputsService;
import edu.cmu.ri.createlab.visualprogrammer.Sensor;
import edu.cmu.ri.createlab.visualprogrammer.SensorImpl;
import edu.cmu.ri.createlab.visualprogrammer.VisualProgrammerDevice;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public final class HummingbirdVisualProgrammerDevice implements VisualProgrammerDevice
   {
   private static final Logger LOG = Logger.getLogger(HummingbirdVisualProgrammerDevice.class);

   private static final PropertyResourceBundle RESOURCES = (PropertyResourceBundle)PropertyResourceBundle.getBundle(HummingbirdVisualProgrammerDevice.class.getName());

   private Hummingbird hummingbird = null;
   private ServiceManager serviceManager = null;
   private final ExpressionBuilderDevice expressionBuilderDevice = new HummingbirdExpressionBuilderDevice();
   private final SortedSet<Sensor> sensors = new TreeSet<Sensor>();

   private final Lock lock = new ReentrantLock();

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
                     hummingbird = null;
                     serviceManager = null;
                     }
                  }
            );
            serviceManager = new HummingbirdServiceManager(hummingbird);

            // Build the set of sensor types.  First get the min and max allowed values from the AnalogInputsService
            sensors.clear();

            int minValue = 0;
            int maxValue = 0;
            int numPorts = 0;
            final Service analogInputsService = serviceManager.getServiceByTypeId(AnalogInputsService.TYPE_ID);
            if (analogInputsService != null)
               {
               final Integer minValueInteger = analogInputsService.getPropertyAsInteger(AnalogInputsService.PROPERTY_NAME_MIN_VALUE);
               final Integer maxValueInteger = analogInputsService.getPropertyAsInteger(AnalogInputsService.PROPERTY_NAME_MAX_VALUE);
               final Integer numPortsInteger = analogInputsService.getPropertyAsInteger(TerkConstants.PropertyKeys.DEVICE_COUNT);

               if (minValueInteger != null)
                  {
                  minValue = minValueInteger;
                  }
               if (maxValueInteger != null)
                  {
                  maxValue = maxValueInteger;
                  }
               if (numPortsInteger != null)
                  {
                  numPorts = numPortsInteger;
                  }
               }

            if (numPorts > 0)
               {
               // add the Light Sensor
               sensors.add(new AnalogInputSensor(RESOURCES.getString("sensor.light.name"),
                                                 numPorts,
                                                 minValue,
                                                 maxValue,
                                                 RESOURCES.getString("sensor.light.if-branch.label"),
                                                 RESOURCES.getString("sensor.light.else-branch.label")));

               // add the Distance Sensor
               sensors.add(new AnalogInputSensor(RESOURCES.getString("sensor.distance.name"),
                                                 numPorts,
                                                 minValue,
                                                 maxValue,
                                                 RESOURCES.getString("sensor.distance.if-branch.label"),
                                                 RESOURCES.getString("sensor.distance.else-branch.label")));
               }
            }
         }
      catch (final Exception e)
         {
         LOG.error("Exception caught while trying to create the Hummingbird or the HummingbirdServiceManager.", e);
         disconnectWorkhorse();
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
      return expressionBuilderDevice;
      }

   @Override
   @NotNull
   public SortedSet<Sensor> getSensors()
      {
      lock.lock();  // block until condition holds
      try
         {
         return Collections.unmodifiableSortedSet(sensors);
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
         disconnectWorkhorse();
         }
      finally
         {
         lock.unlock();
         }
      }

   private void disconnectWorkhorse()
      {
      if (hummingbird != null)
         {
         hummingbird.disconnect();
         }
      hummingbird = null;
      serviceManager = null;
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
      }
   }
