package edu.cmu.ri.createlab.sequencebuilder;

import org.jetbrains.annotations.NotNull;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public final class Sequence
   {
   private final ContainerModel containerModel;
   private final ContainerView containerView;

   public Sequence(@NotNull final ContainerModel containerModel, @NotNull final ContainerView containerView)
      {
      this.containerModel = containerModel;
      this.containerView = containerView;
      }

   public ContainerModel getContainerModel()
      {
      return containerModel;
      }

   public ContainerView getContainerView()
      {
      return containerView;
      }
   }
