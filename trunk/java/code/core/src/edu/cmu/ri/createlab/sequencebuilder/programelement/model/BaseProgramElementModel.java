package edu.cmu.ri.createlab.sequencebuilder.programelement.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import edu.cmu.ri.createlab.util.thread.DaemonThreadFactory;
import edu.cmu.ri.createlab.visualprogrammer.VisualProgrammerDevice;
import org.apache.log4j.Logger;
import org.jdom.CDATA;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
abstract class BaseProgramElementModel<ModelClass extends ProgramElementModel> implements ProgramElementModel<ModelClass>
   {
   private static final Logger LOG = Logger.getLogger(BaseProgramElementModel.class);

   @NotNull
   private final VisualProgrammerDevice visualProgrammerDevice;

   @NotNull
   private String comment = "";
   private boolean isCommentVisible = false;

   private Set<PropertyChangeEventListener> eventListeners = new HashSet<PropertyChangeEventListener>();
   private Map<String, Set<PropertyChangeEventListener>> propertyToEventListenersMap = new HashMap<String, Set<PropertyChangeEventListener>>();
   private ExecutorService executorService = Executors.newCachedThreadPool(new DaemonThreadFactory(this.getClass().getSimpleName()));
   private final Lock listenerLock = new ReentrantLock();

   protected BaseProgramElementModel(@NotNull final VisualProgrammerDevice visualProgrammerDevice,
                                     @Nullable final String comment)
      {
      this.visualProgrammerDevice = visualProgrammerDevice;
      this.comment = comment == null ? "" : comment;
      }

   @NotNull
   public final VisualProgrammerDevice getVisualProgrammerDevice()
      {
      return visualProgrammerDevice;
      }

   @NotNull
   public final String getComment()
      {
      return comment;
      }

   protected final Element getCommentAsElement()
      {
      final Element element = new Element("comment");
      element.setAttribute("is-visible", String.valueOf(isCommentVisible));
      element.setContent(new CDATA(comment));
      return element;
      }

   /**
    * Sets the comment to the given <code>comment</code> and causes {@link PropertyChangeEvent}s to be fired for the
    * {@link #HAS_COMMENT_PROPERTY} and {@link #COMMENT_PROPERTY} properties.
    */
   @Override
   public final void setComment(@Nullable final String comment)
      {
      final String nonNullComment = comment == null ? "" : comment;
      final PropertyChangeEvent commentEvent = new PropertyChangeEventImpl(COMMENT_PROPERTY, this.comment, nonNullComment);
      final boolean oldValueOfHasComment = hasComment();
      this.comment = nonNullComment;
      firePropertyChangeEvent(commentEvent);
      firePropertyChangeEvent(new PropertyChangeEventImpl(HAS_COMMENT_PROPERTY, oldValueOfHasComment, hasComment()));
      }

   @Override
   public final boolean hasComment()
      {
      return comment.length() > 0;
      }

   @Override
   public final boolean isCommentVisible()
      {
      return isCommentVisible;
      }

   @Override
   public final void setIsCommentVisible(final boolean isCommentVisible)
      {
      final PropertyChangeEvent event = new PropertyChangeEventImpl(IS_COMMENT_VISIBLE_PROPERTY, this.isCommentVisible, isCommentVisible);
      this.isCommentVisible = isCommentVisible;
      firePropertyChangeEvent(event);
      }

   @Override
   public final void addPropertyChangeEventListener(@Nullable final PropertyChangeEventListener listener)
      {
      if (listener != null)
         {
         listenerLock.lock();
         try
            {
            eventListeners.add(listener);
            }
         finally
            {
            listenerLock.unlock();
            }
         }
      }

   @Override
   public final void addPropertyChangeEventListener(@Nullable final String propertyName, @Nullable final PropertyChangeEventListener listener)
      {
      if (propertyName != null && listener != null)
         {
         listenerLock.lock();
         try
            {
            Set<PropertyChangeEventListener> listeners = propertyToEventListenersMap.get(propertyName);
            if (listeners == null)
               {
               listeners = new HashSet<PropertyChangeEventListener>();
               propertyToEventListenersMap.put(propertyName, listeners);
               }
            listeners.add(listener);
            }
         finally
            {
            listenerLock.unlock();
            }
         }
      }

   @Override
   public final void removePropertyChangeEventListener(@Nullable final PropertyChangeEventListener listener)
      {
      if (listener != null)
         {
         listenerLock.lock();
         try
            {
            eventListeners.remove(listener);
            }
         finally
            {
            listenerLock.unlock();
            }
         }
      }

   @Override
   public final void removePropertyChangeEventListener(@Nullable final String propertyName, @Nullable final PropertyChangeEventListener listener)
      {
      if (propertyName != null && listener != null)
         {
         listenerLock.lock();
         try
            {
            final Set<PropertyChangeEventListener> listeners = propertyToEventListenersMap.get(propertyName);
            if (listeners != null)
               {
               listeners.remove(listener);
               }
            }
         finally
            {
            listenerLock.unlock();
            }
         }
      }

   protected final void firePropertyChangeEvent(@NotNull final PropertyChangeEvent event)
      {
      final Set<PropertyChangeEventListener> listenersToNotify = new HashSet<PropertyChangeEventListener>();

      // first build a Set of listeners by joining those who listen to all properties and those who listen to this
      // particular property.
      listenerLock.lock();
      try
         {
         listenersToNotify.addAll(eventListeners);
         final Set<PropertyChangeEventListener> listenersToThisProperty = propertyToEventListenersMap.get(event.getPropertyName());
         if (listenersToThisProperty != null)
            {
            listenersToNotify.addAll(listenersToThisProperty);
            }
         }
      finally
         {
         listenerLock.unlock();
         }

      // notify the listeners
      if (!listenersToNotify.isEmpty())
         {
         if (LOG.isTraceEnabled())
            {
            LOG.trace("BaseProgramElementModel.firePropertyChangeEvent(" + event.getPropertyName() + "," + event + "): publishing to [" + listenersToNotify.size() + "] listeners");
            }
         executorService.execute(
               new Runnable()
               {
               public void run()
                  {
                  for (final PropertyChangeEventListener listener : listenersToNotify)
                     {
                     listener.handlePropertyChange(event);
                     }
                  }
               });
         }
      }

   @Override
   public String toString()
      {
      final StringBuilder sb = new StringBuilder();
      sb.append("BaseProgramElementModel");
      sb.append("{name='").append(getName()).append('\'');
      sb.append('}');
      return sb.toString();
      }

   protected final class PropertyChangeEventImpl implements PropertyChangeEvent
      {
      private final String propertyName;
      private final Object oldValue;
      private final Object newValue;

      PropertyChangeEventImpl(@NotNull final String propertyName,
                              @Nullable final Object oldValue,
                              @Nullable final Object newValue)
         {
         this.propertyName = propertyName;
         this.oldValue = oldValue;
         this.newValue = newValue;
         }

      @Override
      @NotNull
      public String getPropertyName()
         {
         return propertyName;
         }

      @Override
      @Nullable
      public Object getOldValue()
         {
         return oldValue;
         }

      @Override
      @Nullable
      public Object getNewValue()
         {
         return newValue;
         }

      @Override
      public String toString()
         {
         final StringBuilder sb = new StringBuilder();
         sb.append("PropertyChangeEventImpl");
         sb.append("{propertyName='").append(propertyName).append('\'');
         sb.append(", oldValue=").append(oldValue);
         sb.append(", newValue=").append(newValue);
         sb.append('}');
         return sb.toString();
         }
      }
   }
