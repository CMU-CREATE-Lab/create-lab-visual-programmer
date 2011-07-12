package edu.cmu.ri.createlab.sequencebuilder.programelement.view.standard;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JToggleButton;
import javax.swing.LayoutStyle;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.TransferHandler;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import edu.cmu.ri.createlab.sequencebuilder.ContainerView;
import edu.cmu.ri.createlab.sequencebuilder.programelement.model.ProgramElementModel;
import edu.cmu.ri.createlab.sequencebuilder.programelement.view.BaseProgramElementView;
import edu.cmu.ri.createlab.sequencebuilder.programelement.view.ProgramElementView;
import edu.cmu.ri.createlab.sequencebuilder.programelement.view.dnd.NoDropsAllowedTransferHandler;
import edu.cmu.ri.createlab.userinterface.util.ImageUtils;
import edu.cmu.ri.createlab.userinterface.util.SwingUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * <p>
 * <code>BaseProgramElementListCellView</code> provides base functionality for {@link ProgramElementView}s for display
 * in a list.
 * </p>
 *
 * @author Chris Bartley (bartley@cmu.edu)
 */
abstract class BaseStandardProgramElementView<ModelClass extends ProgramElementModel> extends BaseProgramElementView<ModelClass>
   {
   private final JPanel panel = new JPanel();
   private final InsertionHighlightArea insertBeforeHighlightArea = new InsertionHighlightArea();
   private final JPanel contentPanel = new JPanel();
   private final InsertionHighlightArea insertAfterHighlightArea = new InsertionHighlightArea();
   private final JButton deleteButton = new JButton(ImageUtils.createImageIcon("/edu/cmu/ri/createlab/sequencebuilder/programelement/view/images/deleteMark.png"));
   private final JScrollPane commentTextAreaScrollPane;

   protected BaseStandardProgramElementView(@NotNull final ContainerView containerView, @NotNull final ModelClass programElementModel)
      {
      super(containerView, programElementModel);

      deleteButton.setName("thinButton");

      final JPanel commentPanel = new JPanel();
      final CommentToggleButton commentToggleButton = new CommentToggleButton(programElementModel);
      commentToggleButton.setName("thinToggleButton");

      final JTextArea commentTextArea = new JTextArea(programElementModel.getComment());
      commentTextArea.setLineWrap(true);
      commentTextArea.setWrapStyleWord(true);
      commentTextArea.setColumns(20);
      commentTextArea.setName("commentTextArea");
      commentTextArea.getDocument().addDocumentListener(
            new DocumentListener()
            {
            @Override
            public void insertUpdate(final DocumentEvent documentEvent)
               {
               updateModel();
               }

            @Override
            public void removeUpdate(final DocumentEvent documentEvent)
               {
               updateModel();
               }

            @Override
            public void changedUpdate(final DocumentEvent documentEvent)
               {
               updateModel();
               }

            private void updateModel()
               {
               programElementModel.setComment(commentTextArea.getText());
               }
            }
      );

      commentTextAreaScrollPane = new JScrollPane(commentTextArea);
      commentTextAreaScrollPane.setVisible(false);
      commentTextAreaScrollPane.setMinimumSize(new Dimension(140, 200));
      commentTextAreaScrollPane.setMaximumSize(new Dimension(140, contentPanel.getMaximumSize().height));
      commentTextAreaScrollPane.setPreferredSize(new Dimension(140, contentPanel.getPreferredSize().height));
      commentTextAreaScrollPane.setVisible(programElementModel.isCommentVisible());
      commentTextAreaScrollPane.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(0, 2, 0, 0), BorderFactory.createMatteBorder(0, 1, 0, 0, Color.DARK_GRAY)));

      commentPanel.setName("commentPanel");

      final GroupLayout commentPanelLayout = new GroupLayout(commentPanel);
      commentPanel.setLayout(commentPanelLayout);
      commentPanelLayout.setHorizontalGroup(
            commentPanelLayout.createSequentialGroup()
                  .addComponent(commentToggleButton)
                  .addComponent(commentTextAreaScrollPane)
      );
      commentPanelLayout.setVerticalGroup(
            commentPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
                  .addGroup(commentPanelLayout.createSequentialGroup()
                                  .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED,
                                                   GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                  .addComponent(commentToggleButton)
                                  .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED,
                                                   GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                  .addComponent(commentTextAreaScrollPane)
      );

      programElementModel.addPropertyChangeEventListener(ProgramElementModel.IS_COMMENT_VISIBLE_PROPERTY,
                                                         new IsCommentVisiblePropertyChangeEventListener());

      //Prevents Comment Visibility in Loops (for sizing)
      commentPanel.setVisible(!containerView.hasParentProgramElementView());
      final JPanel commentOffsetPane = new JPanel();
      commentOffsetPane.setName("commentOffset");

      final GroupLayout layout = new GroupLayout(panel);
      panel.setLayout(layout);
      layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                  .addComponent(insertBeforeHighlightArea.getComponent())
                  .addGroup(layout.createSequentialGroup()
                                  .addComponent(commentPanel)
                                  .addComponent(contentPanel)
                                  .addComponent(commentOffsetPane))
                  .addComponent(insertAfterHighlightArea.getComponent())
      );
      layout.setVerticalGroup(
            layout.createSequentialGroup()
                  .addComponent(insertBeforeHighlightArea.getComponent())
                  .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                                  .addComponent(commentPanel)
                                  .addComponent(contentPanel)
                                  .addComponent(commentOffsetPane))
                  .addComponent(insertAfterHighlightArea.getComponent())
      );

      layout.linkSize(SwingConstants.VERTICAL, commentPanel, contentPanel);
      layout.linkSize(SwingConstants.VERTICAL, commentPanel, commentOffsetPane);
      layout.linkSize(SwingConstants.HORIZONTAL, commentPanel, commentOffsetPane);

      // make sure insert location highlights are hidden
      insertBeforeHighlightArea.setVisible(false);
      insertAfterHighlightArea.setVisible(false);

      // By default, don't allow drops on this view--subclasses will override this as necessary.  Disallowing drops
      // here consumes the event and prevents the ContainerView from getting the drop event.
      setTransferHandler(new NoDropsAllowedTransferHandler());

      deleteButton.addActionListener(
            new ActionListener()
            {
            @Override
            public void actionPerformed(final ActionEvent actionEvent)
               {
               final SwingWorker<Object, Object> worker =
                     new SwingWorker<Object, Object>()
                     {
                     @Override
                     protected Object doInBackground() throws Exception
                        {
                        getContainerModel().remove(programElementModel);
                        return null;
                        }
                     };
               worker.execute();
               }
            }
      );
      }

   @Override
   @NotNull
   public final JComponent getComponent()
      {
      return panel;
      }

   @Override
   public final void setIsEnabled(final boolean isEnabled)
      {
      panel.setEnabled(isEnabled);
      insertBeforeHighlightArea.setEnabled(isEnabled);
      insertAfterHighlightArea.setEnabled(isEnabled);
      contentPanel.setEnabled(isEnabled);
      }

   @Override
   public void showInsertLocationBefore()
      {
      hideInsertLocations();
      insertBeforeHighlightArea.setVisible(true);
      }

   @Override
   public void showInsertLocationAfter()
      {
      hideInsertLocations();
      insertAfterHighlightArea.setVisible(true);
      }

   @Override
   public final void showInsertLocation(@Nullable final Point dropPoint)
      {
      hideInsertLocations();
      if (dropPoint != null)
         {
         final boolean isInsertBefore = isInsertLocationBefore(dropPoint);
         insertBeforeHighlightArea.setVisible(isInsertBefore);
         insertAfterHighlightArea.setVisible(!isInsertBefore);
         }
      }

   @Override
   public final void hideInsertLocations()
      {
      insertBeforeHighlightArea.setVisible(false);
      insertAfterHighlightArea.setVisible(false);
      hideInsertLocationsOfContainedViews();
      }

   protected abstract void hideInsertLocationsOfContainedViews();

   @Override
   public final boolean isInsertLocationBefore(@Nullable final Point dropPoint)
      {
      return dropPoint != null && dropPoint.getY() <= panel.getSize().getHeight() / 2;
      }

   /** Used by subclasses to set a {@link TransferHandler} on the main panel for this view */
   protected final void setTransferHandler(@NotNull final TransferHandler transferHandler)
      {
      panel.setTransferHandler(transferHandler);
      }

   @NotNull
   protected final JButton getDeleteButton()
      {
      return deleteButton;
      }

   @NotNull
   protected final JPanel getContentPanel()
      {
      return contentPanel;
      }

   @NotNull
   protected final JScrollPane getCommentTextAreaScrollPane()
      {
      return commentTextAreaScrollPane;
      }

   private static final class CommentToggleButton extends JToggleButton
      {
      private static final Icon SHOW_EMPTY_COMMENT_ICON = ImageUtils.createImageIcon("/edu/cmu/ri/createlab/sequencebuilder/programelement/view/images/show-empty-comment-icon.png");
      private static final Icon HIDE_EMPTY_COMMENT_ICON = ImageUtils.createImageIcon("/edu/cmu/ri/createlab/sequencebuilder/programelement/view/images/hide-empty-comment-icon.png");
      private static final Icon SHOW_NONEMPTY_COMMENT_ICON = ImageUtils.createImageIcon("/edu/cmu/ri/createlab/sequencebuilder/programelement/view/images/show-nonempty-comment-icon.png");
      private static final Icon HIDE_NONEMPTY_COMMENT_ICON = ImageUtils.createImageIcon("/edu/cmu/ri/createlab/sequencebuilder/programelement/view/images/hide-nonempty-comment-icon.png");

      private CommentToggleButton(@NotNull final ProgramElementModel programElementModel)
         {
         setIcon(programElementModel.hasComment(), programElementModel.isCommentVisible());
         this.setSelected(programElementModel.isCommentVisible());

         addItemListener(
               new ItemListener()
               {
               @Override
               public void itemStateChanged(final ItemEvent itemEvent)
                  {
                  // get the new state
                  final boolean newIsCommentVisibleState = itemEvent.getStateChange() == ItemEvent.SELECTED;

                  // set the icon
                  setIcon(programElementModel.hasComment(), newIsCommentVisibleState);

                  // update the model
                  programElementModel.setIsCommentVisible(newIsCommentVisibleState);
                  }
               });

         programElementModel.addPropertyChangeEventListener(ProgramElementModel.HAS_COMMENT_PROPERTY,
                                                            new ProgramElementModel.PropertyChangeEventListener()
                                                            {
                                                            @Override
                                                            public void handlePropertyChange(@NotNull final ProgramElementModel.PropertyChangeEvent event)
                                                               {
                                                               SwingUtils.runInGUIThread(
                                                                     new Runnable()
                                                                     {
                                                                     @Override
                                                                     public void run()
                                                                        {
                                                                        setIcon(programElementModel.hasComment(), programElementModel.isCommentVisible());
                                                                        }
                                                                     }
                                                               );
                                                               }
                                                            });
         }

      private void setIcon(final boolean hasComment, final boolean isCommentVisible)
         {
         if (hasComment)
            {
            if (isCommentVisible)
               {
               setIcon(HIDE_NONEMPTY_COMMENT_ICON);
               }
            else
               {
               setIcon(SHOW_NONEMPTY_COMMENT_ICON);
               }
            }
         else
            {
            if (isCommentVisible)
               {
               setIcon(HIDE_EMPTY_COMMENT_ICON);
               }
            else
               {
               setIcon(SHOW_EMPTY_COMMENT_ICON);
               }
            }
         }
      }

   private class ToggleCommentTextAreaRunnable implements Runnable
      {
      private final boolean isVisible;

      private ToggleCommentTextAreaRunnable(final boolean isVisible)
         {
         this.isVisible = isVisible;
         }

      @Override
      public void run()
         {
         BaseStandardProgramElementView.this.commentTextAreaScrollPane.setVisible(isVisible);

         // repack the frame
         final JFrame jFrame = BaseStandardProgramElementView.this.getContainerView().getJFrame();
         if (jFrame != null)
            {
            jFrame.pack();
            }
         }
      }

   private class IsCommentVisiblePropertyChangeEventListener implements ProgramElementModel.PropertyChangeEventListener
      {
      private Runnable showCommentTextAreaRunnable = new ToggleCommentTextAreaRunnable(true);
      private Runnable hideCommentTextAreaRunnable = new ToggleCommentTextAreaRunnable(false);

      @SuppressWarnings({"ConstantConditions"})
      @Override
      public void handlePropertyChange(@NotNull final ProgramElementModel.PropertyChangeEvent event)
         {
         SwingUtils.runInGUIThread(
               (Boolean)event.getNewValue() ? showCommentTextAreaRunnable : hideCommentTextAreaRunnable
         );
         }
      }
   }
