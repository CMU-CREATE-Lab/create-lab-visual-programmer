package edu.cmu.ri.createlab.expressionbuilder.controlpanel.services.buzzer;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.lang.reflect.InvocationTargetException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.PropertyResourceBundle;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.NumberFormatter;
import edu.cmu.ri.createlab.audio.AudioHelper;
import edu.cmu.ri.createlab.expressionbuilder.controlpanel.AbstractServiceControlPanel;
import edu.cmu.ri.createlab.expressionbuilder.controlpanel.AbstractServiceControlPanelDevice;
import edu.cmu.ri.createlab.expressionbuilder.controlpanel.ControlPanelManager;
import edu.cmu.ri.createlab.expressionbuilder.controlpanel.ServiceControlPanelDevice;
import edu.cmu.ri.createlab.expressionbuilder.util.IntegerFormatter;
import edu.cmu.ri.createlab.expressionbuilder.widgets.PianoGUI;
import edu.cmu.ri.createlab.terk.expression.XmlParameter;
import edu.cmu.ri.createlab.terk.services.Service;
import edu.cmu.ri.createlab.terk.services.buzzer.BuzzerService;
import edu.cmu.ri.createlab.userinterface.GUIConstants;
import edu.cmu.ri.createlab.userinterface.util.AbstractTimeConsumingAction;
import edu.cmu.ri.createlab.userinterface.util.ImageUtils;
import edu.cmu.ri.createlab.userinterface.util.SwingUtils;
import org.apache.log4j.Logger;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public final class BuzzerServiceControlPanel extends AbstractServiceControlPanel
   {
   private static final Logger LOG = Logger.getLogger(BuzzerServiceControlPanel.class);

   private static final PropertyResourceBundle RESOURCES = (PropertyResourceBundle)PropertyResourceBundle.getBundle(BuzzerServiceControlPanel.class.getName());

   private static final Set<String> PARAMETER_NAMES = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(BuzzerService.PARAMETER_NAME_FREQUENCY, BuzzerService.PARAMETER_NAME_DURATION)));
   private static final Map<String, Set<String>> OPERATIONS_TO_PARAMETERS_MAP;

   static
      {
      final Map<String, Set<String>> operationsToParametersMap = new HashMap<String, Set<String>>();
      operationsToParametersMap.put(BuzzerService.OPERATION_NAME_PLAY_TONE, PARAMETER_NAMES);
      OPERATIONS_TO_PARAMETERS_MAP = Collections.unmodifiableMap(operationsToParametersMap);
      }

   private final BuzzerService service;

   public BuzzerServiceControlPanel(final ControlPanelManager controlPanelManager, final BuzzerService service)
      {
      super(controlPanelManager, service, OPERATIONS_TO_PARAMETERS_MAP);
      this.service = service;
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

   public JLabel getLabelImage(final String imageName)
      {
      return new JLabel(ImageUtils.createImageIcon(RESOURCES.getString(imageName)));
      }

   public void refresh()
      {
      LOG.debug("BuzzerServiceControlPanel.refresh()");

      // nothing to do here
      }

   protected ServiceControlPanelDevice createServiceControlPanelDevice(final Service service, final int deviceIndex)
      {
      return new ControlPanelDevice(deviceIndex);
      }

   private final class ControlPanelDevice extends AbstractServiceControlPanelDevice
      {
      private static final int DEFAULT_FREQUENCY = 440;
      private static final int DEFAULT_DURATION = 500;

      private final JPanel panel = new JPanel();
      private final JLayeredPane layers = new JLayeredPane();

      private final JLabel frequencyLabel = SwingUtils.createLabel(RESOURCES.getString("control-panel.label.tone.frequency"));
      private final JLabel durationLabel = SwingUtils.createLabel(RESOURCES.getString("control-panel.label.tone.duration"));

      private JLabel blockIcon = new JLabel();

      private final ImageIcon act_icon = ImageUtils.createImageIcon(RESOURCES.getString("image.yellow"));
      private final ImageIcon dis_icon = ImageUtils.createImageIcon(RESOURCES.getString("image.yellowdisabled"));
      private final ImageIcon off_icon = ImageUtils.createImageIcon(RESOURCES.getString("image.yellowoff"));

      private final JTextField frequencyTextField = new JTextField(5);
      private final NumberFormatter formatter = new IntegerFormatter();

      private final PianoGUI piano;// = new PianoGUI();
      private final Component toneKeyboard;// = piano.getComponent();

      private JFormattedTextField durationTextField = new JFormattedTextField(new DefaultFormatterFactory(formatter, formatter, formatter));
      private final boolean isToneDurationInSeconds = true;
      private final JButton playToneButton = createPlayButton();

      private final KeyAdapter toneFieldsKeyListener =
            new KeyAdapter()
            {
            public void keyReleased(final KeyEvent e)
               {
               enablePlayToneButtonIfInputsAreValid();
               }
            };

      private ControlPanelDevice(final int deviceIndex)
         {
         super(deviceIndex);

         FocusListener autoSelectOnFocus = new FocusListener()
         {
         @Override
         public void focusGained(FocusEvent e)
            {
            final JTextField source = (JTextField)e.getSource();
            SwingUtilities.invokeLater(
                  new Runnable()
                  {
                  @Override
                  public void run()
                     {
                     source.setText(source.getText());
                     source.selectAll();
                     source.repaint();
                     }
                  });
            }

         @Override
         public void focusLost(FocusEvent e)
            {
            //To change body of implemented methods use File | Settings | File Templates.
            }
         };

         if (isToneDurationInSeconds)
            {
            durationTextField = new JFormattedTextField(NumberFormat.getNumberInstance());
            }
         else
            {
            final NumberFormatter formatter = new DoubleFormatter();
            durationTextField = new JFormattedTextField(new DefaultFormatterFactory(formatter, formatter, formatter));
            }

         durationTextField.setColumns(DEFAULT_TEXT_FIELD_COLUMNS);
         durationTextField.setFont(GUIConstants.FONT_NORMAL);
         durationTextField.setSelectedTextColor(Color.WHITE);
         durationTextField.setSelectionColor(Color.BLUE);

         durationTextField.setMaximumSize(durationTextField.getPreferredSize());
         frequencyTextField.setMaximumSize(frequencyTextField.getPreferredSize());

         durationTextField.setName("audioDuration");
         durationTextField.addFocusListener(
               new FocusAdapter()
               {
               public void focusLost(final FocusEvent e)
                  {
                  durationTextField.setBackground(Color.WHITE);
                  }
               }
         );

         final ActionListener playToneAction = new PlayToneAction();

         final SpinnerNumberModel amplitudeModel = new SpinnerNumberModel(AudioHelper.DEFAULT_AMPLITUDE,
                                                                          AudioHelper.MIN_AMPLITUDE,
                                                                          AudioHelper.MAX_AMPLITUDE,
                                                                          1);

         frequencyTextField.setEditable(false);
         piano = new PianoGUI(playToneAction);
         toneKeyboard = piano.getComponent();

         this.setDuration(DEFAULT_DURATION);
         this.setFrequency(DEFAULT_FREQUENCY);

         frequencyTextField.setName("freqField");
         frequencyTextField.addKeyListener(toneFieldsKeyListener);
         durationTextField.addKeyListener(
               new KeyAdapter()
               {
               public void keyReleased(final KeyEvent e)
                  {
                  if (isDurationTextFieldValid())
                     {
                     durationTextField.setBackground(GUIConstants.TEXT_FIELD_BACKGROUND_COLOR_NO_ERROR);
                     }
                  else
                     {
                     durationTextField.setBackground(GUIConstants.TEXT_FIELD_BACKGROUND_COLOR_HAS_ERROR);
                     }
                  enablePlayToneButtonIfInputsAreValid();
                  }
               }
         );

         frequencyTextField.addActionListener(playToneAction);
         durationTextField.addActionListener(playToneAction);

         playToneButton.addActionListener(playToneAction);

         playToneButton.setFocusable(false);

         playToneButton.setMnemonic(KeyEvent.VK_P);

         frequencyTextField.setFocusable(false);
         durationTextField.addFocusListener(autoSelectOnFocus);

         final JPanel toneGroupPanel = new JPanel();

         final GroupLayout toneGroupPanelLayout = new GroupLayout(panel);
         panel.setLayout(toneGroupPanelLayout);
         panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.black), BorderFactory.createEmptyBorder(2, 2, 2, 2)));
         panel.setBackground(Color.WHITE);

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

         toneGroupPanelLayout.setAutoCreateGaps(true);
         toneGroupPanelLayout.setAutoCreateContainerGaps(true);

         toneGroupPanelLayout.setHorizontalGroup(
               toneGroupPanelLayout.createSequentialGroup()
                     .addGroup(toneGroupPanelLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                                     .addComponent(frequencyLabel)
                                     .addComponent(durationLabel))
                     .addGroup(toneGroupPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                     .addComponent(frequencyTextField, 50, 50, 50)
                                     .addComponent(durationTextField, 50, 50, 50)
                     )
                     .addComponent(toneKeyboard)
                     .addComponent(playToneButton)
         );

         toneGroupPanelLayout.setVerticalGroup(
               toneGroupPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
                     .addGroup(toneGroupPanelLayout.createSequentialGroup()
                                     .addGroup(toneGroupPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
                                                     .addComponent(frequencyLabel)
                                                     .addComponent(frequencyTextField))
                                     .addGroup(toneGroupPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
                                                     .addComponent(durationLabel)
                                                     .addComponent(durationTextField)))
                     .addComponent(playToneButton)
                     .addComponent(toneKeyboard)
         );

         playToneButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

         final Dimension pSize = new Dimension(630, 80);
         final Dimension itSize = iconTitle.getPreferredSize();
         layers.add(panel, new Integer(1));
         layers.add(iconTitle, new Integer(2));

         iconTitle.setBounds(0, 5, itSize.width, itSize.height);
         panel.setBounds(itSize.width + 5, 5, pSize.width, pSize.height);

         //layer.setPreferredSize(new Dimension(sSize.width + 40, sSize.height));
         layers.setPreferredSize(new Dimension(pSize.width + itSize.width + 5, pSize.height + 5));
         layers.setMinimumSize(new Dimension(pSize.width + itSize.width + 5, pSize.height + 5));
         }

      private static final int DEFAULT_TEXT_FIELD_COLUMNS = 5;

      public Component getComponent()
         {
         {
         final JPanel act_box = new JPanel();
         final JPanel dis_box = new JPanel();
         final JLabel icon = new JLabel(ImageUtils.createImageIcon(RESOURCES.getString("image.disabled")));
         icon.setAlignmentX(Component.LEFT_ALIGNMENT);
         icon.setToolTipText("Audio is disabled");
         act_box.setName("active_service_box");
         act_box.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 5));
         act_box.setLayout(new BoxLayout(act_box, BoxLayout.Y_AXIS));
         act_box.add(layers);

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

      public void getFocus()
         {
         frequencyTextField.requestFocus();
         //TODO: Placeholder
         }

      public boolean execute(final String operationName, final Map<String, String> parameterMap)
         {
         if (BuzzerService.OPERATION_NAME_PLAY_TONE.equals(operationName))
            {
            final String freqStr = parameterMap.get(BuzzerService.PARAMETER_NAME_FREQUENCY);
            final String durStr = parameterMap.get(BuzzerService.PARAMETER_NAME_DURATION);
            try
               {
               final int frequency = Integer.parseInt(freqStr);
               final int duration = Integer.parseInt(durStr);

               // update the GUI
               updateToneGUI(frequency, duration);

               // execute the operation on the service
               service.playTone(getDeviceIndex(), frequency, duration);
               return true;
               }
            catch (NumberFormatException e)
               {
               LOG.error("NumberFormatException while trying to convert frequency or duration to an integer.", e);
               }
            catch (Exception e)
               {
               LOG.error("Exception while trying to load or execute the operation.", e);
               }
            }
         return false;
         }

      private JButton createPlayButton()
         {
         final JButton playButton = new JButton("Play", ImageUtils.createImageIcon(RESOURCES.getString("button.play-image")));
         playButton.setFont(GUIConstants.BUTTON_FONT);
         playButton.setName("PlayButton");
         return playButton;
         }

      public String getCurrentOperationName()
         {
         return BuzzerService.OPERATION_NAME_PLAY_TONE;
         }

      public Set<XmlParameter> buildParameters()
         {
         LOG.debug("BuzzerServiceControlPanel$ControlPanelDevice.buildParameters()");

         final Integer f = getFrequency();
         final Integer d = getDuration();

         if (f != null && d != null)
            {
            final Set<XmlParameter> parameters = new HashSet<XmlParameter>();
            parameters.add(new XmlParameter(BuzzerService.PARAMETER_NAME_FREQUENCY, f));
            parameters.add(new XmlParameter(BuzzerService.PARAMETER_NAME_DURATION, d));
            return parameters;
            }

         return null;
         }

      private void enablePlayToneButtonIfInputsAreValid()
         {
         playToneButton.setEnabled(areToneInputsValid());
         }

      // MUST be called from the Swing thread!
      private boolean areToneInputsValid()
         {
         return getTextFieldValueAsInteger(frequencyTextField) != null && isDurationTextFieldValid();
         }

      private boolean isDurationTextFieldValid()
         {
         final String text = durationTextField.getText();
         if (text == null)
            {
            return false;
            }

         try
            {
            final Number number;
            if (isToneDurationInSeconds)
               {
               number = Double.parseDouble(text);
               }
            else
               {
               number = Integer.parseInt(text);
               }
            durationTextField.commitEdit();
            return number.doubleValue() > 0;
            }
         catch (NumberFormatException e1)
            {
            return false;
            }
         catch (ParseException e)
            {
            return false;
            }
         }

      /** Retrieves the value from the specified text field as an <code>Integer</code>. */
      private Integer getTextFieldValueAsInteger(final JTextField textField)
         {
         try
            {
            final String valueStr = getTextFieldValueAsString(textField);
            return (valueStr == null || valueStr.length() <= 0) ? null : Integer.parseInt(valueStr);
            }
         catch (Exception e)
            {
            LOG.error("Exception while retrieving int value", e);
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

      private Integer getFrequency()
         {
         return getTextFieldValueAsInteger(frequencyTextField);
         }

      /** Returns the duration, in milliseconds. */
      private Integer getDuration()
         {
         if (isDurationTextFieldValid())
            {
            if (isToneDurationInSeconds)
               {
               final double value = ((Number)durationTextField.getValue()).doubleValue();
               return (int)Math.round(value * 1000);
               }
            else
               {
               return getTextFieldValueAsInteger(durationTextField);
               }
            }

         return null;
         }

      private void setFrequency(final int frequency)
         {
         setToneTextFieldValueWorkhorse(frequencyTextField, frequency);
         piano.setSelectedFrequency(frequency);
         }

      /** Sets the duration, in milliseconds. */
      private void setDuration(final int duration)
         {
         if (isToneDurationInSeconds)
            {
            final Double durationInSeconds = (double)duration / 1000;
            setToneFormattedTextFieldValueWorkhorse(durationTextField, durationInSeconds);
            }
         else
            {
            setToneFormattedTextFieldValueWorkhorse(durationTextField, duration);
            }
         }

      private void setToneTextFieldValueWorkhorse(final JTextField textField, final int value)
         {
         if (SwingUtilities.isEventDispatchThread())
            {
            textField.setText(String.valueOf(value));
            enablePlayToneButtonIfInputsAreValid();
            }
         else
            {
            SwingUtilities.invokeLater(
                  new Runnable()
                  {
                  public void run()
                     {
                     textField.setText(String.valueOf(value));
                     enablePlayToneButtonIfInputsAreValid();
                     }
                  });
            }
         }

      private Integer getSpinnerValueAsInteger(final JSpinner spinner)
         {
         if (SwingUtilities.isEventDispatchThread())
            {
            return (Integer)spinner.getValue();
            }
         else
            {
            final Integer[] value = new Integer[1];
            try
               {
               SwingUtilities.invokeAndWait(
                     new Runnable()
                     {
                     public void run()
                        {
                        value[0] = (Integer)spinner.getValue();
                        }
                     }
               );
               return value[0];
               }
            catch (InterruptedException e)
               {
               LOG.error("InterruptedException while fetching the spinner value", e);
               }
            catch (InvocationTargetException e)
               {
               LOG.error("InvocationTargetException while fetching the spinner value", e);
               }
            }

         return null;
         }

      private void setToneFormattedTextFieldValueWorkhorse(final JFormattedTextField textField, final Object value)
         {
         if (SwingUtilities.isEventDispatchThread())
            {
            textField.setValue(value);
            enablePlayToneButtonIfInputsAreValid();
            }
         else
            {
            SwingUtilities.invokeLater(
                  new Runnable()
                  {
                  public void run()
                     {
                     textField.setValue(value);
                     enablePlayToneButtonIfInputsAreValid();
                     }
                  });
            }
         }

      private class DoubleFormatter extends NumberFormatter
         {
         public String valueToString(final Object o)
               throws ParseException
            {
            Number number = (Number)o;
            if (number != null)
               {
               final int val = number.intValue();
               number = new Integer(val);
               }

            // get rid of the freakin' commas!
            return super.valueToString(number).replaceAll("[^\\d]", "");
            }

         public Object stringToValue(final String s)
               throws ParseException
            {
            Number number = (Number)super.stringToValue(s);
            if (number != null)
               {
               final int val = number.intValue();
               number = new Integer(val);
               }
            return number;
            }
         }

      private void updateToneGUI(final int frequency, final int duration)
         {
         // Update the GUI, but don't execute the operation
         SwingUtilities.invokeLater(
               new Runnable()
               {
               public void run()
                  {
                  setFrequency(frequency);
                  setDuration(duration);
                  }
               });
         }

      @SuppressWarnings({"CloneableClassWithoutClone"})
      private class PlayToneAction extends AbstractTimeConsumingAction
         {
         private int frequency;
         private int duration;

         private boolean isValid;

         protected void executeGUIActionBefore()
            {
            isValid = areToneInputsValid();
            if (isValid)
               {
               int freq = piano.getSelectedFrequency();
               frequencyTextField.setText(String.valueOf(freq));
               frequency = getTextFieldValueAsInteger(frequencyTextField);

               duration = getDuration();
               }
            }

         protected Object executeTimeConsumingAction()
            {
            if (isValid)
               {
               service.playTone(getDeviceIndex(), frequency, duration);
               }
            return null;
            }
         }
      }
   }