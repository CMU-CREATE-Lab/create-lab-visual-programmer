package edu.cmu.ri.createlab.expressionbuilder.controlpanel;

import java.util.HashMap;
import java.util.Map;
import edu.cmu.ri.createlab.expressionbuilder.controlpanel.services.accelerometer.AccelerometerServiceControlPanel;
import edu.cmu.ri.createlab.expressionbuilder.controlpanel.services.analog.AnalogInputsServiceControlPanel;
import edu.cmu.ri.createlab.expressionbuilder.controlpanel.services.audio.AudioServiceControlPanel;
import edu.cmu.ri.createlab.expressionbuilder.controlpanel.services.buzzer.BuzzerServiceControlPanel;
import edu.cmu.ri.createlab.expressionbuilder.controlpanel.services.led.FullColorLEDServiceControlPanel;
import edu.cmu.ri.createlab.expressionbuilder.controlpanel.services.led.SimpleLEDServiceControlPanel;
import edu.cmu.ri.createlab.expressionbuilder.controlpanel.services.motor.OpenLoopVelocityControllableMotorServiceControlPanel;
import edu.cmu.ri.createlab.expressionbuilder.controlpanel.services.motor.PositionControllableMotorServiceControlPanel;
import edu.cmu.ri.createlab.expressionbuilder.controlpanel.services.motor.SpeedControllableMotorServiceControlPanel;
import edu.cmu.ri.createlab.expressionbuilder.controlpanel.services.motor.VelocityControllableMotorServiceControlPanel;
import edu.cmu.ri.createlab.expressionbuilder.controlpanel.services.obstacle.SimpleObstacleDetectorServiceControlPanel;
import edu.cmu.ri.createlab.expressionbuilder.controlpanel.services.photoresistor.PhotoresistorServiceControlPanel;
import edu.cmu.ri.createlab.expressionbuilder.controlpanel.services.servo.SimpleServoServiceControlPanel;
import edu.cmu.ri.createlab.expressionbuilder.controlpanel.services.thermistor.ThermistorServiceControlPanel;
import edu.cmu.ri.createlab.terk.services.Service;
import edu.cmu.ri.createlab.terk.services.ServiceManager;
import edu.cmu.ri.createlab.terk.services.accelerometer.AccelerometerService;
import edu.cmu.ri.createlab.terk.services.analog.AnalogInputsService;
import edu.cmu.ri.createlab.terk.services.audio.AudioService;
import edu.cmu.ri.createlab.terk.services.buzzer.BuzzerService;
import edu.cmu.ri.createlab.terk.services.led.FullColorLEDService;
import edu.cmu.ri.createlab.terk.services.led.SimpleLEDService;
import edu.cmu.ri.createlab.terk.services.motor.OpenLoopVelocityControllableMotorService;
import edu.cmu.ri.createlab.terk.services.motor.PositionControllableMotorService;
import edu.cmu.ri.createlab.terk.services.motor.SpeedControllableMotorService;
import edu.cmu.ri.createlab.terk.services.motor.VelocityControllableMotorService;
import edu.cmu.ri.createlab.terk.services.obstacle.SimpleObstacleDetectorService;
import edu.cmu.ri.createlab.terk.services.photoresistor.PhotoresistorService;
import edu.cmu.ri.createlab.terk.services.servo.SimpleServoService;
import edu.cmu.ri.createlab.terk.services.thermistor.ThermistorService;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
final class ServiceControlPanelFactory
   {
   private static final ServiceControlPanelFactory INSTANCE = new ServiceControlPanelFactory();

   static ServiceControlPanelFactory getInstance()
      {
      return INSTANCE;
      }

   private Map<String, ServiceControlPanelCreator> serviceControlPanelCreatorMap = new HashMap<String, ServiceControlPanelCreator>();

   private ServiceControlPanelFactory()
      {
      serviceControlPanelCreatorMap.put(AccelerometerService.TYPE_ID,
                                        new ServiceControlPanelCreator()
                                        {
                                        public ServiceControlPanel createServiceControlPanel(final ControlPanelManager controlPanelManager, final Service service)
                                           {
                                           return new AccelerometerServiceControlPanel(controlPanelManager, (AccelerometerService)service);
                                           }
                                        });
      serviceControlPanelCreatorMap.put(AnalogInputsService.TYPE_ID,
                                        new ServiceControlPanelCreator()
                                        {
                                        public ServiceControlPanel createServiceControlPanel(final ControlPanelManager controlPanelManager, final Service service)
                                           {
                                           return new AnalogInputsServiceControlPanel(controlPanelManager, (AnalogInputsService)service);
                                           }
                                        });
      serviceControlPanelCreatorMap.put(AudioService.TYPE_ID,
                                        new ServiceControlPanelCreator()
                                        {
                                        public ServiceControlPanel createServiceControlPanel(final ControlPanelManager controlPanelManager, final Service service)
                                           {
                                           return new AudioServiceControlPanel(controlPanelManager, (AudioService)service);
                                           }
                                        });
      serviceControlPanelCreatorMap.put(BuzzerService.TYPE_ID,
                                        new ServiceControlPanelCreator()
                                        {
                                        public ServiceControlPanel createServiceControlPanel(final ControlPanelManager controlPanelManager, final Service service)
                                           {
                                           return new BuzzerServiceControlPanel(controlPanelManager, (BuzzerService)service);
                                           }
                                        });
      //serviceControlPanelCreatorMap.put(FinchService.TYPE_ID,
      //                                  new ServiceControlPanelCreator()
      //                                  {
      //                                  public ServiceControlPanel createServiceControlPanel(final ControlPanelManager controlPanelManager, final Service service)
      //                                     {
      //                                     return new FinchServiceControlPanel(controlPanelManager, (FinchService)service);
      //                                     }
      //                                  });
      serviceControlPanelCreatorMap.put(FullColorLEDService.TYPE_ID,
                                        new ServiceControlPanelCreator()
                                        {
                                        public ServiceControlPanel createServiceControlPanel(final ControlPanelManager controlPanelManager, final Service service)
                                           {
                                           return new FullColorLEDServiceControlPanel(controlPanelManager, (FullColorLEDService)service);
                                           }
                                        });
      //serviceControlPanelCreatorMap.put(HummingbirdService.TYPE_ID,
      //                                  new ServiceControlPanelCreator()
      //                                  {
      //                                  public ServiceControlPanel createServiceControlPanel(final ControlPanelManager controlPanelManager, final Service service)
      //                                     {
      //                                     return new HummingbirdServiceControlPanel(controlPanelManager, (HummingbirdService)service);
      //                                     }
      //                                  });
      serviceControlPanelCreatorMap.put(OpenLoopVelocityControllableMotorService.TYPE_ID,
                                        new ServiceControlPanelCreator()
                                        {
                                        public ServiceControlPanel createServiceControlPanel(final ControlPanelManager controlPanelManager, final Service service)
                                           {
                                           return new OpenLoopVelocityControllableMotorServiceControlPanel(controlPanelManager, (OpenLoopVelocityControllableMotorService)service);
                                           }
                                        });
      serviceControlPanelCreatorMap.put(PhotoresistorService.TYPE_ID,
                                        new ServiceControlPanelCreator()
                                        {
                                        public ServiceControlPanel createServiceControlPanel(final ControlPanelManager controlPanelManager, final Service service)
                                           {
                                           return new PhotoresistorServiceControlPanel(controlPanelManager, (PhotoresistorService)service);
                                           }
                                        });
      serviceControlPanelCreatorMap.put(PositionControllableMotorService.TYPE_ID,
                                        new ServiceControlPanelCreator()
                                        {
                                        public ServiceControlPanel createServiceControlPanel(final ControlPanelManager controlPanelManager, final Service service)
                                           {
                                           return new PositionControllableMotorServiceControlPanel(controlPanelManager, (PositionControllableMotorService)service);
                                           }
                                        });
      serviceControlPanelCreatorMap.put(SimpleLEDService.TYPE_ID,
                                        new ServiceControlPanelCreator()
                                        {
                                        public ServiceControlPanel createServiceControlPanel(final ControlPanelManager controlPanelManager, final Service service)
                                           {
                                           return new SimpleLEDServiceControlPanel(controlPanelManager, (SimpleLEDService)service);
                                           }
                                        });
      serviceControlPanelCreatorMap.put(SimpleServoService.TYPE_ID,
                                        new ServiceControlPanelCreator()
                                        {
                                        public ServiceControlPanel createServiceControlPanel(final ControlPanelManager controlPanelManager, final Service service)
                                           {
                                           return new SimpleServoServiceControlPanel(controlPanelManager, (SimpleServoService)service);
                                           }
                                        });
      serviceControlPanelCreatorMap.put(SimpleObstacleDetectorService.TYPE_ID,
                                        new ServiceControlPanelCreator()
                                        {
                                        public ServiceControlPanel createServiceControlPanel(final ControlPanelManager controlPanelManager, final Service service)
                                           {
                                           return new SimpleObstacleDetectorServiceControlPanel(controlPanelManager, (SimpleObstacleDetectorService)service);
                                           }
                                        });
      serviceControlPanelCreatorMap.put(SpeedControllableMotorService.TYPE_ID,
                                        new ServiceControlPanelCreator()
                                        {
                                        public ServiceControlPanel createServiceControlPanel(final ControlPanelManager controlPanelManager, final Service service)
                                           {
                                           return new SpeedControllableMotorServiceControlPanel(controlPanelManager, (SpeedControllableMotorService)service);
                                           }
                                        });
      serviceControlPanelCreatorMap.put(ThermistorService.TYPE_ID,
                                        new ServiceControlPanelCreator()
                                        {
                                        public ServiceControlPanel createServiceControlPanel(final ControlPanelManager controlPanelManager, final Service service)
                                           {
                                           return new ThermistorServiceControlPanel(controlPanelManager, (ThermistorService)service);
                                           }
                                        });
      serviceControlPanelCreatorMap.put(VelocityControllableMotorService.TYPE_ID,
                                        new ServiceControlPanelCreator()
                                        {
                                        public ServiceControlPanel createServiceControlPanel(final ControlPanelManager controlPanelManager, final Service service)
                                           {
                                           return new VelocityControllableMotorServiceControlPanel(controlPanelManager, (VelocityControllableMotorService)service);
                                           }
                                        });
      }

   Map<String, ServiceControlPanel> createServiceControlPanelsMap(final ControlPanelManager controlPanelManager, final ServiceManager serviceManager)
      {
      final Map<String, ServiceControlPanel> controlPanels = new HashMap<String, ServiceControlPanel>();
      if (serviceManager != null)
         {
         for (final String serviceTypeId : serviceManager.getTypeIdsOfSupportedServices())
            {
            final Service service = serviceManager.getServiceByTypeId(serviceTypeId);
            final ServiceControlPanel serviceControlPanel = createServiceControlPanel(controlPanelManager, serviceTypeId, service);
            if (serviceControlPanel != null)
               {
               controlPanels.put(serviceTypeId, serviceControlPanel);
               }
            }
         }
      return controlPanels;
      }

   private ServiceControlPanel createServiceControlPanel(final ControlPanelManager controlPanelManager, final String serviceTypeId, final Service service)
      {
      final ServiceControlPanelCreator creator = serviceControlPanelCreatorMap.get(serviceTypeId);
      if (creator != null)
         {
         return creator.createServiceControlPanel(controlPanelManager, service);
         }
      return null;
      }

   private interface ServiceControlPanelCreator
      {
      ServiceControlPanel createServiceControlPanel(final ControlPanelManager controlPanelManager, final Service service);
      }
   }
