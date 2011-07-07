package edu.cmu.ri.createlab.sequencebuilder;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.TransferHandler;
import edu.cmu.ri.createlab.sequencebuilder.programelement.model.ProgramElementModel;
import edu.cmu.ri.createlab.sequencebuilder.programelement.view.ProgramElementView;
import edu.cmu.ri.createlab.sequencebuilder.programelement.view.ViewFactory;
import edu.cmu.ri.createlab.sequencebuilder.programelement.view.dnd.ProgramElementDestinationTransferHandler;
import edu.cmu.ri.createlab.userinterface.util.SwingUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public final class ContainerView
   {
   private final JFrame jFrame;
   private final ContainerModel containerModel;
   private final ViewFactory viewFactory;
   private final JPanel panel = new JPanel();
   private final ProgramElementView parentProgramElementView;

   private final Runnable redrawEverythingRunnable =
         new Runnable()
         {
         @Override
         public void run()
            {
            panel.removeAll();

            panel.setLayout(new GridBagLayout());

            GridBagConstraints c = new GridBagConstraints();

            c.gridx = 0;
            c.anchor = GridBagConstraints.PAGE_START;
            c.fill = GridBagConstraints.NONE;
            c.gridwidth = 1;
            c.gridheight = 1;
            c.weightx = 1.0;
            c.weighty = 0.0;

            int count = 0;

            for (final ProgramElementView programElementView : containerModel.getAsList())
               {
               final JComponent component = programElementView.getComponent();
               c.gridy = count;
               panel.add(component, c);
               count++;
               }

            c.gridy = count;
            c.weighty = 1.0;
            panel.add(SwingUtils.createRigidSpacer(40), c);

            // repaint
            panel.repaint();

            // repack
            jFrame.pack();
            }
         };

   /** Creates a <code>ContainerView</code> with no parent {@link ProgramElementView}. */
   public ContainerView(final JFrame jFrame, final ContainerModel containerModel, final ViewFactory viewFactory)
      {
      this(jFrame, containerModel, viewFactory, null);
      }

   /** Creates a <code>ContainerView</code> with the given parent {@link ProgramElementView}. */
   public ContainerView(final JFrame jFrame, final ContainerModel containerModel, final ViewFactory viewFactory, @Nullable final ProgramElementView parentProgramElementView)
      {
      this.jFrame = jFrame;
      this.containerModel = containerModel;
      this.viewFactory = viewFactory;
      this.parentProgramElementView = parentProgramElementView;

      this.containerModel.addEventListener(new ContainerModelEventListener());
      panel.setTransferHandler(new PanelTransferHandler());
      panel.setAlignmentX(Component.CENTER_ALIGNMENT);
      panel.setName("containerView");
      }

   public ProgramElementView getParentProgramElementView()
      {
      return parentProgramElementView;
      }

   public boolean hasParentProgramElementView()
      {
      return parentProgramElementView != null;
      }

   public JComponent getComponent()
      {
      return panel;
      }

   public JFrame getJFrame()
      {
      return jFrame;
      }

   public ContainerModel getContainerModel()
      {
      return containerModel;
      }

   public void handleDropOfModelOntoView(@NotNull final ProgramElementModel modelBeingDropped,
                                         @NotNull final ProgramElementView programElementViewDropTarget,
                                         final boolean shouldInsertBefore)
      {
      final ProgramElementView newView = viewFactory.createView(this, modelBeingDropped);
      if (shouldInsertBefore)
         {
         getContainerModel().insertBefore(newView, programElementViewDropTarget);
         }
      else
         {
         getContainerModel().insertAfter(newView, programElementViewDropTarget);
         }
      }

   private final class ContainerModelEventListener implements ContainerModel.EventListener
      {
      @Override
      public void handleElementAddedEvent(@NotNull final ProgramElementView programElementView)
         {
         // redraw everything
         SwingUtils.runInGUIThread(redrawEverythingRunnable);
         }

      @Override
      public void handleElementRemovedEvent(@NotNull final ProgramElementView programElementView)
         {
         // redraw everything
         SwingUtils.runInGUIThread(redrawEverythingRunnable);
         }
      }

   /**
    * The {@link TransferHandler} for drops onto the container panel.
    */
   private final class PanelTransferHandler extends ProgramElementDestinationTransferHandler
      {
      @Override
      protected void showInsertLocation(final Point dropPoint)
         {
         final ProgramElementView tail = getContainerModel().getTail();
         if (tail != null)
            {
            tail.showInsertLocationAfter();
            }
         }

      @Override
      protected void performImport(@NotNull final ProgramElementModel model, @NotNull final Point dropPoint)
         {
         // ask the ViewFactory to create a view for the model
         final ProgramElementView view = viewFactory.createView(ContainerView.this, model);

         // add the view to the ContainerModel
         containerModel.add(view);
         }
      }
   }
