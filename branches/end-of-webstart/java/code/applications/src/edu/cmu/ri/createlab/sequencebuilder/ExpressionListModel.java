package edu.cmu.ri.createlab.sequencebuilder;

import java.io.File;
import java.util.Comparator;
import edu.cmu.ri.createlab.sequencebuilder.programelement.model.ExpressionModel;
import edu.cmu.ri.createlab.sequencebuilder.programelement.view.listcell.ExpressionListCellView;
import edu.cmu.ri.createlab.util.AbstractDirectoryPollingListModel;
import edu.cmu.ri.createlab.visualprogrammer.VisualProgrammerDevice;
import org.jetbrains.annotations.NotNull;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
final class ExpressionListModel extends AbstractDirectoryPollingListModel<ExpressionListCellView>
   {
   private final ContainerView containerView;
   private final VisualProgrammerDevice visualProgrammerDevice;

   ExpressionListModel(@NotNull final ContainerView containerView, final VisualProgrammerDevice visualProgrammerDevice)
      {
      super(
            new Comparator<ExpressionListCellView>()
            {
            @Override
            public int compare(final ExpressionListCellView view1, final ExpressionListCellView view2)
               {
               return view1.getProgramElementModel().getExpressionFile().compareTo(view2.getProgramElementModel().getExpressionFile());
               }
            });
      this.containerView = containerView;
      this.visualProgrammerDevice = visualProgrammerDevice;
      }

   @Override
   protected ExpressionListCellView createListItemInstance(@NotNull final File file)
      {
      return new ExpressionListCellView(containerView, new ExpressionModel(visualProgrammerDevice, file));
      }
   }
