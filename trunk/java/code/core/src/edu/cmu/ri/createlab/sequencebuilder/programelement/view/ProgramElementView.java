package edu.cmu.ri.createlab.sequencebuilder.programelement.view;

import java.awt.Point;
import java.util.UUID;
import javax.swing.JComponent;
import edu.cmu.ri.createlab.sequencebuilder.programelement.ProgramElement;
import edu.cmu.ri.createlab.sequencebuilder.programelement.model.ProgramElementModel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public interface ProgramElementView<ModelClass extends ProgramElementModel> extends ProgramElement
   {
   @NotNull
   UUID getUuid();

   @NotNull
   JComponent getComponent();

   /** Returns the {@link ProgramElementModel} for which this instance is a view. */
   ModelClass getProgramElementModel();

   void setIsEnabled(final boolean isEnabled);

   /** Visually indicates the drop location as before this element. */
   void showInsertLocationBefore();

   /** Visually indicates the drop location as after this element. */
   void showInsertLocationAfter();

   /**
    * Visually indicates the drop location based on the given {@link Point}.  If the given {@link Point} is
    * <code>null</code>, the insert location is hidden and the result is the same as calling
    * {@link #hideInsertLocations()}.
    */
   void showInsertLocation(@Nullable final Point dropPoint);

   /** Hides all insert locations for this view. */
   void hideInsertLocations();

   /**
    * Returns <code>true</code> if a drop at the given {@link Point} would cause an insert before this element; returns
    * <code>false</code> otherwise.  Returns false if the given {@link Point} is <code>null</code>.
    */
   boolean isInsertLocationBefore(@Nullable final Point dropPoint);

   /**
    * Resets this view, preparing it for execution of the sequence.
    */
   void resetViewForSequenceExecution();
   }