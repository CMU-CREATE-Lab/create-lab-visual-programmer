package edu.cmu.ri.createlab.visualprogrammer;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import org.apache.log4j.Logger;

/**
 * <p>
 * <code>VisualProgrammerDeviceImplementationClassLoader</code> loads instances of {@link VisualProgrammerDevice}
 * implementation classes.
 * </p>
 *
 * @author Chris Bartley (bartley@cmu.edu)
 */
public final class VisualProgrammerDeviceImplementationClassLoader
   {
   private static final Logger LOG = Logger.getLogger(VisualProgrammerDeviceImplementationClassLoader.class);

   public static final String VISUAL_PROGRAMMER_PROPERTIES_FILE_PATH = "create-lab-visual-programmer.properties";
   public static final String VISUAL_PROGRAMMER_DEVICE_IMPLEMENTATION_CLASS_PROPERTY_KEY = "VisualProgrammerDevice.class";

   /**
    * This method loads all instances of <code>VisualProgrammerDevice</code> implementation classes that it can find. It
    * determines which classes to load by looking for files on the class path with the name
    * <code>create-lab-visual-programmer.properties</code>.  There may be more than one if multiple jars contain a file
    * of this name, so this method finds and reads them all.  It then looks inside each properties file for a key
    * <code>VisualProgrammerDevice.class</code>.  The value of the key should be a fully-qualified class name of a
    * concrete class which implements the {@link VisualProgrammerDevice} interface.  Finally, this method instatiates
    * each implementation class and returns them in a {@link List} sorted by device name as returned by
    * {@link VisualProgrammerDevice#getDeviceName()}.  This method assumes that implementation classes are constructed
    * with a no-arg constructor.  The constructor should not perform any time-consuming activities (e.g. connecting to a
    * device should be performed in the {@link VisualProgrammerDevice#connect()} method).
    */
   public List<VisualProgrammerDevice> loadImplementationClasses()
      {
      // first get the implementation class names
      final Set<String> classNames = getImplementationClassNames();

      // Now try to instantiate each one.  Save the successful ones in a sorted map.
      final List<VisualProgrammerDevice> devicesList = new ArrayList<VisualProgrammerDevice>();
      if ((classNames != null) && (!classNames.isEmpty()))
         {
         final Map<String, VisualProgrammerDevice> devicesMap = new TreeMap<String, VisualProgrammerDevice>();
         for (final String className : classNames)
            {
            final VisualProgrammerDevice device = instantiateDevice(className);
            if (device != null)
               {
               // for the key, just concatenate the device name and the class name, to eliminate the chance of duplicates
               devicesMap.put(device.getDeviceName() + className, device);
               }
            }

         // now that we have all the implementation classes constructed and sorted, build the list
         for (final String key : devicesMap.keySet())
            {
            devicesList.add(devicesMap.get(key));
            }
         }

      return devicesList;
      }

   private Set<String> getImplementationClassNames()
      {
      final Set<String> classNames = new HashSet<String>();

      try
         {
         // get an enumeration of all the properties files on the class path matching the name we're looking for
         final Enumeration<URL> resources = ClassLoader.getSystemClassLoader().getResources(VISUAL_PROGRAMMER_PROPERTIES_FILE_PATH);

         // iterate over all the properties files, and look inside each one for the property key
         while (resources.hasMoreElements())
            {
            final URL url = resources.nextElement();
            if (LOG.isDebugEnabled())
               {
               LOG.debug("VisualProgrammerDeviceImplementationClassLoader.getImplementationClassNames(): inspecting=[" + url + "]");
               }
            try
               {
               final Properties properties = new Properties();
               properties.load(url.openStream());

               final String className = (String)properties.get(VISUAL_PROGRAMMER_DEVICE_IMPLEMENTATION_CLASS_PROPERTY_KEY);
               if (className != null)
                  {
                  classNames.add(className);
                  if (LOG.isDebugEnabled())
                     {
                     LOG.debug("   VisualProgrammerDeviceImplementationClassLoader.getImplementationClassNames(): found [" + className + "]");
                     }
                  }
               }
            catch (IOException e)
               {
               LOG.error("IOException while trying to load properties file [" + url + "]", e);
               }
            }
         }
      catch (IOException e)
         {
         LOG.error("IOException while trying to load device implementation class names", e);
         }

      return classNames;
      }

   private VisualProgrammerDevice instantiateDevice(final String visualProgrammerDeviceClassName)
      {
      try
         {
         final Class clazz = Class.forName(visualProgrammerDeviceClassName);
         final Constructor constructor = clazz.getConstructor();
         if (constructor != null)
            {
            final VisualProgrammerDevice tempDevice = (VisualProgrammerDevice)constructor.newInstance();
            if (tempDevice == null)
               {
               LOG.error("Instantiation of implementation class [" + visualProgrammerDeviceClassName + "] returned null.  Weird.");
               }
            else
               {
               return tempDevice;
               }
            }
         }
      catch (ClassNotFoundException e)
         {
         LOG.error("ClassNotFoundException while trying to find VisualProgrammerDevice implementation [" + visualProgrammerDeviceClassName + "]", e);
         }
      catch (NoSuchMethodException e)
         {
         LOG.error("NoSuchMethodException while trying to find no-arg constructor for VisualProgrammerDevice implementation [" + visualProgrammerDeviceClassName + "]", e);
         }
      catch (IllegalAccessException e)
         {
         LOG.error("IllegalAccessException while trying to instantiate VisualProgrammerDevice implementation [" + visualProgrammerDeviceClassName + "]", e);
         }
      catch (InvocationTargetException e)
         {
         LOG.error("InvocationTargetException while trying to instantiate VisualProgrammerDevice implementation [" + visualProgrammerDeviceClassName + "]", e);
         }
      catch (InstantiationException e)
         {
         LOG.error("InstantiationException while trying to instantiate VisualProgrammerDevice implementation [" + visualProgrammerDeviceClassName + "]", e);
         }

      return null;
      }
   }
