package edu.cmu.ri.createlab.sequencebuilder;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import edu.cmu.ri.createlab.sequencebuilder.programelement.model.ProgramElementModel;
import edu.cmu.ri.createlab.sequencebuilder.programelement.view.ViewEventPublisher;
import edu.cmu.ri.createlab.util.thread.DaemonThreadFactory;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.Nullable;

/**
 * <p>
 * <code>SequenceExecutor</code> is a singleton which manages execution of a {@link Sequence}.
 * </p>
 *
 * @author Chris Bartley (bartley@cmu.edu)
 */
public final class SequenceExecutor
   {
   interface EventListener
      {
      void handleExecutionStart();

      void handleExecutionEnd();
      }

   private static final Logger LOG = Logger.getLogger(SequenceExecutor.class);

   private static final SequenceExecutor INSTANCE = new SequenceExecutor();

   public static SequenceExecutor getInstance()
      {
      return INSTANCE;
      }

   private final AtomicBoolean isRunning = new AtomicBoolean(false);
   private ExecutorService sequenceExecutionExecutor = null;
   private final Set<EventListener> eventListeners = new HashSet<EventListener>();
   private final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(5, new DaemonThreadFactory(this.getClass().getSimpleName()));
   private Runnable resetViewsRunnable =
         new Runnable()
         {
         @Override
         public void run()
            {
            ViewEventPublisher.getInstance().publishResetViewsForSequenceExecutionEvent();
            }
         };

   public void addEventListener(@Nullable final EventListener listener)
      {
      if (listener != null)
         {
         eventListeners.add(listener);
         }
      }

   public void removeEventListener(@Nullable final EventListener listener)
      {
      if (listener != null)
         {
         eventListeners.remove(listener);
         }
      }

   /**
    * Starts executing the given {@link Sequence}.  Does nothing if the {@link Sequence} is <code>null</code> and/or
    * if the <code>SequenceExecutor</code> is already running.
    *
    * @see #isRunning()
    */
   public void start(final Sequence sequence)
      {
      LOG.debug("SequenceExecutorSequenceExecutor.start(" + sequence + ")");
      if (sequence != null)
         {
         if (isRunning.compareAndSet(false, true))
            {
            // notify listeners
            if (!eventListeners.isEmpty())
               {
               for (final EventListener listener : eventListeners)
                  {
                  listener.handleExecutionStart();
                  }
               }

            LOG.debug("SequenceExecutor.start(): calling ViewEventPublisher.publishResetViewsForSequenceExecutionEvent()...");
            ViewEventPublisher.getInstance().publishResetViewsForSequenceExecutionEvent();
            LOG.debug("SequenceExecutor.start(): about to start executing...");
            try
               {
               LOG.debug("SequenceExecutor.start(): creating the sequenceExecutionExecutor");
               sequenceExecutionExecutor = Executors.newSingleThreadExecutor(new DaemonThreadFactory("SequenceExecutionExecutor_" + this.getClass().getSimpleName()));
               LOG.debug("SequenceExecutor.start(): submitting the ProgramElementExecutor to the sequenceExecutionExecutor");
               final Future<Boolean> future = sequenceExecutionExecutor.submit(new ProgramElementExecutor(sequence.getContainerModel().getAsList()));
               LOG.debug("SequenceExecutor.start(): about to call future.get()");
               if (future.get())
                  {
                  LOG.debug("SequenceExecutor.start(): Execution completed successfully.");
                  }
               else
                  {
                  LOG.debug("SequenceExecutor.start(): Execution completed with a failure.");
                  }

               // we're done, so call stop...
               stop();
               }
            catch (Exception e)
               {
               LOG.error("SequenceExecutor.start(): Exception while executing the sequence", e);
               }
            }
         }
      }

   /**
    * Stops the executor.  Does nothing if the executor is not running.
    */
   public void stop()
      {
      LOG.debug("SequenceExecutor.stop()");
      if (isRunning.compareAndSet(true, false))
         {
         if (sequenceExecutionExecutor != null)
            {
            try
               {
               sequenceExecutionExecutor.shutdownNow();
               sequenceExecutionExecutor = null;
               }
            catch (Exception e)
               {
               LOG.error("SequenceExecutor.stop(): Exception while trying to shut down the sequence execution executor.", e);
               }
            }

         // notify listeners
         if (!eventListeners.isEmpty())
            {
            for (final EventListener listener : eventListeners)
               {
               listener.handleExecutionEnd();
               }
            }
         }

      // reset the views, but do so with a delay to allow time for stuff to stop (yes, this is a bit hacky)
      scheduledExecutorService.schedule(resetViewsRunnable, 100, TimeUnit.MILLISECONDS);
      }

   public boolean isRunning()
      {
      return isRunning.get();
      }

   private final class ProgramElementExecutor implements Callable<Boolean>
      {
      private final List<ProgramElementModel> programElementModels;

      private ProgramElementExecutor(final List<ProgramElementModel> programElementModels)
         {
         this.programElementModels = programElementModels;
         }

      @Override
      public Boolean call() throws Exception
         {
         try
            {
            // iterate over the models and execute them
            for (final ProgramElementModel model : programElementModels)
               {
               if (isRunning())
                  {
                  model.execute();
                  }
               }
            }
         catch (Exception e)
            {
            LOG.error("SequenceExecutor$ProgramElementExecutor.call(): Exception while executing the sequence.", e);
            return false;
            }

         return true;
         }
      }
   }
