package edu.cmu.ri.createlab.sequencebuilder;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import edu.cmu.ri.createlab.sequencebuilder.programelement.model.ProgramElementModel;
import edu.cmu.ri.createlab.sequencebuilder.programelement.view.ViewEventPublisher;
import edu.cmu.ri.createlab.util.thread.DaemonThreadFactory;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public final class DefaultSequenceExecutor implements SequenceExecutor
   {
   private static final Logger LOG = Logger.getLogger(DefaultSequenceExecutor.class);

   private final Sequence sequence;
   private final AtomicBoolean isRunning = new AtomicBoolean(false);
   private ExecutorService sequenceExecutionExecutor;

   private final Set<EventListener> eventListeners = new HashSet<EventListener>();

   public DefaultSequenceExecutor(@NotNull final Sequence sequence)
      {
      this.sequence = sequence;
      }

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

   @Override
   public void start()
      {
      LOG.debug("DefaultSequenceExecutor.start()");
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

         LOG.debug("DefaultSequenceExecutor.start(): calling ViewEventPublisher.publishResetViewsForSequenceExecutionEvent()...");
         ViewEventPublisher.getInstance().publishResetViewsForSequenceExecutionEvent();

         try
            {
            sequenceExecutionExecutor = Executors.newSingleThreadExecutor(new DaemonThreadFactory(this.getClass().getSimpleName()));
            final Future<Boolean> future = sequenceExecutionExecutor.submit(new ProgramElementExecutor());
            if (future.get())
               {
               LOG.debug("DefaultSequenceExecutor.start(): Execution completed successfully.");
               }
            else
               {
               LOG.debug("DefaultSequenceExecutor.start(): Execution completed with a failure.");
               }

            // we're done, so call stop...
            stop();
            }
         catch (Exception e)
            {
            LOG.error("DefaultSequenceExecutor.start(): Exception while executing the sequence", e);
            }
         }
      }

   @Override
   public void stop()
      {
      LOG.debug("DefaultSequenceExecutor.stop()");
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
               LOG.error("DefaultSequenceExecutor.stop(): Exception while trying to shut down the sequence execution executor.", e);
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
         LOG.debug("DefaultSequenceExecutor.stop(): calling ViewEventPublisher.publishResetViewsForSequenceExecutionEvent()...");
         ViewEventPublisher.getInstance().publishResetViewsForSequenceExecutionEvent();
         }
      }

   public boolean isRunning()
      {
      return isRunning.get();
      }

   private final class ProgramElementExecutor implements Callable<Boolean>
      {
      @Override
      public Boolean call() throws Exception
         {
         LOG.debug("DefaultSequenceExecutor$ProgramElementExecutor.run() ************************************************");

         try
            {
            // iterate over the models and execute them
            final List<ProgramElementModel> programElementModels = sequence.getContainerModel().getAsList();
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
            LOG.error("DefaultSequenceExecutor$ProgramElementExecutor.call(): Exception while executing the sequence.", e);
            return false;
            }

         return true;
         }
      }
   }
