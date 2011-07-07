package edu.cmu.ri.createlab.expressionbuilder.controlpanel.services.accelerometer;

import java.awt.Color;
import java.awt.Component;
import java.text.DecimalFormat;
import java.util.Map;
import java.util.PropertyResourceBundle;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.GroupLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import edu.cmu.ri.createlab.expressionbuilder.controlpanel.AbstractServiceControlPanel;
import edu.cmu.ri.createlab.expressionbuilder.controlpanel.AbstractServiceControlPanelDevice;
import edu.cmu.ri.createlab.expressionbuilder.controlpanel.ControlPanelManager;
import edu.cmu.ri.createlab.expressionbuilder.controlpanel.ServiceControlPanelDevice;
import edu.cmu.ri.createlab.terk.expression.XmlParameter;
import edu.cmu.ri.createlab.terk.services.Service;
import edu.cmu.ri.createlab.terk.services.accelerometer.AccelerometerGs;
import edu.cmu.ri.createlab.terk.services.accelerometer.AccelerometerService;
import edu.cmu.ri.createlab.terk.services.accelerometer.AccelerometerState;
import edu.cmu.ri.createlab.userinterface.GUIConstants;
import edu.cmu.ri.createlab.userinterface.component.DatasetPlotter;
import edu.cmu.ri.createlab.userinterface.util.SwingUtils;
import org.apache.log4j.Logger;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public final class AccelerometerServiceControlPanel extends AbstractServiceControlPanel
   {
   private static final Logger LOG = Logger.getLogger(AccelerometerServiceControlPanel.class);

   private static final PropertyResourceBundle RESOURCES = (PropertyResourceBundle)PropertyResourceBundle.getBundle(AccelerometerServiceControlPanel.class.getName());

   private final AccelerometerService service;

   public AccelerometerServiceControlPanel(final ControlPanelManager controlPanelManager, final AccelerometerService service)
      {
      super(controlPanelManager, service);
      this.service = service;

      final Timer pollingTimer = new Timer("AccelerometerPollingTimer", true);
      pollingTimer.scheduleAtFixedRate(
            new TimerTask()
            {
            public void run()
               {
               updateAccelerometers();
               }
            },
            0,
            500);
      }

   private void updateAccelerometers()
      {
      // todo: this is inefficient if there is more than one device.  It'd be better to fetch all the states
      // at once with a single call to the service
      for (int i = 0; i < service.getDeviceCount(); i++)
         {
         final ServiceControlPanelDevice device = getDeviceById(i);
         if (device != null && device.isActive())
            {
            try
               {
               final AccelerometerState state = service.getAccelerometerState(i);
               ((ControlPanelDevice)device).setValue(state);
               }
            catch (Exception e)
               {
               LOG.error("Exception while trying to poll accelerometer input [" + i + "]", e);
               }
            }
         }
      }

   public String getDisplayName()
      {
      return RESOURCES.getString("control-panel.title");
      }

   public String getShortDisplayName()
      {
      return RESOURCES.getString("control-panel.short-title");
      }

   public void refresh()
      {
      LOG.debug("AccelerometerServiceControlPanel.refresh()");

      updateAccelerometers();
      }

   protected ServiceControlPanelDevice createServiceControlPanelDevice(final Service service, final int deviceIndex)
      {
      return new ControlPanelDevice(deviceIndex);
      }

   private final class ControlPanelDevice extends AbstractServiceControlPanelDevice
      {
      // todo: get these min and max values from elsewhere
      private static final int ACTUAL_MIN_VALUE = 0;
      private static final int ACTUAL_MAX_VALUE = 255;
      private static final int DISPLAY_MIN_VALUE = 0;
      private static final int DISPLAY_MAX_VALUE = 255;
      private static final String DISPLAY_INITIAL_VALUE = "0";
      private static final int NUM_CHARS_IN_TEXT_FIELDS = 5;

      private final JPanel panel = new JPanel();
      private final JTextField xTextFieldRaw = new JTextField(DISPLAY_INITIAL_VALUE, NUM_CHARS_IN_TEXT_FIELDS);
      private final JTextField yTextFieldRaw = new JTextField(DISPLAY_INITIAL_VALUE, NUM_CHARS_IN_TEXT_FIELDS);
      private final JTextField zTextFieldRaw = new JTextField(DISPLAY_INITIAL_VALUE, NUM_CHARS_IN_TEXT_FIELDS);
      private final JTextField xTextFieldG = new JTextField(DISPLAY_INITIAL_VALUE, NUM_CHARS_IN_TEXT_FIELDS);
      private final JTextField yTextFieldG = new JTextField(DISPLAY_INITIAL_VALUE, NUM_CHARS_IN_TEXT_FIELDS);
      private final JTextField zTextFieldG = new JTextField(DISPLAY_INITIAL_VALUE, NUM_CHARS_IN_TEXT_FIELDS);
      private final DatasetPlotter<Integer> xPlot = new DatasetPlotter<Integer>(ACTUAL_MIN_VALUE, ACTUAL_MAX_VALUE, 60, 60, 100, TimeUnit.MILLISECONDS);
      private final DatasetPlotter<Integer> yPlot = new DatasetPlotter<Integer>(ACTUAL_MIN_VALUE, ACTUAL_MAX_VALUE, 60, 60, 100, TimeUnit.MILLISECONDS);
      private final DatasetPlotter<Integer> zPlot = new DatasetPlotter<Integer>(ACTUAL_MIN_VALUE, ACTUAL_MAX_VALUE, 60, 60, 100, TimeUnit.MILLISECONDS);
      private final DecimalFormat decimalFormat = new DecimalFormat("#.##");

      private ControlPanelDevice(final int deviceIndex)
         {
         super(deviceIndex);

         xPlot.addDataset(Color.RED);
         yPlot.addDataset(Color.GREEN);
         zPlot.addDataset(Color.BLUE);

         xTextFieldRaw.setFont(GUIConstants.FONT_NORMAL);
         xTextFieldRaw.setEditable(false);
         xTextFieldRaw.setMaximumSize(xTextFieldRaw.getPreferredSize());
         xTextFieldRaw.setMinimumSize(xTextFieldRaw.getPreferredSize());

         yTextFieldRaw.setFont(GUIConstants.FONT_NORMAL);
         yTextFieldRaw.setEditable(false);
         yTextFieldRaw.setMaximumSize(yTextFieldRaw.getPreferredSize());
         yTextFieldRaw.setMinimumSize(yTextFieldRaw.getPreferredSize());

         zTextFieldRaw.setFont(GUIConstants.FONT_NORMAL);
         zTextFieldRaw.setEditable(false);
         zTextFieldRaw.setMaximumSize(zTextFieldRaw.getPreferredSize());
         zTextFieldRaw.setMinimumSize(zTextFieldRaw.getPreferredSize());

         xTextFieldG.setFont(GUIConstants.FONT_NORMAL);
         xTextFieldG.setEditable(false);
         xTextFieldG.setMaximumSize(xTextFieldG.getPreferredSize());
         xTextFieldG.setMinimumSize(xTextFieldG.getPreferredSize());

         yTextFieldG.setFont(GUIConstants.FONT_NORMAL);
         yTextFieldG.setEditable(false);
         yTextFieldG.setMaximumSize(yTextFieldG.getPreferredSize());
         yTextFieldG.setMinimumSize(yTextFieldG.getPreferredSize());

         zTextFieldG.setFont(GUIConstants.FONT_NORMAL);
         zTextFieldG.setEditable(false);
         zTextFieldG.setMaximumSize(zTextFieldG.getPreferredSize());
         zTextFieldG.setMinimumSize(zTextFieldG.getPreferredSize());

         final Component topLeftCorner = SwingUtils.createRigidSpacer();
         final Component bottomLeftCorner = SwingUtils.createRigidSpacer();
         final Component padding1 = SwingUtils.createRigidSpacer();
         final Component padding2 = SwingUtils.createRigidSpacer();
         final Component padding3 = SwingUtils.createRigidSpacer();
         final Component padding4 = SwingUtils.createRigidSpacer();
         final JLabel xLabel = SwingUtils.createLabel(RESOURCES.getString("label.x"));
         final JLabel yLabel = SwingUtils.createLabel(RESOURCES.getString("label.y"));
         final JLabel zLabel = SwingUtils.createLabel(RESOURCES.getString("label.z"));
         final JLabel rawLabel = SwingUtils.createLabel(RESOURCES.getString("label.raw"));
         final JLabel gLabel = SwingUtils.createLabel(RESOURCES.getString("label.g"));
         final Component xPlotComponent = xPlot.getComponent();
         final Component yPlotComponent = yPlot.getComponent();
         final Component zPlotComponent = zPlot.getComponent();

         final JPanel borderedPanel = new JPanel();
         final GroupLayout groupLayout = new GroupLayout(borderedPanel);
         borderedPanel.setLayout(groupLayout);
         borderedPanel.setBackground(Color.WHITE);
         borderedPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.GRAY, 1),
                                                                    BorderFactory.createEmptyBorder(5, 5, 5, 5)));

         groupLayout.setHorizontalGroup(
               groupLayout.createSequentialGroup()
                     .addGroup(groupLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                     .addComponent(topLeftCorner)
                                     .addComponent(rawLabel)
                                     .addComponent(gLabel)
                                     .addComponent(bottomLeftCorner)
                     )
                     .addGroup(groupLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
                                     .addComponent(padding1)
                                     .addComponent(padding2)
                                     .addComponent(padding3)
                                     .addComponent(padding4)
                     )
                     .addGroup(groupLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
                                     .addComponent(xLabel)
                                     .addComponent(xTextFieldRaw)
                                     .addComponent(xTextFieldG)
                                     .addComponent(xPlotComponent)
                     )
                     .addGroup(groupLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
                                     .addComponent(yLabel)
                                     .addComponent(yTextFieldRaw)
                                     .addComponent(yTextFieldG)
                                     .addComponent(yPlotComponent)
                     )
                     .addGroup(groupLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
                                     .addComponent(zLabel)
                                     .addComponent(zTextFieldRaw)
                                     .addComponent(zTextFieldG)
                                     .addComponent(zPlotComponent)
                     )
         );
         groupLayout.setVerticalGroup(
               groupLayout.createSequentialGroup()
                     .addGroup(groupLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
                                     .addComponent(topLeftCorner)
                                     .addComponent(padding1)
                                     .addComponent(xLabel)
                                     .addComponent(yLabel)
                                     .addComponent(zLabel)
                     )
                     .addGroup(groupLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
                                     .addComponent(rawLabel)
                                     .addComponent(padding2)
                                     .addComponent(xTextFieldRaw)
                                     .addComponent(yTextFieldRaw)
                                     .addComponent(zTextFieldRaw)
                     )
                     .addGroup(groupLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
                                     .addComponent(gLabel)
                                     .addComponent(padding3)
                                     .addComponent(xTextFieldG)
                                     .addComponent(yTextFieldG)
                                     .addComponent(zTextFieldG)
                     )
                     .addGroup(groupLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
                                     .addComponent(bottomLeftCorner)
                                     .addComponent(padding4)
                                     .addComponent(xPlotComponent)
                                     .addComponent(yPlotComponent)
                                     .addComponent(zPlotComponent)
                     )
         );

         panel.setBackground(Color.WHITE);
         panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
         panel.add(SwingUtils.createLabel(String.valueOf(deviceIndex + 1)));
         panel.add(SwingUtils.createRigidSpacer());
         panel.add(borderedPanel);
         }

      private void setValue(final AccelerometerState state)
         {
         final AccelerometerGs gs = service.isUnitConversionSupported() ? service.convertToGs(state) : null;

         xPlot.setCurrentValues(state.getX());
         yPlot.setCurrentValues(state.getY());
         zPlot.setCurrentValues(state.getZ());

         SwingUtilities.invokeLater(
               new Runnable()
               {
               public void run()
                  {
                  xTextFieldRaw.setText(String.valueOf(scaleToDisplay(state.getX())));
                  yTextFieldRaw.setText(String.valueOf(scaleToDisplay(state.getY())));
                  zTextFieldRaw.setText(String.valueOf(scaleToDisplay(state.getZ())));
                  if (service.isUnitConversionSupported())
                     {
                     xTextFieldG.setText(decimalFormat.format(gs.getX()));
                     yTextFieldG.setText(decimalFormat.format(gs.getY()));
                     zTextFieldG.setText(decimalFormat.format(gs.getZ()));
                     }
                  }
               });
         }

      public Component getComponent()
         {
         return panel;
         }

      public Component getBlockIcon()
         {
         JPanel icon = new JPanel();
         return icon;
         }

      public boolean execute(final String operationName, final Map<String, String> parameterMap)
         {
         return false;
         }

      public String getCurrentOperationName()
         {
         return null;
         }

      public Set<XmlParameter> buildParameters()
         {
         return null;
         }

      private int scaleToDisplay(final int value)
         {
         return scaleValue(value, ACTUAL_MIN_VALUE, ACTUAL_MAX_VALUE, DISPLAY_MIN_VALUE, DISPLAY_MAX_VALUE);
         }
      }
   }