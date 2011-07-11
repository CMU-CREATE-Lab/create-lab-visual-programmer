package edu.cmu.ri.createlab.sequencebuilder;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import edu.cmu.ri.createlab.collections.UniqueNodeLinkedList;
import edu.cmu.ri.createlab.sequencebuilder.programelement.view.ProgramElementView;
import edu.cmu.ri.createlab.util.thread.DaemonThreadFactory;
import org.apache.log4j.Logger;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public final class ContainerModel
   {
   private static final Logger LOG = Logger.getLogger(ContainerModel.class);
   public static final String ELEMENT_NAME = "program-element-container";

   public interface EventListener
      {
      /** Called when the given {@link ProgramElementView} is added to the container. */
      void handleElementAddedEvent(@NotNull final ProgramElementView programElementView);

      /** Called when the given {@link ProgramElementView} is removed from the container. */
      void handleElementRemovedEvent(@NotNull final ProgramElementView programElementView);

      /** Called when the {@link ContainerModel#removeAll()} is called. */
      void handleRemoveAllEvent();
      }

   private final UniqueNodeLinkedList<ProgramElementView> list = new UniqueNodeLinkedList<ProgramElementView>();
   private final Lock listLock = new ReentrantLock();

   private final Set<EventListener> eventListeners = new HashSet<EventListener>();
   private ExecutorService executorService = Executors.newCachedThreadPool(new DaemonThreadFactory(this.getClass().getSimpleName()));

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

   @Nullable
   public ProgramElementView getHead()
      {
      listLock.lock();  // block until condition holds
      try
         {
         return list.getHead();
         }
      finally
         {
         listLock.unlock();
         }
      }

   @Nullable
   public ProgramElementView getTail()
      {
      listLock.lock();  // block until condition holds
      try
         {
         return list.getTail();
         }
      finally
         {
         listLock.unlock();
         }
      }

   public boolean add(@Nullable final ProgramElementView view)
      {
      boolean result = false;

      if (view != null)
         {
         listLock.lock();  // block until condition holds
         try
            {
            result = list.add(view);
            }
         finally
            {
            listLock.unlock();
            }

         if (result)
            {
            // notify listeners
            if (!eventListeners.isEmpty())
               {
               executorService.execute(
                     new Runnable()
                     {
                     public void run()
                        {
                        for (final EventListener listener : eventListeners)
                           {
                           listener.handleElementAddedEvent(view);
                           }
                        }
                     });
               }
            }
         else
            {
            if (LOG.isInfoEnabled())
               {
               LOG.info("ContainerModel.add(): failed to add ProgramElementView [" + view + "] to the model.");
               }
            }
         }

      return result;
      }

   public boolean insertBefore(@Nullable final ProgramElementView newElement, @Nullable final ProgramElementView existingElement)
      {
      boolean result = false;

      if (newElement != null && existingElement != null)
         {
         listLock.lock();  // block until condition holds
         try
            {
            result = list.insertBefore(newElement, existingElement);
            }
         finally
            {
            listLock.unlock();
            }

         if (result)
            {
            // notify listeners
            if (!eventListeners.isEmpty())
               {
               executorService.execute(
                     new Runnable()
                     {
                     public void run()
                        {
                        for (final EventListener listener : eventListeners)
                           {
                           listener.handleElementAddedEvent(newElement);
                           }
                        }
                     });
               }
            }
         else
            {
            if (LOG.isInfoEnabled())
               {
               LOG.info("ContainerModel.add(): failed to insert ProgramElementView [" + newElement + "] before [" + existingElement + "] in the model.");
               }
            }
         }

      return result;
      }

   public boolean insertAfter(@Nullable final ProgramElementView newElement, @Nullable final ProgramElementView existingElement)
      {
      boolean result = false;

      if (newElement != null && existingElement != null)
         {
         listLock.lock();  // block until condition holds
         try
            {
            result = list.insertAfter(newElement, existingElement);
            }
         finally
            {
            listLock.unlock();
            }

         if (result)
            {
            // notify listeners
            if (!eventListeners.isEmpty())
               {
               executorService.execute(
                     new Runnable()
                     {
                     public void run()
                        {
                        for (final EventListener listener : eventListeners)
                           {
                           listener.handleElementAddedEvent(newElement);
                           }
                        }
                     });
               }
            }
         else
            {
            if (LOG.isInfoEnabled())
               {
               LOG.info("ContainerModel.add(): failed to insert ProgramElementView [" + newElement + "] after [" + existingElement + "] in the model.");
               }
            }
         }

      return result;
      }

   public boolean remove(@Nullable final ProgramElementView view)
      {
      boolean result = false;

      if (view != null)
         {
         listLock.lock();  // block until condition holds
         try
            {
            result = list.remove(view);
            }
         finally
            {
            listLock.unlock();
            }

         if (result)
            {
            // notify listeners
            if (!eventListeners.isEmpty())
               {
               executorService.execute(
                     new Runnable()
                     {
                     public void run()
                        {
                        for (final EventListener listener : eventListeners)
                           {
                           listener.handleElementRemovedEvent(view);
                           }
                        }
                     });
               }
            }
         else
            {
            if (LOG.isInfoEnabled())
               {
               LOG.info("ContainerModel.add(): failed to remove ProgramElementView [" + view + "] from the model.");
               }
            }
         }

      return result;
      }

   /** Removes all program elements from the model. */
   public void removeAll()
      {
      listLock.lock();  // block until condition holds
      try
         {
         list.clear();
         }
      finally
         {
         listLock.unlock();
         }

      // notify listeners
      if (!eventListeners.isEmpty())
         {
         executorService.execute(
               new Runnable()
               {
               public void run()
                  {
                  for (final EventListener listener : eventListeners)
                     {
                     listener.handleRemoveAllEvent();
                     ;
                     }
                  }
               });
         }
      }

   @NotNull
   public List<ProgramElementView> getAsList()
      {
      listLock.lock();  // block until condition holds
      try
         {
         return list.getAsList();
         }
      finally
         {
         listLock.unlock();
         }
      }

   /** Returns the number of elements in the model. */
   public int size()
      {
      listLock.lock();  // block until condition holds
      try
         {
         return list.size();
         }
      finally
         {
         listLock.unlock();
         }
      }

   /** Returns <code>true</code> if the model does not contain any program elements; <code>false</code> otherwise. */
   public boolean isEmpty()
      {
      listLock.lock();  // block until condition holds
      try
         {
         return list.isEmpty();
         }
      finally
         {
         listLock.unlock();
         }
      }

   /** Creates an XML element representing this instance. */
   @NotNull
   public Element toElement()
      {
      listLock.lock();  // block until condition holds
      try
         {
         final Element programElementContainerElement = new Element(ELEMENT_NAME);

         final List<ProgramElementView> programElementViews = list.getAsList();
         for (final ProgramElementView programElementView : programElementViews)
            {
            final Element element = programElementView.getProgramElementModel().toElement();
            programElementContainerElement.addContent(element);
            }

         return programElementContainerElement;
         }
      finally
         {
         listLock.unlock();
         }
      }
   }
