package edu.cmu.ri.createlab.sequencebuilder;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.PropertyResourceBundle;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingWorker;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import edu.cmu.ri.createlab.sequencebuilder.programelement.model.ExpressionModel;
import edu.cmu.ri.createlab.sequencebuilder.programelement.model.ProgramElementModel;
import edu.cmu.ri.createlab.sequencebuilder.programelement.model.SavedSequenceModel;
import edu.cmu.ri.createlab.sequencebuilder.programelement.view.ProgramElementView;
import edu.cmu.ri.createlab.sequencebuilder.programelement.view.listcell.ExpressionListCellView;
import edu.cmu.ri.createlab.sequencebuilder.programelement.view.listcell.ProgramElementListCellRenderer;
import edu.cmu.ri.createlab.sequencebuilder.programelement.view.listcell.SavedSequenceListCellView;
import edu.cmu.ri.createlab.terk.expression.manager.ExpressionFile;
import edu.cmu.ri.createlab.userinterface.util.DialogHelper;
import edu.cmu.ri.createlab.userinterface.util.ImageUtils;
import edu.cmu.ri.createlab.userinterface.util.SwingUtils;
import org.jetbrains.annotations.NotNull;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
final class FileManagerControlsView
   {
   private static final PropertyResourceBundle RESOURCES = (PropertyResourceBundle)PropertyResourceBundle.getBundle(FileManagerControlsView.class.getName());

   private final JPanel panel = new JPanel();

   private final JButton appendButton = SwingUtils.createButton(RESOURCES.getString("button.label.append"));
   private final JButton openButton = new  JButton(RESOURCES.getString("button.label.open_exp"));
   private final JButton deleteButton = SwingUtils.createButton(RESOURCES.getString("button.label.delete_exp"));

   private final JFrame jFrame;
   private final JList expressionSourceList;
   private final JList savedSequenceSourceList;

   private final SavedSequenceListModel savedSequenceSourceListModel;
   private final ProgramElementListCellRenderer programElementListCellRenderer;

   private final Sequence sequence;
   private final FileManagerControlsController fileManagerControlsController;

   FileManagerControlsView(final JFrame jFrame,
                           final Sequence seq,
                           final JList expressionSourceList,
                           final JList savedSequenceSourceList,
                           final SavedSequenceListModel savedSeqSourceListModel,
                           final ProgramElementListCellRenderer programElementListCellRenderer,
                           final FileManagerControlsController fileManagerCC)
      {
      this.jFrame = jFrame;
      this.expressionSourceList = expressionSourceList;
      this.savedSequenceSourceList = savedSequenceSourceList;
      this.sequence = seq;
      this.fileManagerControlsController = fileManagerCC;

      this.savedSequenceSourceListModel = savedSeqSourceListModel;
      this.programElementListCellRenderer = programElementListCellRenderer;

      deleteButton.setEnabled(false);
      openButton.setEnabled(false);

      deleteButton.setIcon(ImageUtils.createImageIcon("/edu/cmu/ri/createlab/sequencebuilder/images/deleteMark.png"));
      //deleteButton.setMnemonic(KeyEvent.VK_D);

      openButton.setIcon(ImageUtils.createImageIcon("/edu/cmu/ri/createlab/sequencebuilder/images/openIcon.png"));    
      //openButton.setMnemonic(KeyEvent.VK_O);
      appendButton.setMnemonic(KeyEvent.VK_A);

      panel.setLayout(new GridBagLayout());
      panel.setBackground(Color.WHITE);

      expressionSourceList.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
      savedSequenceSourceList.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

      final GridBagConstraints gbc = new GridBagConstraints();

      gbc.fill = GridBagConstraints.HORIZONTAL;
      gbc.gridx = 0;
      gbc.gridy = 1;
      gbc.weighty = 0.0;
      gbc.weightx = 1.0;
      gbc.insets = new Insets(0,0,0,0);
      gbc.anchor = GridBagConstraints.PAGE_END;

      panel.add(deleteButton, gbc);

      gbc.fill = GridBagConstraints.HORIZONTAL;
      gbc.gridx = 0;
      gbc.gridy = 0;
      gbc.weighty = 0.0;
      gbc.weightx = 1.0;
      gbc.insets = new Insets(0,0,5,0);
      gbc.anchor = GridBagConstraints.PAGE_END;

      panel.add(openButton, gbc);


      // add selection listeners which allow us to toggle whether the buttons are enabled
      expressionSourceList.addListSelectionListener(
            new ListSelectionListener()
            {
            @Override
            public void valueChanged(final ListSelectionEvent listSelectionEvent)
               {
               toggleButtons();
               }
            }
      );
      savedSequenceSourceList.addListSelectionListener(
            new ListSelectionListener()
            {
            @Override
            public void valueChanged(final ListSelectionEvent listSelectionEvent)
               {
               toggleButtons();
               }
            }
      );

      // handle double-clicks in the expression and sequence lists
      final MouseListener fileManagerControlsButtonMouseListener =
            new MouseAdapter()
            {
            public void mouseClicked(final MouseEvent e)
               {
               if (e.getClickCount() == 2)
                  {
                  openExpressionOrSequence();
                  }
               }
            };
      expressionSourceList.addMouseListener(fileManagerControlsButtonMouseListener);
      savedSequenceSourceList.addMouseListener(fileManagerControlsButtonMouseListener);

      appendButton.addActionListener(
            new ActionListener()
            {
            @Override
            public void actionPerformed(final ActionEvent actionEvent)
               {
               ProgramElementView view = null;
               if (!expressionSourceList.isSelectionEmpty())
                  {
                  view = (ExpressionListCellView)expressionSourceList.getSelectedValue();
                  }
               else if (!savedSequenceSourceList.isSelectionEmpty())
                  {
                  view = (SavedSequenceListCellView)savedSequenceSourceList.getSelectedValue();
                  }

               if (view != null)
                  {
                  final ProgramElementModel model = view.getProgramElementModel().createCopy();
                  final SwingWorker sw =
                        new SwingWorker<Object, Object>()
                        {
                        @Override
                        protected Object doInBackground() throws Exception
                           {
                           sequence.appendProgramElement(model);
                           return null;
                           }
                        };
                  sw.execute();
                  }
               }
            }
      );

      openButton.addActionListener(
            new ActionListener()
            {
            @Override
            public void actionPerformed(final ActionEvent actionEvent)
               {
               openExpressionOrSequence();
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

   public JButton getOpenButton()
      {
      return openButton;
      }

   private void toggleButtons()
      {
      final boolean isSomethingSelected = !savedSequenceSourceList.isSelectionEmpty() || !expressionSourceList.isSelectionEmpty();

      if (!savedSequenceSourceList.isSelectionEmpty())
         {
         deleteButton.setText(RESOURCES.getString("button.label.delete_seq"));
         openButton.setText(RESOURCES.getString("button.label.open_seq"));
         }
      else if (!expressionSourceList.isSelectionEmpty())
         {
         deleteButton.setText(RESOURCES.getString("button.label.delete_exp"));
         openButton.setText(RESOURCES.getString("button.label.open_exp"));
         }

      deleteButton.setEnabled(isSomethingSelected);
      openButton.setEnabled(isSomethingSelected);
      appendButton.setEnabled(isSomethingSelected);
      }

   JComponent getComponent()
      {
      return panel;
      }

   private void openExpressionOrSequence()
      {
      if (!expressionSourceList.isSelectionEmpty())
         {
         final ProgramElementView view = (ExpressionListCellView)expressionSourceList.getSelectedValue();
         if (view != null)
            {
            final ExpressionModel expressionModel = (ExpressionModel)view.getProgramElementModel().createCopy();

            jFrame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            final SwingWorker sw =
                  new SwingWorker<Object, Object>()
                  {
                  @Override
                  protected Object doInBackground() throws Exception
                     {
                     fileManagerControlsController.openExpression(new ExpressionFile(expressionModel.getExpressionFile()));
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
      else if (!savedSequenceSourceList.isSelectionEmpty())
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

   private class FileListDialogPanel extends JPanel
      {

      private final GridBagConstraints gbc = new GridBagConstraints();
      private final GridBagLayout gbl = new GridBagLayout();
      private final ArrayList options = new ArrayList();

      private final JList savedSequenceList = new JList(savedSequenceSourceListModel);
      private final JScrollPane savedSequenceSourceListScrollPane = new JScrollPane(savedSequenceList);

      FileListDialogPanel()
         {
         super();
         this.setLayout(gbl);

         savedSequenceList.setCellRenderer(programElementListCellRenderer);
         savedSequenceList.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
         savedSequenceList.setBorder(BorderFactory.createLineBorder(Color.gray));
         savedSequenceList.setDragEnabled(false);
         savedSequenceList.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

         gbc.fill = GridBagConstraints.HORIZONTAL;
         gbc.gridx = 0;
         gbc.gridy = 0;
         gbc.weighty = 0.0;
         gbc.weightx = 1.0;
         gbc.insets = new Insets(2, 2, 2, 2);
         gbc.anchor = GridBagConstraints.LINE_START;

         this.add(SwingUtils.createLabel("Select a sequence to open:"));

         gbc.gridy = 1;
         gbc.weighty = 1.0;
         gbc.fill = GridBagConstraints.BOTH;

         this.add(savedSequenceSourceListScrollPane, gbc);

         this.setMinimumSize(new Dimension(300, 400));
         this.setPreferredSize(new Dimension(300, 400));
         }

      public int getResults()
         {
         if (savedSequenceList.isSelectionEmpty())
            {
            return -1;
            }
         else
            {
            return savedSequenceList.getSelectedIndex();
            }
         }

      public SavedSequenceListCellView getValue()
         {
         return (SavedSequenceListCellView)savedSequenceList.getSelectedValue();
         }

      public void addComponent(Component component, int xpos, int ypos)
         {
         gbc.gridx = xpos;
         gbc.gridy = ypos;
         gbc.insets = new Insets(2, 2, 2, 2);
         gbl.setConstraints(component, gbc);
         this.add(component);
         }

      public void addComponent(Component component, int xpos, int ypos, int anchor)
         {
         gbc.anchor = anchor;
         addComponent(component, xpos, ypos);
         }
      }
   }
