package edu.cmu.ri.createlab.visualprogrammer;

import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public interface Sensor<SensorClass extends Sensor> extends Comparable<SensorClass>
   {
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