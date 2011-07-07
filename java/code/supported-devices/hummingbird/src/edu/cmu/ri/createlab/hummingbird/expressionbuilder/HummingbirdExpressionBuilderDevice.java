package edu.cmu.ri.createlab.hummingbird.expressionbuilder;

import java.util.PropertyResourceBundle;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import javax.swing.JTextField;
import edu.cmu.ri.createlab.device.CreateLabDevicePingFailureEventListener;
import edu.cmu.ri.createlab.expressionbuilder.ExpressionBuilderDevice;
import edu.cmu.ri.createlab.expressionbuilder.controlpanel.DeviceGUI;
import edu.cmu.ri.createlab.hummingbird.Hummingbird;
import edu.cmu.ri.createlab.hummingbird.HummingbirdFactory;
import edu.cmu.ri.createlab.hummingbird.expressionbuilder.controlpanel.HummingbirdGUI;
import edu.cmu.ri.createlab.hummingbird.services.HummingbirdServiceManager;
import edu.cmu.ri.createlab.terk.services.ServiceManager;
import org.apache.log4j.Logger;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public final class HummingbirdExpressionBuilderDevice implements ExpressionBuilderDevice<Hummingbird>
   {
   private static final Logger LOG = Logger.getLogger(HummingbirdExpressionBuilderDevice.class);

   private static final PropertyResourceBundle RESOURCES = (PropertyResourceBundle)PropertyResourceBundle.getBundle(HummingbirdExpressionBuilderDevice.class.getName());

   private Hummingbird hummingbird = null;
   private ServiceManager serviceManager = null;
   private final DeviceGUI deviceGUI = new HummingbirdGUI();

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
   public DeviceGUI getDeviceGUI()
      {
      return deviceGUI;
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

   public void setStageTitleField(JTextField title)
      {
      deviceGUI.setStageTitleField(title);
      }
   }
