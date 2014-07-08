package edu.cmu.ri.createlab.sequencebuilder.programelement.view;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import edu.cmu.ri.createlab.sequencebuilder.ContainerView;
import edu.cmu.ri.createlab.userinterface.util.SwingUtils;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public final class ViewEventPublisher
   {
   private static final Logger LOG = Logger.getLogger(ViewEventPublisher.class);

   private static final Lock INSTANCE_LOCK = new ReentrantLock();

   private static ViewEventPublisher INSTANCE = null;

   public static void createInstance(@NotNull final ContainerView rootContainerView)
      {
      INSTANCE_LOCK.lock();
      try
         {
         if (INSTANCE == null)
            {
            INSTANCE = new ViewEventPublisher(rootContainerView);
            }
         else
            {
            LOG.error("ViewEventPublisher.createInstance(): The ViewEventPublisher instance has already been created, so this invocation of createInstance() will be ignored.");
            }
         }
      finally
         {
         INSTANCE_LOCK.unlock();
         }
      }

   public static ViewEventPublisher getInstance()
      {
      INSTANCE_LOCK.lock();
      try
         {
         if (INSTANCE == null)
            {
            final String message = "The ViewEventPublisher instance has not yet been created!  You must call ViewEventPublisher.createInstance() first.";
            LOG.error("ViewEventPublisher.getInstance(): " + message);
            throw new IllegalStateException(message);
            }
         else
            {
            return INSTANCE;
            }
         }
      finally
         {
         INSTANCE_LOCK.unlock();
         }
      }

   private final ContainerView rootContainerView;
   private final Runnable hideAllInsertLocationsRunnable =
         new Runnable()
         {
         public void run()
            {
            rootContainerView.hideInsertLocationsOfContainedViews();
            }
         };

   private final Runnable resetContainedViewsForExecutionRunnable =
         new Runnable()
         {
         public void run()
            {
            rootContainerView.resetContainedViewsForSequenceExecution();
            }
         };

   private ViewEventPublisher(final ContainerView rootContainerView)
      {
      // private to prevent instantiation
      this.rootContainerView = rootContainerView;
      }

   /** Calls the {@link ProgramElementView#hideInsertLocations()} method on all views.  Runs in the Swing thread. */
   public void publishHideInsertLocationsEvent()
      {
      SwingUtils.runInGUIThread(hideAllInsertLocationsRunnable);
      }

   /** Calls the {@link ProgramElementView#resetViewForSequenceExecution()} method on all views.  Runs in the Swing thread. */
   public void publishResetViewsForSequenceExecutionEvent()
      {
      SwingUtils.runInGUIThread(resetContainedViewsForExecutionRunnable);
      }
   }
