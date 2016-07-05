package edu.cmu.ri.createlab.expressionbuilder.widgets;

import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.InvocationTargetException;
import java.text.NumberFormat;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.swing.*;
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

   private static final int SLIDER_WIDTH = 100;
   private static final int VALUE_THRESHOLD = 10;
   private static final long TIME_THRESHOLD = 250;

   final JPanel panel = new JPanel();
   private final ExecutorService executorPool = Executors.newCachedThreadPool(new DaemonThreadFactory("DeviceSlider"));
   public final JSlider slider;
   private int last_value;
   private long last_time_changed;
   public final JFormattedTextField textField;
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
      last_value = initialValue;
      last_time_changed = System.currentTimeMillis();
      slider = new JSlider(JSlider.HORIZONTAL, minValue, maxValue, initialValue);
      slider.setBackground(Color.WHITE);
      slider.setFocusable(false);
      textField = new JFormattedTextField(NumberFormat.getIntegerInstance());
      textField.setSelectedTextColor(Color.WHITE);
      textField.setSelectionColor(Color.BLUE);

      // configure widgets
      textField.setColumns(3);
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

      textField.addFocusListener(
            new FocusListener()
               {
               @Override
               public void focusGained(FocusEvent e)
                  {
                  final JFormattedTextField source = (JFormattedTextField)e.getSource();
                  SwingUtilities.invokeLater(
                        new Runnable()
                           {
                           @Override
                           public void run()
                              {
                              source.setText(source.getText());
                              source.selectAll();
                              source.repaint();
                              }
                           });
                  }

               @Override
               public void focusLost(FocusEvent e)
                  {
                  //To change body of implemented methods use File | Settings | File Templates.
                  }
               }
      );

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
                  final Runnable updateValue = new Runnable()
                     {
                     public void run()
                        {
                        executionStrategy.execute(deviceIndex, value);
                        last_time_changed = System.currentTimeMillis();
                        last_value = value;
                        }
                     };
                  if (!source.getValueIsAdjusting())
                     {
                     executorPool.execute(updateValue);
                     }
                  else
                     {
                     if (Math.abs(value - last_value) >= VALUE_THRESHOLD)
                        {
                        executorPool.execute(updateValue);
                        }
                     else if (Math.abs(last_time_changed - System.currentTimeMillis()) >= TIME_THRESHOLD)
                        {
                        executorPool.execute(updateValue);
                        }
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

      slider.setMinimumSize(new Dimension(120, 14));
      slider.setPreferredSize(new Dimension(140, 14));
      slider.setMaximumSize(new Dimension(Integer.MAX_VALUE, 14));

      Dimension textSize = textField.getPreferredSize();
      Dimension newSize = new Dimension(80, textSize.height + 3);
      textField.setMinimumSize(newSize);
      textField.setMaximumSize(newSize);
      textField.setPreferredSize(newSize);

      // layout

      panel.setLayout(new GridBagLayout());

      GridBagConstraints gbc = new GridBagConstraints();

      gbc.fill = GridBagConstraints.HORIZONTAL;
      gbc.gridx = 0;
      gbc.gridy = 0;
      gbc.weighty = 1.0;
      gbc.weightx = 1.0;
      gbc.anchor = GridBagConstraints.CENTER;
      panel.add(slider, gbc);

      gbc.fill = GridBagConstraints.NONE;
      gbc.gridx = 1;
      gbc.gridy = 0;
      gbc.weighty = 1.0;
      gbc.weightx = 0.0;
      gbc.anchor = GridBagConstraints.CENTER;
      panel.add(SwingUtils.createRigidSpacer(), gbc);

      gbc.fill = GridBagConstraints.NONE;
      gbc.gridx = 2;
      gbc.gridy = 0;
      gbc.weighty = 1.0;
      gbc.weightx = 0.0;
      gbc.anchor = GridBagConstraints.CENTER;
      panel.add(textField);
      //panel.add(Box.createHorizontalGlue());
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

   public void getFocus()
      {

      SwingUtilities.invokeLater(
            new Runnable()
               {
               @Override
               public void run()
                  {
                  panel.repaint();
                  textField.requestFocusInWindow();
                  }
               });
      //LOG.debug("Textfield gained focus?: " + textField.hasFocus());

      }

   public interface ExecutionStrategy
      {
      void execute(final int deviceIndex, final int value);
      }
   }
