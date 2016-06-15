package edu.cmu.ri.createlab.sequencebuilder;

import java.util.Stack;

/**
 * Created by Brandon on 6/10/2016.
 */
public abstract class SequenceActionListener
   {
   Stack<SequenceAction> actions;
   public SequenceActionListener(Stack<SequenceAction> actions) {
      this.actions = actions;
   }
   public abstract void onAction(SequenceAction action);
   public abstract SequenceAction onUndo();
   }
