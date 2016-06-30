package edu.cmu.ri.createlab.sequencebuilder.programelement.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import edu.cmu.ri.createlab.sequencebuilder.ContainerModel;
import edu.cmu.ri.createlab.sequencebuilder.SequenceActionListener;
import edu.cmu.ri.createlab.util.thread.DaemonThreadFactory;
import edu.cmu.ri.createlab.visualprogrammer.VisualProgrammerDevice;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jdom.Attribute;
import org.jdom.CDATA;
import org.jdom.DataConversionException;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
abstract class BaseProgramElementModel<ModelClass extends ProgramElementModel> implements ProgramElementModel<ModelClass>
   {
   private static final Logger LOG = Logger.getLogger(BaseProgramElementModel.class);
   private static final String XML_ELEMENT_NAME_COMMENT = "comment";
   private static final String XML_ATTRIBUTE_COMMENT_IS_VISIBLE = "is-visible";
   protected SequenceActionListener listener;
   protected ContainerModel parent;

   @Override
   public void setParent(@NotNull ContainerModel p)
      {
      this.parent = p;
      this.listener = p.getActionListener();
      }

   private static Element getCommentElement(@NotNull final Element commentParentElement)
      {
      return commentParentElement.getChild(XML_ELEMENT_NAME_COMMENT);
      }

   protected static boolean getBooleanAttributeValue(@Nullable final Element element, @Nullable final String attributeName)
      {
      boolean b = false;
      if (element != null && attributeName != null)
         {
         try
            {
            final Attribute attribute = element.getAttribute(attributeName);
            if (attribute != null)
               {
               b = attribute.getBooleanValue();
               }
            }
         catch (DataConversionException ignored)
            {
            if (LOG.isEnabledFor(Level.WARN))
               {
               LOG.warn("BaseProgramElementModel.getBooleanAttributeValue(): Could not convert value of attribute [" + attributeName + "] to a boolean.  Using [" + b + "] instead.");
               }
            }
         }
      return b;
      }

   protected static String getValue(@Nullable final Element element, @Nullable final String attributeName, final String defaultValue)
      {
      String s = defaultValue;
      if (element != null && attributeName != null)
         {
         final Attribute attribute = element.getAttribute(attributeName);
         if (attribute != null)
            {
            s = attribute.getValue();
            }
         }
      return s;
      }

   protected static int getIntAttributeValue(@Nullable final Element element, @Nullable final String attributeName, final int defaultValue)
      {
      int i = defaultValue;
      if (element != null && attributeName != null)
         {
         try
            {
            final Attribute attribute = element.getAttribute(attributeName);
            if (attribute != null)
               {
               i = attribute.getIntValue();
               }
            }
         catch (DataConversionException ignored)
            {
            if (LOG.isEnabledFor(Level.WARN))
               {
               LOG.warn("BaseProgramElementModel.getIntAttributeValue(): Could not convert value of attribute [" + attributeName + "] to an int.  Using [" + i + "] instead.");
               }
            }
         }
      return i;
      }

   @Nullable
   protected static String getCommentFromParentXmlElement(@NotNull final Element commentParentElement)
      {
      final Element commentElement = getCommentElement(commentParentElement);
      if (commentElement != null)
         {
         return commentElement.getText();
         }
      return null;
      }

   protected static boolean getIsCommentVisibleFromParentXmlElement(@NotNull final Element commentParentElement)
      {
      return getBooleanAttributeValue(getCommentElement(commentParentElement), XML_ATTRIBUTE_COMMENT_IS_VISIBLE);
      }

   private final UUID uuid = UUID.randomUUID();

   @NotNull
   protected final VisualProgrammerDevice visualProgrammerDevice;

   @NotNull
   private String comment = "";
   private boolean isCommentVisible = false;

   private Set<PropertyChangeEventListener> eventListeners = new HashSet<PropertyChangeEventListener>();
   private Map<String, Set<PropertyChangeEventListener>> propertyToEventListenersMap = new HashMap<String, Set<PropertyChangeEventListener>>();
   private ExecutorService executorService = Executors.newCachedThreadPool(new DaemonThreadFactory(this.getClass().getSimpleName()));
   private final Lock listenerLock = new ReentrantLock();

   protected BaseProgramElementModel(@NotNull final VisualProgrammerDevice visualProgrammerDevice,
                                     @Nullable final String comment,
                                     final boolean isCommentVisible)
      {
      this.visualProgrammerDevice = visualProgrammerDevice;
      this.comment = comment == null ? "" : comment;
      this.isCommentVisible = isCommentVisible;
      }

   @Override
   @NotNull
   public final UUID getUuid()
      {
      return uuid;
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
      final Element element = new Element(XML_ELEMENT_NAME_COMMENT);
      element.setAttribute(XML_ATTRIBUTE_COMMENT_IS_VISIBLE, String.valueOf(isCommentVisible));
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

   /**
    * This implementation of {@link Object#equals(Object)} only compares the {@link #getUuid() UUID} of a
    * {@link BaseProgramElementModel} since we require that all views be unique.
    */
   @Override
   public final boolean equals(final Object o)
      {
      if (this == o)
         {
         return true;
         }
      if (o == null || getClass() != o.getClass())
         {
         return false;
         }

      final BaseProgramElementModel that = (BaseProgramElementModel)o;

      return !(uuid != null ? !uuid.equals(that.uuid) : that.uuid != null);
      }

   /**
    * This implementation of {@link Object#hashCode()} only compares the {@link #getUuid() UUID} of a
    * {@link BaseProgramElementModel} since we require that all views be unique.
    */
   @Override
   public final int hashCode()
      {
      return uuid != null ? uuid.hashCode() : 0;
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
