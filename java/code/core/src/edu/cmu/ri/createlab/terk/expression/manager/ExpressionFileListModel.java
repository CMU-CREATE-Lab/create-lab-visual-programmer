package edu.cmu.ri.createlab.terk.expression.manager;

import java.util.Comparator;
import edu.cmu.ri.createlab.util.AbstractDirectoryPollingListModel;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public class ExpressionFileListModel extends AbstractDirectoryPollingListModel<ExpressionFile>
   {
   private static final Logger LOG = Logger.getLogger(ExpressionFileListModel.class);

   public ExpressionFileListModel()
      {
      super(
            new Comparator<ExpressionFile>()
            {
            @Override
            public int compare(final ExpressionFile view1, final ExpressionFile view2)
               {
               return view1.compareTo(view2);
               }
            });
      }

   @Override
   protected ExpressionFile createListItemInstance(@NotNull final String file)
      {
      try
         {
         return new ExpressionFile(file);
         }
      catch (final Exception e)
         {
         LOG.error("Exception while trying to create the XmlExpression for file [" + file + "]", e);
         }
      return null;
      }
   }
