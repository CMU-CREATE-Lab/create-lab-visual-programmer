package edu.cmu.ri.createlab.sequencebuilder.programelement.view.standard;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.HashMap;
import java.util.Map;
import java.util.PropertyResourceBundle;
import java.util.SortedSet;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import edu.cmu.ri.createlab.sequencebuilder.ContainerView;
import edu.cmu.ri.createlab.sequencebuilder.programelement.model.LoopableConditionalModel;
import edu.cmu.ri.createlab.sequencebuilder.programelement.model.ProgramElementModel;
import edu.cmu.ri.createlab.sequencebuilder.programelement.view.ViewConstants;
import edu.cmu.ri.createlab.sequencebuilder.programelement.view.dnd.AlwaysInsertAfterTransferHandler;
import edu.cmu.ri.createlab.sequencebuilder.programelement.view.dnd.AlwaysInsertBeforeTransferHandler;
import edu.cmu.ri.createlab.sequencebuilder.programelement.view.dnd.ProgramElementDestinationTransferHandler;
import edu.cmu.ri.createlab.userinterface.util.ImageUtils;
import edu.cmu.ri.createlab.userinterface.util.SwingUtils;
import edu.cmu.ri.createlab.visualprogrammer.Sensor;
import edu.cmu.ri.createlab.visualprogrammer.VisualProgrammerDevice;
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

   private final JButton displayModeEditButton = new JButton(ImageUtils.createImageIcon("/edu/cmu/ri/createlab/sequencebuilder/programelement/view/images/smallLock.png"));
   private final JButton editModeEditButton = new JButton(ImageUtils.createImageIcon("/edu/cmu/ri/createlab/sequencebuilder/programelement/view/images/smallUnlock.png"));
   private final JLabel sensorLabel = new JLabel("");
   private final JComboBox sensorComboBox = new JComboBox();
   private final JLabel sensorPortNumberValueLabel = new JLabel("");
   private final JComboBox sensorPortNumberValueComboBox = new JComboBox();
   private final JSlider sensorThresholdPercentageSlider = new JSlider(0, 100);
   private final JProgressBar sensorMeter = new JProgressBar(0, 100);
   private final JPanel sensorSliderMeterCombo;
   private final LoopableConditionalModel loopableConditionalModel;
   private final ContainerView ifBranchLoopContainerView;
   private final ContainerView elseBranchLoopContainerView;

   private final ImageIcon greenArrow = ImageUtils.createImageIcon("/edu/cmu/ri/createlab/sequencebuilder/programelement/view/images/greenArrow.png");
   private final ImageIcon wideOrangeArrow = ImageUtils.createImageIcon("/edu/cmu/ri/createlab/sequencebuilder/programelement/view/images/wideOrangeArrow.png");
   private final Border arrowBorder = BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(0, 128, 0), 3), BorderFactory.createMatteBorder(16, 0, 0, 0, greenArrow));
   private final Border selectedBorder = BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.BLACK, 1), arrowBorder);
   private final Border orangeArrowBorder = BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(3,3,3,3), BorderFactory.createMatteBorder(16, 0, 0, 0, wideOrangeArrow));
   private final Border unselectedBorder = BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.BLACK, 1),orangeArrowBorder);

   final JComponent ifBranchContainerViewPanel;
   final JComponent elseBranchContainerViewPanel;

   private final VisualProgrammerDevice.SensorListener sensorListener =
         new VisualProgrammerDevice.SensorListener()
         {
         @Override
         public void processSensorRawValue(@NotNull final String sensorServiceTypeId, final int portNumber, @NotNull final Integer rawValue)
            {
            final LoopableConditionalModel.SelectedSensor selectedSensor = loopableConditionalModel.getSelectedSensor();
            final Sensor sensor = selectedSensor.getSensor();

            // make sure this raw value is something we currently care about
            if (sensorServiceTypeId.equals(sensor.getServiceTypeId()) && portNumber == selectedSensor.getPortNumber())
               {
               final Integer percentage = sensor.convertRawValueToPercentage(rawValue);
               if (percentage != null)
                  {
                  SwingUtilities.invokeLater(
                        new Runnable()
                        {
                        @Override
                        public void run()
                           {
                           sensorMeter.setValue(percentage);
                           }
                        }
                  );
                  }
               }
            }
         };

   public StandardLoopableConditionalView(@NotNull final ContainerView containerView, @NotNull final LoopableConditionalModel model)
      {
      super(containerView, model);
      this.loopableConditionalModel = model;

      ifBranchLoopContainerView = new ContainerView(containerView.getJFrame(), model.getIfBranchContainerModel(), new StandardViewFactory(), this);
      elseBranchLoopContainerView = new ContainerView(containerView.getJFrame(), model.getElseBranchContainerModel(), new StandardViewFactory(), this);

      // need this for case where the model was loaded from XML, so the contained views haven't been created yet--this forces their creation and display
      ifBranchLoopContainerView.refresh();
      elseBranchLoopContainerView.refresh();

      // configure the top bar area ------------------------------------------------------------------------------------

      final JPanel topBarPanel = new JPanel();
      final JLabel titleLabel = new JLabel(RESOURCES.getString("title.label"));
      titleLabel.setIcon(ImageUtils.createImageIcon("/edu/cmu/ri/createlab/sequencebuilder/programelement/view/images/Sensor.png"));

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
      final DefaultComboBoxModel sensorComboBoxModel = new DefaultComboBoxModel();
      final SortedSet<Sensor> sensors = model.getVisualProgrammerDevice().getSensors();
      final LoopableConditionalModel.SelectedSensor currentlySelectedSensor = model.getSelectedSensor();

      sensorMeter.setName("sensor_progress");

      // set current values
      ifBranchValueLabel.setText(currentlySelectedSensor.getSensor().getIfBranchValueLabel());
      elseBranchValueLabel.setText(currentlySelectedSensor.getSensor().getElseBranchValueLabel());
      sensorLabel.setText(currentlySelectedSensor.getSensor().getName());
      sensorPortNumberValueLabel.setText(String.valueOf(currentlySelectedSensor.getPortNumber() + 1));

      int i = 0;
      int selectedItemIndex = 0;
      final Map<Sensor, ComboBoxModel> sensorToPortNumberValueComboBoxModel = new HashMap<Sensor, ComboBoxModel>(sensors.size());
      for (final Sensor sensor : sensors)
         {
         if (sensor.equals(currentlySelectedSensor.getSensor()))
            {
            selectedItemIndex = i;
            }
         sensorComboBoxModel.addElement(sensor);
         final DefaultComboBoxModel portNumberComboBoxModel = new DefaultComboBoxModel();
         for (int j = 1; j <= sensor.getNumPorts(); j++)
            {
            portNumberComboBoxModel.addElement(j);
            }
         sensorToPortNumberValueComboBoxModel.put(sensor, portNumberComboBoxModel);
         i++;
         }
      sensorComboBox.setModel(sensorComboBoxModel);
      sensorComboBox.setRenderer(
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
                  labelText = ((Sensor)value).getName();
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
      sensorComboBox.setSelectedIndex(selectedItemIndex);
      sensorPortNumberValueComboBox.setModel(sensorToPortNumberValueComboBoxModel.get(currentlySelectedSensor.getSensor()));
      sensorPortNumberValueComboBox.setSelectedIndex(currentlySelectedSensor.getPortNumber());
      sensorThresholdPercentageSlider.setValue(currentlySelectedSensor.getThresholdPercentage());

      sensorComboBox.addActionListener(
            new ActionListener()
            {
            @Override
            public void actionPerformed(final ActionEvent actionEvent)
               {
               final JComboBox comboBox = (JComboBox)actionEvent.getSource();
               final Sensor sensor = (Sensor)(comboBox.getSelectedItem());
               final ComboBoxModel portNumberComboBoxModel = sensorToPortNumberValueComboBoxModel.get(sensor);
               sensorPortNumberValueComboBox.setModel(portNumberComboBoxModel);

               model.setSelectedSensor(new LoopableConditionalModel.SelectedSensor(sensor,
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

               model.setSelectedSensor(new LoopableConditionalModel.SelectedSensor((Sensor)sensorComboBox.getSelectedItem(),
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
                  model.setSelectedSensor(new LoopableConditionalModel.SelectedSensor((Sensor)sensorComboBox.getSelectedItem(),
                                                                                      sensorPortNumberValueComboBox.getSelectedIndex(),
                                                                                      percentage));
                  }
               }
            }
      );

     /* final FocusListener modeFocusListener =
            new FocusListener()
            {
            @Override
            public void focusGained(final FocusEvent e)
               {
               setIsSensorConfigDisplayMode(false);
               }

            @Override
            public void focusLost(final FocusEvent e)
               {
               if (!(sensorComboBox.hasFocus() || sensorPortNumberValueComboBox.hasFocus() || sensorThresholdPercentageSlider.hasFocus()))
                  {
                  setIsSensorConfigDisplayMode(true);
                  }
               }
            };

      sensorComboBox.addFocusListener(modeFocusListener);
      sensorPortNumberValueComboBox.addFocusListener(modeFocusListener);
      sensorThresholdPercentageSlider.addFocusListener(modeFocusListener);*/

      sensorSliderMeterCombo = createLayeredSliderProgressBar();

      setIsSensorConfigDisplayMode(false);

      final JPanel sensorConfigPanel = new JPanel();
      final GroupLayout displaySensorConfigLayout = new GroupLayout(sensorConfigPanel);
      sensorConfigPanel.setLayout(displaySensorConfigLayout);
      displaySensorConfigLayout.setHorizontalGroup(
            displaySensorConfigLayout.createSequentialGroup()
                  .addComponent(ifBranchValueLabel)
                  .addGap(5, 5, 5)
                  .addGroup(displaySensorConfigLayout.createParallelGroup(GroupLayout.Alignment.CENTER, false)
                                  .addGroup(displaySensorConfigLayout.createSequentialGroup()
                                                  .addComponent(sensorLabel)
                                                  .addComponent(sensorComboBox)
                                  )
                                  .addGroup(displaySensorConfigLayout.createSequentialGroup()
                                                  .addComponent(sensorPortNumberLabel)
                                                  .addComponent(sensorPortNumberValueLabel)
                                                  .addComponent(sensorPortNumberValueComboBox)
                                  )
                                  .addComponent(sensorSliderMeterCombo)
                  )
                  .addGap(5, 5, 5)
                  .addComponent(elseBranchValueLabel)
      );
      displaySensorConfigLayout.setVerticalGroup(
            displaySensorConfigLayout.createSequentialGroup()
                  .addGroup(displaySensorConfigLayout.createParallelGroup(GroupLayout.Alignment.CENTER, false)
                                  .addComponent(sensorLabel, 18, 18, 18)
                                  .addComponent(sensorComboBox, 18, 18, 18)
                  )
                  .addGroup(displaySensorConfigLayout.createParallelGroup(GroupLayout.Alignment.CENTER, false)
                                  .addComponent(sensorPortNumberLabel, 18, 18, 18)
                                  .addComponent(sensorPortNumberValueLabel, 18, 18, 18)
                                  .addComponent(sensorPortNumberValueComboBox, 18, 18, 18)
                  )
                  .addGroup(displaySensorConfigLayout.createParallelGroup(GroupLayout.Alignment.CENTER, false)
                                  .addComponent(ifBranchValueLabel)
                                  .addComponent(sensorSliderMeterCombo)
                                  .addComponent(elseBranchValueLabel)
                  )
      );

      final JPanel sensorConfigHolder = new JPanel(new GridBagLayout());
      sensorConfigHolder.setName("sensorPanel");
      sensorConfigPanel.setName("sensorPanel");

      displayModeEditButton.addActionListener(
            new ActionListener()
            {
            @Override
            public void actionPerformed(final ActionEvent actionEvent)
               {
               setIsSensorConfigDisplayMode(false);
               sensorComboBox.requestFocusInWindow();
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

                                                       ifBranchValueLabel.setText(selectedSensor.getSensor().getIfBranchValueLabel());
                                                       elseBranchValueLabel.setText(selectedSensor.getSensor().getElseBranchValueLabel());
                                                       sensorLabel.setText(selectedSensor.getSensor().getName());
                                                       sensorPortNumberValueLabel.setText(String.valueOf(selectedSensor.getPortNumber() + 1));
                                                       }
                                                    }
                                              );
                                              }
                                           });

      model.addExecutionEventListener(
            new LoopableConditionalModel.ExecutionEventListener()
            {
            @Override
            public void handleExecutionStart()
               {
               LOG.debug("StandardLoopableConditionalView.handleExecutionStart()");
               resetHighlightContainers();
               }

            @Override
            public void handleExecutionEnd()
               {
               LOG.debug("StandardLoopableConditionalView.handleExecutionEnd()");
               resetHighlightContainers();
               }
            @Override
            public void handleIfBranchHighlight()
                {
                highlightIfContainer();
                }

            @Override
            public void handleElseBranchHighlight()
                {
                highlightElseContainer();
                }

            @Override
            public void handleResetBranchHightlight()
                {
                resetHighlightContainers();
                }

            }

      );

      topBarPanel.setLayout(new GridBagLayout());
      final GridBagConstraints c = new GridBagConstraints();

      c.gridx = 0;
      c.gridy = 0;
      c.gridwidth = 1;
      c.gridheight = 1;
      c.weightx = 1.0;
      c.weighty = 1.0;
      c.anchor = GridBagConstraints.CENTER;
      c.fill = GridBagConstraints.NONE;
      sensorConfigHolder.add(sensorConfigPanel, c);

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
      c.insets = new Insets(4, 0, 0, 0);
      topBarPanel.add(sensorConfigHolder, c);

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
               final boolean alwaysReevaluate = StandardLoopableConditionalView.this.getProgramElementModel().willReevaluateConditionAfterElseBranchCompletes() && StandardLoopableConditionalView.this.getProgramElementModel().willReevaluateConditionAfterIfBranchCompletes();
               setSpacerArrowVisible(!alwaysReevaluate);
               }
            };
      final JToggleButton elseBranchLoopToggleButton =
            new LoopToggleButton(StandardLoopableConditionalView.this.getProgramElementModel().willReevaluateConditionAfterElseBranchCompletes())
            {
            @Override
            protected void updateWillReevaluateConditional(final boolean willReevaluateConditional)
               {
               StandardLoopableConditionalView.this.getProgramElementModel().setWillReevaluateConditionAfterElseBranchCompletes(willReevaluateConditional);
               final boolean alwaysReevaluate = StandardLoopableConditionalView.this.getProgramElementModel().willReevaluateConditionAfterElseBranchCompletes() && StandardLoopableConditionalView.this.getProgramElementModel().willReevaluateConditionAfterIfBranchCompletes();
               setSpacerArrowVisible(!alwaysReevaluate);
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
      c.insets = new Insets(4,0,0,0);
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

      ifBranchContainerViewPanel = ifBranchLoopContainerView.getComponent();
      ifBranchContainerViewPanel.setMinimumSize(PREFERRED_CONTAINER_DIMENSION);
      ifBranchContainerViewPanel.setMaximumSize(new Dimension(PREFERRED_CONTAINER_DIMENSION.width, ifBranchContainerViewPanel.getMaximumSize().height));

      elseBranchContainerViewPanel = elseBranchLoopContainerView.getComponent();
      elseBranchContainerViewPanel.setMinimumSize(PREFERRED_CONTAINER_DIMENSION);
      elseBranchContainerViewPanel.setMaximumSize(new Dimension(PREFERRED_CONTAINER_DIMENSION.width, elseBranchContainerViewPanel.getMaximumSize().height));

      ifBranchContainerViewPanel.setName("loopFrame");
      elseBranchContainerViewPanel.setName("loopFrame");



      ifBranchContainerViewPanel.setBorder(unselectedBorder);
      elseBranchContainerViewPanel.setBorder(unselectedBorder);

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

       setTransferHandler(
            new ProgramElementDestinationTransferHandler()
            {
            @Override
            protected final void showInsertLocation(final Point dropPoint)
               {
               StandardLoopableConditionalView.this.showInsertLocation(dropPoint);
               }

            @Override
            protected final void performImport(@NotNull final ProgramElementModel model, @NotNull final Point dropPoint)
               {
               getContainerView().handleDropOfModelOntoView(model,
                                                            StandardLoopableConditionalView.this,
                                                            isInsertLocationBefore(dropPoint));
               }
      });

      LOG.debug("StandardLoopableConditionalView.StandardLoopableConditionalView()");
      }

   private JPanel createLayeredSliderProgressBar()
      {
      //Builds the layeredpane that combines the meter and the slider
      final JPanel holder = new JPanel();
      final JLayeredPane layers = new JLayeredPane();

      layers.setPreferredSize(new Dimension(198, 21));
      layers.setMaximumSize(new Dimension(198, 21));
      layers.setMinimumSize(new Dimension(198, 21));

      holder.add(layers);

      sensorMeter.setBounds(0, 0, 196, 21);
      sensorThresholdPercentageSlider.setBounds(0, 0, 196, 14);
      layers.add(sensorMeter, new Integer(1));
      layers.add(sensorThresholdPercentageSlider, new Integer(2));

      sensorThresholdPercentageSlider.setPaintTrack(false);

      holder.setName("sensorPanel");
      layers.setName("sensorPanel");

      return holder;
      }

   private void setIsSensorConfigDisplayMode(final boolean isDisplayMode)
      {
      displayModeEditButton.setVisible(isDisplayMode);
      editModeEditButton.setVisible(!isDisplayMode);

      sensorLabel.setVisible(isDisplayMode);
      sensorComboBox.setVisible(!isDisplayMode);
      sensorPortNumberValueLabel.setVisible(isDisplayMode);
      sensorPortNumberValueComboBox.setVisible(!isDisplayMode);
      sensorThresholdPercentageSlider.setEnabled(!isDisplayMode);
      }

   @Override
   protected void hideInsertLocationsOfContainedViews()
      {
      ifBranchLoopContainerView.hideInsertLocationsOfContainedViews();
      elseBranchLoopContainerView.hideInsertLocationsOfContainedViews();
      }

   @Override
   public final void resetViewForSequenceExecution()
      {
      ifBranchLoopContainerView.resetContainedViewsForSequenceExecution();
      elseBranchLoopContainerView.resetContainedViewsForSequenceExecution();
      resetHighlightContainers();
      }

   @Override
   public void handleAdditionToContainer()
      {
      loopableConditionalModel.getVisualProgrammerDevice().addSensorListener(sensorListener);
      }

   @Override
   public void handleRemovalFromContainer()
      {
      loopableConditionalModel.getVisualProgrammerDevice().removeSensorListener(sensorListener);
      }

   public void highlightIfContainer()
   {
       ifBranchContainerViewPanel.setBorder(selectedBorder);
       elseBranchContainerViewPanel.setBorder(unselectedBorder);
   }

   public void highlightElseContainer()
   {
       ifBranchContainerViewPanel.setBorder(unselectedBorder);
       elseBranchContainerViewPanel.setBorder(selectedBorder);
   }

   public void resetHighlightContainers()
   {
       ifBranchContainerViewPanel.setBorder(unselectedBorder);
       elseBranchContainerViewPanel.setBorder(unselectedBorder);
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
