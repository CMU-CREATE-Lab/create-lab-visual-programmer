package edu.cmu.ri.createlab.sequencebuilder.programelement.model;

import edu.cmu.ri.createlab.visualprogrammer.VisualProgrammerDevice;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * <p>
 * <code>CounterLoopModel</code> is the {@link ProgramElementModel} for a counter loop.
 * </p>
 *
 * @author Chris Bartley (bartley@cmu.edu)
 */
public final class CounterLoopModel extends BaseProgramElementModel<CounterLoopModel>
   {
   public static final String NUMBER_OF_ITERATIONS_PROPERTY = "numberOfIterations";
   public static final int MIN_NUMBER_OF_ITERATIONS = 1;
   public static final int MAX_NUMBER_OF_ITERATIONS = 999999;

   private int numberOfIterations = 1;

   /** Creates a <code>CounterLoopModel</code> with an empty comment and 1 iteration. */
   public CounterLoopModel(@NotNull final VisualProgrammerDevice visualProgrammerDevice)
      {
      this(visualProgrammerDevice, null, 1);
      }

   /**
    * Creates a <code>CounterLoopModel</code> with the given <code>comment</code>.  This constructor ensures that the
    * value is within the range <code>[{@link #MIN_NUMBER_OF_ITERATIONS}, {@link #MAX_NUMBER_OF_ITERATIONS}]</code>.
    */
   public CounterLoopModel(@NotNull final VisualProgrammerDevice visualProgrammerDevice,
                           @Nullable final String comment,
                           final int numberOfIterations)
      {
      super(visualProgrammerDevice, comment);
      this.numberOfIterations = cleanNumberOfIterations(numberOfIterations);
      }

   /** Copy construtor */
   private CounterLoopModel(@NotNull final CounterLoopModel originalCounterLoopModel)
      {
      this(originalCounterLoopModel.getVisualProgrammerDevice(),
           originalCounterLoopModel.getComment(),
           originalCounterLoopModel.getNumberOfIterations());
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

   @Override
   @NotNull
   public CounterLoopModel createCopy()
      {
      return new CounterLoopModel(this);
      }

   public int getNumberOfIterations()
      {
      return numberOfIterations;
      }

   /**
    * Sets the delay in milliseconds, and causes a {@link PropertyChangeEvent} to be fired for the
    * {@link #NUMBER_OF_ITERATIONS_PROPERTY} property.  This method ensures that the value is within the range
    * <code>[{@link #MIN_NUMBER_OF_ITERATIONS}, {@link #MAX_NUMBER_OF_ITERATIONS}]</code>.
    */
   public void setNumberOfIterations(final int numberOfIterations)
      {
      final int cleanedNumberOfIterations = cleanNumberOfIterations(numberOfIterations);
      final PropertyChangeEvent event = new PropertyChangeEventImpl(NUMBER_OF_ITERATIONS_PROPERTY, this.numberOfIterations, cleanedNumberOfIterations);
      this.numberOfIterations = cleanedNumberOfIterations;
      firePropertyChangeEvent(event);
      }

   private int cleanNumberOfIterations(final int numberOfIterations)
      {
      int cleanedNumberOfIterations = numberOfIterations;
      if (numberOfIterations < MIN_NUMBER_OF_ITERATIONS)
         {
         cleanedNumberOfIterations = MIN_NUMBER_OF_ITERATIONS;
         }
      else if (numberOfIterations > MAX_NUMBER_OF_ITERATIONS)
         {
         cleanedNumberOfIterations = MAX_NUMBER_OF_ITERATIONS;
         }
      return cleanedNumberOfIterations;
      }
   }
