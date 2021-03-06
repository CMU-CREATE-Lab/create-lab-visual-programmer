package edu.cmu.ri.createlab.visualprogrammer;

import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public class IntegralValueSensor extends BaseSensor
   {
   private static final Logger LOG = Logger.getLogger(IntegralValueSensor.class);

   private final int minValue;
   private final int maxValue;

   public IntegralValueSensor(@NotNull final String name,
                              @NotNull final String displayName,
                              @NotNull final String serviceTypeId,
                              @NotNull final String operationName,
                              @NotNull final String ifBranchValueLabel,
                              @NotNull final String elseBranchValueLabel,
                              final int numPorts,
                              final int minValue,
                              final int maxValue)
      {
      super(name, displayName, serviceTypeId, operationName, ifBranchValueLabel, elseBranchValueLabel, numPorts);
      this.minValue = minValue;
      this.maxValue = maxValue;

      if (numPorts < 1)
         {
         throw new IllegalArgumentException("Value [" + numPorts + "] is not valid for number of ports.  The number of ports must be positive.");
         }
      }

   public IntegralValueSensor(@NotNull final String name,
                              @NotNull final String serviceTypeId,
                              @NotNull final String operationName,
                              @NotNull final String ifBranchValueLabel,
                              @NotNull final String elseBranchValueLabel,
                              final int numPorts,
                              final int minValue,
                              final int maxValue)
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
         final Integer rawIntegerValue = convertRawValueToInteger(rawValue);

         if (rawIntegerValue != null)
            {
            // clamp the result to be within the range [minValue, maxValue]
            final int cleanedResult = isRangeAscending() ? Math.min(Math.max(rawIntegerValue, minValue), maxValue) : Math.min(Math.max(rawIntegerValue, maxValue), minValue);
            if (LOG.isTraceEnabled())
               {
               LOG.trace("IntegralValueSensor.convertRawValueToPercentage(): cleaned result [" + rawIntegerValue + "] --> [" + cleanedResult + "] and clamped to [min,max] = [" + minValue + "," + maxValue + "]");
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
    * Converts the <code>rawValue</code> to an {@link Integer}.  The default implementation merely casts if the
    * <code>rawValue</code> is an instance of an {@link Integer}, returns <code>null</code> otherwise.
    */
   @Nullable
   protected Integer convertRawValueToInteger(@NotNull final Object rawValue)
      {
      return (rawValue instanceof Integer) ? (Integer)rawValue : null;
      }

   private boolean isRangeAscending()
      {
      return minValue <= maxValue;
      }

   @Override
   public String toString()
      {
      final StringBuilder sb = new StringBuilder();
      sb.append("IntegralValueSensor");
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
