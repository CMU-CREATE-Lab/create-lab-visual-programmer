package edu.cmu.ri.createlab.finch.visualprogrammer;

import java.util.Collections;
import java.util.PropertyResourceBundle;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import edu.cmu.ri.createlab.device.CreateLabDevicePingFailureEventListener;
import edu.cmu.ri.createlab.expressionbuilder.ExpressionBuilderDevice;
import edu.cmu.ri.createlab.finch.expressionbuilder.FinchExpressionBuilderDevice;
import edu.cmu.ri.createlab.terk.robot.finch.DefaultFinchController;
import edu.cmu.ri.createlab.terk.robot.finch.FinchController;
import edu.cmu.ri.createlab.terk.robot.finch.services.FinchServiceManager;
import edu.cmu.ri.createlab.terk.services.ServiceManager;
import edu.cmu.ri.createlab.visualprogrammer.Sensor;
import edu.cmu.ri.createlab.visualprogrammer.VisualProgrammerDevice;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public final class FinchVisualProgrammerDevice implements VisualProgrammerDevice
   {
   private static final Logger LOG = Logger.getLogger(FinchVisualProgrammerDevice.class);

   private static final PropertyResourceBundle RESOURCES = (PropertyResourceBundle)PropertyResourceBundle.getBundle(FinchVisualProgrammerDevice.class.getName());

   private FinchController finch = null;
   private ServiceManager serviceManager = null;
   private final ExpressionBuilderDevice expressionBuilderDevice = new FinchExpressionBuilderDevice();
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
         finch = DefaultFinchController.create();
         if (finch != null)
            {
            finch.addCreateLabDevicePingFailureEventListener(
                  new CreateLabDevicePingFailureEventListener()
                  {
                  @Override
                  public void handlePingFailureEvent()
                     {
                     finch = null;
                     serviceManager = null;
                     }
                  }
            );
            serviceManager = new FinchServiceManager(finch);

            // Build the set of sensor types.  First get the min and max allowed values from the AnalogInputsService
            sensors.clear();
            // TODO: build the set of Sensors (see HummingbirdVisualProgrammerDevice for an example..)

            }
         }
      catch (final Exception e)
         {
         LOG.error("Exception caught while trying to create the Finch or the FinchServiceManager.", e);
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
         return finch != null && serviceManager != null;
         }
      finally
         {
         lock.unlock();
         }
      }

   @Override
   public FinchController getDeviceProxy()
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
   @Nullable
   public Sensor findSensor(@Nullable final String sensorName, @Nullable final String serviceTypeId)
      {
      // TODO: implement me
      return null;
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
      if (finch != null)
         {
         finch.disconnect();
         }
      finch = null;
      serviceManager = null;
      }
   }
