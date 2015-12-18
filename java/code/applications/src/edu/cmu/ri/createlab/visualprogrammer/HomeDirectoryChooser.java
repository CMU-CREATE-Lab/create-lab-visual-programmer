package edu.cmu.ri.createlab.visualprogrammer;

import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.text.MessageFormat;
import java.util.PropertyResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import edu.cmu.ri.createlab.userinterface.GUIConstants;
import edu.cmu.ri.createlab.userinterface.util.SwingUtils;
import edu.cmu.ri.createlab.visualprogrammer.lookandfeel.VisualProgrammerLookAndFeelLoader;
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
      void onDirectoryChosen(@NotNull final File homeDirectory, File projectDirectory);
      }

   private static final Logger LOG = Logger.getLogger(HomeDirectoryChooser.class);
   private static final PropertyResourceBundle RESOURCES = (PropertyResourceBundle)PropertyResourceBundle.getBundle(HomeDirectoryChooser.class.getName());
   private static final String OPEN_NEW_PROJECT = RESOURCES.getString("label.open-new-project");
   private static final String CREATE_NEW_PROJECT = RESOURCES.getString("label.create-new-project");
   private static final String PROJECT_DEFAULT_LOCATION = VisualProgrammerConstants.FilePaths.DEFAULT_VISUAL_PROGRAMMER_HOME_DIR.getParent();

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
      //  final JLabel text2 = SwingUtils.createLabel(RESOURCES.getString("text.startup-explanation2"));

      final JCheckBox rememberHomeDirectoryCheckBox = createRememberDirectoryCheckBox("label.remember-home-directory");
      final JButton createNewProjectButton = SwingUtils.createButton(CREATE_NEW_PROJECT, true);
      final JButton openProjectButton = openProjectChooser(jFrame, eventHandler);

      // This action will open a frame to get the project name and location
      createNewProjectButton.addActionListener(
            new ActionListener()
            {

            File dir = new File(PROJECT_DEFAULT_LOCATION);
            JFrame frame;

            JLabel text1 = SwingUtils.createLabel(RESOURCES.getString("text.heading-create-project"), new Font("Serif", Font.BOLD, 18));
            JLabel text2 = SwingUtils.createLabel(RESOURCES.getString("text.heading2-enter-projectname"), new Font("Serif", Font.PLAIN, 14));
            JLabel label_getProjectName = SwingUtils.createLabel(RESOURCES.getString("label.project-name"), new Font("Serif", Font.PLAIN, 14));
            JLabel label_getLocation = SwingUtils.createLabel(RESOURCES.getString("label.project-location"), new Font("Serif", Font.PLAIN, 14));

            JTextField projectName = new JTextField();
            JTextField projectLocation = new JTextField();

            JButton browserLocationB;
            JButton ok_button;
            JButton cancel;

            @Override
            public void actionPerformed(ActionEvent actionEvent)
               {
               if (frame == null)
                  {
                  //Initialize frame
                  frame = new JFrame("Visual Programmer New Project");
                  frame.setLayout(new FlowLayout());
                  frame.setSize(630, 200);
                  frame.setLocationRelativeTo(null);
                  frame.setResizable(false);

                  //ProjectLocation
                  projectLocation.setFont(new Font("Serif", Font.PLAIN, 14));
                  projectLocation.setText(PROJECT_DEFAULT_LOCATION);

                  ok_button = createProject();
                  browserLocationB = selectLocation();
                  cancel = cancel();

                  //Body
                  JPanel body = new JPanel();
                  GroupLayout bodyLayout = bodyLayout(body);
                  body.setLayout(bodyLayout);
                  bodyLayout.setAutoCreateGaps(true);
                  bodyLayout.setAutoCreateContainerGaps(true);

                  frame.add(body);
                  frame.setVisible(true);
                  }
               }

            private JButton createProject()
               {
               ok_button = SwingUtils.createButton("Create", true);
               ok_button.addActionListener(
                     new ActionListener()
                     {
                     public void actionPerformed(final ActionEvent actionEvent)
                        {
                        File homeDirectory = null;
                        File projectDirectory = null;

                        if (isValidName())

                           {
                           if (PathManager.getInstance().isValidDirectory(dir))
                              {
                              //Directory
                              projectLocation.setText(dir.getAbsolutePath());
                              homeDirectory = new File(dir.getPath() + File.separator + projectName.getText());
                              projectDirectory = new File(homeDirectory + File.separator + projectName.getText() + ".zip");

                              userPreferences.setHomeDirectory(homeDirectory);
                              userPreferences.setProjectDirectory(projectDirectory);
                              eventHandler.onDirectoryChosen(homeDirectory, projectDirectory);

                              PathManager.getInstance().createZipProject(projectDirectory.getPath());
                              frame.dispose();
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
                        else
                           {

                           JOptionPane.showMessageDialog(jFrame,
                                                         RESOURCES.getString("message.invalid-projectname"),
                                                         RESOURCES.getString("title.invalid-projectname"),
                                                         JOptionPane.ERROR_MESSAGE);
                           }
                        }
                     });

               return ok_button;
               }

            //Check that the name is only letters or numbers
            private boolean isValidName()
               {

               String projectName = this.projectName.getText();

               Pattern alphaNum = Pattern.compile("[^a-z0-9 _-]", Pattern.CASE_INSENSITIVE);
               Matcher alphaNumTest = alphaNum.matcher(projectName);
               if (alphaNumTest.find() || projectName.isEmpty())
                  {
                  return false;
                  }
               else
                  {
                  this.projectName.setText(getUniqueName(projectName)); //if the name is repeated it adds an "_" and a number
                  return true;
                  }
               }

            private String getUniqueName(String projectName)
               {
               String prettyName = projectName;
               File temp = new File(dir, prettyName);
               while (temp.exists())
                  {

                  if (prettyName.contains("_") && isNumber(prettyName.substring(prettyName.lastIndexOf("_") + 1)))
                     {
                     final String expression = prettyName.substring(prettyName.lastIndexOf("_"));
                     final int num = Integer.valueOf(prettyName.substring(prettyName.lastIndexOf("_") + 1));
                     prettyName = prettyName.replace(expression, "_" + (num + 1));
                     temp = new File(temp.getParent(), prettyName);
                     }
                  else
                     {
                     prettyName += "_1";
                     temp = new File(temp.getParent(), prettyName);
                     }
                  }
               return prettyName;
               }

            //Method needed for getUniqueName
            private boolean isNumber(final String str)
               {
               try
                  {
                  Integer.parseInt(str);
                  return true;
                  }
               catch (final NumberFormatException e)
                  {
                  return false;
                  }
               }

            private JButton selectLocation()
               {
               VisualProgrammerLookAndFeelLoader.getInstance().resetLookAndFeel();
               final JFileChooser fc = new JFileChooser(PROJECT_DEFAULT_LOCATION);
               VisualProgrammerLookAndFeelLoader.getInstance().loadLookAndFeel();
               fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

               final JButton chooseButton = SwingUtils.createButton("Browser", true);
               chooseButton.addActionListener(
                     new ActionListener()
                     {
                     public void actionPerformed(final ActionEvent actionEvent)
                        {
                        final int returnVal = fc.showOpenDialog(jFrame);

                        if (returnVal == JFileChooser.APPROVE_OPTION)
                           {
                           dir = fc.getSelectedFile();
                           if (PathManager.getInstance().isValidDirectory(dir))
                              {
                              if (LOG.isDebugEnabled())
                                 {
                                 LOG.debug("HomeDirectoryChooser.actionPerformed(): VALID directory: " + dir.getAbsolutePath());
                                 }
                              projectLocation.setText(dir.getAbsolutePath());
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

            //Layout for the Create Project Frame
            private GroupLayout bodyLayout(JPanel panel)
               {

               GroupLayout layout = new GroupLayout(panel);

               layout.setHorizontalGroup(
                     layout.createSequentialGroup()
                           .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                           .addComponent(label_getProjectName)
                                           .addComponent(label_getLocation))
                           .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                           .addComponent(text1)
                                           .addComponent(text2)
                                           .addComponent(projectName)
                                           .addComponent(projectLocation)
                                           .addGroup(layout.createSequentialGroup()
                                                           .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                                                           .addComponent(ok_button))
                                                           .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                                                           .addComponent(cancel))))
                           .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                           .addComponent(browserLocationB))

               );

               layout.setVerticalGroup(
                     layout.createSequentialGroup()
                           .addComponent(text1)
                           .addComponent(text2)
                           .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                           .addComponent(label_getProjectName)
                                           .addComponent(projectName))
                           .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                           .addComponent(label_getLocation)
                                           .addComponent(projectLocation)
                                           .addComponent(browserLocationB))
                           .addGap(30)
                           .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                           .addComponent(ok_button)
                                           .addComponent(cancel))
               );

               return layout;
               }

            private JButton cancel()
               {
               JButton cancel_button = SwingUtils.createButton("Cancel", true);

               cancel_button.addActionListener(new ActionListener()
               {
               @Override
               public void actionPerformed(final ActionEvent actionEvent)
                  {
                  frame.dispose();
                  frame = null;
                  }
               });

               return cancel_button;
               }
            });

      final GroupLayout contentPanelLayout = new GroupLayout(contentPanel);
      contentPanelLayout.setAutoCreateGaps(true);
      contentPanelLayout.setAutoCreateContainerGaps(true);
      contentPanel.setLayout(contentPanelLayout);

      contentPanelLayout.setHorizontalGroup(
            contentPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
                  .addComponent(text1)
                        // .addComponent(text2)
                        // .addGap(GAP * 2)
                  .addGroup(contentPanelLayout.createSequentialGroup()
                                  .addComponent(createNewProjectButton)
                                  .addGap(GAP)
                                  .addComponent(openProjectButton))
                  .addGap(GAP)
            // .addComponent(rememberHomeDirectoryCheckBox)
      );
      contentPanelLayout.setVerticalGroup(
            contentPanelLayout.createSequentialGroup()
                  .addComponent(text1)
                        //.addComponent(text2)
                        // .addGap(GAP * 2)
                  .addGroup(contentPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
                                  .addComponent(createNewProjectButton)
                                  .addGap(GAP)
                                  .addComponent(openProjectButton))
                  .addGap(GAP)
            // .addComponent(rememberHomeDirectoryCheckBox)
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
                                                                       userPreferences.getProjectDirectory()),
                                                  GUIConstants.MONOSPACED_FONT_NORMAL);
      final String text3WhenRemembering = RESOURCES.getString("text.settings-panel-explanation3-when-remembering");
      final String text3WhenNotRemembering = RESOURCES.getString("text.settings-panel-explanation3-when-not-remembering");
      //final JLabel text3 = SwingUtils.createLabel(userPreferences.shouldRememberHomeDirectory() ? text3WhenRemembering : text3WhenNotRemembering);
      final JLabel text3 = SwingUtils.createLabel(RESOURCES.getString("text.settings-panel-explanation3-change-project"));

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
            //  .addComponent(rememberHomeDirectoryCheckBox)
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
            // .addComponent(rememberHomeDirectoryCheckBox)
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

   private JButton openProjectChooser(@NotNull final JFrame jFrame,
                                      @NotNull final EventHandler eventHandler)
      {
      VisualProgrammerLookAndFeelLoader.getInstance().resetLookAndFeel();
      final JFileChooser fc = new JFileChooser(PROJECT_DEFAULT_LOCATION);
      VisualProgrammerLookAndFeelLoader.getInstance().loadLookAndFeel();
      fc.setFileSelectionMode(JFileChooser.FILES_ONLY);

      FileFilter filter = new FileNameExtensionFilter("Zip File", "zip");
      fc.addChoosableFileFilter(filter);

      final JButton chooseButton = SwingUtils.createButton(OPEN_NEW_PROJECT, true);
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

                  if (PathManager.getInstance().isValidZip(dir))
                     {

                     if (LOG.isDebugEnabled())
                        {
                        LOG.debug("HomeDirectoryChooser.actionPerformed(): VALID directory: " + dir.getAbsolutePath());
                        }

                     userPreferences.setHomeDirectory(dir.getParentFile());
                     userPreferences.setProjectDirectory(dir);
                     eventHandler.onDirectoryChosen(dir.getParentFile(), dir);
                     }
                  else
                     {
                     if (LOG.isEnabledFor(Level.ERROR))
                        {
                        LOG.error("HomeDirectoryChooser.actionPerformed(): INVALID directory: " + ((dir == null) ? null : dir.getAbsolutePath()));
                        }

                     JOptionPane.showMessageDialog(jFrame,
                                                   RESOURCES.getString("title.invalid-project"),
                                                   RESOURCES.getString("message.invalid-project"),
                                                   JOptionPane.ERROR_MESSAGE);

                     if (LOG.isDebugEnabled())
                        {
                        LOG.debug("HomeDirectoryChooser.actionPerformed(): VALID directory: " + dir.getAbsolutePath());
                        }
                     }
                  }
               }
            });

      return chooseButton;
      }
   }

