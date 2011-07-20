package edu.cmu.ri.createlab.sequencebuilder;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.PropertyResourceBundle;
import java.util.concurrent.TimeUnit;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.GroupLayout;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.WindowConstants;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import edu.cmu.ri.createlab.sequencebuilder.programelement.model.CounterLoopModel;
import edu.cmu.ri.createlab.sequencebuilder.programelement.model.ExpressionModel;
import edu.cmu.ri.createlab.sequencebuilder.programelement.model.LoopableConditionalModel;
import edu.cmu.ri.createlab.sequencebuilder.programelement.model.ProgramElementModel;
import edu.cmu.ri.createlab.sequencebuilder.programelement.model.SavedSequenceModel;
import edu.cmu.ri.createlab.sequencebuilder.programelement.view.ViewEventPublisher;
import edu.cmu.ri.createlab.sequencebuilder.programelement.view.dnd.ProgramElementListSourceTransferHandler;
import edu.cmu.ri.createlab.sequencebuilder.programelement.view.listcell.CounterLoopListCellView;
import edu.cmu.ri.createlab.sequencebuilder.programelement.view.listcell.ExpressionListCellView;
import edu.cmu.ri.createlab.sequencebuilder.programelement.view.listcell.LoopableConditionalListCellView;
import edu.cmu.ri.createlab.sequencebuilder.programelement.view.listcell.ProgramElementListCellRenderer;
import edu.cmu.ri.createlab.sequencebuilder.programelement.view.listcell.SavedSequenceListCellView;
import edu.cmu.ri.createlab.sequencebuilder.programelement.view.standard.StandardViewFactory;
import edu.cmu.ri.createlab.terk.TerkConstants;
import edu.cmu.ri.createlab.userinterface.GUIConstants;
import edu.cmu.ri.createlab.userinterface.util.DialogHelper;
import edu.cmu.ri.createlab.userinterface.util.SwingUtils;
import edu.cmu.ri.createlab.util.AbstractDirectoryPollingListModel;
import edu.cmu.ri.createlab.util.DirectoryPoller;
import edu.cmu.ri.createlab.visualprogrammer.VisualProgrammerDevice;
import edu.cmu.ri.createlab.visualprogrammer.lookandfeel.VisualProgrammerLookAndFeelLoader;
import edu.cmu.ri.createlab.xml.LocalEntityResolver;
import edu.cmu.ri.createlab.xml.SaveXmlDocumentDialogRunnable;
import edu.cmu.ri.createlab.xml.XmlFilenameFilter;
import edu.cmu.ri.createlab.xml.XmlHelper;
import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SequenceBuilder
   {
   private static final Logger LOG = Logger.getLogger(SequenceBuilder.class);

   private static final PropertyResourceBundle RESOURCES = (PropertyResourceBundle)PropertyResourceBundle.getBundle(SequenceBuilder.class.getName());

   private static final String APPLICATION_NAME = RESOURCES.getString("application.name");

   public static void main(final String[] args)
      {
      // Load the look and feel
      VisualProgrammerLookAndFeelLoader.getInstance().loadLookAndFeel();

      //Schedule a job for the event-dispatching thread: creating and showing this application's GUI.
      SwingUtilities.invokeLater(
            new Runnable()
            {
            public void run()
               {
               final JFrame jFrame = new JFrame(APPLICATION_NAME);

               final SequenceBuilder sequenceBuilder = new SequenceBuilder(jFrame);

               // set various properties for the JFrame
               jFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
               jFrame.setBackground(Color.WHITE);
               jFrame.setResizable(true);
               jFrame.addWindowListener(
                     new WindowAdapter()
                     {
                     public void windowClosing(final WindowEvent event)
                        {
                        // ask if the user really wants to exit
                        final int selectedOption = JOptionPane.showConfirmDialog(jFrame,
                                                                                 RESOURCES.getString("dialog.message.exit-confirmation"),
                                                                                 RESOURCES.getString("dialog.title.exit-confirmation"),
                                                                                 JOptionPane.YES_NO_OPTION,
                                                                                 JOptionPane.QUESTION_MESSAGE);

                        if (selectedOption == JOptionPane.YES_OPTION)
                           {
                           final SwingWorker<Object, Object> worker =
                                 new SwingWorker<Object, Object>()
                                 {
                                 @Override
                                 protected Object doInBackground() throws Exception
                                    {
                                    sequenceBuilder.shutdown();
                                    return null;
                                    }

                                 @Override
                                 protected void done()
                                    {
                                    System.exit(0);
                                    }
                                 };
                           worker.execute();
                           }
                        }
                     });

               jFrame.pack();
               jFrame.setLocationRelativeTo(null);// center the window on the screen
               jFrame.setVisible(true);
               }
            });
      }

   private final JFrame jFrame;
   private final JPanel mainPanel = new JPanel();
   private final Sequence sequence;

   @NotNull
   private final VisualProgrammerDevice visualProgrammerDevice;
   private final boolean isConnectionBeingManagedElsewhere;
   private final StageControlsView stageControlsView;
   private final FileManagerControlsView fileManagerControlsView;

   public SequenceBuilder(final JFrame jFrame)
      {
      this(jFrame, null);
      }

   public SequenceBuilder(final JFrame jFrame, @Nullable final VisualProgrammerDevice visualProgrammerDevice)
      {
      this.jFrame = jFrame;
      this.isConnectionBeingManagedElsewhere = visualProgrammerDevice != null;
      if (this.isConnectionBeingManagedElsewhere)
         {
         this.visualProgrammerDevice = visualProgrammerDevice;
         }
      else
         {
         this.visualProgrammerDevice = new FakeHummingbirdDevice();
         }

      XmlHelper.setLocalEntityResolver(LocalEntityResolver.getInstance());

      final ContainerModel sequenceContainerModel = new ContainerModel();
      final ContainerView sequenceContainerView = new ContainerView(jFrame, sequenceContainerModel, new StandardViewFactory());
      sequence = new Sequence(sequenceContainerModel, sequenceContainerView);

      // initialize the ViewEventPublisher
      ViewEventPublisher.createInstance(sequenceContainerView);

      // configure drag-and-drop
      final ProgramElementListSourceTransferHandler programElementListSourceTransferHandler = new ProgramElementListSourceTransferHandler();

      // create common view stuff
      final ProgramElementListCellRenderer programElementListCellRenderer = new ProgramElementListCellRenderer();

      // Create the expression source list model
      final ExpressionFileListModel expressionSourceListModel = new ExpressionFileListModel(sequenceContainerView);

      // create the expression source list view
      final JList expressionSourceList = new JList(expressionSourceListModel);
      expressionSourceList.setCellRenderer(programElementListCellRenderer);
      expressionSourceList.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      expressionSourceList.setTransferHandler(programElementListSourceTransferHandler);
      expressionSourceList.setDragEnabled(true);

      // Create the expression source list model
      final SavedSequenceFileListModel savedSequenceSourceListModel = new SavedSequenceFileListModel(sequenceContainerView);

      // create the saved sequence source list view
      final JList savedSequenceSourceList = new JList(savedSequenceSourceListModel);
      savedSequenceSourceList.setCellRenderer(programElementListCellRenderer);
      savedSequenceSourceList.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      savedSequenceSourceList.setTransferHandler(programElementListSourceTransferHandler);
      savedSequenceSourceList.setDragEnabled(true);

      // create the model for the list containing the loop elements
      final DefaultListModel loopElementsListModel = new DefaultListModel();
      loopElementsListModel.addElement(new CounterLoopListCellView(sequenceContainerView, new CounterLoopModel(this.visualProgrammerDevice)));
      loopElementsListModel.addElement(new LoopableConditionalListCellView(sequenceContainerView, new LoopableConditionalModel(this.visualProgrammerDevice)));

      // create the view for the list containing the loop elements
      final JList loopElementsList = new JList(loopElementsListModel);
      loopElementsList.setLayoutOrientation(JList.HORIZONTAL_WRAP);
      loopElementsList.setCellRenderer(programElementListCellRenderer);
      loopElementsList.setVisibleRowCount(-1);
      loopElementsList.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      loopElementsList.setTransferHandler(programElementListSourceTransferHandler);
      loopElementsList.setDragEnabled(true);

      //TODO: This width may need to be widened for the "thread" icon
      loopElementsList.setMinimumSize(new Dimension(170, 60));
      loopElementsList.setPreferredSize(new Dimension(170, 60));




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

      // Create the directory poller which the ExpressionFileListModel will listen to
      final DirectoryPoller expressionDirectoryPoller = new DirectoryPoller(TerkConstants.FilePaths.EXPRESSIONS_DIR,
                                                                            new XmlFilenameFilter(),  // TODO: beef this up to validate expressions
                                                                            1,
                                                                            TimeUnit.SECONDS);
      expressionDirectoryPoller.addEventListener(expressionSourceListModel);
      expressionDirectoryPoller.start();

      // Create the directory poller which the SavedSequenceFileListModel will listen to
      final DirectoryPoller savedSequenceDirectoryPoller = new DirectoryPoller(TerkConstants.FilePaths.SEQUENCES_DIR,
                                                                               new XmlFilenameFilter(),  // TODO: beef this up to validate sequences
                                                                               1,
                                                                               TimeUnit.SECONDS);
      savedSequenceDirectoryPoller.addEventListener(savedSequenceSourceListModel);
      savedSequenceDirectoryPoller.start();




      //Border Creation
      final Border blackline = BorderFactory.createLineBorder(Color.black);
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
      expressionSourceListScrollPane.setPreferredSize(new Dimension(170, 200));
      expressionSourceListScrollPane.setBorder(blackline);

      final JPanel expressionSourceListHolder = new JPanel(new GridBagLayout());

      GridBagConstraints gbc = new GridBagConstraints();

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
      sequenceViewScrollPane.setPreferredSize(new Dimension(800, 600));
      sequenceViewScrollPane.setMinimumSize(new Dimension(300, 300));
      sequenceViewScrollPane.setName("sequenceViewScrollPane");

      final SequenceExecutor sequenceExecutor = new DefaultSequenceExecutor(sequence);

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
                     new SaveXmlDocumentDialogRunnable(document, filename, TerkConstants.FilePaths.SEQUENCES_DIR, jFrame, RESOURCES)
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
                  sequenceExecutor.start();
                  }
               }
            }
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
               setStageButtonsEnabledState();
               }

            @Override
            public void handleRemoveAllEvent()
               {
               setStageButtonsEnabledState();
               }

            private void setStageButtonsEnabledState()
               {
               stageControlsView.setEnabled(!sequenceContainerModel.isEmpty());
               }
            }
      );

      // Create a panel containing the stage and the stage controls
      final JPanel stagePanel = new JPanel();
      final GroupLayout stagePanelLayout = new GroupLayout(stagePanel);
      stagePanel.setLayout(stagePanelLayout);
      stagePanel.setName("mainAppPanel");
      stagePanelLayout.setHorizontalGroup(
            stagePanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                  .addComponent(stageControlsView.getComponent())
                  .addComponent(sequenceViewScrollPane)
      );
      stagePanelLayout.setVerticalGroup(
            stagePanelLayout.createSequentialGroup()
                  .addComponent(stageControlsView.getComponent())
                  .addGap(5, 5, 5)
                  .addComponent(sequenceViewScrollPane)
      );

      fileManagerControlsView = new FileManagerControlsView(jFrame,
                                                            sequence,
                                                            expressionSourceList,
                                                            savedSequenceSourceList,
                                                            new MyFileManagerControlsController());
      // create a panel containing all source elements
      final JPanel expressionSourceElementsPanel = new JPanel();
      expressionSourceElementsPanel.setName("expressionFileManager");

      final GroupLayout expressionSourceElementsPanelLayout = new GroupLayout(expressionSourceElementsPanel);
      expressionSourceElementsPanel.setLayout(expressionSourceElementsPanelLayout);

      expressionSourceElementsPanelLayout.setHorizontalGroup(
            expressionSourceElementsPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER, false)
                  .addComponent(fileManagerControlsView.getComponent())
                  .addComponent(expressionSourceListHolder)
                  .addComponent(savedSequenceSourceListHolder)
                  .addComponent(loopListHolderHolder)
      );
      expressionSourceElementsPanelLayout.setVerticalGroup(
            expressionSourceElementsPanelLayout.createSequentialGroup()
                  .addGap(5, 5, 5)
                  .addComponent(fileManagerControlsView.getComponent())
                  .addComponent(expressionSourceListHolder)
                  .addComponent(savedSequenceSourceListHolder)
                  .addComponent(loopListHolderHolder)
      );

      expressionSourceElementsPanel.setBorder(BorderFactory.createMatteBorder(4, 4, 4, 4, new Color(197,193,235)));

      // handle double-clicks ine the expression and sequence lists
      final MouseListener fileManagerControlsButtonMouseListener = new FileManagerControlsButtonMouseListener();
      expressionSourceList.addMouseListener(fileManagerControlsButtonMouseListener);
      savedSequenceSourceList.addMouseListener(fileManagerControlsButtonMouseListener);

      // configure the main panel
      mainPanel.setLayout(new GridBagLayout());
      mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
      mainPanel.setName("mainAppPanel");

      // add the sub-panels to the main panel
      GridBagConstraints c = new GridBagConstraints();

      c.gridx = 0;
      c.gridy = 0;
      c.gridwidth = 1;
      c.gridheight = 1;
      c.weightx = 1.0;
      c.weighty = 1.0;

      c.anchor = GridBagConstraints.PAGE_START;
      c.fill = GridBagConstraints.BOTH;
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

      if (!isConnectionBeingManagedElsewhere)
         {
         // add the main panel to the JFrame
         jFrame.add(mainPanel);
         }
      }

   public JPanel getPanel()
      {
      return mainPanel;
      }

   public void shutdown()
      {
      LOG.debug("SequenceBuilder.shutdown()");
      }

   private final class FileManagerControlsButtonMouseListener extends MouseAdapter
      {
      public void mouseClicked(final MouseEvent e)
         {
         if (e.getClickCount() == 2)
            {
            fileManagerControlsView.doClickOnAppendExpressionOrOpenSequenceButton();
            }
         }
      }

   private final class ExpressionFileListModel extends AbstractDirectoryPollingListModel<ExpressionListCellView>
      {
      private final ContainerView containerView;

      private ExpressionFileListModel(@NotNull final ContainerView containerView)
         {
         super(
               new Comparator<ExpressionListCellView>()
               {
               @Override
               public int compare(final ExpressionListCellView view1, final ExpressionListCellView view2)
                  {
                  return view1.getProgramElementModel().getExpressionFile().compareTo(view2.getProgramElementModel().getExpressionFile());
                  }
               });
         this.containerView = containerView;
         }

      @Override
      protected ExpressionListCellView createListItemInstance(@NotNull final File file)
         {
         return new ExpressionListCellView(containerView, new ExpressionModel(visualProgrammerDevice, file));
         }
      }

   private final class SavedSequenceFileListModel extends AbstractDirectoryPollingListModel<SavedSequenceListCellView>
      {
      private final ContainerView containerView;

      private SavedSequenceFileListModel(@NotNull final ContainerView containerView)
         {
         super(
               new Comparator<SavedSequenceListCellView>()
               {
               @Override
               public int compare(final SavedSequenceListCellView view1, final SavedSequenceListCellView view2)
                  {
                  return view1.getProgramElementModel().getSavedSequenceFile().compareTo(view2.getProgramElementModel().getSavedSequenceFile());
                  }
               });
         this.containerView = containerView;
         }

      @Override
      protected SavedSequenceListCellView createListItemInstance(@NotNull final File file)
         {
         return new SavedSequenceListCellView(containerView, new SavedSequenceModel(visualProgrammerDevice, file));
         }
      }

   private class MyFileManagerControlsController implements FileManagerControlsController
      {
      @Override
      public void openSequence(@NotNull final SavedSequenceModel model)
         {
         LOG.debug("FileManagerControlsController.openSequence(): " + model);

         final File file = model.getSavedSequenceFile();
         try
            {
            final Document document = XmlHelper.createDocument(file);
            if (document == null)
               {
               throw new Exception("Failed to create an XML document from the file [" + file + "]");
               }
            else
               {
               // clear the existing sequence and then load this sequence
               sequence.load(visualProgrammerDevice, document);

               stageControlsView.setTitle(model.getName());
               }
            }
         catch (IOException e)
            {
            LOG.error("IOException while trying to read the file [" + file + "] as an XML document", e);
            DialogHelper.showErrorMessage(RESOURCES.getString("dialog.title.cannot-open-document"),
                                          RESOURCES.getString("dialog.message.cannot-open-document"),
                                          jFrame);
            }
         catch (JDOMException e)
            {
            LOG.error("JDOMException while trying to read the file [" + file + "] as an XML document", e);
            DialogHelper.showErrorMessage(RESOURCES.getString("dialog.title.cannot-open-document"),
                                          RESOURCES.getString("dialog.message.cannot-open-document"),
                                          jFrame);
            }
         catch (Exception e)
            {
            LOG.error("Exception while trying to read the file [" + file + "] as an XML document", e);
            DialogHelper.showErrorMessage(RESOURCES.getString("dialog.title.cannot-open-document"),
                                          RESOURCES.getString("dialog.message.cannot-open-document"),
                                          jFrame);
            }
         }

      @Override
      public boolean deleteExpression(@NotNull final ExpressionModel model)
         {
         return deleteFile(model.getExpressionFile());
         }

      @Override
      public boolean deleteSequence(@NotNull final SavedSequenceModel model)
         {
         return deleteFile(model.getSavedSequenceFile());
         }

      private boolean deleteFile(@Nullable final File fileToDelete)
         {
         if (LOG.isDebugEnabled())
            {
            LOG.debug("FileManagerControlsController.deleteFile(" + fileToDelete + ")");
            }
         // TODO: handle case where this expression/sequence is being used in the current sequence on the stage

         return fileToDelete != null && fileToDelete.isFile() && fileToDelete.delete();
         }
      }
   }

