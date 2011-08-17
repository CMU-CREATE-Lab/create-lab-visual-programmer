package edu.cmu.ri.createlab.expressionbuilder.controlpanel.services.motor;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.PropertyResourceBundle;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import edu.cmu.ri.createlab.expressionbuilder.controlpanel.AbstractServiceControlPanel;
import edu.cmu.ri.createlab.expressionbuilder.controlpanel.AbstractServiceControlPanelDevice;
import edu.cmu.ri.createlab.expressionbuilder.controlpanel.ControlPanelManager;
import edu.cmu.ri.createlab.expressionbuilder.controlpanel.ServiceControlPanelDevice;
import edu.cmu.ri.createlab.expressionbuilder.widgets.DeviceSlider;
import edu.cmu.ri.createlab.expressionbuilder.widgets.IntensitySlider;
import edu.cmu.ri.createlab.terk.expression.XmlParameter;
import edu.cmu.ri.createlab.terk.services.Service;
import edu.cmu.ri.createlab.terk.services.motor.VelocityControllableMotorService;
import edu.cmu.ri.createlab.userinterface.util.ImageUtils;
import edu.cmu.ri.createlab.userinterface.util.SwingUtils;
import org.apache.log4j.Logger;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public final class VelocityControllableMotorServiceControlPanel extends AbstractServiceControlPanel
   {
   private static final Logger LOG = Logger.getLogger(VelocityControllableMotorServiceControlPanel.class);

   private static final PropertyResourceBundle RESOURCES = (PropertyResourceBundle)PropertyResourceBundle.getBundle(VelocityControllableMotorServiceControlPanel.class.getName());

   private static final String OPERATION_NAME = "setVelocity";
   private static final String PARAMETER_NAME = "velocity";
   private static final Set<String> PARAMETER_NAMES = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(PARAMETER_NAME)));
   private static final Map<String, Set<String>> OPERATIONS_TO_PARAMETERS_MAP;

   static
      {
      final Map<String, Set<String>> operationsToParametersMap = new HashMap<String, Set<String>>();
      operationsToParametersMap.put(OPERATION_NAME, PARAMETER_NAMES);
      OPERATIONS_TO_PARAMETERS_MAP = Collections.unmodifiableMap(operationsToParametersMap);
      }

   private final VelocityControllableMotorService service;

   public VelocityControllableMotorServiceControlPanel(final ControlPanelManager controlPanelManager, final VelocityControllableMotorService service)
      {
      super(controlPanelManager, service, OPERATIONS_TO_PARAMETERS_MAP);
      this.service = service;
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

   public JLabel getLabelImage()
      {
      final JLabel icon = new JLabel(ImageUtils.createImageIcon(RESOURCES.getString("image.green")));
      icon.setToolTipText(getDisplayName());
      return icon;
      }

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

   protected ServiceControlPanelDevice createServiceControlPanelDevice(final Service service, final int deviceIndex)
      {
      return new ControlPanelDevice(service, deviceIndex);
      }

   private final class ControlPanelDevice extends AbstractServiceControlPanelDevice
      {
      private static final int DEFAULT_ACTUAL_MIN_VALUE = -255;
      private static final int DEFAULT_ACTUAL_MAX_VALUE = 255;
      private static final int DISPLAY_MIN_VALUE = -100;
      private static final int DISPLAY_MAX_VALUE = 100;
      private static final int DISPLAY_INITIAL_VALUE = 0;

      private final int minAllowedVelocity;
      private final int maxAllowedVelocity;

      private final JPanel panel = new JPanel();
      private final DeviceSlider deviceSlider;
      private final int dIndex;

      private ControlPanelDevice(final Service service, final int deviceIndex)
         {
         super(deviceIndex);
         dIndex = deviceIndex;
         // try to read service properties, using defaults if undefined
         this.minAllowedVelocity = getServicePropertyAsInt(service, VelocityControllableMotorService.PROPERTY_NAME_MIN_VELOCITY, DEFAULT_ACTUAL_MIN_VALUE);
         this.maxAllowedVelocity = getServicePropertyAsInt(service, VelocityControllableMotorService.PROPERTY_NAME_MAX_VELOCITY, DEFAULT_ACTUAL_MAX_VALUE);
         if (LOG.isDebugEnabled())
            {
            LOG.debug("VelocityControllableMotorServiceControlPanel$ControlPanelDevice.ControlPanelDevice(" + deviceIndex + "): minAllowedVelocity=[" + minAllowedVelocity + "]");
            LOG.debug("VelocityControllableMotorServiceControlPanel$ControlPanelDevice.ControlPanelDevice(" + deviceIndex + "): maxAllowedVelocity=[" + maxAllowedVelocity + "]");
            }
         deviceSlider = new IntensitySlider(deviceIndex,
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
                                               VelocityControllableMotorServiceControlPanel.this.service.setVelocity(deviceIndex, scaledValue);
                                               }
                                            },
                                            "speed");

         // layout

         final JButton stopButton = new JButton(ImageUtils.createImageIcon(RESOURCES.getString("image.stop")));
         stopButton.setName("thinButton");
         stopButton.setFocusable(false);
         stopButton.addActionListener(new ActionListener()
         {
         @Override
         public void actionPerformed(ActionEvent e)
            {
            deviceSlider.setValue(0);
            }
         });

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

         Component slide = deviceSlider.getComponent();

         final JLayeredPane layer = new JLayeredPane();
         final Dimension sSize = slide.getPreferredSize();
         final Dimension slideSize = deviceSlider.slider.getPreferredSize();
         final Dimension bSize = stopButton.getPreferredSize();
         final Dimension layerSize = new Dimension(sSize.width, sSize.height + 5);
         layer.add(slide, new Integer(1));
         layer.add(stopButton, new Integer(2));

         layer.setPreferredSize(layerSize);
         layer.setMinimumSize(layerSize);
         layer.setMaximumSize(layerSize);
         layer.setAlignmentX(Component.LEFT_ALIGNMENT);

         stopButton.setBounds(slideSize.width / 2 - bSize.width / 2, 0, bSize.width, bSize.height);
         slide.setBounds(0, 5, sSize.width, sSize.height);

         panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
         panel.add(iconTitle);
         panel.add(layer);

         panel.setName("enabledServicePanel");
         }

      public Component getBlockIcon()
         {
         final JLabel act_icon = new JLabel(ImageUtils.createImageIcon(RESOURCES.getString("image.yellow")));
         final JLabel dis_icon = new JLabel(ImageUtils.createImageIcon(RESOURCES.getString("image.yellowdisabled")));

         if (this.isActive())
            {
            return act_icon;
            }
         else
            {
            return dis_icon;
            }
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

      private void updateGUI(final int value)
         {
         // Update the slider, but we don't want to rely on the execution strategy in order for the call to the
         // service to be made since the execution strategy won't get executed if there's no change in the slider's
         // value.  This can happen if the device's state is changed by some other means than via the service, such
         // as calling emergency stop.
         deviceSlider.setValueNoExecution(scaleToDisplay(value));
         }

      public boolean execute(final String operationName, final Map<String, String> parameterMap)
         {
         if (OPERATION_NAME.equals(operationName))
            {
            final String valueStr = parameterMap.get(PARAMETER_NAME);
            try
               {
               final int value = Integer.parseInt(valueStr);

               updateGUI(value);

               // execute the operation on the service
               service.setVelocity(getDeviceIndex(), value);
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
         final Integer value = scaleToActual(deviceSlider.getValue());

         if (value != null)
            {
            final Set<XmlParameter> parameters = new HashSet<XmlParameter>();
            parameters.add(new XmlParameter(PARAMETER_NAME, value));
            return parameters;
            }

         return null;
         }

      private int scaleToActual(final int value)
         {
         return scaleValue(value, DISPLAY_MIN_VALUE, DISPLAY_MAX_VALUE, minAllowedVelocity, maxAllowedVelocity);
         }

      private int scaleToDisplay(final int value)
         {
         return scaleValue(value, minAllowedVelocity, maxAllowedVelocity, DISPLAY_MIN_VALUE, DISPLAY_MAX_VALUE);
         }
      }
   }