package edu.cmu.ri.createlab.expressionbuilder;

import java.awt.Color;
import java.awt.Component;
import java.util.PropertyResourceBundle;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.LayoutStyle;
import javax.swing.SwingUtilities;
import edu.cmu.ri.createlab.userinterface.util.AbstractTimeConsumingAction;
import edu.cmu.ri.createlab.userinterface.util.SwingUtils;
import org.apache.log4j.Logger;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
@SuppressWarnings({"CloneableClassWithoutClone"})
final class StageControlsView
   {
   private static final Logger LOG = Logger.getLogger(StageControlsView.class);

   private static final PropertyResourceBundle RESOURCES = (PropertyResourceBundle)PropertyResourceBundle.getBundle(StageControlsView.class.getName());

   private final JPanel panel = new JPanel();

   private final JTextField stageControlsTitle = new JTextField(30);
   private final JButton clearButton = SwingUtils.createButton(RESOURCES.getString("button.label.clear"));
   private final JButton refresh = SwingUtils.createButton(RESOURCES.getString("button.label.refresh"));
   private final JButton saveButton = SwingUtils.createButton(RESOURCES.getString("button.label.save"));
   private final Runnable setEnabledRunnable = new SetEnabledRunnable(true);
   private final Runnable setDisabledRunnable = new SetEnabledRunnable(false);

   StageControlsView(final StageControlsController stageControlsController)
      {
      final GroupLayout layout = new GroupLayout(panel);
      panel.setLayout(layout);
      panel.setName("stageControls");
      //panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
      //panel.setBackground(Color.WHITE);

      stageControlsTitle.setName("stageTitleTextField");
      stageControlsTitle.setText("Untitled");
      stageControlsTitle.setEditable(false);
      stageControlsTitle.setSelectionColor(null);
      stageControlsTitle.setSelectedTextColor(Color.BLACK);
      stageControlsTitle.setMaximumSize(stageControlsTitle.getPreferredSize());
      stageControlsTitle.setMinimumSize(stageControlsTitle.getPreferredSize());

      final Component spacerFirst = SwingUtils.createRigidSpacer();
      final Component spacerLeft = SwingUtils.createRigidSpacer();
      final Component spacerRight = SwingUtils.createRigidSpacer();
      final Component spacerEnd = SwingUtils.createRigidSpacer();
      final Component spacerTop = SwingUtils.createRigidSpacer();
      final Component spacerBottom = SwingUtils.createRigidSpacer();

      layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                  .addGroup(layout.createSequentialGroup()
                                  .addComponent(spacerFirst)
                                  .addComponent(stageControlsTitle)
                                  .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                  .addComponent(clearButton)
                                  .addComponent(spacerLeft)
                                  .addComponent(refresh)
                                  .addComponent(spacerRight)
                                  .addComponent(saveButton)
                                  .addComponent(spacerEnd))
                  .addComponent(spacerTop)
                  .addComponent(spacerBottom)
      );
      layout.setVerticalGroup(
            layout.createSequentialGroup()
                  .addComponent(spacerTop)
                  .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                  .addComponent(spacerFirst)
                                  .addComponent(stageControlsTitle)
                                  .addComponent(clearButton)
                                  .addComponent(spacerLeft)
                                  .addComponent(refresh)
                                  .addComponent(spacerRight)
                                  .addComponent(saveButton)
                                  .addComponent(spacerEnd))

                  .addComponent(spacerBottom)
      );

      // clicking the Clear button should clear all existing control panels
      clearButton.addActionListener(
            new AbstractTimeConsumingAction()
            {
            protected Object executeTimeConsumingAction()
               {
               stageControlsController.clearControlPanels();
               return null;
               }
            });

      // clicking the Refresh button should refresh the open control panels on the stage
      refresh.addActionListener(
            new AbstractTimeConsumingAction()
            {
            protected Object executeTimeConsumingAction()
               {
               stageControlsController.refreshControlPanels();
               return null;
               }
            });

      // clicking the Save button should save the current control panel config into a new expression
      saveButton.addActionListener(
            new AbstractTimeConsumingAction()
            {
            protected Object executeTimeConsumingAction()
               {
               stageControlsController.saveExpression();
               return null;
               }
            });
      }

   Component getComponent()
      {
      return panel;
      }

   public void setStageTitle(String str)
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
      LOG.debug("StageControlsView.setEnabled(" + isEnabled + ")");
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
         refresh.setEnabled(isEnabled);
         saveButton.setEnabled(isEnabled);
         clearButton.setEnabled(isEnabled);

         for (int i = 0; i < panel.getComponentCount(); i++)
            {
            panel.getComponent(i).setVisible(isEnabled);
            }
         }
      }
   }