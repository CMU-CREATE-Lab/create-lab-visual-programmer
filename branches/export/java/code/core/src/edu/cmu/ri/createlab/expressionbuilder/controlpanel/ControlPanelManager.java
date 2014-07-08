package edu.cmu.ri.createlab.expressionbuilder.controlpanel;

import edu.cmu.ri.createlab.terk.expression.XmlExpression;
import edu.cmu.ri.createlab.terk.services.ServiceManager;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public interface ControlPanelManager extends ControlPanelManagerEventPublisher
   {
   void deviceConnected(ServiceManager serviceManager);

   void deviceDisconnected();

   void setDeviceActive(final String serviceTypeId, final int deviceIndex, final boolean isActive);

   void loadExpression(final XmlExpression expression);

   /**
    * Refreshes the control panels by calling {@link ServiceControlPanel#refresh()} on each.  Each call is performed in
    * a separate thread.
    */
   void refresh();

   /** Resets all control panels. */
   void reset();

   /** Resets the given control panel. */
   void reset(final ServiceControlPanel serviceControlPanel);

   XmlExpression buildExpression();
   }