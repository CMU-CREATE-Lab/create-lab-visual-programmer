package edu.cmu.ri.createlab.sequencebuilder;

import org.jetbrains.annotations.Nullable;

/**
 * <p>
 * <code>SequenceExecutor</code> manages execution of a sequence.
 * </p>
 *
 * @author Chris Bartley (bartley@cmu.edu)
 */
public interface SequenceExecutor
   {
   interface EventListener
      {
      void handleExecutionStart();

      void handleExecutionEnd();
      }

   void addEventListener(@Nullable final EventListener listener);

   void removeEventListener(@Nullable final EventListener listener);

   void start();

   void stop();

   boolean isRunning();
   }
