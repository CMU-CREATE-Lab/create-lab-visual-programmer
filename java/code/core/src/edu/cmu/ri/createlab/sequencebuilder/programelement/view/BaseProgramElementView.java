package edu.cmu.ri.createlab.sequencebuilder.programelement.view;

import java.util.UUID;
import edu.cmu.ri.createlab.sequencebuilder.ContainerModel;
import edu.cmu.ri.createlab.sequencebuilder.ContainerView;
import edu.cmu.ri.createlab.sequencebuilder.programelement.model.ProgramElementModel;
import org.jetbrains.annotations.NotNull;

/**
 * <p>
 * <code>BaseProgramElementView</code> provides base functionality for all {@link ProgramElementView} classes.
 * </p>
 *
 * @author Chris Bartley (bartley@cmu.edu)
 */
public abstract class BaseProgramElementView<ModelClass extends ProgramElementModel> implements ProgramElementView<ModelClass>
   {
   private final UUID uuid;
   private final ContainerView containerView;
   private final ModelClass programElementModel;

   public BaseProgramElementView(@NotNull final ContainerView containerView, @NotNull final ModelClass programElementModel)
      {
      this.uuid = UUID.randomUUID();
      this.containerView = containerView;
      this.programElementModel = programElementModel;
      }

   @Override
   @NotNull
   public final UUID getUuid()
      {
      return uuid;
      }

   @NotNull
   @Override
   public final String getName()
      {
      return programElementModel.getName();
      }

   @Override
   @NotNull
   public final String getComment()
      {
      return programElementModel.getComment();
      }

   @Override
   public final boolean hasComment()
      {
      return programElementModel.hasComment();
      }

   @Override
   public final boolean isCommentVisible()
      {
      return programElementModel.isCommentVisible();
      }

   @Override
   public final boolean isContainer()
      {
      return programElementModel.isContainer();
      }

   @Override
   @NotNull
   public final ModelClass getProgramElementModel()
      {
      return programElementModel;
      }

   /** Returns the {@link ContainerView} of the container that contains this view instance. */
   protected final ContainerView getContainerView()
      {
      return containerView;
      }

   /** Returns the {@link ContainerModel} of the container that contains this view instance. */
   protected final ContainerModel getContainerModel()
      {
      return containerView.getContainerModel();
      }

   /**
    * This implementation of {@link Object#equals(Object)} only compares the {@link #getUuid() UUID} of a
    * {@link BaseProgramElementView} since we require that all views be unique.
    */
   @Override
   public final boolean equals(final Object o)
      {
      if (this == o)
         {
         return true;
         }
      if (o == null || getClass() != o.getClass())
         {
         return false;
         }

      final BaseProgramElementView that = (BaseProgramElementView)o;

      return !(uuid != null ? !uuid.equals(that.uuid) : that.uuid != null);
      }

   /**
    * This implementation of {@link Object#hashCode()} only compares the {@link #getUuid() UUID} of a
    * {@link BaseProgramElementView} since we require that all views be unique.
    */
   @Override
   public final int hashCode()
      {
      return uuid != null ? uuid.hashCode() : 0;
      }
   }
