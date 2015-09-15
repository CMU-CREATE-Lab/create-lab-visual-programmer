package edu.cmu.ri.createlab.sequencebuilder.programelement.view.standard;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.NumberFormat;
import java.util.PropertyResourceBundle;
import java.util.Set;
import javax.swing.*;
import javax.swing.border.Border;
import edu.cmu.ri.createlab.sequencebuilder.ContainerView;
import edu.cmu.ri.createlab.sequencebuilder.programelement.model.CounterLoopModel;
import edu.cmu.ri.createlab.sequencebuilder.programelement.model.ProgramElementModel;
import edu.cmu.ri.createlab.sequencebuilder.programelement.view.ViewConstants;
import edu.cmu.ri.createlab.sequencebuilder.programelement.view.dnd.AlwaysInsertAfterTransferHandler;
import edu.cmu.ri.createlab.sequencebuilder.programelement.view.dnd.AlwaysInsertBeforeTransferHandler;
import edu.cmu.ri.createlab.sequencebuilder.programelement.view.dnd.ProgramElementDestinationTransferHandler;
import edu.cmu.ri.createlab.userinterface.util.ImageUtils;
import edu.cmu.ri.createlab.userinterface.util.SwingUtils;
import edu.cmu.ri.createlab.xml.SaveXmlDocumentDialogRunnable;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public class StandardCounterLoopView extends BaseStandardProgramElementView<CounterLoopModel>
   {
   private static final Logger LOG = Logger.getLogger(StandardCounterLoopView.class);

   private static final PropertyResourceBundle RESOURCES = (PropertyResourceBundle)PropertyResourceBundle.getBundle(StandardCounterLoopView.class.getName());
   private static final Dimension PREFERRED_CONTAINER_DIMENSION = new Dimension(196, 220);

   private final JPanel displayIterationsPanel = new JPanel();
   private final JPanel editIterationsPanel = new JPanel();
   private final JButton displayModeEditButton = new JButton(ImageUtils.createImageIcon("/edu/cmu/ri/createlab/sequencebuilder/programelement/view/images/smallLock.png"));
   private final JButton editModeEditButton = new JButton(ImageUtils.createImageIcon("/edu/cmu/ri/createlab/sequencebuilder/programelement/view/images/smallUnlock.png"));
   private final ContainerView loopContainerView;
   private final JProgressBar iterationsProgressBar;
   private final MyExecutionEventListener executionEventListener = new MyExecutionEventListener();
   private final JPanel containerViewPanel;

   private final ImageIcon greenArrow = ImageUtils.createImageIcon("/edu/cmu/ri/createlab/sequencebuilder/programelement/view/images/greenArrow.png");
   private final ImageIcon wideOrangeArrow = ImageUtils.createImageIcon("/edu/cmu/ri/createlab/sequencebuilder/programelement/view/images/wideOrangeArrow.png");
   private final Border arrowBorder = BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(0, 128, 0), 3), BorderFactory.createMatteBorder(16, 0, 0, 0, greenArrow));
   private final Border selectedBorder = BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.BLACK, 1), arrowBorder);
   private final Border orangeArrowBorder = BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3), BorderFactory.createMatteBorder(16, 0, 0, 0, wideOrangeArrow));
   private final Border unselectedBorder = BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.BLACK, 1), orangeArrowBorder);

   public StandardCounterLoopView(@NotNull final ContainerView containerView, @NotNull final CounterLoopModel model)
      {
      super(containerView, model);

      loopContainerView = new ContainerView(containerView.getJFrame(),
                                            model.getContainerModel(),
                                            new StandardViewFactory(),
                                            this);
      loopContainerView.refresh();  // need this for case where the model was loaded from XML, so the contained views haven't been created yet--this forces their creation and display

      // configure the top bar area ------------------------------------------------------------------------------------

      final JPanel topBarPanel = new JPanel();
      final JLabel titleLabel = new JLabel(RESOURCES.getString("title.label"));
      titleLabel.setName("loopBlockTitle");
      titleLabel.setIcon(ImageUtils.createImageIcon("/edu/cmu/ri/createlab/sequencebuilder/programelement/view/images/count_icon.png"));

      final JButton deleteButton = getDeleteButton();
      final JButton moveUpButton = getMoveUpButton();
      final JButton moveDownButon = getMoveDownButton();


      displayModeEditButton.setName("thinButton");
      editModeEditButton.setName("thinButton");
      editModeEditButton.setVisible(false);

      displayModeEditButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
      editModeEditButton.setCursor(new Cursor(Cursor.HAND_CURSOR));


      final JPanel editButtonsPanel = new JPanel();
      editButtonsPanel.setLayout(new BoxLayout(editButtonsPanel, BoxLayout.X_AXIS));
      editButtonsPanel.add(displayModeEditButton);
      editButtonsPanel.add(editModeEditButton);

      // Create the iteration panel and its components
      final NumberFormat numberFormat = NumberFormat.getIntegerInstance();
      numberFormat.setMinimumIntegerDigits(1);
      numberFormat.setMaximumIntegerDigits(6);

      final JFormattedTextField iterationsTextField = new JFormattedTextField(numberFormat);
      iterationsTextField.setColumns(6);
      iterationsTextField.setValue(model.getNumberOfIterations());
      iterationsTextField.setFocusLostBehavior(JFormattedTextField.COMMIT_OR_REVERT);
      iterationsTextField.setName("iterTextField");

      iterationsProgressBar = new JProgressBar(0,
                                               model.getNumberOfIterations());
      iterationsProgressBar.setName("count_progress");
      iterationsProgressBar.setCursor(new Cursor(Cursor.HAND_CURSOR));

      final JLabel iterationsLabel = new JLabel(iterationsTextField.getText());
      final JLabel iterationsTextFieldLabel = new JLabel(RESOURCES.getString("number-of-iterations.label"));
      final JLabel timesLabel = new JLabel(model.getNumberOfIterations() == 1 ? RESOURCES.getString("times-singular.label") : RESOURCES.getString("times-plural.label"));

      timesLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
      iterationsLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));

      final GroupLayout displayIterationsPanelLayout = new GroupLayout(displayIterationsPanel);
      displayIterationsPanel.setLayout(displayIterationsPanelLayout);
      displayIterationsPanelLayout.setHorizontalGroup(
            displayIterationsPanelLayout.createSequentialGroup()
                  .addComponent(iterationsProgressBar)
                  .addGap(6, 6, 6)
                  .addComponent(iterationsLabel)
                  .addGap(2, 2, 2)
                  .addComponent(timesLabel)
      );
      displayIterationsPanelLayout.setVerticalGroup(
            displayIterationsPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER, false)
                  .addComponent(iterationsProgressBar)
                  .addComponent(iterationsLabel, 18, 18, 18)
                  .addComponent(timesLabel, 18, 18, 18)
      );

      final GroupLayout editIterationsPanelLayout = new GroupLayout(editIterationsPanel);
      editIterationsPanel.setLayout(editIterationsPanelLayout);
      editIterationsPanelLayout.setHorizontalGroup(
            editIterationsPanelLayout.createSequentialGroup()
                  .addComponent(iterationsTextFieldLabel)
                  .addGap(2, 2, 2)
                  .addComponent(iterationsTextField)
      );
      editIterationsPanelLayout.setVerticalGroup(
            editIterationsPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER, false)
                  .addComponent(iterationsTextFieldLabel, 18, 18, 18)
                  .addComponent(iterationsTextField, 18, 18, 18)
      );
      editIterationsPanel.setVisible(false);

      displayModeEditButton.addActionListener(
            new ActionListener()
            {
            @Override
            public void actionPerformed(final ActionEvent actionEvent)
               {
               setIsIterationCountDisplayMode(false);
               iterationsTextField.requestFocusInWindow();
               iterationsTextField.setText(iterationsTextField.getText()); // silly magic that actually makes the selectAll() work
               iterationsTextField.selectAll();
               iterationsTextField.repaint();
               }
            });

       iterationsProgressBar.addMouseListener(new MouseAdapter()
       {
       public void mouseClicked(MouseEvent e)
          {
          setIsIterationCountDisplayMode(false);
          iterationsTextField.requestFocusInWindow();
          iterationsTextField.setText(iterationsTextField.getText()); // silly magic that actually makes the selectAll() work
          iterationsTextField.selectAll();
          iterationsTextField.repaint();
          }
       });

      iterationsLabel.addMouseListener(new MouseAdapter()
      {
      public void mouseClicked(MouseEvent e)
         {
         setIsIterationCountDisplayMode(false);
         iterationsTextField.requestFocusInWindow();
         iterationsTextField.setText(iterationsTextField.getText()); // silly magic that actually makes the selectAll() work
         iterationsTextField.selectAll();
         iterationsTextField.repaint();
         }
      });

      timesLabel.addMouseListener(new MouseAdapter()
      {
      public void mouseClicked(MouseEvent e)
         {
         setIsIterationCountDisplayMode(false);
         iterationsTextField.requestFocusInWindow();
         iterationsTextField.setText(iterationsTextField.getText()); // silly magic that actually makes the selectAll() work
         iterationsTextField.selectAll();
         iterationsTextField.repaint();
         }
      });

      editModeEditButton.addActionListener(
            new ActionListener()
            {
            @Override
            public void actionPerformed(final ActionEvent actionEvent)
               {
               setIsIterationCountDisplayMode(true);
               }
            });

      iterationsTextField.addPropertyChangeListener(
            "value",
            new PropertyChangeListener()
            {
            public void propertyChange(final PropertyChangeEvent evt)
               {
               if (iterationsTextField.isEditValid())
                  {
                  int numIterations = ((Number)iterationsTextField.getValue()).intValue();
                  if (numIterations < CounterLoopModel.MIN_NUMBER_OF_ITERATIONS)
                     {
                     numIterations = CounterLoopModel.MIN_NUMBER_OF_ITERATIONS;
                     iterationsTextField.setValue(numIterations);
                     }
                  else if (numIterations > CounterLoopModel.MAX_NUMBER_OF_ITERATIONS)
                     {
                     numIterations = CounterLoopModel.MAX_NUMBER_OF_ITERATIONS;
                     iterationsTextField.setValue(numIterations);
                     }
                  else
                     {
                     if (LOG.isDebugEnabled())
                        {
                        LOG.debug("StandardCounterLoopView.propertyChange(): setting numIterations in CounterLoopModel to = [" + numIterations + "]");
                        }
                     model.setNumberOfIterations(numIterations);
                     }
                  }
               }
            });

      iterationsTextField.addFocusListener(
            new FocusAdapter()
            {
            @Override
            public void focusLost(final FocusEvent focusEvent)
               {
               setIsIterationCountDisplayMode(true);
               }

            @Override
            public void focusGained(final FocusEvent focusEvent)
               {
               iterationsTextField.selectAll();
               }
            }
      );
      final JPanel iterationsPanel = new JPanel();
      iterationsPanel.setLayout(new GridBagLayout());
      iterationsPanel.add(displayIterationsPanel);
      iterationsPanel.add(editIterationsPanel);
      iterationsPanel.setName("iterationPanel");
      displayIterationsPanel.setName("iterationPanel");
      editIterationsPanel.setName("iterationPanel");

      model.addPropertyChangeEventListener(CounterLoopModel.NUMBER_OF_ITERATIONS_PROPERTY,
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
                                                       setIsIterationCountDisplayMode(true);
                                                       final int numIterations = (Integer)event.getNewValue();
                                                       iterationsLabel.setText(String.valueOf(numIterations));
                                                       timesLabel.setText(numIterations == 1 ? RESOURCES.getString("times-singular.label") : RESOURCES.getString("times-plural.label"));
                                                       iterationsProgressBar.setMaximum(numIterations);
                                                       }
                                                    }
                                              );
                                              }
                                           });

      model.addExecutionEventListener(executionEventListener);

      topBarPanel.setLayout(new GridBagLayout());
      final GridBagConstraints c = new GridBagConstraints();

      c.gridx = 0;
      c.gridy = 0;
      c.gridwidth = 1;
      c.gridheight = 1;
      c.weightx = 1.0;
      c.weighty = 0.0;
      c.anchor = GridBagConstraints.FIRST_LINE_START;
      c.fill = GridBagConstraints.NONE;
      topBarPanel.add(titleLabel, c);

      c.gridx = 1;
      c.gridy = 0;
      c.gridwidth = 1;
      c.gridheight = 1;
      c.weightx = 0.0;
      c.weighty = 0.0;
      c.anchor = GridBagConstraints.PAGE_START;
      c.fill = GridBagConstraints.NONE;
      topBarPanel.add(editButtonsPanel, c);

      c.gridx = 2;
      c.gridy = 0;
      c.gridwidth = 1;
      c.gridheight = 1;
      c.weightx = 0.0;
      c.weighty = 0.0;
      c.anchor = GridBagConstraints.FIRST_LINE_END;
      c.fill = GridBagConstraints.NONE;
      topBarPanel.add(deleteButton, c);

      c.gridx = 2;
      c.gridy = 1;
      c.gridwidth = 1;
      c.gridheight = 1;
      c.weightx = 0.0;
      c.weighty = 0.0;
      c.anchor = GridBagConstraints.FIRST_LINE_END;
      c.fill = GridBagConstraints.NONE;
      topBarPanel.add(moveUpButton, c);

      c.gridx = 2;
      c.gridy = 2;
      c.gridwidth = 1;
      c.gridheight = 1;
      c.weightx = 0.0;
      c.weighty = 0.0;
      c.anchor = GridBagConstraints.FIRST_LINE_END;
      c.fill = GridBagConstraints.NONE;
      topBarPanel.add(moveDownButon, c);

      c.gridx = 0;
      c.gridy = 1;
      c.gridwidth = 2;
      c.gridheight = 1;
      c.weightx = 1.0;
      c.weighty = 0.0;
      c.anchor = GridBagConstraints.PAGE_END;
      c.fill = GridBagConstraints.BOTH;
      c.insets = new Insets(4, 0, 4, 0);
      topBarPanel.add(iterationsPanel, c);

      topBarPanel.setBackground(ViewConstants.Colors.LOOP_ELEMENT_BACKGROUND_COLOR);
      titleLabel.setBackground(ViewConstants.Colors.LOOP_ELEMENT_BACKGROUND_COLOR);
      iterationsPanel.setBackground(ViewConstants.Colors.LOOP_ELEMENT_BACKGROUND_COLOR);

      topBarPanel.setTransferHandler(new AlwaysInsertBeforeTransferHandler(StandardCounterLoopView.this, containerView));

      // configure the bottom bar area ---------------------------------------------------------------------------------

      final JLabel loopIconLabel = new JLabel(ImageUtils.createImageIcon("/edu/cmu/ri/createlab/sequencebuilder/programelement/view/images/loop-icon.png"));

      final JPanel bottomBarPanel = new JPanel();

      final Component space = SwingUtils.createRigidSpacer(5);

      bottomBarPanel.setLayout(new GridBagLayout());

      c.gridx = 0;
      c.gridy = 0;
      c.gridwidth = 1;
      c.gridheight = 1;
      c.weightx = 1.0;
      c.weighty = 0.0;
      c.insets = new Insets(0, 0, 0, 0);
      c.anchor = GridBagConstraints.PAGE_START;
      c.fill = GridBagConstraints.NONE;
      bottomBarPanel.add(space, c);

      c.gridx = 0;
      c.gridy = 1;
      c.gridwidth = 1;
      c.gridheight = 1;
      c.weightx = 1.0;
      c.weighty = 1.0;
      c.anchor = GridBagConstraints.PAGE_START;
      c.fill = GridBagConstraints.NONE;
      bottomBarPanel.add(loopIconLabel, c);

      bottomBarPanel.setBackground(ViewConstants.Colors.LOOP_ELEMENT_BACKGROUND_COLOR);

      bottomBarPanel.setTransferHandler(new AlwaysInsertAfterTransferHandler(StandardCounterLoopView.this, containerView));

      // configure the container area panel ----------------------------------------------------------------------------

      containerViewPanel = (JPanel)loopContainerView.getComponent();
      containerViewPanel.setMinimumSize(PREFERRED_CONTAINER_DIMENSION);
      containerViewPanel.setMaximumSize(new Dimension(PREFERRED_CONTAINER_DIMENSION.width, containerViewPanel.getMaximumSize().height));

      containerViewPanel.setName("loopFrame");
      resetHighlightContainers();

      // assemble the content panel -----------------------------------------------------------------------------------

      final JPanel contentPanel = getContentPanel();
      final GroupLayout contentPanelLayout = new GroupLayout(contentPanel);
      contentPanel.setLayout(contentPanelLayout);

      contentPanelLayout.setHorizontalGroup(
            contentPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
                  .addComponent(topBarPanel)
                  .addComponent(containerViewPanel)
                  .addComponent(bottomBarPanel)
      );
      contentPanelLayout.setVerticalGroup(
            contentPanelLayout.createSequentialGroup()
                  .addComponent(topBarPanel)
                  .addComponent(containerViewPanel)
                  .addComponent(bottomBarPanel)
      );

      bottomBarPanel.setPreferredSize(new Dimension(containerViewPanel.getPreferredSize().width, bottomBarPanel.getPreferredSize().height));
      bottomBarPanel.setMinimumSize(new Dimension(containerViewPanel.getMinimumSize().width, bottomBarPanel.getMinimumSize().height));
      bottomBarPanel.setMaximumSize(new Dimension(containerViewPanel.getMaximumSize().width, bottomBarPanel.getMaximumSize().height));

      //Background color dependent on container type
      final String panelStyle = containerView.hasParentProgramElementView() ? "loopElementLoop" : "loopElement";
      contentPanel.setName(panelStyle);

      setTransferHandler(
            new ProgramElementDestinationTransferHandler(false)
            {
            @Override
            public Set<DataFlavor> getSupportedDataFlavors()
               {
               return containerView.getSupportedDataFlavors();
               }

            @Override
            protected final void showInsertLocation(final Point dropPoint)
               {
               StandardCounterLoopView.this.showInsertLocation(dropPoint);
               }

            @Override
            protected final void performImport(@NotNull final ProgramElementModel model, @NotNull final Point dropPoint)
               {
               getContainerView().handleDropOfModelOntoView(model,
                                                            StandardCounterLoopView.this,
                                                            isInsertLocationBefore(dropPoint));
               }
            });

      LOG.debug("StandardCounterLoopView.StandardCounterLoopView()");
      }

   public void highlightContainer()
      {
      containerViewPanel.setBorder(selectedBorder);
      }

   public void resetHighlightContainers()
      {
      containerViewPanel.setBorder(unselectedBorder);
      }

   private void setIsIterationCountDisplayMode(final boolean isDisplayMode)
      {
      displayIterationsPanel.setVisible(isDisplayMode);
      editIterationsPanel.setVisible(!isDisplayMode);
      displayModeEditButton.setVisible(isDisplayMode);
      editModeEditButton.setVisible(!isDisplayMode);
      }

   @Override
   protected void hideInsertLocationsOfContainedViews()
      {
      loopContainerView.hideInsertLocationsOfContainedViews();
      }

   @Override
   public final void resetViewForSequenceExecution()
      {
      loopContainerView.resetContainedViewsForSequenceExecution();
      executionEventListener.handleExecutionStart();
      }

   private final class MyExecutionEventListener implements CounterLoopModel.ExecutionEventListener
      {
      private final Runnable handleExecutionStartRunnable =
            new Runnable()
            {
            @Override
            public void run()
               {
               iterationsProgressBar.setValue(iterationsProgressBar.getMinimum());
               resetHighlightContainers();
               }
            };

      private final Runnable handleExecutionEndRunnable =
            new Runnable()
            {
            @Override
            public void run()
               {
               iterationsProgressBar.setValue(iterationsProgressBar.getMaximum());
               resetHighlightContainers();
               }
            };

      @Override
      public void handleExecutionStart()
         {
         SwingUtils.runInGUIThread(handleExecutionStartRunnable);
         }

      @Override
      public void handleHighlight()
         {
         SwingUtils.runInGUIThread(
               new Runnable()
               {
               @Override
               public void run()
                  {

                  highlightContainer();
                  }
               }
         );
         }

      @Override
      public void handleElapsedIterations(final int elapsedIterations)
         {
         SwingUtils.runInGUIThread(
               new Runnable()
               {
               @Override
               public void run()
                  {
                  iterationsProgressBar.setValue(elapsedIterations);
                  highlightContainer();
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
