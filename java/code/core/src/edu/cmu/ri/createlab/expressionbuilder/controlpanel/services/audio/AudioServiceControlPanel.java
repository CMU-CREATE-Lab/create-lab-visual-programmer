package edu.cmu.ri.createlab.expressionbuilder.controlpanel.services.audio;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.PropertyResourceBundle;
import java.util.Set;
import javax.swing.*;

import edu.cmu.ri.createlab.audio.AudioControlPanel;
import edu.cmu.ri.createlab.audio.TerkAudioClipChooser;
import edu.cmu.ri.createlab.expressionbuilder.controlpanel.AbstractServiceControlPanel;
import edu.cmu.ri.createlab.expressionbuilder.controlpanel.AbstractServiceControlPanelDevice;
import edu.cmu.ri.createlab.expressionbuilder.controlpanel.ControlPanelManager;
import edu.cmu.ri.createlab.expressionbuilder.controlpanel.ServiceControlPanelDevice;
import edu.cmu.ri.createlab.speech.Mouth;
import edu.cmu.ri.createlab.terk.expression.XmlParameter;
import edu.cmu.ri.createlab.terk.services.ExceptionHandler;
import edu.cmu.ri.createlab.terk.services.Service;
import edu.cmu.ri.createlab.terk.services.audio.AudioExpressionConstants;
import edu.cmu.ri.createlab.terk.services.audio.AudioService;
import edu.cmu.ri.createlab.userinterface.util.ImageUtils;
import edu.cmu.ri.createlab.util.FileUtils;
import org.apache.log4j.Logger;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public final class AudioServiceControlPanel extends AbstractServiceControlPanel
   {
   private static final Logger LOG = Logger.getLogger(AudioServiceControlPanel.class);

   private static final PropertyResourceBundle RESOURCES = (PropertyResourceBundle)PropertyResourceBundle.getBundle(AudioServiceControlPanel.class.getName());

   private static final Map<AudioControlPanel.Mode, String> MODE_NAME_MAP;

   static
      {
      final Map<AudioControlPanel.Mode, String> modeNameMap = new HashMap<AudioControlPanel.Mode, String>();
      modeNameMap.put(AudioControlPanel.Mode.TONE, AudioExpressionConstants.OPERATION_NAME_TONE);
      modeNameMap.put(AudioControlPanel.Mode.CLIP, AudioExpressionConstants.OPERATION_NAME_CLIP);
      modeNameMap.put(AudioControlPanel.Mode.SPEECH, AudioExpressionConstants.OPERATION_NAME_SPEECH);
      MODE_NAME_MAP = Collections.unmodifiableMap(modeNameMap);
      }

   private final AudioService service;

   public AudioServiceControlPanel(final ControlPanelManager controlPanelManager, final AudioService service)
      {
      super(controlPanelManager, service, AudioExpressionConstants.OPERATIONS_TO_PARAMETERS_MAP);
      this.service = service;
      }

   public String getDisplayName()
      {
      return RESOURCES.getString("control-panel.title");
      }

   public String getShortDisplayName()
      {
      return RESOURCES.getString("control-panel.short-title");
      }

   public JLabel getLabelImage()
      {
      return new JLabel(ImageUtils.createImageIcon(RESOURCES.getString("image.green")));
      }

   public void refresh()
      {
      LOG.debug("AudioServiceControlPanel.refresh()");

      // nothing to do here
      }

   protected ServiceControlPanelDevice createServiceControlPanelDevice(final Service service, final int deviceIndex)
      {
      return new ControlPanelDevice(deviceIndex);
      }

   private final class ControlPanelDevice extends AbstractServiceControlPanelDevice
      {
      private final AudioControlPanel audioControlPanel = new AudioControlPanel(new TerkAudioClipChooser());
      private AudioControlPanelEventListener audioControlPanelEventListener = new AudioControlPanelEventListener();

      private JLabel blockIcon = new JLabel();

      private final ImageIcon act_icon = ImageUtils.createImageIcon(RESOURCES.getString("image.yellow"));
      private final ImageIcon dis_icon = ImageUtils.createImageIcon(RESOURCES.getString("image.yellowdisabled"));
      private final ImageIcon off_icon = ImageUtils.createImageIcon(RESOURCES.getString("image.yellowoff"));

      private ControlPanelDevice(final int deviceIndex)
         {
         super(deviceIndex);

         audioControlPanel.addEventListener(audioControlPanelEventListener);
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
          //TODO: Placeholder
         }


      public Component getComponent()
         {
         final JPanel act_box = new JPanel();
         final JPanel dis_box = new JPanel();
         final JLabel icon = new JLabel(ImageUtils.createImageIcon(RESOURCES.getString("image.disabled")));
         icon.setAlignmentX(Component.LEFT_ALIGNMENT);
         icon.setToolTipText("Audio is disabled");
         act_box.setName("active_service_box");
         act_box.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
         act_box.setLayout(new BoxLayout(act_box, BoxLayout.Y_AXIS));
         act_box.add(audioControlPanel);

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

      private void updateToneGUI(final int frequency, final int amplitude, final int duration)
         {
         // Update the GUI, but don't execute the operation
         SwingUtilities.invokeLater(
               new Runnable()
               {
               public void run()
                  {
                  audioControlPanel.setFrequency(frequency);
                  audioControlPanel.setAmplitude(amplitude);
                  audioControlPanel.setDuration(duration);
                  audioControlPanel.setCurrentMode(AudioControlPanel.Mode.TONE);
                  }
               });
         }

      private void updateClipGUI(final String clipFilePath)
         {
         // Update the GUI, but don't execute the operation
         SwingUtilities.invokeLater(
               new Runnable()
               {
               public void run()
                  {
                  audioControlPanel.setClipPath(clipFilePath);
                  audioControlPanel.setCurrentMode(AudioControlPanel.Mode.CLIP);
                  }
               });
         }

      private void updateSpeechGUI(final String speechText)
         {
         // Update the GUI, but don't execute the operation
         SwingUtilities.invokeLater(
               new Runnable()
               {
               public void run()
                  {
                  audioControlPanel.setSpeechText(speechText);
                  audioControlPanel.setCurrentMode(AudioControlPanel.Mode.SPEECH);
                  }
               });
         }

      public boolean execute(final String operationName, final Map<String, String> parameterMap)
         {
         if (AudioExpressionConstants.OPERATION_NAME_TONE.equals(operationName))
            {
            final String freqStr = parameterMap.get(AudioExpressionConstants.PARAMETER_NAME_FREQUENCY);
            final String ampStr = parameterMap.get(AudioExpressionConstants.PARAMETER_NAME_AMPLITUDE);
            final String durStr = parameterMap.get(AudioExpressionConstants.PARAMETER_NAME_DURATION);
            try
               {
               final int frequency = Integer.parseInt(freqStr);
               final int amplitude = Integer.parseInt(ampStr);
               final int duration = Integer.parseInt(durStr);

               // update the GUI
               updateToneGUI(frequency, amplitude, duration);

               // execute the operation on the service
               audioControlPanelEventListener.playTone(frequency, amplitude, duration);
               return true;
               }
            catch (NumberFormatException e)
               {
               LOG.error("NumberFormatException while trying to convert frequency, amplitude, or duration to an integer.", e);
               }
            catch (Exception e)
               {
               LOG.error("Exception while trying to load or execute the operation.", e);
               }
            }
         else if (AudioExpressionConstants.OPERATION_NAME_CLIP.equals(operationName))
            {
            final String clipFilePath = parameterMap.get(AudioExpressionConstants.PARAMETER_NAME_FILE);

            try
               {
               // update the GUI
               updateClipGUI(clipFilePath);

               // execute the operation on the service
               audioControlPanelEventListener.playSound(TerkAudioClipChooser.convertFilenameToFile(clipFilePath), null);
               return true;
               }
            catch (Exception e)
               {
               LOG.error("Exception while trying to load or execute the operation.", e);
               }
            }
         else if (AudioExpressionConstants.OPERATION_NAME_SPEECH.equals(operationName))
            {
            final String spechText = parameterMap.get(AudioExpressionConstants.PARAMETER_NAME_TEXT);

            try
               {
               // update the GUI
               updateSpeechGUI(spechText);

               // execute the operation on the service
               audioControlPanelEventListener.playSpeech(spechText);
               return true;
               }
            catch (Exception e)
               {
               LOG.error("Exception while trying to load or execute the operation.", e);
               }
            }
         return false;
         }

      public String getCurrentOperationName()
         {
         if (audioControlPanel.isCurrentModePlayable())
            {
            return MODE_NAME_MAP.get(audioControlPanel.getCurrentMode());
            }

         return null;
         }

      public Set<XmlParameter> buildParameters()
         {
         LOG.debug("AudioServiceControlPanel$ControlPanelDevice.buildParameters()");

         final AudioControlPanel.Mode currentMode = audioControlPanel.getCurrentMode();

         if (AudioControlPanel.Mode.TONE.equals(currentMode))
            {
            final Integer f = audioControlPanel.getFrequency();
            final Integer a = audioControlPanel.getAmplitude();
            final Integer d = audioControlPanel.getDuration();

            if (f != null && a != null && d != null)
               {
               final Set<XmlParameter> parameters = new HashSet<XmlParameter>();
               parameters.add(new XmlParameter(AudioExpressionConstants.PARAMETER_NAME_FREQUENCY, f));
               parameters.add(new XmlParameter(AudioExpressionConstants.PARAMETER_NAME_AMPLITUDE, a));
               parameters.add(new XmlParameter(AudioExpressionConstants.PARAMETER_NAME_DURATION, d));
               return parameters;
               }
            }
         else if (AudioControlPanel.Mode.CLIP.equals(currentMode))
            {
            final String clipPath = audioControlPanel.getClipPath();

            if (clipPath != null)
               {
               final Set<XmlParameter> parameters = new HashSet<XmlParameter>();
               parameters.add(new XmlParameter(AudioExpressionConstants.PARAMETER_NAME_FILE, clipPath));
               return parameters;
               }
            }
         else if (AudioControlPanel.Mode.SPEECH.equals(currentMode))
            {
            final String speechText = audioControlPanel.getSpeechText();

            if (speechText != null)
               {
               final Set<XmlParameter> parameters = new HashSet<XmlParameter>();
               parameters.add(new XmlParameter(AudioExpressionConstants.PARAMETER_NAME_TEXT, speechText));
               return parameters;
               }
            }

         return null;
         }

      private final class AudioControlPanelEventListener implements AudioControlPanel.EventListener
         {
         public void playTone(final int frequency, final int amplitude, final int duration)
            {
            service.playToneAsynchronously(frequency, amplitude, duration, null);
            }

         public void playSound(final File file, final ExceptionHandler exceptionHandler)
            {
            if (file != null)
               {
               try
                  {
                  final byte[] data = FileUtils.getFileAsBytes(file);
                  if (data != null)
                     {
                     service.playSoundAsynchronously(data, exceptionHandler);
                     }
                  }
               catch (IOException e)
                  {
                  LOG.error("IOException while trying to read the file for playSound()", e);
                  }
               }
            }

         public void playSpeech(final String speechText)
            {
            final byte[] speechData = Mouth.getInstance().getSpeech(speechText);
            if (speechData != null && speechData.length > 0)
               {
               service.playSoundAsynchronously(speechData, null);
               }
            else
               {
               LOG.error("AudioServiceControlPanel$ControlPanelDevice$AudioControlPanelEventListener.playSpeech(): speech byte array is null or empty");
               }
            }
         }
      }
   }