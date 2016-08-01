package edu.cmu.ri.createlab.expressionbuilder.controlpanel.services.thermistor;

import java.awt.Color;
import java.awt.Component;
import java.text.DecimalFormat;
import java.util.Map;
import java.util.PropertyResourceBundle;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.SwingUtilities;
import edu.cmu.ri.createlab.expressionbuilder.controlpanel.AbstractServiceControlPanel;
import edu.cmu.ri.createlab.expressionbuilder.controlpanel.AbstractServiceControlPanelDevice;
import edu.cmu.ri.createlab.expressionbuilder.controlpanel.ControlPanelManager;
import edu.cmu.ri.createlab.expressionbuilder.controlpanel.ServiceControlPanelDevice;
import edu.cmu.ri.createlab.terk.services.Service;
import edu.cmu.ri.createlab.terk.services.thermistor.ThermistorService;
import edu.cmu.ri.createlab.terk.xml.XmlParameter;
import edu.cmu.ri.createlab.userinterface.GUIConstants;
import edu.cmu.ri.createlab.userinterface.util.SpringLayoutUtilities;
import edu.cmu.ri.createlab.userinterface.util.SwingUtils;
import org.apache.log4j.Logger;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public final class ThermistorServiceControlPanel extends AbstractServiceControlPanel
   {
   private static final Logger LOG = Logger.getLogger(ThermistorServiceControlPanel.class);

   private static final PropertyResourceBundle RESOURCES = (PropertyResourceBundle)PropertyResourceBundle.getBundle(ThermistorServiceControlPanel.class.getName());

   private final ThermistorService service;

   public ThermistorServiceControlPanel(final ControlPanelManager controlPanelManager, final ThermistorService service)
      {
      super(controlPanelManager, service);
      this.service = service;

      final Timer pollingTimer = new Timer("ThermistorPollingTimer", true);
      pollingTimer.scheduleAtFixedRate(
            new TimerTask()
               {
               public void run()
                  {
                  updateThermistors();
                  }
               },
            0,
            500);
      }

   private void updateThermistors()
      {
      // todo: this is inefficient if there is more than one device.  It'd be better to fetch all the states
      // at once with a single call to the service
      for (int i = 0; i < service.getDeviceCount(); i++)
         {
         final ServiceControlPanelDevice device = getDeviceById(i);
         if (device != null && (device.isActive() == ActivityLevels.SET))
            {
            try
               {
               final Integer state = service.getThermistorValue(i);
               if (state != null)
                  {
                  ((ControlPanelDevice)device).setValue(state);
                  }
               }
            catch (Exception e)
               {
               LOG.error("Exception while trying to poll thermistor input [" + i + "]", e);
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

   public String getSingleName()
      {
      return RESOURCES.getString("control-panel.name");
      }

   public void refresh()
      {
      LOG.debug("ThermistorServiceControlPanel.refresh()");

      updateThermistors();
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
      private static final String DISPLAY_INITIAL_VALUE = "0";
      private static final int NUM_CHARS_IN_TEXT_FIELDS = 7;

      private final JPanel panel = new JPanel();
      private final JTextField rawValueTextField = new JTextField(DISPLAY_INITIAL_VALUE, NUM_CHARS_IN_TEXT_FIELDS);
      private final JTextField tempValueTextField = new JTextField(DISPLAY_INITIAL_VALUE, NUM_CHARS_IN_TEXT_FIELDS);
      private final DecimalFormat decimalFormat = new DecimalFormat("###.##");

      private ControlPanelDevice(final int deviceIndex)
         {
         super(deviceIndex);

         rawValueTextField.setFont(GUIConstants.FONT_NORMAL);
         rawValueTextField.setEditable(false);
         rawValueTextField.setMaximumSize(rawValueTextField.getPreferredSize());
         rawValueTextField.setMinimumSize(rawValueTextField.getPreferredSize());

         tempValueTextField.setFont(GUIConstants.FONT_NORMAL);
         tempValueTextField.setEditable(false);
         tempValueTextField.setMaximumSize(tempValueTextField.getPreferredSize());
         tempValueTextField.setMinimumSize(tempValueTextField.getPreferredSize());

         // layout
         final JPanel borderedPanel = new JPanel(new SpringLayout());
         borderedPanel.setBackground(Color.WHITE);
         borderedPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.GRAY, 1),
                                                                    BorderFactory.createEmptyBorder(5, 5, 5, 5)));
         borderedPanel.add(SwingUtils.createLabel(RESOURCES.getString("label.raw")));
         borderedPanel.add(SwingUtils.createRigidSpacer());
         borderedPanel.add(rawValueTextField);
         borderedPanel.add(SwingUtils.createLabel(RESOURCES.getString("label.celsius")));
         borderedPanel.add(SwingUtils.createRigidSpacer());
         borderedPanel.add(tempValueTextField);
         SpringLayoutUtilities.makeCompactGrid(borderedPanel,
                                               2, 3, // rows, cols
                                               0, 0, // initX, initY
                                               0, 0);// xPad, yPad

         panel.setBackground(Color.WHITE);
         panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
         panel.add(SwingUtils.createLabel(String.valueOf(deviceIndex + 1)));
         panel.add(SwingUtils.createRigidSpacer());
         panel.add(borderedPanel);
         }

      private void setValue(final int value)
         {
         SwingUtilities.invokeLater(
               new Runnable()
                  {
                  public void run()
                     {
                     rawValueTextField.setText(String.valueOf(scaleToDisplay(value)));
                     if (service.isUnitConversionSupported())
                        {
                        tempValueTextField.setText(decimalFormat.format(service.convertToCelsius(value)));
                        }
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

      public void updateBlockIcon()
         {
         //TODO: Placeholder
         }

      public void updateComponent()
         {
         //TODO: Placeholder
         }

      public void getFocus()
         {
         rawValueTextField.requestFocus();
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