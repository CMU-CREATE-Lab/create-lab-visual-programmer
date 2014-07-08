package edu.cmu.ri.createlab.visualprogrammer;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.text.MessageFormat;
import java.util.PropertyResourceBundle;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import edu.cmu.ri.createlab.userinterface.GUIConstants;
import edu.cmu.ri.createlab.userinterface.util.SwingUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
final class HomeDirectoryChooser
   {
   public interface EventHandler
      {
      /**
       * Responds to the choosing of the Visual Programmer home directory.  Assumes it is being executed on the Swing thread.
       */
      void onDirectoryChosen(@NotNull final File homeDirectory);
      }

   private static final Logger LOG = Logger.getLogger(HomeDirectoryChooser.class);

   private static final PropertyResourceBundle RESOURCES = (PropertyResourceBundle)PropertyResourceBundle.getBundle(HomeDirectoryChooser.class.getName());

   private static final String CHOOSE_LOCATION = RESOURCES.getString("label.choose-location");
   private static final String USE_DEFAULT_LOCATION = RESOURCES.getString("label.use-default-location");

   private static final int GAP = 10;

   @NotNull
   private final UserPreferences userPreferences;

   HomeDirectoryChooser(@NotNull final UserPreferences userPreferences)
      {
      this.userPreferences = userPreferences;
      }

   JPanel createChooserPanelForStartup(@NotNull final JFrame jFrame,
                                       @NotNull final EventHandler eventHandler)
      {
      final JPanel panel = new JPanel();
      panel.setName("homeDirectoryChooserPanel");

      final JLabel title = SwingUtils.createLabel(RESOURCES.getString("label.connected"), GUIConstants.FONT_LARGE);

      final JPanel contentPanel = new JPanel();
      contentPanel.setName("homeDirectoryChooserContentPanel");

      final JLabel text1 = SwingUtils.createLabel(RESOURCES.getString("text.startup-explanation1"));
      final JLabel text2 = SwingUtils.createLabel(RESOURCES.getString("text.startup-explanation2"));

      final JCheckBox rememberHomeDirectoryCheckBox = createRememberDirectoryCheckBox("label.remember-home-directory");
      final JButton useDefaultButton = SwingUtils.createButton(USE_DEFAULT_LOCATION, true);
      final JButton chooseLocationButton = createChooseLocationButton(jFrame, eventHandler);

      useDefaultButton.addActionListener(
            new ActionListener()
            {
            @Override
            public void actionPerformed(final ActionEvent actionEvent)
               {
               final File dir = VisualProgrammerConstants.FilePaths.DEFAULT_VISUAL_PROGRAMMER_HOME_DIR;
               userPreferences.setHomeDirectory(dir);
               eventHandler.onDirectoryChosen(dir);
               }
            });

      final GroupLayout contentPanelLayout = new GroupLayout(contentPanel);
      contentPanelLayout.setAutoCreateGaps(true);
      contentPanelLayout.setAutoCreateContainerGaps(true);
      contentPanel.setLayout(contentPanelLayout);

      contentPanelLayout.setHorizontalGroup(
            contentPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
                  .addComponent(text1)
                  .addComponent(text2)
                  .addGap(GAP * 2)
                  .addGroup(contentPanelLayout.createSequentialGroup()
                                  .addComponent(useDefaultButton)
                                  .addGap(GAP)
                                  .addComponent(chooseLocationButton))
                  .addGap(GAP)
                  .addComponent(rememberHomeDirectoryCheckBox)
      );
      contentPanelLayout.setVerticalGroup(
            contentPanelLayout.createSequentialGroup()
                  .addComponent(text1)
                  .addComponent(text2)
                  .addGap(GAP * 2)
                  .addGroup(contentPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
                                  .addComponent(useDefaultButton)
                                  .addGap(GAP)
                                  .addComponent(chooseLocationButton))
                  .addGap(GAP)
                  .addComponent(rememberHomeDirectoryCheckBox)
      );

      final GroupLayout panelLayout = new GroupLayout(panel);
      panelLayout.setAutoCreateGaps(true);
      panelLayout.setAutoCreateContainerGaps(true);
      panel.setLayout(panelLayout);

      panelLayout.setHorizontalGroup(
            panelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
                  .addComponent(title)
                  .addComponent(contentPanel)
      );
      panelLayout.setVerticalGroup(
            panelLayout.createSequentialGroup()
                  .addComponent(title)
                  .addComponent(contentPanel)
      );

      return panel;
      }

   JPanel createChooserPanelForSettingsTab()
      {

      final JLabel text1 = SwingUtils.createLabel(RESOURCES.getString("text.settings-panel-explanation1"));
      final JLabel text2 = SwingUtils.createLabel(MessageFormat.format(RESOURCES.getString("text.settings-panel-explanation2"),
                                                                       userPreferences.getHomeDirectory().getAbsolutePath()),
                                                  GUIConstants.MONOSPACED_FONT_NORMAL);
      final String text3WhenRemembering = RESOURCES.getString("text.settings-panel-explanation3-when-remembering");
      final String text3WhenNotRemembering = RESOURCES.getString("text.settings-panel-explanation3-when-not-remembering");
      final JLabel text3 = SwingUtils.createLabel(userPreferences.shouldRememberHomeDirectory() ? text3WhenRemembering : text3WhenNotRemembering);

      final JCheckBox rememberHomeDirectoryCheckBox = createRememberDirectoryCheckBox("label.dont-prompt-for-directory-on-startup");
      rememberHomeDirectoryCheckBox.addItemListener(
            new ItemListener()
            {
            @Override
            public void itemStateChanged(final ItemEvent itemEvent)
               {
               text3.setText(rememberHomeDirectoryCheckBox.isSelected() ? text3WhenRemembering : text3WhenNotRemembering);
               }
            }
      );
      final JPanel panel = new JPanel();
      panel.setName("homeDirectoryChooserContentPanel");

      final GroupLayout panelLayout = new GroupLayout(panel);
      panel.setLayout(panelLayout);

      panelLayout.setHorizontalGroup(
            panelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                  .addComponent(text1)
                  .addGap(GAP)
                  .addGroup(panelLayout.createSequentialGroup()
                                  .addGap(GAP * 2)
                                  .addComponent(text2))
                  .addComponent(text3)
                  .addGap(GAP)
                  .addComponent(rememberHomeDirectoryCheckBox)
      );
      panelLayout.setVerticalGroup(
            panelLayout.createSequentialGroup()
                  .addComponent(text1)
                  .addGap(GAP)
                  .addGroup(panelLayout.createParallelGroup()
                                  .addGap(GAP * 2)
                                  .addComponent(text2))
                  .addComponent(text3)
                  .addGap(GAP)
                  .addComponent(rememberHomeDirectoryCheckBox)
      );

      return panel;
      }

   private JCheckBox createRememberDirectoryCheckBox(final String labelPropertyKey)
      {
      final JCheckBox rememberHomeDirectoryCheckBox = new JCheckBox(RESOURCES.getString(labelPropertyKey),
                                                                    userPreferences.shouldRememberHomeDirectory());

      rememberHomeDirectoryCheckBox.addItemListener(
            new ItemListener()
            {
            @Override
            public void itemStateChanged(final ItemEvent itemEvent)
               {
               userPreferences.setShouldRememberHomeDirectory(rememberHomeDirectoryCheckBox.isSelected());
               }
            }
      );

      return rememberHomeDirectoryCheckBox;
      }

   private JButton createChooseLocationButton(@NotNull final JFrame jFrame,
                                              @NotNull final EventHandler eventHandler)
      {
      final JFileChooser fc = new JFileChooser();
      fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

      final JButton chooseButton = SwingUtils.createButton(CHOOSE_LOCATION, true);
      chooseButton.addActionListener(
            new ActionListener()
            {
            @Override
            public void actionPerformed(final ActionEvent actionEvent)
               {
               final int returnVal = fc.showOpenDialog(jFrame);

               if (returnVal == JFileChooser.APPROVE_OPTION)
                  {
                  final File dir = fc.getSelectedFile();

                  // Make sure the chosen directory exits, is a directory, and we have full access to it
                  if (PathManager.getInstance().isValidDirectory(dir))
                     {
                     if (LOG.isDebugEnabled())
                        {
                        LOG.debug("HomeDirectoryChooser.actionPerformed(): VALID directory: " + dir.getAbsolutePath());
                        }
                     userPreferences.setHomeDirectory(dir);
                     eventHandler.onDirectoryChosen(dir);
                     }
                  else
                     {
                     if (LOG.isEnabledFor(Level.ERROR))
                        {
                        LOG.error("HomeDirectoryChooser.actionPerformed(): INVALID directory: " + ((dir == null) ? null : dir.getAbsolutePath()));
                        }

                     JOptionPane.showMessageDialog(jFrame,
                                                   RESOURCES.getString("message.invalid-directory"),
                                                   RESOURCES.getString("title.invalid-directory"),
                                                   JOptionPane.ERROR_MESSAGE);
                     }
                  }
               }
            });

      return chooseButton;
      }
   }
