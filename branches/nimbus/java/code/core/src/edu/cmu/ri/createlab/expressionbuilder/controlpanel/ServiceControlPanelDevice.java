package edu.cmu.ri.createlab.expressionbuilder.controlpanel;

import java.awt.Component;
import java.util.Map;
import java.util.Set;
import edu.cmu.ri.createlab.terk.xml.XmlDevice;
import edu.cmu.ri.createlab.terk.xml.XmlParameter;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public interface ServiceControlPanelDevice
   {
   String UNKNOWN_VALUE = "?";

   int getDeviceIndex();

   Component getComponent();

   Component getBlockIcon();

   void updateBlockIcon();

   void updateComponent();

   boolean isActive();

   void setActive(final boolean isActive);

   boolean execute(final String operationName, final Map<String, String> parameterMap);

   String getCurrentOperationName();

   XmlDevice buildDevice();

   Set<XmlParameter> buildParameters();
   }