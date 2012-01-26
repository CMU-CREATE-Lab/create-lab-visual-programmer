package edu.cmu.ri.createlab.finch.expressionbuilder.controlpanel.services;

import java.awt.Color;
import java.awt.Component;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.PropertyResourceBundle;
import java.util.Set;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import edu.cmu.ri.createlab.expressionbuilder.controlpanel.AbstractServiceControlPanel;
import edu.cmu.ri.createlab.expressionbuilder.controlpanel.AbstractServiceControlPanelDevice;
import edu.cmu.ri.createlab.expressionbuilder.controlpanel.ControlPanelManager;
import edu.cmu.ri.createlab.expressionbuilder.controlpanel.ServiceControlPanelDevice;
import edu.cmu.ri.createlab.terk.expression.XmlParameter;
import edu.cmu.ri.createlab.terk.services.Service;
import edu.cmu.ri.createlab.terk.services.finch.FinchService;
import edu.cmu.ri.createlab.userinterface.util.AbstractTimeConsumingAction;
import org.apache.log4j.Logger;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
@SuppressWarnings({"CloneableClassWithoutClone"})
public final class FinchServiceControlPanel extends AbstractServiceControlPanel
   {
   private static final Logger LOG = Logger.getLogger(FinchServiceControlPanel.class);

   private static final PropertyResourceBundle RESOURCES = (PropertyResourceBundle)PropertyResourceBundle.getBundle(FinchServiceControlPanel.class.getName());

   private static final String OPERATION_NAME = "emergencyStop";
   private static final Set<String> PARAMETER_NAMES = Collections.unmodifiableSet(new HashSet<String>());
   private static final Map<String, Set<String>> OPERATIONS_TO_PARAMETERS_MAP;

   static
      {
      final Map<String, Set<String>> operationsToParametersMap = new HashMap<String, Set<String>>();
      operationsToParametersMap.put(OPERATION_NAME, PARAMETER_NAMES);
      OPERATIONS_TO_PARAMETERS_MAP = Collections.unmodifiableMap(operationsToParametersMap);
      }

   private final FinchService service;

   public FinchServiceControlPanel(final ControlPanelManager controlPanelManager, final FinchService service)
      {
      super(controlPanelManager, service, OPERATIONS_TO_PARAMETERS_MAP);
      this.service = service;
      }

   public String getDisplayName()
      {
      return RESOURCES.getString("control-panel.title");
      }

       public String getSingleName()
       {
           return RESOURCES.getString("control-panel.name");
       }

       public String getShortDisplayName()
      {
      return RESOURCES.getString("control-panel.short-title");
      }

   public void refresh()
      {
      LOG.debug("FinchServiceControlPanel.refresh()");

      // nothing to do here
      }

   protected ServiceControlPanelDevice createServiceControlPanelDevice(final Service service, final int deviceIndex)
      {
      return new ControlPanelDevice(deviceIndex);
      }

   private final class ControlPanelDevice extends AbstractServiceControlPanelDevice
      {
      private final JPanel panel = new JPanel();
      private final JButton button = new JButton(RESOURCES.getString("button.label.emergency-stop"));

      private ControlPanelDevice(final int deviceIndex)
         {
         super(deviceIndex);

         panel.setBackground(Color.WHITE);
         panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
         panel.add(button);
         button.addActionListener(
               new AbstractTimeConsumingAction()
               {
               protected Object executeTimeConsumingAction()
                  {
                  service.emergencyStop();
                  return null;
                  }
               });
         }

      public Component getComponent()
         {
         return panel;
         }

      public Component getBlockIcon()
         {
         JPanel icon = new JPanel();
         return icon;
         }
      public void updateBlockIcon()
         {
           //TODO: Placeholder
         }
      public void getFocus()
        {
           //TODO: Placeholder
        }

      public boolean execute(final String operationName, final Map<String, String> parameterMap)
         {
         LOG.debug("FinchServiceControlPanel$ControlPanelDevice.execute()");
         if (OPERATION_NAME.equals(operationName))
            {
            service.emergencyStop();
            }
         return true;
         }

      public String getCurrentOperationName()
         {
         return OPERATION_NAME;
         }

      public Set<XmlParameter> buildParameters()
         {
         // return null--the emergencyStop operation doesn't take any parameters
         return null;
         }
      }
   }