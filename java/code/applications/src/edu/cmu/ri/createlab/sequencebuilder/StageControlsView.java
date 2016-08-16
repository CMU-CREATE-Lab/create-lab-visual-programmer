package edu.cmu.ri.createlab.sequencebuilder;

import java.awt.*;
import java.awt.event.*;
import java.util.PropertyResourceBundle;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
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
   private final JButton saveButton = new JButton(RESOURCES.getString("button.label.save"));

   private final JButton undoButton = new JButton(RESOURCES.getString("button.label.undo"));

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

   StageControlsView(final JFrame jFrame,
                     final Sequence sequence,
                     final StageControlsController stageControlsController,
                     final FileManagerControlsView fileManagerControlsView)
      {
      final RepeatButton repeatAllButton = new RepeatButton();
      //final JButton openButton = fileManagerControlsView.getOpenButton();

      panel.setLayout(new GridBagLayout());
      panel.setName("stageControls");

      stageControlsTitle.setName("stageTitleTextField");
      stageControlsTitle.setText(DEFAULT_SEQUENCE_TITLE);
      stageControlsTitle.setEditable(false);
      stageControlsTitle.setSelectionColor(null);
      stageControlsTitle.setSelectedTextColor(Color.BLACK);
      stageControlsTitle.setMaximumSize(stageControlsTitle.getPreferredSize());
      stageControlsTitle.setMinimumSize(new Dimension(50, stageControlsTitle.getPreferredSize().height));
      stageControlsTitle.setFocusable(false);

      final JButton newSequenceButton = SwingUtils.createButton(RESOURCES.getString("button.label.clear"), true);
      newSequenceButton.setMnemonic(KeyEvent.VK_N);
      saveButton.setMnemonic(KeyEvent.VK_S);
      playOrStopButton.setMnemonic(KeyEvent.VK_P);

      saveButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
      undoButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
      newSequenceButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
      playOrStopButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
      repeatAllButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
      stageControlsTitle.setCursor(new Cursor(Cursor.HAND_CURSOR));

      saveButton.setToolTipText(RESOURCES.getString("button.tooltip.save"));
      undoButton.setToolTipText(RESOURCES.getString("button.tooltip.undo"));
      newSequenceButton.setToolTipText(RESOURCES.getString("button.tooltip.new"));
      playOrStopButton.setToolTipText(RESOURCES.getString("button.tooltip.play"));
      repeatAllButton.setToolTipText(RESOURCES.getString("button.tooltip.repeat"));


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
      c.anchor = GridBagConstraints.CENTER;
      c.insets = new Insets(5, 5, 5, 0);
      panel.add(undoButton, c);

      c.fill = GridBagConstraints.NONE;
      c.gridwidth = 1;
      c.gridheight = 1;
      c.gridx = 2;
      c.gridy = 0;
      c.weighty = 1.0;
      c.weightx = 0.0;
      c.anchor = GridBagConstraints.LINE_END;
      c.insets = new Insets(5, 50, 5, 0);
      panel.add(playOrStopButton, c);

      c.fill = GridBagConstraints.NONE;
      c.gridwidth = 1;
      c.gridheight = 1;
      c.gridx = 3;
      c.gridy = 0;
      c.weighty = 1.0;
      c.weightx = 0.0;
      c.anchor = GridBagConstraints.CENTER;
      c.insets = new Insets(5, 5, 5, 0);
      panel.add(repeatAllButton, c);

      c.fill = GridBagConstraints.NONE;
      c.gridwidth = 1;
      c.gridheight = 1;
      c.gridx = 4;
      c.gridy = 0;
      c.weighty = 1.0;
      c.weightx = 0.0;
      c.anchor = GridBagConstraints.CENTER;
      c.insets = new Insets(5, 20, 5, 0);
      panel.add(newSequenceButton, c);

          /*c.fill = GridBagConstraints.NONE;
          c.gridwidth = 1;
          c.gridheight = 1;
          c.gridx = 4;
          c.gridy = 0;
          c.weighty = 1.0;
          c.weightx = 0.0;
          c.anchor = GridBagConstraints.CENTER;
          c.insets = new Insets(5, 5, 5, 0);
          panel.add(openButton, c);*/

      c.fill = GridBagConstraints.NONE;
      c.gridwidth = 1;
      c.gridheight = 1;
      c.gridx = 5;
      c.gridy = 0;
      c.weighty = 1.0;
      c.weightx = 0.0;
      c.anchor = GridBagConstraints.CENTER;
      c.insets = new Insets(5, 5, 5, 0);
      panel.add(saveButton, c);

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

      undoButton.addActionListener(new ActionListener()
         {
         @Override
         public void actionPerformed(final ActionEvent e)
            {
            final SwingWorker sw =
                  new SwingWorker<Object, Object>()
                     {
                     @Override
                     protected Object doInBackground() throws Exception
                        {
                        stageControlsController.undo();
                        return null;
                        }
                     };
            sw.execute();
            }
         });
      undoButton.setEnabled(false);

      newSequenceButton.addActionListener(
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

      stageControlsTitle.addMouseListener(new MouseAdapter() {
          public void mouseClicked(MouseEvent e) {
              final String filename = "Untitled";
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

   void setUndo(final boolean isEnabled) {
      if (undoButton.isEnabled() != isEnabled)
         undoButton.setEnabled(isEnabled);
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
         this.setToolTipText(RESOURCES.getString("button.tooltip.play-loop"));
         }
      }
   }