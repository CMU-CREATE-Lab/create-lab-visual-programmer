package edu.cmu.ri.createlab.sequencebuilder.programelement.view.standard;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.HashMap;
import java.util.Map;
import java.util.PropertyResourceBundle;
import java.util.SortedSet;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JToggleButton;
import javax.swing.ListCellRenderer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import edu.cmu.ri.createlab.sequencebuilder.ContainerModel;
import edu.cmu.ri.createlab.sequencebuilder.ContainerView;
import edu.cmu.ri.createlab.sequencebuilder.programelement.model.LoopableConditionalModel;
import edu.cmu.ri.createlab.sequencebuilder.programelement.model.ProgramElementModel;
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

   private final JButton displayModeEditButton = new JButton(ImageUtils.createImageIcon("/edu/cmu/ri/createlab/sequencebuilder/programelement/view/images/editMark.png"));
   private final JButton editModeEditButton = new JButton(ImageUtils.createImageIcon("/edu/cmu/ri/createlab/sequencebuilder/programelement/view/images/editMark.png"));
   private final JLabel sensorTypeLabel = new JLabel("");
   private final JComboBox sensorTypeComboBox = new JComboBox();
   private final JLabel sensorPortNumberValueLabel = new JLabel("");
   private final JComboBox sensorPortNumberValueComboBox = new JComboBox();
   private final JSlider sensorThresholdPercentageSlider = new JSlider(0, 100);

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
      displayModeEditButton.setName("thinButton");
      editModeEditButton.setName("thinButton");
      displayModeEditButton.setVisible(false);  // we're initially in edit mode

      final JPanel editButtonsPanel = new JPanel();
      editButtonsPanel.setLayout(new BoxLayout(editButtonsPanel, BoxLayout.X_AXIS));
      editButtonsPanel.add(displayModeEditButton);
      editButtonsPanel.add(editModeEditButton);

      // Create the sensor config components and their panel

      final JLabel sensorPortNumberLabel = new JLabel(RESOURCES.getString("sensor-port-number.label"));
      final JLabel ifBranchValueLabel = new JLabel();
      final JLabel elseBranchValueLabel = new JLabel();
      final DefaultComboBoxModel sensorTypeComboBoxModel = new DefaultComboBoxModel();
      final SortedSet<LoopableConditionalModel.SensorType> sensorTypes = model.getVisualProgrammerDevice().getSensorTypes();
      final LoopableConditionalModel.SelectedSensor currentlySelectedSensor = model.getSelectedSensor();

      // set current values
      ifBranchValueLabel.setText(currentlySelectedSensor.getSensorType().getIfBranchValueLabel());
      elseBranchValueLabel.setText(currentlySelectedSensor.getSensorType().getElseBranchValueLabel());
      sensorTypeLabel.setText(currentlySelectedSensor.getSensorType().getName());
      sensorPortNumberValueLabel.setText(String.valueOf(currentlySelectedSensor.getPortNumber() + 1));

      int i = 0;
      int selectedItemIndex = 0;
      final Map<LoopableConditionalModel.SensorType, ComboBoxModel> sensorTypeToPortNumberValueComboBoxModel = new HashMap<LoopableConditionalModel.SensorType, ComboBoxModel>(sensorTypes.size());
      for (final LoopableConditionalModel.SensorType sensorType : sensorTypes)
         {
         if (sensorType.equals(currentlySelectedSensor.getSensorType()))
            {
            selectedItemIndex = i;
            }
         sensorTypeComboBoxModel.addElement(sensorType);
         final DefaultComboBoxModel portNumberComboBoxModel = new DefaultComboBoxModel();
         for (int j = 1; j <= sensorType.getNumPorts(); j++)
            {
            portNumberComboBoxModel.addElement(j);
            }
         sensorTypeToPortNumberValueComboBoxModel.put(sensorType, portNumberComboBoxModel);
         i++;
         }
      sensorTypeComboBox.setModel(sensorTypeComboBoxModel);
      sensorTypeComboBox.setRenderer(
            new ListCellRenderer()
            {
            @Override
            public Component getListCellRendererComponent(final JList list,
                                                          final Object value,
                                                          final int index,
                                                          final boolean isSelected,
                                                          final boolean cellHasFocus)
               {
               final String labelText;
               if (value == null)
                  {
                  labelText = "Unknown";
                  }
               else
                  {
                  labelText = ((LoopableConditionalModel.SensorType)value).getName();
                  }
               return new JLabel(labelText);
               }
            }
      );
      sensorPortNumberValueComboBox.setRenderer(
            new ListCellRenderer()
            {
            @Override
            public Component getListCellRendererComponent(final JList list,
                                                          final Object value,
                                                          final int index,
                                                          final boolean isSelected,
                                                          final boolean cellHasFocus)
               {
               return new JLabel(String.valueOf(value));
               }
            }
      );
      sensorTypeComboBox.setSelectedIndex(selectedItemIndex);
      sensorPortNumberValueComboBox.setModel(sensorTypeToPortNumberValueComboBoxModel.get(currentlySelectedSensor.getSensorType()));
      sensorPortNumberValueComboBox.setSelectedIndex(currentlySelectedSensor.getPortNumber());
      sensorThresholdPercentageSlider.setValue(currentlySelectedSensor.getThresholdPercentage());
      sensorTypeComboBox.addActionListener(
            new ActionListener()
            {
            @Override
            public void actionPerformed(final ActionEvent actionEvent)
               {
               final JComboBox comboBox = (JComboBox)actionEvent.getSource();
               final LoopableConditionalModel.SensorType sensorType = (LoopableConditionalModel.SensorType)(comboBox.getSelectedItem());
               final ComboBoxModel portNumberComboBoxModel = sensorTypeToPortNumberValueComboBoxModel.get(sensorType);
               sensorPortNumberValueComboBox.setModel(portNumberComboBoxModel);

               model.setSelectedSensor(new LoopableConditionalModel.SelectedSensor(sensorType,
                                                                                   sensorPortNumberValueComboBox.getSelectedIndex(),
                                                                                   sensorThresholdPercentageSlider.getValue()));
               }
            }
      );
      sensorPortNumberValueComboBox.addActionListener(
            new ActionListener()
            {
            @Override
            public void actionPerformed(final ActionEvent actionEvent)
               {
               final JComboBox comboBox = (JComboBox)actionEvent.getSource();
               final Integer port = (Integer)(comboBox.getSelectedItem());

               model.setSelectedSensor(new LoopableConditionalModel.SelectedSensor((LoopableConditionalModel.SensorType)sensorTypeComboBox.getSelectedItem(),
                                                                                   port - 1,
                                                                                   sensorThresholdPercentageSlider.getValue()));
               }
            }
      );
      sensorThresholdPercentageSlider.addChangeListener(
            new ChangeListener()
            {
            @Override
            public void stateChanged(final ChangeEvent changeEvent)
               {
               final JSlider source = (JSlider)changeEvent.getSource();
               if (!source.getValueIsAdjusting())
                  {
                  final int percentage = source.getValue();
                  model.setSelectedSensor(new LoopableConditionalModel.SelectedSensor((LoopableConditionalModel.SensorType)sensorTypeComboBox.getSelectedItem(),
                                                                                      sensorPortNumberValueComboBox.getSelectedIndex(),
                                                                                      percentage));
                  }
               }
            }
      );

      setIsSensorConfigDisplayMode(false);

      final JPanel sensorConfigPanel = new JPanel();
      final GroupLayout displaySensorConfigLayout = new GroupLayout(sensorConfigPanel);
      sensorConfigPanel.setLayout(displaySensorConfigLayout);
      displaySensorConfigLayout.setHorizontalGroup(
            displaySensorConfigLayout.createSequentialGroup()
                  .addComponent(ifBranchValueLabel)
                  .addGroup(displaySensorConfigLayout.createParallelGroup(GroupLayout.Alignment.TRAILING, false)
                                  .addGroup(displaySensorConfigLayout.createSequentialGroup()
                                                  .addComponent(sensorTypeLabel)
                                                  .addComponent(sensorTypeComboBox)
                                  )
                                  .addGroup(displaySensorConfigLayout.createSequentialGroup()
                                                  .addComponent(sensorPortNumberLabel)
                                                  .addComponent(sensorPortNumberValueLabel)
                                                  .addComponent(sensorPortNumberValueComboBox)
                                  )
                                  .addComponent(sensorThresholdPercentageSlider)
                  )
                  .addComponent(elseBranchValueLabel)
      );
      displaySensorConfigLayout.setVerticalGroup(
            displaySensorConfigLayout.createSequentialGroup()
                  .addGroup(displaySensorConfigLayout.createParallelGroup(GroupLayout.Alignment.CENTER, false)
                                  .addComponent(sensorTypeLabel)
                                  .addComponent(sensorTypeComboBox)
                  )
                  .addGroup(displaySensorConfigLayout.createParallelGroup(GroupLayout.Alignment.CENTER, false)
                                  .addComponent(sensorPortNumberLabel)
                                  .addComponent(sensorPortNumberValueLabel)
                                  .addComponent(sensorPortNumberValueComboBox)
                  )
                  .addGroup(displaySensorConfigLayout.createParallelGroup(GroupLayout.Alignment.CENTER, false)
                                  .addComponent(ifBranchValueLabel)
                                  .addComponent(sensorThresholdPercentageSlider)
                                  .addComponent(elseBranchValueLabel)
                  )
      );

      displayModeEditButton.addActionListener(
            new ActionListener()
            {
            @Override
            public void actionPerformed(final ActionEvent actionEvent)
               {
               setIsSensorConfigDisplayMode(false);
               sensorTypeComboBox.requestFocusInWindow();
               }
            });

      editModeEditButton.addActionListener(
            new ActionListener()
            {
            @Override
            public void actionPerformed(final ActionEvent actionEvent)
               {
               setIsSensorConfigDisplayMode(true);
               }
            });

      model.addPropertyChangeEventListener(LoopableConditionalModel.SELECTED_SENSOR_PROPERTY,
                                           new ProgramElementModel.PropertyChangeEventListener()
                                           {
                                           @Override
                                           public void handlePropertyChange(@NotNull final ProgramElementModel.PropertyChangeEvent event)
                                              {
                                              SwingUtils.runInGUIThread(
                                                    new Runnable()
                                                    {
                                                    @SuppressWarnings({"ConstantConditions"})
                                                    @Override
                                                    public void run()
                                                       {
                                                       final LoopableConditionalModel.SelectedSensor selectedSensor = (LoopableConditionalModel.SelectedSensor)event.getNewValue();
                                                       LOG.debug("CHANGE EVENT: new selected sensor is: " + selectedSensor);

                                                       ifBranchValueLabel.setText(selectedSensor.getSensorType().getIfBranchValueLabel());
                                                       elseBranchValueLabel.setText(selectedSensor.getSensorType().getElseBranchValueLabel());
                                                       sensorTypeLabel.setText(selectedSensor.getSensorType().getName());
                                                       sensorPortNumberValueLabel.setText(String.valueOf(selectedSensor.getPortNumber() + 1));
                                                       }
                                                    }
                                              );
                                              }
                                           });

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
      topBarPanel.add(editButtonsPanel, c);

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
      topBarPanel.add(sensorConfigPanel, c);

      topBarPanel.setBackground(ViewConstants.Colors.LOOP_ELEMENT_BACKGROUND_COLOR);
      titleLabel.setBackground(ViewConstants.Colors.LOOP_ELEMENT_BACKGROUND_COLOR);
      sensorConfigPanel.setBackground(ViewConstants.Colors.LOOP_ELEMENT_BACKGROUND_COLOR);

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

      final Component containerDivider = Box.createHorizontalStrut(2);

      // assemble the content panel -----------------------------------------------------------------------------------

      final JPanel contentPanel = getContentPanel();
      final GroupLayout contentPanelLayout = new GroupLayout(contentPanel);
      contentPanel.setLayout(contentPanelLayout);

      contentPanelLayout.setHorizontalGroup(
            contentPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
                  .addComponent(topBarPanel)
                  .addGroup(contentPanelLayout.createSequentialGroup()
                                  .addComponent(ifBranchContainerViewPanel)
                                  .addComponent(containerDivider)
                                  .addComponent(elseBranchContainerViewPanel)
                  )
                  .addComponent(bottomBarPanel)
      );
      contentPanelLayout.setVerticalGroup(
            contentPanelLayout.createSequentialGroup()
                  .addComponent(topBarPanel)
                  .addGroup(contentPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
                                  .addComponent(ifBranchContainerViewPanel)
                                  .addComponent(containerDivider)
                                  .addComponent(elseBranchContainerViewPanel)
                  )
                  .addComponent(bottomBarPanel)
      );

      //Background color dependent on container type
      final String panelStyle = containerView.hasParentProgramElementView() ? "loopElementLoop" : "loopElement";
      contentPanel.setName(panelStyle);

      LOG.debug("StandardLoopableConditionalView.StandardLoopableConditionalView()");
      }

   private void setIsSensorConfigDisplayMode(final boolean isDisplayMode)
      {
      displayModeEditButton.setVisible(isDisplayMode);
      editModeEditButton.setVisible(!isDisplayMode);

      sensorTypeLabel.setVisible(isDisplayMode);
      sensorTypeComboBox.setVisible(!isDisplayMode);
      sensorPortNumberValueLabel.setVisible(isDisplayMode);
      sensorPortNumberValueComboBox.setVisible(!isDisplayMode);
      sensorThresholdPercentageSlider.setEnabled(!isDisplayMode);
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
