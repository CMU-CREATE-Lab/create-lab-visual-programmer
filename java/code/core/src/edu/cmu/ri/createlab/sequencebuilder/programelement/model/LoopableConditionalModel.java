package edu.cmu.ri.createlab.sequencebuilder.programelement.model;

import java.util.SortedSet;
import edu.cmu.ri.createlab.visualprogrammer.VisualProgrammerDevice;
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
   public static final String SELECTED_SENSOR_PROPERTY = "selectedSensor";
   public static final String WILL_REEVALUATE_CONDITION_AFTER_IF_BRANCH_COMPLETES_PROPERTY = "willReevaluateConditionAfterIfBranchCompletes";
   public static final String WILL_REEVALUATE_CONDITION_AFTER_ELSE_BRANCH_COMPLETES_PROPERTY = "willReevaluateConditionAfterElseBranchCompletes";

   @NotNull
   private SelectedSensor selectedSensor;
   private boolean willReevaluateConditionAfterIfBranchCompletes = false;
   private boolean willReevaluateConditionAfterElseBranchCompletes = false;

   /**
    * Creates a <code>LoopableConditionalModel</code> with an empty comment, a <code>selectedSensor</code>
    * and <code>false</code> for both booleans which control reevaluation of the conditional after branch completion.
    */
   public LoopableConditionalModel(@NotNull final VisualProgrammerDevice visualProgrammerDevice)
      {
      this(visualProgrammerDevice, null, null, false, false);
      }

   /** Creates a <code>LoopableConditionalModel</code> with the given <code>comment</code>. */
   public LoopableConditionalModel(@NotNull final VisualProgrammerDevice visualProgrammerDevice,
                                   @Nullable final String comment,
                                   @Nullable final SelectedSensor selectedSensor,
                                   final boolean willReevaluateConditionAfterIfBranchCompletes,
                                   final boolean willReevaluateConditionAfterElseBranchCompletes)
      {
      super(visualProgrammerDevice, comment);
      if (selectedSensor == null)
         {
         // choose the first one as a default
         final SortedSet<SensorType> sensorTypes = visualProgrammerDevice.getSensorTypes();
         final SensorType sensor = sensorTypes.first();
         this.selectedSensor = new SelectedSensor(sensor);
         }
      else
         {
         this.selectedSensor = selectedSensor;
         }
      this.willReevaluateConditionAfterIfBranchCompletes = willReevaluateConditionAfterIfBranchCompletes;
      this.willReevaluateConditionAfterElseBranchCompletes = willReevaluateConditionAfterElseBranchCompletes;
      }

   /** Copy construtor */
   private LoopableConditionalModel(@NotNull final LoopableConditionalModel originalLoopableConditionalModel)
      {
      this(originalLoopableConditionalModel.getVisualProgrammerDevice(),
           originalLoopableConditionalModel.getComment(),
           originalLoopableConditionalModel.getSelectedSensor(),
           originalLoopableConditionalModel.willReevaluateConditionAfterIfBranchCompletes(),
           originalLoopableConditionalModel.willReevaluateConditionAfterElseBranchCompletes());
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

   @NotNull
   public SelectedSensor getSelectedSensor()
      {
      return selectedSensor;
      }

   public void setSelectedSensor(@NotNull final SelectedSensor selectedSensor)
      {
      if (LOG.isDebugEnabled())
         {
         LOG.debug("LoopableConditionalModel.setSelectedSensor(" + selectedSensor + ")");
         }
      final PropertyChangeEvent event = new PropertyChangeEventImpl(SELECTED_SENSOR_PROPERTY, this.selectedSensor, selectedSensor);
      this.selectedSensor = selectedSensor;
      firePropertyChangeEvent(event);
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
      if (LOG.isDebugEnabled())
         {
         LOG.debug("LoopableConditionalModel.setWillReevaluateConditionAfterIfBranchCompletes(" + willReevaluateConditionAfterIfBranchCompletes + ")");
         }
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
      if (LOG.isDebugEnabled())
         {
         LOG.debug("LoopableConditionalModel.setWillReevaluateConditionAfterElseBranchCompletes(" + willReevaluateConditionAfterElseBranchCompletes + ")");
         }
      final PropertyChangeEvent event = new PropertyChangeEventImpl(WILL_REEVALUATE_CONDITION_AFTER_ELSE_BRANCH_COMPLETES_PROPERTY, this.willReevaluateConditionAfterElseBranchCompletes, willReevaluateConditionAfterElseBranchCompletes);
      this.willReevaluateConditionAfterElseBranchCompletes = willReevaluateConditionAfterElseBranchCompletes;
      firePropertyChangeEvent(event);
      }

   public static final class SensorType implements Comparable<SensorType>
      {
      @NotNull
      private final String name;

      @NotNull
      private final String serviceTypeId;

      private final int numPorts;
      private final int minValue;
      private final int maxValue;

      @NotNull
      private final String ifBranchValueLabel;

      @NotNull
      private final String elseBranchValueLabel;

      public SensorType(@NotNull final String name,
                        @NotNull final String serviceTypeId,
                        final int numPorts,
                        final int minValue,
                        final int maxValue,
                        @NotNull final String ifBranchValueLabel,
                        @NotNull final String elseBranchValueLabel)
         {
         this.name = name;
         this.serviceTypeId = serviceTypeId;
         this.numPorts = numPorts;
         this.minValue = minValue;
         this.maxValue = maxValue;
         this.ifBranchValueLabel = ifBranchValueLabel;
         this.elseBranchValueLabel = elseBranchValueLabel;

         if (numPorts < 1)
            {
            throw new IllegalArgumentException("Value [" + numPorts + "] is not valid for number of ports.  The number of ports must be positive.");
            }
         }

      @NotNull
      public String getName()
         {
         return name;
         }

      @NotNull
      public String getServiceTypeId()
         {
         return serviceTypeId;
         }

      public int getNumPorts()
         {
         return numPorts;
         }

      public int getMinValue()
         {
         return minValue;
         }

      public int getMaxValue()
         {
         return maxValue;
         }

      public boolean isRangeAscending()
         {
         return minValue <= maxValue;
         }

      @NotNull
      public String getIfBranchValueLabel()
         {
         return ifBranchValueLabel;
         }

      @NotNull
      public String getElseBranchValueLabel()
         {
         return elseBranchValueLabel;
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

         final SensorType that = (SensorType)o;

         if (!name.equals(that.name))
            {
            return false;
            }
         if (!serviceTypeId.equals(that.serviceTypeId))
            {
            return false;
            }

         return true;
         }

      @Override
      public int hashCode()
         {
         int result = name.hashCode();
         result = 31 * result + serviceTypeId.hashCode();
         return result;
         }

      @Override
      public int compareTo(final SensorType sensorType)
         {
         if (this == sensorType)
            {
            return 0;
            }

         if (sensorType == null)
            {
            return 1;
            }

         final int nameComparisonResult = name.compareTo(sensorType.name);
         if (nameComparisonResult != 0)
            {
            return nameComparisonResult;
            }

         return serviceTypeId.compareTo(sensorType.serviceTypeId);
         }

      @Override
      public String toString()
         {
         final StringBuilder sb = new StringBuilder();
         sb.append("SensorType");
         sb.append("{name='").append(name).append('\'');
         sb.append(", serviceTypeId='").append(serviceTypeId).append('\'');
         sb.append(", numPorts=").append(numPorts);
         sb.append(", minValue=").append(minValue);
         sb.append(", maxValue=").append(maxValue);
         sb.append(", ifBranchValueLabel='").append(ifBranchValueLabel).append('\'');
         sb.append(", elseBranchValueLabel='").append(elseBranchValueLabel).append('\'');
         sb.append('}');
         return sb.toString();
         }
      }

   public static final class SelectedSensor
      {
      @NotNull
      private final SensorType sensorType;
      private final int portNumber;
      private final int thresholdPercentage;

      public SelectedSensor(@NotNull final SensorType sensorType,
                            final int portNumber,
                            final int thresholdPercentage)
         {
         this.sensorType = sensorType;
         this.portNumber = portNumber;
         this.thresholdPercentage = Math.min(Math.max(thresholdPercentage, 0), 100);  // clamp the percentage to [0,100]
         }

      /**
       * Creates a <code>SelectedSensor</code> with with given {@link LoopableConditionalModel.SensorType} for port 0
       * and threshold of 50%.
       */
      private SelectedSensor(@NotNull final SensorType sensor)
         {
         this(sensor, 0, 50);
         }

      @NotNull
      public SensorType getSensorType()
         {
         return sensorType;
         }

      public int getPortNumber()
         {
         return portNumber;
         }

      public int getThresholdPercentage()
         {
         return thresholdPercentage;
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

         final SelectedSensor that = (SelectedSensor)o;

         if (portNumber != that.portNumber)
            {
            return false;
            }
         if (thresholdPercentage != that.thresholdPercentage)
            {
            return false;
            }
         if (!sensorType.equals(that.sensorType))
            {
            return false;
            }

         return true;
         }

      @Override
      public int hashCode()
         {
         int result = sensorType.hashCode();
         result = 31 * result + portNumber;
         result = 31 * result + thresholdPercentage;
         return result;
         }

      @Override
      public String toString()
         {
         final StringBuilder sb = new StringBuilder();
         sb.append("SelectedSensor");
         sb.append("{sensorType=").append(sensorType);
         sb.append(", portNumber=").append(portNumber);
         sb.append(", thresholdPercentage=").append(thresholdPercentage);
         sb.append('}');
         return sb.toString();
         }
      }
   }
