package edu.cmu.ri.createlab.expressionbuilder;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Map;
import java.util.PropertyResourceBundle;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;
import edu.cmu.ri.createlab.device.CreateLabDevicePingFailureEventListener;
import edu.cmu.ri.createlab.device.CreateLabDeviceProxy;
import edu.cmu.ri.createlab.expressionbuilder.controlpanel.ControlPanelManager;
import edu.cmu.ri.createlab.expressionbuilder.controlpanel.ControlPanelManagerEventListener;
import edu.cmu.ri.createlab.expressionbuilder.controlpanel.ControlPanelManagerImpl;
import edu.cmu.ri.createlab.expressionbuilder.controlpanel.ControlPanelManagerView;
import edu.cmu.ri.createlab.expressionbuilder.controlpanel.ControlPanelManagerViewEventListener;
import edu.cmu.ri.createlab.expressionbuilder.controlpanel.DeviceGUI;
import edu.cmu.ri.createlab.expressionbuilder.controlpanel.ServiceControlPanel;
import edu.cmu.ri.createlab.terk.expression.XmlExpression;
import edu.cmu.ri.createlab.terk.expression.manager.ExpressionFile;
import edu.cmu.ri.createlab.terk.expression.manager.ExpressionFileListModel;
import edu.cmu.ri.createlab.terk.expression.manager.ExpressionFileManagerView;
import edu.cmu.ri.createlab.terk.services.ServiceManager;
import edu.cmu.ri.createlab.userinterface.GUIConstants;
import edu.cmu.ri.createlab.userinterface.component.Spinner;
import edu.cmu.ri.createlab.userinterface.util.SwingUtils;
import edu.cmu.ri.createlab.visualprogrammer.PathManager;
import edu.cmu.ri.createlab.visualprogrammer.VisualProgrammer;
import edu.cmu.ri.createlab.visualprogrammer.VisualProgrammerDevice;
import edu.cmu.ri.createlab.xml.LocalEntityResolver;
import edu.cmu.ri.createlab.xml.SaveXmlDocumentDialogRunnable;
import edu.cmu.ri.createlab.xml.XmlHelper;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
@SuppressWarnings({"CloneableClassWithoutClone"})
public final class ExpressionBuilder
   {
   private static final Logger LOG = Logger.getLogger(ExpressionBuilder.class);

   private static final PropertyResourceBundle RESOURCES = (PropertyResourceBundle)PropertyResourceBundle.getBundle(ExpressionBuilder.class.getName());

   private final JFrame jFrame;
   private final JPanel stagePanel = new JPanel();
   private final JPanel mainPanel = new JPanel();
   private final JPanel controlPanel = new JPanel();
   private final Spinner spinnerPanel = new Spinner(RESOURCES.getString("label.scanning"));
   private final ControlPanelManager controlPanelManager = new ControlPanelManagerImpl();
   private final ControlPanelManagerView controlPanelManagerView = new ControlPanelManagerView(controlPanelManager);

   private final ExpressionFileListModel expressionFileListModel = new ExpressionFileListModel();
   private final ExpressionFileManagerView expressionFileManagerView = new ExpressionFileManagerView(expressionFileListModel, GUIConstants.FONT_NORMAL);
   private final ExpressionFileManagerControlsView expressionFileManagerControlsView;
   private final StageControlsView stageControlsView;
   private final Runnable jFramePackingRunnable =
         new Runnable()
            {
            public void run()
               {
               jFrame.pack();
               }
            };
   private final Runnable showSpinnerAndConnectRunnable =
         new Runnable()
            {
            @Override
            public void run()
               {
               // show the spinner
               mainPanel.removeAll();

               final GroupLayout mainPanelLayout = new GroupLayout(mainPanel);
               mainPanel.setLayout(mainPanelLayout);
               mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
               mainPanel.setName("mainAppPanel");
               mainPanelLayout.setHorizontalGroup(
                     mainPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                           .addComponent(spinnerPanel)
               );
               mainPanelLayout.setVerticalGroup(
                     mainPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                           .addComponent(spinnerPanel)
               );

               jFrame.pack();
               jFrame.setLocationRelativeTo(null);// center the window on the screen
               }
            };

   private ServiceManager serviceManager = null;
   private CreateLabDeviceProxy createLabDeviceProxy = null;

   public ExpressionBuilder(@NotNull final JFrame jFrame,
                            @NotNull final VisualProgrammerDevice visualProgrammerDevice,
                            @NotNull final VisualProgrammer.TabSwitcher tabSwitcher)
      {
      XmlHelper.setLocalEntityResolver(LocalEntityResolver.getInstance());

      this.jFrame = jFrame;

      // Register the ExpressionFileListModel as a listener to the PathManager's expressions DirectoryPoller
      PathManager.getInstance().registerExpressionsFileEventListener(expressionFileListModel);

      stageControlsView =
            new StageControlsView(
                  controlPanelManager,

                  new StageControlsController()
                     {
                     public void clearControlPanels()
                        {
                        controlPanelManager.reset();
                        setStageTitle("Untitled");
                        }

                     public void refreshControlPanels()
                        {
                        controlPanelManager.refresh();
                        }

                     public void saveExpression(@Nullable final String filename, @Nullable final SaveXmlDocumentDialogRunnable.EventHandler eventHandler)
                        {
                        LOG.debug("ExpressionBuilder.saveExpression(" + filename + ")");
                        final XmlExpression xmlExpression = controlPanelManager.buildExpression();
                        final String xmlDocumentString = xmlExpression == null ? null : xmlExpression.toXmlDocumentStringFormatted();
                        final SaveXmlDocumentDialogRunnable runnable =
                              new SaveXmlDocumentDialogRunnable(xmlDocumentString,
                                                                filename,
                                                                PathManager.getInstance().getExpressionsZipSave(),
                                                                jFrame,
                                                                RESOURCES)
                                 {
                                 @Override
                                 protected void performUponSuccessfulSave(final String savedFilenameWithoutExtension)
                                    {
                                    if (eventHandler != null)
                                       {
                                       eventHandler.handleSuccessfulSave(savedFilenameWithoutExtension);
                                       }
                                    }
                                 };
                        SwingUtils.runInGUIThread(runnable);
                        }
                     },
                  jFrame
            );

      expressionFileManagerControlsView = new ExpressionFileManagerControlsView(this,
                                                                                jFrame,
                                                                                controlPanelManager,
                                                                                expressionFileManagerView,
                                                                                expressionFileListModel,
                                                                                new ExpressionFileManagerControlsController()
                                                                                   {
                                                                                   public void openExpression(final XmlExpression expression)
                                                                                      {
                                                                                      controlPanelManager.loadExpression(expression);
                                                                                      }

                                                                                   public void deleteExpression(final ExpressionFile expressionFile)
                                                                                      {
                                                                                      if (expressionFile != null)
                                                                                         {
                                                                                         final String file = expressionFile.getFileName();
                                                                                         PathManager.getInstance().getExpressionsZipSave().deleteFile(file);

                                                                                         if (file != null)
                                                                                            {
                                                                                            final boolean success = !PathManager.getInstance().getExpressionsZipSave().exist(file);
                                                                                            if (LOG.isInfoEnabled())
                                                                                               {
                                                                                               LOG.info("ExpressionBuilder.deleteExpression(): " + (success ? "deleted" : "failed to delete") + " expression file [" + file + "]");
                                                                                               }
                                                                                            }
                                                                                         }
                                                                                      }
                                                                                   },
                                                                                stageControlsView.getOpenButton(),
                                                                                stageControlsView.getSettingsButton(),
                                                                                tabSwitcher,
                                                                                visualProgrammerDevice.getExportableLanguages());

      // CONTROL PANEL MANAGER -----------------------------------------------------------------------------------------

      controlPanelManager.addControlPanelManagerEventListener(
            new ControlPanelManagerEventListener()
               {
               @Override
               public void handleDeviceConnectedEvent(final Map<String, ServiceControlPanel> serviceControlPanelMap)
                  {
                  // !
                  PathManager.getInstance().forceExpressionsDirectoryPollerRefresh();
                  }

               @Override
               public void handleDeviceDisconnectedEvent()
                  {
                  // nothing to do
                  }

               @Override
               public void handleDeviceActivityStatusChange(final String serviceTypeId, final int deviceIndex, final boolean active)
                  {
                  // nothing to do
                  }
               }
      );
      // make sure we re-pack the jFrame whenever the control panel manager changes
      controlPanelManagerView.addControlPanelManagerViewEventListener(
            new ControlPanelManagerViewEventListener()
               {
               public void handleLayoutChange()
                  {
                  if (SwingUtilities.isEventDispatchThread())
                     {
                     jFramePackingRunnable.run();
                     }
                  else
                     {
                     SwingUtilities.invokeLater(jFramePackingRunnable);
                     }
                  }
               });

      // GUI WIDGETS ---------------------------------------------------------------------------------------------------

      expressionFileManagerView.setEnabled(false);
      expressionFileManagerControlsView.setEnabled(false);

      stageControlsView.setEnabled(false);

      final JScrollPane stageScrolling = new JScrollPane(controlPanelManagerView.getComponent());

      // LAYOUT --------------------------------------------------------------------------------------------------------

      stagePanel.setLayout(new GridBagLayout());

      stagePanel.setName("stagePanel");

      final GridBagConstraints c = new GridBagConstraints();

      c.fill = GridBagConstraints.HORIZONTAL;
      c.gridwidth = 1;
      c.gridheight = 1;
      c.gridx = 0;
      c.gridy = 0;
      c.weighty = 0.0;
      c.weightx = 1.0;
      c.anchor = GridBagConstraints.LINE_START;
      c.insets = new Insets(0, 0, 0, 0);
      stagePanel.add(stageControlsView.getComponent(), c);

      c.fill = GridBagConstraints.BOTH;
      c.gridwidth = 1;
      c.gridheight = 1;
      c.gridx = 0;
      c.gridy = 1;
      c.weighty = 1.0;
      c.weightx = 1.0;
      c.anchor = GridBagConstraints.LINE_START;
      c.insets = new Insets(5, 0, 0, 0);
      stagePanel.add(stageScrolling, c);

      final JPanel expressionFileManagerPanel = new JPanel();
      final TitledBorder titledBorder = BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), RESOURCES.getString("expressions-file-manager-panel.title"));
      titledBorder.setTitleFont(GUIConstants.FONT_NORMAL);
      titledBorder.setTitleColor(Color.BLACK);
      //expressionFileManagerPanel.setBorder(BorderFactory.createTitledBorder(titledBorder));

      expressionFileManagerPanel.setName("expressionFileManager");
      expressionFileManagerPanel.setBorder(BorderFactory.createMatteBorder(4, 4, 4, 4, new Color(197, 193, 235)));

      final JPanel fileListHolder = new JPanel(new GridBagLayout());
      fileListHolder.setMinimumSize(new Dimension(180, 200));
      fileListHolder.setPreferredSize(new Dimension(180, 200));

      final GridBagConstraints gbc = new GridBagConstraints();

      gbc.fill = GridBagConstraints.BOTH;
      gbc.gridx = 0;
      gbc.gridy = 0;
      gbc.weighty = 1.0;
      gbc.weightx = 1.0;
      gbc.anchor = GridBagConstraints.CENTER;
      fileListHolder.add(expressionFileManagerView.getComponent(), gbc);
      fileListHolder.setBorder(titledBorder);
      fileListHolder.setName("expressionFileManager");

      expressionFileManagerPanel.setLayout(new GridBagLayout());

      gbc.fill = GridBagConstraints.BOTH;
      gbc.gridx = 0;
      gbc.gridy = 0;
      gbc.weighty = 1.0;
      gbc.weightx = 1.0;
      gbc.anchor = GridBagConstraints.PAGE_START;
      gbc.insets = new Insets(5, 0, 0, 0);
      expressionFileManagerPanel.add(fileListHolder, gbc);

      gbc.fill = GridBagConstraints.HORIZONTAL;
      gbc.gridx = 0;
      gbc.gridy = 1;
      gbc.weighty = 0.0;
      gbc.weightx = 1.0;
      gbc.anchor = GridBagConstraints.PAGE_END;
      gbc.insets = new Insets(0, 5, 5, 5);
      expressionFileManagerPanel.add(expressionFileManagerControlsView.getComponent(), gbc);

      controlPanel.setName("controlPanel");
      final GroupLayout controlPanelLayout = new GroupLayout(controlPanel);
      controlPanel.setLayout(controlPanelLayout);
      controlPanelLayout.setHorizontalGroup(
            controlPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
                  .addComponent(expressionFileManagerPanel)
      );
      controlPanelLayout.setVerticalGroup(
            controlPanelLayout.createSequentialGroup()
                  .addComponent(expressionFileManagerPanel)
      );
      //controlPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

      // we're already connected, so perform post-connect setup
      performPostConnectSetup(visualProgrammerDevice);
      }

   public void openExpression(@Nullable final ExpressionFile expressionFile)
      {
      expressionFileManagerControlsView.openExpression(expressionFile);
      }

   public JPanel getPanel()
      {
      return mainPanel;
      }

   public void setStageTitle(final String str)
      {
      stageControlsView.setStageTitle(str);
      }

   public void performPostConnectSetup(@NotNull final VisualProgrammerDevice visualProgrammerDevice)
      {
      // TODO: this is an ugly mix of stuff that should happen on the Swing thread, and stuff that shouldn't...fix that someday.

      createLabDeviceProxy = visualProgrammerDevice.getDeviceProxy();
      serviceManager = visualProgrammerDevice.getServiceManager();
      final DeviceGUI deviceGUI = visualProgrammerDevice.getExpressionBuilderDevice().getDeviceGUI();
      controlPanelManagerView.setDeviceGUI(deviceGUI);
      //I'm like 85% sure this code isn't needed because visual programmer handles ping failures
      /*
      createLabDeviceProxy.addCreateLabDevicePingFailureEventListener(
            new CreateLabDevicePingFailureEventListener()
            {
            @Override
            public void handlePingFailureEvent()
               {
               LOG.debug("ExpressionBuilder.handlePingFailureEvent()");
               disconnectFromDevice();
               }
            });
      */
      mainPanel.removeAll();

      //final GroupLayout mainPanelLayout = new GroupLayout(mainPanel);
      mainPanel.setLayout(new GridBagLayout());
      mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
      mainPanel.setName("mainAppPanel");

      final GridBagConstraints gbc = new GridBagConstraints();

      gbc.fill = GridBagConstraints.BOTH;
      gbc.gridx = 0;
      gbc.gridy = 0;
      gbc.weighty = 1.0;
      gbc.weightx = 1.0;
      gbc.anchor = GridBagConstraints.CENTER;
      mainPanel.add(stagePanel, gbc);

      gbc.fill = GridBagConstraints.NONE;
      gbc.gridx = 1;
      gbc.gridy = 0;
      gbc.weighty = 1.0;
      gbc.weightx = 0.0;
      gbc.anchor = GridBagConstraints.CENTER;
      mainPanel.add(SwingUtils.createRigidSpacer(5), gbc);

      gbc.fill = GridBagConstraints.VERTICAL;
      gbc.gridx = 2;
      gbc.gridy = 0;
      gbc.weighty = 1.0;
      gbc.weightx = 0.0;
      gbc.anchor = GridBagConstraints.CENTER;
      mainPanel.add(controlPanel, gbc);

      deviceGUI.setStageTitleField(stageControlsView.getStageTitleComponent());

      expressionFileManagerView.setEnabled(true);
      stageControlsView.setEnabled(true);
      expressionFileManagerControlsView.setEnabled(true);
      controlPanelManager.deviceConnected(serviceManager);

      PathManager.getInstance().forceExpressionsDirectoryPollerRefresh();

      jFrame.pack();
      jFrame.setLocationRelativeTo(null); // center the window on the screen
      }

   private void disconnectFromDevice()
      {
      if (isConnected())
         {
         LOG.debug("ExpressionBuilder.disconnectFromDevice(): performing post-disconnect cleanup...");

         performPostDisconnectCleanup();

         // show the spinner and try to reconnect
         SwingUtils.runInGUIThread(showSpinnerAndConnectRunnable);
         }
      else
         {
         LOG.info("ExpressionBuilder.disconnectFromDevice(): doing nothing since we're already disconnected.");
         }
      }

   public void performPostDisconnectCleanup()
      {
      createLabDeviceProxy = null;
      serviceManager = null;
      controlPanelManagerView.setDeviceGUI(null);

      expressionFileManagerView.setEnabled(false);
      stageControlsView.setEnabled(false);
      expressionFileManagerControlsView.setEnabled(false);
      controlPanelManager.deviceDisconnected();
      }

   private boolean isConnected()
      {
      return serviceManager != null && createLabDeviceProxy != null;
      }

   public void shutdown()
      {
      LOG.debug("ExpressionBuilder.shutdown()");
      disconnectFromDevice();
      }
   }
