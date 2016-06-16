package edu.cmu.ri.createlab.sequencebuilder;

/**
 * Created by Brandon on 6/10/2016.
 */
public class ElementLocation
   {

   public ContainerModel getParent()
      {
      return parent;
      }

   public int getIndexInParent()
      {
      return indexInParent;
      }

   private ContainerModel parent;
   private int indexInParent;

   public ElementLocation(ContainerModel p, int i)
      {
         this.parent = p;
         this.indexInParent = i;
      }
   }
