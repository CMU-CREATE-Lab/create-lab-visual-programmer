package edu.cmu.ri.createlab.expressionbuilder.controlpanel.services.motor;

import edu.cmu.ri.createlab.expressionbuilder.controlpanel.ControlPanelManager;
import edu.cmu.ri.createlab.expressionbuilder.controlpanel.ServiceControlPanelDevice;
import edu.cmu.ri.createlab.terk.services.motor.VelocityControllableMotorService;
import org.apache.log4j.Logger;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public final class VelocityControllableMotorServiceControlPanel extends BaseVelocityControllableMotorServiceControlPanel
   {
   private static final Logger LOG = Logger.getLogger(VelocityControllableMotorServiceControlPanel.class);

   private final VelocityControllableMotorService service;

   public VelocityControllableMotorServiceControlPanel(final ControlPanelManager controlPanelManager, final VelocityControllableMotorService service)
      {
      super(controlPanelManager,
            service,
            VelocityControllableMotorService.PROPERTY_NAME_MIN_VELOCITY,
            VelocityControllableMotorService.PROPERTY_NAME_MAX_VELOCITY);
      this.service = service;
      }

   @Override
   public void refresh()
      {
      LOG.debug("VelocityControllableMotorServiceControlPanel.refresh()");

      // get the current state
      final int[] velocities = service.getVelocities();

      for (final ServiceControlPanelDevice device : getDevices())
         {
         if (device.getDeviceIndex() >= 0 && device.getDeviceIndex() < velocities.length)
            {
            ((ControlPanelDevice)device).updateGUI(velocities[device.getDeviceIndex()]);
            }
         }
      }

   @Override
   protected void setVelocity(final int motorId, final int velocity)
      {
      service.setVelocity(motorId, velocity);
      }
   }