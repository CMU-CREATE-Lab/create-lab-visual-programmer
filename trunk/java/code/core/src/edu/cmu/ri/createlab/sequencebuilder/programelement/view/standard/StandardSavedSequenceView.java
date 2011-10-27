package edu.cmu.ri.createlab.sequencebuilder.programelement.view.standard;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.util.Map;
import java.util.PropertyResourceBundle;
import javax.swing.*;

import edu.cmu.ri.createlab.sequencebuilder.ContainerView;
import edu.cmu.ri.createlab.sequencebuilder.programelement.model.*;
import edu.cmu.ri.createlab.sequencebuilder.programelement.view.dnd.ProgramElementDestinationTransferHandler;
import edu.cmu.ri.createlab.userinterface.util.ImageUtils;
import edu.cmu.ri.createlab.userinterface.util.SwingUtils;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public class StandardSavedSequenceView extends BaseStandardProgramElementView<SavedSequenceModel>
   {
   private static final Logger LOG = Logger.getLogger(StandardSavedSequenceView.class);
   private final JProgressBar progressBar = new JProgressBar(0,100);
   private static final PropertyResourceBundle RESOURCES = (PropertyResourceBundle)PropertyResourceBundle.getBundle(StandardSavedSequenceView.class.getName());

   private final MyExecutionEventListener executionEventListener = new MyExecutionEventListener();

   public StandardSavedSequenceView(@NotNull final ContainerView containerView, @NotNull final SavedSequenceModel model)
      {
      super(containerView, model);

      final JTextArea titleLabel = new JTextArea(2, 15);
      final JButton deleteButton = getDeleteButton();

      progressBar.setName("delay_progress");

      titleLabel.setEditable(false);
      titleLabel.setText(model.getName());

      titleLabel.setLineWrap(true);
      titleLabel.setWrapStyleWord(true);

      model.addExecutionEventListener(executionEventListener);/*
            new SavedSequenceModel.ExecutionEventListener()
            {
            @Override
            public void handleExecutionStart()
               {
               LOG.debug("StandardSavedSequenceView.handleExecutionStart()");
               }

            @Override
            public void handleExecutionEnd()
               {
               LOG.debug("StandardSavedSequenceView.handleExecutionEnd()");
               }
            }
      );*/

     Map elementCounts = model.getElementCounts();

      Integer sequenceCount = new Integer(0);
      Integer expressionCount = new Integer(0);
      Integer loopCount = new Integer(0);

      if (elementCounts.containsKey(SavedSequenceModel.XML_ELEMENT_NAME)){
          sequenceCount = new Integer((Integer)elementCounts.get(SavedSequenceModel.XML_ELEMENT_NAME));
      }

      if (elementCounts.containsKey(ExpressionModel.XML_ELEMENT_NAME)){
          expressionCount = new Integer((Integer)elementCounts.get(ExpressionModel.XML_ELEMENT_NAME));
      }

      int tempCount = 0;

      if (elementCounts.containsKey(LoopableConditionalModel.XML_ELEMENT_NAME)){
          tempCount = ((Integer)elementCounts.get(LoopableConditionalModel.XML_ELEMENT_NAME)).intValue();
          loopCount = new Integer(tempCount);
      }

      if (elementCounts.containsKey(CounterLoopModel.XML_ELEMENT_NAME)){
          tempCount = ((Integer)elementCounts.get(CounterLoopModel.XML_ELEMENT_NAME)).intValue();
          loopCount = new Integer(tempCount);
      }


      final JPanel contentsPanel = new JPanel();
      final JLabel sequenceCountIcon = new JLabel(sequenceCount.toString());
      final JLabel expressionCountIcon = new JLabel(expressionCount.toString());
      final JLabel loopCountIcon = new JLabel(loopCount.toString());

      final JLabel sequenceIcon = new JLabel(ImageUtils.createImageIcon("/edu/cmu/ri/createlab/sequencebuilder/programelement/view/images/sequenceCountIcon.png"), JLabel.CENTER);
      final JLabel expressionIcon = new JLabel(ImageUtils.createImageIcon("/edu/cmu/ri/createlab/sequencebuilder/programelement/view/images/expressionCountIcon.png"), JLabel.CENTER);
      final JLabel loopIcon = new JLabel(ImageUtils.createImageIcon("/edu/cmu/ri/createlab/sequencebuilder/programelement/view/images/loopCountIcon.png"), JLabel.CENTER);


      sequenceCountIcon.setToolTipText("Sequences");
      expressionCountIcon.setToolTipText("Expressions");
      loopCountIcon.setToolTipText("Structures");

      sequenceCountIcon.setVerticalTextPosition(JLabel.BOTTOM);
      sequenceCountIcon.setHorizontalTextPosition(JLabel.CENTER);

      expressionCountIcon.setVerticalTextPosition(JLabel.BOTTOM);
      expressionCountIcon.setHorizontalTextPosition(JLabel.CENTER);

      loopCountIcon.setVerticalTextPosition(JLabel.BOTTOM);
      loopCountIcon.setHorizontalTextPosition(JLabel.CENTER);

      //Gray out (disable) icons with zero counts
      if (loopCount < 1){
        loopIcon.setEnabled(false);
      }
      else{
        loopIcon.setEnabled(true);
      }

      if (expressionCount < 1){
        expressionIcon.setEnabled(false);
      }
      else{
         expressionIcon.setEnabled(true);
      }

      if (sequenceCount <1){
         sequenceIcon.setEnabled(false);
      }
      else{
        sequenceIcon.setEnabled(true);
      }

      //Limits top end of count numbers
      if (loopCount > 99){
        loopCountIcon.setText("99+");
      }
      if (expressionCount > 99){
         expressionCountIcon.setText("99+");
      }
      if (sequenceCount > 99){
         sequenceCountIcon.setText("99+");
      }

      contentsPanel.setLayout(new GridBagLayout());
      final GridBagConstraints c = new GridBagConstraints();

      c.gridx = 0;
      c.gridy = 0;
      c.gridwidth = 1;
      c.gridheight = 1;
      c.weightx = 0.5;
      c.weighty = 1.0;
      c.anchor = GridBagConstraints.CENTER;
      c.fill = GridBagConstraints.NONE;
      contentsPanel.add(expressionIcon, c);
      c.gridx = 1;
      c.gridy = 0;
      c.gridwidth = 1;
      c.gridheight = 1;
      c.weightx = 0.0;
      c.weighty = 1.0;
      c.anchor = GridBagConstraints.CENTER;
      c.fill = GridBagConstraints.NONE;
      contentsPanel.add(sequenceIcon, c);
      c.gridx = 2;
      c.gridy = 0;
      c.gridwidth = 1;
      c.gridheight = 1;
      c.weightx = 0.5;
      c.weighty = 1.0;
      c.anchor = GridBagConstraints.CENTER;
      c.fill = GridBagConstraints.NONE;
      contentsPanel.add(loopIcon, c);

      c.gridx = 0;
      c.gridy = 1;
      c.gridwidth = 1;
      c.gridheight = 1;
      c.weightx = 0.5;
      c.weighty = 1.0;
      c.anchor = GridBagConstraints.CENTER;
      c.fill = GridBagConstraints.NONE;
      contentsPanel.add(expressionCountIcon, c);
      c.gridx = 1;
      c.gridy = 1;
      c.gridwidth = 1;
      c.gridheight = 1;
      c.weightx = 0.0;
      c.weighty = 1.0;
      c.anchor = GridBagConstraints.CENTER;
      c.fill = GridBagConstraints.NONE;
      contentsPanel.add(sequenceCountIcon, c);
      c.gridx = 2;
      c.gridy = 1;
      c.gridwidth = 1;
      c.gridheight = 1;
      c.weightx = 0.5;
      c.weighty = 1.0;
      c.anchor = GridBagConstraints.CENTER;
      c.fill = GridBagConstraints.NONE;
      contentsPanel.add(loopCountIcon, c);

      contentsPanel.setName("sequenceBlockGreen");

      //Element Layout*****************************
      final JPanel panel = getContentPanel();
      panel.setLayout(new GridBagLayout());



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
      c.weighty = 0.0;
      c.anchor = GridBagConstraints.CENTER;
      c.fill = GridBagConstraints.HORIZONTAL;
      panel.add(titleLabel, c);

      c.gridx = 0;
      c.gridy = 2;
      c.gridwidth = 2;
      c.gridheight = 1;
      c.weightx = 1.0;
      c.weighty = 1.0;
      c.anchor = GridBagConstraints.PAGE_END;
      c.fill = GridBagConstraints.HORIZONTAL;
      panel.add(contentsPanel, c);

      c.gridx = 0;
      c.gridy = 3;
      c.gridwidth = 2;
      c.gridheight = 1;
      c.weightx = 1.0;
      c.weighty = .50;
      c.anchor = GridBagConstraints.CENTER;
      c.fill = GridBagConstraints.HORIZONTAL;
      panel.add(progressBar, c);


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


       private final class MyExecutionEventListener implements SavedSequenceModel.ExecutionEventListener
            {
            private final Runnable handleExecutionStartRunnable =
                  new Runnable()
                  {
                  @Override
                  public void run()
                     {
                     progressBar.setIndeterminate(false);
                     progressBar.setValue(progressBar.getMinimum());
                     }
                  };

            private final Runnable handleExecutionEndRunnable =
                  new Runnable()
                  {
                  @Override
                  public void run()
                     {
                     progressBar.setIndeterminate(false);
                     progressBar.setValue(progressBar.getMaximum());
                     }
                  };

            @Override
            public void handleExecutionStart()
               {
               SwingUtils.runInGUIThread(handleExecutionStartRunnable);
               }

            @Override
            public void handleExecutionVisual()
               {
               SwingUtils.runInGUIThread(
                     new Runnable()
                     {
                     @Override
                     public void run()
                        {
                        progressBar.setIndeterminate(true);
                        }
                     }
               );
               }

            @Override
            public void handleExecutionEnd()
               {
               SwingUtils.runInGUIThread(handleExecutionEndRunnable);
               }
            }


   @Override
   protected void hideInsertLocationsOfContainedViews()
      {
      // nothing to do, there are no contained views
      }

   @Override
   public final void resetViewForSequenceExecution()
      {
      // TODO
      }


   }
