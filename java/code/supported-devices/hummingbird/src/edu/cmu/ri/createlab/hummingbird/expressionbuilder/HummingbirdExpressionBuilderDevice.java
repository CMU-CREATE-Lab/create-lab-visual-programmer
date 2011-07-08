package edu.cmu.ri.createlab.hummingbird.expressionbuilder;

import edu.cmu.ri.createlab.expressionbuilder.ExpressionBuilderDevice;
import edu.cmu.ri.createlab.expressionbuilder.controlpanel.DeviceGUI;
import edu.cmu.ri.createlab.hummingbird.expressionbuilder.controlpanel.HummingbirdGUI;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public class HummingbirdExpressionBuilderDevice implements ExpressionBuilderDevice
   {
   private final DeviceGUI deviceGUI = new HummingbirdGUI();

   @Override
   public DeviceGUI getDeviceGUI()
      {
      return deviceGUI;
      }
   }
