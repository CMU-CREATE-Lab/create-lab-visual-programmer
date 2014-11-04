package edu.cmu.ri.createlab.expressionbuilder.controlpanel.services.motor;

import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import javax.swing.JButton;
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
import edu.cmu.ri.createlab.terk.services.motor.BaseVelocityControllableMotorServiceImpl;
import edu.cmu.ri.createlab.terk.services.motor.VelocityControllableMotorService;
import edu.cmu.ri.createlab.terk.services.servo.SimpleServoService;
import edu.cmu.ri.createlab.terk.xml.XmlParameter;
import edu.cmu.ri.createlab.userinterface.util.ImageUtils;
import edu.cmu.ri.createlab.userinterface.util.SwingUtils;
import org.apache.log4j.Logger;

/**
 * <p>
 * <code>BaseVelocityControllableMotorServiceControlPanel</code> provides base functionality for the
 * {@link VelocityControllableMotorServiceControlPanel} and {@link OpenLoopVelocityControllableMotorServiceControlPanel}.
 * </p>
 *
 * @author Chris Bartley (bartley@cmu.edu)
 */
abstract class BaseVelocityControllableMotorServiceControlPanel extends AbstractServiceControlPanel
   {
   private static final Logger LOG = Logger.getLogger(BaseVelocityControllableMotorServiceControlPanel.class);

   private static final PropertyResourceBundle RESOURCES = (PropertyResourceBundle)PropertyResourceBundle.getBundle(BaseVelocityControllableMotorServiceControlPanel.class.getName());
   protected static final String OPERATION_NAME = "setVelocity";
   private static final String PARAMETER_NAME = "velocity";
   protected static final Set<String> PARAMETER_NAMES = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(PARAMETER_NAME)));
   protected static final Map<String, Set<String>> OPERATIONS_TO_PARAMETERS_MAP;

   private static final int DEFAULT_ACTUAL_MIN_VALUE = -255;
   private static final int DEFAULT_ACTUAL_MAX_VALUE = 255;

   static
      {
      final Map<String, Set<String>> operationsToParametersMap = new HashMap<String, Set<String>>();
      operationsToParametersMap.put(OPERATION_NAME, PARAMETER_NAMES);
      OPERATIONS_TO_PARAMETERS_MAP = Collections.unmodifiableMap(operationsToParametersMap);
      }

   private final int minAllowedVelocity;
   private final int maxAllowedVelocity;
   private final ControlPanelManager controlPanelManager;
   private final Service service;

   BaseVelocityControllableMotorServiceControlPanel(final ControlPanelManager controlPanelManager,
                                                    final Service service,
                                                    final String minVelocityServicePropertyName,
                                                    final String maxVelocityServicePropertyName)
      {
      super(controlPanelManager, service, OPERATIONS_TO_PARAMETERS_MAP);

      // try to read service properties, using defaults if undefined
      this.minAllowedVelocity = getServicePropertyAsInt(service, minVelocityServicePropertyName, DEFAULT_ACTUAL_MIN_VALUE);
      this.maxAllowedVelocity = getServicePropertyAsInt(service, maxVelocityServicePropertyName, DEFAULT_ACTUAL_MAX_VALUE);
      this.controlPanelManager = controlPanelManager;
      this.service = service;
      if (LOG.isDebugEnabled())
         {
         LOG.debug("BaseVelocityControllableMotorServiceControlPanel.BaseVelocityControllableMotorServiceControlPanel(): minAllowedVelocity=[" + minAllowedVelocity + "]");
         LOG.debug("BaseVelocityControllableMotorServiceControlPanel.BaseVelocityControllableMotorServiceControlPanel(): maxAllowedVelocity=[" + maxAllowedVelocity + "]");
         }
      }

   public final String getDisplayName()
      {
      return RESOURCES.getString("control-panel.title");
      }

   public final String getSingleName()
      {
      return RESOURCES.getString("control-panel.name");
      }

   public final String getShortDisplayName()
      {
      return RESOURCES.getString("control-panel.short-title");
      }

   public final JLabel getLabelImage(final String imageName)
      {
      final JLabel icon = new JLabel(ImageUtils.createImageIcon(RESOURCES.getString(imageName)));
      icon.setToolTipText(getDisplayName());
      return icon;
      }

   public abstract void refresh();

   protected abstract void setVelocity(final int motorId, final int velocity);

   protected final ServiceControlPanelDevice createServiceControlPanelDevice(final Service service, final int deviceIndex)
      {
      return new ControlPanelDevice(deviceIndex);
      }

   protected final class ControlPanelDevice extends AbstractServiceControlPanelDevice
      {
      private static final int DISPLAY_MIN_VALUE = -100;
      private static final int DISPLAY_MAX_VALUE = 100;
      private static final int DISPLAY_INITIAL_VALUE = 0;

      private final JPanel panel = new JPanel();

      private final JPanel mainPanel = new JPanel();
      private final CardLayout cards = new CardLayout();
      private final String activeCard = "Active Card";
      private final String disabledCard = "Disabled Card";
      final JPanel act_box = new JPanel();
      final JPanel dis_box = new JPanel();

      private final DeviceSlider deviceSlider;
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
                                               BaseVelocityControllableMotorServiceControlPanel.this.setVelocity(deviceIndex, scaledValue);
                                               }
                                            },
                                            "speed");

         // layout
         final JButton stopButton = new JButton(ImageUtils.createImageIcon(RESOURCES.getString("image.stop")));
         stopButton.setName("thinButton");
         stopButton.setFocusable(false);
         stopButton.addActionListener(
               new ActionListener()
               {
               @Override
               public void actionPerformed(final ActionEvent e)
                  {
                  deviceSlider.setValue(0);
                  }
               });
         stopButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

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

         final Component slide = deviceSlider.getComponent();

         final JLayeredPane layer = new JLayeredPane();
         final Dimension sSize = slide.getPreferredSize();
         final Dimension slideSize = deviceSlider.slider.getPreferredSize();
         final Dimension bSize = stopButton.getPreferredSize();
         final Dimension layerSize = new Dimension(sSize.width, sSize.height + 7);
         layer.add(slide, new Integer(1));
         layer.add(stopButton, new Integer(2));

         layer.setPreferredSize(layerSize);
         layer.setMinimumSize(layerSize);
         layer.setMaximumSize(layerSize);
         layer.setAlignmentX(Component.LEFT_ALIGNMENT);

         stopButton.setBounds(slideSize.width / 2 - bSize.width / 2, 0, bSize.width, bSize.height);
         slide.setBounds(0, 7, sSize.width, sSize.height);

         panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
         panel.add(iconTitle);
         panel.add(layer);

         panel.setName("enabledServicePanel");

         final JLabel disicon = new JLabel(ImageUtils.createImageIcon(RESOURCES.getString("image.disabled")));
         disicon.setAlignmentX(Component.LEFT_ALIGNMENT);
         disicon.setToolTipText(getSingleName() + " " + String.valueOf(dIndex + 1) + " is disabled");

         act_box.setName("active_service_box");
         act_box.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
         act_box.setLayout(new BoxLayout(act_box, BoxLayout.Y_AXIS));
         act_box.add(panel);

         dis_box.setName("disabled_service_box");
         dis_box.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
         dis_box.setLayout(new BoxLayout(dis_box, BoxLayout.Y_AXIS));
         dis_box.add(disicon);
         dis_box.setPreferredSize(act_box.getPreferredSize());
         dis_box.setMinimumSize(act_box.getMinimumSize());
         dis_box.setMaximumSize(act_box.getMaximumSize());

         dis_box.addMouseListener(new MouseAdapter() {
             public void mouseClicked(MouseEvent e) {
                 controlPanelManager.setDeviceActive(service.getTypeId(), dIndex, true);

             }
         });

         dis_box.setCursor(new Cursor(Cursor.HAND_CURSOR));

         mainPanel.setLayout(cards);

         mainPanel.add(act_box, activeCard);
         mainPanel.add(dis_box, disabledCard);

         cards.show(mainPanel, disabledCard);
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

         if (this.isActive())
            {
            cards.show(mainPanel, activeCard);
            LOG.debug("Updating BaseVelocityMotor Component Control Panel: activeCard");
            }
         else
            {
            cards.show(mainPanel, disabledCard);
            LOG.debug("Updating BaseVelocityMotor Component Control Panel: disabledCard");
            }
         }

      public void getFocus()
         {
         deviceSlider.getFocus();
         }

      public Component getComponent()
         {
         return mainPanel;
         }

      public void updateGUI(final int value)
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
               final int val = Integer.parseInt(valueStr);

               updateGUI(val);

               // execute the operation on the service
               setVelocity(getDeviceIndex(), val);
               return true;
               }
            catch (NumberFormatException e)
               {
               LOG.debug("BaseVelocityControllableMotorServiceControlPanel$ControlPanelDevice.execute(): NumberFormatException while trying to convert [" + valueStr + "] to an integer.", e);
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
         return scaleValue(value, DISPLAY_MIN_VALUE, DISPLAY_MAX_VALUE, minAllowedVelocity, maxAllowedVelocity);
         }

      private int scaleToDisplay(final int value)
         {
         return scaleValue(value, minAllowedVelocity, maxAllowedVelocity, DISPLAY_MIN_VALUE, DISPLAY_MAX_VALUE);
         }
      }
   }
