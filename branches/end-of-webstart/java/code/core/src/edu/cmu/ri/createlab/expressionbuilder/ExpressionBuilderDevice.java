package edu.cmu.ri.createlab.expressionbuilder;

import edu.cmu.ri.createlab.expressionbuilder.controlpanel.DeviceGUI;

/**
 * <p>
 * <code>ExpressionBuilderDevice</code> defines methods required by any device that is to be controlled by the
 * Expression Builder.
 * </p>
 *
 * @author Chris Bartley (bartley@cmu.edu)
 */
public interface ExpressionBuilderDevice
   {
   /** Returns the {@link DeviceGUI} */
   DeviceGUI getDeviceGUI();
   }
