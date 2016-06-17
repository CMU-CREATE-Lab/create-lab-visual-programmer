package edu.cmu.ri.createlab.sequencebuilder;

import org.jdom.Element;

/**
 * Created by Brandon on 6/10/2016.
 *
 */
public class SequenceAction
   {

   public enum Type
      {
         ADD, REMOVE, UP, DOWN, EXPRESSION_DELAY
      }

   private Type type;
   private ElementLocation location;
   private int delay;
   private Element data;

   public SequenceAction(Type type, ElementLocation location, Element data, int delay)
      {
      this.type = type;
      this.location = location;
      this.data = data;
      this.delay = delay;
      }

   public SequenceAction(Type type, ElementLocation location, Element data)
      {
      this(type, location, data, -1);
      }

   public SequenceAction(Type type, ElementLocation location)
      {
      this(type, location, null, -1);
      }

   public SequenceAction(Type type, ElementLocation location, int delay)
      {
      this(type, location, null, delay);
      }

   public Element getData()
      {
      return data;
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
      return delay;
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
         sb.append("");
         }
      return sb.toString();
      }
   }
