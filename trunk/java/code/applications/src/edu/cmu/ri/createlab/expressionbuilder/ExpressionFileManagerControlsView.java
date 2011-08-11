package edu.cmu.ri.createlab.expressionbuilder;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.nio.channels.NonReadableChannelException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.PropertyResourceBundle;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import edu.cmu.ri.createlab.expressionbuilder.controlpanel.ControlPanelManager;
import edu.cmu.ri.createlab.terk.expression.XmlExpression;
import edu.cmu.ri.createlab.terk.expression.manager.ExpressionFile;
import edu.cmu.ri.createlab.terk.expression.manager.ExpressionFileManagerModel;
import edu.cmu.ri.createlab.terk.expression.manager.ExpressionFileManagerView;
import edu.cmu.ri.createlab.userinterface.GUIConstants;
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
   private final ExpressionFileManagerModel fileManagerModel;
   private final ExpressionFileManagerControlsController expressionFileManagerControlsController;

   private final ControlPanelManager controlPanelManager;

   private final ExpressionBuilder builderApp;

   ExpressionFileManagerControlsView(final ExpressionBuilder build,
                                     final JFrame jFrame,
                                     final ControlPanelManager controlPanelManager,
                                     final ExpressionFileManagerView fileManagerView,
                                     final ExpressionFileManagerModel fileManagerModel,
                                     final ExpressionFileManagerControlsController expressionFileManagerControlsController,
                                     final JButton open)
      {
      this.jFrame = jFrame;
      this.fileManagerView = fileManagerView;
      this.fileManagerModel = fileManagerModel;
      this.expressionFileManagerControlsController = expressionFileManagerControlsController;
      this.builderApp = build;
      this.openButton = open;
      this.controlPanelManager = controlPanelManager;

      deleteButton.setIcon(ImageUtils.createImageIcon("/edu/cmu/ri/createlab/expressionbuilder/images/deleteMark.png"));

      panel.setLayout(new GridBagLayout());
      panel.setBackground(Color.WHITE);

      GridBagConstraints gbc = new GridBagConstraints();

      gbc.fill = GridBagConstraints.HORIZONTAL;
      gbc.gridx = 0;
      gbc.gridy = 0;
      gbc.weighty = 0.0;
      gbc.weightx = 1.0;
      gbc.anchor = GridBagConstraints.PAGE_END;

      panel.add(deleteButton, gbc);
      openButton.setEnabled(true);
      final OpenExpressionAction openExpressionAction = new OpenExpressionAction();
      final OpenExpressionButtonAction openExpressionButtonAction = new OpenExpressionButtonAction();

      // change enabled state of button depending on whether an item in the list is selected
      fileManagerView.addListSelectionListener(
            new ListSelectionListener()
            {
            public void valueChanged(final ListSelectionEvent e)
               {
                deleteButton.setEnabled(!fileManagerView.isSelectionEmpty());
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
      openButton.addActionListener(openExpressionButtonAction);

      // clicking the Delete button should delete the selected expression
      deleteButton.addActionListener(new DeleteExpressionAction());
      }

   Component getComponent()
      {
      return panel;
      }

   public void setEnabled(final boolean isEnabled)
      {
      final Runnable runnable = isEnabled ? setEnabledRunnable : setDisabledRunnable;
      if (SwingUtilities.isEventDispatchThread())
         {
         runnable.run();
         }
      else
         {
         SwingUtilities.invokeLater(runnable);
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
         openButton.setEnabled(isEnabled);
         deleteButton.setEnabled(isEnabled && !fileManagerView.isSelectionEmpty());
         }
      }

   private final class OpenExpressionAction extends AbstractTimeConsumingAction
      {
      //When an expression is double-clicked in the list
      private XmlExpression expression = null;
      private ExpressionFile file = null;

      protected void executeGUIActionBefore()
         {
             final int selectedIndex = fileManagerView.getSelectedIndex();
             final XmlExpression xmlExpression =  controlPanelManager.buildExpression();
             final String xmlDocumentString = xmlExpression == null ? null : xmlExpression.toXmlDocumentStringFormatted();


             if (selectedIndex >= 0)
                {
                    file = fileManagerModel.getExpressionFileAt(selectedIndex);
                    final String message = MessageFormat.format(RESOURCES.getString("dialog.message.open-expression-confirmation"), file.getPrettyName());

                    if (xmlDocumentString==null || DialogHelper.showYesNoDialog(RESOURCES.getString("dialog.title.open-expression-confirmation"), message, jFrame))
                    {
                        expression = fileManagerModel.getExpressionAt(selectedIndex);
                    }
                    else
                    {
                        file = null;
                    }
                }
         }

          protected Object executeTimeConsumingAction()
         {
         if (expression != null)
            {
            expressionFileManagerControlsController.openExpression(expression);
            builderApp.setStageTitle(file.getPrettyName());
            }
         return null;
         }
      }

     private final class OpenExpressionButtonAction extends AbstractTimeConsumingAction
      {
      //When the open button is pressed
      private XmlExpression expression = null;
      private ExpressionFile file = null;

      protected void executeGUIActionBefore()
         {
         final FileListDialogPanel listPanel = new FileListDialogPanel();
         fileManagerView.clearSelection();
         final int selection = JOptionPane.showConfirmDialog(jFrame,
                                                       listPanel,
                                                       "Open",
                                                       JOptionPane.OK_CANCEL_OPTION,
                                                       JOptionPane.PLAIN_MESSAGE);
         if (selection==JOptionPane.OK_OPTION){
             final int selectedIndex = listPanel.getResults();
             if (selectedIndex >= 0)
             {

                final XmlExpression xmlExpression =  controlPanelManager.buildExpression();
                final String xmlDocumentString = xmlExpression == null ? null : xmlExpression.toXmlDocumentStringFormatted();

                file = fileManagerModel.getExpressionFileAt(selectedIndex);

                final String message = MessageFormat.format(RESOURCES.getString("dialog.message.open-expression-confirmation"), file.getPrettyName());

                if (xmlDocumentString==null || DialogHelper.showYesNoDialog(RESOURCES.getString("dialog.title.open-expression-confirmation"), message, jFrame))
                {
                    expression = fileManagerModel.getExpressionAt(selectedIndex);
                }
                else
                {
                 file = null;
                }
             }
             else
             {
                 JOptionPane.showMessageDialog(jFrame,
                                               "Please select an expression to open from the provided list.",
                                               "Open Error",
                                               JOptionPane.WARNING_MESSAGE);
                 executeGUIActionBefore();
             }
         }

         }

      protected Object executeTimeConsumingAction()
         {
         if (expression != null)
            {
            expressionFileManagerControlsController.openExpression(expression);
            builderApp.setStageTitle(file.getPrettyName());
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
            expressionFile = fileManagerModel.getExpressionFileAt(selectedIndex);

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

    private class FileListDialogPanel extends JPanel{

     private final GridBagConstraints gbc = new GridBagConstraints();
     private final GridBagLayout gbl = new GridBagLayout();
     private final ArrayList options = new ArrayList();
     private final ExpressionFileManagerModel dialogFileManagerModel = new ExpressionFileManagerModel();
     private final ExpressionFileManagerView dialogFileManagerView = new ExpressionFileManagerView(dialogFileManagerModel, GUIConstants.FONT_NORMAL);

     FileListDialogPanel(){
         super();
         this.setLayout(gbl);
         gbc.fill = GridBagConstraints.HORIZONTAL;
         gbc.gridx = 0;
         gbc.gridy = 0;
         gbc.weighty = 0.0;
         gbc.weightx = 1.0;
         gbc.insets = new Insets(2,2,2,2);
         gbc.anchor = GridBagConstraints.LINE_START;

         this.add(SwingUtils.createLabel("Select an expression to open:"));

         gbc.gridy = 1;
         gbc.weighty = 1.0;
         gbc.fill = GridBagConstraints.BOTH;

         this.add(dialogFileManagerView.getComponent(),gbc);



         this.setMinimumSize(new Dimension(300, 400));
         this.setPreferredSize(new Dimension(300, 400));
     }

     public int getResults(){
       if  (dialogFileManagerView.isSelectionEmpty()){
       return -1;
       }
       else {
       return dialogFileManagerView.getSelectedIndex();
       }
     }

     public void addComponent(Component component,int xpos, int ypos){
       gbc.gridx = xpos;
       gbc.gridy = ypos;
       gbc.insets = new Insets(2,2,2,2);
       gbl.setConstraints(component,gbc);
       this.add(component);
     }

     public void addComponent (Component component,int xpos,int ypos,int anchor){
       gbc.anchor = anchor;
       addComponent(component,xpos,ypos);
     }

    }


}
