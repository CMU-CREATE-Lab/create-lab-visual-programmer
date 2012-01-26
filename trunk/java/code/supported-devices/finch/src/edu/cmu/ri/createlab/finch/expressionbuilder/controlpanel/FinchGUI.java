package edu.cmu.ri.createlab.finch.expressionbuilder.controlpanel;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Map;
import java.util.SortedMap;
import javax.swing.BoxLayout;
import javax.swing.GroupLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import edu.cmu.ri.createlab.expressionbuilder.controlpanel.DeviceGUI;
import edu.cmu.ri.createlab.expressionbuilder.controlpanel.ServiceControlPanel;
import edu.cmu.ri.createlab.terk.services.audio.AudioService;
import edu.cmu.ri.createlab.terk.services.buzzer.BuzzerService;
import edu.cmu.ri.createlab.terk.services.led.FullColorLEDService;
import edu.cmu.ri.createlab.terk.services.motor.OpenLoopVelocityControllableMotorService;
import edu.cmu.ri.createlab.userinterface.util.ImageUtils;
import edu.cmu.ri.createlab.userinterface.util.SwingUtils;
import edu.cmu.ri.createlab.util.MultiLineLabel;
import org.apache.log4j.Logger;

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
   private final JPanel expressionBlock = new JPanel();
   private final MultiLineLabel block_title = new MultiLineLabel("Untitled", 2, 15);

   private static final Logger LOG = Logger.getLogger(FinchGUI.class);

   private DocumentListener titleChange = new DocumentListener()
   {
   @Override
   public void insertUpdate(DocumentEvent e)
      {
      //To change body of implemented methods use File | Settings | File Templates.
      String str = "";
      try
         {
         str = e.getDocument().getText(0, e.getDocument().getLength());
         }
      catch (BadLocationException be)
         {
         LOG.error("Error on titleChange document listener.", be);
         }
      updateBlockTitle(str);
      }

   @Override
   public void removeUpdate(DocumentEvent e)
      {
      //To change body of implemented methods use File | Settings | File Templates.
      String str = "";
      try
         {
         str = e.getDocument().getText(0, e.getDocument().getLength());
         }
      catch (BadLocationException be)
         {
         LOG.error("Error on titleChange document listener.", be);
         }
      updateBlockTitle(str);
      }

   @Override
   public void changedUpdate(DocumentEvent e)
      {
      String str = "";
      try
         {
         str = e.getDocument().getText(0, e.getDocument().getLength());
         }
      catch (BadLocationException be)
         {
         LOG.error("Error on titleChange document listener.", be);
         }
      updateBlockTitle(str);
      }
   };

   public void createGUI(final JPanel mainPanel, final Map<String, ServiceControlPanel> serviceControlPanelMap, final Map<String, SortedMap<Integer, JCheckBox>> serviceDeviceToggleButtonMap)
      {
      final Component spacer = SwingUtils.createRigidSpacer();
      final JPanel gui = createFinchGUI(serviceControlPanelMap, serviceDeviceToggleButtonMap);
      final JPanel guiControlPanels = createControlPanelsGUI(serviceControlPanelMap);
      final JPanel audioControlPanel = createAudioPanel(serviceControlPanelMap, serviceDeviceToggleButtonMap);

      final Component buzzerControlPanel = serviceControlPanelMap.get(BuzzerService.TYPE_ID).getComponent();

      //TODO: mainPanel is now a GridBag Layout, create appropriate GridBagConstraints
      final GridBagConstraints c = new GridBagConstraints();

      //Top and Left Spacers
      c.fill = GridBagConstraints.NONE;
      c.gridwidth = 1;
      c.gridheight = 3;
      c.gridx = 0;
      c.gridy = 0;
      c.weighty = 1.0;
      c.weightx = .5;
      c.anchor = GridBagConstraints.CENTER;
      c.insets = new Insets(5, 5, 5, 5);
      mainPanel.add(SwingUtils.createRigidSpacer(0), c);

      c.fill = GridBagConstraints.NONE;
      c.gridwidth = 2;
      c.gridheight = 1;
      c.gridx = 1;
      c.gridy = 0;
      c.weighty = 1.0;
      c.weightx = .0;
      c.anchor = GridBagConstraints.CENTER;
      c.insets = new Insets(5, 5, 5, 5);
      mainPanel.add(SwingUtils.createRigidSpacer(0), c);

      //Content
      c.fill = GridBagConstraints.NONE;
      c.gridwidth = 2;
      c.gridheight = 1;
      c.gridx = 1;
      c.gridy = 1;
      c.weighty = .0;
      c.weightx = .0;
      c.anchor = GridBagConstraints.PAGE_END;
      c.insets = new Insets(5, 5, 5, 5);
      mainPanel.add(audioControlPanel, c);

      c.fill = GridBagConstraints.NONE;
      c.gridwidth = 1;
      c.gridheight = 1;
      c.gridx = 1;
      c.gridy = 2;
      c.weighty = .0;
      c.weightx = .0;
      c.anchor = GridBagConstraints.CENTER;
      c.insets = new Insets(5, 5, 5, 5);
      mainPanel.add(gui, c);

      c.fill = GridBagConstraints.HORIZONTAL;
      c.gridwidth = 1;
      c.gridheight = 1;
      c.gridx = 2;
      c.gridy = 2;
      c.weighty = .0;
      c.weightx = .01;
      c.anchor = GridBagConstraints.LINE_END;
      c.insets = new Insets(5, 5, 5, 5);
      mainPanel.add(guiControlPanels, c);

      c.fill = GridBagConstraints.NONE;
      c.gridwidth = 2;
      c.gridheight = 1;
      c.gridx = 1;
      c.gridy = 3;
      c.weighty = .0;
      c.weightx = .0;
      c.anchor = GridBagConstraints.FIRST_LINE_END;
      c.insets = new Insets(5, 5, 5, 5);
      mainPanel.add(buzzerControlPanel, c);

      //Right and Bottom Spacer
      c.fill = GridBagConstraints.NONE;
      c.gridwidth = 2;
      c.gridheight = 1;
      c.gridx = 1;
      c.gridy = 4;
      c.weighty = 1.0;
      c.weightx = .0;
      c.anchor = GridBagConstraints.CENTER;
      c.insets = new Insets(5, 5, 5, 5);
      mainPanel.add(SwingUtils.createRigidSpacer(0), c);

      c.fill = GridBagConstraints.NONE;
      c.gridwidth = 1;
      c.gridheight = 5;
      c.gridx = 3;
      c.gridy = 0;
      c.weighty = 1.0;
      c.weightx = .5;
      c.anchor = GridBagConstraints.CENTER;
      c.insets = new Insets(5, 5, 5, 5);
      mainPanel.add(SwingUtils.createRigidSpacer(0), c);
      }

   private JPanel createFinchGUI(final Map<String, ServiceControlPanel> serviceControlPanelMap, final Map<String, SortedMap<Integer, JCheckBox>> serviceDeviceToggleButtonMap)
      {
      final JPanel orbsPanel = createAlignedButtonPanel(serviceControlPanelMap.get(FullColorLEDService.TYPE_ID),
                                                        serviceDeviceToggleButtonMap.get(FullColorLEDService.TYPE_ID),
                                                        0,
                                                        false,
                                                        "image.white");

      final JPanel buzzerPanel = createAlignedButtonPanel(serviceControlPanelMap.get(BuzzerService.TYPE_ID),
                                                          serviceDeviceToggleButtonMap.get(BuzzerService.TYPE_ID),
                                                          0,
                                                          false,
                                                          "image.white");

      final JPanel motorsPanel1 = createAlignedButtonPanel(serviceControlPanelMap.get(OpenLoopVelocityControllableMotorService.TYPE_ID),
                                                           serviceDeviceToggleButtonMap.get(OpenLoopVelocityControllableMotorService.TYPE_ID),
                                                           0,
                                                           true,
                                                           "image.white");

      final JPanel motorsPanel2 = createAlignedButtonPanel(serviceControlPanelMap.get(OpenLoopVelocityControllableMotorService.TYPE_ID),
                                                           serviceDeviceToggleButtonMap.get(OpenLoopVelocityControllableMotorService.TYPE_ID),
                                                           1,
                                                           true,
                                                           "image.white");

      final JLayeredPane layers = new JLayeredPane();

      final JPanel panel = new JPanel();
      final Dimension board_size = new Dimension(475, 360);

      panel.setLayout(new GridBagLayout());
      panel.setOpaque(false);
      panel.setPreferredSize(board_size);
      final GridBagConstraints c = new GridBagConstraints();
      //Center Area Layout

      c.fill = GridBagConstraints.NONE;
      c.gridwidth = 1;
      c.gridheight = 1;
      c.gridx = 1;
      c.gridy = 0;
      c.weighty = .5;
      c.weightx = .35;
      c.anchor = GridBagConstraints.FIRST_LINE_START;
      c.insets = new Insets(30, 0, 0, 0);
      panel.add(motorsPanel1, c);

      c.fill = GridBagConstraints.NONE;
      c.gridwidth = 1;
      c.gridheight = 1;
      c.gridx = 1;
      c.gridy = 1;
      c.weighty = .5;
      c.weightx = .35;
      c.anchor = GridBagConstraints.LINE_END;
      c.insets = new Insets(0, 0, 0, 10);
      panel.add(orbsPanel, c);

      c.fill = GridBagConstraints.NONE;
      c.gridwidth = 1;
      c.gridheight = 1;
      c.gridx = 0;
      c.gridy = 1;
      c.weighty = .5;
      c.weightx = .65;
      c.anchor = GridBagConstraints.LINE_START;
      c.insets = new Insets(0, 20, 0, 0);
      panel.add(buzzerPanel, c);

      c.fill = GridBagConstraints.NONE;
      c.gridwidth = 1;
      c.gridheight = 1;
      c.gridx = 1;
      c.gridy = 2;
      c.weighty = .5;
      c.weightx = .35;
      c.anchor = GridBagConstraints.LAST_LINE_START;
      c.insets = new Insets(0, 0, 30, 0);
      panel.add(motorsPanel2, c);

      //Create Expression Block
      Dimension block_size = new Dimension(180, 120);
      expressionBlock.setName("expressionBlockWhite");

      block_title.setFocusable(false);
      updateBlockTitle(title.getText());
      block_title.setAlignmentX(Component.CENTER_ALIGNMENT);
      block_title.setName("expressionBlockTitle");

      JSeparator sep = new JSeparator(JSeparator.HORIZONTAL);
      Dimension sep_size = new Dimension(180, 2);
      sep.setPreferredSize(sep_size);
      sep.setMinimumSize(sep_size);
      sep.setMaximumSize(sep_size);

      expressionBlock.setPreferredSize(block_size);
      expressionBlock.setMinimumSize(block_size);
      expressionBlock.setLayout(new BoxLayout(expressionBlock, BoxLayout.Y_AXIS));
      expressionBlock.add(SwingUtils.createRigidSpacer(18));
      expressionBlock.add(block_title);
      //expressionBlock.add(SwingUtils.createRigidSpacer(2));
      expressionBlock.add(createBlockIcons(serviceControlPanelMap));
      expressionBlock.add(SwingUtils.createRigidSpacer(3));
      expressionBlock.add(sep);
      expressionBlock.add(SwingUtils.createRigidSpacer(20));

      layers.setPreferredSize(board_size);
      layers.setMinimumSize(board_size);
      layers.setMaximumSize(board_size);

      JLabel finch_image = new JLabel(ImageUtils.createImageIcon("/edu/cmu/ri/createlab/finch/expressionbuilder/controlpanel/images/finch_back.png"));

      panel.setBounds(0, 0, board_size.width, board_size.height);
      finch_image.setBounds(0, 0, board_size.width, board_size.height);
      expressionBlock.setBounds(130, board_size.height / 2 - block_size.height / 2, block_size.width, block_size.height);

      finch_image.setName("purpleElement");
      layers.add(finch_image, new Integer(0));
      layers.add(panel, new Integer(1));
      layers.add(expressionBlock, new Integer(2));

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
      panel.add(serviceControlPanelMap.get(OpenLoopVelocityControllableMotorService.TYPE_ID).getComponent());

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

   private JPanel createBlockIcons(final Map<String, ServiceControlPanel> serviceControlPanelMap)
      {
      final Component audio = serviceControlPanelMap.get(AudioService.TYPE_ID).getIconPanel();
      //final Component motor = serviceControlPanelMap.get();
      final Component triled = serviceControlPanelMap.get(FullColorLEDService.TYPE_ID).getIconPanel();
      final Component buzzer = serviceControlPanelMap.get(BuzzerService.TYPE_ID).getIconPanel();

      final Component bottomspacer1 = SwingUtils.createRigidSpacer(5);
      final Component bottomspacer2 = SwingUtils.createRigidSpacer(5);
      final Component bottomspacer3 = SwingUtils.createRigidSpacer(5);

      JPanel icongroup = new JPanel();
      GroupLayout layout = new GroupLayout(icongroup);
      icongroup.setLayout(layout);

      layout.setVerticalGroup(layout.createSequentialGroup()

                                    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                                    .addComponent(buzzer)
                                                    .addComponent(bottomspacer1)
                                                          //.addComponent(motor)
                                                    .addComponent(bottomspacer2)
                                                    .addComponent(triled)
                                                    .addComponent(bottomspacer3)
                                                    .addComponent(audio))
      );

      layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)

                                      .addGroup(layout.createSequentialGroup()
                                                      .addComponent(buzzer)
                                                      .addComponent(bottomspacer1)
                                                            //.addComponent(motor)
                                                      .addComponent(bottomspacer2)
                                                      .addComponent(triled)
                                                      .addComponent(bottomspacer3)
                                                      .addComponent(audio))
      );
      icongroup.setName("iconGroup");
      return icongroup;
      }

   private JPanel createAlignedButtonPanel(final ServiceControlPanel serviceControlPanel,
                                           final SortedMap<Integer, JCheckBox> checkBoxMap,
                                           final int deviceId,
                                           final boolean displayId,
                                           final String imageName)
      {
      final JCheckBox checkBox = checkBoxMap.get(deviceId);
      checkBox.setBackground(BOX_COLOR);
      checkBox.setAlignmentY(Component.CENTER_ALIGNMENT);

      Component numLabel = SwingUtils.createRigidSpacer(0);

      if (displayId)
         {
         numLabel = SwingUtils.createTinyFontLabel(String.valueOf(deviceId + 1));
         }

      final JLabel icon = serviceControlPanel.getLabelImage(imageName);
      final JPanel panel = new JPanel();
      panel.setBackground(BOX_COLOR);

      panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
      panel.add(icon);
      panel.add(SwingUtils.createRigidSpacer(2));
      panel.add(SwingUtils.createLabel(serviceControlPanel.getSingleName()));
      if (displayId)
         {
         panel.add(SwingUtils.createRigidSpacer(2));
         panel.add(SwingUtils.createLabel(String.valueOf(deviceId + 1)));
         }
      panel.add(SwingUtils.createRigidSpacer(2));
      panel.add(checkBox);
      panel.setName("iconTitle");

      panel.setOpaque(true);
      return panel;
      }

   public void setStageTitleField(JTextField textfield)
      {
      this.title = textfield;
      this.title.getDocument().addDocumentListener(titleChange);
      ;
      }

   public void updateBlockTitle(String str)
      {
      block_title.updateText(str);
      }
   }