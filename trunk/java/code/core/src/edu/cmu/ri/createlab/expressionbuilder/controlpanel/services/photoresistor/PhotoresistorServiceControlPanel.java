package edu.cmu.ri.createlab.expressionbuilder.controlpanel.services.photoresistor;

import java.awt.Color;
import java.awt.Component;
import java.util.Map;
import java.util.PropertyResourceBundle;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import edu.cmu.ri.createlab.expressionbuilder.controlpanel.AbstractServiceControlPanel;
import edu.cmu.ri.createlab.expressionbuilder.controlpanel.AbstractServiceControlPanelDevice;
import edu.cmu.ri.createlab.expressionbuilder.controlpanel.ControlPanelManager;
import edu.cmu.ri.createlab.expressionbuilder.controlpanel.ServiceControlPanelDevice;
import edu.cmu.ri.createlab.terk.expression.XmlParameter;
import edu.cmu.ri.createlab.terk.services.Service;
import edu.cmu.ri.createlab.terk.services.photoresistor.PhotoresistorService;
import edu.cmu.ri.createlab.userinterface.GUIConstants;
import edu.cmu.ri.createlab.userinterface.util.SwingUtils;
import org.apache.log4j.Logger;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public final class PhotoresistorServiceControlPanel extends AbstractServiceControlPanel
   {
   private static final Logger LOG = Logger.getLogger(PhotoresistorServiceControlPanel.class);

   private static final PropertyResourceBundle RESOURCES = (PropertyResourceBundle)PropertyResourceBundle.getBundle(PhotoresistorServiceControlPanel.class.getName());

   private final PhotoresistorService service;

   public PhotoresistorServiceControlPanel(final ControlPanelManager controlPanelManager, final PhotoresistorService service)
      {
      super(controlPanelManager, service);
      this.service = service;

      final Timer pollingTimer = new Timer("PhotoresistorPollingTimer", true);
      pollingTimer.scheduleAtFixedRate(
            new TimerTask()
            {
            public void run()
               {
               updatePhotoresistors();
               }
            },
            0,
            500);
      }

   private void updatePhotoresistors()
      {
      // todo: this is inefficient if there is more than one device.  It'd be better to fetch all the states
      // at once with a single call to the service
      for (int i = 0; i < service.getDeviceCount(); i++)
         {
         final ServiceControlPanelDevice device = getDeviceById(i);
         if (device != null && device.isActive())
            {
            try
               {
               final Integer state = service.getPhotoresistorValue(i);
               if (state != null)
                  {
                  ((ControlPanelDevice)device).setValue(state);
                  }
               }
            catch (Exception e)
               {
               LOG.error("Exception while trying to poll photoresistor input [" + i + "]", e);
               }
            }
         }
      }

   public String getDisplayName()
      {
      return RESOURCES.getString("control-panel.title");
      }

   public String getShortDisplayName()
      {
      return RESOURCES.getString("control-panel.short-title");
      }

   public void refresh()
      {
      LOG.debug("PhotoresistorServiceControlPanel.refresh()");

      updatePhotoresistors();
      }

   protected ServiceControlPanelDevice createServiceControlPanelDevice(final Service service, final int deviceIndex)
      {
      return new ControlPanelDevice(deviceIndex);
      }

   private final class ControlPanelDevice extends AbstractServiceControlPanelDevice
      {
      private static final int ACTUAL_MIN_VALUE = 0;
      private static final int ACTUAL_MAX_VALUE = 255;
      private static final int DISPLAY_MIN_VALUE = 0;
      private static final int DISPLAY_MAX_VALUE = 255;
      private static final String DISPLAY_INITIAL_VALUE = "255";

      private final JPanel panel = new JPanel();
      private final JTextField valueTextField = new JTextField(DISPLAY_INITIAL_VALUE, DISPLAY_INITIAL_VALUE.length());

      private ControlPanelDevice(final int deviceIndex)
         {
         super(deviceIndex);

         valueTextField.setFont(GUIConstants.FONT_NORMAL);
         valueTextField.setEditable(false);
         valueTextField.setMaximumSize(valueTextField.getPreferredSize());
         valueTextField.setMinimumSize(valueTextField.getPreferredSize());

         // layout
         panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
         panel.add(SwingUtils.createLabel(String.valueOf(deviceIndex + 1)));
         panel.add(SwingUtils.createRigidSpacer());
         panel.add(valueTextField);
         panel.setBackground(Color.WHITE);
         }

      private void setValue(final int value)
         {
         SwingUtilities.invokeLater(
               new Runnable()
               {
               public void run()
                  {
                  valueTextField.setText(String.valueOf(scaleToDisplay(value)));
                  }
               });
         }

      public Component getComponent()
         {
         return panel;
         }

      public Component getBlockIcon()
         {
         JPanel icon = new JPanel();
         return icon;
         }

      public void getFocus()
        {
            valueTextField.requestFocus();
            //TODO: Placeholder
        }

      public boolean execute(final String operationName, final Map<String, String> parameterMap)
         {
         return false;
         }

      public String getCurrentOperationName()
         {
         return null;
         }

      public Set<XmlParameter> buildParameters()
         {
         return null;
         }

      private int scaleToDisplay(final int value)
         {
         return scaleValue(value, ACTUAL_MIN_VALUE, ACTUAL_MAX_VALUE, DISPLAY_MIN_VALUE, DISPLAY_MAX_VALUE);
         }
      }
   }