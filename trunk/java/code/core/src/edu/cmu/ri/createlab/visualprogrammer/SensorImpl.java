package edu.cmu.ri.createlab.visualprogrammer;

import java.util.Map;
import org.apache.log4j.Logger;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public class SensorImpl implements Sensor
   {
   private static final Logger LOG = Logger.getLogger(SensorImpl.class);

   /**
    * Creates a {@link String} key for this {@link Sensor} by combining the {@link Sensor#getName() sensor name}
    * with the {@link Sensor#getServiceTypeId()} sensor's service type ID}.  This key is useful when storing
    * {@link Sensor}s in a {@link Map}.
    */
   public static String createKey(final String sensorName, final String serviceTypeId)
      {
      return sensorName + "|" + serviceTypeId;
      }

   @NotNull
   private final String name;

   @NotNull
   private final String serviceTypeId;

   @NotNull
   private final String operationName;
   private final int numPorts;
   private final int minValue;
   private final int maxValue;

   @NotNull
   private final String ifBranchValueLabel;

   @NotNull
   private final String elseBranchValueLabel;

   public SensorImpl(@NotNull final String name,
                     @NotNull final String serviceTypeId,
                     @NotNull final String operationName,
                     final int numPorts,
                     final int minValue,
                     final int maxValue,
                     @NotNull final String ifBranchValueLabel,
                     @NotNull final String elseBranchValueLabel)
      {
      this.name = name;
      this.serviceTypeId = serviceTypeId;
      this.operationName = operationName;
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

   @Override
   @NotNull
   public final String getName()
      {
      return name;
      }

   @Override
   @NotNull
   public final String getServiceTypeId()
      {
      return serviceTypeId;
      }

   @Override
   @NotNull
   public final String getKey()
      {
      return createKey(getName(), getServiceTypeId());
      }

   @NotNull
   @Override
   public final String getOperationName()
      {
      return operationName;
      }

   @Override
   public final int getNumPorts()
      {
      return numPorts;
      }

   @Override
   public final int getMinValue()
      {
      return minValue;
      }

   @Override
   public final int getMaxValue()
      {
      return maxValue;
      }

   @Override
   @Nullable
   public final Integer convertRawValueToPercentage(@Nullable final Integer rawValue)
      {
      if (rawValue != null)
         {
         // clamp the result to be within the range [minValue, maxValue]
         final int cleanedResult = isRangeAscending() ? Math.min(Math.max(rawValue, minValue), maxValue) : Math.min(Math.max(rawValue, maxValue), minValue);
         if (LOG.isTraceEnabled())
            {
            LOG.trace("SensorImpl.convertRawValueToPercentage(): cleaned result [" + rawValue + "] --> [" + cleanedResult + "] and clamped to [min,max] = [" + minValue + "," + maxValue + "]");
            }

         // calculate the percentage
         final int rawPercentage = (int)(((float)(cleanedResult - minValue) / (float)(maxValue - minValue)) * 100); // TODO: this is almost surely wrong for some cases

         // clamp the percentage to [0,100], just in case
         return Math.min(Math.max(rawPercentage, 0), 100);
         }
      return null;
      }

   @Override
   public final boolean isRangeAscending()
      {
      return minValue <= maxValue;
      }

   @Override
   @NotNull
   public final String getIfBranchValueLabel()
      {
      return ifBranchValueLabel;
      }

   @Override
   @NotNull
   public final String getElseBranchValueLabel()
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

      final SensorImpl that = (SensorImpl)o;

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
   public int compareTo(final Sensor that)
      {
      if (this == that)
         {
         return 0;
         }

      if (that == null)
         {
         return 1;
         }

      final int nameComparisonResult = name.compareTo(that.getName());
      if (nameComparisonResult != 0)
         {
         return nameComparisonResult;
         }

      return serviceTypeId.compareTo(that.getServiceTypeId());
      }

   @Override
   public String toString()
      {
      final StringBuilder sb = new StringBuilder();
      sb.append("SensorImpl");
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

   @Override
   public Element toServiceElementForPort(final int portNumber)
      {
      final Element deviceElement = new Element(XML_ELEMENT_DEVICE);
      deviceElement.setAttribute(XML_ATTRIBUTE_DEVICE_ID, String.valueOf(portNumber));

      final Element operationElement = new Element(XML_ELEMENT_OPERATION);
      operationElement.setAttribute(XML_ATTRIBUTE_OPERATION_NAME, operationName);
      operationElement.addContent(deviceElement);

      final Element serviceElement = new Element(XML_ELEMENT_SERVICE);
      serviceElement.setAttribute(XML_ATTRIBUTE_SERVICE_TYPE_ID, serviceTypeId);
      serviceElement.addContent(operationElement);

      return serviceElement;
      }
   }
