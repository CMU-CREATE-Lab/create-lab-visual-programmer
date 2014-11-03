package edu.cmu.ri.createlab.expressionbuilder.controlpanel;

import java.awt.Color;
import java.awt.Component;
import java.util.Map;
import java.util.SortedMap;
import javax.swing.*;

import edu.cmu.ri.createlab.userinterface.util.SpringLayoutUtilities;
import edu.cmu.ri.createlab.userinterface.util.SwingUtils;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public abstract class DeviceGUI
   {
   public abstract void createGUI(final JPanel mainPanel,
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
                                       null, null,
                                       "image.green");
      }

   protected final JPanel createVerticalButtonPanel(final ServiceControlPanel serviceControlPanel,
                                                    final SortedMap<Integer, JCheckBox> checkBoxMap,
                                                    final boolean isRotateClockwise,
                                                    final Color backgroundColor,
                                                    final Color boxColor,
                                                    final String imageName)
       {
          return createVerticalButtonPanel(serviceControlPanel,
                                           checkBoxMap,
                                           isRotateClockwise,
                                           backgroundColor,
                                           boxColor,
                                           imageName,
                                           Color.BLACK);
       }


   protected final JPanel createVerticalButtonPanel(final ServiceControlPanel serviceControlPanel,
                                                    final SortedMap<Integer, JCheckBox> checkBoxMap,
                                                    final boolean isRotateClockwise,
                                                    final Color backgroundColor,
                                                    final Color boxColor,
                                                    final String imageName,
                                                    final Color titleColor)
      {
      final JPanel buttonPanel = new JPanel(new SpringLayout());

      if (backgroundColor != null)
         {
         buttonPanel.setBackground(boxColor);
         }
      for (final int deviceId : checkBoxMap.keySet())
         {
         final JCheckBox checkBox = checkBoxMap.get(deviceId);
         if (boxColor != null)
            {
            checkBox.setBackground(boxColor);
            }

         if (isRotateClockwise)
            {
            buttonPanel.add(checkBox);
            buttonPanel.add(SwingUtils.createRigidSpacer(4));
            JLabel numLabel = SwingUtils.createTinyFontLabel(String.valueOf(deviceId + 1));
            if (boxColor != null){numLabel.setBackground(boxColor);}
            buttonPanel.add(numLabel);
            buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 2));

            }
         else
            {
            JLabel numLabel = SwingUtils.createTinyFontLabel(String.valueOf(deviceId + 1));
            if (boxColor != null){numLabel.setBackground(boxColor);}
            buttonPanel.add(numLabel);
            buttonPanel.add(SwingUtils.createRigidSpacer(4));
            buttonPanel.add(checkBox);
            buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 2, 0,0));

            }
         }
      SpringLayoutUtilities.makeCompactGrid(buttonPanel,
                                            checkBoxMap.keySet().size(), 3, // rows, cols
                                            0, 0, // initX, initY
                                            0, 0);// xPad, yPad


      //final JLabel label = SwingUtils.createVerticalLabel(serviceControlPanel.getShortDisplayName(), isRotateClockwise);
      final JLabel label = SwingUtils.createLabel(serviceControlPanel.getShortDisplayName());
      final JLabel icon = serviceControlPanel.getLabelImage(imageName);
      final JPanel panel = new JPanel();
      final GroupLayout layout = new GroupLayout(panel);

      label.setForeground(titleColor);

      panel.setLayout(layout);
      if (backgroundColor != null)
         {
         panel.setBackground(backgroundColor);
         }

      if (isRotateClockwise)
         {

         layout.setHorizontalGroup(
               layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                     .addComponent(buttonPanel)
                     .addComponent(label)
                     .addComponent(icon));
         layout.setVerticalGroup(
               layout.createSequentialGroup()
                     .addComponent(icon)
                     .addComponent(label)
                     .addGap(5)
                     .addComponent(buttonPanel)
         );
         }
      else
         {

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
                                         null, null, false, "image.green");
      }

   protected final JPanel createHorizontalButtonPanel(final ServiceControlPanel serviceControlPanel,
                                                      final SortedMap<Integer, JCheckBox> checkBoxMap,
                                                      final boolean isLabelOnTop,
                                                      final Color backgroundColor)
      {
       return createHorizontalButtonPanel(serviceControlPanel,
                                         checkBoxMap,
                                         isLabelOnTop,
                                         backgroundColor, null, false, "image.green");

      }

   protected final JPanel createHorizontalButtonPanel(final ServiceControlPanel serviceControlPanel,
                                                          final SortedMap<Integer, JCheckBox> checkBoxMap,
                                                          final boolean isLabelOnTop,
                                                          final Color backgroundColor,
                                                          final Color boxColor,
                                                          final boolean isReversedOrder,
                                                          final String imageName)
   {
       return createHorizontalButtonPanel(serviceControlPanel,
                                          checkBoxMap,
                                          isLabelOnTop,
                                          backgroundColor,
                                          boxColor,
                                          isReversedOrder,
                                          imageName,
                                          Color.BLACK);
   }


   protected final JPanel createHorizontalButtonPanel(final ServiceControlPanel serviceControlPanel,
                                                      final SortedMap<Integer, JCheckBox> checkBoxMap,
                                                      final boolean isLabelOnTop,
                                                      final Color backgroundColor,
                                                      final Color boxColor,
                                                      final boolean isReversedOrder,
                                                      final String imageName,
                                                      final Color titleColor)
      {
      final JPanel buttonPanel = new JPanel(new SpringLayout());
      if (backgroundColor != null)
         {
         buttonPanel.setBackground(boxColor);
         }
      if (isLabelOnTop && !isReversedOrder)
         {
         for (int deviceId = 0; deviceId < checkBoxMap.size(); deviceId++)
            {
            final JLabel label = SwingUtils.createTinyFontLabel(String.valueOf(deviceId + 1));
            label.setHorizontalAlignment(JLabel.CENTER);
            if (boxColor != null){label.setBackground(boxColor);}
            buttonPanel.add(label);
            }
         for (int deviceId = 0; deviceId < checkBoxMap.size(); deviceId++)
            {
            buttonPanel.add(SwingUtils.createRigidSpacer(4));
            }
         for (int deviceId = 0; deviceId < checkBoxMap.size(); deviceId++)
            {
            final JCheckBox checkBox = checkBoxMap.get(deviceId);
            if (backgroundColor != null)
               {
               checkBox.setBackground(boxColor);

               }
            buttonPanel.add(checkBox);
            }
         }
      else if (isLabelOnTop && isReversedOrder)
         {
         for (int deviceId = checkBoxMap.size()-1; deviceId >= 0; deviceId--)
            {
            final JLabel label = SwingUtils.createTinyFontLabel(String.valueOf(deviceId + 1));
            label.setHorizontalAlignment(JLabel.CENTER);
            if (boxColor != null){label.setBackground(boxColor);}
            buttonPanel.add(label);
            }
         for (int deviceId = 0; deviceId < checkBoxMap.size(); deviceId++)
            {
            buttonPanel.add(SwingUtils.createRigidSpacer(4));
            }
         for (int deviceId = checkBoxMap.size()-1; deviceId >= 0; deviceId--)
            {
            final JCheckBox checkBox = checkBoxMap.get(deviceId);
            if (backgroundColor != null)
               {
               checkBox.setBackground(boxColor);
               }
            buttonPanel.add(checkBox);

            }
         }
       else if (!isLabelOnTop && isReversedOrder)
         {
       for (int deviceId = checkBoxMap.size()-1; deviceId >= 0; deviceId--)
            {
            final JCheckBox checkBox = checkBoxMap.get(deviceId);
            if (backgroundColor != null)
               {
               checkBox.setBackground(boxColor);

               }
            buttonPanel.add(checkBox);
            }
         for (int deviceId = 0; deviceId < checkBoxMap.size(); deviceId++)
            {
            buttonPanel.add(SwingUtils.createRigidSpacer(4));
            }
         for (int deviceId = checkBoxMap.size()-1; deviceId >= 0; deviceId--)
            {
            final JLabel label = SwingUtils.createTinyFontLabel(String.valueOf(deviceId + 1));
            label.setHorizontalAlignment(JLabel.CENTER);
            if (boxColor != null){label.setBackground(boxColor);}
            buttonPanel.add(label);
            }
         }
      else
         {
         for (int deviceId = 0; deviceId < checkBoxMap.size(); deviceId++)
            {
            final JCheckBox checkBox = checkBoxMap.get(deviceId);
            if (backgroundColor != null)
               {
               checkBox.setBackground(boxColor);

               }
            buttonPanel.add(checkBox);
            }
         for (int deviceId = 0; deviceId < checkBoxMap.size(); deviceId++)
            {
            buttonPanel.add(SwingUtils.createRigidSpacer(4));
            }
         for (int deviceId = 0; deviceId < checkBoxMap.size(); deviceId++)
            {
            final JLabel label = SwingUtils.createTinyFontLabel(String.valueOf(deviceId + 1));
            label.setHorizontalAlignment(JLabel.CENTER);
            if (boxColor != null){label.setBackground(boxColor);}
            buttonPanel.add(label);
            }
         }
      SpringLayoutUtilities.makeCompactGrid(buttonPanel,
                                            3, checkBoxMap.keySet().size(), // rows, cols
                                            0, 0, // initX, initY
                                            0, 0);// xPad, yPad

      final JLabel label = SwingUtils.createLabel(serviceControlPanel.getShortDisplayName());
      final JLabel icon = serviceControlPanel.getLabelImage(imageName);
      final Component iconspacer = SwingUtils.createRigidSpacer(5);
      final JPanel panel = new JPanel();
      final GroupLayout layout = new GroupLayout(panel);

      label.setForeground(titleColor);

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
         buttonPanel.setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));
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
         buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 2, 0));
         }

      return panel;
      }
   }