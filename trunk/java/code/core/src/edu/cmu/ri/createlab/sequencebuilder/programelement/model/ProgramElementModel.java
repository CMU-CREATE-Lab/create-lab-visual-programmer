package edu.cmu.ri.createlab.sequencebuilder.programelement.model;

import java.util.UUID;
import edu.cmu.ri.createlab.sequencebuilder.programelement.ProgramElement;
import edu.cmu.ri.createlab.visualprogrammer.VisualProgrammerDevice;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * <p>
 * <code>ProgramElementModel</code> is the model class for an element of a program.
 * </p>
 *
 * @author Chris Bartley (bartley@cmu.edu)
 */
public interface ProgramElementModel<ModelClass extends ProgramElementModel> extends ProgramElement
   {
   String COMMENT_PROPERTY = "comment";
   String HAS_COMMENT_PROPERTY = "hasComment";
   String IS_COMMENT_VISIBLE_PROPERTY = "isCommentVisible";

   interface PropertyChangeEvent
      {
      @NotNull
      String getPropertyName();

      @Nullable
      Object getOldValue();

      @Nullable
      Object getNewValue();
      }

   interface PropertyChangeEventListener
      {
      void handlePropertyChange(@NotNull final PropertyChangeEvent event);
      }

   @NotNull
   UUID getUuid();

   @NotNull
   VisualProgrammerDevice getVisualProgrammerDevice();

   String getElementType();

   void setComment(@Nullable final String comment);

   void setIsCommentVisible(final boolean isCommentVisible);

   void addPropertyChangeEventListener(@Nullable final PropertyChangeEventListener listener);

   void addPropertyChangeEventListener(@Nullable final String propertyName, @Nullable final PropertyChangeEventListener listener);

   void removePropertyChangeEventListener(@Nullable final PropertyChangeEventListener listener);

   void removePropertyChangeEventListener(@Nullable final String propertyName, @Nullable final PropertyChangeEventListener listener);

   /** Creates a copy of this <code>ProgramElementModel</code>. */
   @NotNull
   ModelClass createCopy();

   /** Creates an XML element representing this model instance. */
   @NotNull
   Element toElement();

   /**
    * Executes this ProgramElement.
    */
   void execute();

   /**
    * Refreshes this program element.
    */
   void refresh();
   }