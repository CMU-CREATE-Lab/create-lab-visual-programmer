package edu.cmu.ri.createlab.expressionbuilder.controlpanel;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public interface ControlPanelManagerEventPublisher
   {
   void addControlPanelManagerEventListener(final ControlPanelManagerEventListener listener);

   void removeControlPanelManagerEventListener(final ControlPanelManagerEventListener listener);
   }