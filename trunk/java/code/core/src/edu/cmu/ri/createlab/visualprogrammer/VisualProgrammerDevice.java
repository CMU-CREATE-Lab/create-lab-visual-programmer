package edu.cmu.ri.createlab.visualprogrammer;

import edu.cmu.ri.createlab.device.CreateLabDeviceProxy;
import edu.cmu.ri.createlab.expressionbuilder.ExpressionBuilderDevice;
import edu.cmu.ri.createlab.terk.services.ServiceManager;

/**
 * <p>
 * <code>VisualProgrammerDevice</code> defines methods required by any device that is to be controlled by the
 * Visual Programmer.
 * </p>
 *
 * @author Chris Bartley (bartley@cmu.edu)
 */
public interface VisualProgrammerDevice<ProxyClass extends CreateLabDeviceProxy>
   {
   /** Returns a "pretty name" for the device, for example one which could be shown to an end user. */
   String getDeviceName();

   /** Connects to the device.  This method must block until the connection is complete. */
   void connect();

   /** Returns whether the device is currently connected */
   boolean isConnected();

   /** Returns the device's {@link CreateLabDeviceProxy}, or <code>null</code> if the device isn't connected. */
   ProxyClass getDeviceProxy();

   /** Returns the device's {@link ServiceManager}, or <code>null</code> if the device isn't connected. */
   ServiceManager getServiceManager();

   ExpressionBuilderDevice getExpressionBuilderDevice();

   /** Disconnects from the device. */
   void disconnect();
   }