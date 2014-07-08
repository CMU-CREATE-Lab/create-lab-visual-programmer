package edu.cmu.ri.createlab.sequencebuilder;

import edu.cmu.ri.createlab.sequencebuilder.programelement.model.ExpressionModel;
import edu.cmu.ri.createlab.sequencebuilder.programelement.model.SavedSequenceModel;
import edu.cmu.ri.createlab.terk.expression.manager.ExpressionFile;
import org.jetbrains.annotations.NotNull;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
interface FileManagerControlsController
   {
   void openExpression(@NotNull final ExpressionFile expressionFile);

   void openSequence(@NotNull final SavedSequenceModel savedSequenceModel);

   boolean deleteExpression(@NotNull final ExpressionModel expressionModel);

   boolean deleteSequence(@NotNull final SavedSequenceModel savedSequenceModel);
   }
