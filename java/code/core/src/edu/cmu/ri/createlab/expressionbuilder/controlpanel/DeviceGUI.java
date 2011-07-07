package edu.cmu.ri.createlab.expressionbuilder.controlpanel;

import java.awt.Color;
import java.awt.Component;
import java.util.Map;
import java.util.SortedMap;
import javax.swing.GroupLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import edu.cmu.ri.createlab.userinterface.util.SpringLayoutUtilities;
import edu.cmu.ri.createlab.userinterface.util.SwingUtils;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public abstract class DeviceGUI
   {
   public abstract void createGUI(final GroupLayout mainPanelLayout,
                                  final Map<String, ServiceControlPanel> serviceControlPanelMap,
                                  final Map<String, SortedMap<Integer, JCheckBox>> serviceDeviceToggleButtonMap);

   public abstract void setStageTitleField(JTextField title);

   protected final JPanel createVerticalButtonPanel(final ServiceControlPanel serviceControlPanel,
                                                    final SortedMap<Integer, JCheckBox> checkBoxMap,
                                                    final boolean isRotateClockwise)
      {
      return createVerticalButtonPanel(serviceControlPanel,
                                       checkBoxMap,
                                       isRotateClockwise,
                                       null);
      }

   protected final JPanel createVerticalButtonPanel(final ServiceControlPanel serviceControlPanel,
                                                    final SortedMap<Integer, JCheckBox> checkBoxMap,
                                                    final boolean isRotateClockwise,
                                                    final Color backgroundColor)
      {
      final JPanel buttonPanel = new JPanel(new SpringLayout());
      if (backgroundColor != null)
         {
         buttonPanel.setBackground(backgroundColor);
         }
      for (final int deviceId : checkBoxMap.keySet())
         {
         final JCheckBox checkBox = checkBoxMap.get(deviceId);
         if (backgroundColor != null)
            {
            checkBox.setBackground(backgroundColor);
            }

         if (isRotateClockwise)
            {
            buttonPanel.add(checkBox);
            buttonPanel.add(SwingUtils.createVerticalTinyFontLabel(String.valueOf(deviceId + 1), isRotateClockwise));
            }
         else
            {
            buttonPanel.add(SwingUtils.createVerticalTinyFontLabel(String.valueOf(deviceId + 1), isRotateClockwise));
            buttonPanel.add(checkBox);
            }
         }
      SpringLayoutUtilities.makeCompactGrid(buttonPanel,
                                            checkBoxMap.keySet().size(), 2, // rows, cols
                                            0, 0, // initX, initY
                                            0, 0);// xPad, yPad

      final JLabel label = SwingUtils.createVerticalLabel(serviceControlPanel.getShortDisplayName(), isRotateClockwise);
      final JLabel icon = serviceControlPanel.getLabelImage();
      final JPanel panel = new JPanel();
      final GroupLayout layout = new GroupLayout(panel);
      panel.setLayout(layout);
      if (backgroundColor != null)
         {
         panel.setBackground(backgroundColor);
         }

      if (isRotateClockwise)
         {
         /*layout.setHorizontalGroup(
               layout.createSequentialGroup()
                     .addComponent(buttonPanel)
                     .addComponent(label));
         layout.setVerticalGroup(
               layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                     .addComponent(buttonPanel)
                     .addComponent(label));*/
         layout.setHorizontalGroup(
               layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                     .addComponent(buttonPanel)
                     .addComponent(label)
                     .addComponent(icon));
         layout.setVerticalGroup(
               layout.createSequentialGroup()
                     .addComponent(icon)
                     .addComponent(label)
                     .addComponent(buttonPanel)
         );
         }
      else
         {
         /*layout.setHorizontalGroup(
               layout.createSequentialGroup()
                     .addComponent(label)
                     .addComponent(buttonPanel));
         layout.setVerticalGroup(
               layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                     .addComponent(label)
                     .addComponent(buttonPanel));*/
         layout.setHorizontalGroup(
               layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                     .addComponent(buttonPanel)
                     .addComponent(label)
                     .addComponent(icon));
         layout.setVerticalGroup(
               layout.createSequentialGroup()
                     .addComponent(icon)
                     .addComponent(label)
                     .addComponent(buttonPanel)
         );
         }

      return panel;
      }

   protected final JPanel createHorizontalButtonPanel(final ServiceControlPanel serviceControlPanel,
                                                      final SortedMap<Integer, JCheckBox> checkBoxMap,
                                                      final boolean isLabelOnTop)
      {
      return createHorizontalButtonPanel(serviceControlPanel,
                                         checkBoxMap,
                                         isLabelOnTop,
                                         null);
      }

   protected final JPanel createHorizontalButtonPanel(final ServiceControlPanel serviceControlPanel,
                                                      final SortedMap<Integer, JCheckBox> checkBoxMap,
                                                      final boolean isLabelOnTop,
                                                      final Color backgroundColor)
      {
      final JPanel buttonPanel = new JPanel(new SpringLayout());
      if (backgroundColor != null)
         {
         buttonPanel.setBackground(backgroundColor);
         }
      if (isLabelOnTop)
         {
         for (int deviceId = 0; deviceId < checkBoxMap.size(); deviceId++)
            {
            final JLabel label = SwingUtils.createTinyFontLabel(String.valueOf(deviceId + 1));
            label.setHorizontalAlignment(JLabel.CENTER);
            buttonPanel.add(label);
            }
         for (int deviceId = 0; deviceId < checkBoxMap.size(); deviceId++)
            {
            final JCheckBox checkBox = checkBoxMap.get(deviceId);
            if (backgroundColor != null)
               {
               checkBox.setBackground(backgroundColor);
               }
            buttonPanel.add(checkBox);
            }
         }
      else
         {
         for (int deviceId = 0; deviceId < checkBoxMap.size(); deviceId++)
            {
            final JCheckBox checkBox = checkBoxMap.get(deviceId);
            if (backgroundColor != null)
               {
               checkBox.setBackground(backgroundColor);
               }
            buttonPanel.add(checkBox);
            }
         for (int deviceId = 0; deviceId < checkBoxMap.size(); deviceId++)
            {
            final JLabel label = SwingUtils.createTinyFontLabel(String.valueOf(deviceId + 1));
            label.setHorizontalAlignment(JLabel.CENTER);
            buttonPanel.add(label);
            }
         }
      SpringLayoutUtilities.makeCompactGrid(buttonPanel,
                                            2, checkBoxMap.keySet().size(), // rows, cols
                                            0, 0, // initX, initY
                                            0, 0);// xPad, yPad

      final JLabel label = SwingUtils.createLabel(serviceControlPanel.getShortDisplayName());
      final JLabel icon = serviceControlPanel.getLabelImage();
      final Component iconspacer = SwingUtils.createRigidSpacer(5);
      final JPanel panel = new JPanel();
      final GroupLayout layout = new GroupLayout(panel);
      panel.setLayout(layout);
      if (backgroundColor != null)
         {
         panel.setBackground(backgroundColor);
         }

      if (isLabelOnTop)
         {
         layout.setHorizontalGroup(
               layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                     .addGroup(layout.createSequentialGroup()
                                     .addComponent(icon)
                                     .addComponent(iconspacer)
                                     .addComponent(label))
                     .addComponent(buttonPanel));
         layout.setVerticalGroup(
               layout.createSequentialGroup()
                     .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                                     .addComponent(icon)
                                     .addComponent(iconspacer)
                                     .addComponent(label))
                     .addComponent(buttonPanel));
         }
      else
         {
         layout.setHorizontalGroup(
               layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                     .addComponent(buttonPanel)
                     .addGroup(layout.createSequentialGroup()
                                     .addComponent(icon)
                                     .addComponent(iconspacer)
                                     .addComponent(label)));
         layout.setVerticalGroup(
               layout.createSequentialGroup()
                     .addComponent(buttonPanel)
                     .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                                     .addComponent(icon)
                                     .addComponent(iconspacer)
                                     .addComponent(label)));
         }

      return panel;
      }
   }