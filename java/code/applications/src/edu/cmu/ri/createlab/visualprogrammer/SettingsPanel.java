package edu.cmu.ri.createlab.visualprogrammer;

import java.awt.Component;
import java.text.MessageFormat;
import java.util.PropertyResourceBundle;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import edu.cmu.ri.createlab.userinterface.GUIConstants;
import edu.cmu.ri.createlab.userinterface.util.SwingUtils;
import edu.cmu.ri.createlab.util.StandardVersionNumber;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
final class SettingsPanel
   {
   private static final Logger LOG = Logger.getLogger(SettingsPanel.class);
   private static final PropertyResourceBundle RESOURCES = (PropertyResourceBundle)PropertyResourceBundle.getBundle(SettingsPanel.class.getName());

   private final JPanel mainPanel = new JPanel();
   private final JPanel softwareUpdatePanel = new JPanel();
   private final JPanel homeDirectoryPanel = new JPanel();
   private final JPanel aboutPanel = new JPanel();
   private final JFrame jFrame;

   SettingsPanel(@NotNull final JFrame jFrame,
                 @NotNull final StandardVersionNumber currentVersionNumber,
                 @NotNull final UpdateChecker updateChecker)
      {
      this.jFrame = jFrame;
      mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
      mainPanel.setName("mainAppPanel");
      final GroupLayout mainPanelLayout = new GroupLayout(mainPanel);
      mainPanelLayout.setAutoCreateGaps(true);
      mainPanelLayout.setAutoCreateContainerGaps(true);
      mainPanel.setLayout(mainPanelLayout);

      softwareUpdatePanel.setName("softwareUpdatePanel");
      homeDirectoryPanel.setName("homeDirectoryPanel");
      aboutPanel.setName("aboutPanel");

      // build the homeDirectoryPanel
      buildHomeDirectoryPanel();

      // build the aboutPanel
      buildAboutPanel();

      // do the layout for the main panel
      mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                  .addComponent(softwareUpdatePanel)
                  .addComponent(homeDirectoryPanel)
                  .addComponent(aboutPanel)
      );
      mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createSequentialGroup()
                  .addComponent(softwareUpdatePanel)
                  .addComponent(homeDirectoryPanel)
                  .addComponent(aboutPanel)
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

                        setSoftwareUpdateContent(new HtmlPane(text));
                        }
                     });
               }
            }
      );
      }

   private void buildHomeDirectoryPanel()
      {
      final GroupLayout layout = new GroupLayout(homeDirectoryPanel);
      layout.setAutoCreateGaps(true);
      layout.setAutoCreateContainerGaps(true);
      homeDirectoryPanel.setLayout(layout);

      final JLabel title = SwingUtils.createLabel(RESOURCES.getString("label.section.home-directory"), GUIConstants.FONT_LARGE);

      final JPanel contentPanel = HomeDirectoryChooser.getInstance().createChooserPanelForSettingsTab();
      contentPanel.setName("homeDirectoryPanelContent");

      layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                  .addComponent(title)
                  .addComponent(contentPanel)
      );
      layout.setVerticalGroup(
            layout.createSequentialGroup()
                  .addComponent(title)
                  .addComponent(contentPanel)
      );
      }

   private void buildAboutPanel()
      {
      final GroupLayout layout = new GroupLayout(aboutPanel);
      layout.setAutoCreateGaps(true);
      layout.setAutoCreateContainerGaps(true);
      aboutPanel.setLayout(layout);

      final JLabel title = SwingUtils.createLabel(RESOURCES.getString("label.section.about"), GUIConstants.FONT_LARGE);

      final JPanel contentPanel = new JPanel();
      contentPanel.setName("aboutPanelContent");

      // TODO
      contentPanel.add(new JLabel(RESOURCES.getString("label.section.about.content")));

      layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                  .addComponent(title)
                  .addComponent(contentPanel)
      );
      layout.setVerticalGroup(
            layout.createSequentialGroup()
                  .addComponent(title)
                  .addComponent(contentPanel)
      );
      }

   private void setSoftwareUpdateContent(@NotNull final Component content)
      {
      softwareUpdatePanel.removeAll();

      final GroupLayout softwareUpdatePanelLayout = new GroupLayout(softwareUpdatePanel);
      softwareUpdatePanelLayout.setAutoCreateGaps(true);
      softwareUpdatePanelLayout.setAutoCreateContainerGaps(true);
      softwareUpdatePanel.setLayout(softwareUpdatePanelLayout);

      final JLabel softwareUpdateTitle = new JLabel(RESOURCES.getString("label.section.software-update"));
      softwareUpdateTitle.setFont(GUIConstants.FONT_LARGE);

      softwareUpdatePanelLayout.setHorizontalGroup(
            softwareUpdatePanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                  .addComponent(softwareUpdateTitle)
                  .addComponent(content)
      );
      softwareUpdatePanelLayout.setVerticalGroup(
            softwareUpdatePanelLayout.createSequentialGroup()
                  .addComponent(softwareUpdateTitle)
                  .addComponent(content)
      );
      }

   JPanel getPanel()
      {
      return mainPanel;
      }
   }
