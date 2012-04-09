package edu.cmu.ri.createlab.sequencebuilder;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import edu.cmu.ri.createlab.terk.expression.XmlDevice;
import edu.cmu.ri.createlab.terk.expression.XmlOperation;
import edu.cmu.ri.createlab.terk.expression.XmlParameter;
import edu.cmu.ri.createlab.terk.expression.XmlService;
import edu.cmu.ri.createlab.terk.services.DeviceController;
import edu.cmu.ri.createlab.terk.services.ServiceManager;
import org.apache.log4j.Logger;

/**
 * <p>
 * <code>BaseExpressionServiceIconView</code> provides base functionality for {@link ExpressionServiceIconView}
 * implementations.
 * </p>
 *
 * @author Chris Bartley (bartley@cmu.edu)
 * @author Jenn Cross (jenncross99@gmail.com)
 */
public abstract class BaseExpressionServiceIconView implements ExpressionServiceIconView
   {
   private static final Logger LOG = Logger.getLogger(BaseExpressionServiceIconView.class);

   private Map<String, JLabel> enabledIconMap = new HashMap<String, JLabel>(); //map of ON icons for all devices
   private Map<String, JLabel> offIconMap = new HashMap<String, JLabel>();  //map of OFF icons for all devices
   private Map<String, JLabel> disabledIconMap = new HashMap<String, JLabel>(); //map of DISABLED icons for all devices
   private Map<String, String> toolTipTextMap = new HashMap<String, String>();  //map of tooltip text for all Devices (i.e. short names)
   private Map<String, Set<XmlParameter>> offValueMap = new HashMap<String, Set<XmlParameter>>(); //map of values that indicate that device is off

   protected final Map<String, JLabel> getEnabledIconMap()
      {
      return enabledIconMap;
      }

   protected final Map<String, JLabel> getOffIconMap()
      {
      return offIconMap;
      }

   protected final Map<String, JLabel> getDisabledIconMap()
      {
      return disabledIconMap;
      }

   protected final Map<String, String> getToolTipTextMap()
      {
      return toolTipTextMap;
      }

   protected final Map<String, Set<XmlParameter>> getOffValueMap()
      {
      return offValueMap;
      }

   protected final JPanel createServiceIconPanel(final String typeID,
                                                 final Map<String, XmlService> expressionServiceMap,
                                                 final ServiceManager serviceManager)
      {
      LOG.debug("BaseExpressionServiceIconView.createServiceIconPanel(): Creating service icon panel for typeId [" + typeID + "]");

      final JPanel iconPanel = new JPanel();
      iconPanel.setLayout(new BoxLayout(iconPanel, BoxLayout.X_AXIS));
      iconPanel.setName("iconPanel");
      final int deviceCount = ((DeviceController)serviceManager.getServiceByTypeId(typeID)).getDeviceCount();

      final XmlService xmlService = expressionServiceMap.get(typeID);
      final int[] expressionDevices = new int[deviceCount + 1];
      Arrays.fill(expressionDevices, 0);

      if (xmlService != null)
         {
         for (final XmlOperation operation : xmlService.getOperations())
            {
            //LOG.debug("XmlOperation: " + operation.getName());
            for (final XmlDevice device : operation.getDevices())
               {
               //LOG.debug("XmlDevice: " + device.getClass() +" "+device.getId());
               final HashSet<XmlParameter> paramSet = (HashSet<XmlParameter>)offValueMap.get(typeID);
               for (final XmlParameter param : device.getParameters())
                  {
                  //LOG.debug("Parameters: " + param.getName() + " " + param.getValue());
                  if (paramSet != null && paramSet.contains(param))
                     {
                     if (expressionDevices[device.getId()] < 2)
                        {
                        expressionDevices[device.getId()] = 1;
                        }
                     }
                  else
                     {
                     expressionDevices[device.getId()] = 2;
                     }
                  }
               }
            }
         }

      for (int i = 0; i < deviceCount; i++)
         {
         if (xmlService != null)
            {
            if (expressionDevices[i] == 2)
               {
               iconPanel.add(new JLabel(enabledIconMap.get(typeID).getIcon()));
               }
            else if (expressionDevices[i] == 1)
               {
               iconPanel.add(new JLabel(offIconMap.get(typeID).getIcon()));
               }
            else
               {
               iconPanel.add(new JLabel(disabledIconMap.get(typeID).getIcon()));
               }
            }
         else
            {
            iconPanel.add(new JLabel(disabledIconMap.get(typeID).getIcon()));
            }
         }

      iconPanel.setToolTipText(toolTipTextMap.get(typeID));

      return iconPanel;
      }
   }
