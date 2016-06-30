package edu.cmu.ri.createlab.sequencebuilder;

import edu.cmu.ri.createlab.sequencebuilder.programelement.model.LinkModel;
import edu.cmu.ri.createlab.sequencebuilder.programelement.model.LoopableConditionalModel;
import org.jdom.Element;

/**
 * Created by Brandon on 6/10/2016.
 *
 */
public class SequenceAction
   {

   public enum Type
      {
         ADD, REMOVE, UP, DOWN, EXPRESSION_DELAY, COUNTER_ITERATIONS, SENSOR_SENSOR, SENSOR_IF, SENSOR_ELSE, LINK_SENSOR, LINK_OUTPUT, LINK_DELAY
      }

   private Type type;
   private ElementLocation location;
   private int number; //used for iterations in counters or delay in expressions
   private String string;
   private Element data;

   private LoopableConditionalModel.SelectedSensor selectedSensor;
   private LinkModel.LinkedSensor linkedSensor;
   private Boolean willReevaluateConditionAfterBranchCompletes = null;

   public SequenceAction(Type type, ElementLocation location, Element data, int number, String string)
      {
      this.type = type;
      this.location = location;
      this.data = data;
      this.number = number;
      this.string = string;
      this.selectedSensor = null;
      this.linkedSensor = null;
      this.willReevaluateConditionAfterBranchCompletes = null;
      }

   public SequenceAction(Type type, ElementLocation location, LoopableConditionalModel.SelectedSensor sensor)
      {
      this(type, location);
      this.selectedSensor = sensor;
      }

   public SequenceAction(Type type, ElementLocation location, LinkModel.LinkedSensor sensor)
      {
      this(type, location);
      this.linkedSensor = sensor;
      }

   public SequenceAction(Type type, ElementLocation location, String output, int port)
      {
      this(type, location, port);
      this.string = output;
      }

   public SequenceAction(Type type, ElementLocation location, boolean willReevaluate)
      {
      this(type, location);
      this.willReevaluateConditionAfterBranchCompletes = willReevaluate;
      }

   public SequenceAction(Type type, ElementLocation location, Element data)
      {
      this(type, location, data, -1, null);
      }

   public SequenceAction(Type type, ElementLocation location)
      {
      this(type, location, null, -1, null);
      }

   public SequenceAction(Type type, ElementLocation location, int number)
      {
      this(type, location, null, number, null);
      }

   public Element getData()
      {
      return data;
      }

   public LoopableConditionalModel.SelectedSensor getSelectedSensor()
      {
      return selectedSensor;
      }

   public LinkModel.LinkedSensor getLinkedSensor()
      {
      return linkedSensor;
      }

   public Boolean getWillReevaluate()
      {
      return willReevaluateConditionAfterBranchCompletes;
      }

   public ElementLocation getLocation()
      {
      return location;
      }

   public Type getType()
      {
      return type;
      }

   public int getDelay()
      {
      return number;
      }

   public int getIterations()
      {
      return number;
      }

   public int getPort()
      {
      return number;
      }

   public String getString()
      {
      return string;
      }

   private String getTypeAsString()
      {
      switch (type)
         {
         case ADD:
            return "ADD";
         case REMOVE:
            return "REMOVE";
         case UP:
            return "UP";
         case DOWN:
            return "DOWN";
         case EXPRESSION_DELAY:
            return "EXPRESSION_DELAY";
         case COUNTER_ITERATIONS:
            return "COUNTER_ITERATIONS";
         case SENSOR_SENSOR:
            return "SENSOR_SENSOR";
         case SENSOR_IF:
            return "SENSOR_IF";
         case SENSOR_ELSE:
            return "SENSOR_ELSE";
         case LINK_DELAY:
            return "LINK_DELAY";
         case LINK_OUTPUT:
            return "LINK_OUTPUT";
         case LINK_SENSOR:
            return "LINK_SENSOR";
         default:
            return "ERROR";
         }
      }

   public String toString()
      {
      StringBuilder sb = new StringBuilder();
      sb.append("Type: ");
      sb.append(type != null ? getTypeAsString() : "NULL");
      sb.append(" | ");

      sb.append("Location: ");
      sb.append(location != null ? location.getParent() : "NULL");
      sb.append("@");
      sb.append(location != null ? location.getIndexInParent() : "NULL");
      if (data != null)
         {
         sb.append(" | ");
         sb.append("Data name: ");
         sb.append(data.getName());
         }
      if (number >= 0)
         {
         sb.append(" | ");
         sb.append("Number: ");
         sb.append(number);
         }
      if (willReevaluateConditionAfterBranchCompletes != null)
         {
         sb.append(" | ");
         sb.append("willReevaluate: ");
         sb.append(willReevaluateConditionAfterBranchCompletes);
         }
      if (selectedSensor != null)
         {
         sb.append(" | ");
         sb.append("Sensor: ");
         sb.append(selectedSensor);
         }
      return sb.toString();
      }
   }
