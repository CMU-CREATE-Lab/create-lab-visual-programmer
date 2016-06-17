package edu.cmu.ri.createlab.sequencebuilder.programelement.model;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import javax.swing.JPanel;
import javax.swing.SwingWorker;
import edu.cmu.ri.createlab.sequencebuilder.ContainerModel;
import edu.cmu.ri.createlab.sequencebuilder.ElementLocation;
import edu.cmu.ri.createlab.sequencebuilder.ExpressionExecutor;
import edu.cmu.ri.createlab.sequencebuilder.ExpressionServiceIconView;
import edu.cmu.ri.createlab.sequencebuilder.SequenceAction;
import edu.cmu.ri.createlab.sequencebuilder.SequenceActionListener;
import edu.cmu.ri.createlab.sequencebuilder.SequenceExecutor;
import edu.cmu.ri.createlab.terk.expression.XmlExpression;
import edu.cmu.ri.createlab.terk.services.ServiceManager;
import edu.cmu.ri.createlab.visualprogrammer.PathManager;
import edu.cmu.ri.createlab.visualprogrammer.VisualProgrammerDevice;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * <p>
 * <code>ExpressionModel</code> is the {@link ProgramElementModel} for an expression.
 * </p>
 *
 * @author Chris Bartley (bartley@cmu.edu)
 */
public final class ExpressionModel extends BaseProgramElementModel<ExpressionModel>
   {
   public interface ExecutionEventListener
      {
      void handleExecutionStart();

      void handleElapsedTimeInMillis(final int millis);

      void handleExecutionEnd();
      }

   public interface RefreshEventListener
      {
      void handleRefresh();
      }

   private static final Logger LOG = Logger.getLogger(ExpressionModel.class);

   public static final String DELAY_IN_MILLIS_PROPERTY = "delayInMillis";
   public static final float MIN_DELAY_VALUE_IN_SECS = 0;
   public static final float MAX_DELAY_VALUE_IN_SECS = 999.99f;
   public static final float DEFAULT_DELAY_VALUE_IN_SECS = 1;
   public static final int MIN_DELAY_VALUE_IN_MILLIS = (int)(MIN_DELAY_VALUE_IN_SECS * 1000);
   public static final int MAX_DELAY_VALUE_IN_MILLIS = (int)(MAX_DELAY_VALUE_IN_SECS * 1000);
   public static final int DEFAULT_DELAY_VALUE_IN_MILLIS = (int)(DEFAULT_DELAY_VALUE_IN_SECS * 1000);
   public static final String XML_ELEMENT_NAME = "expression";
   private static final String XML_ATTRIBUTE_FILE = "file";
   private static final String XML_ATTRIBUTE_DELAY_IN_MILLIS = "delay-in-millis";

   @Nullable
   public static ExpressionModel createFromXmlElement(@NotNull final VisualProgrammerDevice visualProgrammerDevice,
                                                      @Nullable final Element element, final ContainerModel parent)
      {
      if (element != null)
         {
         LOG.debug("ExpressionModel.createFromXmlElement(): " + element);

         final String filename = element.getAttributeValue(XML_ATTRIBUTE_FILE);
         if (PathManager.getInstance().getExpressionsZipSave().exist(filename))
            {
            return new ExpressionModel(visualProgrammerDevice,
                                       filename,
                                       getCommentFromParentXmlElement(element),
                                       getIsCommentVisibleFromParentXmlElement(element),
                                       getIntAttributeValue(element, XML_ATTRIBUTE_DELAY_IN_MILLIS, 0),
                                       parent);
            }
         else
            {
            if (LOG.isEnabledFor(Level.WARN))
               {
               LOG.warn("ExpressionModel.createFromXmlElement(): Expression file [" + filename + "] does not exist.  Returning null.");
               }
            }
         }
      return null;
      }

   private final String expressionFileName;
   private XmlExpression xmlExpression;
   private int delayInMillis;
   private final Set<ExecutionEventListener> executionEventListeners = new HashSet<ExecutionEventListener>();
   private final Set<RefreshEventListener> refreshEventListeners = new HashSet<RefreshEventListener>();


   /**
    * Creates an <code>ExpressionModel</code> for the given <code>expressionFile</code> with an empty hidden comment and
    * no delay.
    */
   public ExpressionModel(@NotNull final VisualProgrammerDevice visualProgrammerDevice,
                          @NotNull final String expressionFileName, final ContainerModel parent)
      {
      this(visualProgrammerDevice, expressionFileName, null, false, DEFAULT_DELAY_VALUE_IN_MILLIS, parent);
      }

   /**
    * Creates an <code>ExpressionModel</code> for the given <code>expressionFile</code> with the given
    * <code>comment</code> given <code>delayInMillis</code>. This constructor ensures that the delay is within the range
    * <code>[{@link #MIN_DELAY_VALUE_IN_MILLIS}, {@link #MAX_DELAY_VALUE_IN_MILLIS}]</code>.
    */
   public ExpressionModel(@NotNull final VisualProgrammerDevice visualProgrammerDevice,
                          //  @NotNull final File expressionFile,
                          @NotNull final String expressionFileName,
                          @Nullable final String comment,
                          final boolean isCommentVisible,
                          final int delayInMillis, final ContainerModel parent)
      {
      super(visualProgrammerDevice, comment, isCommentVisible);

      this.expressionFileName = expressionFileName;
      this.delayInMillis = cleanDelayInMillis(delayInMillis);
      this.xmlExpression = loadXmlExpression(expressionFileName);
      this.parent = parent;
      }

   private XmlExpression loadXmlExpression(@NotNull final String expressionFileName)
      {
      try
         {
         //--->
         return XmlExpression.create(PathManager.getInstance().getExpressionsZipSave().getFile_InputStream(expressionFileName));
         }
      catch (IOException e)
         {
         LOG.error("IOException while trying to create the XmlExpression, rethrowing as an IllegalArgumentException", e);
         throw new IllegalArgumentException("IOException while trying to create the XmlExpression", e);
         }
      catch (JDOMException e)
         {
         LOG.error("JDOMException while trying to create the XmlExpression, rethrowing as an IllegalArgumentException", e);
         throw new IllegalArgumentException("JDOMException while trying to create the XmlExpression", e);
         }
      }

   /** Copy constructor */
   private ExpressionModel(@NotNull final ExpressionModel originalExpressionModel)
      {
      this(originalExpressionModel.getVisualProgrammerDevice(),
           originalExpressionModel.getExpressionFileName(),
           originalExpressionModel.getComment(),
           originalExpressionModel.isCommentVisible(),
           originalExpressionModel.getDelayInMillis(),
           originalExpressionModel.parent);
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

   public void addRefreshEventListener(@Nullable final RefreshEventListener listener)
      {
      if (listener != null)
         {
         refreshEventListeners.add(listener);
         }
      }

   public void removeRefreshEventListener(@Nullable final RefreshEventListener listener)
      {
      if (listener != null)
         {
         refreshEventListeners.remove(listener);
         }
      }

   public JPanel getIconBlockView()
      {
      final ExpressionServiceIconView serviceIconView = visualProgrammerDevice.getSequenceBuilderDevice().getExpressionServiceIconView();
      return serviceIconView.createBlockIcons(this.xmlExpression.getServices(), visualProgrammerDevice.getServiceManager());
      }

   public String getElementType()
      {
      return XML_ELEMENT_NAME;
      }

   @Override
   public boolean containsFork()
      {
      return false;
      }

   @Override
   public ContainerModel getParentContainer()
      {
      return parent;
      }

   /** Returns the expression's file name, without the .xml extension. */
   @Override
   @NotNull
   public String getName()
      {
      // get the filename, but strip off any .xml extension
      String fileName = expressionFileName;
      if (fileName.toLowerCase().lastIndexOf(".xml") != -1)
         {
         fileName = fileName.substring(0, fileName.lastIndexOf('.'));
         }

      return fileName;
      }

   @Override
   public boolean isContainer()
      {
      return false;
      }

   @Override
   @NotNull
   public ExpressionModel createCopy()
      {
      return new ExpressionModel(this);
      }

   @NotNull
   @Override
   public Element toElement()
      {
      final Element element = new Element(XML_ELEMENT_NAME);
      element.setAttribute(XML_ATTRIBUTE_FILE, expressionFileName);
      element.setAttribute(XML_ATTRIBUTE_DELAY_IN_MILLIS, String.valueOf(delayInMillis));
      element.addContent(getCommentAsElement());

      return element;
      }

   @Override
   public void execute()
      {
      if (LOG.isDebugEnabled())
         {
         LOG.debug("ExpressionModel.execute(): executing [" + this + "]");
         }

      if (SequenceExecutor.getInstance().isRunning())
         {
         final ServiceManager serviceManager = getVisualProgrammerDevice().getServiceManager();

         // create the swing worker
         final long startTime = System.currentTimeMillis();
         final long endTime = startTime + delayInMillis;
         final SwingWorker<Object, Integer> sw =
               new SwingWorker<Object, Integer>()
               {

               @Override
               protected Boolean doInBackground() throws Exception
                  {
                  // execute the expression (asynchronously)
                  ExpressionExecutor.getInstance().executeAsynchronously(serviceManager, ExpressionModel.this);

                  long currentTime;
                  do
                     {
                     currentTime = System.currentTimeMillis();
                     final int elapsedMillis = (int)(currentTime - startTime);
                     publish(elapsedMillis);
                     try
                        {
                        // TODO: not thrilled about the busy-wait here...
                        Thread.sleep(50);
                        }
                     catch (InterruptedException ignored)
                        {
                        LOG.error("ExpressionModel.execute().doInBackground(): InterruptedException while sleeping");
                        }
                     }
                  while (currentTime < endTime && !isCancelled() && SequenceExecutor.getInstance().isRunning());

                  return null;
                  }

               @Override
               protected void process(final List<Integer> integers)
                  {
                  // just publish the latest update
                  final Integer millis = integers.get(integers.size() - 1);
                  for (final ExecutionEventListener listener : executionEventListeners)
                     {
                     listener.handleElapsedTimeInMillis(millis);
                     }
                  }
               };

         // notify listeners that we're about to begin
         for (final ExecutionEventListener listener : executionEventListeners)
            {
            listener.handleExecutionStart();
            }

         // start the worker and wait for it to finish
         sw.execute();
         try
            {
            sw.get();
            }
         catch (InterruptedException ignored)
            {
            sw.cancel(true);
            LOG.error("ExpressionModel.execute(): InterruptedException while waiting for the SwingWorker to finish");
            }
         catch (ExecutionException ignored)
            {
            sw.cancel(true);
            LOG.error("ExpressionModel.execute(): ExecutionException while waiting for the SwingWorker to finish");
            }
         catch (Exception e)
            {
            sw.cancel(true);
            LOG.error("ExpressionModel.execute(): Exception while waiting for the SwingWorker to finish", e);
            }

         // notify listeners that we're done
         for (final ExecutionEventListener listener : executionEventListeners)
            {
            listener.handleExecutionEnd();
            }
         }
      }

   @Override
   public void refresh()
      {
      LOG.debug("ExpressionModel.refresh(): refreshing " + getName());
      this.xmlExpression = loadXmlExpression(expressionFileName);
      // notify listeners that we're done
      for (final RefreshEventListener listener : refreshEventListeners)
         {
         listener.handleRefresh();
         }
      }

   public String getExpressionFileName()
      {
      return expressionFileName;
      }

/* //---> Ask about this: is needed in the ExpressionListModel as a File!
   public File getExpressionFile()
      {
//---> Change this to return a string
      return PathManager.getInstance().getExpressionsZipSave().getAddedFile(expressionFileName);
      }*/

   public XmlExpression getXmlExpression()
      {
      return xmlExpression;
      }

   public int getDelayInMillis()
      {
      return delayInMillis;
      }

   /**
    * Sets the delay in milliseconds, and causes a {@link PropertyChangeEvent} to be fired for the
    * {@link #DELAY_IN_MILLIS_PROPERTY} property.  This method ensures that the value is within the range
    * <code>[{@link #MIN_DELAY_VALUE_IN_MILLIS}, {@link #MAX_DELAY_VALUE_IN_MILLIS}]</code>.
    */
   public void setDelayInMillis(final int delayInMillis)
      {
      listener.onAction(new SequenceAction(SequenceAction.Type.EXPRESSION_DELAY, new ElementLocation(parent, parent.getAsList().indexOf(this)), this.delayInMillis));
      final int cleanedDelayInMillis = cleanDelayInMillis(delayInMillis);
      final PropertyChangeEvent event = new PropertyChangeEventImpl(DELAY_IN_MILLIS_PROPERTY, this.delayInMillis, cleanedDelayInMillis);
      this.delayInMillis = cleanedDelayInMillis;
      firePropertyChangeEvent(event);
      }

   /**
    * Sets the delay in milliseconds, and causes a {@link PropertyChangeEvent} to be fired for the
    * {@link #DELAY_IN_MILLIS_PROPERTY} property.  This method ensures that the value is within the range
    * <code>[{@link #MIN_DELAY_VALUE_IN_MILLIS}, {@link #MAX_DELAY_VALUE_IN_MILLIS}] This is only called by undo code</code>.
    */
   public void undoSetDelayInMillis(final int delayInMillis)
      {
      final int cleanedDelayInMillis = cleanDelayInMillis(delayInMillis);
      final PropertyChangeEvent event = new PropertyChangeEventImpl(DELAY_IN_MILLIS_PROPERTY, this.delayInMillis, cleanedDelayInMillis);
      this.delayInMillis = cleanedDelayInMillis;
      firePropertyChangeEvent(event);
      }

   private int cleanDelayInMillis(final int delayInMillis)
      {
      int cleanedDelayInMillis = delayInMillis;
      if (delayInMillis < MIN_DELAY_VALUE_IN_MILLIS)
         {
         cleanedDelayInMillis = MIN_DELAY_VALUE_IN_MILLIS;
         }
      else if (delayInMillis > MAX_DELAY_VALUE_IN_MILLIS)
         {
         cleanedDelayInMillis = MAX_DELAY_VALUE_IN_MILLIS;
         }
      return cleanedDelayInMillis;
      }
   }
