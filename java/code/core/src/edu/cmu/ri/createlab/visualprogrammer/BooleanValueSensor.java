package edu.cmu.ri.createlab.visualprogrammer;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public class BooleanValueSensor extends BaseSensor
   {
   public BooleanValueSensor(@NotNull final String name,
                             @NotNull final String displayName,
                             @NotNull final String serviceTypeId,
                             @NotNull final String operationName,
                             @NotNull final String ifBranchValueLabel,
                             @NotNull final String elseBranchValueLabel,
                             final int numPorts)
      {
      super(name, displayName, serviceTypeId, operationName, ifBranchValueLabel, elseBranchValueLabel, numPorts);

      if (numPorts < 1)
         {
         throw new IllegalArgumentException("Value [" + numPorts + "] is not valid for number of ports.  The number of ports must be positive.");
         }
      }

   public BooleanValueSensor(@NotNull final String name,
                             @NotNull final String serviceTypeId,
                             @NotNull final String operationName,
                             @NotNull final String ifBranchValueLabel,
                             @NotNull final String elseBranchValueLabel,
                             final int numPorts)
      {
      this(name, name, serviceTypeId, operationName, ifBranchValueLabel, elseBranchValueLabel, numPorts);
      }

   @Override
   @NotNull
   public final ValueType getValueType()
      {
      return ValueType.BOOLEAN;
      }

   /**
    * Since this is a boolean value sensor, we simply return 0 for <code>false</code> and 100 for <code>true</code>.
    */
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
    * Converts the <code>rawValue</code> to a {@link Boolean}.  The default implementation merely casts if the
    * <code>rawValue</code> is an instance of an {@link Boolean}, returns <code>null</code> otherwise.
    */
   @Nullable
   protected Boolean convertRawValueToBoolean(@NotNull final Object rawValue)
      {
      return (rawValue instanceof Boolean) ? (Boolean)rawValue : null;
      }

   @Override
   public String toString()
      {
      final StringBuilder sb = new StringBuilder();
      sb.append("BooleanValueSensor");
      sb.append("{name='").append(getName()).append('\'');
      sb.append(", displayName='").append(getDisplayName()).append('\'');
      sb.append(", valueType='").append(getValueType()).append('\'');
      sb.append(", serviceTypeId='").append(getServiceTypeId()).append('\'');
      sb.append(", numPorts=").append(getNumPorts());
      sb.append(", ifBranchValueLabel='").append(getIfBranchValueLabel()).append('\'');
      sb.append(", elseBranchValueLabel='").append(getElseBranchValueLabel()).append('\'');
      sb.append('}');
      return sb.toString();
      }
   }
