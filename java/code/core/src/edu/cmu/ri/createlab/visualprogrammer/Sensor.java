package edu.cmu.ri.createlab.visualprogrammer;

import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public interface Sensor<SensorClass extends Sensor> extends Comparable<SensorClass>
   {
   String XML_ELEMENT_SERVICE = "service";
   String XML_ATTRIBUTE_SERVICE_TYPE_ID = "type-id";
   String XML_ELEMENT_OPERATION = "operation";
   String XML_ATTRIBUTE_OPERATION_NAME = "name";
   String XML_ELEMENT_DEVICE = "device";
   String XML_ATTRIBUTE_DEVICE_ID = "id";

   @NotNull
   String getName();

   @NotNull
   String getServiceTypeId();

   @NotNull
   String getOperationName();

   int getNumPorts();

   int getMinValue();

   int getMaxValue();

   /**
    * Converts the given raw value to a percentage.  Returns <code>null</code> if the given value is <code>null</code>.
    */
   // TODO: shouldn't assume that the sensor raw value is an integer
   @Nullable
   Integer convertRawValueToPercentage(@Nullable final Integer rawValue);

   boolean isRangeAscending();

   @NotNull
   String getIfBranchValueLabel();

   @NotNull
   String getElseBranchValueLabel();

   Element toServiceElementForPort(int portNumber);
   }