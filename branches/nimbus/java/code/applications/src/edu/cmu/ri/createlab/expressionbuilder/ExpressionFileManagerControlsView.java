package edu.cmu.ri.createlab.expressionbuilder;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.text.MessageFormat;
import java.util.PropertyResourceBundle;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileSystemView;
import javax.swing.filechooser.FileView;
import javax.swing.plaf.metal.MetalFileChooserUI;
import javax.swing.plaf.metal.MetalLookAndFeel;

import edu.cmu.ri.createlab.expressionbuilder.controlpanel.ControlPanelManager;
import edu.cmu.ri.createlab.terk.expression.XmlExpression;
import edu.cmu.ri.createlab.terk.expression.manager.ExpressionFile;
import edu.cmu.ri.createlab.terk.expression.manager.ExpressionFileListModel;
import edu.cmu.ri.createlab.terk.expression.manager.ExpressionFileManagerView;
import edu.cmu.ri.createlab.userinterface.util.AbstractTimeConsumingAction;
import edu.cmu.ri.createlab.userinterface.util.DialogHelper;
import edu.cmu.ri.createlab.userinterface.util.ImageUtils;
import edu.cmu.ri.createlab.userinterface.util.SwingUtils;
import edu.cmu.ri.createlab.visualprogrammer.VisualProgrammer;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.Nullable;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
@SuppressWarnings({"CloneableClassWithoutClone"})
final class ExpressionFileManagerControlsView
   {
   private static final PropertyResourceBundle RESOURCES = (PropertyResourceBundle)PropertyResourceBundle.getBundle(ExpressionFileManagerControlsView.class.getName());

   private final JPanel panel = new JPanel();

   private final JButton openButton;
   private final JButton settingsButton;
   private final JButton deleteButton = SwingUtils.createButton(RESOURCES.getString("button.label.delete"));
   private final Runnable setEnabledRunnable = new SetEnabledRunnable(true);
   private final Runnable setDisabledRunnable = new SetEnabledRunnable(false);

   private final JFrame jFrame;

   private final JFileChooser fc = new JFileChooser();

  private static final Logger LOG = Logger.getLogger(ExpressionFileManagerControlsView.class);

   private final ExpressionFileManagerView expressionFileManagerView;
   private final ExpressionFileListModel expressionFileListModel;
   private final ExpressionFileManagerControlsController expressionFileManagerControlsController;

   private final ControlPanelManager controlPanelManager;

   @Nullable
   private final VisualProgrammer.TabSwitcher tabSwitcher;

   private final ExpressionBuilder builderApp;

   ExpressionFileManagerControlsView(final ExpressionBuilder build,
                                     final JFrame jFrame,
                                     final ControlPanelManager controlPanelManager,
                                     final ExpressionFileManagerView expressionFileManagerView,
                                     final ExpressionFileListModel expressionFileListModel,
                                     final ExpressionFileManagerControlsController expressionFileManagerControlsController,
                                     final JButton open,
                                     final JButton settings,
                                     @Nullable final VisualProgrammer.TabSwitcher tabSwitcher)
      {
      this.jFrame = jFrame;
      this.expressionFileManagerView = expressionFileManagerView;
      this.expressionFileListModel = expressionFileListModel;
      this.expressionFileManagerControlsController = expressionFileManagerControlsController;
      this.builderApp = build;
      this.openButton = open;
      this.settingsButton = settings;
      this.controlPanelManager = controlPanelManager;
      this.tabSwitcher = tabSwitcher;

      UIManager.put("FileChooser.readOnly", Boolean.TRUE);

      fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
      fc.setAcceptAllFileFilterUsed(false);
      fc.setName("FolderChooser");
      fc.setControlButtonsAreShown(true);
      FileView fv = fc.getFileView();


      FileView newIconFiles = new FileView() {
          @Override
          public Icon getIcon(File f) {

              if (getExtension(f)!= null && !(fc.getTypeDescription(f).equals("Application") || fc.getTypeDescription(f).equals("Shortcut") || fc.getTypeDescription(f).equals("File folder"))){
                  return (Icon)ImageUtils.createImageIcon("/edu/cmu/ri/createlab/expressionbuilder/images/file_icons/file.png");
              }
              else if (fc.getTypeDescription(f).equals("Application") || fc.getTypeDescription(f).equals("Shortcut")){
                  return (Icon)ImageUtils.createImageIcon("/edu/cmu/ri/createlab/expressionbuilder/images/gear.png");
              }
              else if (fc.getFileSystemView().isFileSystem(f)){//fc.getTypeDescription(f).equals("File folder")){
                  return (Icon)ImageUtils.createImageIcon("/edu/cmu/ri/createlab/expressionbuilder/images/file_icons/directory.png");
              }
              else if (fc.getFileSystemView().isDrive(f)){//fc.getTypeDescription(f).equals("Local Disk")){
                  return (Icon)ImageUtils.createImageIcon("/edu/cmu/ri/createlab/expressionbuilder/images/file_icons/harddrive.png");
              }
              else{
                  return (Icon)ImageUtils.createImageIcon("/edu/cmu/ri/createlab/expressionbuilder/images/file_icons/computer.png");//;    //To change body of overridden methods use File | Settings | File Templates.
                }
          }
      };


      File tempFile = null;
      try {
          tempFile = File.createTempFile("demo",".tmp");
          FileOutputStream fout = new FileOutputStream(tempFile);
          PrintStream out = new PrintStream(fout);
          out.println("This is sample text in the temporary file which is created");
      } catch (IOException e) {
          e.printStackTrace();
      }

      fc.setFileView(newIconFiles);
      fc.updateUI();

      deleteButton.setIcon(ImageUtils.createImageIcon("/edu/cmu/ri/createlab/expressionbuilder/images/deleteMark.png"));
      deleteButton.setFocusable(false);
      //deleteButton.setMnemonic(KeyEvent.VK_D);

      openButton.setFocusable(false);
      openButton.setIcon(ImageUtils.createImageIcon("/edu/cmu/ri/createlab/expressionbuilder/images/openIcon.png"));

      settingsButton.setFocusable(false);
      settingsButton.setIcon(ImageUtils.createImageIcon("/edu/cmu/ri/createlab/expressionbuilder/images/gear.png"));
      //settingsButton.setIcon((ImageIcon)UIManager.get("FileChooser.newFolderIcon"));
      //settingsButton.setIcon(fc.getIcon(fc.getFileSystemView().getHomeDirectory()));

              settingsButton.setEnabled(true);

      panel.setLayout(new GridBagLayout());
      panel.setBackground(Color.WHITE);

      final GridBagConstraints gbc = new GridBagConstraints();

      gbc.fill = GridBagConstraints.BOTH;
      gbc.gridx = 0;
      gbc.gridy = 0;
      gbc.weighty = 0.0;
      gbc.weightx = 1.0;
      gbc.gridwidth = 1;
      gbc.anchor = GridBagConstraints.PAGE_END;
      gbc.insets = new Insets(0,0,5,0);
      panel.add(openButton, gbc);

      gbc.fill = GridBagConstraints.BOTH;
      gbc.gridx = 1;
      gbc.gridy = 0;
      gbc.weighty = 0.0;
      gbc.weightx = 1.0;
      gbc.anchor = GridBagConstraints.PAGE_END;
      gbc.insets = new Insets(0,5,5,0);
      panel.add(settingsButton, gbc);

      gbc.fill = GridBagConstraints.BOTH;
      gbc.gridwidth = 2;
      gbc.gridx = 0;
      gbc.gridy = 1;
      gbc.weighty = 0.0;
      gbc.weightx = 1.0;
      gbc.insets = new Insets(0,0,0,0);
      gbc.anchor = GridBagConstraints.PAGE_END;

      panel.add(deleteButton, gbc);

      // change enabled state of button depending on whether an item in the list is selected
      expressionFileManagerView.addListSelectionListener(
            new ListSelectionListener()
            {
            public void valueChanged(final ListSelectionEvent e)
               {
               final boolean isListItemSelected = !expressionFileManagerView.isSelectionEmpty();
               openButton.setEnabled(isListItemSelected);
               deleteButton.setEnabled(isListItemSelected);
               settingsButton.setEnabled(true);
               }
            });

      // double-clicking should cause the expression to be opened
      expressionFileManagerView.addMouseListener(
            new MouseAdapter()
            {
            public void mouseClicked(final MouseEvent e)
               {
               if (e.getClickCount() == 2)
                  {
                  openSelectedExpression();
                  }
               }
            });

      // clicking the Open button should open the selected expression
      openButton.addActionListener(
            new ActionListener()
            {
            @Override
            public void actionPerformed(final ActionEvent actionEvent)
               {
               openSelectedExpression();
               }
            }
      );

      settingsButton.addActionListener(new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent e) {
              chooseFolderAction(e);
          }
      });

      // clicking the Delete button should delete the selected expression
      deleteButton.addActionListener(new DeleteExpressionAction());
      }



   Component getComponent()
      {
      return panel;
      }



    String getExtension(File f) {
        String ext = null;
        String s = f.getName();
        int i = s.lastIndexOf('.');

        if (i > 0 && i < s.length() - 1) {
            ext = s.substring(i + 1).toLowerCase();
        }
        return ext;
    }









   public void setEnabled(final boolean isEnabled)
      {
      SwingUtils.runInGUIThread(isEnabled ? setEnabledRunnable : setDisabledRunnable);
      }

   private void openSelectedExpression()
      {
      final int selectedIndex = expressionFileManagerView.getSelectedIndex();

      if (selectedIndex >= 0)
         {
         openExpression(expressionFileListModel.getNarrowedElementAt(selectedIndex));
         }
      }

   public void openExpression(@Nullable final ExpressionFile expressionFile)
      {
      if (expressionFile != null)
         {
         SwingUtils.runInGUIThread(
               new Runnable()
               {
               @Override
               public void run()
                  {
                  // Build an expression from what's currently on the stage in order to determine whether
                  // there's anything that the user might want to keep.
                  final XmlExpression xmlExpression = controlPanelManager.buildExpression();
                  final String xmlDocumentString = xmlExpression == null ? null : xmlExpression.toXmlDocumentStringFormatted();

                  // prompt the user, if necessary
                  boolean willOpenExpression = true;
                  if (xmlDocumentString != null)
                     {
                     final String message = MessageFormat.format(RESOURCES.getString("dialog.message.open-expression-confirmation"), expressionFile.getPrettyName());
                     if (!DialogHelper.showYesNoDialog(RESOURCES.getString("dialog.title.open-expression-confirmation"), message, jFrame))
                        {
                        willOpenExpression = false;
                        }
                     }

                  if (willOpenExpression)
                     {
                     // make sure the Expression Builder tab has focus (we do this since expressions might be loaded
                     // from the Sequence Builder tab)
                     if (tabSwitcher != null)
                        {
                        tabSwitcher.showExpressionBuilderTab();
                        }

                     // make sure the expression is highlighted in the list (it might not be if the expressions is
                     // being opened from the Sequence Builder tab)
                     expressionFileManagerView.setSelectedIndex(expressionFileListModel.indexOf(expressionFile));

                     // open the expression
                     final SwingWorker sw =
                           new SwingWorker<Object, Object>()
                           {
                           @Override
                           protected Object doInBackground() throws Exception
                              {
                              expressionFileManagerControlsController.openExpression(expressionFile.getExpression());

                              return null;
                              }

                           @Override
                           protected void done()
                              {
                              builderApp.setStageTitle(expressionFile.getPrettyName());
                              }
                           };
                     sw.execute();
                     }
                  }
               });
         }
      }

   private class SetEnabledRunnable implements Runnable
      {
      private final boolean isEnabled;

      private SetEnabledRunnable(final boolean isEnabled)
         {
         this.isEnabled = isEnabled;
         }

      public void run()
         {
         openButton.setEnabled(isEnabled && !expressionFileManagerView.isSelectionEmpty());
         deleteButton.setEnabled(isEnabled && !expressionFileManagerView.isSelectionEmpty());
         }
      }

   private final class DeleteExpressionAction extends AbstractTimeConsumingAction
      {
      private ExpressionFile expressionFile = null;

      protected void executeGUIActionBefore()
         {
         final int selectedIndex = expressionFileManagerView.getSelectedIndex();
         if (selectedIndex >= 0)
            {
            expressionFile = expressionFileListModel.getNarrowedElementAt(selectedIndex);

            final String message = MessageFormat.format(RESOURCES.getString("dialog.message.delete-expression-confirmation"),
                                                        expressionFile.getPrettyName());
            final int selectedOption = JOptionPane.showConfirmDialog(jFrame,
                                                                     message,
                                                                     RESOURCES.getString("dialog.title.delete-expression-confirmation"),
                                                                     JOptionPane.YES_NO_OPTION,
                                                                     JOptionPane.WARNING_MESSAGE);

            if (selectedOption != JOptionPane.YES_OPTION)
               {
               expressionFile = null;
               }
            }
         }

      protected Object executeTimeConsumingAction()
         {
         if (expressionFile != null)
            {
            expressionFileManagerControlsController.deleteExpression(expressionFile);
            expressionFileManagerView.getComponent().repaint();
            }
         return null;
         }
      }

      private void chooseFolderAction (ActionEvent e) {
          //Handle open button action.
          LOG.debug("Home Type Description: " + fc.getTypeDescription(fc.getFileSystemView().getHomeDirectory()));

          if (e.getSource() == settingsButton) {
              int returnVal = fc.showOpenDialog(jFrame);
              fc.updateUI();
              if (returnVal == JFileChooser.APPROVE_OPTION) {
                  File file = fc.getSelectedFile();
                  //This is where a real application would open the file.
                  LOG.debug("Opening: " + file.getName() + ".");
                  LOG.debug("Type Description: " + fc.getTypeDescription(file));
              } else {
                  LOG.debug("Open command cancelled by user.");
              }
          }
      }


   }
