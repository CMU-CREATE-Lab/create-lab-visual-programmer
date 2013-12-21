package edu.cmu.ri.createlab.expressionbuilder.widgets;

import edu.cmu.ri.createlab.userinterface.util.ImageUtils;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.*;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: jcross
 * Date: 12/21/11
 * Time: 12:12 PM
 * To change this template use File | Settings | File Templates.
 */
public class HelpText extends JPanel{

    final JPanel mainPanel = this;

    public HelpText (final Map<String, SortedMap<Integer, JCheckBox>> serviceDeviceToggleButtonMap, String direction)
    {

        Set keys = serviceDeviceToggleButtonMap.keySet();
        final List<JToggleButton> checkedList = new LinkedList<JToggleButton>();

        this.setLayout(new GridBagLayout());

        JLabel helpText1 = new JLabel("Click On An");
        JLabel helpText2 = new JLabel("Output Port");
        helpText2.setIcon(ImageUtils.createImageIcon("/edu/cmu/ri/createlab/expressionbuilder/widgets/images/portExample.png"));
        helpText2.setHorizontalTextPosition(JLabel.LEADING);
        JLabel helpText3 = new JLabel(direction + " to Start");

        helpText1.setName("stageHelpDark");
        helpText2.setName("stageHelpDark");
        helpText3.setName("stageHelpDark");

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.CENTER;
        c.gridy = 0;
        c.weighty = 0.0;
        this.add(helpText1, c);
        c.anchor = GridBagConstraints.CENTER;
        c.gridy = 1;
        c.weighty = 0.0;
        this.add(helpText2, c);
        c.anchor = GridBagConstraints.CENTER;
        c.gridy = 2;
        c.weighty = 0.0;
        this.add(helpText3, c);

        this.setVisible(true);
        this.setName("purpleElement");

        ItemListener listener = new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                //To change body of implemented methods use File | Settings | File Templates.
                JCheckBox changedBox = (JCheckBox)e.getSource();

                if (changedBox.isSelected()){
                    checkedList.add(changedBox);
                }
                else{
                    checkedList.remove(changedBox);
                }

                mainPanel.setVisible(checkedList.isEmpty());
            }
        };

        for (Object key : keys){
           SortedMap<Integer, JCheckBox> checkBoxMap = serviceDeviceToggleButtonMap.get(key);
            for (final int deviceId : checkBoxMap.keySet())
                 {
                 final JCheckBox checkBox = checkBoxMap.get(deviceId);
                 //checkBox.addActionListener(listener);
                 checkBox.addItemListener(listener);
                 }
        }

    }
}
