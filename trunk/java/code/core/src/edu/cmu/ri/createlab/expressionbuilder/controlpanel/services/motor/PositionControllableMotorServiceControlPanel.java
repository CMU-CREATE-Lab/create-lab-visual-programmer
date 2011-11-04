package edu.cmu.ri.createlab.expressionbuilder.controlpanel.services.motor;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.PropertyResourceBundle;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;
import edu.cmu.ri.createlab.expressionbuilder.controlpanel.AbstractServiceControlPanel;
import edu.cmu.ri.createlab.expressionbuilder.controlpanel.AbstractServiceControlPanelDevice;
import edu.cmu.ri.createlab.expressionbuilder.controlpanel.ControlPanelManager;
import edu.cmu.ri.createlab.expressionbuilder.controlpanel.ServiceControlPanelDevice;
import edu.cmu.ri.createlab.terk.expression.XmlParameter;
import edu.cmu.ri.createlab.terk.services.Service;
import edu.cmu.ri.createlab.terk.services.motor.PositionControllableMotorService;
import edu.cmu.ri.createlab.terk.services.motor.PositionControllableMotorState;
import edu.cmu.ri.createlab.userinterface.GUIConstants;
import edu.cmu.ri.createlab.userinterface.util.AbstractTimeConsumingAction;
import edu.cmu.ri.createlab.userinterface.util.SwingUtils;
import org.apache.log4j.Logger;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
@SuppressWarnings({"CloneableClassWithoutClone"})
public final class PositionControllableMotorServiceControlPanel extends AbstractServiceControlPanel
   {
   private static final Logger LOG = Logger.getLogger(PositionControllableMotorServiceControlPanel.class);

   private static final PropertyResourceBundle RESOURCES = (PropertyResourceBundle)PropertyResourceBundle.getBundle(PositionControllableMotorServiceControlPanel.class.getName());

   private static final String OPERATION_NAME = PositionControllableMotorService.OPERATION_NAME_SET_POSITION;
   private static final String PARAMETER_NAME_POSITION = PositionControllableMotorService.PARAMETER_NAME_POSITION;
   private static final String PARAMETER_NAME_SPEED = PositionControllableMotorService.PARAMETER_NAME_SPEED;
   private static final Set<String> PARAMETER_NAMES = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(PARAMETER_NAME_POSITION, PARAMETER_NAME_SPEED)));
   private static final Map<String, Set<String>> OPERATIONS_TO_PARAMETERS_MAP;

   static
      {
      final Map<String, Set<String>> operationsToParametersMap = new HashMap<String, Set<String>>();
      operationsToParametersMap.put(OPERATION_NAME, PARAMETER_NAMES);
      OPERATIONS_TO_PARAMETERS_MAP = Collections.unmodifiableMap(operationsToParametersMap);
      }

   private final PositionControllableMotorService service;

   public PositionControllableMotorServiceControlPanel(final ControlPanelManager controlPanelManager, final PositionControllableMotorService service)
      {
      super(controlPanelManager, service, OPERATIONS_TO_PARAMETERS_MAP);
      this.service = service;

      final Timer pollingTimer = new Timer("PositionControllableMotorPollingTimer", true);
      pollingTimer.scheduleAtFixedRate(
            new TimerTask()
            {
            public void run()
               {
               updateCurrentState();
               }
            },
            0,
            500);
      }

   private void updateCurrentState()
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
               final PositionControllableMotorState currentState = service.getState(i);
               if (currentState != null)
                  {
                  ((ControlPanelDevice)device).setCurrentState(currentState);
                  }
               }
            catch (Exception e)
               {
               LOG.error("Exception while trying to poll current motor position from motor [" + i + "]", e);
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
      LOG.debug("PositionControllableMotorServiceControlPanel.refresh()");

      updateCurrentState();
      }

   protected ServiceControlPanelDevice createServiceControlPanelDevice(final Service service, final int deviceIndex)
      {
      return new ControlPanelDevice((PositionControllableMotorService)service, deviceIndex);
      }

   private final class ControlPanelDevice extends AbstractServiceControlPanelDevice
      {
      private static final int DEFAULT_POSITION_MIN_VALUE = Short.MIN_VALUE;
      private static final int DEFAULT_POSITION_MAX_VALUE = Short.MAX_VALUE;
      private static final int POSITION_INITIAL_VALUE = 0;

      private static final int DEFAULT_SPEED_MIN_VALUE = 0;
      private static final int DEFAULT_SPEED_MAX_VALUE = 20;
      private static final int SPEED_INITIAL_VALUE = 0;

      private final JPanel panel = new JPanel();

      private final int minAllowedPositionDelta;
      private final int maxAllowedPositionDelta;
      private final int minAllowedSpeed;
      private final int maxAllowedSpeed;

      private final JTextField currentPositionTextField;
      private final JTextField currentSpecifiedPositionTextField;
      private final JTextField currentSpecifiedSpeedTextField;

      private final JTextField desiredPositionTextField = new JTextField();
      private final JTextField desiredSpeedTextField = new JTextField();
      private final JButton setPositionAndSpeedButton = SwingUtils.createButton(RESOURCES.getString("button.label.set"));

      private ControlPanelDevice(final PositionControllableMotorService service, final int deviceIndex)
         {
         super(deviceIndex);

         // try to read service properties, using defaults if undefined
         this.minAllowedPositionDelta = getServicePropertyAsInt(service, PositionControllableMotorService.PROPERTY_NAME_MIN_POSITION_DELTA, DEFAULT_POSITION_MIN_VALUE);
         this.maxAllowedPositionDelta = getServicePropertyAsInt(service, PositionControllableMotorService.PROPERTY_NAME_MAX_POSITION_DELTA, DEFAULT_POSITION_MAX_VALUE);
         this.minAllowedSpeed = getServicePropertyAsInt(service, PositionControllableMotorService.PROPERTY_NAME_MIN_SPEED, DEFAULT_SPEED_MIN_VALUE);
         this.maxAllowedSpeed = getServicePropertyAsInt(service, PositionControllableMotorService.PROPERTY_NAME_MAX_SPEED, DEFAULT_SPEED_MAX_VALUE);
         if (LOG.isDebugEnabled())
            {
            LOG.debug("PositionControllableMotorServiceControlPanel$ControlPanelDevice.ControlPanelDevice(" + deviceIndex + "): minAllowedPositionDelta=[" + minAllowedPositionDelta + "]");
            LOG.debug("PositionControllableMotorServiceControlPanel$ControlPanelDevice.ControlPanelDevice(" + deviceIndex + "): maxAllowedPositionDelta=[" + maxAllowedPositionDelta + "]");
            LOG.debug("PositionControllableMotorServiceControlPanel$ControlPanelDevice.ControlPanelDevice(" + deviceIndex + "): minAllowedSpeed        =[" + minAllowedSpeed + "]");
            LOG.debug("PositionControllableMotorServiceControlPanel$ControlPanelDevice.ControlPanelDevice(" + deviceIndex + "): maxAllowedSpeed        =[" + maxAllowedSpeed + "]");
            }

         final int numColumnsForTextFields = Math.max(Math.max(String.valueOf(minAllowedPositionDelta).length(),
                                                               String.valueOf(minAllowedPositionDelta).length()),
                                                      Math.max(String.valueOf(maxAllowedSpeed).length(),
                                                               String.valueOf(minAllowedSpeed).length()));

         // ------------------------------------------------------------------------------------------------------------
         // Define and configure the widgets for displaying the current state
         // ------------------------------------------------------------------------------------------------------------

         currentPositionTextField = new JTextField(String.valueOf(POSITION_INITIAL_VALUE), numColumnsForTextFields);
         currentPositionTextField.setFont(GUIConstants.FONT_NORMAL);
         currentPositionTextField.setEditable(false);
         currentPositionTextField.setMaximumSize(currentPositionTextField.getPreferredSize());
         currentPositionTextField.setMinimumSize(currentPositionTextField.getPreferredSize());

         currentSpecifiedPositionTextField = new JTextField(String.valueOf(POSITION_INITIAL_VALUE), numColumnsForTextFields);
         currentSpecifiedPositionTextField.setFont(GUIConstants.FONT_NORMAL);
         currentSpecifiedPositionTextField.setEditable(false);
         currentSpecifiedPositionTextField.setMaximumSize(currentSpecifiedPositionTextField.getPreferredSize());
         currentSpecifiedPositionTextField.setMinimumSize(currentSpecifiedPositionTextField.getPreferredSize());

         currentSpecifiedSpeedTextField = new JTextField(String.valueOf(SPEED_INITIAL_VALUE), numColumnsForTextFields);
         currentSpecifiedSpeedTextField.setFont(GUIConstants.FONT_NORMAL);
         currentSpecifiedSpeedTextField.setEditable(false);
         currentSpecifiedSpeedTextField.setMaximumSize(currentSpecifiedSpeedTextField.getPreferredSize());
         currentSpecifiedSpeedTextField.setMinimumSize(currentSpecifiedSpeedTextField.getPreferredSize());

         final TitledBorder currentStatePanelTitledBorder = BorderFactory.createTitledBorder(RESOURCES.getString("label.current"));
         currentStatePanelTitledBorder.setTitleFont(GUIConstants.FONT_NORMAL);

         final JLabel currentPositionLabel = SwingUtils.createLabel(RESOURCES.getString("label.current-absolute-position"));
         final JLabel currentSpecifiedPositionLabel = SwingUtils.createLabel(RESOURCES.getString("label.current-desired-absolute-position"));
         final JLabel currentSpecifiedSpeedLabel = SwingUtils.createLabel(RESOURCES.getString("label.current-desired-speed"));

         // ------------------------------------------------------------------------------------------------------------
         // Layout the widgets for displaying the current state
         // ------------------------------------------------------------------------------------------------------------

         final JPanel currentStatePanel = new JPanel();
         final GroupLayout currentStatePanelLayout = new GroupLayout(currentStatePanel);
         currentStatePanel.setLayout(currentStatePanelLayout);
         currentStatePanel.setBorder(currentStatePanelTitledBorder);

         currentStatePanelLayout.setHorizontalGroup(
               currentStatePanelLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                     .addGroup(currentStatePanelLayout.createSequentialGroup()
                                     .addComponent(currentPositionLabel)
                                     .addComponent(currentPositionTextField))
                     .addGroup(currentStatePanelLayout.createSequentialGroup()
                                     .addComponent(currentSpecifiedPositionLabel)
                                     .addComponent(currentSpecifiedPositionTextField))
                     .addGroup(currentStatePanelLayout.createSequentialGroup()
                                     .addComponent(currentSpecifiedSpeedLabel)
                                     .addComponent(currentSpecifiedSpeedTextField))
         );
         currentStatePanelLayout.setVerticalGroup(
               currentStatePanelLayout.createSequentialGroup()
                     .addGroup(currentStatePanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
                                     .addComponent(currentPositionLabel)
                                     .addComponent(currentPositionTextField))
                     .addGroup(currentStatePanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
                                     .addComponent(currentSpecifiedPositionLabel)
                                     .addComponent(currentSpecifiedPositionTextField))
                     .addGroup(currentStatePanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
                                     .addComponent(currentSpecifiedSpeedLabel)
                                     .addComponent(currentSpecifiedSpeedTextField))
         );

         // ------------------------------------------------------------------------------------------------------------
         // Define and configure the widgets for changing the current state
         // ------------------------------------------------------------------------------------------------------------

         final TitledBorder desiredStatePanelTitledBorder = BorderFactory.createTitledBorder(RESOURCES.getString("label.desired"));
         desiredStatePanelTitledBorder.setTitleFont(GUIConstants.FONT_NORMAL);

         final JLabel desiredPositionLabel = SwingUtils.createLabel(RESOURCES.getString("label.desired-position"));
         final JLabel desiredSpeedLabel = SwingUtils.createLabel(RESOURCES.getString("label.desired-speed"));
         final Component spacer = Box.createRigidArea(setPositionAndSpeedButton.getPreferredSize());

         desiredPositionTextField.setColumns(numColumnsForTextFields);
         desiredSpeedTextField.setColumns(numColumnsForTextFields);

         desiredPositionTextField.setFont(GUIConstants.FONT_NORMAL);
         desiredSpeedTextField.setFont(GUIConstants.FONT_NORMAL);

         desiredPositionTextField.setMaximumSize(currentPositionTextField.getPreferredSize());
         desiredPositionTextField.setMinimumSize(currentPositionTextField.getPreferredSize());
         desiredSpeedTextField.setMaximumSize(currentPositionTextField.getPreferredSize());
         desiredSpeedTextField.setMinimumSize(currentPositionTextField.getPreferredSize());

         desiredPositionTextField.addKeyListener(
               new KeyAdapter()
               {
               public void keyReleased(final KeyEvent e)
                  {
                  if (isDesiredPositionTextFieldValid())
                     {
                     desiredPositionTextField.setBackground(Color.WHITE);
                     }
                  else
                     {
                     desiredPositionTextField.setBackground(GUIConstants.TEXT_FIELD_BACKGROUND_COLOR_HAS_ERROR);
                     }
                  enableSetButtonIfInputsAreValid();
                  }
               });
         desiredSpeedTextField.addKeyListener(
               new KeyAdapter()
               {
               public void keyReleased(final KeyEvent e)
                  {
                  if (isDesiredSpeedTextFieldValid())
                     {
                     desiredSpeedTextField.setBackground(GUIConstants.TEXT_FIELD_BACKGROUND_COLOR_NO_ERROR);
                     }
                  else
                     {
                     desiredSpeedTextField.setBackground(GUIConstants.TEXT_FIELD_BACKGROUND_COLOR_HAS_ERROR);
                     }
                  enableSetButtonIfInputsAreValid();
                  }
               });

         setPositionAndSpeedButton.addActionListener(
               new AbstractTimeConsumingAction(panel)
               {
               protected Object executeTimeConsumingAction()
                  {
                  final Integer position = getTextFieldValueAsInteger(desiredPositionTextField);
                  final Integer speed = getTextFieldValueAsInteger(desiredSpeedTextField);
                  if (position != null && speed != null)
                     {
                     service.setPosition(getDeviceIndex(), position, speed);
                     }
                  return null;
                  }
               });

         // ------------------------------------------------------------------------------------------------------------
         // Layout the widgets for changing the current state
         // ------------------------------------------------------------------------------------------------------------

         final JPanel desiredStatePanel = new JPanel();
         final GroupLayout desiredStatePanelLayout = new GroupLayout(desiredStatePanel);
         desiredStatePanel.setLayout(desiredStatePanelLayout);
         desiredStatePanel.setBorder(desiredStatePanelTitledBorder);
         desiredStatePanelLayout.setHorizontalGroup(
               desiredStatePanelLayout.createSequentialGroup()
                     .addGroup(desiredStatePanelLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                                     .addComponent(desiredPositionLabel)
                                     .addComponent(desiredSpeedLabel))
                     .addGroup(desiredStatePanelLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                                     .addComponent(desiredPositionTextField)
                                     .addComponent(desiredSpeedTextField))
                     .addGroup(desiredStatePanelLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                                     .addComponent(setPositionAndSpeedButton)
                                     .addComponent(spacer))
         );
         desiredStatePanelLayout.setVerticalGroup(
               desiredStatePanelLayout.createSequentialGroup()
                     .addGroup(desiredStatePanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
                                     .addComponent(desiredPositionLabel)
                                     .addComponent(desiredPositionTextField)
                                     .addComponent(setPositionAndSpeedButton))
                     .addGroup(desiredStatePanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
                                     .addComponent(desiredSpeedLabel)
                                     .addComponent(desiredSpeedTextField)
                                     .addComponent(spacer))
         );

         // ------------------------------------------------------------------------------------------------------------
         // Layout of the entire panel
         // ------------------------------------------------------------------------------------------------------------

         final JPanel devicePanel = new JPanel();
         devicePanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.GRAY, 1),
                                                                  BorderFactory.createEmptyBorder(5, 5, 5, 5)));
         devicePanel.setLayout(new BoxLayout(devicePanel, BoxLayout.Y_AXIS));
         devicePanel.add(currentStatePanel);
         devicePanel.add(SwingUtils.createRigidSpacer());
         devicePanel.add(desiredStatePanel);

         panel.setBackground(Color.WHITE);
         panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
         panel.add(SwingUtils.createLabel(String.valueOf(deviceIndex + 1)));
         panel.add(SwingUtils.createRigidSpacer());
         panel.add(devicePanel);
         }

      private void enableSetButtonIfInputsAreValid()
         {
         setPositionAndSpeedButton.setEnabled(areInputsValid());
         }

      // this method does not need to run in the GUI thread
      private boolean areInputsValid()
         {
         return isDesiredPositionTextFieldValid() && isDesiredSpeedTextFieldValid();
         }

      // this method does not need to run in the GUI thread
      private boolean isDesiredPositionTextFieldValid()
         {
         return isIntegerTextFieldValid(desiredPositionTextField, minAllowedPositionDelta, maxAllowedPositionDelta);
         }

      // this method does not need to run in the GUI thread
      private boolean isDesiredSpeedTextFieldValid()
         {
         return isIntegerTextFieldValid(desiredSpeedTextField, minAllowedSpeed, maxAllowedSpeed);
         }

      // this method does not need to run in the GUI thread
      @SuppressWarnings({"UnusedCatchParameter"})
      private boolean isIntegerTextFieldValid(final JTextField textField, final int minValue, final int maxValue)
         {
         final Integer value = getTextFieldValueAsInteger(textField);
         return (value != null) && (value >= minValue) && (value <= maxValue);
         }

      // this method does not need to run in the GUI thread

      /** Retrieves the value from the specified text field as an <code>Integer</code>. */
      @SuppressWarnings({"UnusedCatchParameter"})
      private Integer getTextFieldValueAsInteger(final JTextField textField)
         {
         try
            {
            final String valueStr = getTextFieldValueAsString(textField);
            return (valueStr == null || valueStr.length() <= 0) ? null : Integer.parseInt(valueStr);
            }
         catch (Exception e)
            {
            // do nothing, just let it fall through and return null
            }
         return null;
         }

      /** Retrieves the value from the specified text field as a {@link String}. */
      @SuppressWarnings({"UnusedCatchParameter"})
      private String getTextFieldValueAsString(final JTextField textField)
         {
         final String text;
         if (SwingUtilities.isEventDispatchThread())
            {
            text = textField.getText();
            }
         else
            {
            final String[] textFieldValue = new String[1];
            try
               {
               SwingUtilities.invokeAndWait(
                     new Runnable()
                     {
                     public void run()
                        {
                        textFieldValue[0] = textField.getText();
                        }
                     });
               }
            catch (Exception e)
               {
               LOG.error("Exception while getting the value from text field.  Returning null instead.");
               textFieldValue[0] = null;
               }

            text = textFieldValue[0];
            }

         return (text != null) ? text.trim() : null;
         }

      private void setCurrentState(final PositionControllableMotorState state)
         {
         SwingUtilities.invokeLater(
               new Runnable()
               {
               public void run()
                  {
                  currentPositionTextField.setText(String.valueOf(state.getCurrentPosition()));
                  currentSpecifiedPositionTextField.setText(String.valueOf(state.getSpecifiedPosition()));
                  currentSpecifiedSpeedTextField.setText(String.valueOf(state.getSpecifiedSpeed()));
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

      public void getFocus()
      {
          //TODO: Placeholder
          currentPositionTextField.requestFocus();
      }

      private void updateGUI(final int specifiedPosition, final int specifiedSpeed)
         {
         // Update the slider, but we don't want to rely on the execution strategy in order for the call to the
         // service to be made since the execution strategy won't get executed if there's no change in the slider's
         // value.  This can happen if the device's state is changed by some other means than via the service, such
         // as calling emergency stop.

         this.setIntegerTextFieldValue(desiredPositionTextField, specifiedPosition);
         this.setIntegerTextFieldValue(desiredSpeedTextField, specifiedSpeed);
         }

      private void setIntegerTextFieldValue(final JTextField textField, final int value)
         {
         if (SwingUtilities.isEventDispatchThread())
            {
            setIntegerTextFieldValueWorkhorse(textField, value);
            }
         else
            {
            SwingUtilities.invokeLater(
                  new Runnable()
                  {
                  public void run()
                     {
                     setIntegerTextFieldValueWorkhorse(textField, value);
                     }
                  });
            }
         }

      private void setIntegerTextFieldValueWorkhorse(final JTextField textField, final int value)
         {
         textField.setText(String.valueOf(value));
         enableSetButtonIfInputsAreValid();
         }

      public boolean execute(final String operationName, final Map<String, String> parameterMap)
         {
         if (OPERATION_NAME.equals(operationName))
            {
            final String specifiedPositionStr = parameterMap.get(PARAMETER_NAME_POSITION);
            final String specifiedSpeedStr = parameterMap.get(PARAMETER_NAME_SPEED);
            try
               {
               final int specifiedPosition = Integer.parseInt(specifiedPositionStr);
               final int specifiedSpeed = Integer.parseInt(specifiedSpeedStr);

               updateGUI(specifiedPosition, specifiedSpeed);

               // execute the operation on the service
               service.setPosition(getDeviceIndex(), specifiedPosition, specifiedSpeed);
               return true;
               }
            catch (NumberFormatException e)
               {
               LOG.error("NumberFormatException while trying to convert the position [" + specifiedPositionStr + "] or speed [" + specifiedSpeedStr + "] to an integer.", e);
               }
            }
         return false;
         }

      public String getCurrentOperationName()
         {
         if (areInputsValid())
            {
            return OPERATION_NAME;
            }

         return null;
         }

      public Set<XmlParameter> buildParameters()
         {
         final Integer specifiedPosition = getTextFieldValueAsInteger(desiredPositionTextField);
         final Integer specifiedSpeed = getTextFieldValueAsInteger(desiredSpeedTextField);

         if (specifiedPosition != null && specifiedSpeed != null)
            {
            final Set<XmlParameter> parameters = new HashSet<XmlParameter>();
            parameters.add(new XmlParameter(PARAMETER_NAME_POSITION, specifiedPosition));
            parameters.add(new XmlParameter(PARAMETER_NAME_SPEED, specifiedSpeed));
            return parameters;
            }

         return null;
         }
      }
   }