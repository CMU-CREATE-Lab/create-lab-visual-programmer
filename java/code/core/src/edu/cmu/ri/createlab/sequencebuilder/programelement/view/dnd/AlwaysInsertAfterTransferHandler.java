package edu.cmu.ri.createlab.sequencebuilder.programelement.view.dnd;

import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.util.Set;
import edu.cmu.ri.createlab.sequencebuilder.ContainerView;
import edu.cmu.ri.createlab.sequencebuilder.programelement.model.ProgramElementModel;
import edu.cmu.ri.createlab.sequencebuilder.programelement.view.ProgramElementView;
import org.jetbrains.annotations.NotNull;

/**
 * <p>
 * <code>AlwaysInsertBeforeTransferHandler</code> always causes drops to be placed after the given
 * {@link ProgramElementView}.
 * </p>
 *
 * @author Chris Bartley (bartley@cmu.edu)
 */
public final class AlwaysInsertAfterTransferHandler extends ProgramElementDestinationTransferHandler
   {
   private final ProgramElementView view;
   private final ContainerView containerView;

   public AlwaysInsertAfterTransferHandler(@NotNull final ProgramElementView view,
                                           @NotNull final ContainerView containerView)
      {
      super(true, containerView.getParentProgramElementView());
      this.view = view;
      this.containerView = containerView;
      }

   @Override
   public Set<DataFlavor> getSupportedDataFlavors()
      {
      return containerView.getSupportedDataFlavors();
      }

   @Override
   protected final void showInsertLocation(final Point dropPoint)
      {
      view.showInsertLocationAfter();
      }

   @Override
   protected final void performImport(@NotNull final ProgramElementModel model, @NotNull final Point dropPoint)
      {
      containerView.handleDropOfModelOntoView(model, view, false);
      }
   }
