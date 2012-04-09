package edu.cmu.ri.createlab.hummingbird.sequencebuilder;

import java.awt.Component;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.swing.GroupLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import edu.cmu.ri.createlab.sequencebuilder.BaseExpressionServiceIconView;
import edu.cmu.ri.createlab.terk.expression.XmlParameter;
import edu.cmu.ri.createlab.terk.expression.XmlService;
import edu.cmu.ri.createlab.terk.services.ServiceManager;
import edu.cmu.ri.createlab.terk.services.audio.AudioService;
import edu.cmu.ri.createlab.terk.services.led.FullColorLEDService;
import edu.cmu.ri.createlab.terk.services.led.SimpleLEDService;
import edu.cmu.ri.createlab.terk.services.motor.SpeedControllableMotorService;
import edu.cmu.ri.createlab.terk.services.motor.VelocityControllableMotorService;
import edu.cmu.ri.createlab.terk.services.servo.SimpleServoService;
import edu.cmu.ri.createlab.userinterface.util.ImageUtils;
import edu.cmu.ri.createlab.userinterface.util.SwingUtils;

/**
 * @author Jenn Cross (jenncross99@gmail.com)
 * @author Chris Bartley (bartley@cmu.edu)
 */
public class HummingbirdExpressionServiceIconView extends BaseExpressionServiceIconView
   {
   public HummingbirdExpressionServiceIconView()
      {
      //TODO: This big mess of map creating needs to be moved elsewhere - preferably more autonomously.

      final String audio = AudioService.TYPE_ID;
      getEnabledIconMap().put(audio, new JLabel(ImageUtils.createImageIcon("/edu/cmu/ri/createlab/sequencebuilder/programelement/view/images/service_icons/YE_aud.png")));
      getOffIconMap().put(audio, new JLabel(ImageUtils.createImageIcon("/edu/cmu/ri/createlab/sequencebuilder/programelement/view/images/service_icons/YD_aud.png")));
      getDisabledIconMap().put(audio, new JLabel(ImageUtils.createImageIcon("/edu/cmu/ri/createlab/sequencebuilder/programelement/view/images/service_icons/YY_aud.png")));
      getToolTipTextMap().put(audio, "Audio");

      final String motors = VelocityControllableMotorService.TYPE_ID;
      getEnabledIconMap().put(motors, new JLabel(ImageUtils.createImageIcon("/edu/cmu/ri/createlab/sequencebuilder/programelement/view/images/service_icons/YE_motor.png")));
      getOffIconMap().put(motors, new JLabel(ImageUtils.createImageIcon("/edu/cmu/ri/createlab/sequencebuilder/programelement/view/images/service_icons/YD_motor.png")));
      getDisabledIconMap().put(motors, new JLabel(ImageUtils.createImageIcon("/edu/cmu/ri/createlab/sequencebuilder/programelement/view/images/service_icons/YY_motor.png")));
      getToolTipTextMap().put(motors, "Motors");
      final HashSet<XmlParameter> motorParamSet = new HashSet<XmlParameter>();
      motorParamSet.add(new XmlParameter(VelocityControllableMotorService.PARAMETER_NAME_VELOCITY, 0));
      getOffValueMap().put(motors, motorParamSet);

      final String vibMotors = SpeedControllableMotorService.TYPE_ID;
      getEnabledIconMap().put(vibMotors, new JLabel(ImageUtils.createImageIcon("/edu/cmu/ri/createlab/sequencebuilder/programelement/view/images/service_icons/YE_vib.png")));
      getOffIconMap().put(vibMotors, new JLabel(ImageUtils.createImageIcon("/edu/cmu/ri/createlab/sequencebuilder/programelement/view/images/service_icons/YD_vib.png")));
      getDisabledIconMap().put(vibMotors, new JLabel(ImageUtils.createImageIcon("/edu/cmu/ri/createlab/sequencebuilder/programelement/view/images/service_icons/YY_vib.png")));
      getToolTipTextMap().put(vibMotors, "Vibration Motors");
      final HashSet<XmlParameter> vibMotorParamSet = new HashSet<XmlParameter>();
      vibMotorParamSet.add(new XmlParameter(SpeedControllableMotorService.PARAMETER_NAME_SPEED, 0));
      getOffValueMap().put(vibMotors, vibMotorParamSet);

      final String servos = SimpleServoService.TYPE_ID;
      getEnabledIconMap().put(servos, new JLabel(ImageUtils.createImageIcon("/edu/cmu/ri/createlab/sequencebuilder/programelement/view/images/service_icons/YE_servo.png")));
      getOffIconMap().put(servos, new JLabel(ImageUtils.createImageIcon("/edu/cmu/ri/createlab/sequencebuilder/programelement/view/images/service_icons/YD_servo.png")));
      getDisabledIconMap().put(servos, new JLabel(ImageUtils.createImageIcon("/edu/cmu/ri/createlab/sequencebuilder/programelement/view/images/service_icons/YY_servo.png")));
      getToolTipTextMap().put(servos, "Servos");

      final String fullColorLEDs = FullColorLEDService.TYPE_ID;
      getEnabledIconMap().put(fullColorLEDs, new JLabel(ImageUtils.createImageIcon("/edu/cmu/ri/createlab/sequencebuilder/programelement/view/images/service_icons/YE_TriLED.png")));
      getOffIconMap().put(fullColorLEDs, new JLabel(ImageUtils.createImageIcon("/edu/cmu/ri/createlab/sequencebuilder/programelement/view/images/service_icons/YD_TriLED.png")));
      getDisabledIconMap().put(fullColorLEDs, new JLabel(ImageUtils.createImageIcon("/edu/cmu/ri/createlab/sequencebuilder/programelement/view/images/service_icons/YY_TriLED.png")));
      getToolTipTextMap().put(fullColorLEDs, "Tri-Color LEDs");
      final HashSet<XmlParameter> fullColorLEDParamSet = new HashSet<XmlParameter>();
      fullColorLEDParamSet.add(new XmlParameter("red", 0));
      fullColorLEDParamSet.add(new XmlParameter("blue", 0));
      fullColorLEDParamSet.add(new XmlParameter("green", 0));
      getOffValueMap().put(fullColorLEDs, fullColorLEDParamSet);

      final String simpleLEDs = SimpleLEDService.TYPE_ID;
      getEnabledIconMap().put(simpleLEDs, new JLabel(ImageUtils.createImageIcon("/edu/cmu/ri/createlab/sequencebuilder/programelement/view/images/service_icons/YE_LED.png")));
      getOffIconMap().put(simpleLEDs, new JLabel(ImageUtils.createImageIcon("/edu/cmu/ri/createlab/sequencebuilder/programelement/view/images/service_icons/YD_LED.png")));
      getDisabledIconMap().put(simpleLEDs, new JLabel(ImageUtils.createImageIcon("/edu/cmu/ri/createlab/sequencebuilder/programelement/view/images/service_icons/YY_LED.png")));
      getToolTipTextMap().put(simpleLEDs, "LEDs");
      final HashSet<XmlParameter> simpleLEDParamSet = new HashSet<XmlParameter>();
      simpleLEDParamSet.add(new XmlParameter(SimpleLEDService.PARAMETER_NAME_INTENSITY, 0));
      getOffValueMap().put(simpleLEDs, simpleLEDParamSet);
      }

   @Override
   public JPanel createBlockIcons(final Set<XmlService> expressionServices, final ServiceManager serviceManager)
      {
      final Map<String, XmlService> expressionServiceMap = new HashMap<String, XmlService>();
      for (final XmlService xmlService : expressionServices)
         {
         expressionServiceMap.put(xmlService.getTypeId(), xmlService);
         }

      final JPanel audio = createServiceIconPanel(AudioService.TYPE_ID, expressionServiceMap, serviceManager);
      final JPanel motor = createServiceIconPanel(VelocityControllableMotorService.TYPE_ID, expressionServiceMap, serviceManager);
      final JPanel vib = createServiceIconPanel(SpeedControllableMotorService.TYPE_ID, expressionServiceMap, serviceManager);
      final JPanel servo = createServiceIconPanel(SimpleServoService.TYPE_ID, expressionServiceMap, serviceManager);
      final JPanel triled = createServiceIconPanel(FullColorLEDService.TYPE_ID, expressionServiceMap, serviceManager);
      final JPanel led = createServiceIconPanel(SimpleLEDService.TYPE_ID, expressionServiceMap, serviceManager);

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
