package edu.cmu.ri.createlab.visualprogrammer;

import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

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

   boolean isRangeAscending();

   @NotNull
   String getIfBranchValueLabel();

   @NotNull
   String getElseBranchValueLabel();

   Element toServiceElementForPort(int portNumber);
   }