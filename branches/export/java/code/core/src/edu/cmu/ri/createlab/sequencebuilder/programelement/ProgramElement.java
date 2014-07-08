package edu.cmu.ri.createlab.sequencebuilder.programelement;

import org.jetbrains.annotations.NotNull;

/**
 * <p>
 * <code>ProgramElement</code> represents an element of a program.
 * </p>
 *
 * @author Chris Bartley (bartley@cmu.edu)
 */
public interface ProgramElement
   {
   @NotNull
   String getName();

   /**
    * Returns the comment, or the empty {@link String} if there is no comment or if it was set to <code>null</code>.
    * This method will never return <code>null</code>.
    */
   @NotNull
   String getComment();

   boolean hasComment();

   boolean isCommentVisible();

   /** Returns whether this <code>ProgramElement</code> can contain other <code>ProgramElement</code>s. */
   boolean isContainer();
   }