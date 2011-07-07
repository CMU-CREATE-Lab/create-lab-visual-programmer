package edu.cmu.ri.createlab.expressionbuilder;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;
import java.util.PropertyResourceBundle;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.WindowConstants;
import javax.swing.border.TitledBorder;
import edu.cmu.ri.createlab.device.CreateLabDevicePingFailureEventListener;
import edu.cmu.ri.createlab.device.CreateLabDeviceProxy;
import edu.cmu.ri.createlab.expressionbuilder.controlpanel.ControlPanelManager;
import edu.cmu.ri.createlab.expressionbuilder.controlpanel.ControlPanelManagerImpl;
import edu.cmu.ri.createlab.expressionbuilder.controlpanel.ControlPanelManagerView;
import edu.cmu.ri.createlab.expressionbuilder.controlpanel.ControlPanelManagerViewEventListener;
import edu.cmu.ri.createlab.terk.expression.XmlExpression;
import edu.cmu.ri.createlab.terk.expression.manager.ExpressionFile;
import edu.cmu.ri.createlab.terk.expression.manager.ExpressionFileManagerModel;
import edu.cmu.ri.createlab.terk.expression.manager.ExpressionFileManagerView;
import edu.cmu.ri.createlab.terk.services.ServiceManager;
import edu.cmu.ri.createlab.userinterface.GUIConstants;
import edu.cmu.ri.createlab.userinterface.util.AbstractTimeConsumingAction;
import edu.cmu.ri.createlab.userinterface.util.SwingUtils;
import edu.cmu.ri.createlab.visualprogrammer.lookandfeel.VisualProgrammerLookAndFeelLoader;
import org.apache.log4j.Logger;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
@SuppressWarnings({"CloneableClassWithoutClone"})
public final class ExpressionBuilder
   {
   private static final Logger LOG = Logger.getLogger(ExpressionBuilder.class);

   private static final PropertyResourceBundle RESOURCES = (PropertyResourceBundle)PropertyResourceBundle.getBundle(ExpressionBuilder.class.getName());

   private static final String APPLICATION_NAME = RESOURCES.getString("application.name");

   public static void main(final String[] args)
      {
      // Load the look and feel
      VisualProgrammerLookAndFeelLoader.getInstance().loadLookAndFeel();

      // Schedule a job for the event-dispatching thread: creating and showing this application's GUI.
      SwingUtilities.invokeLater(
            new Runnable()
            {
            public void run()
               {
               final JFrame jFrame = new JFrame(APPLICATION_NAME);

               final ExpressionBuilder expressionBuilder = new ExpressionBuilder(jFrame);

               // add the root panel to the JFrame
               jFrame.add(expressionBuilder.getPanel());

               // set various properties for the JFrame
               jFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
               jFrame.setName("appFrame");
               //jFrame.setBackground(Color.WHITE);
               jFrame.setResizable(true);
               jFrame.addWindowListener(
                     new WindowAdapter()
                     {
                     public void windowClosing(final WindowEvent event)
                        {
                        // ask if the user really wants to exit
                        final int selectedOption = JOptionPane.showConfirmDialog(jFrame,
                                                                                 RESOURCES.getString("dialog.message.exit-confirmation"),
                                                                                 RESOURCES.getString("dialog.title.exit-confirmation"),
                                                                                 JOptionPane.YES_NO_OPTION,
                                                                                 JOptionPane.QUESTION_MESSAGE);

                        if (selectedOption == JOptionPane.YES_OPTION)
                           {
                           final SwingWorker<Object, Object> worker =
                                 new SwingWorker<Object, Object>()
                                 {
                                 @Override
                                 protected Object doInBackground() throws Exception
                                    {
                                    expressionBuilder.shutdown();
                                    return null;
                                    }

                                 @Override
                                 protected void done()
                                    {
                                    System.exit(0);
                                    }
                                 };
                           worker.execute();
                           }
                        }
                     });

               jFrame.pack();
               jFrame.setLocationRelativeTo(null);// center the window on the screen
               jFrame.setVisible(true);
               }
            });
      }

   private final JFrame jFrame;
   private final JPanel stagePanel = new JPanel();
   private final JPanel mainPanel = new JPanel();
   private final ConnectDisconnectButton connectDisconnectButton;
   private final ControlPanelManager controlPanelManager = new ControlPanelManagerImpl();
   private final ControlPanelManagerView controlPanelManagerView = new ControlPanelManagerView(controlPanelManager);

   private final ExpressionFileManagerModel expressionFileManagerModel = new ExpressionFileManagerModel();
   private final ExpressionFileManagerView expressionFileManagerView = new ExpressionFileManagerView(expressionFileManagerModel, GUIConstants.FONT_NORMAL);
   private final ExpressionFileManagerControlsController expressionFileManagerControlsController =
         new ExpressionFileManagerControlsController()
         {
         public void openExpression(final XmlExpression expression)
            {
            controlPanelManager.loadExpression(expression);
            }

         public void deleteExpression(final ExpressionFile expressionFile)
            {
            expressionFileManagerModel.deleteExpression(expressionFile);
            }
         };
   private final ExpressionFileManagerControlsView expressionFileManagerControlsView;
   private final StageControlsView stageControlsView =
         new StageControlsView(
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

               public void saveExpression()
                  {
                  expressionFileManagerModel.saveExpression(controlPanelManager.buildExpression(), jFrame, stageControlsView.getStageTitle(), stageControlsView.getStageTitleComponent());
                  }
               }
         );
   private final Runnable jFramePackingRunnable =
         new Runnable()
         {
         public void run()
            {
            jFrame.pack();
            }
         };
   private ServiceManager serviceManager = null;
   private CreateLabDeviceProxy createLabDeviceProxy = null;
   private final ExpressionBuilderDeviceImplementationClassLoader expressionBuilderDeviceImplementationClassLoader = new ExpressionBuilderDeviceImplementationClassLoader();

   public ExpressionBuilder(final JFrame jFrame)
      {
      this.jFrame = jFrame;
      expressionFileManagerControlsView = new ExpressionFileManagerControlsView(this,
                                                                                jFrame,
                                                                                expressionFileManagerView,
                                                                                expressionFileManagerModel,
                                                                                expressionFileManagerControlsController);

      // CONTROL PANEL MANAGER -----------------------------------------------------------------------------------------

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

      connectDisconnectButton = new ConnectDisconnectButton();
      connectDisconnectButton.addActionListener(new ConnectDisconnectActionListener(jFrame));

      expressionFileManagerView.setEnabled(false);
      expressionFileManagerControlsView.setEnabled(false);

      stageControlsView.setEnabled(false);

      // LAYOUT --------------------------------------------------------------------------------------------------------

      final Component stageSpacer = SwingUtils.createRigidSpacer();
      final GroupLayout stagePanelLayout = new GroupLayout(stagePanel);

      stagePanel.setLayout(stagePanelLayout);

      stagePanel.setName("stagePanel");

      stagePanelLayout.setHorizontalGroup(
            stagePanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
                  .addComponent(stageControlsView.getComponent())
                  .addComponent(stageSpacer)
                  .addComponent(controlPanelManagerView.getComponent())
      );
      stagePanelLayout.setVerticalGroup(
            stagePanelLayout.createSequentialGroup()
                  .addComponent(stageControlsView.getComponent())
                  .addComponent(stageSpacer)
                  .addComponent(controlPanelManagerView.getComponent())
      );

      final Component expressionFileManagerPanelSpacer = SwingUtils.createRigidSpacer();
      final JPanel expressionFileManagerPanel = new JPanel();
      final TitledBorder titledBorder = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLACK, 1), RESOURCES.getString("expressions-file-manager-panel.title"));
      titledBorder.setTitleFont(GUIConstants.FONT_NORMAL);
      titledBorder.setTitleColor(Color.BLACK);
      expressionFileManagerPanel.setBorder(BorderFactory.createTitledBorder(titledBorder));

      expressionFileManagerPanel.setName("expressionFileManager");

      final GroupLayout expressionFileManagerPanelLayout = new GroupLayout(expressionFileManagerPanel);
      expressionFileManagerPanel.setLayout(expressionFileManagerPanelLayout);
      expressionFileManagerPanelLayout.setHorizontalGroup(
            expressionFileManagerPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
                  .addComponent(expressionFileManagerControlsView.getComponent())
                  .addComponent(expressionFileManagerPanelSpacer)
                  .addComponent(expressionFileManagerView.getComponent())
      );
      expressionFileManagerPanelLayout.setVerticalGroup(
            expressionFileManagerPanelLayout.createSequentialGroup()
                  .addComponent(expressionFileManagerControlsView.getComponent())
                  .addComponent(expressionFileManagerPanelSpacer)
                  .addComponent(expressionFileManagerView.getComponent())
      );

      final JPanel controlPanel = new JPanel();

      controlPanel.setName("controlPanel");
      final GroupLayout controlPanelLayout = new GroupLayout(controlPanel);
      controlPanel.setLayout(controlPanelLayout);
      controlPanelLayout.setHorizontalGroup(
            controlPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
                  .addComponent(connectDisconnectButton)
                  .addComponent(expressionFileManagerPanel)
      );
      controlPanelLayout.setVerticalGroup(
            controlPanelLayout.createSequentialGroup()
                  .addComponent(connectDisconnectButton)
                  .addComponent(expressionFileManagerPanel)
      );
      controlPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

      final GroupLayout mainPanelLayout = new GroupLayout(mainPanel);
      mainPanel.setLayout(mainPanelLayout);
      mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

      mainPanel.setName("mainAppPanel");

      mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createSequentialGroup()
                  .addComponent(stagePanel)
                  .addComponent(controlPanel)
      );
      mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                  .addComponent(stagePanel)
                  .addComponent(controlPanel)
      );
      }

   private JPanel getPanel()
      {
      return mainPanel;
      }

   public void setStageTitle(String str)
      {
      stageControlsView.setStageTitle(str);
      }

   public String getStageTitle()
      {
      return stageControlsView.getStageTitle();
      }

   public JTextField getStageTitleComponent()
      {
      return stageControlsView.getStageTitleComponent();
      }

   public Component getStageComponent()
      {
      return stagePanel;
      }

   public void loadExpression(final XmlExpression expression)
      {
      controlPanelManager.loadExpression(expression);
      }

   public void shutdown()
      {
      LOG.debug("ExpressionBuilder.shutdown()");
      disconnectFromDevice();
      }

   private void disconnectFromDevice()
      {
      if (isConnected())
         {
         LOG.debug("ExpressionBuilder.disconnectFromDevice(): disconnecting from device...");

         try
            {
            createLabDeviceProxy.disconnect();
            }
         catch (Exception e)
            {
            LOG.error("Exception while trying to disconnect from the device.  Ignoring.", e);
            }
         finally
            {
            createLabDeviceProxy = null;
            serviceManager = null;
            controlPanelManagerView.setDeviceGUI(null);
            connectDisconnectButton.setConnectionState(false);

            expressionFileManagerView.setEnabled(false);
            stageControlsView.setEnabled(false);
            expressionFileManagerControlsView.setEnabled(false);
            controlPanelManager.deviceDisconnected();
            }
         }
      else
         {
         LOG.info("ExpressionBuilder.disconnectFromDevice(): doing nothing since we're already disconnected.");
         }
      }

   private void connectToDevice()
      {
      if (!isConnected())
         {
         LOG.debug("ExpressionBuilder.connectToDevice(): connecting to device...");

         // first get the class names
         final List<ExpressionBuilderDevice> expressionBuilderDevices = expressionBuilderDeviceImplementationClassLoader.loadImplementationClasses();

         if (expressionBuilderDevices.size() > 0)
            {
            // TODO: present the user with a choice.  For now, just take the first one...
            final ExpressionBuilderDevice expressionBuilderDevice = expressionBuilderDevices.get(0);

            // connect to the device...
            expressionBuilderDevice.connect();

            createLabDeviceProxy = expressionBuilderDevice.getDeviceProxy();
            serviceManager = expressionBuilderDevice.getServiceManager();
            controlPanelManagerView.setDeviceGUI(expressionBuilderDevice.getDeviceGUI());
            connectDisconnectButton.setConnectionState(true);

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
            expressionBuilderDevice.setStageTitleField(stageControlsView.getStageTitleComponent());
            expressionFileManagerView.setEnabled(true);
            stageControlsView.setEnabled(true);
            expressionFileManagerControlsView.setEnabled(true);
            controlPanelManager.deviceConnected(serviceManager);
            }
         else
            {
            // TODO: alert the user before shutting down
            LOG.error("Could not find any valid implementations of class ExpressionBuilderDevice.  Will now exit.");
            System.exit(1);
            }
         }
      else
         {
         LOG.info("ExpressionBuilder.connectToDevice(): doing nothing since we're already connected.");
         }
      }

   private boolean isConnected()
      {
      return serviceManager != null && createLabDeviceProxy != null;
      }

   @SuppressWarnings({"CloneableClassWithoutClone"})
   private final class ConnectDisconnectActionListener extends AbstractTimeConsumingAction
      {
      private ConnectDisconnectActionListener(final Component parentComponent)
         {
         super(parentComponent);
         }

      @Override
      protected void executeGUIActionBefore()
         {
         connectDisconnectButton.setEnabled(false);
         }

      @Override
      protected Object executeTimeConsumingAction()
         {
         LOG.debug("ExpressionBuilder$ConnectDisconnectActionListener.executeTimeConsumingAction()");
         if (isConnected())
            {
            disconnectFromDevice();
            }
         else
            {
            connectToDevice();
            }
         return null;
         }

      @Override
      protected void executeGUIActionAfter(final Object resultOfTimeConsumingAction)
         {
         connectDisconnectButton.setEnabled(true);
         }
      }
   }
