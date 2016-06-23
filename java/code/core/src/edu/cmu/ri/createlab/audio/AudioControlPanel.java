package edu.cmu.ri.createlab.audio;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.PropertyResourceBundle;
import java.util.Set;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.NumberFormatter;
import edu.cmu.ri.createlab.expressionbuilder.widgets.PianoGUI;
import edu.cmu.ri.createlab.terk.services.ExceptionHandler;
import edu.cmu.ri.createlab.userinterface.GUIConstants;
import edu.cmu.ri.createlab.userinterface.util.AbstractTimeConsumingAction;
import edu.cmu.ri.createlab.userinterface.util.ImageUtils;
import edu.cmu.ri.createlab.userinterface.util.SwingUtils;
import edu.cmu.ri.createlab.util.ZipSave;
import edu.cmu.ri.createlab.visualprogrammer.PathManager;
import org.apache.log4j.Logger;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public final class AudioControlPanel extends JPanel
   {
   private static final Logger LOG = Logger.getLogger(AudioControlPanel.class);

   private static final PropertyResourceBundle RESOURCES = (PropertyResourceBundle)PropertyResourceBundle.getBundle(AudioControlPanel.class.getName());

   public static final String TONE_FREQUENCY_PROPERTY_KEY = "audio-control-panel.label.tone.frequency";
   public static final String TONE_DURATION_PROPERTY_KEY = "audio-control-panel.label.tone.duration";
   public static final String TONE_AMPLITUDE_PROPERTY_KEY = "audio-control-panel.label.tone.amplitude";
   public static final String IS_TONE_DURATION_SPECIFIED_IN_SECONDS_PROPERTY_KEY = "audio-control-panel.tone.duration.specified-in-seconds";
   private static final int DEFAULT_FREQUENCY = 440;
   private static final int DEFAULT_DURATION = 500;

   public interface EventListener
      {
      void playTone(final int frequency, final int amplitude, final int duration);

      //void playSound(final File file, final ExceptionHandler exceptionHandler);

      void playFile(final File file, final ExceptionHandler exceptionHandler);

      void playSpeech(final String speechText);
      }

   public static enum Mode
      {
         TONE(RESOURCES.getString("tab.label.tone")),
         CLIP(RESOURCES.getString("tab.label.clip")),
         SPEECH(RESOURCES.getString("tab.label.speech"));

      private final String name;

      private Mode(final String name)
         {
         this.name = name;
         }

      public String getName()
         {
         return name;
         }

      public String toString()
         {
         return "Mode{" +
                "name='" + name + '\'' +
                '}';
         }
      }

   private static final int DEFAULT_TEXT_FIELD_COLUMNS = 5;

   public static void main(final String[] args)
      {
      //Schedule a job for the event-dispatching thread: creating and showing this application's GUI.
      SwingUtilities.invokeLater(
            new Runnable()
               {
               public void run()
                  {
                  final JFrame jFrame = new JFrame("AudioControlPanel");

                  // add the root panel to the JFrame
                  final AudioClipChooser audioClipChooser = new TerkAudioClipChooser();
                  final AudioControlPanel audioControlPanel = new AudioControlPanel(jFrame, audioClipChooser);
                  audioControlPanel.addEventListener(
                        new EventListener()
                           {
                           public void playTone(final int frequency, final int amplitude, final int duration)
                              {
                              LOG.info("AudioControlPanel.playTone(" + frequency + "," + amplitude + "," + duration + ")");
                              }

                           public void playFile(final File file, final ExceptionHandler exceptionHandler)
                              {
                              LOG.info("AudioControlPanel.playFile(" + file.getAbsolutePath() + ")");
                              }

                           public void playSound(final File file, final ExceptionHandler exceptionHandler)
                              {
                              LOG.info("AudioControlPanel.playSound(" + file.getAbsolutePath() + ")");
                              }

                           public void playSpeech(final String speechText)
                              {
                              LOG.info("AudioControlPanel.playSpeech(" + speechText + ")");
                              }
                           }
                  );
                  jFrame.add(audioControlPanel);

                  // set various properties for the JFrame
                  jFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
                  jFrame.setBackground(Color.WHITE);
                  jFrame.setResizable(true);
                  jFrame.pack();
                  jFrame.setLocationRelativeTo(null);// center the window on the screen
                  jFrame.setVisible(true);
                  }
               });
      }

   private static String getPropertyFromSystemOrResource(final String propertyKey)
      {
      return System.getProperty(propertyKey, RESOURCES.getString(propertyKey));
      }

   //private final JTabbedPane tabbedPane;
   private final JPanel cardDeck;
   private final Map<Integer, Mode> indexToModeMap = new HashMap<Integer, Mode>();
   private final Map<String, Integer> textToIndex = new HashMap<String, Integer>();
   private final Map<Mode, Integer> modeToIndexMap = new HashMap<Mode, Integer>();
   //private final Map<Mode, JRadioButton> modeToButton = new HashMap<Mode, JRadioButton>();
   private final Map<Mode, JCheckBox> modeToButton = new HashMap<Mode, JCheckBox>();
   private Mode currentMode;
   private Integer currentIndex;

   private final ButtonGroup audOptions;

   private final JLabel frequencyLabel = SwingUtils.createLabel(getPropertyFromSystemOrResource(TONE_FREQUENCY_PROPERTY_KEY));
   private final JLabel durationLabel = SwingUtils.createLabel(getPropertyFromSystemOrResource(TONE_DURATION_PROPERTY_KEY));
   private final JLabel amplitudeLabel = SwingUtils.createLabel(getPropertyFromSystemOrResource(TONE_AMPLITUDE_PROPERTY_KEY));
   private final JTextField frequencyTextField = createIntegerTextField();
   private final JFormattedTextField durationTextField;
   private final JSpinner amplitudeSpinner;
   private final JButton playToneButton = createPlayButton();
   private final boolean isToneDurationInSeconds = Boolean.parseBoolean(getPropertyFromSystemOrResource(IS_TONE_DURATION_SPECIFIED_IN_SECONDS_PROPERTY_KEY));

   private final PianoGUI piano;// = new PianoGUI();
   private final Component toneKeyboard;// = piano.getComponent();

   private final JButton playClipButton = createPlayButton();
   private final Runnable failedToPlayFileRunnable;
   private final Runnable invalidAudioFileRunnable;
   private final Runnable audioFileTooLargeRunnable;
   private final Runnable audioFileQueueFullRunnable;
   private final Runnable couldNotLoadAudioFileRunnable;

   private final JLabel speechLabel = SwingUtils.createLabel(RESOURCES.getString("label.text"));
   private final JTextField speechTextField = createTextField(10);
   private final JButton playSpeechButton = createPlayButton();

   private final KeyAdapter toneFieldsKeyListener =
         new KeyAdapter()
            {
            public void keyReleased(final KeyEvent e)
               {
               enablePlayToneButtonIfInputsAreValid();
               }
            };
   private final KeyAdapter speechFieldsKeyListener =
         new KeyAdapter()
            {
            public void keyReleased(final KeyEvent e)
               {
               enablePlaySpeechButtonIfInputsAreValid();
               }
            };

   private final Set<EventListener> eventListeners = new HashSet<EventListener>();
   private final AudioClipChooser audioClipChooser;

   public AudioControlPanel(final AudioClipChooser audioClipChooser)
      {
      this(null, audioClipChooser);
      }

   /**
    * <p>
    * Creates an AudioControlPanel using the given {@link Component} as the parent component (for dialogs and such). If
    * the {@link Component} is <code>null</code>, then the dialogs use this panel as the parent component.
    * </p>
    * <p>
    * Users of this class may override the default labels for the tone inputs (frequency, amplitude, and duration) by
    * setting the following system properties before the instance is constructed:
    * <ul>
    *    <li>audio-control-panel.label.tone.frequency</li>
    *    <li>audio-control-panel.label.tone.duration</li>
    *    <li>audio-control-panel.label.tone.amplitude</li>
    * </ul>
    * </p>
    */
   public AudioControlPanel(final Component parentComponent, final AudioClipChooser audioClipChooser)
      {
      final Component parent = (parentComponent == null ? this : parentComponent);
      this.audioClipChooser = audioClipChooser;
      audioClipChooser.setRecordActionListener(new recordActionListener(parentComponent));
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

      failedToPlayFileRunnable = new ErrorMessageDialogRunnable(parent,
                                                                RESOURCES.getString("dialog.message.play-failed"),
                                                                RESOURCES.getString("dialog.title.play-failed"));

      invalidAudioFileRunnable = new ErrorMessageDialogRunnable(parent,
                                                                RESOURCES.getString("dialog.message.invalid-audio-file"),
                                                                RESOURCES.getString("dialog.title.invalid-audio-file"));

      audioFileTooLargeRunnable = new ErrorMessageDialogRunnable(parent,
                                                                 RESOURCES.getString("dialog.message.audio-file-too-large"),
                                                                 RESOURCES.getString("dialog.title.play-failed"));

      audioFileQueueFullRunnable = new ErrorMessageDialogRunnable(parent,
                                                                  RESOURCES.getString("dialog.message.audio-file-queue-full"),
                                                                  RESOURCES.getString("dialog.title.play-failed"));

      couldNotLoadAudioFileRunnable = new ErrorMessageDialogRunnable(parent,
                                                                     RESOURCES.getString("dialog.message.failed-to-load-audio-file"),
                                                                     RESOURCES.getString("dialog.title.failed-to-load-audio-file"));

      final ActionListener playToneAction = new PlayToneAction();
      final ActionListener playClipAction = new PlayClipAction();
      final ActionListener playSpeechAction = new PlaySpeechAction();

      final SpinnerNumberModel amplitudeModel = new SpinnerNumberModel(AudioHelper.DEFAULT_AMPLITUDE,
                                                                       AudioHelper.MIN_AMPLITUDE,
                                                                       AudioHelper.MAX_AMPLITUDE,
                                                                       1);

      frequencyTextField.setEditable(false);
      piano = new PianoGUI(playToneAction);
      toneKeyboard = piano.getComponent();

      this.setDuration(DEFAULT_DURATION);
      this.setFrequency(DEFAULT_FREQUENCY);

      amplitudeSpinner = new JSpinner(amplitudeModel);
      amplitudeSpinner.setFont(GUIConstants.FONT_NORMAL);

/*     Dimension prefSize = new Dimension(120, 16);
     frequencyTextField.setPreferredSize(prefSize);
     durationTextField.setPreferredSize(prefSize);
     amplitudeSpinner.setPreferredSize(prefSize);

     frequencyTextField.setMinimumSize(prefSize);
     durationTextField.setMinimumSize(prefSize);
     amplitudeSpinner.setMinimumSize(prefSize);

     frequencyTextField.setMaximumSize(prefSize);
     durationTextField.setMaximumSize(prefSize);
     amplitudeSpinner.setMaximumSize(prefSize);*/

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

      amplitudeSpinner.addKeyListener(toneFieldsKeyListener);
      frequencyTextField.addActionListener(playToneAction);
      durationTextField.addActionListener(playToneAction);
      audioClipChooser.addFilePathFieldActionListener(playClipAction);
      audioClipChooser.addAudioClipChooserEventListener(
            new AudioClipChooserEventListener()
               {
               public void handleSelectedFileChange()
                  {
                  enablePlayClipButtonIfInputsAreValid();
                  }
               }
      );

      speechTextField.addKeyListener(speechFieldsKeyListener);
      speechTextField.addActionListener(playSpeechAction);
      speechTextField.setPreferredSize(new Dimension(speechTextField.getPreferredSize().width, playSpeechButton.getPreferredSize().height));
      speechTextField.setMinimumSize(new Dimension(200, playSpeechButton.getPreferredSize().height));
      speechTextField.setPreferredSize(new Dimension(200, playSpeechButton.getPreferredSize().height));
      speechTextField.setMaximumSize(new Dimension(Integer.MAX_VALUE, playSpeechButton.getPreferredSize().height));

      playToneButton.addActionListener(playToneAction);
      playClipButton.addActionListener(playClipAction);
      playSpeechButton.addActionListener(playSpeechAction);

      playClipButton.setFocusable(false);
      playSpeechButton.setFocusable(false);
      playToneButton.setFocusable(false);

      playClipButton.setMnemonic(KeyEvent.VK_P);
      playSpeechButton.setMnemonic(KeyEvent.VK_P);
      playToneButton.setMnemonic(KeyEvent.VK_P);

      frequencyTextField.setFocusable(false);
      durationTextField.addFocusListener(autoSelectOnFocus);
      speechTextField.addFocusListener(autoSelectOnFocus);

      // ===============================================================================================================

      this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
      this.setBackground(Color.WHITE);

      // ===============================================================================================================

      final JPanel toneGroupPanel = new JPanel();
      final GroupLayout toneGroupPanelLayout = new GroupLayout(toneGroupPanel);
      toneGroupPanel.setLayout(toneGroupPanelLayout);
      toneGroupPanel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
      toneGroupPanel.setBackground(Color.WHITE);

      toneGroupPanelLayout.setAutoCreateGaps(true);
      toneGroupPanelLayout.setAutoCreateContainerGaps(true);

      toneGroupPanelLayout.setHorizontalGroup(
            toneGroupPanelLayout.createSequentialGroup()
                  .addGroup(toneGroupPanelLayout.createSequentialGroup()
                                  .addGroup(toneGroupPanelLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                                                  .addComponent(frequencyLabel)
                                                  .addComponent(amplitudeLabel)
                                                  .addComponent(durationLabel))
                                  .addGroup(toneGroupPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                                  .addComponent(frequencyTextField, 50, 50, 50)
                                                  .addComponent(amplitudeSpinner, 50, 50, 50)
                                                  .addComponent(durationTextField, 50, 50, 50)
                                  )
                  )
                  .addComponent(toneKeyboard)
                  .addComponent(playToneButton)
      );

      toneGroupPanelLayout.setVerticalGroup(
            toneGroupPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
                  .addGroup(toneGroupPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
                                  .addGroup(toneGroupPanelLayout.createSequentialGroup()
                                                  .addGroup(toneGroupPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
                                                                  .addComponent(frequencyLabel)
                                                                  .addComponent(frequencyTextField))
                                                  .addGroup(toneGroupPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
                                                                  .addComponent(amplitudeLabel)
                                                                  .addComponent(amplitudeSpinner))
                                                  .addGroup(toneGroupPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
                                                                  .addComponent(durationLabel)
                                                                  .addComponent(durationTextField)))
                                  .addComponent(playToneButton))
                  .addComponent(toneKeyboard)
      );

      playToneButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

      final JPanel tonePanel = new JPanel();
      tonePanel.setLayout(new BoxLayout(tonePanel, BoxLayout.X_AXIS));
      tonePanel.setName("audioSubPanel");

      tonePanel.add(toneGroupPanel);

      // ===============================================================================================================

      final JPanel clipGroupPanel = new JPanel();
      final GroupLayout clipGroupPanelLayout = new GroupLayout(clipGroupPanel);
      clipGroupPanel.setLayout(clipGroupPanelLayout);
      clipGroupPanel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
      clipGroupPanel.setBackground(Color.WHITE);

      clipGroupPanelLayout.setAutoCreateGaps(false);
      clipGroupPanelLayout.setAutoCreateContainerGaps(true);

      clipGroupPanelLayout.setHorizontalGroup(
            clipGroupPanelLayout.createSequentialGroup()
                  .addComponent(audioClipChooser.getComponent())
                  .addComponent(playClipButton)
      );

      clipGroupPanelLayout.setVerticalGroup(
            clipGroupPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
                  .addComponent(audioClipChooser.getComponent())
                  .addComponent(playClipButton)
      );

      playClipButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
      audioClipChooser.getComponent().setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

      final JPanel clipPanel = new JPanel();
      clipPanel.setLayout(new BoxLayout(clipPanel, BoxLayout.X_AXIS));
      clipPanel.setName("audioSubPanel");

      clipPanel.add(clipGroupPanel);

      // ===============================================================================================================

      final JPanel speechGroupPanel = new JPanel();
      final GroupLayout speechGroupPanelLayout = new GroupLayout(speechGroupPanel);
      speechGroupPanel.setLayout(speechGroupPanelLayout);
      speechGroupPanel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
      speechGroupPanel.setBackground(Color.WHITE);

      speechGroupPanelLayout.setAutoCreateGaps(true);
      speechGroupPanelLayout.setAutoCreateContainerGaps(true);

      speechGroupPanelLayout.setHorizontalGroup(
            speechGroupPanelLayout.createSequentialGroup()
                  .addComponent(speechLabel)
                  .addComponent(speechTextField)
                  .addComponent(playSpeechButton)
      );

      speechGroupPanelLayout.setVerticalGroup(
            speechGroupPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
                  .addComponent(speechLabel)
                  .addComponent(speechTextField)
                  .addComponent(playSpeechButton)
      );

      playSpeechButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

      final JPanel speechPanel = new JPanel();
      speechPanel.setName("audioSubPanel");
      speechPanel.setLayout(new BoxLayout(speechPanel, BoxLayout.X_AXIS));

      speechPanel.add(speechGroupPanel);

      // ===============================================================================================================

      //================================================================================================================
      //========= MAPS

      // create the index to mode map
      indexToModeMap.put(0, Mode.TONE);
      indexToModeMap.put(1, Mode.CLIP);
      indexToModeMap.put(2, Mode.SPEECH);

      for (final Integer i : indexToModeMap.keySet())
         {
         modeToIndexMap.put(indexToModeMap.get(i), i);
         }

      textToIndex.put(Mode.TONE.getName(), 0);
      textToIndex.put(Mode.CLIP.getName(), 1);
      textToIndex.put(Mode.SPEECH.getName(), 2);

      //========= CARD LAYOUT

      final JPanel cardHolder = new JPanel();
      cardDeck = new JPanel(new CardLayout());
      cardDeck.setBorder(BorderFactory.createLineBorder(Color.black));
      final JPanel buttonDeck = new JPanel(new GridLayout(0, 1));
      buttonDeck.setName("audioOptions");

      cardDeck.add(tonePanel, Mode.TONE.getName());
      cardDeck.add(clipPanel, Mode.CLIP.getName());
      cardDeck.add(speechPanel, Mode.SPEECH.getName());

      // final JRadioButton toneButton = new JRadioButton(Mode.TONE.getName(), true);
      // final JRadioButton clipButton = new JRadioButton(Mode.CLIP.getName(), false);
      // final JRadioButton speechButton = new JRadioButton(Mode.SPEECH.getName(), false);

      // final JRadioButton toneButton = new JRadioButton(Mode.TONE.getName(), true);
      // final JRadioButton clipButton = new JRadioButton(Mode.CLIP.getName(), false);
      // final JRadioButton speechButton = new JRadioButton(Mode.SPEECH.getName(), false);

      final JCheckBox toneButton = new JCheckBox(Mode.TONE.getName(), true);
      final JCheckBox clipButton = new JCheckBox(Mode.CLIP.getName(), false);
      final JCheckBox speechButton = new JCheckBox(Mode.SPEECH.getName(), false);

      toneButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
      clipButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
      speechButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

      toneButton.setMinimumSize(toneButton.getPreferredSize());
      clipButton.setMinimumSize(clipButton.getPreferredSize());
      speechButton.setMinimumSize(speechButton.getPreferredSize());

      modeToButton.put(Mode.TONE, toneButton);
      modeToButton.put(Mode.CLIP, clipButton);
      modeToButton.put(Mode.SPEECH, speechButton);

      final ChangeListener changeListener = new ChangeListener()
         {
         public void stateChanged(ChangeEvent changEvent)
            {
            final AbstractButton aButton = (AbstractButton)changEvent.getSource();
            final ButtonModel aModel = aButton.getModel();
            final String buttonText = aButton.getText();
            if (aModel.isSelected())
               {
               currentIndex = textToIndex.get(buttonText);
               currentMode = indexToModeMap.get(currentIndex);
               final CardLayout cl = (CardLayout)(cardDeck.getLayout());
               cl.show(cardDeck, buttonText);
               }
            }
         };

     /* ActionListener checkboxUnclick = new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent e) {

          }
      };
*/
      toneButton.addChangeListener(changeListener);
      clipButton.addChangeListener(changeListener);
      speechButton.addChangeListener(changeListener);

      toneButton.setFont(GUIConstants.FONT_NORMAL);
      clipButton.setFont(GUIConstants.FONT_NORMAL);
      speechButton.setFont(GUIConstants.FONT_NORMAL);

      toneButton.setFocusable(false);
      clipButton.setFocusable(false);
      speechButton.setFocusable(false);

      audOptions = new ButtonGroup();
      audOptions.add(toneButton);
      audOptions.add(clipButton);
      audOptions.add(speechButton);

      toneButton.setMinimumSize(new Dimension(80, toneButton.getPreferredSize().height));
      toneButton.setPreferredSize(toneButton.getMinimumSize());
      clipButton.setMinimumSize(new Dimension(80, clipButton.getPreferredSize().height));
      clipButton.setPreferredSize(clipButton.getMinimumSize());
      speechButton.setMinimumSize(new Dimension(80, speechButton.getPreferredSize().height));
      speechButton.setPreferredSize(speechButton.getMinimumSize());

      buttonDeck.add(toneButton);
      buttonDeck.add(clipButton);
      buttonDeck.add(speechButton);

      currentIndex = 0;
      currentMode = indexToModeMap.get(currentIndex);

      cardHolder.setLayout(new BoxLayout(cardHolder, BoxLayout.X_AXIS));
      cardHolder.add(buttonDeck);
      cardHolder.add(cardDeck);
      this.add(cardHolder);
      // ===============================================================================================================
      // Finally set all Play buttons
      enablePlayToneButtonIfInputsAreValid();
      enablePlayClipButtonIfInputsAreValid();
      enablePlaySpeechButtonIfInputsAreValid();
      //this.add(tabbedPane);
      }

   public Mode getCurrentMode()
      {
      return currentMode;
      }

   public void setCurrentMode(final Mode newMode)
      {
      final Integer index = modeToIndexMap.get(newMode);
      final JCheckBox selected = modeToButton.get(newMode);
      final CardLayout cl = (CardLayout)(cardDeck.getLayout());

      if (index != null)
         {
         if (SwingUtilities.isEventDispatchThread())
            {
            //tabbedPane.setSelectedIndex(index);
            cl.show(cardDeck, selected.getText());
            audOptions.setSelected(selected.getModel(), true);
            currentMode = newMode;
            }
         else
            {
            SwingUtilities.invokeLater(
                  new Runnable()
                     {
                     public void run()
                        {
                        audOptions.setSelected(selected.getModel(), true);
                        cl.show(cardDeck, selected.getText());
                        currentMode = newMode;
                        //tabbedPane.setSelectedIndex(index);
                        }
                     }
            );
            }
         }
      }

   public boolean isCurrentModePlayable()
      {
      if (SwingUtilities.isEventDispatchThread())
         {
         return isCurrentModePlayableWorkhorse();
         }
      else
         {
         try
            {
            final boolean[] isEnabled = new boolean[1];
            SwingUtilities.invokeAndWait(
                  new Runnable()
                     {
                     public void run()
                        {
                        isEnabled[0] = isCurrentModePlayableWorkhorse();
                        }
                     });

            return isEnabled[0];
            }
         catch (InterruptedException e)
            {
            LOG.error("InterruptedException in AudioControlPanel.isCurrentModePlayable()", e);
            }
         catch (InvocationTargetException e)
            {
            LOG.error("InvocationTargetException in AudioControlPanel.isCurrentModePlayable()", e);
            }

         return false;
         }
      }

   private boolean isCurrentModePlayableWorkhorse()
      {
      if (Mode.TONE.equals(currentMode))
         {
         return playToneButton.isEnabled();
         }
      else if (Mode.CLIP.equals(currentMode))
         {
         return playClipButton.isEnabled();
         }
      else if (Mode.SPEECH.equals(currentMode))
         {
         return playSpeechButton.isEnabled();
         }

      return false;
      }

   public void addEventListener(final EventListener listener)
      {
      if (listener != null)
         {
         eventListeners.add(listener);
         }
      }

   public void removeEventListener(final EventListener listener)
      {
      if (listener != null)
         {
         eventListeners.remove(listener);
         }
      }

   public void setEnabled(final boolean isEnabled)
      {
      frequencyTextField.setEnabled(isEnabled);
      durationTextField.setEnabled(isEnabled);
      amplitudeSpinner.setEnabled(isEnabled);
      playToneButton.setEnabled(isEnabled && areToneInputsValid());

      audioClipChooser.setEnabled(isEnabled);
      playClipButton.setEnabled(isEnabled && areClipInputsValid());

      speechTextField.setEnabled(isEnabled);
      playSpeechButton.setEnabled(isEnabled && areSpeechInputsValid());
      }

   public Integer getFrequency()
      {
      return getTextFieldValueAsInteger(frequencyTextField);
      }

   public Integer getAmplitude()
      {
      return getSpinnerValueAsInteger(amplitudeSpinner);
      }

   /** Returns the duration, in milliseconds. */
   public Integer getDuration()
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

   public String getClipPath()
      {
      return audioClipChooser.getSelectedFilePath();
      }

   public String getSpeechText()
      {
      return getTextFieldValueAsString(speechTextField);
      }

   public void setFrequency(final int frequency)
      {
      setToneTextFieldValueWorkhorse(frequencyTextField, frequency);
      piano.setSelectedFrequency(frequency);
      }

   public void setAmplitude(final int amplitude)
      {
      setToneSpinnerValueWorkhorse(amplitudeSpinner, amplitude);
      }

   /** Sets the duration, in milliseconds. */
   public void setDuration(final int duration)
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

   private void setToneSpinnerValueWorkhorse(final JSpinner spinner, final int value)
      {
      if (SwingUtilities.isEventDispatchThread())
         {
         spinner.setValue(value);
         enablePlayToneButtonIfInputsAreValid();
         }
      else
         {
         SwingUtilities.invokeLater(
               new Runnable()
                  {
                  public void run()
                     {
                     spinner.setValue(value);
                     enablePlayToneButtonIfInputsAreValid();
                     }
                  });
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

   public void setClipPath(final String path)
      {
      audioClipChooser.setSelectedFilePath(path);
      }

   public void setSpeechText(final String text)
      {
      if (SwingUtilities.isEventDispatchThread())
         {
         speechTextField.setText(text);
         enablePlaySpeechButtonIfInputsAreValid();
         }
      else
         {
         SwingUtilities.invokeLater(
               new Runnable()
                  {
                  public void run()
                     {
                     speechTextField.setText(text);
                     enablePlaySpeechButtonIfInputsAreValid();
                     }
                  });
         }
      }

   private JButton createPlayButton()
      {
      final JButton playButton = new JButton("Play", ImageUtils.createImageIcon(RESOURCES.getString("button.play-image")));
      playButton.setFont(GUIConstants.BUTTON_FONT);
      playButton.setName("PlayButton");
      return playButton;
      }

   private JTextField createIntegerTextField()
      {
      return createIntegerTextField(DEFAULT_TEXT_FIELD_COLUMNS);
      }

   private JTextField createIntegerTextField(final int numColumns)
      {
      final JTextField textField = new JTextField(numColumns);
      textField.setFont(GUIConstants.FONT_NORMAL);
      textField.setName("audioTextField");
      textField.setSelectedTextColor(Color.WHITE);
      textField.setSelectionColor(Color.BLUE);
      return textField;
      }

   private JTextField createTextField(final int numColumns)
      {
      final JTextField textField = new JTextField(numColumns);
      textField.setFont(GUIConstants.FONT_NORMAL);
      textField.setName("audioTextField");
      textField.setSelectedTextColor(Color.WHITE);
      textField.setSelectionColor(Color.BLUE);
      return textField;
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

   private void enablePlayClipButtonIfInputsAreValid()
      {
      playClipButton.setEnabled(areClipInputsValid());
      }

   private boolean areClipInputsValid()
      {
      return audioClipChooser.isFileSelected();
      }

   private void enablePlaySpeechButtonIfInputsAreValid()
      {
      playSpeechButton.setEnabled(areSpeechInputsValid());
      }

   private boolean areSpeechInputsValid()
      {
      return isTextFieldNonEmpty(speechTextField);
      }

   private boolean isTextFieldNonEmpty(final JTextField textField)
      {
      final String text1 = textField.getText();
      final String trimmedText1 = (text1 != null) ? text1.trim() : null;
      return (trimmedText1 != null) && (trimmedText1.length() > 0);
      }

   /** Retrieves the value from the specified text field as an <code>Integer</code>. */
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

   private class ErrorMessageDialogRunnable implements Runnable
      {
      private final Component parentComponent;
      private final String message;
      private final String title;

      private ErrorMessageDialogRunnable(final Component parentComponent, final String message, final String title)
         {
         this.parentComponent = parentComponent;
         this.message = message;
         this.title = title;
         }

      public void run()
         {
         JOptionPane.showMessageDialog(parentComponent,
                                       message,
                                       title,
                                       JOptionPane.ERROR_MESSAGE);
         }
      }

   private final class MyExceptionHandler extends ExceptionHandler
      {
      public void handleException(final Exception exception)
         {
         LOG.error("Exception caught while playing the sound: ", exception);
         SwingUtilities.invokeLater(failedToPlayFileRunnable);
         }
      }

   @SuppressWarnings({"CloneableClassWithoutClone"})
   private class PlayToneAction extends AbstractTimeConsumingAction
      {
      private int frequency;
      private int amplitude;
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
            amplitude = getSpinnerValueAsInteger(amplitudeSpinner);
            duration = getDuration();
            }
         }

      protected Object executeTimeConsumingAction()
         {
         if (isValid)
            {
            for (final EventListener listener : eventListeners)
               {
               listener.playTone(frequency, amplitude, duration);
               }
            }
         return null;
         }
      }

   @SuppressWarnings({"CloneableClassWithoutClone"})
   private class PlayClipAction extends AbstractTimeConsumingAction
      {
      private File file;
      private boolean isValid;
      private final MyExceptionHandler exceptionHandler = new MyExceptionHandler();

      protected void executeGUIActionBefore()
         {
         isValid = areClipInputsValid();
         if (isValid)
            {
            file = audioClipChooser.getSelectedFile();
            }
         }

      protected Object executeTimeConsumingAction()
         {
         if (isValid)
            {
            if (file != null)
               {
               if (file.exists())
                  {
                  for (final EventListener listener : eventListeners)
                     {
                     listener.playFile(file, exceptionHandler);
                     }
                  }
               else
                  {
                  LOG.error("File [" + file.getAbsolutePath() + "] does not exist!");
                  SwingUtilities.invokeLater(couldNotLoadAudioFileRunnable);
                  }
               }
            else
               {
               LOG.error("File is null!");
               SwingUtilities.invokeLater(couldNotLoadAudioFileRunnable);
               }
            }

         return null;
         }
      }

   @SuppressWarnings({"CloneableClassWithoutClone"})
   private class PlaySpeechAction extends AbstractTimeConsumingAction
      {
      private String text;
      private boolean isValid;

      protected void executeGUIActionBefore()
         {
         isValid = areSpeechInputsValid();
         if (isValid)
            {
            text = getTextFieldValueAsString(speechTextField);
            }
         }

      protected Object executeTimeConsumingAction()
         {
         if (isValid)
            {
            for (final EventListener listener : eventListeners)
               {
               listener.playSpeech(text);
               }
            }
         return null;
         }
      }

   private static class DoubleFormatter extends NumberFormatter
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

   private static class recordActionListener implements ActionListener
      {

      private Component parentComponent;

      public recordActionListener(Component parentComponent)
         {
         this.parentComponent = parentComponent;
         }

      @Override
      public void actionPerformed(final ActionEvent e)
         {
         String[] buttons = {"Start Recording", "Cancel"};
         String[] buttons2 = {"Finish Recording"};

         String filename;
         JPanel panel = new JPanel();
         panel.add(new JLabel("Please enter a name for your new audio recording: "));
         JTextField textField = new JTextField(10);
         panel.add(textField);

         int choice = JOptionPane.showOptionDialog(parentComponent, panel, "Audio Recorder", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, buttons, null);
         if (choice != JOptionPane.OK_OPTION)
            {
            return;
            }
         filename = textField.getText();
         LOG.debug("GOT INIT FILE NAME OF: " + filename);
         filename = filename.replaceAll("\\s+", "") + ".wav";
         while (TerkAudioClipChooser.convertFilenameToFile(filename).exists())
            {
            panel = new JPanel();
            panel.add(new JLabel("Sorry, that name is already taken. Please choose another: "));
            textField = new JTextField(10);
            panel.add(textField);
            choice = JOptionPane.showOptionDialog(parentComponent, panel, "Audio Recorder", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, buttons, null);
            if (choice != JOptionPane.OK_OPTION)
               {
               return;
               }
            filename = textField.getText();
            filename = filename.replaceAll("\\s+", "") + ".wav";
            }
         final File audioFile = TerkAudioClipChooser.convertFilenameToFile(filename);
         final AudioRecorder recorder = new AudioRecorder(audioFile);
         Thread recorderThread = new Thread(new Runnable()
            {
            public void run()
               {
               recorder.startRecording();
               }
            });
         recorderThread.start();
         JOptionPane.showOptionDialog(parentComponent, "Audio is now recording!", "Audio Recorder", JOptionPane.OK_OPTION, JOptionPane.PLAIN_MESSAGE, null, buttons2, null);
         recorder.stopRecording();
         ZipSave savedAudio = PathManager.getInstance().getAudioZipSave();
         savedAudio.addNewFile(filename, audioFile);
         LOG.debug("RECORD CLICKED!");
         }
      }
   }
