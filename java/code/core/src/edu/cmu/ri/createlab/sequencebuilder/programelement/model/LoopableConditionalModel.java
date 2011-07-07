package edu.cmu.ri.createlab.sequencebuilder.programelement.model;

import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * <p>
 * <code>LoopableConditionalModel</code> is the {@link ProgramElementModel} for a loopable conditional.
 * </p>
 *
 * @author Chris Bartley (bartley@cmu.edu)
 */
public final class LoopableConditionalModel extends BaseProgramElementModel<LoopableConditionalModel>
   {
   private static final Logger LOG = Logger.getLogger(LoopableConditionalModel.class);
   public static final String WILL_REEVALUATE_CONDITION_AFTER_IF_BRANCH_COMPLETES_PROPERTY = "willReevaluateConditionAfterIfBranchCompletes";
   public static final String WILL_REEVALUATE_CONDITION_AFTER_ELSE_BRANCH_COMPLETES_PROPERTY = "willReevaluateConditionAfterElseBranchCompletes";

   private boolean willReevaluateConditionAfterIfBranchCompletes = false;
   private boolean willReevaluateConditionAfterElseBranchCompletes = false;

   /** Creates a <code>LoopableConditionalModel</code> with an empty comment. */
   public LoopableConditionalModel()
      {
      this((String)null);
      }

   /** Creates a <code>LoopableConditionalModel</code> with the given <code>comment</code>. */
   public LoopableConditionalModel(@Nullable final String comment)
      {
      super(comment);
      }

   /** Copy construtor */
   private LoopableConditionalModel(@NotNull final LoopableConditionalModel originalLoopableConditionalModel)
      {
      this(originalLoopableConditionalModel.getComment());
      }

   @Override
   @NotNull
   public String getName()
      {
      return this.getClass().getSimpleName();
      }

   @Override
   public boolean isContainer()
      {
      return true;
      }

   @NotNull
   @Override
   public LoopableConditionalModel createCopy()
      {
      return new LoopableConditionalModel(this);
      }

   public boolean willReevaluateConditionAfterIfBranchCompletes()
      {
      return willReevaluateConditionAfterIfBranchCompletes;
      }

   /**
    * Sets whether the condition will be reevaluated after the if-branch completes and causes a
    * {@link PropertyChangeEvent} to be fired for the
    * {@link #WILL_REEVALUATE_CONDITION_AFTER_IF_BRANCH_COMPLETES_PROPERTY} property.
    */
   public void setWillReevaluateConditionAfterIfBranchCompletes(final boolean willReevaluateConditionAfterIfBranchCompletes)
      {
      LOG.debug("LoopableConditionalModel.setWillReevaluateConditionAfterIfBranchCompletes(" + willReevaluateConditionAfterIfBranchCompletes + ")");
      final PropertyChangeEvent event = new PropertyChangeEventImpl(WILL_REEVALUATE_CONDITION_AFTER_IF_BRANCH_COMPLETES_PROPERTY, this.willReevaluateConditionAfterIfBranchCompletes, willReevaluateConditionAfterIfBranchCompletes);
      this.willReevaluateConditionAfterIfBranchCompletes = willReevaluateConditionAfterIfBranchCompletes;
      firePropertyChangeEvent(event);
      }

   public boolean willReevaluateConditionAfterElseBranchCompletes()
      {
      return willReevaluateConditionAfterElseBranchCompletes;
      }

   /**
    * Sets whether the condition will be reevaluated after the else-branch completes and causes a
    * {@link PropertyChangeEvent} to be fired for the
    * {@link #WILL_REEVALUATE_CONDITION_AFTER_ELSE_BRANCH_COMPLETES_PROPERTY} property.
    */
   public void setWillReevaluateConditionAfterElseBranchCompletes(final boolean willReevaluateConditionAfterElseBranchCompletes)
      {
      LOG.debug("LoopableConditionalModel.setWillReevaluateConditionAfterElseBranchCompletes(" + willReevaluateConditionAfterElseBranchCompletes + ")");
      final PropertyChangeEvent event = new PropertyChangeEventImpl(WILL_REEVALUATE_CONDITION_AFTER_ELSE_BRANCH_COMPLETES_PROPERTY, this.willReevaluateConditionAfterElseBranchCompletes, willReevaluateConditionAfterElseBranchCompletes);
      this.willReevaluateConditionAfterElseBranchCompletes = willReevaluateConditionAfterElseBranchCompletes;
      firePropertyChangeEvent(event);
      }

   @Override
   public boolean equals(final Object o)
      {
      if (this == o)
         {
         return true;
         }
      if (o == null || getClass() != o.getClass())
         {
         return false;
         }
      if (!super.equals(o))
         {
         return false;
         }

      final LoopableConditionalModel that = (LoopableConditionalModel)o;

      if (willReevaluateConditionAfterElseBranchCompletes != that.willReevaluateConditionAfterElseBranchCompletes)
         {
         return false;
         }
      if (willReevaluateConditionAfterIfBranchCompletes != that.willReevaluateConditionAfterIfBranchCompletes)
         {
         return false;
         }

      return true;
      }

   @Override
   public int hashCode()
      {
      int result = super.hashCode();
      result = 31 * result + (willReevaluateConditionAfterIfBranchCompletes ? 1 : 0);
      result = 31 * result + (willReevaluateConditionAfterElseBranchCompletes ? 1 : 0);
      return result;
      }
   }
