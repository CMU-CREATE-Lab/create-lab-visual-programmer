package edu.cmu.ri.createlab.expressionbuilder;

import java.awt.*;
import java.awt.event.*;
import java.util.PropertyResourceBundle;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import edu.cmu.ri.createlab.expressionbuilder.controlpanel.ControlPanelManager;
import edu.cmu.ri.createlab.terk.expression.XmlExpression;
import edu.cmu.ri.createlab.userinterface.util.AbstractTimeConsumingAction;
import edu.cmu.ri.createlab.userinterface.util.DialogHelper;
import edu.cmu.ri.createlab.userinterface.util.SwingUtils;
import edu.cmu.ri.createlab.xml.SaveXmlDocumentDialogRunnable;
import org.jetbrains.annotations.NotNull;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
@SuppressWarnings({"CloneableClassWithoutClone"})
final class StageControlsView
   {
   private static final PropertyResourceBundle RESOURCES = (PropertyResourceBundle)PropertyResourceBundle.getBundle(StageControlsView.class.getName());

   private final JPanel panel = new JPanel();

   private final JTextField stageControlsTitle = new JTextField(30);
   private final JButton clearButton = SwingUtils.createButton(RESOURCES.getString("button.label.clear"));
   private final JButton openButton = SwingUtils.createButton(RESOURCES.getString("button.label.open"));
   private final JButton saveButton = SwingUtils.createButton(RESOURCES.getString("button.label.save"));
   private final JButton settingsButton = SwingUtils.createButton(RESOURCES.getString("button.label.settings"));

   private final Runnable setEnabledRunnable = new SetEnabledRunnable(true);
   private final Runnable setDisabledRunnable = new SetEnabledRunnable(false);

   StageControlsView(final ControlPanelManager controlPanelManager, final StageControlsController stageControlsController, final JFrame parentComponent)
      {
      panel.setLayout(new GridBagLayout());
      panel.setName("stageControls");

      clearButton.setFocusable(false);
      openButton.setFocusable(false);
      saveButton.setFocusable(false);
      settingsButton.setFocusable(false);

      clearButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
      openButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
      saveButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
      settingsButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
      stageControlsTitle.setCursor(new Cursor(Cursor.HAND_CURSOR));

      saveButton.setMnemonic(KeyEvent.VK_S);
      //openButton.setMnemonic(KeyEvent.VK_O);
      clearButton.setMnemonic(KeyEvent.VK_N);

      stageControlsTitle.setName("stageTitleTextField");
      stageControlsTitle.setText("Untitled");
      stageControlsTitle.setEditable(false);
      stageControlsTitle.setSelectionColor(null);
      stageControlsTitle.setSelectedTextColor(Color.BLACK);
      stageControlsTitle.setMaximumSize(stageControlsTitle.getPreferredSize());
      stageControlsTitle.setMinimumSize(new Dimension(50, stageControlsTitle.getPreferredSize().height));
      stageControlsTitle.setFocusable(false);

      GridBagConstraints c = new GridBagConstraints();

      c.fill = GridBagConstraints.HORIZONTAL;
      c.gridwidth = 1;
      c.gridheight = 1;
      c.gridx = 0;
      c.gridy = 0;
      c.weighty = 1.0;
      c.weightx = 1.0;
      c.anchor = GridBagConstraints.LINE_START;
      c.insets = new Insets(5, 5, 5, 0);
      panel.add(stageControlsTitle, c);

      c.fill = GridBagConstraints.NONE;
      c.gridwidth = 1;
      c.gridheight = 1;
      c.gridx = 1;
      c.gridy = 0;
      c.weighty = 1.0;
      c.weightx = 0.0;
      c.anchor = GridBagConstraints.LINE_END;
      c.insets = new Insets(5, 50, 5, 0);
      panel.add(clearButton, c);

/*
      c.fill = GridBagConstraints.NONE;
      c.gridwidth = 1;
      c.gridheight = 1;
      c.gridx = 2;
      c.gridy = 0;
      c.weighty = 1.0;
      c.weightx = 0.0;
      c.anchor = GridBagConstraints.CENTER;
      c.insets = new Insets(5, 5, 5, 0);
      panel.add(openButton, c);
*/

      c.fill = GridBagConstraints.NONE;
      c.gridwidth = 1;
      c.gridheight = 1;
      c.gridx = 3;
      c.gridy = 0;
      c.weighty = 1.0;
      c.weightx = 0.0;
      c.anchor = GridBagConstraints.CENTER;
      c.insets = new Insets(5, 5, 5, 0);
      panel.add(saveButton, c);

      // clicking the Clear button should clear all existing control panels
      clearButton.addActionListener(
            new AbstractTimeConsumingAction()
            {

            protected Object executeTimeConsumingAction()
               {
               final XmlExpression xmlExpression = controlPanelManager.buildExpression();
               final String xmlDocumentString = xmlExpression == null ? null : xmlExpression.toXmlDocumentStringFormatted();

               if (xmlDocumentString == null || DialogHelper.showYesNoDialog(RESOURCES.getString("dialog.title.warning"),
                                                                             RESOURCES.getString("dialog.message.clear-expression"),
                                                                             parentComponent))
                  {
                  stageControlsController.clearControlPanels();
                  return null;
                  }
               return null;
               }
            }
      );

      // clicking the Save button should save the current control panel config into a new expression
      saveButton.addActionListener(
            new ActionListener()
            {
            @Override
            public void actionPerformed(final ActionEvent actionEvent)
               {
               final String filename = stageControlsTitle.getText();
               final SwingWorker sw =
                     new SwingWorker<Object, Object>()
                     {
                     @Override
                     protected Object doInBackground() throws Exception
                        {
                        stageControlsController.saveExpression(filename,
                                                               new SaveXmlDocumentDialogRunnable.EventHandler()
                                                               {
                                                               @Override
                                                               public void handleSuccessfulSave(@NotNull final String savedFilenameWithoutExtension)
                                                                  {
                                                                  stageControlsTitle.setText(savedFilenameWithoutExtension);
                                                                  }
                                                               });
                        return null;
                        }
                     };
               sw.execute();
               }
            });

      stageControlsTitle.addMouseListener(new MouseAdapter() {
          public void mouseClicked(MouseEvent e) {
              final String filename = "Untitled";
              final SwingWorker sw =
                      new SwingWorker<Object, Object>()
                      {
                          @Override
                          protected Object doInBackground() throws Exception
                          {
                              stageControlsController.saveExpression(filename,
                                      new SaveXmlDocumentDialogRunnable.EventHandler()
                                      {
                                          @Override
                                          public void handleSuccessfulSave(@NotNull final String savedFilenameWithoutExtension)
                                          {
                                              stageControlsTitle.setText(savedFilenameWithoutExtension);
                                          }
                                      });
                              return null;
                          }
                      };
              sw.execute();
          }
      });

      }



   Component getComponent()
      {
      return panel;
      }

   public void setStageTitle(final String str)
      {
      stageControlsTitle.setText(str);
      }

   public String getStageTitle()
      {
      return stageControlsTitle.getText();
      }

   public JTextField getStageTitleComponent()
      {
      return stageControlsTitle;
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

   public JButton getOpenButton()
      {
      return openButton;
      }
   public JButton getSettingsButton()
    {
       return settingsButton;
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
         saveButton.setEnabled(isEnabled);
         clearButton.setEnabled(isEnabled);

         for (int i = 0; i < panel.getComponentCount(); i++)
            {
            panel.getComponent(i).setVisible(isEnabled);
            }
         }
      }
   }