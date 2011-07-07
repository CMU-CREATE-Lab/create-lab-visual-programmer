package edu.cmu.ri.createlab.sequencebuilder.programelement.view;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import edu.cmu.ri.createlab.userinterface.util.SwingUtils;
import org.jetbrains.annotations.Nullable;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public final class ViewEventPublisher
   {
   private static final ViewEventPublisher INSTANCE = new ViewEventPublisher();

   public static ViewEventPublisher getInstance()
      {
      return INSTANCE;
      }

   private final Set<ProgramElementView> programElementViews = new HashSet<ProgramElementView>();
   private final Lock setLock = new ReentrantLock();
   private final Runnable hideAllInsertLocationsRunnable =
         new Runnable()
         {
         public void run()
            {
            setLock.lock();  // block until condition holds
            try
               {
               for (final ProgramElementView view : programElementViews)
                  {
                  view.hideInsertLocations();
                  }
               }
            finally
               {
               setLock.unlock();
               }
            }
         };

   /** Adds the given view to the <code>ViewEventPublisher</code> so that it will be notified of events. */
   public void addProgramElementView(@Nullable final ProgramElementView view)
      {
      if (view != null)
         {
         setLock.lock();  // block until condition holds
         try
            {
            programElementViews.add(view);
            }
         finally
            {
            setLock.unlock();
            }
         }
      }

   /** Removes the given view from the <code>ViewEventPublisher</code> so that it will no longer be notified of events. */
   public void removeProgramElementView(@Nullable final ProgramElementView view)
      {
      if (view != null)
         {
         setLock.lock();  // block until condition holds
         try
            {
            programElementViews.remove(view);
            }
         finally
            {
            setLock.unlock();
            }
         }
      }

   /**
    * Calls the {@link ProgramElementView#hideInsertLocations()} method on all listeners.  Runs in the Swing
    * thread.
    */
   public void publishHideInsertLocationsEvent()
      {
      setLock.lock();  // block until condition holds
      try
         {
         SwingUtils.runInGUIThread(hideAllInsertLocationsRunnable);
         }
      finally
         {
         setLock.unlock();
         }
      }

   private ViewEventPublisher()
      {
      // private to prevent instantiation
      }
   }
