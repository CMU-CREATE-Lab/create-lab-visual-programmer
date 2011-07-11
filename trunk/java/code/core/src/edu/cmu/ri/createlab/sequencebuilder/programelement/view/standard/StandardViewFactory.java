package edu.cmu.ri.createlab.sequencebuilder.programelement.view.standard;

import edu.cmu.ri.createlab.sequencebuilder.ContainerView;
import edu.cmu.ri.createlab.sequencebuilder.programelement.model.CounterLoopModel;
import edu.cmu.ri.createlab.sequencebuilder.programelement.model.ExpressionModel;
import edu.cmu.ri.createlab.sequencebuilder.programelement.model.LoopableConditionalModel;
import edu.cmu.ri.createlab.sequencebuilder.programelement.model.ProgramElementModel;
import edu.cmu.ri.createlab.sequencebuilder.programelement.model.SavedSequenceModel;
import edu.cmu.ri.createlab.sequencebuilder.programelement.view.ProgramElementView;
import edu.cmu.ri.createlab.sequencebuilder.programelement.view.ViewFactory;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.Nullable;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public final class StandardViewFactory implements ViewFactory
   {
   private static final Logger LOG = Logger.getLogger(StandardViewFactory.class);

   @Override
   @Nullable
   public ProgramElementView createView(@Nullable final ContainerView containerView, @Nullable final ProgramElementModel programElementModel)
      {
      ProgramElementView view = null;
      if (containerView != null && programElementModel != null)
         {
         if (programElementModel instanceof CounterLoopModel)
            {
            view = new StandardCounterLoopView(containerView, (CounterLoopModel)programElementModel);
            }
         else if (programElementModel instanceof ExpressionModel)
            {
            view = new StandardExpressionView(containerView, (ExpressionModel)programElementModel);
            }
         else if (programElementModel instanceof LoopableConditionalModel)
            {
            view = new StandardLoopableConditionalView(containerView, (LoopableConditionalModel)programElementModel);
            }
         else if (programElementModel instanceof SavedSequenceModel)
            {
            view = new StandardSavedSequenceView(containerView, (SavedSequenceModel)programElementModel);
            }
         else
            {
            LOG.error("StandardViewFactory.createView(): Unexpected ProgramElementModel instance [" + programElementModel + "], returning null");
            }
         }
      return view;
      }
   }
