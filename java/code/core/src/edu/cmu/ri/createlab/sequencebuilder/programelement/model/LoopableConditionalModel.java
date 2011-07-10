package edu.cmu.ri.createlab.sequencebuilder.programelement.model;

import java.util.SortedSet;
import edu.cmu.ri.createlab.sequencebuilder.ContainerModel;
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
   private static final Logger LOG = Logger.getLogger(LoopableConditionalModel.class);
   public static final String SELECTED_SENSOR_PROPERTY = "selectedSensor";
   public static final String WILL_REEVALUATE_CONDITION_AFTER_IF_BRANCH_COMPLETES_PROPERTY = "willReevaluateConditionAfterIfBranchCompletes";
   public static final String WILL_REEVALUATE_CONDITION_AFTER_ELSE_BRANCH_COMPLETES_PROPERTY = "willReevaluateConditionAfterElseBranchCompletes";

   @NotNull
   private SelectedSensor selectedSensor;
   private boolean willReevaluateConditionAfterIfBranchCompletes = false;
   private boolean willReevaluateConditionAfterElseBranchCompletes = false;
   private final ContainerModel ifBranchContainerModel;
   private final ContainerModel elseBranchContainerModel;

   /**
    * Creates a <code>LoopableConditionalModel</code> with an empty comment, a <code>selectedSensor</code>
    * and <code>false</code> for both booleans which control reevaluation of the conditional after branch completion.
    */
   public LoopableConditionalModel(@NotNull final VisualProgrammerDevice visualProgrammerDevice)
      {
      this(visualProgrammerDevice, null, null, false, false, new ContainerModel(), new ContainerModel());
      }

   /** Creates a <code>LoopableConditionalModel</code> with the given <code>comment</code>. */
   public LoopableConditionalModel(@NotNull final VisualProgrammerDevice visualProgrammerDevice,
                                   @Nullable final String comment,
                                   @Nullable final SelectedSensor selectedSensor,
                                   final boolean willReevaluateConditionAfterIfBranchCompletes,
                                   final boolean willReevaluateConditionAfterElseBranchCompletes,
                                   @NotNull final ContainerModel ifBranchContainerModel,
                                   @NotNull final ContainerModel elseBranchContainerModel)
      {
      super(visualProgrammerDevice, comment);
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
           originalLoopableConditionalModel.getSelectedSensor(),
           originalLoopableConditionalModel.willReevaluateConditionAfterIfBranchCompletes(),
           originalLoopableConditionalModel.willReevaluateConditionAfterElseBranchCompletes(),
           originalLoopableConditionalModel.getIfBranchContainerModel(),
           originalLoopableConditionalModel.getElseBranchContainerModel());
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
      final Element ifBranchElement = new Element("if-branch");
      ifBranchElement.addContent(ifBranchContainerModel.toElement());

      final Element elseBranchElement = new Element("else-branch");
      elseBranchElement.addContent(elseBranchContainerModel.toElement());

      final Element element = new Element("loopable-conditional");
      element.setAttribute("will-reevaluate-conditional-after-if-branch-completes", String.valueOf(willReevaluateConditionAfterIfBranchCompletes));
      element.setAttribute("will-reevaluate-conditional-after-else-branch-completes", String.valueOf(willReevaluateConditionAfterElseBranchCompletes));
      element.addContent(getCommentAsElement());
      element.addContent(selectedSensor.toSensorConditionalElement());
      element.addContent(ifBranchElement);
      element.addContent(elseBranchElement);

      return element;
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
      @NotNull
      private final Sensor sensor;
      private final int portNumber;
      private final int thresholdPercentage;

      public SelectedSensor(@NotNull final Sensor sensor,
                            final int portNumber,
                            final int thresholdPercentage)
         {
         this.sensor = sensor;
         this.portNumber = portNumber;
         this.thresholdPercentage = Math.min(Math.max(thresholdPercentage, 0), 100);  // clamp the percentage to [0,100]
         }

      /**
       * Creates a <code>SelectedSensor</code> with with given {@link SensorImpl} for port 0
       * and threshold of 50%.
       */
      private SelectedSensor(@NotNull final Sensor sensor)
         {
         this(sensor, 0, 50);
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
         final Element sensorConditionalElement = new Element("sensor-conditional");
         sensorConditionalElement.setAttribute("threshold-percentage", String.valueOf(thresholdPercentage));
         sensorConditionalElement.addContent(sensor.toServiceElementForPort(portNumber));

         return sensorConditionalElement;
         }
      }
   }
