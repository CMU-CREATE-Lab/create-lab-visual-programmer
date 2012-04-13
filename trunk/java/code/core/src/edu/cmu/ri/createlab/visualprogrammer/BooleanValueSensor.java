package edu.cmu.ri.createlab.visualprogrammer;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public class BooleanValueSensor extends BaseSensor
   {
   public BooleanValueSensor(@NotNull final String name,
                             @NotNull final String serviceTypeId,
                             @NotNull final String operationName,
                             final int numPorts,
                             @NotNull final String ifBranchValueLabel,
                             @NotNull final String elseBranchValueLabel)
      {
      super(elseBranchValueLabel, operationName, numPorts, name, ifBranchValueLabel, serviceTypeId);

      if (numPorts < 1)
         {
         throw new IllegalArgumentException("Value [" + numPorts + "] is not valid for number of ports.  The number of ports must be positive.");
         }
      }

   @Override
   @Nullable
   public final Integer convertRawValueToPercentage(@Nullable final Object rawValue)
      {
      if (rawValue != null)
         {
         final Boolean rawBooleanValue = convertRawValueToBoolean(rawValue);
         if (rawBooleanValue != null)
            {
            return rawBooleanValue ? 100 : 0;
            }
         }
      return null;
      }

   /**
    * Converts the <code>rawValue</code> to a {@link Boolean}.  The default implementation merely casts.
    */
   @Nullable
   protected Boolean convertRawValueToBoolean(@NotNull final Object rawValue)
      {
      return (Boolean)rawValue;
      }

   @Override
   public String toString()
      {
      final StringBuilder sb = new StringBuilder();
      sb.append("BooleanValueSensor");
      sb.append("{name='").append(getName()).append('\'');
      sb.append(", serviceTypeId='").append(getServiceTypeId()).append('\'');
      sb.append(", numPorts=").append(getNumPorts());
      sb.append(", ifBranchValueLabel='").append(getIfBranchValueLabel()).append('\'');
      sb.append(", elseBranchValueLabel='").append(getElseBranchValueLabel()).append('\'');
      sb.append('}');
      return sb.toString();
      }
   }
