package edu.cmu.ri.createlab.sequencebuilder.programelement.model;

import java.io.File;
import java.io.IOException;
import java.security.Provider;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import javax.swing.*;

import edu.cmu.ri.createlab.expressionbuilder.controlpanel.ServiceControlPanelDevice;
import edu.cmu.ri.createlab.sequencebuilder.ExpressionExecutor;
import edu.cmu.ri.createlab.sequencebuilder.ExpressionServiceIconView;
import edu.cmu.ri.createlab.sequencebuilder.SequenceExecutor;
import edu.cmu.ri.createlab.terk.expression.XmlDevice;
import edu.cmu.ri.createlab.terk.expression.XmlExpression;
import edu.cmu.ri.createlab.terk.expression.XmlOperation;
import edu.cmu.ri.createlab.terk.expression.XmlService;
import edu.cmu.ri.createlab.terk.services.DeviceController;
import edu.cmu.ri.createlab.terk.services.Service;
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
                                                      @Nullable final Element element)
      {
      if (element != null)
         {
         LOG.debug("ExpressionModel.createFromXmlElement(): " + element);

         final String filename = element.getAttributeValue(XML_ATTRIBUTE_FILE);
         final File file = new File(PathManager.getInstance().getExpressionsDirectory(), filename);
         if (file.exists())
            {
            return new ExpressionModel(visualProgrammerDevice,
                                       file,
                                       getCommentFromParentXmlElement(element),
                                       getIsCommentVisibleFromParentXmlElement(element),
                                       getIntAttributeValue(element, XML_ATTRIBUTE_DELAY_IN_MILLIS, 0));
            }
         else
            {
            if (LOG.isEnabledFor(Level.WARN))
               {
               LOG.warn("ExpressionModel.createFromXmlElement(): Expression file [" + file + "] does not exist.  Returning null.");
               }
            }
         }
      return null;
      }

   private final File expressionFile;
   private final XmlExpression xmlExpression;
   private int delayInMillis;
   private final Set<ExecutionEventListener> executionEventListeners = new HashSet<ExecutionEventListener>();
   private ExpressionServiceIconView iconView;
   /**
    * Creates an <code>ExpressionModel</code> for the given <code>expressionFile</code> with an empty hidden comment and
    * no delay.
    */
   public ExpressionModel(@NotNull final VisualProgrammerDevice visualProgrammerDevice,
                          @NotNull final File expressionFile)
      {
      this(visualProgrammerDevice, expressionFile, null, false, DEFAULT_DELAY_VALUE_IN_MILLIS);
      }

   /**
    * Creates an <code>ExpressionModel</code> for the given <code>expressionFile</code> with the given
    * <code>comment</code> given <code>delayInMillis</code>. This constructor ensures that the delay is within the range
    * <code>[{@link #MIN_DELAY_VALUE_IN_MILLIS}, {@link #MAX_DELAY_VALUE_IN_MILLIS}]</code>.
    */
   public ExpressionModel(@NotNull final VisualProgrammerDevice visualProgrammerDevice,
                          @NotNull final File expressionFile,
                          @Nullable final String comment,
                          final boolean isCommentVisible,
                          final int delayInMillis)
      {
      super(visualProgrammerDevice, comment, isCommentVisible);
      this.expressionFile = expressionFile;
      this.delayInMillis = cleanDelayInMillis(delayInMillis);
      try
         {
         this.xmlExpression = XmlExpression.create(expressionFile);
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

      //TODO This iconView is only called when the expression model (and subsequent views) are created
      //Todo... an update-able format would be preferable, some runnable function to be called in the SwingThread
      this.iconView = new ExpressionServiceIconView(this.xmlExpression.getServices(),visualProgrammerDevice.getServiceManager());


      }

   /** Copy constructor */
   private ExpressionModel(@NotNull final ExpressionModel originalExpressionModel)
      {
      this(originalExpressionModel.getVisualProgrammerDevice(),
           originalExpressionModel.getExpressionFile(),
           originalExpressionModel.getComment(),
           originalExpressionModel.isCommentVisible(),
           originalExpressionModel.getDelayInMillis());
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

   public JPanel getIconBlockView()
   {
       this.iconView = new ExpressionServiceIconView(this.xmlExpression.getServices(),visualProgrammerDevice.getServiceManager());
       //LOG.debug("Creating Icon Block");
       return iconView.createBlockIcons();
   }

   public String getElementType(){
           return XML_ELEMENT_NAME;
   }

   /** Returns the expression's file name, without the .xml extension. */
   @Override
   @NotNull
   public String getName()
      {
      // get the filename, but strip off any .xml extension
      String fileName = expressionFile.getName();
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
      element.setAttribute(XML_ATTRIBUTE_FILE, expressionFile.getName());
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

   public File getExpressionFile()
      {
      return expressionFile;
      }

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
