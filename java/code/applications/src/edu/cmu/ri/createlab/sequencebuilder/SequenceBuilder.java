package edu.cmu.ri.createlab.sequencebuilder;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.IOException;
import java.util.PropertyResourceBundle;
import java.util.Set;
import java.util.Stack;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import edu.cmu.ri.createlab.expressionbuilder.ExpressionBuilder;
import edu.cmu.ri.createlab.sequencebuilder.programelement.model.CounterLoopModel;
import edu.cmu.ri.createlab.sequencebuilder.programelement.model.ExpressionModel;
import edu.cmu.ri.createlab.sequencebuilder.programelement.model.ForkModel;
import edu.cmu.ri.createlab.sequencebuilder.programelement.model.LinkModel;
import edu.cmu.ri.createlab.sequencebuilder.programelement.model.LoopableConditionalModel;
import edu.cmu.ri.createlab.sequencebuilder.programelement.model.ProgramElementModel;
import edu.cmu.ri.createlab.sequencebuilder.programelement.model.SavedSequenceModel;
import edu.cmu.ri.createlab.sequencebuilder.programelement.view.ViewEventPublisher;
import edu.cmu.ri.createlab.sequencebuilder.programelement.view.dnd.ProgramElementListSourceTransferHandler;
import edu.cmu.ri.createlab.sequencebuilder.programelement.view.listcell.CounterLoopListCellView;
import edu.cmu.ri.createlab.sequencebuilder.programelement.view.listcell.ForkListCellView;
import edu.cmu.ri.createlab.sequencebuilder.programelement.view.listcell.LinkListCellView;
import edu.cmu.ri.createlab.sequencebuilder.programelement.view.listcell.LoopableConditionalListCellView;
import edu.cmu.ri.createlab.sequencebuilder.programelement.view.listcell.ProgramElementListCellRenderer;
import edu.cmu.ri.createlab.sequencebuilder.programelement.view.standard.StandardViewFactory;
import edu.cmu.ri.createlab.terk.expression.manager.ExpressionFile;
import edu.cmu.ri.createlab.userinterface.GUIConstants;
import edu.cmu.ri.createlab.userinterface.util.DialogHelper;
import edu.cmu.ri.createlab.userinterface.util.SwingUtils;
import edu.cmu.ri.createlab.util.FileEventListener;
import edu.cmu.ri.createlab.visualprogrammer.PathManager;
import edu.cmu.ri.createlab.visualprogrammer.VisualProgrammerDevice;
import edu.cmu.ri.createlab.xml.LocalEntityResolver;
import edu.cmu.ri.createlab.xml.SaveXmlDocumentDialogRunnable;
import edu.cmu.ri.createlab.xml.XmlHelper;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SequenceBuilder
   {
   private static final Logger LOG = Logger.getLogger(SequenceBuilder.class);

   private static final PropertyResourceBundle RESOURCES = (PropertyResourceBundle)PropertyResourceBundle.getBundle(SequenceBuilder.class.getName());

   private final JFrame jFrame;

   @NotNull
   private final VisualProgrammerDevice visualProgrammerDevice;

   @NotNull
   private final ExpressionBuilder expressionBuilder;

   private final JPanel mainPanel = new JPanel();
   public final Sequence sequence;

   private final StageControlsView stageControlsView;

   private final FileManagerControlsView fileManagerControlsView;

   private Stack<SequenceAction> actions;
   private SequenceActionListener actionListener;

   public SequenceBuilder(final JFrame jFrame,
                          @NotNull final VisualProgrammerDevice visualProgrammerDevice,
                          @NotNull final ExpressionBuilder expressionBuilder)
      {
      this.jFrame = jFrame;
      this.visualProgrammerDevice = visualProgrammerDevice;
      this.expressionBuilder = expressionBuilder;
      this.actions = new Stack<SequenceAction>();
      this.actionListener = new SequenceActionListener(actions)
         {
         @Override
         public void onAction(final SequenceAction action)
            {
            LOG.info("SequenceActionListener: ActionListener got action: " + action);
            this.actions.push(action);
            stageControlsView.setUndo(true);
            }

         @Override
         public SequenceAction onUndo()
            {
            SequenceAction action = actions.pop();
            if (action == null)
               {
               stageControlsView.setUndo(false);
               return null;
               }
            LOG.debug("SequenceActionListener: ActionListener got undo: " + action);
            ContainerModel parent = action.getLocation().getParent();
            int index = action.getLocation().getIndexInParent();
            LOG.debug("SequenceActionListener: ActionListener got location info: " + action);
            final ProgramElementModel model;
            final Element programElement = action.getData();
            final int delay = action.getDelay();
            final int iterations = action.getIterations();
            LOG.debug("SequenceActionListener: ActionListener got iterations, delay and data: " + action);
            final LoopableConditionalModel.SelectedSensor sensor = action.getSensor();
            LOG.debug("SequenceActionListener: ActionListener got sensor: " + action);
            final Boolean willReevaluate = action.getWillReevaluate();
            LOG.debug("SequenceActionListener: About to create model: " + action);
            if (programElement != null)
               {
               if (ExpressionModel.XML_ELEMENT_NAME.equals(programElement.getName()))
                  {
                  model = ExpressionModel.createFromXmlElement(visualProgrammerDevice, programElement, parent);
                  }
               else if (SavedSequenceModel.XML_ELEMENT_NAME.equals(programElement.getName()))
                  {
                  model = SavedSequenceModel.createFromXmlElement(visualProgrammerDevice, programElement, parent);
                  }
               else if (CounterLoopModel.XML_ELEMENT_NAME.equals(programElement.getName()))
                  {
                  model = CounterLoopModel.createFromXmlElement(visualProgrammerDevice, programElement, parent);
                  }
               else if (LoopableConditionalModel.XML_ELEMENT_NAME.equals(programElement.getName()))
                  {
                  model = LoopableConditionalModel.createFromXmlElement(visualProgrammerDevice, programElement, parent);
                  }
               else if (ForkModel.XML_ELEMENT_NAME.equals(programElement.getName()))
                  {
                  model = ForkModel.createFromXmlElement(visualProgrammerDevice, programElement, parent);
                  }
               else
                  {
                  model = null;
                  }
               }
            else
               {
               model = parent.getAsList().get(index);
               }
            LOG.debug("SequenceActionListener: About to execute undo of: " + action);
            switch (action.getType())
               {
               case ADD:
                  if (parent.removeAtIndex(index) == null)
                     {
                     LOG.warn("Failed to undo a add operation!");
                     }
                  break;
               case REMOVE:
                  if (parent == null || model == null || !parent.insertAtIndex(model, index))
                     {
                     LOG.warn("Failed to undo a remove operation!");
                     }
                  break;
               case UP:
                  parent.moveDownUndo(model);
                  break;
               case DOWN:
                  parent.moveUpUndo(model);
                  break;
               case EXPRESSION_DELAY:
                  if (delay >= 0 && model instanceof ExpressionModel)
                     {
                     ((ExpressionModel)model).undoSetDelayInMillis(delay);
                     }
                  else
                     {
                     LOG.warn("Failed to undo a delay change operation");
                     }
                  break;
               case COUNTER_ITERATIONS:
                  if (iterations >= 0 && model instanceof CounterLoopModel)
                     {
                     ((CounterLoopModel)model).undoSetNumberOfIterations(iterations);
                     }
                  else
                     {
                     LOG.warn("Failed to undo a iterations change operation");
                     }
                  break;
               case SENSOR_SENSOR:
                  if (sensor != null && model instanceof LoopableConditionalModel)
                     {
                     ((LoopableConditionalModel)model).undoSetSelectedSensor(sensor);
                     }
                  else
                     {
                     LOG.warn("Failed to undo a sensor change operation");
                     }
                  break;
               case SENSOR_IF:
                  if (model instanceof LoopableConditionalModel && willReevaluate != null)
                     {
                     ((LoopableConditionalModel)model).undoSetWillReevaluateConditionAfterIfBranchCompletes(willReevaluate);
                     }
                  else
                     {
                     LOG.warn("Failed to undo a sensor change operation");
                     }
                  break;
               case SENSOR_ELSE:
                  if (model instanceof LoopableConditionalModel && willReevaluate != null)
                     {
                     ((LoopableConditionalModel)model).undoSetWillReevaluateConditionAfterElseBranchCompletes(willReevaluate);
                     }
                  else
                     {
                     LOG.warn("Failed to undo a sensor change operation");
                     }
                  break;
               }
            stageControlsView.setUndo(!actions.isEmpty());
            return action;
            }
         };
      XmlHelper.setLocalEntityResolver(LocalEntityResolver.getInstance());

      final ContainerModel sequenceContainerModel = new ContainerModel();
      final ContainerView sequenceContainerView = new ContainerView(jFrame, sequenceContainerModel, new StandardViewFactory());
      sequence = new Sequence(sequenceContainerModel, sequenceContainerView);
      sequence.setActionListener(this.actionListener);
      // initialize the ViewEventPublisher
      ViewEventPublisher.createInstance(sequenceContainerView);

      // configure drag-and-drop
      final ProgramElementListSourceTransferHandler programElementListSourceTransferHandler = new ProgramElementListSourceTransferHandler();

      // create common view stuff
      final ProgramElementListCellRenderer programElementListCellRenderer = new ProgramElementListCellRenderer();

      // Create the expression source list model and register it as a listener to the PathManager's expressions DirectoryPoller
      final ExpressionListModel expressionSourceListModel = new ExpressionListModel(sequenceContainerView, this.visualProgrammerDevice);
      PathManager.getInstance().registerExpressionsFileEventListener(expressionSourceListModel);

      // Register another listener to the PathManager's expressions DirectoryPoller which will kick the sequence
      // whenever an expression is modified or deleted.  We need to do this so that the sequence can update its
      // model and UI appropriately

      //??--->
      PathManager.getInstance().registerExpressionsFileEventListener(
            new FileEventListener()
               {
               @Override
               public void handleNewFileEvent(@NotNull final Set<String> files)
                  {

                  }

               @Override
               public void handleModifiedFileEvent(@NotNull final Set<String> files)
                  {

                  }

               @Override
               public void handleDeletedFileEvent(@NotNull final Set<String> files)
                  {

                  }
               });

      //*** Remember to change the name!
      PathManager.getInstance().registerSequencesDirectoryPollerEventListener(
            new FileEventListener()
               {

               @Override
               public void handleNewFileEvent(@NotNull final Set<String> files)
                  {
                  // nothing to do
                  }

               @Override
               public void handleModifiedFileEvent(@NotNull final Set<String> files)
                  {
                  if (LOG.isDebugEnabled())
                     {
                     LOG.debug("SequenceBuilder.handleModifiedFileEvent(): " + files.size() + " modified expression(s):");
                     if (!files.isEmpty())
                        {
                        for (final String file : files)
                           {
                           LOG.debug("   " + file);
                           }
                        }
                     }

                  // kick the sequence so it knows to update its model and UI
                  sequence.refresh();
                  }

               @Override
               public void handleDeletedFileEvent(@NotNull final Set<String> files)
                  {
                  LOG.debug("SequenceBuilder.handleDeletedFileEvent(): " + files.size() + " deleted expression(s)");
                  // TODO: kick the sequence so it knows to update its model and UI
                  }
               });

      // create the expression source list view
      final JList expressionSourceList = new JList(expressionSourceListModel);
      expressionSourceList.setCellRenderer(programElementListCellRenderer);
      expressionSourceList.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      expressionSourceList.setTransferHandler(programElementListSourceTransferHandler);
      expressionSourceList.setDragEnabled(true);

      // Create the sequence source list model and register it as a listener to the PathManager's sequences DirectoryPoller
      final SavedSequenceListModel savedSequenceSourceListModel = new SavedSequenceListModel(sequenceContainerView, this.visualProgrammerDevice);
      PathManager.getInstance().registerSequencesDirectoryPollerEventListener(savedSequenceSourceListModel);

      // create the saved sequence source list view
      final JList savedSequenceSourceList = new JList(savedSequenceSourceListModel);
      savedSequenceSourceList.setCellRenderer(programElementListCellRenderer);
      savedSequenceSourceList.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      savedSequenceSourceList.setTransferHandler(programElementListSourceTransferHandler);
      savedSequenceSourceList.setDragEnabled(true);

      // create the model for the list containing the loop elements
      final DefaultListModel loopElementsListModel = new DefaultListModel();
      loopElementsListModel.addElement(new CounterLoopListCellView(sequenceContainerView, new CounterLoopModel(this.visualProgrammerDevice, sequence.getContainerModel())));
      loopElementsListModel.addElement(new ForkListCellView(sequenceContainerView, new ForkModel(this.visualProgrammerDevice, sequence.getContainerModel())));
      loopElementsListModel.addElement(new LinkListCellView(sequenceContainerView, new LinkModel(this.visualProgrammerDevice, sequence.getContainerModel())));
      loopElementsListModel.addElement(new LoopableConditionalListCellView(sequenceContainerView, new LoopableConditionalModel(this.visualProgrammerDevice, sequence.getContainerModel())));

      // create the view for the list containing the loop elements
      final JList loopElementsList = new JList(loopElementsListModel);
      loopElementsList.setLayoutOrientation(JList.HORIZONTAL_WRAP);
      loopElementsList.setCellRenderer(programElementListCellRenderer);
      loopElementsList.setVisibleRowCount(-1);
      loopElementsList.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      loopElementsList.setTransferHandler(programElementListSourceTransferHandler);
      loopElementsList.setDragEnabled(true);
      loopElementsList.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

      loopElementsList.setMinimumSize(new Dimension(250, 60));
      loopElementsList.setPreferredSize(new Dimension(250, 60));

      // add selection listeners which ensure that only one item between the two lists is ever selected
      expressionSourceList.addListSelectionListener(
            new ListSelectionListener()
               {
               @Override
               public void valueChanged(final ListSelectionEvent listSelectionEvent)
                  {
                  if (!expressionSourceList.isSelectionEmpty())
                     {
                     savedSequenceSourceList.clearSelection();
                     loopElementsList.clearSelection();
                     }
                  }
               }
      );
      savedSequenceSourceList.addListSelectionListener(
            new ListSelectionListener()
               {
               @Override
               public void valueChanged(final ListSelectionEvent listSelectionEvent)
                  {
                  if (!savedSequenceSourceList.isSelectionEmpty())
                     {
                     expressionSourceList.clearSelection();
                     loopElementsList.clearSelection();
                     }
                  }
               }
      );

      loopElementsList.addListSelectionListener(
            new ListSelectionListener()
               {
               @Override
               public void valueChanged(final ListSelectionEvent listSelectionEvent)
                  {
                  if (!loopElementsList.isSelectionEmpty())
                     {
                     expressionSourceList.clearSelection();
                     savedSequenceSourceList.clearSelection();
                     }
                  }
               }
      );

      //Border Creation
      final Border blackline = BorderFactory.createLineBorder(Color.gray);
      final Border empty = BorderFactory.createEmptyBorder();

      final TitledBorder expBorder = BorderFactory.createTitledBorder(empty, "Expressions");
      final TitledBorder seqBorder = BorderFactory.createTitledBorder(empty, "Sequences");
      final TitledBorder loopBorder = BorderFactory.createTitledBorder(empty, "Structures");

      expBorder.setTitleFont(GUIConstants.FONT_NORMAL);
      expBorder.setTitleColor(Color.BLACK);
      seqBorder.setTitleFont(GUIConstants.FONT_NORMAL);
      seqBorder.setTitleColor(Color.BLACK);
      loopBorder.setTitleFont(GUIConstants.FONT_NORMAL);
      loopBorder.setTitleColor(Color.BLACK);

      //loopElementsList.setBorder(blackline);

      // Create the expression source area scroll pane
      final JScrollPane expressionSourceListScrollPane = new JScrollPane(expressionSourceList);
      expressionSourceListScrollPane.getVerticalScrollBar().setUnitIncrement(20);
      expressionSourceListScrollPane.setPreferredSize(new Dimension(170, 200));
      expressionSourceListScrollPane.setBorder(blackline);

      final JPanel expressionSourceListHolder = new JPanel(new GridBagLayout());

      final GridBagConstraints gbc = new GridBagConstraints();

      gbc.fill = GridBagConstraints.BOTH;
      gbc.gridx = 0;
      gbc.gridy = 0;
      gbc.weighty = 1.0;
      gbc.weightx = 1.0;
      gbc.anchor = GridBagConstraints.CENTER;

      expressionSourceListHolder.add(expressionSourceListScrollPane, gbc);
      expressionSourceListHolder.setBorder(expBorder);

      final JPanel loopListHolder = new JPanel(new GridBagLayout());
      gbc.fill = GridBagConstraints.NONE;
      loopListHolder.add(loopElementsList, gbc);
      loopListHolder.setBorder(blackline);

      final JPanel loopListHolderHolder = new JPanel(new GridBagLayout());
      gbc.fill = GridBagConstraints.BOTH;
      loopListHolderHolder.add(loopListHolder, gbc);
      loopListHolderHolder.setBorder(loopBorder);

      // Create the saved sequence source area scroll pane
      final JScrollPane savedSequenceSourceListScrollPane = new JScrollPane(savedSequenceSourceList);
      savedSequenceSourceListScrollPane.setPreferredSize(new Dimension(170, 200));
      savedSequenceSourceListScrollPane.getVerticalScrollBar().setUnitIncrement(20);
      savedSequenceSourceListScrollPane.setBorder(blackline);

      final JPanel savedSequenceSourceListHolder = new JPanel(new GridBagLayout());

      savedSequenceSourceListHolder.add(savedSequenceSourceListScrollPane, gbc);
      savedSequenceSourceListHolder.setBorder(seqBorder);

      savedSequenceSourceListHolder.setName("expressionFileManager");
      expressionSourceListHolder.setName("expressionFileManager");
      loopListHolder.setName("expressionFileManager");
      loopListHolderHolder.setName("expressionFileManager");

      // Create the sequence stage area
      final JScrollPane sequenceViewScrollPane = new JScrollPane(sequence.getContainerView().getComponent());
      // sequenceViewScrollPane.setPreferredSize(new Dimension(800, 600));
      // sequenceViewScrollPane.setMinimumSize(new Dimension(300, 300));
      sequenceViewScrollPane.getVerticalScrollBar().setUnitIncrement(20);
      sequenceViewScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
      sequenceViewScrollPane.setName("sequenceViewScrollPane");
      //sequenceViewScrollPane.setAutoscrolls(true);

      sequenceContainerView.setScrollPaneParent(sequenceViewScrollPane);

      final IndicatorLayeredPane sequenceScrollPaneIndicated = new IndicatorLayeredPane(sequenceViewScrollPane);

      sequenceContainerView.setScrollPaneIndicators(sequenceScrollPaneIndicated);

      final SequenceExecutor sequenceExecutor = SequenceExecutor.getInstance();

      fileManagerControlsView = new FileManagerControlsView(jFrame,
                                                            sequence,
                                                            expressionSourceList,
                                                            savedSequenceSourceList,
                                                            savedSequenceSourceListModel,
                                                            programElementListCellRenderer,
                                                            new MyFileManagerControlsController(),
                                                            visualProgrammerDevice.getExportableLanguages());

      // Create the stage controls
      stageControlsView = new StageControlsView(
            jFrame,
            sequence,
            new StageControlsController()
               {
               @Override
               public void clearStage()
                  {
                  sequence.clear();
                  }

               @Override
               public void saveSequence(@Nullable final String filename, @Nullable final SaveXmlDocumentDialogRunnable.EventHandler eventHandler)
                  {
                  final Document document = sequence.toXmlDocument();
                  final SaveXmlDocumentDialogRunnable runnable =
                        new SaveXmlDocumentDialogRunnable(document,
                                                          filename,
                                                          //-->                                                  //PathManager.SEQUENCES_DIRECTORY_FILE_PROVIDER,
                                                          PathManager.getInstance().getSequencesZipSave(),
                                                          jFrame,
                                                          RESOURCES)
                           {
                           @Override
                           protected void performUponSuccessfulSave(final String savedFilenameWithoutExtension)
                              {
                              if (eventHandler != null)
                                 {
                                 eventHandler.handleSuccessfulSave(savedFilenameWithoutExtension);
                                 }
                              }
                           };
                  SwingUtils.runInGUIThread(runnable);
                  }

               @Override
               public void startOrStopSequenceExecution()
                  {
                  LOG.debug("SequenceBuilder.startOrStopSequenceExecution()");
                  if (sequenceExecutor.isRunning())
                     {
                     sequenceExecutor.stop();
                     }
                  else
                     {
                     sequenceExecutor.start(sequence);
                     }
                  }

               @Override
               public void setWillLoopPlayback(final boolean willLoopPlayback)
                  {
                  sequenceExecutor.setWillLoopPlayback(willLoopPlayback);
                  }

               @Override
               public void undo()
                  {
                  actionListener.onUndo();
                  }
               },
            fileManagerControlsView
      );

      // register the stageControlsView as a listener to the sequenceExecutor so it can toggle the text of the Play/Stop button
      sequenceExecutor.addEventListener(stageControlsView);

      // Add a listener to the main container model so we can enable/disable the stage buttons
      sequenceContainerModel.addEventListener(
            new ContainerModel.EventListener()
               {
               @Override
               public void handleElementAddedEvent(@NotNull final ProgramElementModel model)
                  {
                  setStageButtonsEnabledState();
                  }

               @Override
               public void handleElementRemovedEvent(@NotNull final ProgramElementModel model)
                  {
                  makeSurePlaybackIsStopped();

                  // now set the stage buttons' enabled state accordingly
                  setStageButtonsEnabledState();
                  }

               @Override
               public void handleRemoveAllEvent()
                  {
                  makeSurePlaybackIsStopped();

                  setStageButtonsEnabledState();
                  }

               public void handleResetAllProgressBarsForExecution()
                  {
                  //Todo: Does this need to do something?
                  LOG.debug("handleResetAllProgressBarsForExecution called in SequenceBuilder. This should not happen.");
                  }

               private void makeSurePlaybackIsStopped()
                  {
                  // if the stage is now empty because the last element was just removed, then
                  // we need to automatically stop playback
                  if (sequenceContainerModel.isEmpty() && sequenceExecutor.isRunning())
                     {
                     LOG.debug("SequenceBuilder.makeSurePlaybackIsStopped(): Automatically stopping playback because the stage is now empty. ");
                     sequenceExecutor.stop();
                     }
                  }

               private void setStageButtonsEnabledState()
                  {
                  stageControlsView.setEnabled(!sequenceContainerModel.isEmpty());
                  }
               }
      );

      // Create a panel containing the stage and the stage controls
      final JPanel stagePanel = new JPanel();

      stagePanel.setLayout(new GridBagLayout());
      //GroupLayout stagePanelLayout = new GroupLayout(stagePanel);
      // stagePanel.setLayout(stagePanelLayout);
      stagePanel.setName("mainAppPanel");

      /*stagePanelLayout.setHorizontalGroup(
            stagePanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                  .addComponent(stageControlsView.getComponent())
                  .addComponent(sequenceScrollPaneIndicated)
      );
      stagePanelLayout.setVerticalGroup(
            stagePanelLayout.createSequentialGroup()
                  .addComponent(stageControlsView.getComponent())
                  .addComponent(sequenceScrollPaneIndicated)
      );*/

      final GridBagConstraints c = new GridBagConstraints();

      c.fill = GridBagConstraints.HORIZONTAL;
      c.gridwidth = 1;
      c.gridheight = 1;
      c.gridx = 0;
      c.gridy = 0;
      c.weighty = 0.0;
      c.weightx = 1.0;
      c.anchor = GridBagConstraints.CENTER;
      c.insets = new Insets(0, 0, 0, 0);
      stagePanel.add(stageControlsView.getComponent(), c);

      c.fill = GridBagConstraints.BOTH;
      c.gridwidth = 1;
      c.gridheight = 1;
      c.gridx = 0;
      c.gridy = 1;
      c.weighty = 1.0;
      c.weightx = 1.0;
      c.anchor = GridBagConstraints.CENTER;
      c.insets = new Insets(5, 0, 0, 0);
      stagePanel.add(sequenceScrollPaneIndicated, c);

      // create a panel containing all source elements
      final JPanel expressionSourceElementsPanel = new JPanel();
      expressionSourceElementsPanel.setName("expressionFileManager");

      //final GroupLayout expressionSourceElementsPanelLayout = new GroupLayout(expressionSourceElementsPanel);
      expressionSourceElementsPanel.setLayout(new GridBagLayout());

      gbc.fill = GridBagConstraints.BOTH;
      gbc.gridx = 0;
      gbc.gridy = 0;
      gbc.weighty = 0.5;
      gbc.weightx = 1.0;
      gbc.anchor = GridBagConstraints.PAGE_START;
      gbc.insets = new Insets(5, 0, 0, 0);
      expressionSourceElementsPanel.add(expressionSourceListHolder, gbc);

      gbc.fill = GridBagConstraints.BOTH;
      gbc.gridx = 0;
      gbc.gridy = 1;
      gbc.weighty = 0.5;
      gbc.weightx = 1.0;
      gbc.anchor = GridBagConstraints.PAGE_START;
      gbc.insets = new Insets(0, 0, 0, 0);
      expressionSourceElementsPanel.add(savedSequenceSourceListHolder, gbc);

      gbc.fill = GridBagConstraints.HORIZONTAL;
      gbc.gridx = 0;
      gbc.gridy = 2;
      gbc.weighty = 0.0;
      gbc.weightx = 1.0;
      gbc.anchor = GridBagConstraints.PAGE_START;
      gbc.insets = new Insets(0, 5, 5, 5);
      expressionSourceElementsPanel.add(fileManagerControlsView.getComponent(), gbc);

      gbc.fill = GridBagConstraints.BOTH;
      gbc.gridx = 0;
      gbc.gridy = 3;
      gbc.weighty = 0.0;
      gbc.weightx = 1.0;
      gbc.anchor = GridBagConstraints.PAGE_START;
      gbc.insets = new Insets(5, 0, 5, 0);
      expressionSourceElementsPanel.add(loopListHolderHolder, gbc);

      expressionSourceElementsPanel.setBorder(BorderFactory.createMatteBorder(4, 4, 4, 4, new Color(197, 193, 235)));

      // configure the main panel
      mainPanel.setLayout(new GridBagLayout());
      mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
      mainPanel.setName("mainAppPanel");

      // add the sub-panels to the main panel
      // final GridBagConstraints c = new GridBagConstraints();

      c.gridx = 0;
      c.gridy = 0;
      c.gridwidth = 1;
      c.gridheight = 1;
      c.weightx = 1.0;
      c.weighty = 1.0;
      c.anchor = GridBagConstraints.PAGE_START;
      c.fill = GridBagConstraints.BOTH;
      c.insets = new Insets(0, 0, 0, 0);

      mainPanel.add(stagePanel, c);

      c.gridx = 1;
      c.gridy = 0;
      c.gridwidth = 1;
      c.gridheight = 1;
      c.weightx = 0.0;
      c.weighty = 1.0;
      c.insets = new Insets(0, 5, 0, 0);
      c.anchor = GridBagConstraints.PAGE_START;
      c.fill = GridBagConstraints.BOTH;
      mainPanel.add(expressionSourceElementsPanel, c);

      PathManager.getInstance().forceExpressionsDirectoryPollerRefresh();
      PathManager.getInstance().forceSequencesDirectoryPollerRefresh();

      mainPanel.addComponentListener(
            new ComponentAdapter()
               {
               @Override
               public void componentResized(final ComponentEvent e)
                  {
                  //To change body of implemented methods use File | Settings | File Templates.
                  LOG.debug("Resize of Sequence Builder");
                  sequenceScrollPaneIndicated.alignIndicators();
                  //sequenceScrollPaneIndicated.repaint();
                  }
               });
      }

   public void performPostDisconnectCleanup()
      {
      final SequenceExecutor sequenceExecutor = SequenceExecutor.getInstance();

      stageControlsView.setEnabled(false);
      sequenceExecutor.destroyInstance();
      }

   public JPanel getPanel()
      {
      return mainPanel;
      }

   public void shutdown()
      {
      LOG.debug("SequenceBuilder.shutdown()");
      }

   public void setExport(boolean enabled)
      {
      fileManagerControlsView.setExport(enabled);
      }

   private class MyFileManagerControlsController implements FileManagerControlsController
      {
      @Override
      public void openExpression(@NotNull final ExpressionFile expressionFile)
         {
         expressionBuilder.openExpression(expressionFile);
         }

      @Override
      public void openSequence(@NotNull final SavedSequenceModel model)
         {
         actions.clear();
         LOG.debug("FileManagerControlsController.openSequence(): " + model);
         try
            {
            final Document document = XmlHelper.createDocument(PathManager.getInstance().getSequencesZipSave().getFile_InputStream(model.getName() + ".xml"));
            if (document == null)
               {
               // throw new Exception("Failed to create an XML document from the file [" + file + "]");
               throw new Exception("Failed to create an XML document from the file [" + model.getName() + "]");
               }
            else
               {
               // clear the existing sequence and then load this sequence
               sequence.load(visualProgrammerDevice, document);

               stageControlsView.setTitle(model.getName());
               }
            }
         catch (final IOException e)
            {
            LOG.error("IOException while trying to read the file [" + model.getName() + "] as an XML document", e);
            DialogHelper.showErrorMessage(RESOURCES.getString("dialog.title.cannot-open-document"),
                                          RESOURCES.getString("dialog.message.cannot-open-document"),
                                          jFrame);
            }
         catch (final JDOMException e)
            {
            LOG.error("JDOMException while trying to read the file [" + model.getName() + "] as an XML document", e);
            DialogHelper.showErrorMessage(RESOURCES.getString("dialog.title.cannot-open-document"),
                                          RESOURCES.getString("dialog.message.cannot-open-document"),
                                          jFrame);
            }
         catch (final Exception e)
            {
            LOG.error("Exception while trying to read the file [" + model.getName() + "] as an XML document", e);
            DialogHelper.showErrorMessage(RESOURCES.getString("dialog.title.cannot-open-document"),
                                          RESOURCES.getString("dialog.message.cannot-open-document"),
                                          jFrame);
            }
         }

      @Override
      public boolean deleteExpression(@NotNull final ExpressionModel model)
         {
         PathManager.getInstance().getExpressionsZipSave().deleteFile(model.getExpressionFileName());
         return !PathManager.getInstance().getExpressionsZipSave().exist(model.getExpressionFileName());
         }

      @Override
      public boolean deleteSequence(@NotNull final SavedSequenceModel model)
         {
         PathManager.getInstance().getSequencesZipSave().deleteFile(model.getSavedSequenceFileName());
         return !PathManager.getInstance().getSequencesZipSave().exist(model.getSavedSequenceFileName());
         }

    /*  private boolean deleteFile(@Nullable final File fileToDelete)
         {
         if (LOG.isDebugEnabled())
            {
            LOG.debug("FileManagerControlsController.deleteFile(" + fileToDelete + ")");
            }
         // TODO: handle case where this expression/sequence is being used in the current sequence on the stage


         return fileToDelete != null && fileToDelete.isFile() && fileToDelete.delete();
         }*/
      }
   }

