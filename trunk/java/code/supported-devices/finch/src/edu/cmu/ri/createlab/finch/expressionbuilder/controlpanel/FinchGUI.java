package edu.cmu.ri.createlab.finch.expressionbuilder.controlpanel;

import java.awt.*;
import java.util.Map;
import java.util.SortedMap;
import javax.swing.*;

import edu.cmu.ri.createlab.expressionbuilder.controlpanel.DeviceGUI;
import edu.cmu.ri.createlab.expressionbuilder.controlpanel.ServiceControlPanel;
import edu.cmu.ri.createlab.terk.services.accelerometer.AccelerometerService;
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


     //TODO: mainPanel is now a GridBag Layout, create appropriate GridBagConstraints
          final GridBagConstraints c = new GridBagConstraints();
          //Center Area Layout

          c.fill = GridBagConstraints.HORIZONTAL;
          c.gridwidth = 1;
          c.gridheight = 1;
          c.gridx = 0;
          c.gridy = 0;
          c.weighty = 1.0;
          c.anchor = GridBagConstraints.CENTER;
          c.insets = new Insets(0, 0, 0, 0);
          mainPanel.add(gui, c);

          c.fill = GridBagConstraints.HORIZONTAL;
          c.gridwidth = 1;
          c.gridheight = 1;
          c.gridx = 0;
          c.gridy = 1;
          c.weighty = 1.0;
          c.anchor = GridBagConstraints.CENTER;
          c.insets = new Insets(0, 0, 0, 0);
          mainPanel.add(guiControlPanels, c);

      }

   private JPanel createFinchGUI(final Map<String, ServiceControlPanel> serviceControlPanelMap, final Map<String, SortedMap<Integer, JCheckBox>> serviceDeviceToggleButtonMap)
      {
      final JPanel orbsPanel = createHorizontalButtonPanel(serviceControlPanelMap.get(FullColorLEDService.TYPE_ID),
              serviceDeviceToggleButtonMap.get(FullColorLEDService.TYPE_ID),
              false,
              BACKGROUND_COLOR, BOX_COLOR, false,
              "image.green");

      final JPanel buzzerPanel = createHorizontalButtonPanel(serviceControlPanelMap.get(BuzzerService.TYPE_ID),
                                                           serviceDeviceToggleButtonMap.get(BuzzerService.TYPE_ID),
                                                           false,
                                                           BACKGROUND_COLOR, BOX_COLOR, false,
                                                         "image.green");
      final JPanel motorsPanel = new JPanel();
      motorsPanel.add(SwingUtils.createLabel("Open Loop Motors Panel will go here"));
      /*
      TODO: add this
      final JPanel motorsPanel = createHorizontalButtonPanel(serviceControlPanelMap.get(OpenLoopVelocityControllableMotorService.TYPE_ID),
                                                            serviceDeviceToggleButtonMap.get(OpenLoopVelocityControllableMotorService.TYPE_ID),
                                                            false,
                                                            BACKGROUND_COLOR);
      */
      final JLayeredPane layers = new JLayeredPane();

      final JPanel panel = new JPanel();
      Dimension board_size = new Dimension(300, 400);


      panel.setLayout(new GridBagLayout());
      panel.setOpaque(false);

          final GridBagConstraints c = new GridBagConstraints();
          //Center Area Layout

          c.fill = GridBagConstraints.NONE;
          c.gridwidth = 1;
          c.gridheight = 1;
          c.gridx = 0;
          c.gridy = 0;
          c.weighty = 1.0;
          c.anchor = GridBagConstraints.CENTER;
          c.insets = new Insets(0, 0, 0, 0);
          panel.add(orbsPanel, c);

          c.fill = GridBagConstraints.NONE;
          c.gridwidth = 1;
          c.gridheight = 1;
          c.gridx = 0;
          c.gridy = 1;
          c.weighty = 1.0;
          c.anchor = GridBagConstraints.CENTER;
          c.insets = new Insets(0, 0, 0, 0);
          panel.add(motorsPanel, c);

          c.fill = GridBagConstraints.NONE;
          c.gridwidth = 1;
          c.gridheight = 1;
          c.gridx = 0;
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
      panel.add(serviceControlPanelMap.get(BuzzerService.TYPE_ID).getComponent());
      //TODO: panel.add(serviceControlPanelMap.get(OpenLoopVelocityControllableMotorService.TYPE_ID).getComponent());

      return panel;
      }

   public void setStageTitleField(JTextField textfield)
      {
      this.title = textfield;
      }
   }