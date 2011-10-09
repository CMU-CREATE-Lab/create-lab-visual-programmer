package edu.cmu.ri.createlab.sequencebuilder.programelement.model;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import edu.cmu.ri.createlab.sequencebuilder.ContainerModel;
import edu.cmu.ri.createlab.sequencebuilder.SequenceExecutor;
import edu.cmu.ri.createlab.visualprogrammer.VisualProgrammerDevice;
import org.apache.log4j.Logger;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * <p>
 * <code>CounterLoopModel</code> is the {@link ProgramElementModel} for a counter loop.
 * </p>
 *
 * @author Chris Bartley (bartley@cmu.edu)
 */
public final class CounterLoopModel extends BaseProgramElementModel<CounterLoopModel>
   {
   public interface ExecutionEventListener
      {
      void handleExecutionStart();

      void handleElapsedIterations(final int elapsedIterations);

      void handleExecutionEnd();
      }

   private static final Logger LOG = Logger.getLogger(CounterLoopModel.class);

   public static final String NUMBER_OF_ITERATIONS_PROPERTY = "numberOfIterations";
   public static final int MIN_NUMBER_OF_ITERATIONS = 1;
   public static final int MAX_NUMBER_OF_ITERATIONS = 999999;
   public static final String XML_ELEMENT_NAME = "counter-loop";
   private static final String XML_ATTRIBUTE_NUMBER_OF_ITERATIONS = "iterations";

   @Nullable
   public static CounterLoopModel createFromXmlElement(@NotNull final VisualProgrammerDevice visualProgrammerDevice,
                                                       @Nullable final Element element)
      {
      if (element != null)
         {
         LOG.debug("CounterLoopModel.createFromXmlElement(): " + element);

         // create and populate the container
         final ContainerModel theContainerModel = new ContainerModel();
         theContainerModel.load(visualProgrammerDevice, element.getChild(ContainerModel.XML_ELEMENT_NAME));

         return new CounterLoopModel(visualProgrammerDevice,
                                     getCommentFromParentXmlElement(element),
                                     getIsCommentVisibleFromParentXmlElement(element),
                                     getIntAttributeValue(element, XML_ATTRIBUTE_NUMBER_OF_ITERATIONS, MIN_NUMBER_OF_ITERATIONS),
                                     theContainerModel);
         }
      return null;
      }

   private int numberOfIterations = MIN_NUMBER_OF_ITERATIONS;
   private final ContainerModel containerModel;
   private final Set<ExecutionEventListener> executionEventListeners = new HashSet<ExecutionEventListener>();

   /** Creates a <code>CounterLoopModel</code> with an empty hidden comment and 1 iteration. */
   public CounterLoopModel(@NotNull final VisualProgrammerDevice visualProgrammerDevice)
      {
      this(visualProgrammerDevice, null, false, 1, new ContainerModel());
      }

   /**
    * Creates a <code>CounterLoopModel</code> with the given <code>comment</code>.  This constructor ensures that the
    * value is within the range <code>[{@link #MIN_NUMBER_OF_ITERATIONS}, {@link #MAX_NUMBER_OF_ITERATIONS}]</code>.
    */
   public CounterLoopModel(@NotNull final VisualProgrammerDevice visualProgrammerDevice,
                           @Nullable final String comment,
                           final boolean isCommentVisible,
                           final int numberOfIterations,
                           @NotNull final ContainerModel containerModel)
      {
      super(visualProgrammerDevice, comment, isCommentVisible);
      this.numberOfIterations = cleanNumberOfIterations(numberOfIterations);
      this.containerModel = containerModel;
      }

   /** Copy construtor */
   private CounterLoopModel(@NotNull final CounterLoopModel originalCounterLoopModel)
      {
      this(originalCounterLoopModel.getVisualProgrammerDevice(),
           originalCounterLoopModel.getComment(),
           originalCounterLoopModel.isCommentVisible(),
           originalCounterLoopModel.getNumberOfIterations(),
           new ContainerModel());   // we DON'T want to share the container model!
      }

   public void addExecutionEventListener(@Nullable final ExecutionEventListener listener)
      {
      if (listener != null)
         {
         executionEventListeners.add(listener);
         }
      }

   public void removeExecutionEventListener(@Nullable final ExecutionEventListener listener)
      {
      if (listener != null)
         {
         executionEventListeners.remove(listener);
         }
      }

   public String getElementType(){
           return XML_ELEMENT_NAME;
   }

   @Override
   @NotNull
   public String getName()
      {
      return this.getClass().getSimpleName();
      }

   @Override
   public boolean isContainer()
      {
      return true;
      }

   @Override
   @NotNull
   public CounterLoopModel createCopy()
      {
      return new CounterLoopModel(this);
      }

   @NotNull
   @Override
   public Element toElement()
      {
      final Element element = new Element(XML_ELEMENT_NAME);
      element.setAttribute(XML_ATTRIBUTE_NUMBER_OF_ITERATIONS, String.valueOf(numberOfIterations));
      element.addContent(getCommentAsElement());
      element.addContent(containerModel.toElement());

      return element;
      }

   @Override
   public void execute()
      {
      LOG.debug("CounterLoopModel.execute()");
      if (SequenceExecutor.getInstance().isRunning())
         {
         // notify listeners that we're about to begin
         for (final ExecutionEventListener listener : executionEventListeners)
            {
            listener.handleExecutionStart();
            }

         for (int i = 1; i <= numberOfIterations; i++)
            {
            if (!SequenceExecutor.getInstance().isRunning())
               {
               break;
               }

            if (LOG.isDebugEnabled())
               {
               LOG.debug("CounterLoopModel.execute(): iteration " + i);
               }

            // iterate over the models and execute them
            final List<ProgramElementModel> programElementModels = containerModel.getAsList();
            for (final ProgramElementModel model : programElementModels)
               {
               model.execute();
               }

            // notify listeners that we just completed the ith iteration
            for (final ExecutionEventListener listener : executionEventListeners)
               {
               listener.handleElapsedIterations(i);
               }
            }

         // notify listeners that we're done
         for (final ExecutionEventListener listener : executionEventListeners)
            {
            listener.handleExecutionEnd();
            }
         }
      }

   public int getNumberOfIterations()
      {
      return numberOfIterations;
      }

   /**
    * Sets the delay in milliseconds, and causes a {@link PropertyChangeEvent} to be fired for the
    * {@link #NUMBER_OF_ITERATIONS_PROPERTY} property.  This method ensures that the value is within the range
    * <code>[{@link #MIN_NUMBER_OF_ITERATIONS}, {@link #MAX_NUMBER_OF_ITERATIONS}]</code>.
    */
   public void setNumberOfIterations(final int numberOfIterations)
      {
      final int cleanedNumberOfIterations = cleanNumberOfIterations(numberOfIterations);
      final PropertyChangeEvent event = new PropertyChangeEventImpl(NUMBER_OF_ITERATIONS_PROPERTY, this.numberOfIterations, cleanedNumberOfIterations);
      this.numberOfIterations = cleanedNumberOfIterations;
      firePropertyChangeEvent(event);
      }

   private int cleanNumberOfIterations(final int numberOfIterations)
      {
      int cleanedNumberOfIterations = numberOfIterations;
      if (numberOfIterations < MIN_NUMBER_OF_ITERATIONS)
         {
         cleanedNumberOfIterations = MIN_NUMBER_OF_ITERATIONS;
         }
      else if (numberOfIterations > MAX_NUMBER_OF_ITERATIONS)
         {
         cleanedNumberOfIterations = MAX_NUMBER_OF_ITERATIONS;
         }
      return cleanedNumberOfIterations;
      }

   public ContainerModel getContainerModel()
      {
      return containerModel;
      }
   }
