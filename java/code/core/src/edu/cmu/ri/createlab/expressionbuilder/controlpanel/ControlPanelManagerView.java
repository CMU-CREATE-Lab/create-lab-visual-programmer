package edu.cmu.ri.createlab.expressionbuilder.controlpanel;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.GridBagLayout;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import edu.cmu.ri.createlab.userinterface.util.AbstractTimeConsumingAction;
import edu.cmu.ri.createlab.userinterface.util.SwingUtils;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.Nullable;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public final class ControlPanelManagerView implements ControlPanelManagerViewEventPublisher
   {
   private static final Logger LOG = Logger.getLogger(ControlPanelManagerView.class);

   private final JPanel mainPanel = new JPanel();
   //private final GroupLayout mainPanelLayout = new GroupLayout(mainPanel);
   private final Collection<ControlPanelManagerViewEventListener> controlPanelManagerViewEventListeners = new HashSet<ControlPanelManagerViewEventListener>();
   private Map<String, ServiceControlPanel> serviceControlPanelMap;
   private Map<String, SortedMap<Integer, JCheckBox>> serviceDeviceToggleButtonMap;
   private GUICreationRunnable guiCreationRunnable = null;
   private final Runnable removeAllFromMainPanelRunnable =
         new Runnable()
         {
         public void run()
            {
            mainPanel.removeAll();
            }
         };
   private final ControlPanelManager controlPanelManager;

   public ControlPanelManagerView(final ControlPanelManager controlPanelManager)
      {
      this.controlPanelManager = controlPanelManager;
      // add self as a listener
      controlPanelManager.addControlPanelManagerEventListener(new MyControlPanelManagerEventListener());

      mainPanel.setLayout(new GridBagLayout());
      mainPanel.setName("mainPanel");
      }

   public void addControlPanelManagerViewEventListener(final ControlPanelManagerViewEventListener listener)
     {
      if (listener != null)
         {
         controlPanelManagerViewEventListeners.add(listener);
         }
     }

   public void removeControlPanelManagerViewEventListener(final ControlPanelManagerViewEventListener listener)
      {
      if (listener != null)
         {
         controlPanelManagerViewEventListeners.remove(listener);
         }
      }

   public Component getComponent()
      {
      return mainPanel;
      }

   public void setDeviceGUI(@Nullable final DeviceGUI deviceGUI)
      {
      if (deviceGUI == null)
         {
         this.guiCreationRunnable = null;
         }
      else
         {
         this.guiCreationRunnable = new GUICreationRunnable(deviceGUI);
         }
      }

   private void addControlPanels()
      {
      if (LOG.isDebugEnabled())
         {
         LOG.debug("ControlPanelManagerView.addControlPanels()");
         for (final String key : serviceControlPanelMap.keySet())
            {
            LOG.debug("   [" + key + "]=[" + serviceControlPanelMap.get(key).getTypeId() + "]");
            }
         }

      SwingUtils.runInGUIThread(guiCreationRunnable);
      }

   private void removeControlPanels()
      {
      LOG.debug("ControlPanelManagerView.removeControlPanels()");

      SwingUtils.runInGUIThread(removeAllFromMainPanelRunnable);
      }

   private final class MyControlPanelManagerEventListener implements ControlPanelManagerEventListener
      {
      public void handleDeviceConnectedEvent(final Map<String, ServiceControlPanel> serviceControlPanelMap)
         {
         ControlPanelManagerView.this.serviceControlPanelMap = new HashMap<String, ServiceControlPanel>(serviceControlPanelMap);
         ControlPanelManagerView.this.serviceDeviceToggleButtonMap = new HashMap<String, SortedMap<Integer, JCheckBox>>();

         addControlPanels();

         notifyListeners();
         }

      public void handleDeviceDisconnectedEvent()
         {
         removeControlPanels();

         notifyListeners();
         }

      public void handleDeviceActivityStatusChange(final String serviceTypeId, final int deviceIndex, final AbstractServiceControlPanel.ActivityLevels isActive)
         {
         SwingUtilities.invokeLater(
               new Runnable()
               {
               public void run()
                  {
                  final SortedMap<Integer, JCheckBox> checkBoxMap = serviceDeviceToggleButtonMap.get(serviceTypeId);
                  if (checkBoxMap != null)
                     {
                     final JCheckBox checkBox = checkBoxMap.get(deviceIndex);
                     if (checkBox != null)
                        {
                        checkBox.setSelected(isActive != AbstractServiceControlPanel.ActivityLevels.STAY);
                        }
                     }
                  }
               });
         notifyListeners();
         }

      private void notifyListeners()
         {
         // notify ControlPanelManagerViewEventListeners of layout change
         for (final ControlPanelManagerViewEventListener listener : controlPanelManagerViewEventListeners)
            {
            listener.handleLayoutChange();
            }
         }
      }

   private final class GUICreationRunnable implements Runnable
      {
      private final DeviceGUI deviceGUI;

      private GUICreationRunnable(final DeviceGUI deviceGUI)
         {
         this.deviceGUI = deviceGUI;
         }

      private JCheckBox createDeviceCheckBox(final int deviceIndex, final String serviceTypeId)
         {
         final JCheckBox checkBox = new JCheckBox();
         checkBox.addActionListener(
               new AbstractTimeConsumingAction()
               {
               protected Object executeTimeConsumingAction()
                  {
                  controlPanelManager.setDeviceActive(serviceTypeId, deviceIndex, checkBox.isSelected() ? AbstractServiceControlPanel.ActivityLevels.SET : AbstractServiceControlPanel.ActivityLevels.STAY);
                  return null;
                  }
               });
         return checkBox;
         }

      public final void run()
         {
         if (serviceControlPanelMap != null)
            {
            // clear the current map
            serviceDeviceToggleButtonMap.clear();

            // loop over the services and create a checkbox for each device
            for (final ServiceControlPanel serviceControlPanel : serviceControlPanelMap.values())
               {
               final SortedMap<Integer, JCheckBox> checkBoxMap = new TreeMap<Integer, JCheckBox>();
               serviceDeviceToggleButtonMap.put(serviceControlPanel.getTypeId(), checkBoxMap);
               LOG.debug("ControlPanelManagerView$GUICreationRunnable.run(): ############## [" + serviceControlPanel.getTypeId() + "]");
               // create the device checkboxes
               final int numDevices = serviceControlPanel.getDeviceCount();
               for (int i = 0; i < numDevices; i++)
                  {
                  final JCheckBox checkBox = createDeviceCheckBox(i, serviceControlPanel.getTypeId());
                  checkBox.setFocusable(false);
                  checkBox.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                  checkBoxMap.put(i, checkBox);
                  }
               }
            LOG.debug("ControlPanelManagerView$GUICreationRunnable.run(): ############## about to create the GUI...");
            deviceGUI.createGUI(mainPanel, serviceControlPanelMap, serviceDeviceToggleButtonMap);
            }
         }
      }
   }

