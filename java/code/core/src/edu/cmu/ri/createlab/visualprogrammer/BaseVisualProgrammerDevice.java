package edu.cmu.ri.createlab.visualprogrammer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.PropertyResourceBundle;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.Nullable;

/**
 * <p>
 * <code>BaseVisualProgrammerDevice</code> provides base functionality for {@link VisualProgrammerDevice}
 * implementations.
 * </p>
 *
 * @author Chris Bartley (bartley@cmu.edu)
 */
public abstract class BaseVisualProgrammerDevice implements VisualProgrammerDevice
   {
   private static final Logger LOG = Logger.getLogger(BaseVisualProgrammerDevice.class);

   private final PropertyResourceBundle resources;
   private final SortedMap<String, Sensor> sensorMap = new TreeMap<String, Sensor>();
   private final List<Sensor> sensorList = new ArrayList<Sensor>();

   public BaseVisualProgrammerDevice(final PropertyResourceBundle resources)
      {
      this.resources = resources;
      }

   /**
    * Returns the name of the device, which is assumed to be defined in a property with the key <code>device.name</code>.
    */
   @Override
   public final String getDeviceName()
      {
      return resources.getString("device.name");
      }

   protected final void unregisterAllSensors()
      {
      sensorMap.clear();
      sensorList.clear();
      }

   protected final void registerSensor(final Sensor sensor)
      {
      if (sensor != null)
         {
         final Sensor previousSensor = sensorMap.put(sensor.getKey(), sensor);

         // keep the two collections in sync
         if (previousSensor != null)
            {
            sensorList.remove(previousSensor);
            }
         sensorList.add(sensor);
         }
      }

   /**
    * Returns the previously-registered {@link Sensor} with the given <code>sensorName</code> and
    * <code>serviceTypeId</code>.  Returns <code>null</code> if no such {@link Sensor} has been registered.
    */
   @Nullable
   protected final Sensor getRegisteredSensor(@Nullable final String sensorName, @Nullable final String serviceTypeId)
      {
      return sensorMap.get(BaseSensor.createKey(sensorName, serviceTypeId));
      }

   /** Returns the registered {@link Sensor}s as an unmodifiable {@link SortedSet}. */
   protected final SortedSet<Sensor> getSensorsAsSortedSet()
      {
      return Collections.unmodifiableSortedSet(new TreeSet<Sensor>(sensorMap.values()));
      }

   /**
    * Returns the registered {@link Sensor}s as an unmodifiable {@link List}.  List items are ordered in the order they
    * were registered.
    */
   protected final List<Sensor> getSensorsAsList()
      {
      return Collections.unmodifiableList(sensorList);
      }

   protected final int readConfigValueAsInt(final String propertyKey, final int defaultValue)
      {
      int value = defaultValue;
      try
         {
         final String valueFromProperties = resources.getString(propertyKey);
         value = Integer.parseInt(valueFromProperties);
         }
      catch (final Exception ignored)
         {
         if (LOG.isInfoEnabled())
            {
            LOG.debug("BaseVisualProgrammerDevice.readConfigValueAsInt(): Exception reading or converting '" + propertyKey + "' property value from properties file, using default [" + value + "]");
            }
         }
      return value;
      }

   @Nullable
   @Override
   public Set<String> getExportableLanguages()
      {
      return null;
      }
   }
