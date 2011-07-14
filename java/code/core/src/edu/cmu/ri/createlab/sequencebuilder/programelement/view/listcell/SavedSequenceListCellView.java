package edu.cmu.ri.createlab.sequencebuilder.programelement.view.listcell;

import edu.cmu.ri.createlab.sequencebuilder.ContainerView;
import edu.cmu.ri.createlab.sequencebuilder.programelement.model.SavedSequenceModel;
import edu.cmu.ri.createlab.userinterface.util.ImageUtils;
import org.jetbrains.annotations.NotNull;

/**
 * <p>
 * <code>ExpressionListCellView</code> helps render a {@link SavedSequenceModel} as a cell in a list.
 * </p>
 *
 * @author Chris Bartley (bartley@cmu.edu)
 */
public final class SavedSequenceListCellView extends BaseProgramElementListCellView<SavedSequenceModel>
   {
   public SavedSequenceListCellView(@NotNull final ContainerView containerView, @NotNull final SavedSequenceModel model)
      {
      super(containerView, model);
      setIcon(ImageUtils.createImageIcon("/edu/cmu/ri/createlab/sequencebuilder/programelement/view/images/sequence-icon-small.png"));
      }
   }
