package edu.cmu.ri.createlab.sequencebuilder;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import edu.cmu.ri.createlab.sequencebuilder.programelement.model.ProgramElementModel;
import edu.cmu.ri.createlab.sequencebuilder.programelement.view.ProgramElementView;
import edu.cmu.ri.createlab.sequencebuilder.programelement.view.ViewFactory;
import edu.cmu.ri.createlab.sequencebuilder.programelement.view.dnd.ProgramElementDestinationTransferHandler;
import edu.cmu.ri.createlab.sequencebuilder.programelement.view.standard.InsertionHighlightArea;
import edu.cmu.ri.createlab.userinterface.util.ImageUtils;
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

   private final UUID uuid = UUID.randomUUID();
   private final JFrame jFrame;
   private final ContainerModel containerModel;
   private final ViewFactory viewFactory;
   private final JPanel panel = new JPanel();
   private final ProgramElementView parentProgramElementView;
   private final Map<ProgramElementModel, ProgramElementView> modelToViewMap = new HashMap<ProgramElementModel, ProgramElementView>();
   private final Lock lock = new ReentrantLock();  // lock for the modelToViewMap, which we will only ever use in the Swing thread
   private JScrollPane scrollPaneParent;
   private InsertionHighlightArea containerHighlight;
   private final PanelTransferHandler panelTransferHandler;

   private final TailPanelTransferHandler tailPanelTransferHandler;
   private final JPanel tailSpacer = new JPanel();

   private IndicatorLayeredPane scrollPaneIndicators;

   private final Runnable redrawEverythingRunnable =
         new Runnable()
         {
         @Override
         public void run()
            {
            if (LOG.isTraceEnabled())
               {
               LOG.trace("ContainerView[" + uuid + "].redrawEverythingRunnable()");
               }
            panel.removeAll();

            panel.setLayout(new GridBagLayout());

            final GridBagConstraints c = new GridBagConstraints();

            containerHighlight = new InsertionHighlightArea(hasParentProgramElementView());

            c.gridx = 0;
            c.anchor = GridBagConstraints.PAGE_START;
            c.fill = GridBagConstraints.VERTICAL;
            c.gridwidth = 1;
            c.gridheight = 1;
            c.weightx = 1.0;
            c.weighty = 0.0;

            final ImageIcon arrow = ImageUtils.createImageIcon("/edu/cmu/ri/createlab/sequencebuilder/programelement/view/images/purpleArrow.png");
            final ImageIcon orangeArrow = ImageUtils.createImageIcon("/edu/cmu/ri/createlab/sequencebuilder/programelement/view/images/orangeArrow.png");
            if (containerModel.isEmpty())
               {
               //Empty ContainerView
               final String panelStyle = hasParentProgramElementView() ? "loopHelp" : "stageHelp";

               final JLabel helpText1 = new JLabel("Drag-and-Drop ");
               final JLabel helpText2 = new JLabel("Program Elements");
               final JLabel helpText3 = new JLabel("Here");
               final JLabel helpText4 = new JLabel("Here");

               if (hasParentProgramElementView())
                  {
                  //Orange Loop Container
                  helpText2.setText("Expressions and");
                  helpText3.setText("Sequences");

                  c.anchor = GridBagConstraints.CENTER;
                  c.gridy = 0;
                  c.weighty = 0.0;
                  panel.add(helpText1, c);

                  c.anchor = GridBagConstraints.CENTER;
                  c.fill = GridBagConstraints.NONE;
                  c.gridy = 1;
                  c.weighty = 0.0;
                  panel.add(helpText2, c);

                  c.anchor = GridBagConstraints.CENTER;
                  c.fill = GridBagConstraints.VERTICAL;
                  c.gridy = 2;
                  c.weighty = 0.0;
                  panel.add(helpText3, c);

                  c.anchor = GridBagConstraints.CENTER;
                  c.fill = GridBagConstraints.VERTICAL;
                  c.gridy = 3;
                  c.weighty = 0.0;
                  panel.add(helpText4, c);

                  c.anchor = GridBagConstraints.PAGE_START;
                  c.fill = GridBagConstraints.VERTICAL;
                  c.gridy = 4;
                  c.weighty = 0.0;
                  panel.add(new JLabel(orangeArrow), c);

                  c.anchor = GridBagConstraints.PAGE_START;
                  c.fill = GridBagConstraints.VERTICAL;
                  c.gridy = 5;
                  c.weighty = 0.0;
                  panel.add(containerHighlight.getComponent(), c);

                  c.gridy = 6;
                  c.weighty = 1.0;
                  panel.add(SwingUtils.createRigidSpacer(40), c);
                  }
               else
                  {
                  //Purple Stage Container
                  c.anchor = GridBagConstraints.CENTER;
                  c.gridy = 0;
                  c.weighty = 0.0;
                  panel.add(helpText1, c);

                  c.anchor = GridBagConstraints.CENTER;
                  c.fill = GridBagConstraints.NONE;
                  c.gridy = 1;
                  c.weighty = 0.0;
                  panel.add(helpText2, c);

                  c.anchor = GridBagConstraints.CENTER;
                  c.fill = GridBagConstraints.VERTICAL;
                  c.gridy = 2;
                  c.weighty = 0.0;
                  panel.add(helpText3, c);

                  c.anchor = GridBagConstraints.PAGE_START;
                  c.fill = GridBagConstraints.VERTICAL;
                  c.gridy = 3;
                  c.weighty = 0.0;
                  panel.add(new JLabel(arrow), c);

                  c.anchor = GridBagConstraints.PAGE_START;
                  c.fill = GridBagConstraints.VERTICAL;
                  c.gridy = 4;
                  c.weighty = 0.0;
                  panel.add(containerHighlight.getComponent(), c);

                  c.gridy = 5;
                  c.weighty = 1.0;
                  c.fill = GridBagConstraints.VERTICAL;
                  panel.add(SwingUtils.createRigidSpacer(40), c);
                  }

               helpText1.setName(panelStyle);
               helpText2.setName(panelStyle);
               helpText3.setName(panelStyle);
               helpText4.setName(panelStyle);
               }
            else
               {
               //Container View with Elements Within
               final String panelStyle = hasParentProgramElementView() ? "loopHelp" : "stageHelp";
               tailSpacer.setName(panelStyle);

               //panel.add(SwingUtils.createRigidSpacer(2), c);
               if (hasParentProgramElementView())
                  {
                  c.anchor = GridBagConstraints.CENTER;
                  c.fill = GridBagConstraints.VERTICAL;
                  c.gridy = 0;
                  c.weighty = 0.0;
                  panel.add(SwingUtils.createRigidSpacer(0), c);
                  }
               else
                  {
                  c.anchor = GridBagConstraints.CENTER;
                  c.fill = GridBagConstraints.VERTICAL;
                  c.gridy = 0;
                  c.weighty = 0.0;
                  panel.add(SwingUtils.createRigidSpacer(10), c);
                  }

               int count = 1;
               for (final ProgramElementModel model : containerModel.getAsList())
                  {
                  // make sure there's a view for this model (there will be if it's a drag-and-drop, but there won't be if it's coming from loaded XML)
                  final ProgramElementView view = ensureViewIsCreatedForModelWorkhorse(model);

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



              c.fill = GridBagConstraints.BOTH;
              c.gridy = count;
              c.insets = new Insets(0, 0, 0, 0);
              c.weighty = 1.0;
              panel.add(tailSpacer, c);
               }

            containerHighlight.setVisible(false);

            // repaint
            panel.repaint();

            // repack
            if (jFrame != null)
               {
               jFrame.pack();
               }
            }
         };

   /** Creates a <code>ContainerView</code> with no parent {@link ProgramElementView}. */
   public ContainerView(final JFrame jFrame, final ContainerModel containerModel, final ViewFactory viewFactory)
      {
      this(jFrame, containerModel, viewFactory, null);
      }

   /** Creates a <code>ContainerView</code> with the given parent {@link ProgramElementView}. */
   public ContainerView(@Nullable final JFrame jFrame, final ContainerModel containerModel, final ViewFactory viewFactory, @Nullable final ProgramElementView parentProgramElementView)
      {
      this.jFrame = jFrame;
      this.containerModel = containerModel;
      this.viewFactory = viewFactory;
      this.parentProgramElementView = parentProgramElementView;

      this.containerModel.addEventListener(new ContainerModelEventListener());

      // Create the PanelTransferHandler.  The boolean we pass in specifies whether this container is allowed to contain
      // program elements that are themselves containers.  The UI (currently) doesn't allow containers such as the
      // Counter Loop to contain other containers, so this is how we enforce it.  So, the boolean will be false for all
      // containers *except* the root container (the stage).  We can easily tell whether this is the root container by
      // checking whether the parent is null.
      panelTransferHandler = new PanelTransferHandler(parentProgramElementView == null);
      panel.setTransferHandler(panelTransferHandler);
      panel.setAlignmentX(Component.CENTER_ALIGNMENT);
      panel.setName("containerView");

      tailPanelTransferHandler = new TailPanelTransferHandler(parentProgramElementView == null);
      tailSpacer.setTransferHandler(tailPanelTransferHandler);
      tailSpacer.setName("containerView");


      // Used for the indicator behavior on main sequence stage
      scrollPaneParent = null;
      // scrollPaneIndicators = null;

      //TODO: Autoscroll is now enabled for source component lists but only workings when you aren't dragging something(?)
      final MouseMotionListener doScrollRectToVisible =
            new MouseMotionAdapter()
            {
            public void mouseDragged(final MouseEvent e)
               {
               final Rectangle r = new Rectangle(e.getX(), e.getY() + 5, 1, 1);
               ((JPanel)e.getSource()).scrollRectToVisible(r);
               //LOG.debug("Autoscroll motion from: " + this);
               }
            };

      panel.addMouseMotionListener(doScrollRectToVisible);
      panel.setAutoscrolls(true);

      refresh();
      }

   public void setScrollPaneParent(final JScrollPane scrollPane)
      {
      scrollPaneParent = scrollPane;
      }

   public void setScrollPaneIndicators(final IndicatorLayeredPane indicators)
      {
      scrollPaneIndicators = indicators;
      }

   public boolean hasParentProgramElementView()
      {
      return parentProgramElementView != null;
      }

   public JComponent getComponent()
      {
      return panel;
      }

   @Nullable
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
      if (LOG.isTraceEnabled())
         {
         LOG.trace("ContainerView[" + uuid + "].handleDropOfModelOntoView(" + modelBeingDropped + "|" + modelBeingDropped.getUuid() + ")");
         }
      ensureViewIsCreatedForModel(modelBeingDropped);
      if (shouldInsertBefore)
         {
         getContainerModel().insertBefore(modelBeingDropped, programElementViewDropTarget.getProgramElementModel());
         }
      else
         {
         getContainerModel().insertAfter(modelBeingDropped, programElementViewDropTarget.getProgramElementModel());
         }
      containerHighlight.setVisible(false);
      }

   private void appendModel(@Nullable final ProgramElementModel model)
      {
      if (model != null)
         {
         if (LOG.isTraceEnabled())
            {
            LOG.trace("ContainerView[" + uuid + "].appendModel(" + model + "|" + model.getUuid() + ")");
            }

         // create a view for the model and cache it in the modelToViewMap
         ensureViewIsCreatedForModel(model);

         // add the model to the ContainerModel
         containerModel.add(model);
         }
      }

   private void appendModelBefore(@Nullable final ProgramElementModel model, @Nullable final ProgramElementModel oldModel)
      {
      if (model != null)
         {
         if (LOG.isTraceEnabled())
            {
            LOG.trace("ContainerView[" + uuid + "].appendModelBefore(" + model + "|" + model.getUuid() + ")");
            }

         // create a view for the model and cache it in the modelToViewMap
         ensureViewIsCreatedForModel(model);

         // add the model to the ContainerModel
         containerModel.insertBefore(model, oldModel);
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
   @Nullable
   private ProgramElementView ensureViewIsCreatedForModelWorkhorse(final ProgramElementModel model)
      {
      if (LOG.isTraceEnabled())
         {
         LOG.trace("ContainerView[" + uuid + "].ensureViewIsCreatedForModelWorkhorse(" + model + "|" + model.getUuid() + ")");
         }
      lock.lock();  // block until condition holds
      try
         {
         ProgramElementView view = modelToViewMap.get(model);
         if (view == null)
            {
            view = viewFactory.createView(ContainerView.this, model);
            if (view != null)
               {
               view.handleAdditionToContainer();
               }
            modelToViewMap.put(model, view);
            if (LOG.isTraceEnabled())
               {
               LOG.trace("ContainerView[" + uuid + "].ensureViewIsCreatedForModelWorkhorse(): map now contains [" + modelToViewMap.size() + "] items");
               }
            }
         return view;
         }
      finally
         {
         lock.unlock();
         }
      }

   /**
    * Calls {@link ProgramElementView#hideInsertLocations()} on all {@link ProgramElementView}s contained by this container.
    *
    * MUST be called from the Swing thread!
    */
   public void hideInsertLocationsOfContainedViews()
      {
      lock.lock();  // block until condition holds
      try
         {
         final Collection<ProgramElementView> views = modelToViewMap.values();
         containerHighlight.setVisible(false);
         if (scrollPaneIndicators != null)
            {
            scrollPaneIndicators.setAboveIndicatorVisible(false);
            scrollPaneIndicators.setBelowIndicatorVisible(false);
            scrollPaneIndicators.revalidate();
            scrollPaneIndicators.repaint();
            }
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

   /**
    * Calls {@link ProgramElementView#resetViewForSequenceExecution()} on all {@link ProgramElementView}s contained by this container.
    *
    * MUST be called from the Swing thread!
    */
   public void resetContainedViewsForSequenceExecution()
      {
      lock.lock();  // block until condition holds
      try
         {
         final Collection<ProgramElementView> views = modelToViewMap.values();
         for (final ProgramElementView view : views)
            {
            view.resetViewForSequenceExecution();
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

   public Set<DataFlavor> getSupportedDataFlavors()
      {
      return panelTransferHandler.getSupportedDataFlavors();
      }

   private final class ContainerModelEventListener implements ContainerModel.EventListener
      {
      @Override
      public void handleElementAddedEvent(@NotNull final ProgramElementModel model)
         {
         if (LOG.isTraceEnabled())
            {
            LOG.trace("ContainerView[" + uuid + "]$ContainerModelEventListener.handleElementAddedEvent(" + model + "|" + model.getUuid() + ")");
            }
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
            final ProgramElementView view = modelToViewMap.remove(model);
            if (view != null)
               {
               view.handleRemovalFromContainer();
               }
            if (LOG.isTraceEnabled())
               {
               LOG.trace("ContainerView[" + uuid + "]$ContainerModelEventListener.handleElementRemovedEvent(" + model + "|" + model.getUuid() + "): map now contains [" + modelToViewMap.size() + "] items");
               }
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
            for (final ProgramElementView view : modelToViewMap.values())
               {
               if (view != null)
                  {
                  view.handleRemovalFromContainer();
                  }
               }
            modelToViewMap.clear();
            if (LOG.isTraceEnabled())
               {
               LOG.trace("ContainerView[" + uuid + "]$ContainerModelEventListener.handleRemoveAllEvent(): map now contains [" + modelToViewMap.size() + "] items");
               }
            }
         finally
            {
            lock.unlock();
            }
         refresh();
         }

      @Override
      public void handleResetAllProgressBarsForExecution()
         {
         resetContainedViewsForSequenceExecution();
         }
      }

   /**
    * The {@link TransferHandler} for drops onto the container panel.
    */
   private final class PanelTransferHandler extends ProgramElementDestinationTransferHandler
      {
      private PanelTransferHandler(final boolean canContainProgramElementContainers)
         {
         super(canContainProgramElementContainers);
         }

      @Override
      //Todo: Clean this up
      protected void showInsertLocation(final Point dropPoint)
         {
         final ProgramElementView view;
         final boolean insertBefore = isInsertLocationBefore(dropPoint);

         lock.lock();  // block until condition holds
         try
            {
            if (insertBefore)
               {
               view = modelToViewMap.get(getContainerModel().getHead());
               }
            else
               {
               view = modelToViewMap.get(getContainerModel().getTail());
               }
            }
         finally
            {
            lock.unlock();
            }

         //Todo: Below conditions for the indicators are all sort of a hack, could be made way cleaner
         if (view != null)
            {
            if (insertBefore)
               {
               view.showInsertLocationBefore();

               if (scrollPaneIndicators != null && scrollPaneParent != null)
                  {
                  scrollPaneParent.revalidate();
                  scrollPaneParent.repaint();
                  final Rectangle elementBounds = view.getComponent().getBounds();
                  final Rectangle highlightBounds = view.getInsertionHighlightAreaBefore().getBounds();
                  final Rectangle viewBounds = scrollPaneParent.getViewport().getViewRect();

                  highlightBounds.setLocation((int)(highlightBounds.getX() + elementBounds.getX()), (int)(highlightBounds.getY() + elementBounds.getY()));

                  if (viewBounds.contains(highlightBounds) || highlightBounds.getHeight() == 0)
                     {
                     scrollPaneIndicators.setAboveIndicatorVisible(false);
                     scrollPaneIndicators.setBelowIndicatorVisible(false);
                     scrollPaneIndicators.revalidate();
                     scrollPaneIndicators.repaint();
                     }
                  else
                     {
                     scrollPaneIndicators.setAboveIndicatorVisible(true);
                     scrollPaneIndicators.setBelowIndicatorVisible(false);
                     scrollPaneIndicators.revalidate();
                     scrollPaneIndicators.repaint();
                     //TODO Comments below provide rough autoscrolling behavior
                     //Rectangle newView = new Rectangle((int)viewBounds.getX(), (int)viewBounds.getY()-1, (int)viewBounds.getWidth(), (int)viewBounds.getHeight());
                     //scrollPaneParent.getViewport().scrollRectToVisible(newView);

                     //LOG.debug("Sequence Builder: Show Above Indicator:  " + viewBounds + "  " + highlightBounds);
                     }
                  }
               }
            else
               {
               view.showInsertLocationAfter();
               if (scrollPaneIndicators != null && scrollPaneParent != null)
                  {
                  scrollPaneParent.revalidate();
                  scrollPaneParent.repaint();
                  final Rectangle elementBounds = view.getComponent().getBounds();
                  final Rectangle highlightBounds = view.getInsertionHighlightAreaAfter().getBounds();
                  final Rectangle viewBounds = scrollPaneParent.getViewport().getViewRect();

                  highlightBounds.setLocation((int)(highlightBounds.getX() + elementBounds.getX()), (int)(highlightBounds.getY() + elementBounds.getY()));

                  if (viewBounds.contains(highlightBounds) || highlightBounds.getHeight() == 0)
                     {
                     scrollPaneIndicators.setAboveIndicatorVisible(false);
                     scrollPaneIndicators.setBelowIndicatorVisible(false);
                     scrollPaneIndicators.revalidate();
                     scrollPaneIndicators.repaint();
                     }
                  else
                     {
                     scrollPaneIndicators.setAboveIndicatorVisible(false);
                     scrollPaneIndicators.setBelowIndicatorVisible(true);
                     scrollPaneIndicators.revalidate();
                     scrollPaneIndicators.repaint();
                     //TODO Comments below provide rough autoscrolling behavior
                     //Rectangle newView = new Rectangle((int)viewBounds.getX(), (int)viewBounds.getY()+1, (int)viewBounds.getWidth(), (int)viewBounds.getHeight());
                     //scrollPaneParent.getViewport().scrollRectToVisible(newView);

                     //LOG.debug("Sequence Builder: Show Below Indicator:  " + viewBounds + "  " + highlightBounds);
                     }
                  }
               }
            }
         else
            {
            containerHighlight.setVisible(true);
            }
         }

      @Override
      protected void performImport(@NotNull final ProgramElementModel model, @NotNull final Point dropPoint)
         {
         if (LOG.isTraceEnabled())
            {
            LOG.trace("ContainerView[" + uuid + "]$PanelTransferHandler.performImport(" + model + "|" + model.getUuid() + ")");
            }

         if (isInsertLocationBefore(dropPoint))
            {
            final ProgramElementModel head = getContainerModel().getHead();
            if (head != null)
               {
               appendModelBefore(model, head);
               }
            else
               {
               appendModel(model);
               }
            }
         else
            {
            appendModel(model);
            }
         }

      private boolean isInsertLocationBefore(@Nullable final Point dropPoint)
         {
         if (scrollPaneParent != null)
            {
            final Rectangle viewRect = scrollPaneParent.getViewport().getViewRect();
            return dropPoint != null && dropPoint.getY() <= viewRect.getY() + viewRect.getHeight() / 2;
            }
         else
            {
            return dropPoint != null && dropPoint.getY() <= panel.getSize().getHeight() / 2;
            }
         }
      }


       /**
        * The {@link TransferHandler} for drops onto the container panel.
        */
       private final class TailPanelTransferHandler extends ProgramElementDestinationTransferHandler
       {
           private TailPanelTransferHandler(final boolean canContainProgramElementContainers)
           {
               super(canContainProgramElementContainers);
           }

           @Override
           //Todo: Clean this up
           protected void showInsertLocation(final Point dropPoint)
           {
               final ProgramElementView view;
               final boolean insertBefore = false;

               lock.lock();  // block until condition holds
               try
               {
                     view = modelToViewMap.get(getContainerModel().getTail());
               }
               finally
               {
                   lock.unlock();
               }

               //Todo: Below conditions for the indicators are all sort of a hack, could be made way cleaner
               if (view != null)
               {
                       view.showInsertLocationAfter();
                       if (scrollPaneIndicators != null && scrollPaneParent != null)
                       {
                           scrollPaneParent.revalidate();
                           scrollPaneParent.repaint();
                           final Rectangle elementBounds = view.getComponent().getBounds();
                           final Rectangle highlightBounds = view.getInsertionHighlightAreaAfter().getBounds();
                           final Rectangle viewBounds = scrollPaneParent.getViewport().getViewRect();

                           highlightBounds.setLocation((int)(highlightBounds.getX() + elementBounds.getX()), (int)(highlightBounds.getY() + elementBounds.getY()));

                           if (viewBounds.contains(highlightBounds) || highlightBounds.getHeight() == 0)
                           {
                               scrollPaneIndicators.setAboveIndicatorVisible(false);
                               scrollPaneIndicators.setBelowIndicatorVisible(false);
                               scrollPaneIndicators.revalidate();
                               scrollPaneIndicators.repaint();
                           }
                           else
                           {
                               scrollPaneIndicators.setAboveIndicatorVisible(false);
                               scrollPaneIndicators.setBelowIndicatorVisible(true);
                               scrollPaneIndicators.revalidate();
                               scrollPaneIndicators.repaint();
                               //TODO Comments below provide rough autoscrolling behavior
                               //Rectangle newView = new Rectangle((int)viewBounds.getX(), (int)viewBounds.getY()+1, (int)viewBounds.getWidth(), (int)viewBounds.getHeight());
                               //scrollPaneParent.getViewport().scrollRectToVisible(newView);

                               //LOG.debug("Sequence Builder: Show Below Indicator:  " + viewBounds + "  " + highlightBounds);
                           }
                       }
               }
               else
               {
                   containerHighlight.setVisible(true);
               }
           }

           @Override
           protected void performImport(@NotNull final ProgramElementModel model, @NotNull final Point dropPoint)
           {
               if (LOG.isTraceEnabled())
               {
                   LOG.trace("ContainerView[" + uuid + "]$PanelTransferHandler.performImport(" + model + "|" + model.getUuid() + ")");
               }
               appendModel(model);
           }

       }



   }
