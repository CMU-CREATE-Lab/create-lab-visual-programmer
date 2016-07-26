package edu.cmu.ri.createlab.expressionbuilder.controlpanel;

import edu.cmu.ri.createlab.terk.xml.XmlDevice;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public abstract class AbstractServiceControlPanelDevice implements ServiceControlPanelDevice
   {
   protected AbstractServiceControlPanel.ActivityLevels isActive = AbstractServiceControlPanel.ActivityLevels.STAY;
   private final int deviceIndex;

   protected AbstractServiceControlPanelDevice(final int deviceIndex)
      {
      this.deviceIndex = deviceIndex;
      }

   public int getDeviceIndex()
      {
      return deviceIndex;
      }

   public final AbstractServiceControlPanel.ActivityLevels isActive()
      {
      return isActive;
      }

   public final void setActive(final AbstractServiceControlPanel.ActivityLevels isActive)
      {
      this.isActive = isActive;
      //TODO: Change1
      if (isActive != AbstractServiceControlPanel.ActivityLevels.STAY)
         {
         getFocus();
         }
      updateComponent();
      updateBlockIcon();
      }

   public abstract void getFocus();

   public final XmlDevice buildDevice()
      {
      return new XmlDevice(deviceIndex, buildParameters());
      }
   }
