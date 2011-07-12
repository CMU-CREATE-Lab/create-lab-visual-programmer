package edu.cmu.ri.createlab.expressionbuilder.widgets;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.InvocationTargetException;
import java.text.NumberFormat;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFormattedTextField;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import edu.cmu.ri.createlab.userinterface.GUIConstants;
import edu.cmu.ri.createlab.userinterface.util.SwingUtils;
import edu.cmu.ri.createlab.util.thread.DaemonThreadFactory;
import org.apache.log4j.Logger;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
@SuppressWarnings({"CloneableClassWithoutClone"})
public class DeviceSlider
   {
   private static final Logger LOG = Logger.getLogger(DeviceSlider.class);

   private static final int SLIDER_WIDTH = 120;

   final JPanel panel = new JPanel();
   private final ExecutorService executorPool = Executors.newCachedThreadPool(new DaemonThreadFactory("DeviceSlider"));
   public final JSlider slider;
   final JFormattedTextField textField;
   private final ChangeListener sliderChangeListenerForExecutionStrategy;

   public DeviceSlider(final int deviceIndex,
                       final int minValue,
                       final int maxValue,
                       final int initialValue,
                       final int minorTickSpacing,
                       final int majorTickSpacing,
                       final ExecutionStrategy executionStrategy)
      {
      // declare widgets
      slider = new JSlider(JSlider.HORIZONTAL, minValue, maxValue, initialValue);
      slider.setBackground(Color.WHITE);
      textField = new JFormattedTextField(NumberFormat.getIntegerInstance());
      textField.setSelectedTextColor(Color.WHITE);
      textField.setSelectionColor(Color.BLUE);

      // configure widgets
      textField.setColumns(4);
      textField.setFont(GUIConstants.FONT_NORMAL);
      textField.setValue(initialValue);
      textField.addPropertyChangeListener(
            "value",
            new PropertyChangeListener()
            {
            public void propertyChange(final PropertyChangeEvent evt)
               {
               if (textField.isEditValid())
                  {
                  int value = ((Number)textField.getValue()).intValue();
                  if (value < minValue)
                     {
                     value = minValue;
                     textField.setValue(value);
                     }
                  else if (value > maxValue)
                     {
                     value = maxValue;
                     textField.setValue(value);
                     }
                  else
                     {
                     slider.setValue(value);
                     }
                  }
               }
            });

      slider.setFont(GUIConstants.FONT_NORMAL);
      slider.setMinorTickSpacing(minorTickSpacing);
      slider.setMajorTickSpacing(majorTickSpacing);
      slider.setPaintTicks(false);
      slider.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
      sliderChangeListenerForExecutionStrategy =
            new ChangeListener()
            {
            public void stateChanged(final ChangeEvent e)
               {
               final JSlider source = (JSlider)e.getSource();
               final int value = source.getValue();
               if (!source.getValueIsAdjusting())
                  {
                  executorPool.execute(
                        new Runnable()
                        {
                        public void run()
                           {
                           executionStrategy.execute(deviceIndex, value);
                           }
                        });
                  }
               }
            };
      slider.addChangeListener(sliderChangeListenerForExecutionStrategy);
      slider.addChangeListener(
            new ChangeListener()
            {
            public void stateChanged(final ChangeEvent e)
               {
               final JSlider source = (JSlider)e.getSource();
               final int value = source.getValue();
               textField.setText(String.valueOf(value));
               }
            });

      slider.setMinimumSize(new Dimension(SLIDER_WIDTH, 20));
      slider.setPreferredSize(new Dimension(SLIDER_WIDTH + 40, 20));
      slider.setMaximumSize(new Dimension(SLIDER_WIDTH + 80, 20));

      Dimension textSize = textField.getPreferredSize();
      Dimension newSize = new Dimension(textSize.width, textSize.height + 3);
      textField.setMinimumSize(newSize);
      textField.setMaximumSize(newSize);
      textField.setPreferredSize(newSize);

      // layout

      panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
      panel.add(slider);
      panel.add(SwingUtils.createRigidSpacer());
      panel.add(textField);
      panel.add(Box.createHorizontalGlue());
      textField.setName("slider_field");

      panel.setAlignmentX(Component.LEFT_ALIGNMENT);
      panel.setName("deviceSliderPanel");
      }

   public Component getComponent()
      {
      return panel;
      }

   /**
    * Sets the slider's value (and updates the text field) and also causes the {@link ExecutionStrategy} to be fired.
    * This method ensures that it runs within the Swing GUI thread, so callers don't have to worry about it.
    */
   public void setValue(final int value)
      {
      if (SwingUtilities.isEventDispatchThread())
         {
         slider.setValue(value);
         }
      else
         {
         SwingUtilities.invokeLater(
               new Runnable()
               {
               public void run()
                  {
                  slider.setValue(value);
                  }
               });
         }
      }

   /**
    * Sets the slider's value (and updates the text field), but doesn't cause the {@link ExecutionStrategy} to be fired.
    * This method ensures that it runs within the Swing GUI thread, so callers don't have to worry about it.
    */
   public void setValueNoExecution(final int value)
      {
      if (SwingUtilities.isEventDispatchThread())
         {
         setValueNoExecutionWorkhorse(value);
         }
      else
         {
         SwingUtilities.invokeLater(
               new Runnable()
               {
               public void run()
                  {
                  setValueNoExecutionWorkhorse(value);
                  }
               });
         }
      }

   private void setValueNoExecutionWorkhorse(final int value)
      {
      slider.removeChangeListener(sliderChangeListenerForExecutionStrategy);
      slider.setValue(value);
      slider.addChangeListener(sliderChangeListenerForExecutionStrategy);
      }

   /**
    * Gets the slider's value, or returns <code>null</code> if the value could not be retrieved. This method ensures
    * that it runs within the Swing GUI thread, so callers don't have to worry about it.
    */
   public Integer getValue()
      {
      if (SwingUtilities.isEventDispatchThread())
         {
         return slider.getValue();
         }
      else
         {
         final int[] value = new int[1];
         try
            {
            SwingUtilities.invokeAndWait(
                  new Runnable()
                  {
                  public void run()
                     {
                     value[0] = slider.getValue();
                     }
                  });

            return value[0];
            }
         catch (InterruptedException e)
            {
            LOG.error("InterruptedException while trying to get the slider value", e);
            }
         catch (InvocationTargetException e)
            {
            LOG.error("InvocationTargetException while trying to get the slider value", e);
            }
         }
      return null;
      }

   public interface ExecutionStrategy
      {
      void execute(final int deviceIndex, final int value);
      }
   }
