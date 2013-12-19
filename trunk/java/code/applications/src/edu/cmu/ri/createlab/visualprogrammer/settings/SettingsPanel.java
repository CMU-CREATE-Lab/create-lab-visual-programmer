package edu.cmu.ri.createlab.visualprogrammer.settings;

import java.awt.Component;
import java.awt.Desktop;
import java.text.MessageFormat;
import java.util.PropertyResourceBundle;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import edu.cmu.ri.createlab.userinterface.GUIConstants;
import edu.cmu.ri.createlab.util.StandardVersionNumber;
import edu.cmu.ri.createlab.visualprogrammer.UpdateChecker;
import edu.cmu.ri.createlab.visualprogrammer.VisualProgrammerConstants;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public final class SettingsPanel
   {
   private static final Logger LOG = Logger.getLogger(SettingsPanel.class);
   private static final PropertyResourceBundle RESOURCES = (PropertyResourceBundle)PropertyResourceBundle.getBundle(SettingsPanel.class.getName());

   private final JPanel mainPanel = new JPanel();
   private final JPanel versionInfoPanel = new JPanel();

   public SettingsPanel(@NotNull final StandardVersionNumber currentVersionNumber, @NotNull final UpdateChecker updateChecker)
      {
      mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
      mainPanel.setName("mainAppPanel");
      final GroupLayout mainPanelLayout = new GroupLayout(mainPanel);
      mainPanelLayout.setAutoCreateGaps(true);
      mainPanelLayout.setAutoCreateContainerGaps(true);
      mainPanel.setLayout(mainPanelLayout);

      versionInfoPanel.setName("softwareUpdatePanel");

      mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                  .addComponent(versionInfoPanel)
      );
      mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createSequentialGroup()
                  .addComponent(versionInfoPanel)
      );

      updateChecker.addUpdateCheckResultListener(
            new UpdateChecker.UpdateCheckResultListener()
            {
            @Override
            public void handleUpdateCheckResult(final boolean wasCheckSuccessful,
                                                final boolean isUpdateAvailable,
                                                @Nullable final StandardVersionNumber versionNumberOfUpdate)
               {
               // Make sure this happens in the Swing thread...
               SwingUtilities.invokeLater(
                     new Runnable()
                     {
                     @Override
                     public void run()
                        {
                        final String text;
                        if (wasCheckSuccessful)
                           {
                           if (isUpdateAvailable && versionNumberOfUpdate != null)
                              {
                              text = MessageFormat.format(RESOURCES.getString("label.update-available"),
                                                          currentVersionNumber.toString(),
                                                          versionNumberOfUpdate.toString(),
                                                          VisualProgrammerConstants.Urls.VISUAL_PROGRAMMER_SOFTWARE_HOME);
                              }
                           else
                              {
                              text = MessageFormat.format(RESOURCES.getString("label.no-update-available"),
                                                          currentVersionNumber.toString());
                              }
                           }
                        else
                           {
                           text = MessageFormat.format(RESOURCES.getString("label.update-check-failed"),
                                                       VisualProgrammerConstants.Urls.VISUAL_PROGRAMMER_SOFTWARE_HOME);
                           }

                        setVersionInfoContent(new HtmlPane(text));
                        }
                     });
               }
            }
      );
      }

   private void setVersionInfoContent(@NotNull final Component content)
      {
      versionInfoPanel.removeAll();

      final GroupLayout versionInfoPanelLayout = new GroupLayout(versionInfoPanel);
      versionInfoPanelLayout.setAutoCreateGaps(true);
      versionInfoPanelLayout.setAutoCreateContainerGaps(true);
      versionInfoPanel.setLayout(versionInfoPanelLayout);

      final JLabel versionInfoTitle = new JLabel(RESOURCES.getString("label.section.software-update"));
      versionInfoTitle.setFont(GUIConstants.FONT_LARGE);

      versionInfoPanelLayout.setHorizontalGroup(
            versionInfoPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                  .addComponent(versionInfoTitle)
                  .addComponent(content)
      );
      versionInfoPanelLayout.setVerticalGroup(
            versionInfoPanelLayout.createSequentialGroup()
                  .addComponent(versionInfoTitle)
                  .addComponent(content)
      );
      }

   public JPanel getPanel()
      {
      return mainPanel;
      }

   private static final class HtmlPane extends JEditorPane
      {
      private HtmlPane(final String htmlContent)
         {
         super("text/html", htmlContent);
         this.setEditable(false);
         this.addHyperlinkListener(
               new HyperlinkListener()
               {
               public void hyperlinkUpdate(final HyperlinkEvent event)
                  {
                  try
                     {
                     if (HyperlinkEvent.EventType.ACTIVATED.equals(event.getEventType()))
                        {
                        Desktop.getDesktop().browse(event.getURL().toURI());
                        }
                     }
                  catch (Exception e)
                     {
                     LOG.error("SettingsPanel$HtmlPane.hyperlinkUpdate():  Exception while trying to launch the link in the browser.", e);
                     }
                  }
               });
         }
      }
   }
