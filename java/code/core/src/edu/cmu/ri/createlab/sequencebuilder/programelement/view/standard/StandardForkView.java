package edu.cmu.ri.createlab.sequencebuilder.programelement.view.standard;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.util.PropertyResourceBundle;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;
import edu.cmu.ri.createlab.sequencebuilder.ContainerView;
import edu.cmu.ri.createlab.sequencebuilder.programelement.model.ForkModel;
import edu.cmu.ri.createlab.sequencebuilder.programelement.model.ProgramElementModel;
import edu.cmu.ri.createlab.sequencebuilder.programelement.view.ViewConstants;
import edu.cmu.ri.createlab.sequencebuilder.programelement.view.dnd.AlwaysInsertAfterTransferHandler;
import edu.cmu.ri.createlab.sequencebuilder.programelement.view.dnd.AlwaysInsertBeforeTransferHandler;
import edu.cmu.ri.createlab.sequencebuilder.programelement.view.dnd.ProgramElementDestinationTransferHandler;
import edu.cmu.ri.createlab.userinterface.util.ImageUtils;
import edu.cmu.ri.createlab.userinterface.util.SwingUtils;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

/**
 * @author Brandon Price (baprice@andrew.cmu.edu)
 */
public class StandardForkView extends BaseStandardProgramElementView<ForkModel>
   {
   private static final Logger LOG = Logger.getLogger(StandardForkView.class);

   private static final PropertyResourceBundle RESOURCES = (PropertyResourceBundle)PropertyResourceBundle.getBundle(StandardForkView.class.getName());
   private static final Dimension PREFERRED_CONTAINER_DIMENSION = new Dimension(196, 220);

   private final ForkModel ForkModel;
   private final ContainerView thread1ContainerView;
   private final ContainerView thread2ContainerView;

   private final ImageIcon greenArrow = ImageUtils.createImageIcon("/edu/cmu/ri/createlab/sequencebuilder/programelement/view/images/greenArrow.png");
   private final ImageIcon wideOrangeArrow = ImageUtils.createImageIcon("/edu/cmu/ri/createlab/sequencebuilder/programelement/view/images/wideOrangeArrow.png");
   private final Border arrowBorder = BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(0, 128, 0), 3), BorderFactory.createMatteBorder(16, 0, 0, 0, greenArrow));
   private final Border selectedBorder = BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.BLACK, 1), arrowBorder);
   private final Border orangeArrowBorder = BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3), BorderFactory.createMatteBorder(16, 0, 0, 0, wideOrangeArrow));
   private final Border unselectedBorder = BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.BLACK, 1), orangeArrowBorder);

   private final JComponent thread1ContainerViewPanel;
   private final JComponent thread2ContainerViewPanel;

   private final Runnable highlightThread1Runnable =
         new Runnable()
            {
            @Override
            public void run()
               {
               thread1ContainerViewPanel.setBorder(selectedBorder);
               thread2ContainerViewPanel.setBorder(unselectedBorder);
               }
            };

   private final Runnable highlightThread2Runnable =
         new Runnable()
            {
            @Override
            public void run()
               {
               thread1ContainerViewPanel.setBorder(unselectedBorder);
               thread2ContainerViewPanel.setBorder(selectedBorder);
               }
            };

   private final Runnable resetHighlightContainersRunnable =
         new Runnable()
            {
            @Override
            public void run()
               {
               thread1ContainerViewPanel.setBorder(unselectedBorder);
               thread2ContainerViewPanel.setBorder(unselectedBorder);
               }
            };

   public StandardForkView(@NotNull final ContainerView containerView, @NotNull final ForkModel model)
      {
      super(containerView, model);
      this.ForkModel = model;

      thread1ContainerView = new ContainerView(containerView.getJFrame(), model.getThread1ContainerModel(), new StandardViewFactory(), this);
      thread2ContainerView = new ContainerView(containerView.getJFrame(), model.getThread2ContainerModel(), new StandardViewFactory(), this);

      // need this for case where the model was loaded from XML, so the contained views haven't been created yet--this forces their creation and display
      thread1ContainerView.refresh();
      thread2ContainerView.refresh();

      // configure the top bar area ------------------------------------------------------------------------------------

      final JPanel topBarPanel = new JPanel();
      final JLabel titleLabel = new JLabel(RESOURCES.getString("title.label"));
      titleLabel.setIcon(ImageUtils.createImageIcon("/edu/cmu/ri/createlab/sequencebuilder/programelement/view/images/Sensor.png"));

      titleLabel.setName("forkBlockTitle");

      final JButton deleteButton = getDeleteButton();
      final JButton moveUpButton = getMoveUpButton();
      final JButton moveDownButon = getMoveDownButton();

      model.addExecutionEventListener(
            new ForkModel.ExecutionEventListener()
               {
               @Override
               public void handleExecutionStart()
                  {
                  LOG.debug("StandardForkView.handleExecutionStart()");
                  resetHighlightContainers();
                  }

               @Override
               public void handleExecutionEnd()
                  {
                  LOG.debug("StandardForkView.handleExecutionEnd()");
                  resetHighlightContainers();
                  }

               @Override
               public void handleThread1Highlight()
                  {
                  highlightThread1Container();
                  }

               @Override
               public void handleThread2Highlight()
                  {
                  highlightThread2Container();
                  }

               @Override
               public void handleResetHightlight()
                  {
                  resetHighlightContainers();
                  }
               }

      );

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


      topBarPanel.setBackground(ViewConstants.Colors.LOOP_ELEMENT_BACKGROUND_COLOR);
      titleLabel.setBackground(ViewConstants.Colors.LOOP_ELEMENT_BACKGROUND_COLOR);

      topBarPanel.setTransferHandler(new AlwaysInsertBeforeTransferHandler(StandardForkView.this, containerView));

      final JPanel bottomBarPanel = new JPanel();
      bottomBarPanel.setLayout(new GridBagLayout());

      bottomBarPanel.setBackground(ViewConstants.Colors.LOOP_ELEMENT_BACKGROUND_COLOR);

      bottomBarPanel.setTransferHandler(new AlwaysInsertAfterTransferHandler(StandardForkView.this, containerView));

      // configure the container area panels ---------------------------------------------------------------------------

      thread1ContainerViewPanel = thread1ContainerView.getComponent();
      thread1ContainerViewPanel.setMinimumSize(PREFERRED_CONTAINER_DIMENSION);
      thread1ContainerViewPanel.setMaximumSize(new Dimension(PREFERRED_CONTAINER_DIMENSION.width, thread1ContainerViewPanel.getMaximumSize().height));

      thread2ContainerViewPanel = thread2ContainerView.getComponent();
      thread2ContainerViewPanel.setMinimumSize(PREFERRED_CONTAINER_DIMENSION);
      thread2ContainerViewPanel.setMaximumSize(new Dimension(PREFERRED_CONTAINER_DIMENSION.width, thread2ContainerViewPanel.getMaximumSize().height));

      thread1ContainerViewPanel.setName("forkFrame");
      thread2ContainerViewPanel.setName("forkFrame");

      thread1ContainerViewPanel.setBorder(unselectedBorder);
      thread2ContainerViewPanel.setBorder(unselectedBorder);

      final Component containerDivider = Box.createHorizontalStrut(2);

      // assemble the content panel -----------------------------------------------------------------------------------

      final JPanel contentPanel = getContentPanel();
      final GroupLayout contentPanelLayout = new GroupLayout(contentPanel);
      contentPanel.setLayout(contentPanelLayout);

      contentPanelLayout.setHorizontalGroup(
            contentPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
                  .addComponent(topBarPanel)
                  .addGroup(contentPanelLayout.createSequentialGroup()
                                  .addComponent(thread1ContainerViewPanel)
                                  .addComponent(containerDivider)
                                  .addComponent(thread2ContainerViewPanel)
                  )
                  .addComponent(bottomBarPanel)
      );
      contentPanelLayout.setVerticalGroup(
            contentPanelLayout.createSequentialGroup()
                  .addComponent(topBarPanel)
                  .addGroup(contentPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
                                  .addComponent(thread1ContainerViewPanel)
                                  .addComponent(containerDivider)
                                  .addComponent(thread2ContainerViewPanel)
                  )
                  .addComponent(bottomBarPanel)
      );

      //Background color dependent on container type
      final String panelStyle = containerView.hasParentProgramElementView() ? "forkElementLoop" : "forkElement";
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
                  StandardForkView.this.showInsertLocation(dropPoint);
                  }

               @Override
               protected final void performImport(@NotNull final ProgramElementModel model, @NotNull final Point dropPoint)
                  {
                  getContainerView().handleDropOfModelOntoView(model,
                                                               StandardForkView.this,
                                                               isInsertLocationBefore(dropPoint));
                  }
               });

      LOG.debug("StandardForkView.StandardForkView()");
      }


   @Override
   protected void hideInsertLocationsOfContainedViews()
      {
      thread1ContainerView.hideInsertLocationsOfContainedViews();
      thread2ContainerView.hideInsertLocationsOfContainedViews();
      }

   @Override
   public final void resetViewForSequenceExecution()
      {
      thread1ContainerView.resetContainedViewsForSequenceExecution();
      thread2ContainerView.resetContainedViewsForSequenceExecution();
      resetHighlightContainers();
      }

   @Override
   public void handleAdditionToContainer()
      {
      }

   @Override
   public void handleRemovalFromContainer()
      {
      }

   public void highlightThread1Container()
      {
      SwingUtils.runInGUIThread(highlightThread1Runnable);
      }

   public void highlightThread2Container()
      {
      SwingUtils.runInGUIThread(highlightThread2Runnable);
      }

   public void resetHighlightContainers()
      {
      SwingUtils.runInGUIThread(resetHighlightContainersRunnable);
      }

   }
