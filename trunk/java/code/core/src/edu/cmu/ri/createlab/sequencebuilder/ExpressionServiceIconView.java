package edu.cmu.ri.createlab.sequencebuilder;

import edu.cmu.ri.createlab.terk.expression.XmlDevice;
import edu.cmu.ri.createlab.terk.expression.XmlOperation;
import edu.cmu.ri.createlab.terk.expression.XmlService;
import edu.cmu.ri.createlab.terk.services.DeviceController;
import edu.cmu.ri.createlab.terk.services.ServiceManager;
import edu.cmu.ri.createlab.terk.services.audio.AudioService;
import edu.cmu.ri.createlab.terk.services.led.FullColorLEDService;
import edu.cmu.ri.createlab.terk.services.led.SimpleLEDService;
import edu.cmu.ri.createlab.terk.services.motor.SpeedControllableMotorService;
import edu.cmu.ri.createlab.terk.services.motor.VelocityControllableMotorService;
import edu.cmu.ri.createlab.terk.services.servo.SimpleServoService;
import edu.cmu.ri.createlab.userinterface.util.ImageUtils;
import edu.cmu.ri.createlab.userinterface.util.SwingUtils;


import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: jcross
 * Date: 10/8/11
 * Time: 8:44 PM
 * To change this template use File | Settings | File Templates.
 */
public class ExpressionServiceIconView {

private Set<XmlService> expressionServices;
private ServiceManager deviceServiceManager;
private Map<String, JLabel> enabledIconMap = new HashMap<String, JLabel>();
private Map<String, JLabel> disabledIconMap = new HashMap<String, JLabel>();
private Map<String, String> toolTipTextMap = new HashMap<String, String>();
private Map<String, XmlService> expressionServiceMap = new HashMap<String, XmlService>();


public ExpressionServiceIconView(final Set<XmlService> expressionServices, final ServiceManager deviceServiceManager)
{
   this.expressionServices = expressionServices;
   this.deviceServiceManager = deviceServiceManager;
   createIconMaps();

   for (final XmlService xmlService : expressionServices)
   {
        expressionServiceMap.put(xmlService.getTypeId(), xmlService);
   }
}

private void createIconMaps()
{
      final String audio = AudioService.TYPE_ID;
      enabledIconMap.put(audio, new JLabel(ImageUtils.createImageIcon("/edu/cmu/ri/createlab/sequencebuilder/programelement/view/images/service_icons/YE_aud.png")));
      disabledIconMap.put(audio, new JLabel(ImageUtils.createImageIcon("/edu/cmu/ri/createlab/sequencebuilder/programelement/view/images/service_icons/YD_aud.png")));
      toolTipTextMap.put(audio, "Audio");

      final String motors = VelocityControllableMotorService.TYPE_ID;
      enabledIconMap.put(motors, new JLabel(ImageUtils.createImageIcon("/edu/cmu/ri/createlab/sequencebuilder/programelement/view/images/service_icons/YE_motor.png")));
      disabledIconMap.put(motors, new JLabel(ImageUtils.createImageIcon("/edu/cmu/ri/createlab/sequencebuilder/programelement/view/images/service_icons/YD_motor.png")));
      toolTipTextMap.put(motors, "Motors");

      final String vibMotors= SpeedControllableMotorService.TYPE_ID;
      enabledIconMap.put(vibMotors, new JLabel(ImageUtils.createImageIcon("/edu/cmu/ri/createlab/sequencebuilder/programelement/view/images/service_icons/YE_vib.png")));
      disabledIconMap.put(vibMotors, new JLabel(ImageUtils.createImageIcon("/edu/cmu/ri/createlab/sequencebuilder/programelement/view/images/service_icons/YD_vib.png")));
      toolTipTextMap.put(vibMotors, "Vibration Motors");

      final String servos = SimpleServoService.TYPE_ID;
      enabledIconMap.put(servos, new JLabel(ImageUtils.createImageIcon("/edu/cmu/ri/createlab/sequencebuilder/programelement/view/images/service_icons/YE_servo.png")));
      disabledIconMap.put(servos, new JLabel(ImageUtils.createImageIcon("/edu/cmu/ri/createlab/sequencebuilder/programelement/view/images/service_icons/YD_servo.png")));
      toolTipTextMap.put(servos, "Servos");

      final String fullColorLEDs = FullColorLEDService.TYPE_ID;
      enabledIconMap.put(fullColorLEDs, new JLabel(ImageUtils.createImageIcon("/edu/cmu/ri/createlab/sequencebuilder/programelement/view/images/service_icons/YE_TriLED.png")));
      disabledIconMap.put(fullColorLEDs, new JLabel(ImageUtils.createImageIcon("/edu/cmu/ri/createlab/sequencebuilder/programelement/view/images/service_icons/YD_TriLED.png")));
      toolTipTextMap.put(fullColorLEDs, "Tri-Color LEDs");

      final String simpleLEDs = SimpleLEDService.TYPE_ID;
      enabledIconMap.put(simpleLEDs, new JLabel(ImageUtils.createImageIcon("/edu/cmu/ri/createlab/sequencebuilder/programelement/view/images/service_icons/YE_LED.png")));
      disabledIconMap.put(simpleLEDs, new JLabel(ImageUtils.createImageIcon("/edu/cmu/ri/createlab/sequencebuilder/programelement/view/images/service_icons/YD_LED.png")));
      toolTipTextMap.put(simpleLEDs, "LEDs");
}

private JPanel createServiceIconPanel(final String typeID)
{
    final JPanel iconPanel = new JPanel();
    iconPanel.setLayout(new BoxLayout(iconPanel, BoxLayout.X_AXIS));
    iconPanel.setName("iconPanel");
    final int deviceCount = ((DeviceController)deviceServiceManager.getServiceByTypeId(typeID)).getDeviceCount();

    final XmlService xmlService = expressionServiceMap.get(typeID);
    final boolean[] expressionDevices = new boolean[deviceCount+1];
    java.util.Arrays.fill(expressionDevices, false);

    if(xmlService != null){
         for (final XmlOperation operation: xmlService.getOperations()){
             for (XmlDevice device: operation.getDevices()){
                 expressionDevices[device.getId()]=true;
             }
         }
    }

    for (int i = 0; i < deviceCount; i++)
    {
        if(xmlService != null){
             if (expressionDevices[i]){
                iconPanel.add(new JLabel(enabledIconMap.get(typeID).getIcon()));
             }
            else{
                 iconPanel.add(new JLabel(disabledIconMap.get(typeID).getIcon()));
             }
        }
        else{
            iconPanel.add(new JLabel(disabledIconMap.get(typeID).getIcon()));
        }
    }

    iconPanel.setToolTipText(toolTipTextMap.get(typeID));

    return iconPanel;

}

public JPanel createBlockIcons()
{

      final JPanel audio = createServiceIconPanel(AudioService.TYPE_ID);
      final JPanel motor = createServiceIconPanel(VelocityControllableMotorService.TYPE_ID);
      final JPanel vib = createServiceIconPanel(SpeedControllableMotorService.TYPE_ID);
      final JPanel servo = createServiceIconPanel(SimpleServoService.TYPE_ID);
      final JPanel triled = createServiceIconPanel(FullColorLEDService.TYPE_ID);
      final JPanel led = createServiceIconPanel(SimpleLEDService.TYPE_ID);

      final Component topspacer = SwingUtils.createRigidSpacer(5);

      final Component bottomspacer1 = SwingUtils.createRigidSpacer(5);
      final Component bottomspacer2 = SwingUtils.createRigidSpacer(5);
      final Component bottomspacer3 = SwingUtils.createRigidSpacer(5);

      final JPanel icongroup = new JPanel();
      final GroupLayout layout = new GroupLayout(icongroup);
      icongroup.setLayout(layout);

      layout.setVerticalGroup(layout.createSequentialGroup()
                                    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                                                    .addComponent(led)
                                                    .addComponent(topspacer)
                                                    .addComponent(servo))

                                    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                                    .addComponent(audio)
                                                    .addComponent(bottomspacer1)
                                                    .addComponent(triled)
                                                    .addComponent(bottomspacer2)
                                                    .addComponent(motor)
                                                    .addComponent(bottomspacer3)
                                                    .addComponent(vib))
      );
      layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)

                                      .addGroup(layout.createSequentialGroup()
                                                      .addComponent(led)
                                                      .addComponent(topspacer)
                                                      .addComponent(servo))
                                      .addGroup(layout.createSequentialGroup()
                                                      .addComponent(audio)
                                                      .addComponent(bottomspacer1)
                                                      .addComponent(triled)
                                                      .addComponent(bottomspacer2)
                                                      .addComponent(motor)
                                                      .addComponent(bottomspacer3)
                                                      .addComponent(vib))
      );
      icongroup.setName("iconGroup");
      return icongroup;
}



}
