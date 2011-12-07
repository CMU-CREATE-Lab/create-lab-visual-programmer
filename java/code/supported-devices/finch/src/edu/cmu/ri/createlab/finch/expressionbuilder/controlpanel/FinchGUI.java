package edu.cmu.ri.createlab.finch.expressionbuilder.controlpanel;

import java.awt.Color;
import java.awt.Component;
import java.util.Map;
import java.util.SortedMap;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.GroupLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTextField;
import edu.cmu.ri.createlab.expressionbuilder.controlpanel.DeviceGUI;
import edu.cmu.ri.createlab.expressionbuilder.controlpanel.ServiceControlPanel;
import edu.cmu.ri.createlab.terk.services.accelerometer.AccelerometerService;
import edu.cmu.ri.createlab.terk.services.buzzer.BuzzerService;
import edu.cmu.ri.createlab.terk.services.led.FullColorLEDService;
import edu.cmu.ri.createlab.terk.services.obstacle.SimpleObstacleDetectorService;
import edu.cmu.ri.createlab.terk.services.photoresistor.PhotoresistorService;
import edu.cmu.ri.createlab.terk.services.thermistor.ThermistorService;
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
   private static final Color BACKGROUND_COLOR = new Color(167, 211, 111);
   private JTextField title;

   public void createGUI(final JPanel mainPanel, final Map<String, ServiceControlPanel> serviceControlPanelMap, final Map<String, SortedMap<Integer, JCheckBox>> serviceDeviceToggleButtonMap)
      {
      final Component spacer = SwingUtils.createRigidSpacer();
      final JPanel gui = createFinchGUI(serviceControlPanelMap, serviceDeviceToggleButtonMap);
      final JPanel guiControlPanels = createControlPanelsGUI(serviceControlPanelMap);


     //TODO: mainPanel is now a GridBag Layout, create appropriate GridBagConstraints
  /*    mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
                  .addComponent(gui)
                  .addComponent(spacer)
                  .addComponent(guiControlPanels)
      );
      mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createSequentialGroup()
                  .addComponent(gui)
                  .addComponent(spacer)
                  .addComponent(guiControlPanels)
      );*/
      }

   private JPanel createFinchGUI(final Map<String, ServiceControlPanel> serviceControlPanelMap, final Map<String, SortedMap<Integer, JCheckBox>> serviceDeviceToggleButtonMap)
      {
      final JPanel orbsPanel = createVerticalButtonPanel(serviceControlPanelMap.get(FullColorLEDService.TYPE_ID),
                                                         serviceDeviceToggleButtonMap.get(FullColorLEDService.TYPE_ID),
                                                         false,
                                                         BACKGROUND_COLOR,
                                                         "image.green");
      final JPanel accelerometersPanel = createVerticalButtonPanel(serviceControlPanelMap.get(AccelerometerService.TYPE_ID),
                                                                   serviceDeviceToggleButtonMap.get(AccelerometerService.TYPE_ID),
                                                                   false,
                                                                   BACKGROUND_COLOR,
                                                         "image.green");
      final JPanel photoresistorsPanel = createVerticalButtonPanel(serviceControlPanelMap.get(PhotoresistorService.TYPE_ID),
                                                                   serviceDeviceToggleButtonMap.get(PhotoresistorService.TYPE_ID),
                                                                   false,
                                                                   BACKGROUND_COLOR,
                                                         "image.green");
      final JPanel thermistorsPanel = createVerticalButtonPanel(serviceControlPanelMap.get(ThermistorService.TYPE_ID),
                                                                serviceDeviceToggleButtonMap.get(ThermistorService.TYPE_ID),
                                                                false,
                                                                BACKGROUND_COLOR,
                                                         "image.green");
      final JPanel obstaclesPanel = createVerticalButtonPanel(serviceControlPanelMap.get(SimpleObstacleDetectorService.TYPE_ID),
                                                              serviceDeviceToggleButtonMap.get(SimpleObstacleDetectorService.TYPE_ID),
                                                              false,
                                                              BACKGROUND_COLOR,
                                                         "image.green");
      final JPanel buzzerPanel = createVerticalButtonPanel(serviceControlPanelMap.get(BuzzerService.TYPE_ID),
                                                           serviceDeviceToggleButtonMap.get(BuzzerService.TYPE_ID),
                                                           false,
                                                           BACKGROUND_COLOR,
                                                         "image.green");
      final JPanel motorsPanel = new JPanel();
      motorsPanel.add(SwingUtils.createLabel("Open Loop Motors Panel will go here"));
      /*
      TODO: add this
      final JPanel motorsPanel = createVerticalButtonPanel(serviceControlPanelMap.get(OpenLoopVelocityControllableMotorService.TYPE_ID),
                                                            serviceDeviceToggleButtonMap.get(OpenLoopVelocityControllableMotorService.TYPE_ID),
                                                            false,
                                                            BACKGROUND_COLOR);
      */

      final JPanel panel = new JPanel();
      panel.setBackground(BACKGROUND_COLOR);
      panel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
      final GroupLayout layout = new GroupLayout(panel);
      panel.setLayout(layout);

      layout.setHorizontalGroup(
            layout.createSequentialGroup()
                  .addComponent(orbsPanel)
                  .addComponent(accelerometersPanel)
                  .addComponent(photoresistorsPanel)
                  .addComponent(thermistorsPanel)
                  .addComponent(obstaclesPanel)
                  .addComponent(buzzerPanel)
                  .addComponent(motorsPanel)
      );
      layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                  .addComponent(orbsPanel)
                  .addComponent(accelerometersPanel)
                  .addComponent(photoresistorsPanel)
                  .addComponent(thermistorsPanel)
                  .addComponent(obstaclesPanel)
                  .addComponent(buzzerPanel)
                  .addComponent(motorsPanel)
      );

      return panel;
      }

   private JPanel createControlPanelsGUI(final Map<String, ServiceControlPanel> serviceControlPanelMap)
      {
      final JPanel panel = new JPanel();
      panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
      panel.add(serviceControlPanelMap.get(FullColorLEDService.TYPE_ID).getComponent());
      panel.add(serviceControlPanelMap.get(AccelerometerService.TYPE_ID).getComponent());
      panel.add(serviceControlPanelMap.get(PhotoresistorService.TYPE_ID).getComponent());
      panel.add(serviceControlPanelMap.get(ThermistorService.TYPE_ID).getComponent());
      panel.add(serviceControlPanelMap.get(SimpleObstacleDetectorService.TYPE_ID).getComponent());
      panel.add(serviceControlPanelMap.get(BuzzerService.TYPE_ID).getComponent());
      //TODO: panel.add(serviceControlPanelMap.get(OpenLoopVelocityControllableMotorService.TYPE_ID).getComponent());

      return panel;
      }

   public void setStageTitleField(JTextField textfield)
      {
      this.title = textfield;
      }
   }