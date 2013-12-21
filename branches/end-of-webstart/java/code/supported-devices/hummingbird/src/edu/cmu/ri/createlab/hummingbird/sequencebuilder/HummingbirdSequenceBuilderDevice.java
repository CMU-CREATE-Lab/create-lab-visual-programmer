package edu.cmu.ri.createlab.hummingbird.sequencebuilder;

import edu.cmu.ri.createlab.sequencebuilder.ExpressionServiceIconView;
import edu.cmu.ri.createlab.sequencebuilder.SequenceBuilderDevice;
import org.jetbrains.annotations.NotNull;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public final class HummingbirdSequenceBuilderDevice implements SequenceBuilderDevice
   {
   private final ExpressionServiceIconView expressionServiceIconView = new HummingbirdExpressionServiceIconView();

   @Override
   @NotNull
   public ExpressionServiceIconView getExpressionServiceIconView()
      {
      return expressionServiceIconView;
      }
   }
