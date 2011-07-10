package edu.cmu.ri.createlab.visualprogrammer;

import java.util.SortedSet;
import edu.cmu.ri.createlab.device.CreateLabDeviceProxy;
import edu.cmu.ri.createlab.expressionbuilder.ExpressionBuilderDevice;
import edu.cmu.ri.createlab.sequencebuilder.programelement.model.LoopableConditionalModel;
import edu.cmu.ri.createlab.terk.services.ServiceManager;
import org.jetbrains.annotations.NotNull;

/**
 * <p>
 * <code>VisualProgrammerDevice</code> defines methods required by any device that is to be controlled by the
 * Visual Programmer.
 * </p>
 *
 * @author Chris Bartley (bartley@cmu.edu)
 */
public interface VisualProgrammerDevice
   {
   /** Returns a "pretty name" for the device, for example one which could be shown to an end user. */
   String getDeviceName();

   /** Connects to the device.  This method must block until the connection is complete. */
   void connect();

   /** Returns whether the device is currently connected */
   boolean isConnected();

   /** Returns the device's {@link CreateLabDeviceProxy}, or <code>null</code> if the device isn't connected. */
   CreateLabDeviceProxy getDeviceProxy();

   /** Returns the device's {@link ServiceManager}, or <code>null</code> if the device isn't connected. */
   ServiceManager getServiceManager();

   ExpressionBuilderDevice getExpressionBuilderDevice();

   /**
    * Returns the an unmodifiable {@link SortedSet} of {@link LoopableConditionalModel.SensorType}s supported by this device.  Guaranteed to not
    * return <code>null</code>, but may return an empty set.  Will return an empty set if there are no sensor types, or
    * if this method is called before a connection has been established.
    */
   @NotNull
   SortedSet<LoopableConditionalModel.SensorType> getSensorTypes();

   /** Disconnects from the device. */
   void disconnect();
   }