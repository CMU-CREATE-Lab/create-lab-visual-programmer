package edu.cmu.ri.createlab.sequencebuilder.programelement.model;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import edu.cmu.ri.createlab.sequencebuilder.ContainerModel;
import edu.cmu.ri.createlab.sequencebuilder.ImpressionExecutor;
import edu.cmu.ri.createlab.sequencebuilder.SequenceExecutor;
import edu.cmu.ri.createlab.terk.expression.XmlDevice;
import edu.cmu.ri.createlab.terk.expression.XmlOperation;
import edu.cmu.ri.createlab.terk.expression.XmlService;
import edu.cmu.ri.createlab.visualprogrammer.Sensor;
import edu.cmu.ri.createlab.visualprogrammer.SensorImpl;
import edu.cmu.ri.createlab.visualprogrammer.VisualProgrammerDevice;
import org.apache.log4j.Logger;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * <p>
 * <code>LoopableConditionalModel</code> is the {@link ProgramElementModel} for a loopable conditional.
 * </p>
 *
 * @author Chris Bartley (bartley@cmu.edu)
 */
public final class LoopableConditionalModel extends BaseProgramElementModel<LoopableConditionalModel>
   {
   public interface ExecutionEventListener
      {
      void handleExecutionStart();

      void handleExecutionEnd();
      }

   private static final Logger LOG = Logger.getLogger(LoopableConditionalModel.class);
   public static final String SELECTED_SENSOR_PROPERTY = "selectedSensor";
   public static final String WILL_REEVALUATE_CONDITION_AFTER_IF_BRANCH_COMPLETES_PROPERTY = "willReevaluateConditionAfterIfBranchCompletes";
   public static final String WILL_REEVALUATE_CONDITION_AFTER_ELSE_BRANCH_COMPLETES_PROPERTY = "willReevaluateConditionAfterElseBranchCompletes";
   public static final String XML_ELEMENT_NAME = "loopable-conditional";
   private static final String XML_ATTRIBUTE_WILL_REEVALUATE_CONDITIONAL_AFTER_IF_BRANCH_COMPLETES = "will-reevaluate-conditional-after-if-branch-completes";
   private static final String XML_ATTRIBUTE_WILL_REEVALUATE_CONDITIONAL_AFTER_ELSE_BRANCH_COMPLETES = "will-reevaluate-conditional-after-else-branch-completes";
   private static final String XML_ELEMENT_IF_BRANCH = "if-branch";
   private static final String XML_ELEMENT_ELSE_BRANCH = "else-branch";

   @Nullable
   public static LoopableConditionalModel createFromXmlElement(@NotNull final VisualProgrammerDevice visualProgrammerDevice,
                                                               @Nullable final Element element)
      {
      if (element != null)
         {
         LOG.debug("LoopableConditionalModel.createFromXmlElement(): " + element);

         // create and populate the containers
         final ContainerModel ifContainerModel = new ContainerModel();
         final ContainerModel elseContainerModel = new ContainerModel();
         ifContainerModel.load(visualProgrammerDevice, element.getChild(XML_ELEMENT_IF_BRANCH).getChild(ContainerModel.XML_ELEMENT_NAME));
         elseContainerModel.load(visualProgrammerDevice, element.getChild(XML_ELEMENT_ELSE_BRANCH).getChild(ContainerModel.XML_ELEMENT_NAME));

         return new LoopableConditionalModel(visualProgrammerDevice,
                                             getCommentFromParentXmlElement(element),
                                             getIsCommentVisibleFromParentXmlElement(element),
                                             SelectedSensor.createFromXmlElement(visualProgrammerDevice, element.getChild(SelectedSensor.XML_ELEMENT_SENSOR_CONDITIONAL)),
                                             getBooleanAttributeValue(element, XML_ATTRIBUTE_WILL_REEVALUATE_CONDITIONAL_AFTER_IF_BRANCH_COMPLETES),
                                             getBooleanAttributeValue(element, XML_ATTRIBUTE_WILL_REEVALUATE_CONDITIONAL_AFTER_ELSE_BRANCH_COMPLETES),
                                             ifContainerModel,
                                             elseContainerModel);
         }
      return null;
      }

   @NotNull
   private SelectedSensor selectedSensor;
   private boolean willReevaluateConditionAfterIfBranchCompletes = false;
   private boolean willReevaluateConditionAfterElseBranchCompletes = false;
   private final ContainerModel ifBranchContainerModel;
   private final ContainerModel elseBranchContainerModel;
   private final Set<ExecutionEventListener> executionEventListeners = new HashSet<ExecutionEventListener>();

   /**
    * Creates a <code>LoopableConditionalModel</code> with an empty hidden comment, a <code>selectedSensor</code>
    * and <code>false</code> for both booleans which control reevaluation of the conditional after branch completion.
    */
   public LoopableConditionalModel(@NotNull final VisualProgrammerDevice visualProgrammerDevice)
      {
      this(visualProgrammerDevice, null, false, null, false, false, new ContainerModel(), new ContainerModel());
      }

   /** Creates a <code>LoopableConditionalModel</code> with the given <code>comment</code>. */
   public LoopableConditionalModel(@NotNull final VisualProgrammerDevice visualProgrammerDevice,
                                   @Nullable final String comment,
                                   final boolean isCommentVisible,
                                   @Nullable final SelectedSensor selectedSensor,
                                   final boolean willReevaluateConditionAfterIfBranchCompletes,
                                   final boolean willReevaluateConditionAfterElseBranchCompletes,
                                   @NotNull final ContainerModel ifBranchContainerModel,
                                   @NotNull final ContainerModel elseBranchContainerModel)
      {
      super(visualProgrammerDevice, comment, isCommentVisible);
      if (selectedSensor == null)
         {
         // choose the first one as a default
         final SortedSet<Sensor> sensors = visualProgrammerDevice.getSensors();
         final Sensor sensor = sensors.first();
         this.selectedSensor = new SelectedSensor(sensor);
         }
      else
         {
         this.selectedSensor = selectedSensor;
         }
      this.willReevaluateConditionAfterIfBranchCompletes = willReevaluateConditionAfterIfBranchCompletes;
      this.willReevaluateConditionAfterElseBranchCompletes = willReevaluateConditionAfterElseBranchCompletes;
      this.ifBranchContainerModel = ifBranchContainerModel;
      this.elseBranchContainerModel = elseBranchContainerModel;
      }

   /** Copy construtor */
   private LoopableConditionalModel(@NotNull final LoopableConditionalModel originalLoopableConditionalModel)
      {
      this(originalLoopableConditionalModel.getVisualProgrammerDevice(),
           originalLoopableConditionalModel.getComment(),
           originalLoopableConditionalModel.isCommentVisible(),
           originalLoopableConditionalModel.getSelectedSensor(),
           originalLoopableConditionalModel.willReevaluateConditionAfterIfBranchCompletes(),
           originalLoopableConditionalModel.willReevaluateConditionAfterElseBranchCompletes(),
           new ContainerModel(),
           new ContainerModel());   // we DON'T want to share the container models!
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

   public String getElementType()
      {
      return XML_ELEMENT_NAME;
      }

   @Override
   @NotNull
   public String getName()
      {
      return this.getClass().getSimpleName();
      }

   @Override
   public boolean isContainer()
      {
      return true;
      }

   @NotNull
   @Override
   public LoopableConditionalModel createCopy()
      {
      return new LoopableConditionalModel(this);
      }

   @NotNull
   @Override
   public Element toElement()
      {
      final Element ifBranchElement = new Element(XML_ELEMENT_IF_BRANCH);
      ifBranchElement.addContent(ifBranchContainerModel.toElement());

      final Element elseBranchElement = new Element(XML_ELEMENT_ELSE_BRANCH);
      elseBranchElement.addContent(elseBranchContainerModel.toElement());

      final Element element = new Element(XML_ELEMENT_NAME);
      element.setAttribute(XML_ATTRIBUTE_WILL_REEVALUATE_CONDITIONAL_AFTER_IF_BRANCH_COMPLETES, String.valueOf(willReevaluateConditionAfterIfBranchCompletes));
      element.setAttribute(XML_ATTRIBUTE_WILL_REEVALUATE_CONDITIONAL_AFTER_ELSE_BRANCH_COMPLETES, String.valueOf(willReevaluateConditionAfterElseBranchCompletes));
      element.addContent(getCommentAsElement());
      element.addContent(selectedSensor.toSensorConditionalElement());
      element.addContent(ifBranchElement);
      element.addContent(elseBranchElement);

      return element;
      }

   @Override
   public void execute()
      {
      LOG.debug("LoopableConditionalModel.execute()");
      if (SequenceExecutor.getInstance().isRunning())
         {
         // notify listeners that we're about to begin
         for (final ExecutionEventListener listener : executionEventListeners)
            {
            listener.handleExecutionStart();
            }

         boolean willReevaluateCondition = willReevaluateConditionAfterIfBranchCompletes;
         do
            {
            // check sensor
            final Integer rawValue = ImpressionExecutor.getInstance().execute(getVisualProgrammerDevice().getServiceManager(), selectedSensor.toXmlService());

            // convert raw value to percentage
            if (rawValue != null)
               {
               final Integer percentage = selectedSensor.getSensor().convertRawValueToPercentage(rawValue);
               if (percentage != null)
                  {
                  final ContainerModel containerModelOfChosenBranch;
                  if (percentage < selectedSensor.getThresholdPercentage())
                     {
                     LOG.debug("LoopableConditionalModel.execute(): chose if branch (percentage=" + percentage + ")");
                     containerModelOfChosenBranch = ifBranchContainerModel;
                     willReevaluateCondition = willReevaluateConditionAfterIfBranchCompletes;
                     }
                  else
                     {
                     LOG.debug("LoopableConditionalModel.execute(): chose else branch (percentage=" + percentage + ")");
                     containerModelOfChosenBranch = elseBranchContainerModel;
                     willReevaluateCondition = willReevaluateConditionAfterElseBranchCompletes;
                     }

                  // iterate over the models and execute them
                  final List<ProgramElementModel> programElementModels = containerModelOfChosenBranch.getAsList();
                  for (final ProgramElementModel model : programElementModels)
                     {
                     model.execute();
                     }
                  }
               }
            }
         while (willReevaluateCondition && SequenceExecutor.getInstance().isRunning());

         // notify listeners that we're done
         for (final ExecutionEventListener listener : executionEventListeners)
            {
            listener.handleExecutionEnd();
            }
         }
      }

   @NotNull
   public SelectedSensor getSelectedSensor()
      {
      return selectedSensor;
      }

   public void setSelectedSensor(@NotNull final SelectedSensor selectedSensor)
      {
      if (LOG.isDebugEnabled())
         {
         LOG.debug("LoopableConditionalModel.setSelectedSensor(" + selectedSensor + ")");
         }
      final PropertyChangeEvent event = new PropertyChangeEventImpl(SELECTED_SENSOR_PROPERTY, this.selectedSensor, selectedSensor);
      this.selectedSensor = selectedSensor;
      firePropertyChangeEvent(event);
      }

   public boolean willReevaluateConditionAfterIfBranchCompletes()
      {
      return willReevaluateConditionAfterIfBranchCompletes;
      }

   /**
    * Sets whether the condition will be reevaluated after the if-branch completes and causes a
    * {@link PropertyChangeEvent} to be fired for the
    * {@link #WILL_REEVALUATE_CONDITION_AFTER_IF_BRANCH_COMPLETES_PROPERTY} property.
    */
   public void setWillReevaluateConditionAfterIfBranchCompletes(final boolean willReevaluateConditionAfterIfBranchCompletes)
      {
      if (LOG.isDebugEnabled())
         {
         LOG.debug("LoopableConditionalModel.setWillReevaluateConditionAfterIfBranchCompletes(" + willReevaluateConditionAfterIfBranchCompletes + ")");
         }
      final PropertyChangeEvent event = new PropertyChangeEventImpl(WILL_REEVALUATE_CONDITION_AFTER_IF_BRANCH_COMPLETES_PROPERTY, this.willReevaluateConditionAfterIfBranchCompletes, willReevaluateConditionAfterIfBranchCompletes);
      this.willReevaluateConditionAfterIfBranchCompletes = willReevaluateConditionAfterIfBranchCompletes;
      firePropertyChangeEvent(event);
      }

   public boolean willReevaluateConditionAfterElseBranchCompletes()
      {
      return willReevaluateConditionAfterElseBranchCompletes;
      }

   /**
    * Sets whether the condition will be reevaluated after the else-branch completes and causes a
    * {@link PropertyChangeEvent} to be fired for the
    * {@link #WILL_REEVALUATE_CONDITION_AFTER_ELSE_BRANCH_COMPLETES_PROPERTY} property.
    */
   public void setWillReevaluateConditionAfterElseBranchCompletes(final boolean willReevaluateConditionAfterElseBranchCompletes)
      {
      if (LOG.isDebugEnabled())
         {
         LOG.debug("LoopableConditionalModel.setWillReevaluateConditionAfterElseBranchCompletes(" + willReevaluateConditionAfterElseBranchCompletes + ")");
         }
      final PropertyChangeEvent event = new PropertyChangeEventImpl(WILL_REEVALUATE_CONDITION_AFTER_ELSE_BRANCH_COMPLETES_PROPERTY, this.willReevaluateConditionAfterElseBranchCompletes, willReevaluateConditionAfterElseBranchCompletes);
      this.willReevaluateConditionAfterElseBranchCompletes = willReevaluateConditionAfterElseBranchCompletes;
      firePropertyChangeEvent(event);
      }

   public ContainerModel getIfBranchContainerModel()
      {
      return ifBranchContainerModel;
      }

   public ContainerModel getElseBranchContainerModel()
      {
      return elseBranchContainerModel;
      }

   public static final class SelectedSensor
      {
      private static final String XML_ELEMENT_SENSOR_CONDITIONAL = "sensor-conditional";
      private static final String XML_ATTRIBUTE_SENSOR_NAME = "sensor-name";
      private static final String XML_ATTRIBUTE_THRESHOLD_PERCENTAGE = "threshold-percentage";
      private static final int DEFAULT_THRESHOLD_PERCENTAGE = 50;
      private static final int DEFAULT_PORT_NUMBER = 0;
      @NotNull
      private final Sensor sensor;
      private final int portNumber;
      private final int thresholdPercentage;

      @Nullable
      private static SelectedSensor createFromXmlElement(@NotNull final VisualProgrammerDevice visualProgrammerDevice,
                                                         @Nullable final Element element)
         {
         if (element != null)
            {
            final Element serviceElement = element.getChild(Sensor.XML_ELEMENT_SERVICE);
            final Element operationElement = serviceElement.getChild(Sensor.XML_ELEMENT_OPERATION);
            final Element deviceElement = operationElement.getChild(Sensor.XML_ELEMENT_DEVICE);

            final String sensorName = element.getAttributeValue(XML_ATTRIBUTE_SENSOR_NAME);
            final String serviceTypeId = serviceElement.getAttributeValue(Sensor.XML_ATTRIBUTE_SERVICE_TYPE_ID);

            final Sensor sensor = visualProgrammerDevice.findSensor(sensorName, serviceTypeId);
            if (sensor == null)
               {
               LOG.error("LoopableConditionalModel$SelectedSensor.createFromXmlElement(): Could not find sensor matching [" + sensorName + "|" + serviceTypeId + "].  Returning null.");
               }
            else
               {
               return new SelectedSensor(sensor,
                                         BaseProgramElementModel.getIntAttributeValue(deviceElement, Sensor.XML_ATTRIBUTE_DEVICE_ID, DEFAULT_PORT_NUMBER),
                                         BaseProgramElementModel.getIntAttributeValue(element, XML_ATTRIBUTE_THRESHOLD_PERCENTAGE, DEFAULT_THRESHOLD_PERCENTAGE));
               }
            }
         return null;
         }

      public SelectedSensor(@NotNull final Sensor sensor,
                            final int portNumber,
                            final int thresholdPercentage)
         {
         this.sensor = sensor;
         this.portNumber = portNumber;
         this.thresholdPercentage = Math.min(Math.max(thresholdPercentage, 0), 100);  // clamp the percentage to [0,100]
         }

      /**
       * Creates a <code>SelectedSensor</code> with with given {@link SensorImpl} for port 0 and threshold of 50%.
       */
      private SelectedSensor(@NotNull final Sensor sensor)
         {
         this(sensor, DEFAULT_PORT_NUMBER, DEFAULT_THRESHOLD_PERCENTAGE);
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

      public int getThresholdPercentage()
         {
         return thresholdPercentage;
         }

      @Override
      public boolean equals(final Object o)
         {
         if (this == o)
            {
            return true;
            }
         if (o == null || getClass() != o.getClass())
            {
            return false;
            }

         final SelectedSensor that = (SelectedSensor)o;

         if (portNumber != that.portNumber)
            {
            return false;
            }
         if (thresholdPercentage != that.thresholdPercentage)
            {
            return false;
            }
         if (!sensor.equals(that.sensor))
            {
            return false;
            }

         return true;
         }

      @Override
      public int hashCode()
         {
         int result = sensor.hashCode();
         result = 31 * result + portNumber;
         result = 31 * result + thresholdPercentage;
         return result;
         }

      @Override
      public String toString()
         {
         final StringBuilder sb = new StringBuilder();
         sb.append("SelectedSensor");
         sb.append("{sensor=").append(sensor);
         sb.append(", portNumber=").append(portNumber);
         sb.append(", thresholdPercentage=").append(thresholdPercentage);
         sb.append('}');
         return sb.toString();
         }

      public Element toSensorConditionalElement()
         {
         final Element sensorConditionalElement = new Element(XML_ELEMENT_SENSOR_CONDITIONAL);
         sensorConditionalElement.setAttribute(XML_ATTRIBUTE_SENSOR_NAME, sensor.getName());
         sensorConditionalElement.setAttribute(XML_ATTRIBUTE_THRESHOLD_PERCENTAGE, String.valueOf(thresholdPercentage));
         sensorConditionalElement.addContent(sensor.toServiceElementForPort(portNumber));

         return sensorConditionalElement;
         }

      public XmlService toXmlService()
         {
         return new XmlService(sensor.getServiceTypeId(), new XmlOperation(sensor.getOperationName(), new XmlDevice(portNumber)));
         }
      }
   }
