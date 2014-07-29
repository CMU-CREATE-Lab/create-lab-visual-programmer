package edu.cmu.ri.createlab.sequencebuilder.programelement.view.standard;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.PropertyResourceBundle;
import java.util.Set;
import javax.swing.BoxLayout;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.SwingUtilities;
import edu.cmu.ri.createlab.sequencebuilder.ContainerView;
import edu.cmu.ri.createlab.sequencebuilder.programelement.model.ExpressionModel;
import edu.cmu.ri.createlab.sequencebuilder.programelement.model.ProgramElementModel;
import edu.cmu.ri.createlab.sequencebuilder.programelement.view.dnd.ProgramElementDestinationTransferHandler;
import edu.cmu.ri.createlab.userinterface.util.ImageUtils;
import edu.cmu.ri.createlab.userinterface.util.SwingUtils;
import edu.cmu.ri.createlab.util.MultiLineLabel;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public class StandardExpressionView extends BaseStandardProgramElementView<ExpressionModel>
   {
   private static final Logger LOG = Logger.getLogger(StandardExpressionView.class);

   private static final PropertyResourceBundle RESOURCES = (PropertyResourceBundle)PropertyResourceBundle.getBundle(StandardExpressionView.class.getName());

   private final JPanel displayDelayPanel = new JPanel();
   private final JPanel editDelayPanel = new JPanel();
   private final JPanel iconBlockViewPanelContainer = new JPanel();
   private final JProgressBar delayProgressBar;
   private final MyExecutionEventListener executionEventListener = new MyExecutionEventListener();

   public StandardExpressionView(@NotNull final ContainerView containerView, @NotNull final ExpressionModel model)
      {
      super(containerView, model);

      //final JTextArea titleArea = new JTextArea(2, 15);
      final MultiLineLabel titleLabel = new MultiLineLabel(model.getName(), 2, 15);

      final JButton deleteButton = getDeleteButton();

      /*      //titleLabel.setEditable(false);
      //titleLabel.setText(model.getName());
      //titleLabel.setLineWrap(true);
      //titleLabel.setWrapStyleWord(true);
      //titleLabel.setDragEnabled(true);

      final Dimension title_size = new Dimension(titleArea.getPreferredSize().width, titleArea.getPreferredSize().height);
      titleLabel.setPreferredSize(title_size);
      titleLabel.setMaximumSize(title_size);
      titleLabel.setMinimumSize(title_size);*/

      // Create the delay panel and its components
      final NumberFormat numberFormat = NumberFormat.getNumberInstance();
      numberFormat.setMinimumIntegerDigits(1);
      numberFormat.setMaximumIntegerDigits(3);
      numberFormat.setMinimumFractionDigits(2);
      numberFormat.setMaximumFractionDigits(2);
      numberFormat.setRoundingMode(RoundingMode.HALF_UP);

      final JFormattedTextField delayTextField = new JFormattedTextField(numberFormat);
      delayTextField.setColumns(6);
      final int delayInMillis = model.getDelayInMillis();
      delayTextField.setValue(delayInMillis / 1000.0);
      delayTextField.setFocusLostBehavior(JFormattedTextField.COMMIT_OR_REVERT);
      delayTextField.setName("delayTextField");

      // Create the progress bar, giving it the min and max values.
      delayProgressBar = new JProgressBar(ExpressionModel.MIN_DELAY_VALUE_IN_MILLIS,
                                          computeDelayProgressBarMaximumValue(delayInMillis));
      delayProgressBar.setName("delay_progress");

      final JLabel delayLabel = new JLabel(delayTextField.getText());
      final JLabel delayTextFieldLabel = new JLabel(RESOURCES.getString("delay.label"));
      final JLabel secondsLabel1 = new JLabel(RESOURCES.getString("seconds.label"));
      final JLabel secondsLabel2 = new JLabel(RESOURCES.getString("seconds.label"));
      final JButton displayModeSetDelayButton = new JButton(ImageUtils.createImageIcon("/edu/cmu/ri/createlab/sequencebuilder/programelement/view/images/clock-icon-small.png"));
      final JButton editModeSetDelayButton = new JButton(ImageUtils.createImageIcon("/edu/cmu/ri/createlab/sequencebuilder/programelement/view/images/checkMark.png"));

      displayModeSetDelayButton.setName("thinButton");
      editModeSetDelayButton.setName("thinButton");

      displayDelayPanel.setName("delayPanel");
      editDelayPanel.setName("delayPanel");
      updateIconBlockViewPanel(model);

      // setting the layout is required here so that the icon panel doesn't have an ugly grey border around it
      final BoxLayout iconBlockViewPanelLayout = new BoxLayout(iconBlockViewPanelContainer, BoxLayout.X_AXIS);
      iconBlockViewPanelContainer.setLayout(iconBlockViewPanelLayout);

      final GroupLayout displayDelayPanelLayout = new GroupLayout(displayDelayPanel);
      displayDelayPanel.setLayout(displayDelayPanelLayout);
      displayDelayPanelLayout.setHorizontalGroup(
            displayDelayPanelLayout.createSequentialGroup()
                  .addComponent(delayProgressBar)
                  .addGap(6, 6, 6)
                  .addComponent(delayLabel)
                  .addGap(2, 2, 2)
                  .addComponent(secondsLabel1)
                  .addGap(6, 6, 6)
                  .addComponent(displayModeSetDelayButton)
      );
      displayDelayPanelLayout.setVerticalGroup(
            displayDelayPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
                  .addComponent(delayProgressBar)
                  .addComponent(delayLabel)
                  .addComponent(secondsLabel1)
                  .addComponent(displayModeSetDelayButton)
      );

      final GroupLayout editDelayPanelLayout = new GroupLayout(editDelayPanel);
      editDelayPanel.setLayout(editDelayPanelLayout);
      editDelayPanelLayout.setHorizontalGroup(
            editDelayPanelLayout.createSequentialGroup()
                  .addComponent(delayTextFieldLabel)
                  .addGap(2, 2, 2)
                  .addComponent(delayTextField)
                  .addGap(2, 2, 2)
                  .addComponent(secondsLabel2)
                  .addGap(6, 6, 6)
                  .addComponent(editModeSetDelayButton)
      );
      editDelayPanelLayout.setVerticalGroup(
            editDelayPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
                  .addComponent(delayTextFieldLabel)
                  .addComponent(delayTextField)
                  .addComponent(secondsLabel2)
                  .addComponent(editModeSetDelayButton)
      );
      editDelayPanel.setVisible(false);

      displayModeSetDelayButton.addActionListener(
            new ActionListener()
            {
            @Override
            public void actionPerformed(final ActionEvent actionEvent)
               {
               setIsDelayDisplayMode(false);
               delayTextField.requestFocus();
               SwingUtilities.invokeLater(
                     new Runnable()
                     {
                     @Override
                     public void run()
                        {
                        delayTextField.setText(delayTextField.getText());
                        delayTextField.selectAll();
                        delayTextField.repaint();
                        }
                     });
               }
            }
      );
      editModeSetDelayButton.addActionListener(
            new ActionListener()
            {
            @Override
            public void actionPerformed(final ActionEvent actionEvent)
               {
               setIsDelayDisplayMode(true);
               }
            }
      );

      delayTextField.addPropertyChangeListener(
            "value",
            new PropertyChangeListener()
            {
            public void propertyChange(final PropertyChangeEvent evt)
               {
               if (delayTextField.isEditValid())
                  {
                  float value = ((Number)delayTextField.getValue()).floatValue();
                  if (value < ExpressionModel.MIN_DELAY_VALUE_IN_SECS)
                     {
                     value = ExpressionModel.MIN_DELAY_VALUE_IN_SECS;
                     delayTextField.setValue(value);
                     }
                  else if (value > ExpressionModel.MAX_DELAY_VALUE_IN_SECS)
                     {
                     value = ExpressionModel.MAX_DELAY_VALUE_IN_SECS;
                     delayTextField.setValue(value);
                     }
                  else
                     {
                     final int delayInMillis = (int)(value * 1000);
                     if (LOG.isDebugEnabled())
                        {
                        LOG.debug("StandardExpressionView.propertyChange(): setting delayInMillis in ExpressionModel to = [" + delayInMillis + "]");
                        }
                     model.setDelayInMillis(delayInMillis);
                     }
                  }
               }
            });

      delayTextField.addFocusListener(
            new FocusListener()
            {
            @Override
            public void focusGained(final FocusEvent focusEvent)
               {
               delayTextField.selectAll();
               }

            @Override
            public void focusLost(final FocusEvent focusEvent)
               {
               setIsDelayDisplayMode(true);
               }
            }
      );

      final JPanel delayPanel = new JPanel();
      delayPanel.setLayout(new BoxLayout(delayPanel, BoxLayout.X_AXIS));
      delayPanel.add(displayDelayPanel);
      delayPanel.add(editDelayPanel);

      model.addPropertyChangeEventListener(ExpressionModel.DELAY_IN_MILLIS_PROPERTY,
                                           new ProgramElementModel.PropertyChangeEventListener()
                                           {
                                           @Override
                                           public void handlePropertyChange(@NotNull final ProgramElementModel.PropertyChangeEvent event)
                                              {
                                              SwingUtils.runInGUIThread(
                                                    new Runnable()
                                                    {
                                                    @SuppressWarnings({"ConstantConditions"})
                                                    @Override
                                                    public void run()
                                                       {
                                                       setIsDelayDisplayMode(true);
                                                       final int delayInMillis = (Integer)event.getNewValue();
                                                       final String delayInSecs = numberFormat.format(delayInMillis / 1000.0);
                                                       delayLabel.setText(delayInSecs);
                                                       delayProgressBar.setMaximum(computeDelayProgressBarMaximumValue(delayInMillis));
                                                       }
                                                    }
                                              );
                                              }
                                           });

      model.addExecutionEventListener(executionEventListener);
      model.addRefreshEventListener(
            new ExpressionModel.RefreshEventListener()
            {
            private final Runnable handleRefreshRunnable =
                  new Runnable()
                  {
                  @Override
                  public void run()
                     {
                     updateIconBlockViewPanel(model);
                     }
                  };

            @Override
            public void handleRefresh()
               {
               SwingUtils.runInGUIThread(handleRefreshRunnable);
               }
            });

      final JSeparator sep = new JSeparator(JSeparator.HORIZONTAL);
      final Dimension sep_size = new Dimension(180, 2);
      sep.setPreferredSize(sep_size);
      sep.setMinimumSize(sep_size);
      sep.setMaximumSize(sep_size);

      //Element Layout*****************************
      final JPanel panel = getContentPanel();

      panel.setLayout(new GridBagLayout());

      final GridBagConstraints c = new GridBagConstraints();

      c.gridx = 1;
      c.gridy = 0;
      c.gridwidth = 1;
      c.gridheight = 1;
      c.weightx = 0.0;
      c.weighty = 0.0;
      c.anchor = GridBagConstraints.FIRST_LINE_END;
      c.fill = GridBagConstraints.NONE;
      panel.add(deleteButton, c);

      c.gridx = 0;
      c.gridy = 1;
      c.gridwidth = 2;
      c.gridheight = 1;
      c.weightx = 1.0;
      c.weighty = 0.0;
      c.anchor = GridBagConstraints.PAGE_START;
      c.fill = GridBagConstraints.HORIZONTAL;
      panel.add(titleLabel, c);

      c.gridx = 0;
      c.gridy = 2;
      c.gridwidth = 2;
      c.gridheight = 1;
      c.weightx = 1.0;
      c.weighty = 0.0;
      c.anchor = GridBagConstraints.CENTER;
      c.fill = GridBagConstraints.NONE;
      panel.add(iconBlockViewPanelContainer, c);

      c.gridx = 0;
      c.gridy = 3;
      c.gridwidth = 2;
      c.gridheight = 1;
      c.weightx = 1.0;
      c.weighty = 1.0;
      c.anchor = GridBagConstraints.PAGE_END;
      c.fill = GridBagConstraints.HORIZONTAL;
      panel.add(sep, c);

      c.gridx = 0;
      c.gridy = 4;
      c.gridwidth = 2;
      c.gridheight = 1;
      c.weightx = 1.0;
      c.weighty = 0.0;
      c.insets = new Insets(2, 0, 0, 0);
      c.anchor = GridBagConstraints.PAGE_END;
      c.fill = GridBagConstraints.HORIZONTAL;
      panel.add(delayPanel, c);

      //Skinning Information**********************

      //Background color dependent on container type
      final String panelStyle = containerView.hasParentProgramElementView() ? "expressionElementLoop" : "expressionElement";
      panel.setName(panelStyle);

      final Dimension block_size = new Dimension(180, 120);
      panel.setPreferredSize(block_size);
      panel.setMaximumSize(block_size);
      panel.setMinimumSize(block_size);

      final JScrollPane commentTextAreaScrollPane = getCommentTextAreaScrollPane();
      final Dimension comment_size = commentTextAreaScrollPane.getPreferredSize();
      commentTextAreaScrollPane.setPreferredSize(new Dimension(comment_size.width, block_size.height - 6));
      commentTextAreaScrollPane.setMaximumSize(new Dimension(comment_size.width, block_size.height - 6));
      commentTextAreaScrollPane.setMinimumSize(new Dimension(comment_size.width, block_size.height - 6));

      titleLabel.setName("expressionBlockTitle");
      //*******************************************

      setTransferHandler(
            new ProgramElementDestinationTransferHandler()
            {
            @Override
            public Set<DataFlavor> getSupportedDataFlavors()
               {
               return containerView.getSupportedDataFlavors();
               }

            @Override
            protected final void showInsertLocation(final Point dropPoint)
               {
               StandardExpressionView.this.showInsertLocation(dropPoint);
               }

            @Override
            protected final void performImport(@NotNull final ProgramElementModel model, @NotNull final Point dropPoint)
               {
               getContainerView().handleDropOfModelOntoView(model,
                                                            StandardExpressionView.this,
                                                            isInsertLocationBefore(dropPoint));
               }
            });
      }

   private int computeDelayProgressBarMaximumValue(final int delayInMillis)
      {
      // Note that we artificially set the max value to be 1 ms greater than the min if the duration would be zero (or
      // negative).  We do this only for view purposes, so that, once the expression finishes executing, the progress
      // bar gets rendered at 100% even for zero-length durations
      return delayInMillis <= ExpressionModel.MIN_DELAY_VALUE_IN_MILLIS ? ExpressionModel.MIN_DELAY_VALUE_IN_MILLIS + 1 : delayInMillis;
      }

   private void setIsDelayDisplayMode(final boolean isDelayDisplayMode)
      {
      displayDelayPanel.setVisible(isDelayDisplayMode);
      editDelayPanel.setVisible(!isDelayDisplayMode);
      }

   @Override
   protected void hideInsertLocationsOfContainedViews()
      {
      // nothing to do, there are no contained views
      }

   @Override
   public final void resetViewForSequenceExecution()
      {
      executionEventListener.handleExecutionStart();
      }

   private void updateIconBlockViewPanel(@NotNull final ExpressionModel model)
      {
      iconBlockViewPanelContainer.removeAll();
      iconBlockViewPanelContainer.add(model.getIconBlockView());
      iconBlockViewPanelContainer.repaint();
      }

   private final class MyExecutionEventListener implements ExpressionModel.ExecutionEventListener
      {
      private final Runnable handleExecutionStartRunnable =
            new Runnable()
            {
            @Override
            public void run()
               {
               delayProgressBar.setValue(delayProgressBar.getMinimum());
               }
            };

      private final Runnable handleExecutionEndRunnable =
            new Runnable()
            {
            @Override
            public void run()
               {
               delayProgressBar.setValue(delayProgressBar.getMaximum());
               }
            };

      @Override
      public void handleExecutionStart()
         {
         SwingUtils.runInGUIThread(handleExecutionStartRunnable);
         }

      @Override
      public void handleElapsedTimeInMillis(final int millis)
         {
         SwingUtils.runInGUIThread(
               new Runnable()
               {
               @Override
               public void run()
                  {
                  delayProgressBar.setValue(millis);
                  }
               }
         );
         }

      @Override
      public void handleExecutionEnd()
         {
         SwingUtils.runInGUIThread(handleExecutionEndRunnable);
         }
      }
   }
