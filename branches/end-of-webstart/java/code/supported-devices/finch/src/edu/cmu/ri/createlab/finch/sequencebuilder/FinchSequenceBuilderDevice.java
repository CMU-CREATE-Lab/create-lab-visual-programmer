package edu.cmu.ri.createlab.finch.sequencebuilder;

import edu.cmu.ri.createlab.sequencebuilder.ExpressionServiceIconView;
import edu.cmu.ri.createlab.sequencebuilder.SequenceBuilderDevice;
import org.jetbrains.annotations.NotNull;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public final class FinchSequenceBuilderDevice implements SequenceBuilderDevice
   {
   private final ExpressionServiceIconView expressionServiceIconView = new FinchExpressionServiceIconView();

   @Override
   @NotNull
   public ExpressionServiceIconView getExpressionServiceIconView()
      {
      return expressionServiceIconView;
      }
   }
