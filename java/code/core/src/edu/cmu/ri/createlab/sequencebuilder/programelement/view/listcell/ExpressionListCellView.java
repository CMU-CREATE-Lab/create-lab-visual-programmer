package edu.cmu.ri.createlab.sequencebuilder.programelement.view.listcell;

import edu.cmu.ri.createlab.sequencebuilder.ContainerView;
import edu.cmu.ri.createlab.sequencebuilder.programelement.model.ExpressionModel;
import edu.cmu.ri.createlab.userinterface.util.ImageUtils;
import org.jetbrains.annotations.NotNull;

/**
 * <p>
 * <code>ExpressionListCellView</code> helps render an {@link ExpressionModel} as a cell in a list.
 * </p>
 *
 * @author Chris Bartley (bartley@cmu.edu)
 */
public final class ExpressionListCellView extends BaseProgramElementListCellView<ExpressionModel>
   {
   public ExpressionListCellView(@NotNull final ContainerView containerView, @NotNull final ExpressionModel model)
      {
      super(containerView, model);
      setIcon(ImageUtils.createImageIcon("/edu/cmu/ri/createlab/sequencebuilder/programelement/view/images/expression-icon.png"));
      }
   }
