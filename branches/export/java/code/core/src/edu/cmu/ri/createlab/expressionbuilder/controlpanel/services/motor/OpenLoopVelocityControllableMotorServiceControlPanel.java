package edu.cmu.ri.createlab.expressionbuilder.controlpanel.services.motor;

import edu.cmu.ri.createlab.expressionbuilder.controlpanel.ControlPanelManager;
import edu.cmu.ri.createlab.expressionbuilder.controlpanel.ServiceControlPanelDevice;
import edu.cmu.ri.createlab.terk.services.motor.OpenLoopVelocityControllableMotorService;
import org.apache.log4j.Logger;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public final class OpenLoopVelocityControllableMotorServiceControlPanel extends BaseVelocityControllableMotorServiceControlPanel
   {
   private static final Logger LOG = Logger.getLogger(OpenLoopVelocityControllableMotorServiceControlPanel.class);

   private final OpenLoopVelocityControllableMotorService service;

   public OpenLoopVelocityControllableMotorServiceControlPanel(final ControlPanelManager controlPanelManager, final OpenLoopVelocityControllableMotorService service)
      {
      super(controlPanelManager,
            service,
            OpenLoopVelocityControllableMotorService.PROPERTY_NAME_MIN_VELOCITY,
            OpenLoopVelocityControllableMotorService.PROPERTY_NAME_MAX_VELOCITY);
      this.service = service;
      }

   @Override
   public void refresh()
      {
      LOG.debug("OpenLoopVelocityControllableMotorServiceControlPanel.refresh()");

      // We can't get the current state since the service doesn't support it, so just set everything to 0
      for (final ServiceControlPanelDevice device : getDevices())
         {
         ((ControlPanelDevice)device).updateGUI(0);
         }
      }

   @Override
   protected void setVelocity(final int motorId, final int velocity)
      {
      service.setVelocity(motorId, velocity);
      }
   }