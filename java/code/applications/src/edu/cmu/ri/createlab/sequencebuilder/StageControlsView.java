package edu.cmu.ri.createlab.sequencebuilder;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.PropertyResourceBundle;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.LayoutStyle;
import javax.swing.SwingWorker;
import edu.cmu.ri.createlab.sequencebuilder.programelement.view.ViewEventPublisher;
import edu.cmu.ri.createlab.userinterface.util.AbstractTimeConsumingAction;
import edu.cmu.ri.createlab.userinterface.util.DialogHelper;
import edu.cmu.ri.createlab.userinterface.util.ImageUtils;
import edu.cmu.ri.createlab.userinterface.util.SwingUtils;
import edu.cmu.ri.createlab.xml.SaveXmlDocumentDialogRunnable;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
@SuppressWarnings({"CloneableClassWithoutClone"})
final class StageControlsView implements SequenceExecutor.EventListener
   {
   private static final Logger LOG = Logger.getLogger(StageControlsView.class);

   private static final PropertyResourceBundle RESOURCES = (PropertyResourceBundle)PropertyResourceBundle.getBundle(StageControlsView.class.getName());
   private static final String DEFAULT_SEQUENCE_TITLE = RESOURCES.getString("textfield.text.default-sequence-filename");

   private final JPanel panel = new JPanel();

   private final JTextField stageControlsTitle = new JTextField(30);
   private final JButton saveButton = SwingUtils.createButton(RESOURCES.getString("button.label.save"));

   private final JButton playOrStopButton = new JButton(RESOURCES.getString("button.label.play"), ImageUtils.createImageIcon("/edu/cmu/ri/createlab/sequencebuilder/images/playIcon.png"));

   private final Runnable setEnabledRunnable = new SetEnabledRunnable(true);
   private final Runnable setDisabledRunnable = new SetEnabledRunnable(false);
   private final Runnable executionStartRunnable =
         new Runnable()
         {
         @Override
         public void run()
            {
            playOrStopButton.setText(RESOURCES.getString("button.label.stop"));
            playOrStopButton.setMnemonic(KeyEvent.VK_T);
            playOrStopButton.setIcon(ImageUtils.createImageIcon("/edu/cmu/ri/createlab/sequencebuilder/images/smallStop.png"));
            }
         };
   private final Runnable executionStopRunnable =
         new Runnable()
         {
         @Override
         public void run()
            {
            playOrStopButton.setText(RESOURCES.getString("button.label.play"));
            playOrStopButton.setMnemonic(KeyEvent.VK_P);
            playOrStopButton.setIcon(ImageUtils.createImageIcon("/edu/cmu/ri/createlab/sequencebuilder/images/playIcon.png"));
            ViewEventPublisher.getInstance().publishResetViewsForSequenceExecutionEvent();
            }
         };

   StageControlsView(final JFrame jFrame, final Sequence sequence, final StageControlsController stageControlsController, final FileManagerControlsView fileManagerControlsView)
      {
      final JButton clearButton = SwingUtils.createButton(RESOURCES.getString("button.label.clear"), true);
      final RepeatButton repeatAllButton = new RepeatButton();
      final JButton openButton = fileManagerControlsView.getOpenButton();

      final GroupLayout layout = new GroupLayout(panel);
      panel.setLayout(layout);
      panel.setName("stageControls");

      stageControlsTitle.setName("stageTitleTextField");
      stageControlsTitle.setText(DEFAULT_SEQUENCE_TITLE);
      stageControlsTitle.setEditable(false);
      stageControlsTitle.setSelectionColor(null);
      stageControlsTitle.setSelectedTextColor(Color.BLACK);
      stageControlsTitle.setMaximumSize(stageControlsTitle.getPreferredSize());
      stageControlsTitle.setMinimumSize(stageControlsTitle.getPreferredSize());
      stageControlsTitle.setFocusable(false);

      clearButton.setMnemonic(KeyEvent.VK_N);
      saveButton.setMnemonic(KeyEvent.VK_S);

      playOrStopButton.setMnemonic(KeyEvent.VK_P);

      layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.TRAILING)

                  .addGroup(layout.createSequentialGroup()
                                  .addGap(5, 5, 5)
                                  .addComponent(stageControlsTitle)
                                  .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                  .addComponent(playOrStopButton)
                                  .addGap(5, 5, 5)
                                  .addComponent(repeatAllButton)
                                  .addGap(20, 20, 20)
                                  .addComponent(clearButton)
                                  .addGap(5, 5, 5)
                                  .addComponent(openButton)
                                  .addGap(5, 5, 5)
                                  .addComponent(saveButton)
                                  .addGap(5, 5, 5))

      );
      layout.setVerticalGroup(
            layout.createSequentialGroup()

                  .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)

                                  .addComponent(stageControlsTitle)
                                  .addComponent(clearButton)
                                  .addComponent(openButton)
                                  .addComponent(playOrStopButton)
                                  .addComponent(repeatAllButton)
                                  .addComponent(saveButton)
                  )

      );

      repeatAllButton.addActionListener(
            new ActionListener()
            {
            @Override
            public void actionPerformed(final ActionEvent e)
               {
               final boolean isSelected = ((RepeatButton)e.getSource()).isSelected();
               final SwingWorker sw =
                     new SwingWorker<Object, Object>()
                     {
                     @Override
                     protected Object doInBackground() throws Exception
                        {
                        stageControlsController.setWillLoopPlayback(isSelected);
                        return null;
                        }
                     };
               sw.execute();
               }
            });

      clearButton.addActionListener(
            new ActionListener()
            {
            @Override
            public void actionPerformed(final ActionEvent actionEvent)
               {
               // if the sequence is empty, just make sure the title is reset to the default (the user may have
               // opened/saved a sequence and then manually removed all program elements, which would leave an empty
               // stage, but with a title field that isn't the default)
               if (sequence.isEmpty())
                  {
                  stageControlsTitle.setText(DEFAULT_SEQUENCE_TITLE);
                  }
               else
                  {
                  // otherwise, ask the user if she's sure she wants to clear the stage (if non-empty)
                  if (DialogHelper.showYesNoDialog(RESOURCES.getString("dialog.title.clear-sequence-confirmation"),
                                                   RESOURCES.getString("dialog.message.clear-sequence-confirmation"),
                                                   jFrame))
                     {
                     final SwingWorker sw =
                           new SwingWorker<Object, Object>()
                           {
                           @Override
                           protected Object doInBackground() throws Exception
                              {
                              stageControlsController.clearStage();
                              return null;
                              }

                           @Override
                           protected void done()
                              {
                              stageControlsTitle.setText(DEFAULT_SEQUENCE_TITLE);
                              }
                           };
                     sw.execute();
                     }
                  }
               }
            }
      );

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

      playOrStopButton.addActionListener(
            new AbstractTimeConsumingAction()
            {
            protected Object executeTimeConsumingAction()
               {
               stageControlsController.startOrStopSequenceExecution();
               return null;
               }
            });

      // should initially be disabled since nothing is on the stage
      setEnabled(false);
      }

   Component getComponent()
      {
      return panel;
      }

   public void setEnabled(final boolean isEnabled)
      {
      SwingUtils.runInGUIThread(isEnabled ? setEnabledRunnable : setDisabledRunnable);
      }

   public void setTitle(@NotNull final String name)
      {
      SwingUtils.runInGUIThread(
            new Runnable()
            {
            @Override
            public void run()
               {
               stageControlsTitle.setText(name);
               }
            }
      );
      }

   @Override
   public void handleExecutionStart()
      {
      LOG.debug("StageControlsView.handleExecutionStart()");
      SwingUtils.runInGUIThread(executionStartRunnable);
      }

   @Override
   public void handleExecutionEnd()
      {
      LOG.debug("StageControlsView.handleExecutionEnd()");
      SwingUtils.runInGUIThread(executionStopRunnable);
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
         playOrStopButton.setEnabled(isEnabled);
         saveButton.setEnabled(isEnabled);
         }
      }

   private class RepeatButton extends JToggleButton
      {
      private RepeatButton()
         {
         super();
         this.setName("repeatToggleButton");
         this.setFocusable(false);
         this.setSelected(false);
         this.setToolTipText(RESOURCES.getString("button.tooltop.play-loop"));
         }
      }
   }