package edu.cmu.ri.createlab.sequencebuilder.programelement.view.standard;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.util.PropertyResourceBundle;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import edu.cmu.ri.createlab.sequencebuilder.ContainerView;
import edu.cmu.ri.createlab.sequencebuilder.programelement.model.ProgramElementModel;
import edu.cmu.ri.createlab.sequencebuilder.programelement.model.SavedSequenceModel;
import edu.cmu.ri.createlab.sequencebuilder.programelement.view.dnd.ProgramElementDestinationTransferHandler;
import org.jetbrains.annotations.NotNull;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public class StandardSavedSequenceView extends BaseStandardProgramElementView<SavedSequenceModel>
   {
   private static final PropertyResourceBundle RESOURCES = (PropertyResourceBundle)PropertyResourceBundle.getBundle(StandardSavedSequenceView.class.getName());

   public StandardSavedSequenceView(@NotNull final ContainerView containerView, @NotNull final SavedSequenceModel model)
      {
      super(containerView, model);

      final JTextArea titleLabel = new JTextArea(2, 15);
      final JButton deleteButton = getDeleteButton();

      titleLabel.setEditable(false);
      titleLabel.setText(model.getName());//(RESOURCES.getString("title.label") + ": " + model.getName());

      titleLabel.setLineWrap(true);
      titleLabel.setWrapStyleWord(true);

      //Element Layout*****************************
      final JPanel panel = getContentPanel();
      panel.setLayout(new GridBagLayout());

      GridBagConstraints c = new GridBagConstraints();

      c.gridx = 1;
      c.gridy = 0;
      c.gridwidth = 1;
      c.gridheight = 1;
      c.weightx = 0.0;
      c.weighty = 0.0;
      c.anchor = GridBagConstraints.FIRST_LINE_END;
      c.fill = GridBagConstraints.NONE;
      panel.add(deleteButton, c);

      c.gridx = 0;
      c.gridy = 1;
      c.gridwidth = 2;
      c.gridheight = 1;
      c.weightx = 1.0;
      c.weighty = 1.0;
      c.anchor = GridBagConstraints.PAGE_START;
      c.fill = GridBagConstraints.HORIZONTAL;
      panel.add(titleLabel, c);

      //Skinning Information**********************
      final Dimension title_size = titleLabel.getPreferredSize();
      titleLabel.setPreferredSize(title_size);
      titleLabel.setMaximumSize(title_size);
      titleLabel.setMinimumSize(title_size);

      //Background color dependent on container type
      final String panelStyle = containerView.hasParentProgramElementView() ? "sequenceElementLoop" : "sequenceElement";
      panel.setName(panelStyle);

      final Dimension block_size = new Dimension(180, 120);
      panel.setPreferredSize(block_size);
      panel.setMaximumSize(block_size);
      panel.setMinimumSize(block_size);

      titleLabel.setName("sequenceBlockTitle");

      final JScrollPane commentTextAreaScrollPane = getCommentTextAreaScrollPane();
      final Dimension comment_size = commentTextAreaScrollPane.getPreferredSize();
      commentTextAreaScrollPane.setPreferredSize(new Dimension(comment_size.width, block_size.height - 6));
      commentTextAreaScrollPane.setMaximumSize(new Dimension(comment_size.width, block_size.height - 6));
      commentTextAreaScrollPane.setMinimumSize(new Dimension(comment_size.width, block_size.height - 6));
      //*******************************************

      setTransferHandler(
            new ProgramElementDestinationTransferHandler()
            {
            @Override
            protected final void showInsertLocation(final Point dropPoint)
               {
               StandardSavedSequenceView.this.showInsertLocation(dropPoint);
               }

            @Override
            protected final void performImport(@NotNull final ProgramElementModel model, @NotNull final Point dropPoint)
               {
               getContainerView().handleDropOfModelOntoView(model,
                                                            StandardSavedSequenceView.this,
                                                            isInsertLocationBefore(dropPoint));
               }
            });
      }

   @Override
   protected void hideInsertLocationsOfContainedViews()
      {
      // nothing to do, there are no contained views
      }
   }
