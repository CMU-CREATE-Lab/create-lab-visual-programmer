package edu.cmu.ri.createlab.sequencebuilder;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.Comparator;
import java.util.PropertyResourceBundle;
import java.util.concurrent.TimeUnit;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.WindowConstants;
import edu.cmu.ri.createlab.sequencebuilder.programelement.model.CounterLoopModel;
import edu.cmu.ri.createlab.sequencebuilder.programelement.model.LoopableConditionalModel;
import edu.cmu.ri.createlab.sequencebuilder.programelement.view.dnd.ProgramElementListSourceTransferHandler;
import edu.cmu.ri.createlab.sequencebuilder.programelement.view.listcell.CounterLoopListCellView;
import edu.cmu.ri.createlab.sequencebuilder.programelement.view.listcell.ExpressionListCellView;
import edu.cmu.ri.createlab.sequencebuilder.programelement.view.listcell.LoopableConditionalListCellView;
import edu.cmu.ri.createlab.sequencebuilder.programelement.view.listcell.ProgramElementListCellRenderer;
import edu.cmu.ri.createlab.sequencebuilder.programelement.view.listcell.SavedSequenceListCellView;
import edu.cmu.ri.createlab.sequencebuilder.programelement.view.standard.StandardViewFactory;
import edu.cmu.ri.createlab.terk.TerkConstants;
import edu.cmu.ri.createlab.util.AbstractDirectoryPollingListModel;
import edu.cmu.ri.createlab.util.DirectoryPoller;
import edu.cmu.ri.createlab.visualprogrammer.lookandfeel.VisualProgrammerLookAndFeelLoader;
import edu.cmu.ri.createlab.xml.LocalEntityResolver;
import edu.cmu.ri.createlab.xml.XmlFilenameFilter;
import edu.cmu.ri.createlab.xml.XmlHelper;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

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

   public SequenceBuilder(final JFrame jFrame)
      {
      XmlHelper.setLocalEntityResolver(LocalEntityResolver.getInstance());

      final ContainerModel sequenceContainerModel = new ContainerModel();
      final ContainerView sequenceContainerView = new ContainerView(jFrame, sequenceContainerModel, new StandardViewFactory());
      final Sequence sequence = new Sequence(sequenceContainerModel, sequenceContainerView);

      // configure drag-and-drop
      final ProgramElementListSourceTransferHandler programElementListSourceTransferHandler = new ProgramElementListSourceTransferHandler();

      // create common view stuff
      final ProgramElementListCellRenderer programElementListCellRenderer = new ProgramElementListCellRenderer();

      // Create the expression source list model
      final ExpressionFileListModel expressionSourceListModel = new ExpressionFileListModel(sequenceContainerView);

      // create the expression source list view
      final JList expressionSourceList = new JList(expressionSourceListModel);
      expressionSourceList.setCellRenderer(programElementListCellRenderer);
      expressionSourceList.setVisibleRowCount(-1);
      expressionSourceList.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      expressionSourceList.setTransferHandler(programElementListSourceTransferHandler);
      expressionSourceList.setDragEnabled(true);

      // Create the expression source list model
      final SavedSequenceFileListModel savedSequenceSourceListModel = new SavedSequenceFileListModel(sequenceContainerView);

      // create the saved sequence source list view
      final JList savedSequenceSourceList = new JList(savedSequenceSourceListModel);
      savedSequenceSourceList.setCellRenderer(programElementListCellRenderer);
      savedSequenceSourceList.setVisibleRowCount(-1);
      savedSequenceSourceList.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      savedSequenceSourceList.setTransferHandler(programElementListSourceTransferHandler);
      savedSequenceSourceList.setDragEnabled(true);

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

      // create the model for the list containing the loop elements
      final DefaultListModel loopElementsListModel = new DefaultListModel();
      loopElementsListModel.addElement(new CounterLoopListCellView(sequenceContainerView, new CounterLoopModel()));
      loopElementsListModel.addElement(new LoopableConditionalListCellView(sequenceContainerView, new LoopableConditionalModel()));

      // create the view for the list containing the loop elements
      final JList loopElementsList = new JList(loopElementsListModel);
      loopElementsList.setLayoutOrientation(JList.HORIZONTAL_WRAP);
      loopElementsList.setCellRenderer(programElementListCellRenderer);
      loopElementsList.setVisibleRowCount(-1);
      loopElementsList.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      loopElementsList.setTransferHandler(programElementListSourceTransferHandler);
      loopElementsList.setDragEnabled(true);

      // Create the expression source area scroll pane
      final JScrollPane expressionSourceListScrollPane = new JScrollPane(expressionSourceList);
      expressionSourceListScrollPane.setPreferredSize(new Dimension(300, 300));

      // Create the saved sequence source area scroll pane
      final JScrollPane savedSequenceSourceListScrollPane = new JScrollPane(savedSequenceSourceList);
      savedSequenceSourceListScrollPane.setPreferredSize(new Dimension(300, 300));

      // Create the destination area
      final JScrollPane sequenceViewScrollPane = new JScrollPane(sequence.getContainerView().getComponent());
      sequenceViewScrollPane.setPreferredSize(new Dimension(800, 600));
      sequenceViewScrollPane.setName("sequenceViewScrollPane");

      // create a panel containing all source elements
      final JPanel expressionSourceElementsPanel = new JPanel();
      expressionSourceElementsPanel.setLayout(new BoxLayout(expressionSourceElementsPanel, BoxLayout.Y_AXIS));
      expressionSourceElementsPanel.add(expressionSourceListScrollPane);
      expressionSourceElementsPanel.add(savedSequenceSourceListScrollPane);
      expressionSourceElementsPanel.add(loopElementsList);

      // create the main panel
      final JPanel mainPanel = new JPanel();

      mainPanel.setLayout(new GridBagLayout());
      mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

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
      mainPanel.add(sequenceViewScrollPane, c);

      c.gridx = 1;
      c.gridy = 0;
      c.gridwidth = 1;
      c.gridheight = 1;
      c.weightx = 0.0;
      c.weighty = 1.0;
      c.anchor = GridBagConstraints.PAGE_START;
      c.fill = GridBagConstraints.BOTH;
      mainPanel.add(expressionSourceElementsPanel, c);

      /*mainPanel.add(sequenceViewScrollPane);
      mainPanel.add(expressionSourceElementsPanel);*/

      // add the main panel to the JFrame
      jFrame.add(mainPanel);
      }

   public void shutdown()
      {
      LOG.debug("SequenceBuilder.shutdown()");
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
         return new ExpressionListCellView(containerView, file);
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
         return new SavedSequenceListCellView(containerView, file);
         }
      }
   }

