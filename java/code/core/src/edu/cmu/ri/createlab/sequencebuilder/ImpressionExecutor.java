package edu.cmu.ri.createlab.sequencebuilder;

import java.util.List;
import edu.cmu.ri.createlab.terk.impression.ImpressionOperationExecutor;
import edu.cmu.ri.createlab.terk.services.Service;
import edu.cmu.ri.createlab.terk.services.ServiceManager;
import edu.cmu.ri.createlab.terk.xml.XmlOperation;
import edu.cmu.ri.createlab.terk.xml.XmlService;
import org.apache.log4j.Logger;
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

   @Nullable
   public Object execute(@Nullable final ServiceManager serviceManager, @Nullable final XmlService xmlService)
      {
      if (serviceManager == null || xmlService == null)
         {
         LOG.debug("ImpressionExecutor.execute(): The ServiceManager and/or XmlService was null.  Returning null...");
         }
      else
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
               if (service instanceof ImpressionOperationExecutor)
                  {
                  try
                     {
                     return ((ImpressionOperationExecutor)service).executeImpressionOperation(operation);
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
         }

      return null;
      }
   }
