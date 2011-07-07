package edu.cmu.ri.createlab.sequencebuilder.programelement.view.listcell;

import edu.cmu.ri.createlab.sequencebuilder.ContainerView;
import edu.cmu.ri.createlab.sequencebuilder.programelement.model.CounterLoopModel;
import edu.cmu.ri.createlab.userinterface.util.ImageUtils;
import org.jetbrains.annotations.NotNull;

/**
 * <p>
 * <code>ExpressionListCellView</code> helps render a {@link CounterLoopModel} as a cell in a list.
 * </p>
 *
 * @author Chris Bartley (bartley@cmu.edu)
 */
public final class CounterLoopListCellView extends BaseProgramElementListCellView<CounterLoopModel>
   {
   public CounterLoopListCellView(@NotNull final ContainerView containerView, @NotNull final CounterLoopModel model)
      {
      super(containerView, model);
      setIcon(ImageUtils.createImageIcon("/edu/cmu/ri/createlab/sequencebuilder/programelement/view/images/counter-loop-icon.png"));
      setText(null);
      }
   }
