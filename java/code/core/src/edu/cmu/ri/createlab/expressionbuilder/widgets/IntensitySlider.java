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
      c.fill = GridBagConstraints.HORIZONTAL;
      slide_plus.add(imagePane, c);
      c.gridy = 1;
      c.fill = GridBagConstraints.HORIZONTAL;
      slide_plus.add(slider, c);

      /*   GridLayout grid = new GridLayout(2,1, 0, 0);
      slide_plus.setLayout(grid);
      slide_plus.add(imagePane);
      slide_plus.add(slider);*/

      slide_plus.setAlignmentY(Component.BOTTOM_ALIGNMENT);
      textField.setAlignmentY(Component.BOTTOM_ALIGNMENT);

      panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
      panel.add(slide_plus);
      panel.add(SwingUtils.createRigidSpacer());
      panel.add(textField);
      panel.add(Box.createHorizontalGlue());
      textField.setName("slider_field");

      panel.setAlignmentX(Component.LEFT_ALIGNMENT);
      panel.setName("deviceSliderPanel");
      slide_plus.setName("deviceSliderPanel");
      }
   }
