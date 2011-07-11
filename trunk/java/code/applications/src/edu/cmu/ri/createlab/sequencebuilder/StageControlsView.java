package edu.cmu.ri.createlab.sequencebuilder;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.PropertyResourceBundle;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.LayoutStyle;
import javax.swing.SwingWorker;
import edu.cmu.ri.createlab.userinterface.util.AbstractTimeConsumingAction;
import edu.cmu.ri.createlab.userinterface.util.SwingUtils;
import edu.cmu.ri.createlab.xml.SaveXmlDocumentDialogRunnable;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

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
   private final JButton clearButton = SwingUtils.createButton(RESOURCES.getString("button.label.clear"), true);
   private final JButton saveButton = SwingUtils.createButton(RESOURCES.getString("button.label.save"));
   private final JButton playButton = SwingUtils.createButton(RESOURCES.getString("button.label.play"));
   private final Runnable setEnabledRunnable = new SetEnabledRunnable(true);
   private final Runnable setDisabledRunnable = new SetEnabledRunnable(false);

   StageControlsView(final StageControlsController stageControlsController)
      {
      final GroupLayout layout = new GroupLayout(panel);
      panel.setLayout(layout);
      panel.setName("stageControls");

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
                  .addComponent(spacerTop)
                  .addGroup(layout.createSequentialGroup()
                                  .addComponent(spacerFirst)
                                  .addComponent(stageControlsTitle)
                                  .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                  .addComponent(clearButton)
                                  .addComponent(spacerLeft)
                                  .addComponent(playButton)
                                  .addComponent(spacerRight)
                                  .addComponent(saveButton)
                                  .addComponent(spacerEnd))
                  .addComponent(spacerBottom)
      );
      layout.setVerticalGroup(
            layout.createSequentialGroup()
                  .addComponent(spacerTop)
                  .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                                  .addComponent(spacerFirst)
                                  .addComponent(stageControlsTitle)
                                  .addComponent(clearButton)
                                  .addComponent(spacerLeft)
                                  .addComponent(playButton)
                                  .addComponent(spacerRight)
                                  .addComponent(saveButton)
                                  .addComponent(spacerEnd))
                  .addComponent(spacerBottom)
      );

      clearButton.addActionListener(
            new AbstractTimeConsumingAction()
            {
            protected Object executeTimeConsumingAction()
               {
               stageControlsController.clearStage();
               return null;
               }
            });

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
                        stageControlsController.saveSequence(filename,
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

      playButton.addActionListener(
            new AbstractTimeConsumingAction()
            {
            protected Object executeTimeConsumingAction()
               {
               stageControlsController.playSequence();
               return null;
               }
            });
      }

   Component getComponent()
      {
      return panel;
      }

   public void setEnabled(final boolean isEnabled)
      {
      LOG.debug("StageControlsView.setEnabled(" + isEnabled + ")");
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
         playButton.setEnabled(isEnabled);
         saveButton.setEnabled(isEnabled);
         }
      }
   }