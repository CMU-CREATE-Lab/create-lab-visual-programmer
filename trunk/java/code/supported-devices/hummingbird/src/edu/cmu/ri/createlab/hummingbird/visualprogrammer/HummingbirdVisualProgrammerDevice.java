package edu.cmu.ri.createlab.hummingbird.visualprogrammer;

import java.util.PropertyResourceBundle;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import edu.cmu.ri.createlab.device.CreateLabDevicePingFailureEventListener;
import edu.cmu.ri.createlab.expressionbuilder.ExpressionBuilderDevice;
import edu.cmu.ri.createlab.hummingbird.Hummingbird;
import edu.cmu.ri.createlab.hummingbird.HummingbirdFactory;
import edu.cmu.ri.createlab.hummingbird.expressionbuilder.HummingbirdExpressionBuilderDevice;
import edu.cmu.ri.createlab.hummingbird.services.HummingbirdServiceManager;
import edu.cmu.ri.createlab.terk.services.ServiceManager;
import edu.cmu.ri.createlab.visualprogrammer.VisualProgrammerDevice;
import org.apache.log4j.Logger;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public final class HummingbirdVisualProgrammerDevice implements VisualProgrammerDevice<Hummingbird>
   {
   private static final Logger LOG = Logger.getLogger(HummingbirdVisualProgrammerDevice.class);

   private static final PropertyResourceBundle RESOURCES = (PropertyResourceBundle)PropertyResourceBundle.getBundle(HummingbirdVisualProgrammerDevice.class.getName());

   private Hummingbird hummingbird = null;
   private ServiceManager serviceManager = null;
   private final ExpressionBuilderDevice expressionBuilderDevice = new HummingbirdExpressionBuilderDevice();

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
   }
