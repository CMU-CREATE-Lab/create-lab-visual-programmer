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
         ADD, REMOVE, UP, DOWN, MODIFIED
      }

   private Type type;
   private ElementLocation location;
   private Element data;

   public SequenceAction(Type type, ElementLocation location, Element data)
      {
      this.type = type;
      this.location = location;
      this.data = data;
      }

   public SequenceAction(Type type, ElementLocation location)
      {
      this(type, location, null);
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

   private String getTypeAsString()
      {
      switch (type)
         {
         case ADD: return "ADD";
         case REMOVE: return "REMOVE";
         case UP: return "UP";
         case DOWN: return "DOWN";
         case MODIFIED: return "MODIFIED";
         default: return "ERROR";
         }
      }

   public String toString()
      {
      StringBuilder sb = new StringBuilder();
      sb.append("Type: ");
      sb.append(type != null ? getTypeAsString(): "NULL");
      sb.append(" | ");

      sb.append("Location: ");
      sb.append(location != null ? location.getParent(): "NULL");
      sb.append("@");
      sb.append(location != null ? location.getIndexInParent(): "NULL");
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
