package edu.cmu.ri.createlab.finch.expressionbuilder;

import java.util.PropertyResourceBundle;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import javax.swing.JTextField;
import edu.cmu.ri.createlab.device.CreateLabDevicePingFailureEventListener;
import edu.cmu.ri.createlab.expressionbuilder.ExpressionBuilderDevice;
import edu.cmu.ri.createlab.expressionbuilder.controlpanel.DeviceGUI;
import edu.cmu.ri.createlab.finch.expressionbuilder.controlpanel.FinchGUI;
import edu.cmu.ri.createlab.terk.robot.finch.DefaultFinchController;
import edu.cmu.ri.createlab.terk.robot.finch.FinchController;
import edu.cmu.ri.createlab.terk.robot.finch.services.FinchServiceManager;
import edu.cmu.ri.createlab.terk.services.ServiceManager;
import org.apache.log4j.Logger;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public final class FinchExpressionBuilderDevice implements ExpressionBuilderDevice<FinchController>
   {
   private static final Logger LOG = Logger.getLogger(FinchExpressionBuilderDevice.class);

   private static final PropertyResourceBundle RESOURCES = (PropertyResourceBundle)PropertyResourceBundle.getBundle(FinchExpressionBuilderDevice.class.getName());

   private FinchController finch = null;
   private ServiceManager serviceManager = null;
   private final DeviceGUI deviceGUI = new FinchGUI();

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
      if (finch != null)
         {
         finch.disconnect();
         }
      finch = null;
      serviceManager = null;
      }

   public void setStageTitleField(JTextField title)
      {
      deviceGUI.setStageTitleField(title);
      }
   }
