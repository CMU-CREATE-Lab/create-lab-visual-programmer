package edu.cmu.ri.createlab.finch.expressionbuilder.controlpanel;

import java.awt.*;
import java.util.Map;
import java.util.SortedMap;
import javax.swing.*;

import edu.cmu.ri.createlab.expressionbuilder.controlpanel.DeviceGUI;
import edu.cmu.ri.createlab.expressionbuilder.controlpanel.ServiceControlPanel;
import edu.cmu.ri.createlab.terk.services.accelerometer.AccelerometerService;
import edu.cmu.ri.createlab.terk.services.audio.AudioService;
import edu.cmu.ri.createlab.terk.services.buzzer.BuzzerService;
import edu.cmu.ri.createlab.terk.services.led.FullColorLEDService;
import edu.cmu.ri.createlab.terk.services.obstacle.SimpleObstacleDetectorService;
import edu.cmu.ri.createlab.terk.services.photoresistor.PhotoresistorService;
import edu.cmu.ri.createlab.terk.services.thermistor.ThermistorService;
import edu.cmu.ri.createlab.userinterface.util.ImageUtils;
import edu.cmu.ri.createlab.userinterface.util.SwingUtils;

/**
 * <p>
 * <code>FinchGUI</code> creates the GUI for Finches.
 * </p>
 *
 * @author Chris Bartley (bartley@cmu.edu)
 */
public final class FinchGUI extends DeviceGUI
   {
   private static final Color BACKGROUND_COLOR = new Color(255, 255, 255);
   private static final Color BOX_COLOR = new Color(255, 255, 255);
   private JTextField title;

   public void createGUI(final JPanel mainPanel, final Map<String, ServiceControlPanel> serviceControlPanelMap, final Map<String, SortedMap<Integer, JCheckBox>> serviceDeviceToggleButtonMap)
      {
      final Component spacer = SwingUtils.createRigidSpacer();
      final JPanel gui = createFinchGUI(serviceControlPanelMap, serviceDeviceToggleButtonMap);
      final JPanel guiControlPanels = createControlPanelsGUI(serviceControlPanelMap);
      final JPanel audioControlPanel = createAudioPanel(serviceControlPanelMap, serviceDeviceToggleButtonMap);

      final Component buzzerControlPanel = serviceControlPanelMap.get(BuzzerService.TYPE_ID).getComponent();
          
     //TODO: mainPanel is now a GridBag Layout, create appropriate GridBagConstraints
          final GridBagConstraints c = new GridBagConstraints();

          c.fill = GridBagConstraints.NONE;
          c.gridwidth = 2;
          c.gridheight = 1;
          c.gridx = 0;
          c.gridy = 0;
          c.weighty = .5;
          c.weightx = 1.0;
          c.anchor = GridBagConstraints.CENTER;
          c.insets = new Insets(5, 5, 5, 5);
          mainPanel.add(audioControlPanel, c);


          c.fill = GridBagConstraints.NONE;
          c.gridwidth = 1;
          c.gridheight = 1;
          c.gridx = 1;
          c.gridy = 1;
          c.weighty = .5;
          c.weightx = .5;
          c.anchor = GridBagConstraints.LINE_START;
          c.insets = new Insets(5, 10, 5, 10);
          mainPanel.add(gui, c);

          c.fill = GridBagConstraints.NONE;
          c.gridwidth = 1;
          c.gridheight = 1;
          c.gridx = 0;
          c.gridy = 1;
          c.weighty = .5;
          c.weightx = .5;
          c.anchor = GridBagConstraints.LINE_END;
          c.insets = new Insets(5, 10, 5, 10);
          mainPanel.add(guiControlPanels, c);

          c.fill = GridBagConstraints.NONE;
          c.gridwidth = 2;
          c.gridheight = 1;
          c.gridx = 0;
          c.gridy = 2;
          c.weighty = .5;
          c.weightx = .5;
          c.anchor = GridBagConstraints.CENTER;
          c.insets = new Insets(5, 5, 5, 5);
          mainPanel.add(buzzerControlPanel, c);

      }

   private JPanel createFinchGUI(final Map<String, ServiceControlPanel> serviceControlPanelMap, final Map<String, SortedMap<Integer, JCheckBox>> serviceDeviceToggleButtonMap)
      {
          final JPanel orbsPanel = createHorizontalButtonPanel(serviceControlPanelMap.get(FullColorLEDService.TYPE_ID),
                  serviceDeviceToggleButtonMap.get(FullColorLEDService.TYPE_ID),
                  true,
                  BACKGROUND_COLOR, BOX_COLOR, true,
                  "image.white");

      final JPanel buzzerPanel = createHorizontalButtonPanel(serviceControlPanelMap.get(BuzzerService.TYPE_ID),
                                                           serviceDeviceToggleButtonMap.get(BuzzerService.TYPE_ID),
                                                            true,
                                                           BACKGROUND_COLOR, BOX_COLOR, true,
                                                           "image.white");
      final JPanel motorsPanel = new JPanel();
      motorsPanel.add(SwingUtils.createLabel("Open Loop Motors Panel"));
      /*
      TODO: add this
      final JPanel motorsPanel = createHorizontalButtonPanel(serviceControlPanelMap.get(OpenLoopVelocityControllableMotorService.TYPE_ID),
                                                            serviceDeviceToggleButtonMap.get(OpenLoopVelocityControllableMotorService.TYPE_ID),
                                                            false,
                                                            BACKGROUND_COLOR);
      */
      final JLayeredPane layers = new JLayeredPane();

      final JPanel panel = new JPanel();
      Dimension board_size = new Dimension(400, 300);


      panel.setLayout(new GridBagLayout());
      panel.setOpaque(false);

          final GridBagConstraints c = new GridBagConstraints();
          //Center Area Layout

          c.fill = GridBagConstraints.NONE;
          c.gridwidth = 1;
          c.gridheight = 3;
          c.gridx = 0;
          c.gridy = 0;
          c.weighty = 1.0;
          c.anchor = GridBagConstraints.LINE_START;
          c.insets = new Insets(0, 0, 0, 0);
          panel.add(orbsPanel, c);

          c.fill = GridBagConstraints.NONE;
          c.gridwidth = 1;
          c.gridheight = 1;
          c.gridx = 1;
          c.gridy = 0;
          c.weighty = 1.0;
          c.anchor = GridBagConstraints.CENTER;
          c.insets = new Insets(0, 0, 0, 0);
          panel.add(motorsPanel, c);

          c.fill = GridBagConstraints.NONE;
          c.gridwidth = 1;
          c.gridheight = 1;
          c.gridx = 1;
          c.gridy = 2;
          c.weighty = 1.0;
          c.anchor = GridBagConstraints.CENTER;
          c.insets = new Insets(0, 0, 0, 0);
          panel.add(buzzerPanel, c);


          layers.setPreferredSize(board_size);
          layers.setMinimumSize(board_size);
          layers.setMaximumSize(board_size);

          JLabel finch_image = new JLabel(ImageUtils.createImageIcon("/edu/cmu/ri/createlab/finch/expressionbuilder/controlpanel/images/finch_back.png"));

          panel.setBounds(0, 0, board_size.width, board_size.height);
          finch_image.setBounds(0, 0, board_size.width, board_size.height);
          finch_image.setName("purpleElement");
          layers.add(panel, new Integer(1));
          layers.add(finch_image, new Integer(0));

          JPanel main_panel = new JPanel();
          main_panel.setLayout(new GridBagLayout());

          c.fill = GridBagConstraints.BOTH;
          c.gridwidth = 1;
          c.gridheight = 1;
          c.gridx = 0;
          c.gridy = 0;
          c.weighty = 1.0;
          c.anchor = GridBagConstraints.CENTER;
          c.insets = new Insets(0, 0, 0, 0);
          main_panel.add(layers, c);


      return main_panel;
      }

   private JPanel createControlPanelsGUI(final Map<String, ServiceControlPanel> serviceControlPanelMap)
      {
      final JPanel panel = new JPanel();
      panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
      panel.add(serviceControlPanelMap.get(FullColorLEDService.TYPE_ID).getComponent());
      
      //TODO: panel.add(serviceControlPanelMap.get(OpenLoopVelocityControllableMotorService.TYPE_ID).getComponent());

      return panel;
      }

       private JPanel createAudioPanel(final Map<String, ServiceControlPanel> serviceControlPanelMap, final Map<String, SortedMap<Integer, JCheckBox>> serviceDeviceToggleButtonMap)
       {
           final Component audio = serviceControlPanelMap.get(AudioService.TYPE_ID).getComponent();

           final JPanel buttonPanel = new JPanel(new SpringLayout());
           buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
           final ServiceControlPanel serviceControlPanel = serviceControlPanelMap.get(AudioService.TYPE_ID);
           final SortedMap<Integer, JCheckBox> checkBoxMap = serviceDeviceToggleButtonMap.get(AudioService.TYPE_ID);
           for (final int deviceId : checkBoxMap.keySet())
           {
               final JCheckBox checkBox = checkBoxMap.get(deviceId);
               buttonPanel.add(checkBox);
           }
           final JLabel label = SwingUtils.createLabel(serviceControlPanel.getShortDisplayName());

           final JPanel audioPanel = new JPanel();
           audioPanel.setLayout(new BoxLayout(audioPanel, BoxLayout.Y_AXIS));
           audioPanel.add(label);
           audioPanel.add(buttonPanel);
           audioPanel.setName("finchAudioButtons");
           buttonPanel.setName("finchAudioButtons");

           final JPanel audioSpeakerButton = new JPanel();

           audioSpeakerButton.setLayout(new BoxLayout(audioSpeakerButton, BoxLayout.X_AXIS));
           audioSpeakerButton.setName("speaker_panel_finch");

           audioSpeakerButton.add(audioPanel);

           final JPanel mainPanel = new JPanel();
           mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.X_AXIS));
           mainPanel.add(audioSpeakerButton);
           mainPanel.add(SwingUtils.createRigidSpacer(10));
           mainPanel.add(audio);
           mainPanel.setName("audio_holder");
           // mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
           return mainPanel;
       }


   public void setStageTitleField(JTextField textfield)
      {
      this.title = textfield;
      }
   }