package edu.cmu.ri.createlab.sequencebuilder;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import edu.cmu.ri.createlab.sequencebuilder.programelement.model.ExpressionModel;
import edu.cmu.ri.createlab.terk.expression.XmlExpression;
import edu.cmu.ri.createlab.terk.expression.XmlOperation;
import edu.cmu.ri.createlab.terk.expression.XmlService;
import edu.cmu.ri.createlab.terk.services.OperationExecutor;
import edu.cmu.ri.createlab.terk.services.Service;
import edu.cmu.ri.createlab.terk.services.ServiceManager;
import edu.cmu.ri.createlab.util.thread.DaemonThreadFactory;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

/**
 * <p>
 * <code>ExpressionExecutor</code> executes an {@link ExpressionModel}.
 * </p>
 *
 * @author Alex Styler (styler@cmu.edu)
 * @author Chris Bartley (bartley@cmu.edu)
 */
public final class ExpressionExecutor
   {
   private static final Logger LOG = Logger.getLogger(ExpressionExecutor.class);

   private static final ExpressionExecutor INSTANCE = new ExpressionExecutor();
   private final ExecutorService executorService = Executors.newCachedThreadPool(new DaemonThreadFactory(this.getClass().getSimpleName()));

   public static ExpressionExecutor getInstance()
      {
      return INSTANCE;
      }

   private ExpressionExecutor()
      {
      // private to prevent instantiation
      }

   public void executeAsynchronously(@NotNull final ServiceManager serviceManager, @NotNull final ExpressionModel expressionModel)
      {
      executorService.execute(
            new Runnable()
            {
            @Override
            public void run()
               {
               execute(serviceManager, expressionModel);
               }
            }
      );
      }

   public void execute(@NotNull final ServiceManager serviceManager, @NotNull final ExpressionModel expressionModel)
      {
      final XmlExpression xmlExpression = expressionModel.getXmlExpression();
      if (LOG.isTraceEnabled())
         {
         LOG.trace("ExpressionExecutor.execute(): Executing expression: \n" + xmlExpression.toXmlStringFormatted());
         }

      final Set<XmlService> xmlServices = xmlExpression.getServices();
      if (xmlServices != null)
         {
         for (final XmlService xmlService : xmlServices)
            {
            final Service service = serviceManager.getServiceByTypeId(xmlService.getTypeId());
            if (service != null)
               {
               final List<XmlOperation> operations = xmlService.getOperations();
               for (final XmlOperation operation : operations)
                  {
                  if (service instanceof OperationExecutor)
                     {
                     try
                        {
                        ((OperationExecutor)service).executeOperation(operation);
                        }
                     catch (UnsupportedOperationException e)
                        {
                        LOG.error("ExpressionExecutor.execute(): UnsupportedOperationException while trying to execute the operation [" + operation.getName() + "] on the [" + xmlService.getTypeId() + "] service.  Ignoring and continuing.", e);
                        }
                     }
                  else
                     {
                     LOG.warn("ExpressionExecutor.execute(): Operation not executed since service [" + service.getTypeId() + "] does not implement the OperationExecutor interface.");
                     }
                  }
               }
            else
               {
               if (LOG.isDebugEnabled())
                  {
                  LOG.debug("ExpressionExecutor.execute(): Service " + xmlService.getTypeId() + " not available for execution.");
                  }
               }
            }
         }
      }
   }
