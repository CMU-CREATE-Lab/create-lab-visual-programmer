package edu.cmu.ri.createlab.sequencebuilder.export;

/**
 * Created by c3morales on 07-09-14.
 * Method_Manager
 * Class that holds the name of the method to be generated,
 * the instructions, and the delay. Also it verify if the method is
 * repeated.
 */

import java.util.ArrayList;

public class Method_Manager
   {
   private String name;
   private String instruction;
   private int delay;
   private String comment;

   //CONSTRUCTOR
   public Method_Manager(final String n, final String i)
      {
      name = n;
      instruction = i;
      }

   //returns the name of the method
   public String getName()
      {
      return name;
      }

   // to change the name of the method
   public void setName(final String n)
      {
      name = n;
      }

   //to change the instructions
   public void setInstructions(final String i)
      {
      instruction = i;
      }

   //returns the block of instructions
   public String getInstructions()
      {
      return instruction;
      }

   //Returns the value of the delay
   public int getDelay()
      {
      return delay;
      }

   //to change the value of the delay
   public void setDelay(final int d)
      {
      delay = d;
      }

   public String getComment()
      {
      return comment;
      }

   public void setComment(final String c)
      {
      comment = c;
      }

   //Verify if the method to be added exist in the list
   public boolean exist(final ArrayList<Method_Manager> other)
      {
      if (other.size() == 0)
         {
         return false;
         }
      for (final Method_Manager elem : other)
         {
         if (name.equalsIgnoreCase(elem.getName()))
            {
            return true;
            }
         }
      return false;
      }
   }

