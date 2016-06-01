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
 * <code>ForkModel</code> is the {@link ProgramElementModel} for a fork expression.
 * </p>
 *
 * @author Brandon Price (baprice@andrew.cmu.edu)
 */
public final class ForkModel extends BaseProgramElementModel<ForkModel>
   {
   public interface ExecutionEventListener
      {
      void handleExecutionStart();

      void handleExecutionEnd();

      void handleThread1Highlight();

      void handleThread2Highlight();

      void handleResetHightlight();
      }

   private static final Logger LOG = Logger.getLogger(ForkModel.class);
   public static final String XML_ELEMENT_NAME = "fork";
   private static final String XML_ELEMENT_THREAD1 = "thread1";
   private static final String XML_ELEMENT_THREAD2 = "thread2";

   @Nullable
   public static ForkModel createFromXmlElement(@NotNull final VisualProgrammerDevice visualProgrammerDevice,
                                                @Nullable final Element element)
      {
      if (element != null)
         {
         LOG.debug("ForkModel.createFromXmlElement(): " + element);

         // create and populate the containers
         final ContainerModel thread1 = new ContainerModel();
         final ContainerModel thread2 = new ContainerModel();
         thread1.load(visualProgrammerDevice, element.getChild(XML_ELEMENT_THREAD1).getChild(ContainerModel.XML_ELEMENT_NAME));
         thread2.load(visualProgrammerDevice, element.getChild(XML_ELEMENT_THREAD2).getChild(ContainerModel.XML_ELEMENT_NAME));

         return new ForkModel(visualProgrammerDevice,
                              getCommentFromParentXmlElement(element),
                              getIsCommentVisibleFromParentXmlElement(element),
                              thread1,
                              thread2);
         }
      return null;
      }

   private final ContainerModel thread1ContainerModel;
   private final ContainerModel thread2ContainerModel;
   private final Set<ExecutionEventListener> executionEventListeners = new HashSet<ExecutionEventListener>();

   /**
    * Creates a <code>ForkModel</code> with an empty hidden comment, a <code>selectedSensor</code>
    * and <code>false</code> for both booleans which control reevaluation of the conditional after branch completion.
    */
   public ForkModel(@NotNull final VisualProgrammerDevice visualProgrammerDevice)
      {
      this(visualProgrammerDevice, null, false, new ContainerModel(), new ContainerModel());
      }

   /** Creates a <code>ForkModel</code> with the given <code>comment</code>. */
   public ForkModel(@NotNull final VisualProgrammerDevice visualProgrammerDevice,
                    @Nullable final String comment,
                    final boolean isCommentVisible,
                    @NotNull final ContainerModel thread1ContainerModel,
                    @NotNull final ContainerModel thread2ContainerModel)
      {
      super(visualProgrammerDevice, comment, isCommentVisible);
      this.thread1ContainerModel = thread1ContainerModel;
      this.thread2ContainerModel = thread2ContainerModel;
      }

   /** Copy construtor */
   private ForkModel(@NotNull final ForkModel originalForkModel)
      {
      this(originalForkModel.getVisualProgrammerDevice(),
           originalForkModel.getComment(),
           originalForkModel.isCommentVisible(),
           new ContainerModel(),
           new ContainerModel());   // we DON'T want to share the container models!
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

   public String getElementType()
      {
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

   @NotNull
   @Override
   public ForkModel createCopy()
      {
      return new ForkModel(this);
      }

   @NotNull
   @Override
   public Element toElement()
      {
      final Element thread1Element = new Element(XML_ELEMENT_THREAD1);
      thread1Element.addContent(thread1ContainerModel.toElement());

      final Element thread2Element = new Element(XML_ELEMENT_THREAD2);
      thread2Element.addContent(thread2ContainerModel.toElement());

      final Element element = new Element(XML_ELEMENT_NAME);
      element.addContent(getCommentAsElement());
      element.addContent(thread1Element);
      element.addContent(thread2Element);

      return element;
      }

   @Override
   public void execute()
      {
      LOG.debug("ForkModel.execute()");
      if (SequenceExecutor.getInstance().isRunning())
         {
         // notify listeners that we're about to begin
         for (final ExecutionEventListener listener : executionEventListeners)
            {
            listener.handleExecutionStart();
            }

         thread1ContainerModel.resetProgressBarsForExecution();
         thread2ContainerModel.resetProgressBarsForExecution();

         Thread t1 = new Thread(new Runnable()
            {
            @Override
            public void run()
               {
               final List<ProgramElementModel> programElementModels = thread1ContainerModel.getAsList();
               for (final ProgramElementModel model : programElementModels)
                  {
                  model.execute();
                  }
               }
            });
         Thread t2 = new Thread(new Runnable()
            {
            @Override
            public void run()
               {
               final List<ProgramElementModel> programElementModels = thread2ContainerModel.getAsList();
               for (final ProgramElementModel model : programElementModels)
                  {
                  model.execute();
                  }
               }
            });

         t1.start();
         t2.start();
         while (t1.isAlive() || t2.isAlive());
         }

      // notify listeners that we're done
      for (final ExecutionEventListener listener : executionEventListeners)
         {
         listener.handleExecutionEnd();
         }
      }

   @Override
   public void refresh()
      {
      LOG.debug("ForkModel.refresh(): refreshing " + getName() + " thread1");
      thread1ContainerModel.refresh();
      LOG.debug("ForkModel.refresh(): refreshing " + getName() + " thread2");
      thread2ContainerModel.refresh();
      }

   public ContainerModel getThread1ContainerModel()
      {
      return thread1ContainerModel;
      }

   public ContainerModel getThread2ContainerModel()
      {
      return thread2ContainerModel;
      }
   }

