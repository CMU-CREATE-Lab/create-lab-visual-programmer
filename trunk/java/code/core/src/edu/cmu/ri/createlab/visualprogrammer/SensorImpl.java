package edu.cmu.ri.createlab.visualprogrammer;

import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public class SensorImpl implements Sensor<SensorImpl>
   {
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
   public int compareTo(final SensorImpl that)
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
