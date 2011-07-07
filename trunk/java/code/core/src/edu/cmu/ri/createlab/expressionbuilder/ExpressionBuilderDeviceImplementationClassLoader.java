package edu.cmu.ri.createlab.expressionbuilder;

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
 * <code>ExpressionBuilderDeviceImplementationClassLoader</code> loads instances of {@link ExpressionBuilderDevice}
 * implementation classes.
 * </p>
 *
 * @author Chris Bartley (bartley@cmu.edu)
 */
public class ExpressionBuilderDeviceImplementationClassLoader
   {
   private static final Logger LOG = Logger.getLogger(ExpressionBuilderDeviceImplementationClassLoader.class);

   private static final String EXPRESSION_BUILDER_DEVICE_PROPERTIES_FILE_PATH = "create-lab-expression-builder-device.properties";
   private static final String EXPRESSION_BUILDER_DEVICE_CLASS_PROPERTY_KEY = "ExpressionBuilderDevice.class";

   /**
    * This method loads all instances of {@link ExpressionBuilderDevice} implementation classes that it can find. It
    * determines which classes to load by looking for files on the class path with the name
    * <code>create-lab-expression-builder-device.properties</code>.  There may be more than one if multiple jars contain
    * a file of this name, so this method finds and reads them all.  It then looks inside each properties file for a key
    * named <code>ExpressionBuilderDevice.class</code>.  The value of the key should be a fully-qualified class name of
    * a concrete class which implements the {@link ExpressionBuilderDevice} interface.  Finally, this method instatiates
    * each implementation class and returns them in a {@link List} sorted by device name as returned by
    * {@link ExpressionBuilderDevice#getDeviceName()}.  This method assumes that implementation classes are constructed
    * with a no-arg constructor.  The constructor should not perform any time-consuming activities (e.g. connecting to
    * a device should be performed in the {@link ExpressionBuilderDevice#connect()} method).
    */
   public final List<ExpressionBuilderDevice> loadImplementationClasses()
      {
      // first get the implementation class names
      final Set<String> classNames = getExpressionBuilderDeviceImplementationClassNames();

      // Now try to instantiate each one.  Save the successful ones in a sorted map.
      final List<ExpressionBuilderDevice> expressionBuilderDevicesList = new ArrayList<ExpressionBuilderDevice>();
      if ((classNames != null) && (!classNames.isEmpty()))
         {
         final Map<String, ExpressionBuilderDevice> expressionBuilderDevicesMap = new TreeMap<String, ExpressionBuilderDevice>();
         for (final String className : classNames)
            {
            final ExpressionBuilderDevice expressionBuilderDevice = instantiateExpressionBuilderDevice(className);
            if (expressionBuilderDevice != null)
               {
               // for the key, just concatenate the device name and the class name, to eliminate the chance of duplicates
               expressionBuilderDevicesMap.put(expressionBuilderDevice.getDeviceName() + className, expressionBuilderDevice);
               }
            }

         // now that we have all the implementation classes constructed and sorted, build the list
         for (final String key : expressionBuilderDevicesMap.keySet())
            {
            expressionBuilderDevicesList.add(expressionBuilderDevicesMap.get(key));
            }
         }

      return expressionBuilderDevicesList;
      }

   private Set<String> getExpressionBuilderDeviceImplementationClassNames()
      {
      final Set<String> classNames = new HashSet<String>();

      try
         {
         // get an enumeration of all the properties files on the class path matching the name we're looking for
         final Enumeration<URL> resources = ClassLoader.getSystemClassLoader().getResources(EXPRESSION_BUILDER_DEVICE_PROPERTIES_FILE_PATH);

         // iterate over all the properties files, and look inside each one for the property key
         while (resources.hasMoreElements())
            {
            final URL url = resources.nextElement();
            if (LOG.isDebugEnabled())
               {
               LOG.debug("ExpressionBuilderDeviceImplementationClassLoader.getExpressionBuilderDeviceImplementationClassNames(): inspecting=[" + url + "]");
               }
            try
               {
               final Properties properties = new Properties();
               properties.load(url.openStream());

               final String className = (String)properties.get(EXPRESSION_BUILDER_DEVICE_CLASS_PROPERTY_KEY);
               if (className != null)
                  {
                  classNames.add(className);
                  if (LOG.isDebugEnabled())
                     {
                     LOG.debug("   ExpressionBuilderDeviceImplementationClassLoader.getExpressionBuilderDeviceImplementationClassNames(): found [" + className + "]");
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
         LOG.error("IOException while trying to load ExpressionBuilderDevice implementation class names", e);
         }

      return classNames;
      }

   private static ExpressionBuilderDevice instantiateExpressionBuilderDevice(final String expressionBuilderDeviceClassName)
      {
      try
         {
         final Class clazz = Class.forName(expressionBuilderDeviceClassName);
         final Constructor constructor = clazz.getConstructor();
         if (constructor != null)
            {
            final ExpressionBuilderDevice tempExpressionBuilderDevice = (ExpressionBuilderDevice)constructor.newInstance();
            if (tempExpressionBuilderDevice == null)
               {
               LOG.error("Instantiation of ExpressionBuilderDevice implementation [" + expressionBuilderDeviceClassName + "] returned null.  Weird.");
               }
            else
               {
               return tempExpressionBuilderDevice;
               }
            }
         }
      catch (ClassNotFoundException e)
         {
         LOG.error("ClassNotFoundException while trying to find ExpressionBuilderDevice implementation [" + expressionBuilderDeviceClassName + "]", e);
         }
      catch (NoSuchMethodException e)
         {
         LOG.error("NoSuchMethodException while trying to find no-arg constructor for ExpressionBuilderDevice implementation [" + expressionBuilderDeviceClassName + "]", e);
         }
      catch (IllegalAccessException e)
         {
         LOG.error("IllegalAccessException while trying to instantiate ExpressionBuilderDevice implementation [" + expressionBuilderDeviceClassName + "]", e);
         }
      catch (InvocationTargetException e)
         {
         LOG.error("InvocationTargetException while trying to instantiate ExpressionBuilderDevice implementation [" + expressionBuilderDeviceClassName + "]", e);
         }
      catch (InstantiationException e)
         {
         LOG.error("InstantiationException while trying to instantiate ExpressionBuilderDevice implementation [" + expressionBuilderDeviceClassName + "]", e);
         }

      return null;
      }
   }
