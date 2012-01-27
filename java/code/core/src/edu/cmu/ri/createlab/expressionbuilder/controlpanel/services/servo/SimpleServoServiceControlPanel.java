package edu.cmu.ri.createlab.expressionbuilder.controlpanel.services.servo;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.PropertyResourceBundle;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import edu.cmu.ri.createlab.expressionbuilder.controlpanel.AbstractServiceControlPanel;
import edu.cmu.ri.createlab.expressionbuilder.controlpanel.AbstractServiceControlPanelDevice;
import edu.cmu.ri.createlab.expressionbuilder.controlpanel.ControlPanelManager;
import edu.cmu.ri.createlab.expressionbuilder.controlpanel.ServiceControlPanelDevice;
import edu.cmu.ri.createlab.expressionbuilder.widgets.DeviceSlider;
import edu.cmu.ri.createlab.expressionbuilder.widgets.ServoDial;
import edu.cmu.ri.createlab.terk.expression.XmlParameter;
import edu.cmu.ri.createlab.terk.services.Service;
import edu.cmu.ri.createlab.terk.services.servo.SimpleServoService;
import edu.cmu.ri.createlab.userinterface.util.ImageUtils;
import edu.cmu.ri.createlab.userinterface.util.SwingUtils;
import org.apache.log4j.Logger;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public final class SimpleServoServiceControlPanel extends AbstractServiceControlPanel
   {
   private static final Logger LOG = Logger.getLogger(SimpleServoServiceControlPanel.class);

   private static final PropertyResourceBundle RESOURCES = (PropertyResourceBundle)PropertyResourceBundle.getBundle(SimpleServoServiceControlPanel.class.getName());

   private static final String OPERATION_NAME = "setPosition";
   private static final String PARAMETER_NAME = "position";
   private static final Set<String> PARAMETER_NAMES = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(PARAMETER_NAME)));
   private static final Map<String, Set<String>> OPERATIONS_TO_PARAMETERS_MAP;

   private static final int DEFAULT_MIN_VALUE = 0;
   private static final int DEFAULT_MAX_VALUE = 255;

   static
      {
      final Map<String, Set<String>> operationsToParametersMap = new HashMap<String, Set<String>>();
      operationsToParametersMap.put(OPERATION_NAME, PARAMETER_NAMES);
      OPERATIONS_TO_PARAMETERS_MAP = Collections.unmodifiableMap(operationsToParametersMap);
      }

   private final SimpleServoService service;
   private final int minAllowedPosition;
   private final int maxAllowedPosition;

   public SimpleServoServiceControlPanel(final ControlPanelManager controlPanelManager, final SimpleServoService service)
      {
      super(controlPanelManager, service, OPERATIONS_TO_PARAMETERS_MAP);
      this.service = service;

      final Integer minPosition = service.getPropertyAsInteger(SimpleServoService.PROPERTY_NAME_MIN_POSITION);
      final Integer maxPosition = service.getPropertyAsInteger(SimpleServoService.PROPERTY_NAME_MAX_POSITION);

      final Integer minSafePosition = service.getPropertyAsInteger(SimpleServoService.PROPERTY_NAME_MIN_SAFE_POSITION);
      final Integer maxSafePosition = service.getPropertyAsInteger(SimpleServoService.PROPERTY_NAME_MAX_SAFE_POSITION);

      final int min = (minPosition == null) ? DEFAULT_MIN_VALUE : minPosition;
      final int max = (maxPosition == null) ? DEFAULT_MAX_VALUE : maxPosition;

      minAllowedPosition = (minSafePosition == null) ? min : Math.max(min, minSafePosition);
      maxAllowedPosition = (maxSafePosition == null) ? max : Math.min(max, maxSafePosition);

      LOG.debug("SimpleServoServiceControlPanel.SimpleServoServiceControlPanel(): minPosition/maxPosition = (" + minPosition + "," + maxPosition + ")");
      LOG.debug("SimpleServoServiceControlPanel.SimpleServoServiceControlPanel(): minSafePosition/maxPosition = (" + minSafePosition + "," + maxSafePosition + ")");
      LOG.debug("SimpleServoServiceControlPanel.SimpleServoServiceControlPanel(): min/max = (" + min + "," + max + ")");
      LOG.debug("SimpleServoServiceControlPanel.SimpleServoServiceControlPanel(): minAllowedPosition/maxAllowedPosition = (" + minAllowedPosition + "," + maxAllowedPosition + ")");
      }

   public String getDisplayName()
      {
      return RESOURCES.getString("control-panel.title");
      }

   public String getSingleName()
      {
      return RESOURCES.getString("control-panel.name");
      }

   public String getShortDisplayName()
      {
      return RESOURCES.getString("control-panel.short-title");
      }

   public JLabel getLabelImage(String imageName)
      {
      final JLabel icon = new JLabel(ImageUtils.createImageIcon(RESOURCES.getString(imageName)));
      icon.setToolTipText(getDisplayName());
      return icon;
      }

   public void refresh()
      {
      LOG.debug("SimpleServoServiceControlPanel.refresh()");

      // get the current state
      final int[] positions = service.getPositions();

      for (final ServiceControlPanelDevice device : getDevices())
         {
         if (device.getDeviceIndex() >= 0 && device.getDeviceIndex() < positions.length)
            {
            ((ControlPanelDevice)device).updateGUI(positions[device.getDeviceIndex()]);
            }
         }
      }

   protected ServiceControlPanelDevice createServiceControlPanelDevice(final Service service, final int deviceIndex)
      {
      return new ControlPanelDevice(deviceIndex);
      }

   private final class ControlPanelDevice extends AbstractServiceControlPanelDevice
      {
      private static final int DISPLAY_MIN_VALUE = 0;
      private static final int DISPLAY_MAX_VALUE = 180;
      private static final int DISPLAY_INITIAL_VALUE = 0;

      private final JPanel panel = new JPanel();
      private final DeviceSlider deviceSlider;
      private final ServoDial dial;
      private final int dIndex;

      private int value;
      private JLabel blockIcon = new JLabel();

      private final ImageIcon act_icon = ImageUtils.createImageIcon(RESOURCES.getString("image.yellow"));
      private final ImageIcon dis_icon = ImageUtils.createImageIcon(RESOURCES.getString("image.yellowdisabled"));
      private final ImageIcon off_icon = ImageUtils.createImageIcon(RESOURCES.getString("image.yellowoff"));

      private ControlPanelDevice(final int deviceIndex)
         {
         super(deviceIndex);
         dIndex = deviceIndex;

         dial = new ServoDial(DISPLAY_INITIAL_VALUE);

         deviceSlider = new DeviceSlider(deviceIndex,
                                         DISPLAY_MIN_VALUE,
                                         DISPLAY_MAX_VALUE,
                                         DISPLAY_INITIAL_VALUE,
                                         100,
                                         500,
                                         new DeviceSlider.ExecutionStrategy()
                                         {
                                         public void execute(final int deviceIndex, final int value)
                                            {
                                            final int scaledValue = scaleToActual(value);
                                            service.setPosition(deviceIndex, scaledValue);
                                            }
                                         });

         deviceSlider.slider.addChangeListener(
               new ChangeListener()
               {
               public void stateChanged(final ChangeEvent e)
                  {
                  final JSlider source = (JSlider)e.getSource();
                  value = source.getValue();
                  dial.setValue(value);
                  updateBlockIcon();
                  }
               });

         // layout
         final JLabel icon = new JLabel(ImageUtils.createImageIcon(RESOURCES.getString("image.enabled")));
         final JPanel iconTitle = new JPanel();
         iconTitle.setLayout(new BoxLayout(iconTitle, BoxLayout.X_AXIS));
         iconTitle.add(icon);
         iconTitle.add(SwingUtils.createRigidSpacer(2));
         iconTitle.add(SwingUtils.createLabel(getSingleName()));
         iconTitle.add(SwingUtils.createRigidSpacer(5));
         iconTitle.add(SwingUtils.createLabel(String.valueOf(deviceIndex + 1)));
         iconTitle.setName("iconTitle");
         iconTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

         panel.setLayout(new GridBagLayout());

         final GridBagConstraints c = new GridBagConstraints();
         c.fill = GridBagConstraints.NONE;
         c.gridx = 0;
         c.gridy = 0;
         c.gridwidth = 3;
         c.weighty = 0.0;
         c.weightx = 1.0;
         c.anchor = GridBagConstraints.FIRST_LINE_START;
         panel.add(iconTitle, c);

         c.fill = GridBagConstraints.HORIZONTAL;
         c.gridx = 0;
         c.gridy = 1;
         c.gridwidth = 1;
         c.weighty = 1.0;
         c.weightx = 1.0;
         c.anchor = GridBagConstraints.CENTER;
         panel.add(deviceSlider.slider, c);

         c.fill = GridBagConstraints.HORIZONTAL;
         c.gridx = 1;
         c.gridy = 0;
         c.gridheight = 2;
         c.weighty = 1.0;
         c.weightx = 0.0;
         c.anchor = GridBagConstraints.PAGE_END;
         c.insets = new Insets(0, 5, 0, 0);
         panel.add(dial.getComponent(), c);

         c.fill = GridBagConstraints.HORIZONTAL;
         c.gridx = 2;
         c.gridy = 1;
         c.weighty = 1.0;
         c.weightx = 0.0;
         c.insets = new Insets(0, 5, 0, 0);
         c.anchor = GridBagConstraints.CENTER;
         panel.add(deviceSlider.textField, c);

         panel.setName("enabledServicePanel");
         }

      public Component getBlockIcon()
         {
         updateBlockIcon();

         return blockIcon;
         }

      public void updateBlockIcon()
         {

         if (this.isActive())
            {

            blockIcon.setIcon(act_icon);
            }
         else
            {
            blockIcon.setIcon(dis_icon);
            }
         }

          public void updateComponent()
          {
              //TODO: Placeholder
          }

          public void getFocus()
         {
         deviceSlider.getFocus();
         }

      public Component getComponent()
         {
         final JPanel act_box = new JPanel();
         final JPanel dis_box = new JPanel();
         final JLabel icon = new JLabel(ImageUtils.createImageIcon(RESOURCES.getString("image.disabled")));
         icon.setAlignmentX(Component.LEFT_ALIGNMENT);
         icon.setToolTipText(getSingleName() + " " + String.valueOf(dIndex + 1) + " is disabled");

         act_box.setName("active_service_box");
         act_box.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
         act_box.setLayout(new BoxLayout(act_box, BoxLayout.Y_AXIS));
         act_box.add(panel);

         dis_box.setName("disabled_service_box");
         dis_box.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
         dis_box.setLayout(new BoxLayout(dis_box, BoxLayout.Y_AXIS));
         dis_box.add(icon);
         dis_box.setPreferredSize(act_box.getPreferredSize());
         dis_box.setMinimumSize(act_box.getMinimumSize());
         dis_box.setMaximumSize(act_box.getMaximumSize());

         if (this.isActive())
            {
            return act_box;
            }
         else
            {
            return dis_box;
            }
         }

      private void updateGUI(final int intensity)
         {
         // Update the slider, but we don't want to rely on the execution strategy in order for the call to the
         // service to be made since the execution strategy won't get executed if there's no change in the slider's
         // value.  This can happen if the device's state is changed by some other means than via the service, such
         // as calling emergency stop.
         deviceSlider.setValueNoExecution(scaleToDisplay(intensity));
         dial.setValue(scaleToDisplay(intensity));
         }

      public boolean execute(final String operationName, final Map<String, String> parameterMap)
         {
         if (OPERATION_NAME.equals(operationName))
            {
            final String valueStr = parameterMap.get(PARAMETER_NAME);
            try
               {
               final int val = Math.max(minAllowedPosition, Math.min(maxAllowedPosition, Integer.parseInt(valueStr)));

               LOG.debug("SimpleServoServiceControlPanel$ControlPanelDevice.execute(): converted [" + valueStr + "] to [" + val + "] ****************************************");
               updateGUI(val);

               // execute the operation on the service
               service.setPosition(getDeviceIndex(), val);
               return true;
               }
            catch (NumberFormatException e)
               {
               LOG.error("NumberFormatException while trying to convert [" + valueStr + "] to an integer.", e);
               }
            }
         return false;
         }

      public String getCurrentOperationName()
         {
         return OPERATION_NAME;
         }

      public Set<XmlParameter> buildParameters()
         {
         final Integer val = scaleToActual(deviceSlider.getValue());

         if (val != null)
            {
            final Set<XmlParameter> parameters = new HashSet<XmlParameter>();
            parameters.add(new XmlParameter(PARAMETER_NAME, val));
            return parameters;
            }

         return null;
         }

      private int scaleToActual(final int value)
         {
         return scaleValue(value, DISPLAY_MIN_VALUE, DISPLAY_MAX_VALUE, minAllowedPosition, maxAllowedPosition);
         }

      private int scaleToDisplay(final int value)
         {
         return scaleValue(value, minAllowedPosition, maxAllowedPosition, DISPLAY_MIN_VALUE, DISPLAY_MAX_VALUE);
         }
      }
   }
