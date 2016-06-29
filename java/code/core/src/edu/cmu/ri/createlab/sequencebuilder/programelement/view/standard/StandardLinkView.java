package edu.cmu.ri.createlab.sequencebuilder.programelement.view.standard;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.PropertyResourceBundle;
import java.util.Set;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JSlider;
import javax.swing.ListCellRenderer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import edu.cmu.ri.createlab.sequencebuilder.ContainerView;
import edu.cmu.ri.createlab.sequencebuilder.programelement.model.LinkModel;
import edu.cmu.ri.createlab.sequencebuilder.programelement.model.LoopableConditionalModel;
import edu.cmu.ri.createlab.sequencebuilder.programelement.model.ProgramElementModel;
import edu.cmu.ri.createlab.sequencebuilder.programelement.view.ViewConstants;
import edu.cmu.ri.createlab.sequencebuilder.programelement.view.dnd.AlwaysInsertAfterTransferHandler;
import edu.cmu.ri.createlab.sequencebuilder.programelement.view.dnd.AlwaysInsertBeforeTransferHandler;
import edu.cmu.ri.createlab.sequencebuilder.programelement.view.dnd.ProgramElementDestinationTransferHandler;
import edu.cmu.ri.createlab.userinterface.util.ImageUtils;
import edu.cmu.ri.createlab.userinterface.util.SwingUtils;
import edu.cmu.ri.createlab.util.RangeSlider;
import edu.cmu.ri.createlab.visualprogrammer.Sensor;
import edu.cmu.ri.createlab.visualprogrammer.VisualProgrammerDevice;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import sun.awt.image.ImageWatched;

/**
 * Created by Brandon on 6/24/2016.
 */
public class StandardLinkView extends BaseStandardProgramElementView<LinkModel>
   {
   private static final Logger LOG = Logger.getLogger(StandardLinkView.class);

   private static final PropertyResourceBundle RESOURCES = (PropertyResourceBundle)PropertyResourceBundle.getBundle(StandardLinkView.class.getName());

   private final JComboBox sensorComboBox = new JComboBox();
   private final JComboBox sensorPortNumberValueComboBox = new JComboBox();

   private final JComboBox outputComboBox = new JComboBox();
   private final JComboBox outputPortNumberValueComboBox = new JComboBox();

   private final RangeSlider sensorThresholdPercentageSlider = new RangeSlider(0, 100);
   //private final JSlider sensorThresholdPercentageSlider2 = new JSlider(0, 100);

   private final JProgressBar sensorMeter = new JProgressBar(0, 100);
   private final JPanel valuePanelContainer = new JPanel();
   private final JLabel valuePanelForBooleanSensors = new JLabel();
   private final JComponent valuePanelForRangeSensors;
   private final LinkModel linkModel;

   private final VisualProgrammerDevice.SensorListener sensorListener =
         new VisualProgrammerDevice.SensorListener()
            {
            @Override
            public void processSensorRawValue(@NotNull final String sensorServiceTypeId, final int portNumber, @NotNull final Object rawValue)
               {
               final LinkModel.LinkedSensor selectedSensor = linkModel.getSelectedSensor();
               final Sensor sensor = selectedSensor.getSensor();

               // make sure this raw value is something we currently care about
               if (sensorServiceTypeId.equals(sensor.getServiceTypeId()) && portNumber == selectedSensor.getPortNumber())
                  {
                  renderRawSensorValue(sensor, rawValue);
                  }
               }
            };

   private void renderRawSensorValue(@NotNull final Sensor sensor, @Nullable final Object rawValue)
      {
      final Integer percentage = sensor.convertRawValueToPercentage(rawValue);
      if (percentage != null)
         {
         sensorMeter.setValue(percentage);
         }
      }

   public StandardLinkView(@NotNull final ContainerView containerView, @NotNull final LinkModel model)
      {
      super(containerView, model);
      this.linkModel = model;

      // configure the top bar area ------------------------------------------------------------------------------------

      final JPanel topBarPanel = new JPanel();
      final JLabel titleLabel = new JLabel(RESOURCES.getString("title.label"));
      final JLabel sensorLabel = new JLabel(RESOURCES.getString("sensor.label"));

      titleLabel.setIcon(ImageUtils.createImageIcon("/edu/cmu/ri/createlab/sequencebuilder/programelement/view/images/Sensor.png"));

      titleLabel.setName("linkBlockTitle");

      final JButton deleteButton = getDeleteButton();
      final JButton moveUpButton = getMoveUpButton();
      final JButton moveDownButon = getMoveDownButton();

      // Create the sensor config components and their panel

      final JLabel sensorPortNumberLabel = new JLabel(RESOURCES.getString("port-number.label"));
      final JLabel outputPortNumberLabel = new JLabel(RESOURCES.getString("port-number.label"));
      final DefaultComboBoxModel sensorComboBoxModel = new DefaultComboBoxModel();
      final DefaultComboBoxModel outputComboBoxModel = new DefaultComboBoxModel();
      final Collection<Sensor> sensors = linkModel.getVisualProgrammerDevice().getSensors();
      final LinkModel.LinkedSensor currentlySelectedSensor = model.getSelectedSensor();
      final String currentlySelectedOutput = linkModel.getSelectedOutput();

      final JLabel minLabel = new JLabel();
      final JLabel maxLabel = new JLabel();
      minLabel.setText(currentlySelectedSensor.getSensor().getIfBranchValueLabel());
      maxLabel.setText(currentlySelectedSensor.getSensor().getElseBranchValueLabel());

      sensorMeter.setName("sensor_progress");

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

      i = 0;
      int selectedItemIndex2 = 0;
      final Map<String, ComboBoxModel> outputToPortNumberValueComboBoxModel = new HashMap<String, ComboBoxModel>(linkModel.outputs.keySet().size());
      for (final String output : linkModel.outputs.keySet())
         {
         if (output.equals(linkModel.getSelectedOutput()))
            {
            selectedItemIndex2 = i;
            }
         outputComboBoxModel.addElement(output);
         final DefaultComboBoxModel portNumberComboBoxModel = new DefaultComboBoxModel();
         for (int j = 1; j <= ((linkModel.outputs.get(output)).getNumPorts()); j++)
            {
            portNumberComboBoxModel.addElement(j);
            }
         outputToPortNumberValueComboBoxModel.put(output, portNumberComboBoxModel);
         i++;
         }
      outputComboBox.setModel(outputComboBoxModel);

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
                     labelText = ((Sensor)value).getDisplayName();
                     }
                  return new JLabel(labelText);
                  }
               }
      );

      outputComboBox.setRenderer(
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
                     labelText = ((String)value);
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
      outputPortNumberValueComboBox.setRenderer(
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

      sensorComboBox.setCursor(new Cursor(Cursor.HAND_CURSOR));
      sensorPortNumberValueComboBox.setCursor(new Cursor(Cursor.HAND_CURSOR));
      sensorThresholdPercentageSlider.setCursor(new Cursor(Cursor.HAND_CURSOR));
      //sensorThresholdPercentageSlider2.setCursor(new Cursor(Cursor.HAND_CURSOR));

      outputComboBox.setCursor(new Cursor(Cursor.HAND_CURSOR));
      outputPortNumberValueComboBox.setCursor(new Cursor(Cursor.HAND_CURSOR));

      sensorComboBox.setSelectedIndex(selectedItemIndex);
      sensorPortNumberValueComboBox.setModel(sensorToPortNumberValueComboBoxModel.get(currentlySelectedSensor.getSensor()));
      sensorPortNumberValueComboBox.setSelectedIndex(currentlySelectedSensor.getPortNumber());
      outputComboBox.setSelectedIndex(selectedItemIndex2);
      outputPortNumberValueComboBox.setModel(outputToPortNumberValueComboBoxModel.get(currentlySelectedOutput));
      outputPortNumberValueComboBox.setSelectedIndex(linkModel.getSelectedOutputPort());
      //sensorThresholdPercentageSlider.setValue(currentlySelectedSensor.getThresholdUpper());
      sensorThresholdPercentageSlider.setUpperValue(currentlySelectedSensor.getThresholdUpper());
      sensorThresholdPercentageSlider.setValue(currentlySelectedSensor.getThresholdLower());

      //sensorThresholdPercentageSlider2.setValue(currentlySelectedSensor.getThresholdLower());

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
                  int upper = sensorThresholdPercentageSlider.isInverted() ? sensorThresholdPercentageSlider.getValue() : sensorThresholdPercentageSlider.getUpperValue();
                  int lower = sensorThresholdPercentageSlider.isInverted() ? sensorThresholdPercentageSlider.getUpperValue() : sensorThresholdPercentageSlider.getValue();

                  model.setSelectedSensor(new LinkModel.LinkedSensor(sensor,
                                                                     sensorPortNumberValueComboBox.getSelectedIndex(),
                                                                     upper, lower
                  ));
                  }
               }
      );
      outputComboBox.addActionListener(
            new ActionListener()
               {
               @Override
               public void actionPerformed(final ActionEvent actionEvent)
                  {
                  final JComboBox comboBox = (JComboBox)actionEvent.getSource();
                  final String output = (String)(comboBox.getSelectedItem());
                  final ComboBoxModel portNumberComboBoxModel = outputToPortNumberValueComboBoxModel.get(output);
                  outputPortNumberValueComboBox.setModel(portNumberComboBoxModel);
                  model.setSelectedOutput(output, outputPortNumberValueComboBox.getSelectedIndex());
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
                  int upper = sensorThresholdPercentageSlider.isInverted() ? sensorThresholdPercentageSlider.getValue() : sensorThresholdPercentageSlider.getUpperValue();
                  int lower = sensorThresholdPercentageSlider.isInverted() ? sensorThresholdPercentageSlider.getUpperValue() : sensorThresholdPercentageSlider.getValue();
                  model.setSelectedSensor(new LinkModel.LinkedSensor((Sensor)sensorComboBox.getSelectedItem(),
                                                                     port - 1,
                                                                     upper, lower));
                  }
               }
      );
      outputPortNumberValueComboBox.addActionListener(
            new ActionListener()
               {
               @Override
               public void actionPerformed(final ActionEvent actionEvent)
                  {
                  final JComboBox comboBox = (JComboBox)actionEvent.getSource();
                  final Integer port = (Integer)(comboBox.getSelectedItem());

                  model.setSelectedOutput((String)outputComboBox.getSelectedItem(), port - 1);
                  }
               }
      );
      sensorThresholdPercentageSlider.addChangeListener(
            new ChangeListener()
               {
               @Override
               public void stateChanged(final ChangeEvent changeEvent)
                  {
                  final RangeSlider source = (RangeSlider)changeEvent.getSource();
                  if (!source.getValueIsAdjusting())
                     {
                     final int percentageUpper = source.isInverted() ? source.getValue() : source.getUpperValue();
                     final int percentageLower = source.isInverted() ? source.getUpperValue() : source.getValue();
                     model.setSelectedSensor(new LinkModel.LinkedSensor((Sensor)sensorComboBox.getSelectedItem(),
                                                                        sensorPortNumberValueComboBox.getSelectedIndex(),
                                                                        percentageUpper, percentageLower));
                     }
                  }
               }
      );

      // Create the appropriate widgets for rendering the sensor's value
      valuePanelContainer.setBackground(new Color(255, 185, 128));
      valuePanelForRangeSensors = createValuePanelForRangeSensors();
      displayAppropriateValuePanel(currentlySelectedSensor.getSensor());

      setIsSensorConfigDisplayMode(false);

      final JPanel sensorConfigPanel = new JPanel();
      final GroupLayout displaySensorConfigLayout = new GroupLayout(sensorConfigPanel);
      sensorConfigPanel.setLayout(displaySensorConfigLayout);
      final JLabel outputLabel = new JLabel(RESOURCES.getString("output.label"));
      displaySensorConfigLayout.setHorizontalGroup(
            displaySensorConfigLayout.createSequentialGroup()
                  .addComponent(minLabel)
                  .addGap(5, 5, 5)
                  .addGroup(displaySensorConfigLayout.createParallelGroup(GroupLayout.Alignment.CENTER, false)
                                  .addGroup(displaySensorConfigLayout.createSequentialGroup()
                                                  .addComponent(sensorLabel)
                                                  .addComponent(sensorComboBox)
                                  )
                                  .addGroup(displaySensorConfigLayout.createSequentialGroup()
                                                  .addComponent(sensorPortNumberLabel)
                                                  .addComponent(sensorPortNumberValueComboBox)
                                  )
                                  .addGroup(displaySensorConfigLayout.createSequentialGroup()
                                                  .addComponent(outputLabel)
                                                  .addComponent(outputComboBox)
                                  )
                                  .addGroup(displaySensorConfigLayout.createSequentialGroup()
                                                  .addComponent(outputPortNumberLabel)
                                                  .addComponent(outputPortNumberValueComboBox)
                                  )
                                  .addComponent(valuePanelContainer)
                  )
                  .addGap(5, 5, 5)
                  .addComponent(maxLabel)

      );
      displaySensorConfigLayout.setVerticalGroup(
            displaySensorConfigLayout.createSequentialGroup()
                  .addGroup(displaySensorConfigLayout.createParallelGroup(GroupLayout.Alignment.CENTER, false)
                                  .addComponent(sensorLabel, 18, 18, 18)
                                  .addComponent(sensorComboBox, 18, 18, 18)
                  )
                  .addGroup(displaySensorConfigLayout.createParallelGroup(GroupLayout.Alignment.CENTER, false)
                                  .addComponent(sensorPortNumberLabel, 18, 18, 18)
                                  .addComponent(sensorPortNumberValueComboBox, 18, 18, 18)
                  )
                  .addGroup(displaySensorConfigLayout.createParallelGroup(GroupLayout.Alignment.CENTER, false)
                                  .addComponent(outputLabel, 18, 18, 18)
                                  .addComponent(outputComboBox, 18, 18, 18)
                  )
                  .addGroup(displaySensorConfigLayout.createParallelGroup(GroupLayout.Alignment.CENTER, false)
                                  .addComponent(outputPortNumberLabel, 18, 18, 18)
                                  .addComponent(outputPortNumberValueComboBox, 18, 18, 18)
                  )
                  .addGroup(displaySensorConfigLayout.createParallelGroup(GroupLayout.Alignment.CENTER, false)
                                  .addComponent(minLabel)
                                  .addComponent(valuePanelContainer)
                                  .addComponent(maxLabel)

                  )
      );

      final JPanel sensorConfigHolder = new JPanel(new GridBagLayout());
      sensorConfigHolder.setName("sensorPanel");
      sensorConfigPanel.setName("sensorPanel");

      model.addPropertyChangeEventListener(LinkModel.SELECTED_SENSOR_PROPERTY,
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
                                                             final LinkModel.LinkedSensor selectedSensor = (LinkModel.LinkedSensor)event.getNewValue();
                                                             LOG.debug("CHANGE EVENT: new selected sensor is: " + selectedSensor);

                                                             final Sensor sensor = selectedSensor.getSensor();
                                                             minLabel.setText(sensor.getIfBranchValueLabel());
                                                             maxLabel.setText(sensor.getElseBranchValueLabel());

                                                             ActionListener listeners[] = sensorPortNumberValueComboBox.getActionListeners();
                                                             for (ActionListener l : listeners)
                                                                {
                                                                sensorPortNumberValueComboBox.removeActionListener(l);
                                                                }
                                                             sensorPortNumberValueComboBox.setSelectedIndex(selectedSensor.getPortNumber());
                                                             for (ActionListener l : listeners)
                                                                {
                                                                sensorPortNumberValueComboBox.addActionListener(l);
                                                                }

                                                             ActionListener listeners2[] = sensorComboBox.getActionListeners();
                                                             for (ActionListener l : listeners2)
                                                                {
                                                                sensorComboBox.removeActionListener(l);
                                                                }
                                                             sensorComboBox.setSelectedItem(sensor);
                                                             for (ActionListener l : listeners2)
                                                                {
                                                                sensorComboBox.addActionListener(l);
                                                                }

                                                             if (Sensor.ValueType.BOOLEAN.equals(sensor.getValueType()))
                                                                {
                                                                LOG.debug("CHANGE EVENT: need to render the ARROWS!!!!");
                                                                }
                                                             else
                                                                {
                                                                LOG.debug("CHANGE EVENT: need to render the RANGE!!!!");
                                                                }

                                                             displayAppropriateValuePanel(sensor);
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

      c.gridx = 2;
      c.gridy = 0;
      c.gridwidth = 1;
      c.gridheight = 1;
      c.weightx = 0.0;
      c.weighty = 0.0;
      c.anchor = GridBagConstraints.FIRST_LINE_END;
      c.fill = GridBagConstraints.NONE;
      topBarPanel.add(deleteButton, c);

      c.gridx = 2;
      c.gridy = 1;
      c.gridwidth = 1;
      c.gridheight = 1;
      c.weightx = 0.0;
      c.weighty = 0.0;
      c.anchor = GridBagConstraints.FIRST_LINE_END;
      c.fill = GridBagConstraints.NONE;
      topBarPanel.add(moveUpButton, c);

      c.gridx = 2;
      c.gridy = 2;
      c.gridwidth = 1;
      c.gridheight = 1;
      c.weightx = 0.0;
      c.weighty = 0.0;
      c.anchor = GridBagConstraints.FIRST_LINE_END;
      c.fill = GridBagConstraints.NONE;
      topBarPanel.add(moveDownButon, c);

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

      topBarPanel.setTransferHandler(new AlwaysInsertBeforeTransferHandler(StandardLinkView.this, containerView));
      // configure the bottom bar area ---------------------------------------------------------------------------------

      final JPanel bottomBarPanel = new JPanel();
      bottomBarPanel.setLayout(new GridBagLayout());

      bottomBarPanel.setBackground(ViewConstants.Colors.LOOP_ELEMENT_BACKGROUND_COLOR);

      bottomBarPanel.setTransferHandler(new AlwaysInsertAfterTransferHandler(StandardLinkView.this, containerView));

      // assemble the content panel -----------------------------------------------------------------------------------

      final JPanel contentPanel = getContentPanel();
      final GroupLayout contentPanelLayout = new GroupLayout(contentPanel);
      contentPanel.setLayout(contentPanelLayout);

      contentPanelLayout.setHorizontalGroup(
            contentPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
                  .addComponent(topBarPanel)
                  .addComponent(bottomBarPanel)
      );
      contentPanelLayout.setVerticalGroup(
            contentPanelLayout.createSequentialGroup()
                  .addComponent(topBarPanel)
                  .addComponent(bottomBarPanel)
      );
      //Background color dependent on container type (same as loop element)
      final String panelStyle = containerView.hasParentProgramElementView() ? "loopElementLoop" : "loopElement";
      contentPanel.setName(panelStyle);

      setTransferHandler(
            new ProgramElementDestinationTransferHandler(true, containerView.getParentProgramElementView())
               {
               @Override
               public Set<DataFlavor> getSupportedDataFlavors()
                  {
                  return containerView.getSupportedDataFlavors();
                  }

               @Override
               protected final void showInsertLocation(final Point dropPoint)
                  {
                  StandardLinkView.this.showInsertLocation(dropPoint);
                  }

               @Override
               protected final void performImport(@NotNull final ProgramElementModel model, @NotNull final Point dropPoint)
                  {
                  getContainerView().handleDropOfModelOntoView(model,
                                                               StandardLinkView.this,
                                                               isInsertLocationBefore(dropPoint));
                  }
               });

      LOG.debug("StandardLinkView.Constructor()");
      }

   private void displayAppropriateValuePanel(final Sensor sensor)
      {
      valuePanelContainer.removeAll();
      if (Sensor.ValueType.BOOLEAN.equals(sensor.getValueType()))
         {
         valuePanelContainer.add(valuePanelForBooleanSensors);
         }
      else
         {
         valuePanelContainer.add(valuePanelForRangeSensors);
         }
      }

   private JComponent createValuePanelForRangeSensors()
      {
      //Builds the layeredpane that combines the meter and the slider

      final JPanel holder = new JPanel();
      final JLayeredPane layers = new JLayeredPane();

      layers.setPreferredSize(new Dimension(198, 22));
      layers.setMaximumSize(new Dimension(198, 22));
      layers.setMinimumSize(new Dimension(198, 22));

      JButton invertButton = new JButton("Swap");
      invertButton.addMouseListener(new MouseListener()
         {
         @Override
         public void mouseClicked(final MouseEvent e)
            {
            LOG.debug("Inverted Slider");
            sensorThresholdPercentageSlider.invert();
            sensorThresholdPercentageSlider.updateUI();
            int upper = sensorThresholdPercentageSlider.isInverted() ? sensorThresholdPercentageSlider.getValue() : sensorThresholdPercentageSlider.getUpperValue();
            int lower = sensorThresholdPercentageSlider.isInverted() ? sensorThresholdPercentageSlider.getUpperValue() : sensorThresholdPercentageSlider.getValue();
            linkModel.setSelectedSensor(new LinkModel.LinkedSensor((Sensor)sensorComboBox.getSelectedItem(),
                                                               sensorPortNumberValueComboBox.getSelectedIndex(),
                                                               upper, lower));
            }

         @Override
         public void mousePressed(final MouseEvent e)
            {

            }

         @Override
         public void mouseReleased(final MouseEvent e)
            {

            }

         @Override
         public void mouseEntered(final MouseEvent e)
            {

            }

         @Override
         public void mouseExited(final MouseEvent e)
            {

            }
         });
      holder.add(layers);
      holder.add(invertButton);

      sensorMeter.setBounds(0, 0, 196, 22);
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
      sensorComboBox.setVisible(!isDisplayMode);
      sensorPortNumberValueComboBox.setVisible(!isDisplayMode);
      sensorThresholdPercentageSlider.setEnabled(!isDisplayMode);
      }

   @Override
   public void handleAdditionToContainer()
      {
      linkModel.getVisualProgrammerDevice().addSensorListener(sensorListener);
      }

   @Override
   public void handleRemovalFromContainer()
      {
      linkModel.getVisualProgrammerDevice().removeSensorListener(sensorListener);
      }

   @Override
   protected void hideInsertLocationsOfContainedViews()
      {

      }

   @Override
   public void resetViewForSequenceExecution()
      {

      }
   }
