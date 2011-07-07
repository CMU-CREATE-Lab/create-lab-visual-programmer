package edu.cmu.ri.createlab.sequencebuilder.programelement.view.dnd;

import java.awt.Point;
import edu.cmu.ri.createlab.sequencebuilder.ContainerView;
import edu.cmu.ri.createlab.sequencebuilder.programelement.model.ProgramElementModel;
import edu.cmu.ri.createlab.sequencebuilder.programelement.view.ProgramElementView;
import org.jetbrains.annotations.NotNull;

/**
 * <p>
 * <code>AlwaysInsertBeforeTransferHandler</code> always causes drops to be placed before the given
 * {@link ProgramElementView}.
 * </p>
 *
 * @author Chris Bartley (bartley@cmu.edu)
 */
public final class AlwaysInsertBeforeTransferHandler extends ProgramElementDestinationTransferHandler
   {
   private final ProgramElementView view;
   private final ContainerView containerView;

   public AlwaysInsertBeforeTransferHandler(@NotNull final ProgramElementView view,
                                            @NotNull final ContainerView containerView)
      {
      this.view = view;
      this.containerView = containerView;
      }

   @Override
   protected final void showInsertLocation(final Point dropPoint)
      {
      view.showInsertLocationBefore();
      }

   @Override
   protected final void performImport(@NotNull final ProgramElementModel model, @NotNull final Point dropPoint)
      {
      containerView.handleDropOfModelOntoView(model, view, true);
      }
   }
