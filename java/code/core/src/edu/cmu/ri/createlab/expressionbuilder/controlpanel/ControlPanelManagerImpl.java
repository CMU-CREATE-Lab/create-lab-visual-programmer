package edu.cmu.ri.createlab.expressionbuilder.controlpanel;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import edu.cmu.ri.createlab.terk.expression.XmlExpression;
import edu.cmu.ri.createlab.terk.services.ServiceManager;
import edu.cmu.ri.createlab.terk.xml.XmlOperation;
import edu.cmu.ri.createlab.terk.xml.XmlService;
import edu.cmu.ri.createlab.util.thread.DaemonThreadFactory;
import org.apache.log4j.Logger;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public final class ControlPanelManagerImpl implements ControlPanelManager
   {
   private static final Logger LOG = Logger.getLogger(ControlPanelManagerImpl.class);

   private final Collection<ControlPanelManagerEventListener> controlPanelManagerEventListeners = new HashSet<ControlPanelManagerEventListener>();
   private Map<String, ServiceControlPanel> serviceControlPanelsMap;
   private final ExecutorService executorPool = Executors.newCachedThreadPool(new DaemonThreadFactory("ControlPanelManagerImpl"));

   public void addControlPanelManagerEventListener(final ControlPanelManagerEventListener listener)
      {
      if (listener != null)
         {
         controlPanelManagerEventListeners.add(listener);
         }
      }

   public void removeControlPanelManagerEventListener(final ControlPanelManagerEventListener listener)
      {
      if (listener != null)
         {
         controlPanelManagerEventListeners.remove(listener);
         }
      }

   public void deviceConnected(final ServiceManager serviceManager)
      {
      // create the control panels
      serviceControlPanelsMap = ServiceControlPanelFactory.getInstance().createServiceControlPanelsMap(this, serviceManager);

      // refresh the state of the control panels
      refresh();

      // notify listeners of the connection
      for (final ControlPanelManagerEventListener listener : controlPanelManagerEventListeners)
         {
         listener.handleDeviceConnectedEvent(serviceControlPanelsMap);
         }
      }

   public void deviceDisconnected()
      {
      serviceControlPanelsMap = null;

      // notify listeners of the disconnection
      for (final ControlPanelManagerEventListener listener : controlPanelManagerEventListeners)
         {
         listener.handleDeviceDisconnectedEvent();
         }
      }

   public void setDeviceActive(final String serviceTypeId, final int deviceIndex, final boolean isActive)
      {
      if (serviceControlPanelsMap != null)
         {
         final ServiceControlPanel serviceControlPanel = serviceControlPanelsMap.get(serviceTypeId);
         if (serviceControlPanel != null)
            {
            serviceControlPanel.setDeviceActive(deviceIndex, isActive);

            // notify listeners of the change in device activity
            for (final ControlPanelManagerEventListener listener : controlPanelManagerEventListeners)
               {
               listener.handleDeviceActivityStatusChange(serviceTypeId, deviceIndex, isActive);
               }
            }
         }
      }

   public void loadExpression(final XmlExpression expression)
      {
      LOG.debug("ControlPanelManagerImpl.loadExpression()");
      if (expression != null)
         {
         if (serviceControlPanelsMap != null)
            {
            // first create a collection of the ServiceControlPanels which will be affected by this expression
            final Map<ServiceControlPanel, XmlService> serviceControlPanelsMapForExpression = new HashMap<ServiceControlPanel, XmlService>();
            final Set<XmlService> services = expression.getServices();
            for (final XmlService service : services)
               {
               if (service != null)
                  {
                  final String serviceTypeId = service.getTypeId();
                  final ServiceControlPanel serviceControlPanel = serviceControlPanelsMap.get(serviceTypeId);
                  if (serviceControlPanel != null)
                     {
                     serviceControlPanelsMapForExpression.put(serviceControlPanel, service);
                     }
                  }
               }

            // now iterate over each of the known ServiceControlPanels.  Those that AREN'T affected by the expression
            // will simply be reset so all its devices are inactive.  Those that ARE affected by the expression will
            // have its devices toggled active/inactive accordingly.
            for (final ServiceControlPanel serviceControlPanel : serviceControlPanelsMap.values())
               {
               if (serviceControlPanel != null)
                  {
                  // see wether this ServiceControlPanel will be affected by this expression
                  if (serviceControlPanelsMapForExpression.keySet().contains(serviceControlPanel))
                     {
                     LOG.debug("   Loading operations into service [" + serviceControlPanel.getTypeId() + "]");

                     // load the operations, keeping track of which devices were affected
                     final List<XmlOperation> operations = serviceControlPanelsMapForExpression.get(serviceControlPanel).getOperations();
                     final Set<Integer> affectedDeviceIds = new HashSet<Integer>();
                     for (final XmlOperation operation : operations)
                        {
                        if (operation != null)
                           {
                           affectedDeviceIds.addAll(serviceControlPanel.loadOperation(operation));
                           }
                        }

                     // update the device active/inactive status
                     final int numDevices = serviceControlPanel.getDeviceCount();
                     for (int i = 0; i < numDevices; i++)
                        {
                        setDeviceActive(serviceControlPanel.getTypeId(), i, affectedDeviceIds.contains(i));
                        }
                     }
                  else
                     {
                     // reset this ServiceControlPanel
                     reset(serviceControlPanel);
                     }
                  }
               else
                  {
                  LOG.debug("   Ignoring null ServiceControlPanel");
                  }
               }
            }
         }
      }

   public void refresh()
      {
      LOG.debug("ControlPanelManagerImpl.refresh()");
      if (serviceControlPanelsMap != null)
         {
         for (final ServiceControlPanel serviceControlPanel : serviceControlPanelsMap.values())
            {
            executorPool.execute(
                  new Runnable()
                  {
                  public void run()
                     {
                     serviceControlPanel.refresh();
                     }
                  });
            }
         }
      }

   public void reset()
      {
      if (serviceControlPanelsMap != null)
         {
         for (final ServiceControlPanel serviceControlPanel : serviceControlPanelsMap.values())
            {
            reset(serviceControlPanel);
            }
         }
      }

   public void reset(final ServiceControlPanel serviceControlPanel)
      {
      if (serviceControlPanel != null)
         {
         final int numDevices = serviceControlPanel.getDeviceCount();

         for (int i = 0; i < numDevices; i++)
            {
            setDeviceActive(serviceControlPanel.getTypeId(), i, false);
            }
         }
      }

   public XmlExpression buildExpression()
      {
      LOG.debug("ControlPanelManagerImpl.buildExpression()");

      final Set<XmlService> services = new HashSet<XmlService>();
      for (final ServiceControlPanel serviceControlPanel : serviceControlPanelsMap.values())
         {
         final XmlService service = serviceControlPanel.buildService();
         if (service != null)
            {
            services.add(service);
            }
         }
      if (!services.isEmpty())
         {
         return XmlExpression.create(services);
         }

      LOG.error("   no control panels have active devices, returning null");
      return null;
      }
   }
