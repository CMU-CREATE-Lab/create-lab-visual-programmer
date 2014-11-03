package edu.cmu.ri.createlab.hummingbird.expressionbuilder.controlpanel;

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
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import edu.cmu.ri.createlab.expressionbuilder.controlpanel.DeviceGUI;
import edu.cmu.ri.createlab.expressionbuilder.controlpanel.ServiceControlPanel;
import edu.cmu.ri.createlab.expressionbuilder.widgets.HelpText;
import edu.cmu.ri.createlab.terk.services.analog.AnalogInputsService;
import edu.cmu.ri.createlab.terk.services.audio.AudioService;
import edu.cmu.ri.createlab.terk.services.led.FullColorLEDService;
import edu.cmu.ri.createlab.terk.services.led.SimpleLEDService;
import edu.cmu.ri.createlab.terk.services.motor.SpeedControllableMotorService;
import edu.cmu.ri.createlab.terk.services.motor.VelocityControllableMotorService;
import edu.cmu.ri.createlab.terk.services.servo.SimpleServoService;
import edu.cmu.ri.createlab.userinterface.util.SwingUtils;
import edu.cmu.ri.createlab.util.MultiLineLabel;
import org.apache.log4j.Logger;

/**
 * <p>
 * <code>HummingbirdDuoGUI</code> creates the GUI for Hummingbird Duos.
 * </p>
 *
 * @author Chris Bartley (bartley@cmu.edu)
 */
public final class HummingbirdDuoGUI extends DeviceGUI
{
    private static final Color BACKGROUND_COLOR = new Color(187, 187, 187);//new Color(78, 77, 88);
    private static final Color ORBS_COLOR = new Color(125, 204, 116);//new Color(128, 224, 171);
    private static final Color LEDS_COLOR = new Color(125, 204, 116);//new Color(128, 224, 171);
    private static final Color SERVOS_COLOR = new Color(142, 142, 142); //new Color(155, 153, 167);//new Color(203, 203, 209);
    private static final Color MOTORS_COLOR = new Color(244, 163, 76);//new Color(255, 185, 128);
    private static final Color VIBMOTORS_COLOR = new Color(255, 220, 65);//new Color(255, 231, 125);
    private static final Color SENSORS_COLOR = new Color(87, 150, 233);//new Color(135, 233, 255);
    private final Color label_color = Color.BLACK;
    private JTextField title;
    private final String icon_color =  "image.duo";
    private final JPanel expressionBlock = new JPanel();
    private final MultiLineLabel block_title = new MultiLineLabel("Untitled", 2, 15);

    private static final Logger LOG = Logger.getLogger(HummingbirdDuoGUI.class);

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

        final JPanel audio = createAudioPanel(serviceControlPanelMap, serviceDeviceToggleButtonMap);

        HelpText helpText = new HelpText(serviceDeviceToggleButtonMap, "Below");

        final JPanel gui = createHummingbirdGUI(serviceControlPanelMap, serviceDeviceToggleButtonMap);

        SwingUtilities.updateComponentTreeUI(gui);

        final JPanel leftGUIControlPanels = createLeftControlPanelsGUI(serviceControlPanelMap);
        final JPanel rightGUIControlPanels = createRightControlPanelsGUI(serviceControlPanelMap);

        final JPanel centerArea = new JPanel();
        centerArea.setName("centerArea");
        centerArea.setLayout(new GridBagLayout());

        final GridBagConstraints c = new GridBagConstraints();
        //Center Area Layout

        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.gridx = 0;
        c.gridy = 0;
        c.weighty = 1.0;
        c.anchor = GridBagConstraints.CENTER;
        c.insets = new Insets(0, 0, 20, 0);
        centerArea.add(helpText, c);

        c.gridy = 1;
        c.insets = new Insets(4, 2, 4, 2);
        c.anchor = GridBagConstraints.PAGE_END;
        c.weighty = 0.0;
        centerArea.add(gui, c);

        //Main Panel Layout
        c.fill = GridBagConstraints.NONE;
        c.gridx = 0;
        c.gridy = 0;
        c.weighty = 0.0;
        c.weightx = 0.5;
        c.anchor = GridBagConstraints.PAGE_END;
        mainPanel.add(leftGUIControlPanels, c);

        c.gridx = 1;
        c.gridy = 0;
        c.weighty = 1.0;
        c.weightx = 0.0;
        c.anchor = GridBagConstraints.PAGE_END;
        mainPanel.add(centerArea, c);

        c.gridx = 2;
        c.gridy = 0;
        c.weighty = 0.0;
        c.weightx = 0.5;
        c.anchor = GridBagConstraints.PAGE_END;
        mainPanel.add(rightGUIControlPanels, c);

        c.insets = new Insets(0, 2, 4, 2);
        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 3;
        c.weighty = 1.0;
        c.weightx = 1.0;
        c.anchor = GridBagConstraints.PAGE_START;
        mainPanel.add(audio, c);

      /*mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createSequentialGroup()
                  .addGroup(mainPanelLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                                  .addComponent(leftGUIControlPanels)
                                  .addComponent(centerArea)
                                  .addComponent(rightGUIControlPanels))
                  .addComponent(audio)
      );

      mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
                  .addGroup(mainPanelLayout.createSequentialGroup()
                                  .addComponent(leftGUIControlPanels)
                                  .addComponent(centerArea)
                                  .addComponent(rightGUIControlPanels))
                  .addComponent(audio)
      );*/
    }

    private JPanel createHummingbirdGUI(final Map<String, ServiceControlPanel> serviceControlPanelMap, final Map<String, SortedMap<Integer, JCheckBox>> serviceDeviceToggleButtonMap)
    {

        final JPanel ledsPanel = createVerticalButtonPanel(serviceControlPanelMap.get(SimpleLEDService.TYPE_ID),
                serviceDeviceToggleButtonMap.get(SimpleLEDService.TYPE_ID),
                false,
                BACKGROUND_COLOR, LEDS_COLOR,
                icon_color,
                label_color);
        final JPanel orbsPanel = createVerticalButtonPanel(serviceControlPanelMap.get(FullColorLEDService.TYPE_ID),
                serviceDeviceToggleButtonMap.get(FullColorLEDService.TYPE_ID),
                false,
                BACKGROUND_COLOR, ORBS_COLOR,
                icon_color,
                label_color);


        final JPanel vibMotorsPanel = createVerticalButtonPanel (serviceControlPanelMap.get(SpeedControllableMotorService.TYPE_ID),
                serviceDeviceToggleButtonMap.get(SpeedControllableMotorService.TYPE_ID),
                true,
                BACKGROUND_COLOR, VIBMOTORS_COLOR,
                icon_color,
                label_color);

        //final JPanel sensorsPanel = createVerticalButtonPanel(serviceControlPanelMap.get(AnalogInputsService.TYPE_ID),
        //serviceDeviceToggleButtonMap.get(AnalogInputsService.TYPE_ID),
        //false,
        //BACKGROUND_COLOR);
        final JPanel servosPanel = createHorizontalButtonPanel(serviceControlPanelMap.get(SimpleServoService.TYPE_ID),
                serviceDeviceToggleButtonMap.get(SimpleServoService.TYPE_ID),
                false,
                BACKGROUND_COLOR, SERVOS_COLOR, false,
                icon_color,
                label_color);
        final JPanel motorsPanel = createHorizontalButtonPanel(serviceControlPanelMap.get(VelocityControllableMotorService.TYPE_ID),
                serviceDeviceToggleButtonMap.get(VelocityControllableMotorService.TYPE_ID),
                false,
                BACKGROUND_COLOR, MOTORS_COLOR, false,
                icon_color,
                label_color);


        final JLabel sensorIcon = serviceControlPanelMap.get(AnalogInputsService.TYPE_ID).getLabelImage(icon_color);
        final JLabel sensorLabel = new JLabel(serviceControlPanelMap.get(AnalogInputsService.TYPE_ID).getShortDisplayName());

        sensorLabel.setForeground(label_color);

        final JPanel sensorPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        final JPanel sensorBox = new JPanel();
        sensorBox.add(SwingUtils.createRigidSpacer(25, 70));
        sensorBox.setBackground(SENSORS_COLOR);

        sensorPanel.setBackground(BACKGROUND_COLOR);
        sensorBox.setToolTipText("Sensors are not used in Expression Builder");

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        gbc.insets = new Insets(0, 0, 0, 0);
        gbc.anchor = GridBagConstraints.LAST_LINE_START;
        sensorPanel.add(sensorIcon, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        gbc.insets = new Insets(0, 0, 5, 0);
        gbc.anchor = GridBagConstraints.LAST_LINE_START;
        sensorPanel.add(sensorLabel, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        gbc.insets = new Insets(0, 0, 0, 0);
        gbc.anchor = GridBagConstraints.LAST_LINE_START;
        sensorPanel.add(sensorBox, gbc);

        final JPanel panel = new JPanel();
        final JLayeredPane layers = new JLayeredPane();
        final JPanel holder = new JPanel();

        //final Component ledSpacer = SwingUtils.createRigidSpacer(20);

        panel.setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();

        //TOP PANELS
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 2;
        c.gridheight = 1;
        c.weightx = 0.0; //.5;
        c.weighty = 0.5;
        c.anchor = GridBagConstraints.PAGE_START;
        c.insets = new Insets(2, 20, 5, 0);
        panel.add(servosPanel, c);

        c.gridx = 2;
        c.gridy = 0;
        c.gridwidth = 2;
        c.gridheight = 1;
        c.weightx = 0.0;
        c.weighty = 0.5;
        c.anchor = GridBagConstraints.PAGE_START;
        c.insets = new Insets(2, 20, 5, 0);
        panel.add(motorsPanel, c);

        //LEFT PANELS
        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 1;
        c.gridheight = 2;
        c.weightx = 0.0;
        c.weighty = 0.0;
        c.anchor = GridBagConstraints.FIRST_LINE_START;
        c.insets = new Insets(0, 2, 0, 0);
        panel.add(sensorPanel, c);

        c.gridx = 0;
        c.gridy = 3;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.0;
        c.weighty = 0.5;
        c.anchor = GridBagConstraints.FIRST_LINE_START;
        c.insets = new Insets(20, 2, 5, 0);
        panel.add(vibMotorsPanel, c);

        //RIGHT PANELS
        c.gridx = 3;
        c.gridy = 1;
        c.gridwidth = 2;
        c.gridheight = 1;
        c.weightx = 1.0;
        c.weighty = 0.0;
        c.anchor = GridBagConstraints.FIRST_LINE_END;
        c.insets = new Insets(0, 0, 0, 2);
        panel.add(orbsPanel, c);

        c.gridx = 4;
        c.gridy = 2;
        c.gridwidth = 1;
        c.gridheight = 2;
        c.weightx = 1.0;
        c.weighty = 0.5;
        c.anchor = GridBagConstraints.FIRST_LINE_END;
        c.insets = new Insets(20, 0, 0, 2);
        panel.add(ledsPanel, c);





        block_title.setFocusable(false);
        updateBlockTitle(title.getText());
        block_title.setAlignmentX(Component.CENTER_ALIGNMENT);
        block_title.setName("expressionBlockTitle");
        //block_title.setLineWrap(true);
        //block_title.setWrapStyleWord(true);

        JSeparator sep = new JSeparator(JSeparator.HORIZONTAL);
        Dimension sep_size = new Dimension(180, 2);
        sep.setPreferredSize(sep_size);
        sep.setMinimumSize(sep_size);
        sep.setMaximumSize(sep_size);

        expressionBlock.setName("expressionBlockDuo");

        Dimension board_size = new Dimension(260, 330);
        Dimension block_size = new Dimension(180, 120);

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

        panel.setBounds(0, 0, board_size.width, board_size.height);
        layers.add(panel, new Integer(0));
      expressionBlock.setBounds(board_size.width / 2 - block_size.width / 2, board_size.height / 2 - block_size.height / 2 + 20, block_size.width, block_size.height);
      layers.add(expressionBlock, new Integer(1));

        holder.add(layers);
        holder.setName("centerArea");
        layers.setName("centerArea");
        panel.setName("HummingbirdDuoBoard");
        return holder;
    }

    private JPanel createRightControlPanelsGUI(final Map<String, ServiceControlPanel> serviceControlPanelMap)
    {
        final Component orbs = serviceControlPanelMap.get(FullColorLEDService.TYPE_ID).getComponent();
        final Component leds = serviceControlPanelMap.get(SimpleLEDService.TYPE_ID).getComponent();

        final JPanel panel = new JPanel();
        panel.setName("leftArea");
        panel.setLayout(new GridBagLayout());

        final GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;

        c.insets = new Insets(5, 2, 5, 2);
        c.gridy = 0;
        c.weighty = 1.0;
        c.anchor = GridBagConstraints.PAGE_END;
        panel.add(orbs, c);

        c.gridy = 1;
        c.weighty = 0.0;
        panel.add(leds, c);
        return panel;
    }

    private JPanel createLeftControlPanelsGUI(final Map<String, ServiceControlPanel> serviceControlPanelMap)
    {
        final Component motors = serviceControlPanelMap.get(VelocityControllableMotorService.TYPE_ID).getComponent();
        final Component vibMotors = serviceControlPanelMap.get(SpeedControllableMotorService.TYPE_ID).getComponent();
        final Component servos = serviceControlPanelMap.get(SimpleServoService.TYPE_ID).getComponent();

        final JPanel panel = new JPanel();
        panel.setName("rightArea");
        panel.setLayout(new GridBagLayout());

        final GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.weighty = 1.0;
        c.anchor = GridBagConstraints.PAGE_END;
        c.insets = new Insets(5, 2, 5, 2);

        c.gridy = 0;
        panel.add(motors, c);

        c.weighty = 0.0;
        c.gridy = 1;
        panel.add(servos, c);

        c.weighty = 0.0;
        c.gridy = 2;
        panel.add(vibMotors, c);
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
        label.setForeground(label_color);

        final JPanel audioPanel = new JPanel();
        audioPanel.setLayout(new BoxLayout(audioPanel, BoxLayout.Y_AXIS));
        audioPanel.add(label);
        audioPanel.add(buttonPanel);
        audioPanel.setName("DUOAudioButtons");
        buttonPanel.setName("DUOAudioButtons");

        final JPanel audioSpeakerButton = new JPanel();

        audioSpeakerButton.setLayout(new BoxLayout(audioSpeakerButton, BoxLayout.X_AXIS));
        audioSpeakerButton.setName("speaker_panel_duo");

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
        final Component motor = serviceControlPanelMap.get(VelocityControllableMotorService.TYPE_ID).getIconPanel();
        final Component vib = serviceControlPanelMap.get(SpeedControllableMotorService.TYPE_ID).getIconPanel();
        final Component servo = serviceControlPanelMap.get(SimpleServoService.TYPE_ID).getIconPanel();
        final Component triled = serviceControlPanelMap.get(FullColorLEDService.TYPE_ID).getIconPanel();
        final Component led = serviceControlPanelMap.get(SimpleLEDService.TYPE_ID).getIconPanel();

        final Component topspacer = SwingUtils.createRigidSpacer(5);

        final Component bottomspacer1 = SwingUtils.createRigidSpacer(5);
        final Component bottomspacer2 = SwingUtils.createRigidSpacer(5);
        final Component bottomspacer3 = SwingUtils.createRigidSpacer(5);

        final JPanel icongroup = new JPanel();
        final GroupLayout layout = new GroupLayout(icongroup);
        icongroup.setLayout(layout);

        layout.setVerticalGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                                .addComponent(led)
                                .addComponent(topspacer)
                                .addComponent(servo))

                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                .addComponent(audio)
                                .addComponent(bottomspacer1)
                                .addComponent(triled)
                                .addComponent(bottomspacer2)
                                .addComponent(motor)
                                .addComponent(bottomspacer3)
                                .addComponent(vib))
        );
        layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)

                        .addGroup(layout.createSequentialGroup()
                                .addComponent(led)
                                .addComponent(topspacer)
                                .addComponent(servo))
                        .addGroup(layout.createSequentialGroup()
                                .addComponent(audio)
                                .addComponent(bottomspacer1)
                                .addComponent(triled)
                                .addComponent(bottomspacer2)
                                .addComponent(motor)
                                .addComponent(bottomspacer3)
                                .addComponent(vib))
        );
        icongroup.setName("iconGroup");
        return icongroup;
    }

    public void setStageTitleField(JTextField textfield)
    {
        this.title = textfield;
        this.title.getDocument().addDocumentListener(titleChange);
    }

    public void updateBlockTitle(String str)
    {
        block_title.updateText(str);
    }
}

