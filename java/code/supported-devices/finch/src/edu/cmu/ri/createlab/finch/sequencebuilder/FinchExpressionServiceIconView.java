package edu.cmu.ri.createlab.finch.sequencebuilder;

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
import edu.cmu.ri.createlab.terk.services.buzzer.BuzzerService;
import edu.cmu.ri.createlab.terk.services.led.FullColorLEDService;
import edu.cmu.ri.createlab.terk.services.motor.OpenLoopVelocityControllableMotorService;
import edu.cmu.ri.createlab.userinterface.util.ImageUtils;
import edu.cmu.ri.createlab.userinterface.util.SwingUtils;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 * @author Jenn Cross (jenncross99@gmail.com)
 */
public class FinchExpressionServiceIconView extends BaseExpressionServiceIconView
   {
   public FinchExpressionServiceIconView()
      {
      //TODO: This big mess of map creating needs to be moved elsewhere - preferably more autonomously.

      final String audio = AudioService.TYPE_ID;
      getEnabledIconMap().put(audio, new JLabel(ImageUtils.createImageIcon("/edu/cmu/ri/createlab/sequencebuilder/programelement/view/images/service_icons/YE_aud.png")));
      getOffIconMap().put(audio, new JLabel(ImageUtils.createImageIcon("/edu/cmu/ri/createlab/sequencebuilder/programelement/view/images/service_icons/YD_aud.png")));
      getDisabledIconMap().put(audio, new JLabel(ImageUtils.createImageIcon("/edu/cmu/ri/createlab/sequencebuilder/programelement/view/images/service_icons/YY_aud.png")));
      getToolTipTextMap().put(audio, "Audio");

      final String buzzer = BuzzerService.TYPE_ID;
      getEnabledIconMap().put(buzzer, new JLabel(ImageUtils.createImageIcon("/edu/cmu/ri/createlab/sequencebuilder/programelement/view/images/service_icons/YE_aud.png")));
      getOffIconMap().put(buzzer, new JLabel(ImageUtils.createImageIcon("/edu/cmu/ri/createlab/sequencebuilder/programelement/view/images/service_icons/YD_aud.png")));
      getDisabledIconMap().put(buzzer, new JLabel(ImageUtils.createImageIcon("/edu/cmu/ri/createlab/sequencebuilder/programelement/view/images/service_icons/YY_aud.png")));
      getToolTipTextMap().put(buzzer, "Buzzer");

      final String fullColorLEDs = FullColorLEDService.TYPE_ID;
      getEnabledIconMap().put(fullColorLEDs, new JLabel(ImageUtils.createImageIcon("/edu/cmu/ri/createlab/sequencebuilder/programelement/view/images/service_icons/YE_TriLED.png")));
      getOffIconMap().put(fullColorLEDs, new JLabel(ImageUtils.createImageIcon("/edu/cmu/ri/createlab/sequencebuilder/programelement/view/images/service_icons/YD_TriLED.png")));
      getDisabledIconMap().put(fullColorLEDs, new JLabel(ImageUtils.createImageIcon("/edu/cmu/ri/createlab/sequencebuilder/programelement/view/images/service_icons/YY_TriLED.png")));
      getToolTipTextMap().put(fullColorLEDs, "Tri-Color LED");
      final HashSet<XmlParameter> fullColorLEDParamSet = new HashSet<XmlParameter>();
      fullColorLEDParamSet.add(new XmlParameter("red", 0));
      fullColorLEDParamSet.add(new XmlParameter("blue", 0));
      fullColorLEDParamSet.add(new XmlParameter("green", 0));
      getOffValueMap().put(fullColorLEDs, fullColorLEDParamSet);

      final String motors = OpenLoopVelocityControllableMotorService.TYPE_ID;
      getEnabledIconMap().put(motors, new JLabel(ImageUtils.createImageIcon("/edu/cmu/ri/createlab/sequencebuilder/programelement/view/images/service_icons/YE_motor.png")));
      getOffIconMap().put(motors, new JLabel(ImageUtils.createImageIcon("/edu/cmu/ri/createlab/sequencebuilder/programelement/view/images/service_icons/YD_motor.png")));
      getDisabledIconMap().put(motors, new JLabel(ImageUtils.createImageIcon("/edu/cmu/ri/createlab/sequencebuilder/programelement/view/images/service_icons/YY_motor.png")));
      getToolTipTextMap().put(motors, "Motors");
      final HashSet<XmlParameter> motorParamSet = new HashSet<XmlParameter>();
      motorParamSet.add(new XmlParameter(OpenLoopVelocityControllableMotorService.PARAMETER_NAME_VELOCITY, 0));
      getOffValueMap().put(motors, motorParamSet);
      }

   @Override
   public JPanel createBlockIcons(final Set<XmlService> expressionServices, final ServiceManager serviceManager)
      {
      final Map<String, XmlService> expressionServiceMap = new HashMap<String, XmlService>();
      for (final XmlService xmlService : expressionServices)
         {
         expressionServiceMap.put(xmlService.getTypeId(), xmlService);
         }

      final Component audio = createServiceIconPanel(AudioService.TYPE_ID, expressionServiceMap, serviceManager);
      final Component motor = createServiceIconPanel(OpenLoopVelocityControllableMotorService.TYPE_ID, expressionServiceMap, serviceManager);
      final Component triled = createServiceIconPanel(FullColorLEDService.TYPE_ID, expressionServiceMap, serviceManager);
      final Component buzzer = createServiceIconPanel(BuzzerService.TYPE_ID, expressionServiceMap, serviceManager);

      final Component bottomspacer1 = SwingUtils.createRigidSpacer(5);
      final Component bottomspacer2 = SwingUtils.createRigidSpacer(5);
      final Component bottomspacer3 = SwingUtils.createRigidSpacer(5);

      final JPanel icongroup = new JPanel();
      final GroupLayout layout = new GroupLayout(icongroup);
      icongroup.setLayout(layout);

      layout.setVerticalGroup(layout.createSequentialGroup()

                                    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                                    .addComponent(buzzer)
                                                    .addComponent(bottomspacer1)
                                                    .addComponent(motor)
                                                    .addComponent(bottomspacer2)
                                                    .addComponent(triled)
                                                    .addComponent(bottomspacer3)
                                                    .addComponent(audio))
      );

      layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)

                                      .addGroup(layout.createSequentialGroup()
                                                      .addComponent(buzzer)
                                                      .addComponent(bottomspacer1)
                                                      .addComponent(motor)
                                                      .addComponent(bottomspacer2)
                                                      .addComponent(triled)
                                                      .addComponent(bottomspacer3)
                                                      .addComponent(audio))
      );
      icongroup.setName("iconGroup");
      return icongroup;
      }
   }
