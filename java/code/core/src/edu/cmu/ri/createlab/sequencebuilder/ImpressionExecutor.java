package edu.cmu.ri.createlab.sequencebuilder;

import java.util.List;
import edu.cmu.ri.createlab.terk.expression.XmlOperation;
import edu.cmu.ri.createlab.terk.expression.XmlService;
import edu.cmu.ri.createlab.terk.services.OperationExecutor;
import edu.cmu.ri.createlab.terk.services.Service;
import edu.cmu.ri.createlab.terk.services.ServiceManager;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * <p>
 * <code>ImpressionExecutor</code> executes a {@link XmlService}.
 * </p>
 *
 * @author Chris Bartley (bartley@cmu.edu)
 */
public final class ImpressionExecutor
   {
   private static final Logger LOG = Logger.getLogger(ImpressionExecutor.class);

   private static final ImpressionExecutor INSTANCE = new ImpressionExecutor();

   public static ImpressionExecutor getInstance()
      {
      return INSTANCE;
      }

   private ImpressionExecutor()
      {
      // private to prevent instantiation
      }

   // TODO: can't assume this will always return an Integer!
   @Nullable
   public Integer execute(@NotNull final ServiceManager serviceManager, @NotNull final XmlService xmlService)
      {
      if (LOG.isTraceEnabled())
         {
         LOG.trace("ImpressionExecutor.execute(): Executing impression: \n" + xmlService.toXmlStringFormatted());
         }

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
                  return (Integer)((OperationExecutor)service).executeOperation(operation);
                  }
               catch (UnsupportedOperationException e)
                  {
                  LOG.error("ImpressionExecutor.execute(): UnsupportedOperationException while trying to execute the operation [" + operation.getName() + "] on the [" + xmlService.getTypeId() + "] service.  Ignoring and continuing.", e);
                  }
               }
            else
               {
               LOG.warn("ImpressionExecutor.execute(): Operation not executed since service [" + service.getTypeId() + "] does not implement the OperationExecutor interface.");
               }
            }
         }
      else
         {
         if (LOG.isDebugEnabled())
            {
            LOG.debug("ImpressionExecutor.execute(): Service " + xmlService.getTypeId() + " not available for execution.");
            }
         }

      return null;
      }
   }
