package edu.cmu.ri.createlab.expressionbuilder.controlpanel;

import edu.cmu.ri.createlab.terk.expression.XmlDevice;
import org.apache.log4j.Logger;


/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public abstract class AbstractServiceControlPanelDevice implements ServiceControlPanelDevice
   {

   private static final Logger LOG = Logger.getLogger(AbstractServiceControlPanelDevice.class);

   protected boolean isActive = false;
   private final int deviceIndex;

   protected AbstractServiceControlPanelDevice(final int deviceIndex)
      {
      this.deviceIndex = deviceIndex;
      }

   public int getDeviceIndex()
      {
      return deviceIndex;
      }

   public final boolean isActive()
      {
      return isActive;
      }

   public final void setActive(final boolean isActive)
      {
      this.isActive = isActive;
      if(isActive){
          getFocus();

          //LOG.debug("Focus requested by: " + this);
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
