package edu.cmu.ri.createlab.visualprogrammer;

import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public interface Sensor extends Comparable<Sensor>
   {
   enum ValueType
      {
         RANGE("Range"),
         BOOLEAN("Boolean");

      private final String name;

      private ValueType(final String name)
         {
         this.name = name;
         }

      public String getName()
         {
         return name;
         }

      public String toString()
         {
         return "ValueType{" +
                "name='" + name + '\'' +
                '}';
         }
      }

   @NotNull
   String getName();

   @NotNull
   String getDisplayName();

   @NotNull
   ValueType getValueType();

   @NotNull
   String getServiceTypeId();

   @NotNull
   String getKey();

   @NotNull
   String getOperationName();

   int getNumPorts();

   /**
    * Converts the given raw value to a percentage.  Returns <code>null</code> if the given value is <code>null</code>.
    */
   @Nullable
   Integer convertRawValueToPercentage(@Nullable final Object rawValue);

   @NotNull
   String getIfBranchValueLabel();

   @NotNull
   String getElseBranchValueLabel();

   Element toServiceElementForPort(int portNumber);
   }