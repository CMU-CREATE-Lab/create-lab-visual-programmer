package edu.cmu.ri.createlab.sequencebuilder.programelement.view.standard;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.PropertyResourceBundle;
import javax.swing.Box;
import javax.swing.GroupLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import edu.cmu.ri.createlab.sequencebuilder.ContainerModel;
import edu.cmu.ri.createlab.sequencebuilder.ContainerView;
import edu.cmu.ri.createlab.sequencebuilder.programelement.model.LoopableConditionalModel;
import edu.cmu.ri.createlab.sequencebuilder.programelement.view.ViewConstants;
import edu.cmu.ri.createlab.sequencebuilder.programelement.view.dnd.AlwaysInsertAfterTransferHandler;
import edu.cmu.ri.createlab.sequencebuilder.programelement.view.dnd.AlwaysInsertBeforeTransferHandler;
import edu.cmu.ri.createlab.userinterface.util.ImageUtils;
import edu.cmu.ri.createlab.userinterface.util.SwingUtils;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public class StandardLoopableConditionalView extends BaseStandardProgramElementView<LoopableConditionalModel>
   {
   private static final Logger LOG = Logger.getLogger(StandardLoopableConditionalView.class);

   private static final PropertyResourceBundle RESOURCES = (PropertyResourceBundle)PropertyResourceBundle.getBundle(StandardLoopableConditionalView.class.getName());
   private static final Dimension PREFERRED_CONTAINER_DIMENSION = new Dimension(196, 160);

   public StandardLoopableConditionalView(@NotNull final ContainerView containerView, @NotNull final LoopableConditionalModel model)
      {
      super(containerView, model);

      final ContainerModel ifBranchLoopContainerModel = new ContainerModel();
      final ContainerView ifBranchLoopContainerView = new ContainerView(containerView.getJFrame(), ifBranchLoopContainerModel, new StandardViewFactory(), this);
      final ContainerModel elseBranchLoopContainerModel = new ContainerModel();
      final ContainerView elseBranchLoopContainerView = new ContainerView(containerView.getJFrame(), elseBranchLoopContainerModel, new StandardViewFactory(), this);

      // configure the top bar area ------------------------------------------------------------------------------------

      final JPanel topBarPanel = new JPanel();
      final JLabel titleLabel = new JLabel(RESOURCES.getString("title.label"));
      titleLabel.setName("loopBlockTitle");

      final JButton deleteButton = getDeleteButton();
      final JButton editButton = new JButton(ImageUtils.createImageIcon("/edu/cmu/ri/createlab/sequencebuilder/programelement/view/images/editMark.png"));
      editButton.setName("thinButton");

      final JPanel thresholdPanel = new JPanel();

      final JLabel thresholdPanelPlaceholderText = SwingUtils.createLabel("Sensor threshold bar goes here...");

      final GroupLayout thresholdPanelLayout = new GroupLayout(thresholdPanel);
      thresholdPanel.setLayout(thresholdPanelLayout);

      thresholdPanelLayout.setHorizontalGroup(
            thresholdPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
                  .addComponent(thresholdPanelPlaceholderText)
      );
      thresholdPanelLayout.setVerticalGroup(
            thresholdPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
                  .addComponent(thresholdPanelPlaceholderText)
      );

      topBarPanel.setLayout(new GridBagLayout());
      final GridBagConstraints c = new GridBagConstraints();

      c.gridx = 0;
      c.gridy = 0;
      c.gridwidth = 1;
      c.gridheight = 1;
      c.weightx = 1.0;
      c.weighty = 0.0;
      c.anchor = GridBagConstraints.FIRST_LINE_START;
      c.fill = GridBagConstraints.NONE;
      topBarPanel.add(titleLabel, c);

      c.gridx = 1;
      c.gridy = 0;
      c.gridwidth = 1;
      c.gridheight = 1;
      c.weightx = 0.0;
      c.weighty = 0.0;
      c.anchor = GridBagConstraints.PAGE_START;
      c.fill = GridBagConstraints.NONE;
      topBarPanel.add(editButton, c);

      c.gridx = 2;
      c.gridy = 0;
      c.gridwidth = 1;
      c.gridheight = 1;
      c.weightx = 0.0;
      c.weighty = 0.0;
      c.anchor = GridBagConstraints.FIRST_LINE_END;
      c.fill = GridBagConstraints.NONE;
      topBarPanel.add(deleteButton, c);

      c.gridx = 0;
      c.gridy = 1;
      c.gridwidth = 3;
      c.gridheight = 1;
      c.weightx = 1.0;
      c.weighty = 0.0;
      c.anchor = GridBagConstraints.PAGE_END;
      c.fill = GridBagConstraints.BOTH;
      c.insets = new Insets(4, 0, 4, 0);
      topBarPanel.add(thresholdPanel, c);

      topBarPanel.setBackground(ViewConstants.Colors.LOOP_ELEMENT_BACKGROUND_COLOR);
      titleLabel.setBackground(ViewConstants.Colors.LOOP_ELEMENT_BACKGROUND_COLOR);
      thresholdPanel.setBackground(ViewConstants.Colors.LOOP_ELEMENT_BACKGROUND_COLOR);

      topBarPanel.setTransferHandler(new AlwaysInsertBeforeTransferHandler(StandardLoopableConditionalView.this, containerView));

      // configure the bottom bar area ---------------------------------------------------------------------------------

      final JToggleButton ifBranchLoopToggleButton =
            new LoopToggleButton(StandardLoopableConditionalView.this.getProgramElementModel().willReevaluateConditionAfterIfBranchCompletes())
            {
            @Override
            protected void updateWillReevaluateConditional(final boolean willReevaluateConditional)
               {
               StandardLoopableConditionalView.this.getProgramElementModel().setWillReevaluateConditionAfterIfBranchCompletes(willReevaluateConditional);
               }
            };
      final JToggleButton elseBranchLoopToggleButton =
            new LoopToggleButton(StandardLoopableConditionalView.this.getProgramElementModel().willReevaluateConditionAfterElseBranchCompletes())
            {
            @Override
            protected void updateWillReevaluateConditional(final boolean willReevaluateConditional)
               {
               StandardLoopableConditionalView.this.getProgramElementModel().setWillReevaluateConditionAfterElseBranchCompletes(willReevaluateConditional);
               }
            };
      final JPanel bottomBarPanel = new JPanel();
      bottomBarPanel.setLayout(new GridBagLayout());

      c.gridx = 0;
      c.gridy = 0;
      c.gridwidth = 1;
      c.gridheight = 1;
      c.weightx = 1.0;
      c.weighty = 0.0;
      c.anchor = GridBagConstraints.PAGE_START;
      c.fill = GridBagConstraints.NONE;
      c.insets = new Insets(0, 0, 0, 0);
      bottomBarPanel.add(ifBranchLoopToggleButton, c);

      c.gridx = 1;
      c.gridy = 0;
      c.gridwidth = 1;
      c.gridheight = 1;
      c.weightx = 1.0;
      c.weighty = 0.0;
      c.anchor = GridBagConstraints.PAGE_START;
      c.fill = GridBagConstraints.NONE;
      bottomBarPanel.add(elseBranchLoopToggleButton, c);

      ifBranchLoopToggleButton.setName("loopToggleButton");
      elseBranchLoopToggleButton.setName("loopToggleButton");

      bottomBarPanel.setBackground(ViewConstants.Colors.LOOP_ELEMENT_BACKGROUND_COLOR);

      bottomBarPanel.setTransferHandler(new AlwaysInsertAfterTransferHandler(StandardLoopableConditionalView.this, containerView));

      // configure the container area panels ---------------------------------------------------------------------------

      final JComponent ifBranchContainerViewPanel = ifBranchLoopContainerView.getComponent();
      ifBranchContainerViewPanel.setMinimumSize(PREFERRED_CONTAINER_DIMENSION);
      ifBranchContainerViewPanel.setMaximumSize(new Dimension(PREFERRED_CONTAINER_DIMENSION.width, ifBranchContainerViewPanel.getMaximumSize().height));

      final JComponent elseBranchContainerViewPanel = elseBranchLoopContainerView.getComponent();
      elseBranchContainerViewPanel.setMinimumSize(PREFERRED_CONTAINER_DIMENSION);
      elseBranchContainerViewPanel.setMaximumSize(new Dimension(PREFERRED_CONTAINER_DIMENSION.width, elseBranchContainerViewPanel.getMaximumSize().height));

      ifBranchContainerViewPanel.setName("loopFrame");
      elseBranchContainerViewPanel.setName("loopFrame");

      final Component containerLeftIndent = Box.createHorizontalStrut(10);
      final Component containerDivider = Box.createHorizontalStrut(2);
      final Component containerRightIndent = Box.createHorizontalStrut(10);

      // assemble the content panel -----------------------------------------------------------------------------------

      final JPanel contentPanel = getContentPanel();
      final GroupLayout contentPanelLayout = new GroupLayout(contentPanel);
      contentPanel.setLayout(contentPanelLayout);

      contentPanelLayout.setHorizontalGroup(
            contentPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
                  .addComponent(topBarPanel)
                  .addGroup(contentPanelLayout.createSequentialGroup()
                        //.addComponent(containerLeftIndent)
                                  .addComponent(ifBranchContainerViewPanel)
                                  .addComponent(containerDivider)
                                  .addComponent(elseBranchContainerViewPanel)
                            //.addComponent(containerRightIndent)
                  )
                  .addComponent(bottomBarPanel)
      );
      contentPanelLayout.setVerticalGroup(
            contentPanelLayout.createSequentialGroup()
                  .addComponent(topBarPanel)
                  .addGroup(contentPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
                        //.addComponent(containerLeftIndent)
                                  .addComponent(ifBranchContainerViewPanel)
                                  .addComponent(containerDivider)
                                  .addComponent(elseBranchContainerViewPanel)
                            //.addComponent(containerRightIndent)
                  )
                  .addComponent(bottomBarPanel)
      );

      //Background color dependent on container type
      final String panelStyle = containerView.hasParentProgramElementView() ? "loopElementLoop" : "loopElement";
      contentPanel.setName(panelStyle);

      //contentPanel.setMaximumSize(new Dimension(PREFERRED_CONTAINER_DIMENSION.width * 2 + 24, contentPanel.getMaximumSize().height));

      LOG.debug("StandardLoopableConditionalView.StandardLoopableConditionalView()");
      }

   private abstract static class LoopToggleButton extends JToggleButton
      {
      private static final Icon LOOP_ICON = ImageUtils.createImageIcon("/edu/cmu/ri/createlab/sequencebuilder/programelement/view/images/toggle-button-loop-icon.png");
      private static final Icon PASS_THROUGH_ICON = ImageUtils.createImageIcon("/edu/cmu/ri/createlab/sequencebuilder/programelement/view/images/toggle-button-passthrough-icon.png");

      private LoopToggleButton(final boolean initialStateOfWillReevaluateConditional)
         {
         super(PASS_THROUGH_ICON);

         setIcon(initialStateOfWillReevaluateConditional);
         this.setSelected(initialStateOfWillReevaluateConditional);

         addItemListener(
               new ItemListener()
               {
               @Override
               public void itemStateChanged(final ItemEvent itemEvent)
                  {
                  final boolean willReevaluateConditional = itemEvent.getStateChange() == ItemEvent.SELECTED;
                  setIcon(willReevaluateConditional);
                  updateWillReevaluateConditional(willReevaluateConditional);
                  }
               });
         }

      private void setIcon(final boolean willReevaluateConditional)
         {
         setIcon(willReevaluateConditional ? LOOP_ICON : PASS_THROUGH_ICON);
         }

      protected abstract void updateWillReevaluateConditional(final boolean willReevaluateConditional);
      }
   }
