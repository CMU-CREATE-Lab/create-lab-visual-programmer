package edu.cmu.ri.createlab.sequencebuilder;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import edu.cmu.ri.createlab.sequencebuilder.programelement.model.ProgramElementModel;
import edu.cmu.ri.createlab.sequencebuilder.programelement.view.ProgramElementView;
import edu.cmu.ri.createlab.sequencebuilder.programelement.view.ViewFactory;
import edu.cmu.ri.createlab.sequencebuilder.programelement.view.dnd.ProgramElementDestinationTransferHandler;
import edu.cmu.ri.createlab.userinterface.util.SwingUtils;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public final class ContainerView
   {
   private static final Logger LOG = Logger.getLogger(ContainerView.class);

   private final JFrame jFrame;
   private final ContainerModel containerModel;
   private final ViewFactory viewFactory;
   private final JPanel panel = new JPanel();
   private final ProgramElementView parentProgramElementView;
   private final Map<ProgramElementModel, ProgramElementView> modelToViewMap = new HashMap<ProgramElementModel, ProgramElementView>();
   private final Lock lock = new ReentrantLock();  // lock for the modelToViewMap, which we will only ever use in the Swing thread

   private final Runnable redrawEverythingRunnable =
         new Runnable()
         {
         @Override
         public void run()
            {
            panel.removeAll();

            panel.setLayout(new GridBagLayout());

            final GridBagConstraints c = new GridBagConstraints();

            c.gridx = 0;
            c.anchor = GridBagConstraints.PAGE_START;
            c.fill = GridBagConstraints.NONE;
            c.gridwidth = 1;
            c.gridheight = 1;
            c.weightx = 1.0;
            c.weighty = 0.0;

            int count = 0;

            for (final ProgramElementModel model : containerModel.getAsList())
               {
               // make sure there's a view for this model (there will be if it's a drag-and-drop, but there won't be if it's coming from loaded XML)
               ensureViewIsCreatedForModelWorkhorse(model);

               final ProgramElementView view;
               lock.lock();  // block until condition holds
               try
                  {
                  view = modelToViewMap.get(model);
                  }
               finally
                  {
                  lock.unlock();
                  }

               if (view == null)
                  {
                  LOG.error("ContainerView.redrawEverythingRunnable(): found a null view for model [" + model + "].  This should only happen if the ViewFactory doesn't know how to create a view for the given model.");
                  }
               else
                  {
                  final JComponent component = view.getComponent();
                  c.gridy = count;
                  panel.add(component, c);
                  count++;
                  }
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
      ensureViewIsCreatedForModel(modelBeingDropped);
      if (shouldInsertBefore)
         {
         getContainerModel().insertBefore(modelBeingDropped, programElementViewDropTarget.getProgramElementModel());
         }
      else
         {
         getContainerModel().insertAfter(modelBeingDropped, programElementViewDropTarget.getProgramElementModel());
         }
      }

   private void appendModel(@Nullable final ProgramElementModel model)
      {
      if (model != null)
         {
         // create a view for the model and cache it in the modelToViewMap
         ensureViewIsCreatedForModel(model);

         // add the model to the ContainerModel
         containerModel.add(model);
         }
      }

   private void ensureViewIsCreatedForModel(final ProgramElementModel model)
      {
      if (SwingUtilities.isEventDispatchThread())
         {
         ensureViewIsCreatedForModelWorkhorse(model);
         }
      else
         {
         SwingUtilities.invokeLater(
               new Runnable()
               {
               @Override
               public void run()
                  {
                  ensureViewIsCreatedForModelWorkhorse(model);
                  }
               }
         );
         }
      }

   // MUST only ever be called from the Swing thread!
   private void ensureViewIsCreatedForModelWorkhorse(final ProgramElementModel model)
      {
      lock.lock();  // block until condition holds
      try
         {
         if (!modelToViewMap.containsKey(model))
            {
            modelToViewMap.put(model, viewFactory.createView(ContainerView.this, model));
            }
         }
      finally
         {
         lock.unlock();
         }
      }

   /** Calls {@link ProgramElementView#hideInsertLocations()} on all {@link ProgramElementView}s contained by this container. */
   public void hideInsertLocationsOfContainedViews()
      {
      lock.lock();  // block until condition holds
      try
         {
         final Collection<ProgramElementView> views = modelToViewMap.values();
         for (final ProgramElementView view : views)
            {
            view.hideInsertLocations();
            }
         }
      finally
         {
         lock.unlock();
         }
      }

   public void refresh()
      {
      // redraw everything
      SwingUtils.runInGUIThread(redrawEverythingRunnable);
      }

   private final class ContainerModelEventListener implements ContainerModel.EventListener
      {
      @Override
      public void handleElementAddedEvent(@NotNull final ProgramElementModel model)
         {
         // make sure there's a view for this model (there will be if it's a drag-and-drop, but there won't be if it's coming from loaded XML)
         ensureViewIsCreatedForModel(model);
         refresh();
         }

      @Override
      public void handleElementRemovedEvent(@NotNull final ProgramElementModel model)
         {
         lock.lock();  // block until condition holds
         try
            {
            modelToViewMap.remove(model);
            }
         finally
            {
            lock.unlock();
            }
         refresh();
         }

      @Override
      public void handleRemoveAllEvent()
         {
         lock.lock();  // block until condition holds
         try
            {
            modelToViewMap.clear();
            }
         finally
            {
            lock.unlock();
            }
         refresh();
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
         final ProgramElementView view;
         lock.lock();  // block until condition holds
         try
            {
            view = modelToViewMap.get(getContainerModel().getTail());
            }
         finally
            {
            lock.unlock();
            }

         if (view != null)
            {
            view.showInsertLocationAfter();
            }
         }

      @Override
      protected void performImport(@NotNull final ProgramElementModel model, @NotNull final Point dropPoint)
         {
         appendModel(model);
         }
      }
   }
