package edu.cmu.ri.createlab.finch.expressionbuilder;

import edu.cmu.ri.createlab.expressionbuilder.ExpressionBuilderDevice;
import edu.cmu.ri.createlab.expressionbuilder.controlpanel.DeviceGUI;
import edu.cmu.ri.createlab.finch.expressionbuilder.controlpanel.FinchGUI;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public final class FinchExpressionBuilderDevice implements ExpressionBuilderDevice
   {
   private final DeviceGUI deviceGUI = new FinchGUI();

   @Override
   public DeviceGUI getDeviceGUI()
      {
      return deviceGUI;
      }
   }
