package edu.cmu.ri.createlab.sequencebuilder;

import org.jetbrains.annotations.NotNull;

/**
 * <p>
 * <code>SequenceBuilderDevice</code> defines methods required by any device that is to be controlled by the
 * Sequence Builder.
 * </p>
 *
 * @author Chris Bartley (bartley@cmu.edu)
 */
public interface SequenceBuilderDevice
   {
   /** Returns an {@link ExpressionServiceIconView} */
   @NotNull
   ExpressionServiceIconView getExpressionServiceIconView();
   }
