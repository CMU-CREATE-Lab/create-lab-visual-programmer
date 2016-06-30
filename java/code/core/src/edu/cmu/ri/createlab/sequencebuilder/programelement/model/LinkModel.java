package edu.cmu.ri.createlab.sequencebuilder.programelement.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import javax.swing.SwingWorker;
import edu.cmu.ri.createlab.sequencebuilder.ContainerModel;
import edu.cmu.ri.createlab.sequencebuilder.ImpressionExecutor;
import edu.cmu.ri.createlab.sequencebuilder.SequenceExecutor;
import edu.cmu.ri.createlab.terk.expression.ExpressionOperationExecutor;
import edu.cmu.ri.createlab.terk.services.Service;
import edu.cmu.ri.createlab.terk.services.ServiceManager;
import edu.cmu.ri.createlab.terk.services.led.FullColorLEDService;
import edu.cmu.ri.createlab.terk.services.led.SimpleLEDService;
import edu.cmu.ri.createlab.terk.services.motor.SpeedControllableMotorService;
import edu.cmu.ri.createlab.terk.services.motor.VelocityControllableMotorService;
import edu.cmu.ri.createlab.terk.services.servo.SimpleServoService;
import edu.cmu.ri.createlab.terk.xml.XmlDevice;
import edu.cmu.ri.createlab.terk.xml.XmlOperation;
import edu.cmu.ri.createlab.terk.xml.XmlParameter;
import edu.cmu.ri.createlab.terk.xml.XmlService;
import edu.cmu.ri.createlab.visualprogrammer.Sensor;
import edu.cmu.ri.createlab.visualprogrammer.VisualProgrammerDevice;
import org.apache.log4j.Logger;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * <p>
 * <code>LinkModel</code> is the {@link ProgramElementModel} for a Link Structure.
 * </p>
 * Created by Brandon on 6/24/2016.
 */
public class LinkModel extends BaseProgramElementModel<LinkModel>
   {
   public final Map<String, OutputInfo> outputs = new HashMap<String, OutputInfo>();
   private static final Logger LOG = Logger.getLogger(LinkModel.class);

   public static final String SELECTED_SENSOR_PROPERTY = "selectedSensor";
   public static final String DELAY_IN_MILLIS_PROPERTY = "delayInMillis";
   public static final float MIN_DELAY_VALUE_IN_SECS = 0;
   public static final float MAX_DELAY_VALUE_IN_SECS = 999.99f;
   public static final float DEFAULT_DELAY_VALUE_IN_SECS = 1;
   public static final int MIN_DELAY_VALUE_IN_MILLIS = (int)(MIN_DELAY_VALUE_IN_SECS * 1000);
   public static final int MAX_DELAY_VALUE_IN_MILLIS = (int)(MAX_DELAY_VALUE_IN_SECS * 1000);
   public static final int DEFAULT_DELAY_VALUE_IN_MILLIS = (int)(DEFAULT_DELAY_VALUE_IN_SECS * 1000);
   public static final String DEFAULT_OUTPUT = "LED";
   public static final int DEFAULT_PORT = 0;

   public static final String XML_ELEMENT_NAME = "link";
   private static final String XML_ATTRIBUTE_DELAY_IN_MILLIS = "delay-in-millis";
   private static final String XML_OUTPUT_NAME = "output-name";
   private static final String XML_OUTPUT_PORT = "output-port";

   private LinkedSensor linkedSensor;
   private String selectedOutput;
   private int selectedOutputPort;
   private final ContainerModel parent;
   private int delayInMillis;
   private final Set<ExecutionEventListener> executionEventListeners = new HashSet<ExecutionEventListener>();
   private final Set<RefreshEventListener> refreshEventListeners = new HashSet<RefreshEventListener>();

   @Nullable
   public static LinkModel createFromXmlElement(@NotNull final VisualProgrammerDevice visualProgrammerDevice,
                                                @Nullable final Element element, ContainerModel parent)
      {
      if (element != null)
         {
         return new LinkModel(visualProgrammerDevice,
                              getIntAttributeValue(element, XML_ATTRIBUTE_DELAY_IN_MILLIS, DEFAULT_DELAY_VALUE_IN_MILLIS),
                              getValue(element, XML_OUTPUT_NAME, DEFAULT_OUTPUT),
                              getIntAttributeValue(element, XML_OUTPUT_PORT, DEFAULT_PORT),
                              LinkedSensor.createFromXmlElement(visualProgrammerDevice, element.getChild(LinkedSensor.XML_ELEMENT_SENSOR_LINK)),
                              getCommentFromParentXmlElement(element),
                              getIsCommentVisibleFromParentXmlElement(element),
                              parent);
         }
      else
         {
         return null;
         }
      }

   private LinkModel(@NotNull LinkModel originalModel)
      {
      this(originalModel.visualProgrammerDevice, originalModel.parent);
      }

   public LinkModel(@NotNull final VisualProgrammerDevice visualProgrammerDevice, ContainerModel parent)
      {
      this(visualProgrammerDevice, DEFAULT_DELAY_VALUE_IN_MILLIS, DEFAULT_OUTPUT, DEFAULT_PORT, new LinkedSensor(visualProgrammerDevice.getSensors().iterator().next()), null, false, parent);
      }

   public LinkModel(@NotNull final VisualProgrammerDevice visualProgrammerDevice, int delayInMillis, String selectedOutput, int selectedOutputPort, LinkedSensor linkedSensor, @Nullable final String comment, final boolean isCommentVisible, ContainerModel parent)
      {
      super(visualProgrammerDevice, comment, isCommentVisible);
      this.parent = parent;

      outputs.put("LED", new OutputInfo(SimpleLEDService.TYPE_ID, SimpleLEDService.OPERATION_NAME_SET_INTENSITY, 0, 255, 4, new ArrayList<String>(Collections.singletonList(SimpleLEDService.PARAMETER_NAME_INTENSITY))));
      outputs.put("Tri-LED", new OutputInfo(FullColorLEDService.TYPE_ID, FullColorLEDService.OPERATION_NAME_SET_COLOR, 0, 255, 2, new ArrayList<String>(Arrays.asList("red", "green", "blue"))));
      outputs.put("Vibration", new OutputInfo(SpeedControllableMotorService.TYPE_ID, SpeedControllableMotorService.OPERATION_NAME_SET_SPEED, 0, 255, 2, Collections.singletonList(SpeedControllableMotorService.PARAMETER_NAME_SPEED)));
      outputs.put("Motor", new OutputInfo(VelocityControllableMotorService.TYPE_ID, VelocityControllableMotorService.OPERATION_NAME_SET_VELOCITY, -255, 255, 2, Collections.singletonList(VelocityControllableMotorService.PARAMETER_NAME_VELOCITY)));
      outputs.put("Servo", new OutputInfo(SimpleServoService.TYPE_ID, SimpleServoService.OPERATION_NAME_SET_POSITION, 0, 255, 4, Collections.singletonList(SimpleServoService.PARAMETER_NAME_POSITION)));

      this.linkedSensor = linkedSensor;
      this.delayInMillis = delayInMillis;
      this.selectedOutput = selectedOutput;
      this.selectedOutputPort = selectedOutputPort;
      }

   @Override
   public String getElementType()
      {
      return XML_ELEMENT_NAME;
      }

   @Override
   public boolean containsFork()
      {
      return false;
      }

   @Override
   public ContainerModel getParentContainer()
      {
      return parent;
      }

   @NotNull
   @Override
   public LinkModel createCopy()
      {
      return new LinkModel(this);
      }

   public void addExecutionEventListener(@Nullable final ExecutionEventListener listener)
      {
      if (listener != null)
         {
         executionEventListeners.add(listener);
         }
      }

   public void removeExecutionEventListener(@Nullable final ExecutionEventListener listener)
      {
      if (listener != null)
         {
         executionEventListeners.remove(listener);
         }
      }

   public void addRefreshEventListener(@Nullable final RefreshEventListener listener)
      {
      if (listener != null)
         {
         refreshEventListeners.add(listener);
         }
      }

   public void removeRefreshEventListener(@Nullable final RefreshEventListener listener)
      {
      if (listener != null)
         {
         refreshEventListeners.remove(listener);
         }
      }

   @NotNull
   @Override
   public Element toElement()
      {
      final Element element = new Element(XML_ELEMENT_NAME);
      element.setAttribute(XML_ATTRIBUTE_DELAY_IN_MILLIS, String.valueOf(delayInMillis));
      element.setAttribute(XML_OUTPUT_NAME, selectedOutput);
      element.setAttribute(XML_OUTPUT_PORT, String.valueOf(selectedOutputPort));
      element.addContent(getCommentAsElement());
      element.addContent(linkedSensor.toElement());
      return element;
      }

   @Override
   public void execute()
      {
      LOG.debug("LinkModel.execute()");
      if (SequenceExecutor.getInstance().isRunning())
         {
         final long startTime = System.currentTimeMillis();
         final long endTime = startTime + delayInMillis;
         final ServiceManager serviceManager = getVisualProgrammerDevice().getServiceManager();
         final SwingWorker<Object, Integer> sw =
               new SwingWorker<Object, Integer>()
                  {

                  @Override
                  protected Boolean doInBackground() throws Exception
                     {
                     // check sensor
                     final Object rawValue = ImpressionExecutor.getInstance().execute(getVisualProgrammerDevice().getServiceManager(), linkedSensor.toXmlService());
                     if (rawValue != null)
                        {
                        final Sensor sensor = linkedSensor.getSensor();
                        final Integer percentage = sensor.convertRawValueToPercentage(rawValue);
                        OutputInfo outputInfo = outputs.get(selectedOutput);
                        Double adjustedValue = (percentage.doubleValue() - linkedSensor.getThresholdLower());
                        Double slope = ((double)outputInfo.getMaxValue() - outputInfo.getMinValue()) /
                                       ((double)linkedSensor.getThresholdUpper() - linkedSensor.getThresholdLower());
                        adjustedValue = slope * adjustedValue;
                        adjustedValue = adjustedValue + outputInfo.getMinValue();
                        adjustedValue = Math.max(adjustedValue, outputInfo.getMinValue());
                        adjustedValue = Math.min(adjustedValue, outputInfo.getMaxValue());
                        Integer finalValue = new Double(Math.round(adjustedValue)).intValue();

                        final Set<XmlParameter> parameters = new HashSet<XmlParameter>();
                        for (String parameter : outputInfo.getParameterNames())
                           {
                           parameters.add(new XmlParameter(parameter, finalValue));
                           }

                        final XmlOperation operation = new XmlOperation(outputInfo.getOperationName(), new XmlDevice(selectedOutputPort, parameters));
                        final Service service = serviceManager.getServiceByTypeId(outputInfo.getServiceTypeId());
                        ((ExpressionOperationExecutor)service).executeExpressionOperation(operation);
                        }
                     long currentTime;
                     do
                        {
                        currentTime = System.currentTimeMillis();
                        final int elapsedMillis = (int)(currentTime - startTime);
                        publish(elapsedMillis);
                        try
                           {
                           // TODO: not thrilled about the busy-wait here...
                           Thread.sleep(50);
                           }
                        catch (InterruptedException ignored)
                           {
                           LOG.error("LinkModel.execute().doInBackground(): InterruptedException while sleeping");
                           }
                        }
                     while (currentTime < endTime && !isCancelled() && SequenceExecutor.getInstance().isRunning());
                     return null;
                     }

                  @Override
                  protected void process(final List<Integer> integers)
                     {
                     // just publish the latest update
                     final Integer millis = integers.get(integers.size() - 1);
                     for (final ExecutionEventListener listener : executionEventListeners)
                        {
                        listener.handleElapsedTimeInMillis(millis);
                        }
                     }
                  };

         // notify listeners that we're about to begin
         for (final ExecutionEventListener listener : executionEventListeners)
            {
            listener.handleExecutionStart();
            }

         // start the worker and wait for it to finish
         sw.execute();
         try
            {
            sw.get();
            }
         catch (InterruptedException ignored)
            {
            sw.cancel(true);
            LOG.error("LinkModel.execute(): InterruptedException while waiting for the SwingWorker to finish");
            }
         catch (ExecutionException ignored)
            {
            sw.cancel(true);
            LOG.error("LinkModel.execute(): ExecutionException while waiting for the SwingWorker to finish");
            }
         catch (Exception e)
            {
            sw.cancel(true);
            LOG.error("LinkModel.execute(): Exception while waiting for the SwingWorker to finish", e);
            }

         // notify listeners that we're done
         for (final ExecutionEventListener listener : executionEventListeners)
            {
            listener.handleExecutionEnd();
            }
         }
      }

   @Override
   public void refresh()
      {
      LOG.debug("ExpressionModel.refresh(): refreshing " + getName());
      for (final RefreshEventListener listener : refreshEventListeners)
         {
         listener.handleRefresh();
         }
      }

   public void setSelectedSensor(LinkedSensor sensor)
      {
      final PropertyChangeEvent event = new PropertyChangeEventImpl(SELECTED_SENSOR_PROPERTY, this.linkedSensor, sensor);
      this.linkedSensor = sensor;
      firePropertyChangeEvent(event);
      }

   public void setSelectedOutput(String output, int port)
      {
      this.selectedOutput = output;
      this.selectedOutputPort = port;
      }

   public String getSelectedOutput()
      {
      return this.selectedOutput;
      }

   public int getSelectedOutputPort()
      {
      return this.selectedOutputPort;
      }

   public Set<String> getOutputs()
      {
      return outputs.keySet();
      }

   @NotNull
   @Override
   public String getName()
      {
      return this.getClass().getSimpleName();
      }

   @Override
   public boolean isContainer()
      {
      return false;
      }

   @NotNull
   public LinkedSensor getSelectedSensor()
      {
      return linkedSensor;
      }

   private int cleanDelayInMillis(final int delayInMillis)
      {
      int cleanedDelayInMillis = delayInMillis;
      if (delayInMillis < MIN_DELAY_VALUE_IN_MILLIS)
         {
         cleanedDelayInMillis = MIN_DELAY_VALUE_IN_MILLIS;
         }
      else if (delayInMillis > MAX_DELAY_VALUE_IN_MILLIS)
         {
         cleanedDelayInMillis = MAX_DELAY_VALUE_IN_MILLIS;
         }
      return cleanedDelayInMillis;
      }

   public int getDelayInMillis()
      {
      return delayInMillis;
      }

   /**
    * Sets the delay in milliseconds, and causes a {@link PropertyChangeEvent} to be fired for the
    * {@link #DELAY_IN_MILLIS_PROPERTY} property.  This method ensures that the value is within the range
    * <code>[{@link #MIN_DELAY_VALUE_IN_MILLIS}, {@link #MAX_DELAY_VALUE_IN_MILLIS}]</code>.
    */
   public void setDelayInMillis(final int delayInMillis)
      {
      //listener.onAction(new SequenceAction(SequenceAction.Type.EXPRESSION_DELAY, new ElementLocation(parent, parent.getAsList().indexOf(this)), this.delayInMillis));
      final int cleanedDelayInMillis = cleanDelayInMillis(delayInMillis);
      final PropertyChangeEvent event = new PropertyChangeEventImpl(DELAY_IN_MILLIS_PROPERTY, this.delayInMillis, cleanedDelayInMillis);
      this.delayInMillis = cleanedDelayInMillis;
      firePropertyChangeEvent(event);
      }

   public static final class LinkedSensor
      {
      private static final String XML_ELEMENT_SENSOR_LINK = "sensor-link";
      private static final String XML_ATTRIBUTE_SENSOR_NAME = "sensor-name";
      private static final String XML_ATTRIBUTE_THRESHOLD_UPPER = "threshold-upper";
      private static final String XML_ATTRIBUTE_THRESHOLD_LOWER = "threshold-lower";
      private static final int DEFAULT_UPPER_PERCENTAGE = 100;
      private static final int DEFAULT_LOWER_PERCENTAGE = 0;

      private static final int DEFAULT_PORT_NUMBER = 0;
      @NotNull
      private final Sensor sensor;
      private final int portNumber;
      private final int thresholdUpper;
      private final int thresholdLower;

      @Nullable
      private static LinkedSensor createFromXmlElement(@NotNull final VisualProgrammerDevice visualProgrammerDevice,
                                                       @Nullable final Element element)
         {
         if (element != null)
            {
            final Element serviceElement = element.getChild(XmlService.ELEMENT_NAME);
            final XmlService service = new XmlService(serviceElement);

            // assume there's only a single operation
            final XmlOperation operation = service.getOperations().iterator().next();

            // assume there's only a single device
            final XmlDevice device = operation.getDevices().iterator().next();

            final String sensorName = element.getAttributeValue(XML_ATTRIBUTE_SENSOR_NAME);
            final String serviceTypeId = service.getTypeId();

            final Sensor sensor = visualProgrammerDevice.findSensor(sensorName, serviceTypeId);
            if (sensor == null)
               {
               LOG.error("LinkModel$LinkedSensor.createFromXmlElement(): Could not find sensor matching [" + sensorName + "|" + serviceTypeId + "].  Returning null.");
               }
            else
               {
               return new LinkedSensor(sensor,
                                       device.getId(),
                                       BaseProgramElementModel.getIntAttributeValue(element, XML_ATTRIBUTE_THRESHOLD_UPPER, DEFAULT_UPPER_PERCENTAGE),
                                       BaseProgramElementModel.getIntAttributeValue(element, XML_ATTRIBUTE_THRESHOLD_LOWER, DEFAULT_LOWER_PERCENTAGE));
               }
            }
         return null;
         }

      public LinkedSensor(@NotNull final Sensor sensor,
                          final int portNumber,
                          final int thresholdUpper,
                          final int thresholdLower)
         {
         this.sensor = sensor;
         this.portNumber = portNumber;
         this.thresholdUpper = Math.min(Math.max(thresholdUpper, 0), 100);  // clamp the percentage to [0,100]
         this.thresholdLower = Math.min(Math.max(thresholdLower, 0), 100);  // clamp the percentage to [0,100]
         }

      /**
       * Creates a <code>LinkedSensor</code> with with given {@link Sensor} for port 0
       */
      private LinkedSensor(@NotNull final Sensor sensor)
         {
         this(sensor, DEFAULT_PORT_NUMBER, DEFAULT_UPPER_PERCENTAGE, DEFAULT_LOWER_PERCENTAGE);
         }

      @NotNull
      public Sensor getSensor()
         {
         return sensor;
         }

      public int getPortNumber()
         {
         return portNumber;
         }

      public int getThresholdUpper()
         {
         return thresholdUpper;
         }

      public int getThresholdLower()
         {
         return thresholdLower;
         }

      @Override
      public boolean equals(final Object o)
         {
         if (this == o)
            {
            return true;
            }
         if (!(o instanceof LinkedSensor))
            {
            return false;
            }

         final LinkedSensor that = (LinkedSensor)o;

         if (portNumber != that.portNumber)
            {
            return false;
            }
         if (thresholdUpper != that.thresholdUpper || thresholdLower != that.thresholdLower)
            {
            return false;
            }
         return sensor.equals(that.sensor);
         }

      @Override
      public int hashCode()
         {
         int result = sensor.hashCode();
         result = 31 * result + portNumber;
         result = 31 * result + thresholdUpper;
         result = 31 * result + thresholdLower;
         return result;
         }

      @Override
      public String toString()
         {
         return "LinkedSensor" +
                "{sensor=" + sensor +
                ", portNumber=" + portNumber +
                ", thresholdUpper=" + thresholdUpper +
                ", thresholdLower=" + thresholdLower +
                '}';
         }

      public Element toElement()
         {
         final Element element = new Element(XML_ELEMENT_SENSOR_LINK);
         element.setAttribute(XML_ATTRIBUTE_SENSOR_NAME, sensor.getName());
         element.setAttribute(XML_ATTRIBUTE_THRESHOLD_UPPER, String.valueOf(thresholdUpper));
         element.setAttribute(XML_ATTRIBUTE_THRESHOLD_LOWER, String.valueOf(thresholdLower));
         element.addContent(sensor.toServiceElementForPort(portNumber));

         return element;
         }

      public XmlService toXmlService()
         {
         return new XmlService(sensor.getServiceTypeId(), new XmlOperation(sensor.getOperationName(), new XmlDevice(portNumber)));
         }
      }

   public final class OutputInfo
      {
      private final String serviceTypeId;
      private final String operationName;
      private final int minValue;
      private final int maxValue;
      private final List<String> parameterNames;
      private final int numPorts;

      public OutputInfo(String serviceTypeId, String operationName, int minValue, int maxValue, int numPorts, List<String> parameterNames)
         {
         this.serviceTypeId = serviceTypeId;
         this.operationName = operationName;
         this.minValue = minValue;
         this.maxValue = maxValue;
         this.parameterNames = parameterNames;
         this.numPorts = numPorts;
         }

      public String getServiceTypeId()
         {
         return serviceTypeId;
         }

      public String getOperationName()
         {
         return operationName;
         }

      public int getMinValue()
         {
         return minValue;
         }

      public int getMaxValue()
         {
         return maxValue;
         }

      public List<String> getParameterNames()
         {
         return parameterNames;
         }

      public int getNumPorts()
         {
         return numPorts;
         }
      }

   public interface ExecutionEventListener
      {
      void handleExecutionStart();

      void handleElapsedTimeInMillis(final int millis);

      void handleExecutionEnd();
      }

   public interface RefreshEventListener
      {
      void handleRefresh();
      }
   }
