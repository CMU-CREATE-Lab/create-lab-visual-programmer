package edu.cmu.ri.createlab.expressionbuilder;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.MessageFormat;
import java.util.PropertyResourceBundle;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import edu.cmu.ri.createlab.expressionbuilder.controlpanel.ControlPanelManager;
import edu.cmu.ri.createlab.terk.expression.XmlExpression;
import edu.cmu.ri.createlab.terk.expression.manager.ExpressionFile;
import edu.cmu.ri.createlab.terk.expression.manager.ExpressionFileListModel;
import edu.cmu.ri.createlab.terk.expression.manager.ExpressionFileManagerView;
import edu.cmu.ri.createlab.userinterface.util.AbstractTimeConsumingAction;
import edu.cmu.ri.createlab.userinterface.util.DialogHelper;
import edu.cmu.ri.createlab.userinterface.util.ImageUtils;
import edu.cmu.ri.createlab.userinterface.util.SwingUtils;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
@SuppressWarnings({"CloneableClassWithoutClone"})
final class ExpressionFileManagerControlsView
   {
   private static final PropertyResourceBundle RESOURCES = (PropertyResourceBundle)PropertyResourceBundle.getBundle(ExpressionFileManagerControlsView.class.getName());

   private final JPanel panel = new JPanel();

   private final JButton openButton;// = SwingUtils.createButton(RESOURCES.getString("button.label.open"));
   private final JButton deleteButton = SwingUtils.createButton(RESOURCES.getString("button.label.delete"));
   private final Runnable setEnabledRunnable = new SetEnabledRunnable(true);
   private final Runnable setDisabledRunnable = new SetEnabledRunnable(false);

   private final JFrame jFrame;
   private final ExpressionFileManagerView fileManagerView;
   private final ExpressionFileListModel expressionFileListModel;
   private final ExpressionFileManagerControlsController expressionFileManagerControlsController;

   private final ControlPanelManager controlPanelManager;

   private final ExpressionBuilder builderApp;

   ExpressionFileManagerControlsView(final ExpressionBuilder build,
                                     final JFrame jFrame,
                                     final ControlPanelManager controlPanelManager,
                                     final ExpressionFileManagerView fileManagerView,
                                     final ExpressionFileListModel expressionFileListModel,
                                     final ExpressionFileManagerControlsController expressionFileManagerControlsController,
                                     final JButton open)
      {
      this.jFrame = jFrame;
      this.fileManagerView = fileManagerView;
      this.expressionFileListModel = expressionFileListModel;
      this.expressionFileManagerControlsController = expressionFileManagerControlsController;
      this.builderApp = build;
      this.openButton = open;
      this.controlPanelManager = controlPanelManager;

      deleteButton.setIcon(ImageUtils.createImageIcon("/edu/cmu/ri/createlab/expressionbuilder/images/deleteMark.png"));
      deleteButton.setFocusable(false);
      deleteButton.setMnemonic(KeyEvent.VK_D);

      panel.setLayout(new GridBagLayout());
      panel.setBackground(Color.WHITE);

      final GridBagConstraints gbc = new GridBagConstraints();

      gbc.fill = GridBagConstraints.HORIZONTAL;
      gbc.gridx = 0;
      gbc.gridy = 0;
      gbc.weighty = 0.0;
      gbc.weightx = 1.0;
      gbc.anchor = GridBagConstraints.PAGE_END;

      panel.add(deleteButton, gbc);
      final OpenExpressionAction openExpressionAction = new OpenExpressionAction();

      // change enabled state of button depending on whether an item in the list is selected
      fileManagerView.addListSelectionListener(
            new ListSelectionListener()
            {
            public void valueChanged(final ListSelectionEvent e)
               {
               final boolean isListItemSelected = !fileManagerView.isSelectionEmpty();
               openButton.setEnabled(isListItemSelected);
               deleteButton.setEnabled(isListItemSelected);
               }
            });

      // double-clicking should cause the expression to be opened
      fileManagerView.addMouseListener(
            new MouseAdapter()
            {
            public void mouseClicked(final MouseEvent e)
               {
               if (e.getClickCount() == 2)
                  {
                  openExpressionAction.actionPerformed(null);
                  }
               }
            });

      // clicking the Open button should open the selected expression
      openButton.addActionListener(openExpressionAction);

      // clicking the Delete button should delete the selected expression
      deleteButton.addActionListener(new DeleteExpressionAction());
      }

   Component getComponent()
      {
      return panel;
      }

   public void setEnabled(final boolean isEnabled)
      {
      SwingUtils.runInGUIThread(isEnabled ? setEnabledRunnable : setDisabledRunnable);
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
         openButton.setEnabled(isEnabled && !fileManagerView.isSelectionEmpty());
         deleteButton.setEnabled(isEnabled && !fileManagerView.isSelectionEmpty());
         }
      }

   // Used when an expression is double-clicked in the list
   private final class OpenExpressionAction extends AbstractTimeConsumingAction
      {
      private ExpressionFile expressionFile = null;

      protected void executeGUIActionBefore()
         {
         final int selectedIndex = fileManagerView.getSelectedIndex();

         if (selectedIndex >= 0)
            {
            // Build an expression from what's currently on the stage in order to determine whether
            // there's anything that the user might want to keep.
            final XmlExpression xmlExpression = controlPanelManager.buildExpression();
            final String xmlDocumentString = xmlExpression == null ? null : xmlExpression.toXmlDocumentStringFormatted();

            expressionFile = expressionFileListModel.getNarrowedElementAt(selectedIndex);
            if (xmlDocumentString != null)
               {
               final String message = MessageFormat.format(RESOURCES.getString("dialog.message.open-expression-confirmation"), expressionFile.getPrettyName());
               if (!DialogHelper.showYesNoDialog(RESOURCES.getString("dialog.title.open-expression-confirmation"), message, jFrame))
                  {
                  expressionFile = null;
                  }
               }
            }
         }

      protected Object executeTimeConsumingAction()
         {
         if (expressionFile != null)
            {
            expressionFileManagerControlsController.openExpression(expressionFile.getExpression());
            builderApp.setStageTitle(expressionFile.getPrettyName());
            }
         return null;
         }
      }

   private final class DeleteExpressionAction extends AbstractTimeConsumingAction
      {
      private ExpressionFile expressionFile = null;

      protected void executeGUIActionBefore()
         {
         final int selectedIndex = fileManagerView.getSelectedIndex();
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
            fileManagerView.getComponent().repaint();
            }
         return null;
         }
      }
   }
