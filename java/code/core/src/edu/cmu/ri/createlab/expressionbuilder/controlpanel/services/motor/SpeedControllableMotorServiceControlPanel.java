package edu.cmu.ri.createlab.expressionbuilder.controlpanel.services.motor;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import edu.cmu.ri.createlab.expressionbuilder.controlpanel.AbstractServiceControlPanel;
import edu.cmu.ri.createlab.expressionbuilder.controlpanel.AbstractServiceControlPanelDevice;
import edu.cmu.ri.createlab.expressionbuilder.controlpanel.ControlPanelManager;
import edu.cmu.ri.createlab.expressionbuilder.controlpanel.ServiceControlPanelDevice;
import edu.cmu.ri.createlab.expressionbuilder.widgets.DeviceSlider;
import edu.cmu.ri.createlab.expressionbuilder.widgets.IntensitySlider;
import edu.cmu.ri.createlab.terk.services.Service;
import edu.cmu.ri.createlab.terk.services.motor.SpeedControllableMotorService;
import edu.cmu.ri.createlab.terk.xml.XmlParameter;
import edu.cmu.ri.createlab.userinterface.util.ImageUtils;
import edu.cmu.ri.createlab.userinterface.util.SwingUtils;
import org.apache.log4j.Logger;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public final class SpeedControllableMotorServiceControlPanel extends AbstractServiceControlPanel
   {
   private static final Logger LOG = Logger.getLogger(SpeedControllableMotorServiceControlPanel.class);

   private static final PropertyResourceBundle RESOURCES = (PropertyResourceBundle)PropertyResourceBundle.getBundle(SpeedControllableMotorServiceControlPanel.class.getName());

   private static final String OPERATION_NAME = "setSpeed";
   private static final String PARAMETER_NAME = "speed";
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

   private final SpeedControllableMotorService service;
   private final int minAllowedSpeed;
   private final int maxAllowedSpeed;
   private final ControlPanelManager controlPanelManager;

   public SpeedControllableMotorServiceControlPanel(final ControlPanelManager controlPanelManager, final SpeedControllableMotorService service)
      {
      super(controlPanelManager, service, OPERATIONS_TO_PARAMETERS_MAP);
      this.service = service;
      this.controlPanelManager = controlPanelManager;

      final Integer minSpeed = service.getPropertyAsInteger(SpeedControllableMotorService.PROPERTY_NAME_MIN_SPEED);
      final Integer maxSpeed = service.getPropertyAsInteger(SpeedControllableMotorService.PROPERTY_NAME_MAX_SPEED);

      minAllowedSpeed = (minSpeed == null) ? DEFAULT_MIN_VALUE : minSpeed;
      final Integer maxSafeSpeed = service.getPropertyAsInteger(SpeedControllableMotorService.PROPERTY_NAME_MAX_SAFE_SPEED);
      final int max = (maxSpeed == null) ? DEFAULT_MAX_VALUE : maxSpeed;
      this.maxAllowedSpeed = (maxSafeSpeed == null) ? max : Math.min(max, maxSafeSpeed);
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

   public JLabel getLabelImage(final String imageName)
      {
      final JLabel icon = new JLabel(ImageUtils.createImageIcon(RESOURCES.getString(imageName)));
      icon.setToolTipText(getDisplayName());
      return icon;
      }

   public void refresh()
      {
      LOG.debug("SpeedControllableMotorServiceControlPanel.refresh()");

      // get the current state
      final int[] speeds = service.getSpeeds();

      for (final ServiceControlPanelDevice device : getDevices())
         {
         if (device.getDeviceIndex() >= 0 && device.getDeviceIndex() < speeds.length)
            {
            ((ControlPanelDevice)device).updateGUI(speeds[device.getDeviceIndex()]);
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
      private static final int DISPLAY_MAX_VALUE = 100;
      private static final int DISPLAY_INITIAL_VALUE = 0;

      private JLabel blockIcon = new JLabel();
      private final ImageIcon act_icon = ImageUtils.createImageIcon(RESOURCES.getString("image.yellow"));
      private final ImageIcon dis_icon = ImageUtils.createImageIcon(RESOURCES.getString("image.yellowdisabled"));
      private final ImageIcon off_icon = ImageUtils.createImageIcon(RESOURCES.getString("image.yellowoff"));
      private int value;

      private final JPanel panel = new JPanel();
      private final DeviceSlider deviceSlider;
      private final int dIndex;

      private ControlPanelDevice(final int deviceIndex)
         {
         super(deviceIndex);
         dIndex = deviceIndex;

         value = DISPLAY_INITIAL_VALUE;
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
                                               service.setSpeed(deviceIndex, scaledValue);
                                               }
                                            },
                                            "vibMotor");

         // layout
         deviceSlider.slider.addChangeListener(
               new ChangeListener()
               {
               public void stateChanged(final ChangeEvent e)
                  {
                  final JSlider source = (JSlider)e.getSource();
                  value = source.getValue();
                  updateBlockIcon();
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

         icon.addMouseListener(new MouseAdapter() {
             public void mousePressed(MouseEvent e) {
                 controlPanelManager.setDeviceActive(service.getTypeId(), dIndex, false);

             }
         });
         icon.setCursor(new Cursor(Cursor.HAND_CURSOR));


         panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
         final Component slide = deviceSlider.getComponent();
         panel.setName("enabledServicePanel");

         final JLayeredPane layer = new JLayeredPane();
         final Dimension sSize = slide.getPreferredSize();
         final Dimension itSize = iconTitle.getPreferredSize();
         layer.add(slide, new Integer(1));
         layer.add(iconTitle, new Integer(2));

         iconTitle.setBounds(0, -3, itSize.width, itSize.height);
         slide.setBounds(0, 18, sSize.width, sSize.height);

         layer.setPreferredSize(new Dimension(sSize.width, sSize.height + 18));
         layer.setMinimumSize(new Dimension(sSize.width, sSize.height + 18));
         panel.add(layer);
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
            if (this.value == 0)
               {
               blockIcon.setIcon(off_icon);
               }
            else
               {
               blockIcon.setIcon(act_icon);
               }
            }
         else
            {
            blockIcon.setIcon(dis_icon);
            }
         }

      public void updateComponent()
         {
         //TODO Need to add this maybe...
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
         dis_box.setBorder(BorderFactory.createEmptyBorder(2, 5, 5, 5));
         dis_box.setLayout(new BoxLayout(dis_box, BoxLayout.Y_AXIS));
         dis_box.add(icon);
         dis_box.setPreferredSize(act_box.getPreferredSize());
         dis_box.setMinimumSize(act_box.getMinimumSize());
         dis_box.setMaximumSize(act_box.getMaximumSize());

         dis_box.addMouseListener(new MouseAdapter() {
             public void mousePressed(MouseEvent e) {
                 controlPanelManager.setDeviceActive(SpeedControllableMotorService.TYPE_ID, dIndex, true);

             }
         });

         icon.addMouseListener(new MouseAdapter() {
             public void mousePressed(MouseEvent e) {
                 controlPanelManager.setDeviceActive(service.TYPE_ID, dIndex, true);

             }
         });


         dis_box.setCursor(new Cursor(Cursor.HAND_CURSOR));

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
               final int val = Math.max(minAllowedSpeed, Math.min(maxAllowedSpeed, Integer.parseInt(valueStr)));

               updateGUI(val);

               // execute the operation on the service
               service.setSpeed(getDeviceIndex(), val);
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
         final Integer actualValue = scaleToActual(deviceSlider.getValue());

         if (actualValue != null)
            {
            final Set<XmlParameter> parameters = new HashSet<XmlParameter>();
            parameters.add(new XmlParameter(PARAMETER_NAME, actualValue));
            return parameters;
            }

         return null;
         }

      private int scaleToActual(final int value)
         {
         return scaleValue(value, DISPLAY_MIN_VALUE, DISPLAY_MAX_VALUE, minAllowedSpeed, maxAllowedSpeed);
         }

      private int scaleToDisplay(final int value)
         {
         return scaleValue(value, minAllowedSpeed, maxAllowedSpeed, DISPLAY_MIN_VALUE, DISPLAY_MAX_VALUE);
         }
      }
   }