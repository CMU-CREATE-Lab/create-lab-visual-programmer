package edu.cmu.ri.createlab.expressionbuilder.controlpanel;

import java.util.Map;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public interface ControlPanelManagerEventListener
   {
   void handleDeviceConnectedEvent(final Map<String, ServiceControlPanel> serviceControlPanelMap);

   void handleDeviceDisconnectedEvent();

   void handleDeviceActivityStatusChange(final String serviceTypeId, final int deviceIndex, final AbstractServiceControlPanel.ActivityLevels active);
   }