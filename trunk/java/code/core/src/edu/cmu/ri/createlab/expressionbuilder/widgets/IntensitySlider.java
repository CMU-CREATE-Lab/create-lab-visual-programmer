package edu.cmu.ri.createlab.expressionbuilder.widgets;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import edu.cmu.ri.createlab.userinterface.util.SwingUtils;

/**
 * Created by IntelliJ IDEA.
 * User: jcross1
 * Date: 6/14/11
 * Time: 11:39 AM
 * To change this template use File | Settings | File Templates.
 */
public class IntensitySlider extends DeviceSlider
   {

   public IntensitySlider(final int deviceIndex,
                          final int minValue,
                          final int maxValue,
                          final int initialValue,
                          final int minorTickSpacing,
                          final int majorTickSpacing,
                          final ExecutionStrategy executionStrategy,
                          final String LFName)
      {
      super(deviceIndex, minValue, maxValue, initialValue, minorTickSpacing, majorTickSpacing, executionStrategy);

      panel.removeAll();

      final JPanel slide_plus = new JPanel();
      final JPanel imagePane = new JPanel();

      imagePane.setName(LFName);
      imagePane.setLayout(new BoxLayout(imagePane, BoxLayout.X_AXIS));
      imagePane.add(SwingUtils.createRigidSpacer(13));

      slide_plus.setLayout(new GridBagLayout());
      final GridBagConstraints c = new GridBagConstraints();
      c.gridx = 0;
      c.gridy = 0;
      c.weighty = 1.0;
      c.weightx = 1.0;
      c.fill = GridBagConstraints.HORIZONTAL;
      c.anchor = GridBagConstraints.PAGE_END;
      slide_plus.add(imagePane, c);
      c.gridy = 1;
      c.weighty = 0.0;
      c.anchor = GridBagConstraints.PAGE_START;
      c.weightx = 1.0;
      c.fill = GridBagConstraints.HORIZONTAL;
      slide_plus.add(slider, c);


      panel.setLayout(new GridBagLayout());

      c.fill = GridBagConstraints.HORIZONTAL;
      c.gridx = 0;
      c.gridy = 0;
      c.weighty = 1.0;
      c.weightx = 1.0;
      c.anchor = GridBagConstraints.PAGE_END;
      panel.add(slide_plus, c);

      c.fill = GridBagConstraints.NONE;
      c.gridx = 1;
      c.gridy = 0;
      c.weighty = 1.0;
      c.weightx = 0.0;
      c.anchor = GridBagConstraints.PAGE_END;
      panel.add(SwingUtils.createRigidSpacer(), c);

      c.fill = GridBagConstraints.NONE;
      c.gridx = 2;
      c.gridy = 0;
      c.weighty = 1.0;
      c.weightx = 0.0;
      c.anchor = GridBagConstraints.PAGE_END;
      panel.add(textField, c);

      textField.setName("slider_field");

      panel.setAlignmentX(Component.LEFT_ALIGNMENT);
      panel.setName("deviceSliderPanel");
      slide_plus.setName("deviceSliderPanel");
      }
   }
