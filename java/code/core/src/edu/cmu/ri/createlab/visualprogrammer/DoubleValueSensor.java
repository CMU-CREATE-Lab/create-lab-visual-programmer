package edu.cmu.ri.createlab.visualprogrammer;

import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public class DoubleValueSensor extends BaseSensor
   {
   private static final Logger LOG = Logger.getLogger(DoubleValueSensor.class);

   private final double minValue;
   private final double maxValue;

   public DoubleValueSensor(@NotNull final String name,
                            @NotNull final String displayName,
                            @NotNull final String serviceTypeId,
                            @NotNull final String operationName,
                            @NotNull final String ifBranchValueLabel,
                            @NotNull final String elseBranchValueLabel,
                            final int numPorts,
                            final double minValue,
                            final double maxValue)
      {
      super(name, displayName, serviceTypeId, operationName, ifBranchValueLabel, elseBranchValueLabel, numPorts);
      this.minValue = minValue;
      this.maxValue = maxValue;

      if (numPorts < 1)
         {
         throw new IllegalArgumentException("Value [" + numPorts + "] is not valid for number of ports.  The number of ports must be positive.");
         }
      }

   public DoubleValueSensor(@NotNull final String name,
                            @NotNull final String serviceTypeId,
                            @NotNull final String operationName,
                            @NotNull final String ifBranchValueLabel,
                            @NotNull final String elseBranchValueLabel,
                            final int numPorts,
                            final double minValue,
                            final double maxValue)
      {
      this(name, name, serviceTypeId, operationName, ifBranchValueLabel, elseBranchValueLabel, numPorts, minValue, maxValue);
      }

   @Override
   @NotNull
   public final ValueType getValueType()
      {
      return ValueType.RANGE;
      }

   @Override
   @Nullable
   public final Integer convertRawValueToPercentage(@Nullable final Object rawValue)
      {
      if (rawValue != null)
         {
         final Double rawDoubleValue = convertRawValueToDouble(rawValue);

         if (rawDoubleValue != null)
            {
            // clamp the result to be within the range [minValue, maxValue]
            final double cleanedResult = isRangeAscending() ? Math.min(Math.max(rawDoubleValue, minValue), maxValue) : Math.min(Math.max(rawDoubleValue, maxValue), minValue);
            if (LOG.isTraceEnabled())
               {
               LOG.trace("DoubleValueSensor.convertRawValueToPercentage(): cleaned result [" + rawDoubleValue + "] --> [" + cleanedResult + "] and clamped to [min,max] = [" + minValue + "," + maxValue + "]");
               }

            // calculate the percentage
            final int rawPercentage = (int)(((float)(cleanedResult - minValue) / (float)(maxValue - minValue)) * 100);

            // clamp the percentage to [0,100], just in case
            return Math.min(Math.max(rawPercentage, 0), 100);
            }
         }
      return null;
      }

   /**
    * Converts the <code>rawValue</code> to a {@link Double}.  The default implementation merely casts if the
    * <code>rawValue</code> is an instance of an {@link Double}, returns <code>null</code> otherwise.
    */
   @Nullable
   protected Double convertRawValueToDouble(@NotNull final Object rawValue)
      {
      return (rawValue instanceof Double) ? (Double)rawValue : null;
      }

   private boolean isRangeAscending()
      {
      return minValue <= maxValue;
      }

   @Override
   public String toString()
      {
      final StringBuilder sb = new StringBuilder();
      sb.append("DoubleValueSensor");
      sb.append("{name='").append(getName()).append('\'');
      sb.append(", displayName='").append(getDisplayName()).append('\'');
      sb.append(", valueType='").append(getValueType()).append('\'');
      sb.append(", serviceTypeId='").append(getServiceTypeId()).append('\'');
      sb.append(", numPorts=").append(getNumPorts());
      sb.append(", minValue=").append(minValue);
      sb.append(", maxValue=").append(maxValue);
      sb.append(", ifBranchValueLabel='").append(getIfBranchValueLabel()).append('\'');
      sb.append(", elseBranchValueLabel='").append(getElseBranchValueLabel()).append('\'');
      sb.append('}');
      return sb.toString();
      }
   }
