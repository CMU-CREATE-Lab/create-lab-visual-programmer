package edu.cmu.ri.createlab.expressionbuilder.controlpanel.services.led;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
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
import edu.cmu.ri.createlab.terk.services.led.SimpleLEDService;
import edu.cmu.ri.createlab.terk.xml.XmlParameter;
import edu.cmu.ri.createlab.userinterface.util.ImageUtils;
import edu.cmu.ri.createlab.userinterface.util.SwingUtils;
import org.apache.log4j.Logger;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public final class SimpleLEDServiceControlPanel extends AbstractServiceControlPanel
   {
   private static final Logger LOG = Logger.getLogger(SimpleLEDServiceControlPanel.class);
   private static final PropertyResourceBundle RESOURCES = (PropertyResourceBundle)PropertyResourceBundle.getBundle(SimpleLEDServiceControlPanel.class.getName());
   private static final String OPERATION_NAME = SimpleLEDService.OPERATION_NAME_SET_INTENSITY;
   private static final String PARAMETER_NAME = SimpleLEDService.PARAMETER_NAME_INTENSITY;
   private static final Set<String> PARAMETER_NAMES = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(PARAMETER_NAME)));
   private static final Map<String, Set<String>> OPERATIONS_TO_PARAMETERS_MAP;

   static
      {
      final Map<String, Set<String>> operationsToParametersMap = new HashMap<String, Set<String>>();
      operationsToParametersMap.put(OPERATION_NAME, PARAMETER_NAMES);
      OPERATIONS_TO_PARAMETERS_MAP = Collections.unmodifiableMap(operationsToParametersMap);
      }

   private final SimpleLEDService service;
   private final ControlPanelManager controlPanelManager;

   public SimpleLEDServiceControlPanel(final ControlPanelManager controlPanelManager, final SimpleLEDService service)
      {
      super(controlPanelManager, service, OPERATIONS_TO_PARAMETERS_MAP);
      this.service = service;
      this.controlPanelManager = controlPanelManager;
      }

   public String getSingleName()
      {
      return RESOURCES.getString("control-panel.name");
      }

   public String getDisplayName()
      {
      return RESOURCES.getString("control-panel.title");
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
      LOG.debug("SimpleLEDServiceControlPanel.refresh()");

      // get the current state
      final int[] intensities = service.getIntensities();

      for (final ServiceControlPanelDevice device : getDevices())
         {
         if (device.getDeviceIndex() >= 0 && device.getDeviceIndex() < intensities.length)
            {
            ((ControlPanelDevice)device).updateGUI(intensities[device.getDeviceIndex()]);
            }
         }
      }

   protected ServiceControlPanelDevice createServiceControlPanelDevice(final Service service, final int deviceIndex)
      {
      return new ControlPanelDevice(service, deviceIndex);
      }

   private final class ControlPanelDevice extends AbstractServiceControlPanelDevice
      {
      private static final int DEFAULT_ACTUAL_MIN_VALUE = 0;
      private static final int DEFAULT_ACTUAL_MAX_VALUE = 255;
      private static final int DISPLAY_MIN_VALUE = 0;
      private static final int DISPLAY_MAX_VALUE = 100;
      private static final int DISPLAY_INITIAL_VALUE = 0;

      private final int minAllowedIntensity;
      private final int maxAllowedIntensity;
      private final JPanel panel = new JPanel();
      private final DeviceSlider deviceSlider;
      private final int dIndex;
      private final ImageIcon act_icon = ImageUtils.createImageIcon(RESOURCES.getString("image.yellow"));
      private final ImageIcon dis_icon = ImageUtils.createImageIcon(RESOURCES.getString("image.yellowdisabled"));
      private final ImageIcon off_icon = ImageUtils.createImageIcon(RESOURCES.getString("image.yellowoff"));
      private int value;
      private JLabel blockIcon = new JLabel();

      private ControlPanelDevice(final Service service, final int deviceIndex)
         {
         super(deviceIndex);
         dIndex = deviceIndex;
         value = DISPLAY_INITIAL_VALUE;
         // try to read service properties, using defaults if undefined
         this.minAllowedIntensity = getServicePropertyAsInt(service, SimpleLEDService.PROPERTY_NAME_MIN_INTENSITY, DEFAULT_ACTUAL_MIN_VALUE);
         this.maxAllowedIntensity = getServicePropertyAsInt(service, SimpleLEDService.PROPERTY_NAME_MAX_INTENSITY, DEFAULT_ACTUAL_MAX_VALUE);
         if (LOG.isDebugEnabled())
            {
            LOG.debug("SimpleLEDServiceControlPanel$ControlPanelDevice.ControlPanelDevice(" + deviceIndex + "): minAllowedIntensity=[" + minAllowedIntensity + "]");
            LOG.debug("SimpleLEDServiceControlPanel$ControlPanelDevice.ControlPanelDevice(" + deviceIndex + "): maxAllowedIntensity=[" + maxAllowedIntensity + "]");
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
                                                  SimpleLEDServiceControlPanel.this.service.set(deviceIndex, scaledValue);
                                                  }
                                               },
                                            "simpleLED");

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

         icon.addMouseListener(new MouseAdapter()
            {
            public void mousePressed(MouseEvent e)
               {
               controlPanelManager.setDeviceActive(service.getTypeId(), dIndex, ActivityLevels.OFF);
               }
            });
         icon.setCursor(new Cursor(Cursor.HAND_CURSOR));

         panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
         /*panel.add(iconTitle);*/
         final Component slide = deviceSlider.getComponent();
         panel.setName("enabledServicePanel");

         final JLayeredPane layer = new JLayeredPane();
         final Dimension sSize = slide.getPreferredSize();
         final Dimension itSize = iconTitle.getPreferredSize();
         layer.add(slide, new Integer(1));
         layer.add(iconTitle, new Integer(2));

         iconTitle.setBounds(0, 0, itSize.width, itSize.height);
         slide.setBounds(40, 15, sSize.width, sSize.height);

         //layer.setPreferredSize(new Dimension(sSize.width + 40, sSize.height));
         layer.setPreferredSize(new Dimension(sSize.width + 40, sSize.height + 15));
         layer.setMinimumSize(new Dimension(sSize.width + 40, sSize.height + 15));
         panel.add(layer);
         }

      public Component getBlockIcon()
         {
         updateBlockIcon();
         return blockIcon;
         }

      public void updateBlockIcon()
         {
         if (this.isActive() == ActivityLevels.SET ||
             this.isActive() == ActivityLevels.OFF)
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
         final JPanel off_box = new JPanel();
         final JLabel icon = new JLabel(ImageUtils.createImageIcon(RESOURCES.getString("image.disabled")));
         icon.setAlignmentX(Component.LEFT_ALIGNMENT);

         act_box.setName("active_service_box");
         act_box.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
         act_box.setLayout(new BoxLayout(act_box, BoxLayout.Y_AXIS));
         act_box.add(panel);

         final JLabel icon2 = new JLabel(ImageUtils.createImageIcon(RESOURCES.getString("image.disabled")));
         icon2.setAlignmentX(Component.LEFT_ALIGNMENT);

         off_box.setName("off_service_box");
         off_box.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
         final JPanel iconTitle = new JPanel();
         iconTitle.setLayout(new BoxLayout(iconTitle, BoxLayout.X_AXIS));
         iconTitle.add(icon2);
         iconTitle.add(SwingUtils.createRigidSpacer(2));
         iconTitle.add(SwingUtils.createLabel(getSingleName()));
         iconTitle.add(SwingUtils.createRigidSpacer(5));
         iconTitle.add(SwingUtils.createLabel(String.valueOf(dIndex + 1)));
         iconTitle.setName("iconTitle");
         iconTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
         final Dimension itSize = iconTitle.getPreferredSize();
         iconTitle.setBounds(0, 0, itSize.width, itSize.height);

         off_box.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
         off_box.setLayout(new BoxLayout(off_box, BoxLayout.Y_AXIS));
         off_box.add(iconTitle);

         final JPanel offLabel = new JPanel();
         offLabel.setLayout(new BoxLayout(offLabel, BoxLayout.X_AXIS));
         offLabel.add(SwingUtils.createRigidSpacer(100, 17));
         offLabel.add(SwingUtils.createLabel("OFF"));
         offLabel.setName("iconTitle");
         offLabel.setBounds(0, 0, itSize.width, itSize.height);
         offLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

         off_box.add(offLabel);
         icon2.setToolTipText(getSingleName() + " " + String.valueOf(dIndex + 1) + " is off");
         off_box.setPreferredSize(off_box.getPreferredSize());
         off_box.setMinimumSize(off_box.getMinimumSize());
         off_box.setMaximumSize(off_box.getMaximumSize());
         off_box.addMouseListener(new MouseAdapter()
            {
            public void mousePressed(MouseEvent e)
               {
               controlPanelManager.setDeviceActive(SimpleLEDService.TYPE_ID, dIndex, ActivityLevels.STAY);
               }
            });
         icon2.addMouseListener(new MouseAdapter()
            {
            public void mousePressed(MouseEvent e)
               {
               controlPanelManager.setDeviceActive(service.TYPE_ID, dIndex, ActivityLevels.STAY);
               }
            });
         off_box.setCursor(new Cursor(Cursor.HAND_CURSOR));

         dis_box.setName("disabled_service_box");
         dis_box.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
         dis_box.setLayout(new BoxLayout(dis_box, BoxLayout.Y_AXIS));
         dis_box.add(icon);
         icon.setToolTipText(getSingleName() + " " + String.valueOf(dIndex + 1) + " is disabled");
         dis_box.setPreferredSize(act_box.getPreferredSize());
         dis_box.setMinimumSize(act_box.getMinimumSize());
         dis_box.setMaximumSize(act_box.getMaximumSize());

         dis_box.addMouseListener(new MouseAdapter()
            {
            public void mousePressed(MouseEvent e)
               {
               controlPanelManager.setDeviceActive(SimpleLEDService.TYPE_ID, dIndex, ActivityLevels.SET);
               }
            });

         icon.addMouseListener(new MouseAdapter()
            {
            public void mousePressed(MouseEvent e)
               {
               controlPanelManager.setDeviceActive(SimpleLEDService.TYPE_ID, dIndex, ActivityLevels.SET);
               }
            });

         dis_box.setCursor(new Cursor(Cursor.HAND_CURSOR));
         if (this.isActive() == ActivityLevels.SET)
            {
            return act_box;
            }
         else if (this.isActive() == ActivityLevels.STAY)
            {
            return dis_box;
            }
         else
            {
            deviceSlider.setValue(0);
            return off_box;
            }
         }

      private void updateGUI(final int intensity)
         {
         // Update the slider, but we don't want to rely on the execution strategy in order for the call to the
         // service to be made since the execution strategy won't get executed if there's no change in the slider's
         // value.  This can happen if the device's state is changed by some other means than via the service, such
         // as calling emergency stop.
         deviceSlider.setValueNoExecution(scaleToDisplay(intensity));
         }

      public boolean execute(final String operationName, final Map<String, String> parameterMap)
         {
         if (OPERATION_NAME.equals(operationName))
            {
            final String valueStr = parameterMap.get(PARAMETER_NAME);
            try
               {
               final int value = Integer.parseInt(valueStr);

               // update the GUI - ish
               updateGUI(value);

               // execute the operation on the service
               service.set(getDeviceIndex(), value);
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
         return scaleValue(value, DISPLAY_MIN_VALUE, DISPLAY_MAX_VALUE, minAllowedIntensity, maxAllowedIntensity);
         }

      private int scaleToDisplay(final int value)
         {
         return scaleValue(value, minAllowedIntensity, maxAllowedIntensity, DISPLAY_MIN_VALUE, DISPLAY_MAX_VALUE);
         }
      }
   }