package edu.cmu.ri.createlab.sequencebuilder.programelement.view;

import edu.cmu.ri.createlab.sequencebuilder.ContainerView;
import edu.cmu.ri.createlab.sequencebuilder.programelement.model.ProgramElementModel;
import org.jetbrains.annotations.Nullable;

/**
 * <p>
 * <code>ViewFactory</code> creates a {@link ProgramElementView} instance from a {@link ProgramElementModel}.
 * </p>
 *
 * @author Chris Bartley (bartley@cmu.edu)
 */
public interface ViewFactory
   {
   /**
    * Creates a {@link ProgramElementView} for the given {@link ProgramElementModel} and registers it with the
    * {@link ViewEventPublisher}.  Returns <code>null</code> if the given {@link ProgramElementModel} is <code>null</code> or
    * if this factory doesn't know how to create an appropriate view for it.
    */
   @Nullable
   ProgramElementView createView(final ContainerView containerView, @Nullable final ProgramElementModel programElementModel);
   }