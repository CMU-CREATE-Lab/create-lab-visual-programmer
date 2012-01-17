package edu.cmu.ri.createlab.visualprogrammer;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;
import java.util.PropertyResourceBundle;
import java.util.concurrent.ExecutionException;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.WindowConstants;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import edu.cmu.ri.createlab.device.CreateLabDevicePingFailureEventListener;
import edu.cmu.ri.createlab.device.CreateLabDeviceProxy;
import edu.cmu.ri.createlab.expressionbuilder.ExpressionBuilder;
import edu.cmu.ri.createlab.sequencebuilder.SequenceBuilder;
import edu.cmu.ri.createlab.sequencebuilder.SequenceExecutor;
import edu.cmu.ri.createlab.terk.services.ServiceManager;
import edu.cmu.ri.createlab.userinterface.util.DialogHelper;
import edu.cmu.ri.createlab.userinterface.util.ImageUtils;
import edu.cmu.ri.createlab.visualprogrammer.lookandfeel.VisualProgrammerLookAndFeelLoader;
import edu.cmu.ri.createlab.xml.LocalEntityResolver;
import edu.cmu.ri.createlab.xml.XmlHelper;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public final class VisualProgrammer
   {
   private static final Logger LOG = Logger.getLogger(VisualProgrammer.class);

   private static final PropertyResourceBundle RESOURCES = (PropertyResourceBundle)PropertyResourceBundle.getBundle(VisualProgrammer.class.getName());

   private static final String APPLICATION_NAME = RESOURCES.getString("application.name");
   private static final String VERSION_NUMBER = RESOURCES.getString("version.number");
   private static final String APPLICATION_NAME_AND_VERSION_NUMBER = APPLICATION_NAME + " v" + VERSION_NUMBER;
   private final JFrame jFrame;

   public interface TabSwitcher
      {
      /** Switches to the Expression Builder tab. This method assumes it's being called from the Swing thread. */
      void showExpressionBuilderTab();

      /** Switches to the Sequence Builder tab. This method assumes it's being called from the Swing thread. */
      void showSequenceBuilderTab();
      }

   public static void main(final String[] args)
      {
      // Load the look and feel
      VisualProgrammerLookAndFeelLoader.getInstance().loadLookAndFeel();

      //Schedule a job for the event-dispatching thread: creating and showing this application's GUI.
      SwingUtilities.invokeLater(
            new Runnable()
            {
            public void run()
               {
               final JFrame jFrame = new JFrame(APPLICATION_NAME_AND_VERSION_NUMBER);

               final VisualProgrammer application = new VisualProgrammer(jFrame);

               // set various properties for the JFrame
               jFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
               jFrame.setBackground(Color.WHITE);
               jFrame.setPreferredSize(new Dimension(1024, 728));
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
                                    application.shutdown();
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

               jFrame.addWindowFocusListener(
                     new WindowAdapter()
                     {
                     public void windowGainedFocus(final WindowEvent e)
                        {
                        jFrame.repaint();
                        }
                     });

               jFrame.addComponentListener(
                     new ComponentAdapter()
                     {
                     @Override
                     public void componentResized(final ComponentEvent e)
                        {
                        final Component source = e.getComponent();
                        if (source.equals(jFrame))
                           {
                           final Dimension size = source.getSize();
                           //jFrame.setMinimumSize(size);
                           //jFrame.setMaximumSize(size);
                           jFrame.setPreferredSize(size);
                           }
                        }
                     });

               jFrame.pack();
               jFrame.setLocationRelativeTo(null);    // center the window on the screen
               jFrame.setVisible(true);
               }
            });
      }

   private ServiceManager serviceManager = null;
   private CreateLabDeviceProxy createLabDeviceProxy = null;
   private final VisualProgrammerDeviceImplementationClassLoader visualProgrammerDeviceImplementationClassLoader = new VisualProgrammerDeviceImplementationClassLoader();

   private ExpressionBuilder expressionBuilder = null;
   private SequenceBuilder sequenceBuilder = null;

   private final JPanel mainPanel = new JPanel();
   final JLabel hintsGraphic = new JLabel(ImageUtils.createImageIcon("/edu/cmu/ri/createlab/expressionbuilder/images/animated_tips2.gif"));
   final JLabel connectingGraphic = new JLabel(ImageUtils.createImageIcon("/edu/cmu/ri/createlab/expressionbuilder/images/animated_scroll.gif"));
   private final JTabbedPane tabbedPane = new JTabbedPane();

   private VisualProgrammer(@NotNull final JFrame jFrame)
      {
      this.jFrame = jFrame;

      XmlHelper.setLocalEntityResolver(LocalEntityResolver.getInstance());

      //Add decorative border to spinnerPanel
      final Border purple = BorderFactory.createMatteBorder(4, 4, 4, 4, new Color(197, 193, 235));
      hintsGraphic.setBorder(purple);

      // Configure the main panel
      showSpinner();

      // connect to the device...
      connectToDevice();
      }

   private void connectToDevice()
      {
      if (isConnected())
         {
         LOG.info("VisualProgrammer.connectToDevice(): doing nothing since we're already connected.");
         }
      else
         {
         // connect to the device...
         final SwingWorker<VisualProgrammerDevice, Object> sw =
               new SwingWorker<VisualProgrammerDevice, Object>()
               {
               @Override
               protected VisualProgrammerDevice doInBackground() throws Exception
                  {
                  LOG.debug("VisualProgrammer.connectToDevice(): connecting to device...");

                  // first get the class names
                  final List<VisualProgrammerDevice> visualProgrammerDevices = visualProgrammerDeviceImplementationClassLoader.loadImplementationClasses();

                  if (visualProgrammerDevices.size() > 0)
                     {
                     // TODO: present the user with a choice.  For now, just take the first one...
                     final VisualProgrammerDevice visualProgrammerDevice = visualProgrammerDevices.get(0);

                     // connect to the device...
                     visualProgrammerDevice.connect();
                     LOG.debug("VisualProgrammer.connectToDevice(): Connected!");

                     createLabDeviceProxy = visualProgrammerDevice.getDeviceProxy();
                     serviceManager = visualProgrammerDevice.getServiceManager();

                     createLabDeviceProxy.addCreateLabDevicePingFailureEventListener(
                           new CreateLabDevicePingFailureEventListener()
                           {
                           @Override
                           public void handlePingFailureEvent()
                              {
                              LOG.debug("VisualProgrammer.handlePingFailureEvent(): disconnecting from device...");
                              disconnectFromDevice();

                              LOG.debug("VisualProgrammer.handlePingFailureEvent(): attempting reconnection to device...");
                              connectToDevice();   // TODO: is this good enough?
                              }
                           });

                     return visualProgrammerDevice;
                     }
                  else
                     {
                     // TODO: alert the user before shutting down
                     LOG.error("Could not find any valid implementations of class VisualProgrammerDevice.  Will now exit.");
                     System.exit(1);
                     }

                  return null;
                  }

               @Override
               protected void done()
                  {
                  try
                     {

                     LOG.error("Starting VisualProgrammerDevice");
                     final VisualProgrammerDevice visualProgrammerDevice = get();

                     PathManager.getInstance().setVisualProgrammerDevice(visualProgrammerDevice);
                     expressionBuilder = new ExpressionBuilder(jFrame, visualProgrammerDevice,
                                                               new TabSwitcher()
                                                               {
                                                               @Override
                                                               public void showExpressionBuilderTab()
                                                                  {
                                                                  tabbedPane.setSelectedIndex(0);
                                                                  }

                                                               @Override
                                                               public void showSequenceBuilderTab()
                                                                  {
                                                                  tabbedPane.setSelectedIndex(1);
                                                                  }
                                                               });
                     sequenceBuilder = new SequenceBuilder(jFrame, visualProgrammerDevice, expressionBuilder);

                     final JPanel placeholderPanel = new JPanel();
                     placeholderPanel.add(new JLabel("Something will go here."));

                     // clear everything out of the mainPanel and recreate it

                     mainPanel.removeAll();
                     tabbedPane.removeAll();
                     tabbedPane.setFocusable(false);
                     tabbedPane.addTab(RESOURCES.getString("expression-builder-tab.name"), expressionBuilder.getPanel());
                     tabbedPane.addTab(RESOURCES.getString("sequence-builder-tab.name"), sequenceBuilder.getPanel());
                     tabbedPane.setFont(new Font("Verdana", Font.PLAIN, 11));

                     tabbedPane.setMnemonicAt(0, KeyEvent.VK_E);
                     tabbedPane.setMnemonicAt(1, KeyEvent.VK_Q);

                     tabbedPane.addChangeListener(
                           new ChangeListener()
                           {
                           @Override
                           public void stateChanged(final ChangeEvent changeEvent)
                              {
                              if (tabbedPane.getSelectedIndex() == 0)
                                 {
                                 // If a sequence is playing, then ask the user whether she wants to stop it now that the Expression Builder tab is visible
                                 if (SequenceExecutor.getInstance().isRunning())
                                    {
                                    if (DialogHelper.showYesNoDialog(RESOURCES.getString("dialog.title.confirm-stop-sequence-playback-when-expression-builder-tab-gets-focus"),
                                                                     RESOURCES.getString("dialog.message.confirm-stop-sequence-playback-when-expression-builder-tab-gets-focus")))
                                       {
                                       SequenceExecutor.getInstance().stop();
                                       }
                                    }
                                 }
                              }
                           }
                     );
                     jFrame.setPreferredSize(new Dimension(1024, 728));

                     mainPanel.setLayout(new GridBagLayout());

                     final GridBagConstraints c = new GridBagConstraints();
                     c.fill = GridBagConstraints.BOTH;
                     c.gridx = 0;
                     c.gridy = 0;
                     c.weighty = 1.0;
                     c.weightx = 1.0;
                     c.anchor = GridBagConstraints.CENTER;
                     mainPanel.add(tabbedPane, c);

                     jFrame.pack();
                     jFrame.repaint();
                     jFrame.setLocationRelativeTo(null);    // center the window on the screen
                     }
                  catch (InterruptedException e)
                     {
                     LOG.error("InterruptedException while trying to get the visualProgrammerDevice", e);
                     }
                  catch (ExecutionException e)
                     {
                     LOG.error("ExecutionException while trying to get the visualProgrammerDevice", e);
                     }
                  }
               };
         sw.execute();
         }
      }

   private void disconnectFromDevice()
      {
      if (isConnected())
         {
         LOG.debug("VisualProgrammer.disconnectFromDevice(): disconnecting from device...");

         try
            {
            createLabDeviceProxy.disconnect();
            LOG.debug("VisualProgrammer.disconnectFromDevice(): Disconnected!");
            }
         catch (Exception e)
            {
            LOG.error("Exception while trying to disconnect from the device.  Ignoring.", e);
            }
         finally
            {
            createLabDeviceProxy = null;
            serviceManager = null;

            PathManager.getInstance().setVisualProgrammerDevice(null);

            expressionBuilder.performPostDisconnectCleanup();
            //TODO: sequenceBuilder.performPostDisconnectCleanup();

            expressionBuilder = null;
            sequenceBuilder = null;

            showSpinner();
            }
         }
      else
         {
         LOG.info("VisualProgrammer.disconnectFromDevice(): doing nothing since we're already disconnected.");
         }
      }

   private boolean isConnected()
      {
      return serviceManager != null && createLabDeviceProxy != null;
      }

   private void shutdown()
      {
      LOG.debug("VisualProgrammer.shutdown()");

      if (SequenceExecutor.getInstance().isRunning())
         {
         LOG.debug("VisualProgrammer.shutdown(): stopping sequence playback");
         SequenceExecutor.getInstance().stop();
         }

      LOG.debug("VisualProgrammer.shutdown(): disconnecting from device");
      disconnectFromDevice();
      }

   private void showSpinner()
      {
      // TODO: do this in the Swing thread, and factor this out somewhere since it's duplicated in main()

      mainPanel.removeAll();

      mainPanel.setLayout(new GridBagLayout());

      final GridBagConstraints gbc = new GridBagConstraints();
      gbc.fill = GridBagConstraints.NONE;
      gbc.gridx = 0;
      gbc.gridy = 0;
      gbc.weighty = 0.5;
      gbc.weightx = 1.0;
      gbc.anchor = GridBagConstraints.PAGE_END;
      gbc.insets = new Insets(0, 0, 20, 0);
      mainPanel.add(connectingGraphic, gbc);

      gbc.fill = GridBagConstraints.NONE;
      gbc.gridx = 0;
      gbc.gridy = 1;
      gbc.weighty = 0.0;
      gbc.weightx = 1.0;
      gbc.insets = new Insets(0, 0, 0, 0);
      gbc.anchor = GridBagConstraints.PAGE_START;
      mainPanel.add(hintsGraphic, gbc);

      final JLabel versionLabel = new JLabel(APPLICATION_NAME_AND_VERSION_NUMBER);

      gbc.fill = GridBagConstraints.NONE;
      gbc.gridx = 0;
      gbc.gridy = 2;
      gbc.weighty = 0.5;
      gbc.weightx = 1.0;
      gbc.anchor = GridBagConstraints.LAST_LINE_START;
      mainPanel.add(versionLabel, gbc);

      jFrame.setLayout(new GridBagLayout());

      gbc.fill = GridBagConstraints.BOTH;
      gbc.gridx = 0;
      gbc.gridy = 0;
      gbc.weighty = 1.0;
      gbc.weightx = 1.0;
      gbc.anchor = GridBagConstraints.CENTER;
      jFrame.add(mainPanel, gbc);

      mainPanel.setName("mainPanel");
      versionLabel.setName("versionNumber");

      jFrame.pack();
      jFrame.repaint();
      jFrame.setLocationRelativeTo(null);
      }
   }
