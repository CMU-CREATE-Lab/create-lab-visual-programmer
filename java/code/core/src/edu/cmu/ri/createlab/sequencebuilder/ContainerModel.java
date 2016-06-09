package edu.cmu.ri.createlab.sequencebuilder;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import edu.cmu.ri.createlab.collections.UniqueNodeLinkedList;
import edu.cmu.ri.createlab.sequencebuilder.programelement.model.CounterLoopModel;
import edu.cmu.ri.createlab.sequencebuilder.programelement.model.ExpressionModel;
import edu.cmu.ri.createlab.sequencebuilder.programelement.model.ForkModel;
import edu.cmu.ri.createlab.sequencebuilder.programelement.model.LoopableConditionalModel;
import edu.cmu.ri.createlab.sequencebuilder.programelement.model.ProgramElementModel;
import edu.cmu.ri.createlab.sequencebuilder.programelement.model.SavedSequenceModel;
import edu.cmu.ri.createlab.util.thread.DaemonThreadFactory;
import edu.cmu.ri.createlab.visualprogrammer.VisualProgrammerDevice;
import edu.cmu.ri.createlab.xml.XmlHelper;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public final class ContainerModel
   {
   private static final Logger LOG = Logger.getLogger(ContainerModel.class);
   public static final String XML_ELEMENT_NAME = "program-element-container";

   public interface EventListener
      {
      /** Called when the given {@link ProgramElementModel} is added to the container. */
      void handleElementAddedEvent(@NotNull final ProgramElementModel programElementModel);

      /** Called when the given {@link ProgramElementModel} is removed from the container. */
      void handleElementRemovedEvent(@NotNull final ProgramElementModel programElementModel);

      /** Called when the {@link ContainerModel#removeAll()} is called. */
      void handleRemoveAllEvent();

      void handleResetAllProgressBarsForExecution();
      }

   private final UniqueNodeLinkedList<ProgramElementModel> list = new UniqueNodeLinkedList<ProgramElementModel>();

   private final Lock listLock = new ReentrantLock();
   private final Set<EventListener> eventListeners = new HashSet<EventListener>();

   private final ExecutorService executorService = Executors.newCachedThreadPool(new DaemonThreadFactory(this.getClass().getSimpleName()));

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
   public ProgramElementModel getHead()
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
   public ProgramElementModel getTail()
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

   public void resetProgressBarsForExecution()
      {
      if (!eventListeners.isEmpty())
         {
         executorService.execute(
               new Runnable()
                  {
                  public void run()
                     {
                     for (final EventListener listener : eventListeners)
                        {
                        listener.handleResetAllProgressBarsForExecution();
                        }
                     }
                  });
         }
      }

   public boolean containsFork() {
      for(ProgramElementModel pem: list.getAsList()) {
         if (pem.containsFork())
            return true;
      }
      return false;
   }

   public boolean add(@Nullable final ProgramElementModel model)
      {
      boolean result = false;

      if (model != null)
         {
         listLock.lock();  // block until condition holds
         try
            {
            result = list.add(model);
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
                              listener.handleElementAddedEvent(model);
                              }
                           }
                        });
               }
            }
         else
            {
            if (LOG.isInfoEnabled())
               {
               LOG.info("ContainerModel.add(): failed to add ProgramElementModel [" + model + "] to the model.");
               }
            }
         }

      return result;
      }

   public boolean insertBefore(@Nullable final ProgramElementModel newElement, @Nullable final ProgramElementModel existingElement)
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
               LOG.info("ContainerModel.add(): failed to insert ProgramElementModel [" + newElement + "] before [" + existingElement + "] in the model.");
               }
            }
         }

      return result;
      }

   public boolean insertAfter(@Nullable final ProgramElementModel newElement, @Nullable final ProgramElementModel existingElement)
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
               LOG.info("ContainerModel.add(): failed to insert ProgramElementModel [" + newElement + "] after [" + existingElement + "] in the model.");
               }
            }
         }

      return result;
      }

   public boolean remove(@Nullable final ProgramElementModel model)
      {
      boolean result = false;

      if (model != null)
         {
         listLock.lock();  // block until condition holds
         try
            {
            result = list.remove(model);
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
                              listener.handleElementRemovedEvent(model);
                              }
                           }
                        });
               }
            }
         else
            {
            if (LOG.isInfoEnabled())
               {
               LOG.info("ContainerModel.add(): failed to remove ProgramElementModel [" + model + "] from the model.");
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
                        }
                     }
                  });
         }
      }

   @NotNull
   public List<ProgramElementModel> getAsList()
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

   /** Moves an element up one spot in the sequence if possible*/
   public boolean moveUp(ProgramElementModel toMove)
      {
      listLock.lock();
      ProgramElementModel prev = null;
      try
         {
         prev = list.getPrevious(toMove);
         }
      finally
         {
         listLock.unlock();
         }
      boolean result = true;
      if (prev != null)
         {
         result &= remove(toMove);
         result &= insertBefore(toMove, prev);
         }
      return result;
      }

   public boolean moveDown(ProgramElementModel toMove)
      {
      listLock.lock();
      ProgramElementModel next = null;
      try
         {
         next = list.getNext(toMove);
         }
      finally
         {
         listLock.unlock();
         }
      boolean result = true;
      if (next != null)
         {
         result &= remove(toMove);
         result &= insertAfter(toMove, next);
         }
      return result;
      }

   /** Creates an XML element representing this instance. */
   @NotNull
   public Element toElement()
      {
      listLock.lock();  // block until condition holds
      try
         {
         final Element programElementContainerElement = new Element(XML_ELEMENT_NAME);

         final List<ProgramElementModel> models = list.getAsList();
         for (final ProgramElementModel model : models)
            {
            final Element element = model.toElement();
            programElementContainerElement.addContent(element);
            }

         return programElementContainerElement;
         }
      finally
         {
         listLock.unlock();
         }
      }

   public void load(@NotNull final VisualProgrammerDevice visualProgrammerDevice, @NotNull final Element containerXmlElement)
      {
      removeAll();

      for (final Object o : (containerXmlElement.getChildren()))
         {
         final Element programElement = (Element)o;

         final ProgramElementModel model;
         if (ExpressionModel.XML_ELEMENT_NAME.equals(programElement.getName()))
            {
            model = ExpressionModel.createFromXmlElement(visualProgrammerDevice, programElement);
            }
         else if (SavedSequenceModel.XML_ELEMENT_NAME.equals(programElement.getName()))
            {
            model = SavedSequenceModel.createFromXmlElement(visualProgrammerDevice, programElement);
            }
         else if (CounterLoopModel.XML_ELEMENT_NAME.equals(programElement.getName()))
            {
            model = CounterLoopModel.createFromXmlElement(visualProgrammerDevice, programElement);
            }
         else if (LoopableConditionalModel.XML_ELEMENT_NAME.equals(programElement.getName()))
            {
            model = LoopableConditionalModel.createFromXmlElement(visualProgrammerDevice, programElement);
            }
         else if (ForkModel.XML_ELEMENT_NAME.equals(programElement.getName()))
            {
            model = ForkModel.createFromXmlElement(visualProgrammerDevice, programElement);
            }
         else
            {
            model = null;
            if (LOG.isEnabledFor(Level.WARN))
               {
               LOG.warn("ContainerModel.load(): Skipping unexpected program element [" + programElement + "]");
               }
            }

         if (model != null)
            {
            add(model);
            }
         }
      }

   public void load(@NotNull final VisualProgrammerDevice visualProgrammerDevice, @NotNull final Document sequenceXmlDocument)
      {
      if (LOG.isDebugEnabled())
         {
         LOG.debug("ContainerModel.load(): XML = \n" + XmlHelper.writeDocumentToStringFormatted(sequenceXmlDocument));
         }

      final Element rootElement = sequenceXmlDocument.getRootElement();
      if (rootElement != null)
         {
         final Element containerElement = rootElement.getChild(ContainerModel.XML_ELEMENT_NAME);
         if (containerElement != null)
            {
            load(visualProgrammerDevice, containerElement);
            }
         }
      }

   /**
    * Calls
    */
   public void refresh()
      {
      LOG.debug("ContainerModel.refresh(): refreshing ContainerModel");
      listLock.lock();  // block until condition holds
      try
         {
         final List<ProgramElementModel> models = list.getAsList();
         for (final ProgramElementModel model : models)
            {
            model.refresh();
            }
         }
      finally
         {
         listLock.unlock();
         }
      }
   }
