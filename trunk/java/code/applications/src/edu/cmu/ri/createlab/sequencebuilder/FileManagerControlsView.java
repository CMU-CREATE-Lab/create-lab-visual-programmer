package edu.cmu.ri.createlab.sequencebuilder;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.MessageFormat;
import java.util.PropertyResourceBundle;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingWorker;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import edu.cmu.ri.createlab.sequencebuilder.programelement.model.ExpressionModel;
import edu.cmu.ri.createlab.sequencebuilder.programelement.model.ProgramElementModel;
import edu.cmu.ri.createlab.sequencebuilder.programelement.model.SavedSequenceModel;
import edu.cmu.ri.createlab.sequencebuilder.programelement.view.listcell.ExpressionListCellView;
import edu.cmu.ri.createlab.sequencebuilder.programelement.view.listcell.SavedSequenceListCellView;
import edu.cmu.ri.createlab.sequencebuilder.sequence.Sequence;
import edu.cmu.ri.createlab.userinterface.util.DialogHelper;
import edu.cmu.ri.createlab.userinterface.util.SwingUtils;
import org.jetbrains.annotations.NotNull;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
final class FileManagerControlsView
   {
   private static final PropertyResourceBundle RESOURCES = (PropertyResourceBundle)PropertyResourceBundle.getBundle(FileManagerControlsView.class.getName());

   private final JPanel panel = new JPanel();

   private final JButton openSequenceButton = SwingUtils.createButton(RESOURCES.getString("button.label.open"));
   private final JButton deleteButton = SwingUtils.createButton(RESOURCES.getString("button.label.delete"));

   private final JFrame jFrame;
   private final JList expressionSourceList;
   private final JList savedSequenceSourceList;

   FileManagerControlsView(final JFrame jFrame,
                           final Sequence sequence,
                           final JList expressionSourceList,
                           final JList savedSequenceSourceList,
                           final FileManagerControlsController fileManagerControlsController)
      {
      this.jFrame = jFrame;
      this.expressionSourceList = expressionSourceList;
      this.savedSequenceSourceList = savedSequenceSourceList;

      final GroupLayout layout = new GroupLayout(panel);
      panel.setLayout(layout);
      panel.setBackground(Color.WHITE);

      final Component spacer = SwingUtils.createRigidSpacer();
      layout.setHorizontalGroup(
            layout.createSequentialGroup()
                  .addComponent(openSequenceButton)
                  .addComponent(spacer)
                  .addComponent(deleteButton)
      );
      layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                  .addComponent(openSequenceButton)
                  .addComponent(spacer)
                  .addComponent(deleteButton)
      );

      // add selection listeners which allow us to toggle whether the buttons are enabled
      expressionSourceList.addListSelectionListener(
            new ListSelectionListener()
            {
            @Override
            public void valueChanged(final ListSelectionEvent listSelectionEvent)
               {
               toggleButtonsEnabled();
               }
            }
      );
      savedSequenceSourceList.addListSelectionListener(
            new ListSelectionListener()
            {
            @Override
            public void valueChanged(final ListSelectionEvent listSelectionEvent)
               {
               toggleButtonsEnabled();
               }
            }
      );

      openSequenceButton.addActionListener(
            new ActionListener()
            {
            @Override
            public void actionPerformed(final ActionEvent actionEvent)
               {
               if (!savedSequenceSourceList.isSelectionEmpty())
                  {
                  final SavedSequenceListCellView savedSequenceListCellView = (SavedSequenceListCellView)savedSequenceSourceList.getSelectedValue();
                  final SavedSequenceModel savedSequenceModel = savedSequenceListCellView.getProgramElementModel();
                  final String message = MessageFormat.format(RESOURCES.getString("dialog.message.open-sequence-confirmation"), savedSequenceModel.getName());
                  if (sequence.isEmpty() || DialogHelper.showYesNoDialog(RESOURCES.getString("dialog.title.open-sequence-confirmation"), message, jFrame))
                     {
                     jFrame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                     final SwingWorker sw =
                           new SwingWorker<Object, Object>()
                           {
                           @Override
                           protected Object doInBackground() throws Exception
                              {
                              fileManagerControlsController.openSequence(savedSequenceModel);
                              return null;
                              }

                           @Override
                           protected void done()
                              {
                              jFrame.setCursor(Cursor.getDefaultCursor());
                              }
                           };
                     sw.execute();
                     }
                  }
               }
            }
      );

      deleteButton.addActionListener(
            new ActionListener()
            {
            @Override
            public void actionPerformed(final ActionEvent actionEvent)
               {
               FileDeleter fileDeleter = null;
               if (!expressionSourceList.isSelectionEmpty())
                  {
                  final ExpressionListCellView expressionListCellView = (ExpressionListCellView)expressionSourceList.getSelectedValue();
                  final ExpressionModel expressionModel = expressionListCellView.getProgramElementModel();
                  fileDeleter =
                        new FileDeleter<ExpressionModel>(expressionModel,
                                                         expressionSourceList,
                                                         RESOURCES.getString("dialog.message.delete-expression-confirmation"))
                        {
                        @Override
                        protected void performDelete(final ExpressionModel model)
                           {
                           fileManagerControlsController.deleteExpression(model);
                           }
                        };
                  }
               else if (!savedSequenceSourceList.isSelectionEmpty())
                  {
                  final SavedSequenceListCellView savedSequenceListCellView = (SavedSequenceListCellView)savedSequenceSourceList.getSelectedValue();
                  final SavedSequenceModel savedSequenceModel = savedSequenceListCellView.getProgramElementModel();
                  fileDeleter =
                        new FileDeleter<SavedSequenceModel>(savedSequenceModel,
                                                            savedSequenceSourceList,
                                                            RESOURCES.getString("dialog.message.delete-sequence-confirmation"))
                        {
                        @Override
                        protected void performDelete(final SavedSequenceModel model)
                           {
                           fileManagerControlsController.deleteSequence(model);
                           }
                        };
                  }

               if (fileDeleter != null)
                  {
                  fileDeleter.delete();
                  }
               }
            }
      );
      }

   private void toggleButtonsEnabled()
      {
      openSequenceButton.setEnabled(!savedSequenceSourceList.isSelectionEmpty());
      deleteButton.setEnabled(!savedSequenceSourceList.isSelectionEmpty() ||
                              !expressionSourceList.isSelectionEmpty());
      }

   JComponent getComponent()
      {
      return panel;
      }

   void doClickOnOpenSequenceButton()
      {
      openSequenceButton.doClick();
      }

   private abstract class FileDeleter<ModelClass extends ProgramElementModel>
      {
      private final ModelClass model;
      private final JList jList;
      private final String confirmationMessage;

      private FileDeleter(@NotNull final ModelClass model,
                          @NotNull final JList jList,
                          @NotNull final String confirmationMessage)
         {
         this.model = model;
         this.jList = jList;
         this.confirmationMessage = confirmationMessage;
         }

      public void delete()
         {

         final String message = MessageFormat.format(confirmationMessage, model.getName());
         final int selectedOption = JOptionPane.showConfirmDialog(jFrame,
                                                                  message,
                                                                  RESOURCES.getString("dialog.title.delete-confirmation"),
                                                                  JOptionPane.YES_NO_OPTION,
                                                                  JOptionPane.WARNING_MESSAGE);

         if (selectedOption == JOptionPane.YES_OPTION)
            {
            final SwingWorker sw =
                  new SwingWorker<Object, Object>()
                  {
                  @Override
                  protected Object doInBackground() throws Exception
                     {
                     performDelete(model);
                     return null;
                     }

                  @Override
                  protected void done()
                     {
                     jList.repaint();
                     super.done();
                     }
                  };
            sw.execute();
            }
         }

      protected abstract void performDelete(final ModelClass model);
      }
   }
