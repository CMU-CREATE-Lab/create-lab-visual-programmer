package edu.cmu.ri.createlab.sequencebuilder.programelement.view.listcell;

import edu.cmu.ri.createlab.sequencebuilder.ContainerView;
import edu.cmu.ri.createlab.sequencebuilder.programelement.model.LoopableConditionalModel;
import edu.cmu.ri.createlab.userinterface.util.ImageUtils;
import org.jetbrains.annotations.NotNull;

/**
 * <p>
 * <code>ExpressionListCellView</code> helps render a {@link LoopableConditionalModel} as a cell in a list.
 * </p>
 *
 * @author Chris Bartley (bartley@cmu.edu)
 */
public final class LoopableConditionalListCellView extends BaseProgramElementListCellView<LoopableConditionalModel>
   {
   public LoopableConditionalListCellView(@NotNull final ContainerView containerView, @NotNull final LoopableConditionalModel model)
      {
      super(containerView, model);
      setIcon(ImageUtils.createImageIcon("/edu/cmu/ri/createlab/sequencebuilder/programelement/view/images/loopable-conditional-icon.png"));
      setText(null);
      }
   }
