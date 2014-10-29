package edu.cmu.ri.createlab.visualprogrammer;

import java.util.Map;
import edu.cmu.ri.createlab.terk.xml.XmlDevice;
import edu.cmu.ri.createlab.terk.xml.XmlOperation;
import edu.cmu.ri.createlab.terk.xml.XmlService;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

/**
 * <p>
 * <code>BaseSensor</code> provides base functionality for {@link Sensor} implementations.
 * </p>
 *
 * @author Chris Bartley (bartley@cmu.edu)
 */
public abstract class BaseSensor implements Sensor
   {
   @NotNull
   private final String name;

   @NotNull
   private final String displayName;

   @NotNull
   private final String operationName;

   @NotNull
   private final String ifBranchValueLabel;

   @NotNull
   private final String elseBranchValueLabel;

   @NotNull
   private final String serviceTypeId;

   private final int numPorts;

   public BaseSensor(@NotNull final String name,
                     @NotNull final String displayName,
                     @NotNull final String serviceTypeId,
                     @NotNull final String operationName,
                     @NotNull final String ifBranchValueLabel,
                     @NotNull final String elseBranchValueLabel,
                     final int numPorts)
      {
      this.name = name;
      this.displayName = displayName;
      this.serviceTypeId = serviceTypeId;
      this.operationName = operationName;
      this.ifBranchValueLabel = ifBranchValueLabel;
      this.elseBranchValueLabel = elseBranchValueLabel;
      this.numPorts = numPorts;
      }

   public BaseSensor(@NotNull final String name,
                     @NotNull final String serviceTypeId,
                     @NotNull final String operationName,
                     @NotNull final String ifBranchValueLabel,
                     @NotNull final String elseBranchValueLabel,
                     final int numPorts)
      {
      this(name, name, serviceTypeId, operationName, ifBranchValueLabel, elseBranchValueLabel, numPorts);
      }

   /**
    * Creates a {@link String} key for this {@link Sensor} by combining the {@link Sensor#getName() sensor name}
    * with the {@link Sensor#getServiceTypeId()} sensor's service type ID}.  This key is useful when storing
    * {@link Sensor}s in a {@link Map}.
    */
   public static String createKey(final String sensorName, final String serviceTypeId)
      {
      return sensorName + "|" + serviceTypeId;
      }

   @Override
   @NotNull
   public final String getName()
      {
      return name;
      }

   @Override
   @NotNull
   public final String getDisplayName()
      {
      return displayName;
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
   public final Element toServiceElementForPort(final int portNumber)
      {
      final XmlDevice device = new XmlDevice(portNumber);
      final XmlOperation operation = new XmlOperation(operationName, device);
      final XmlService service = new XmlService(serviceTypeId, operation);
      return service.toElement();
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

      final Sensor that = (Sensor)o;

      return name.equals(that.getName()) && serviceTypeId.equals(that.getServiceTypeId());
      }

   @Override
   public int hashCode()
      {
      int result = name.hashCode();
      result = 31 * result + serviceTypeId.hashCode();
      return result;
      }

   @Override
   public final int compareTo(final Sensor that)
      {
      if (this == that) // yes, I really meant to use == here, to check for equivalence.
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
   }
