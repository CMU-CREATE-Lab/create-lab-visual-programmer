package edu.cmu.ri.createlab.sequencebuilder.programelement.view.dnd;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import edu.cmu.ri.createlab.sequencebuilder.programelement.model.CounterLoopModel;
import edu.cmu.ri.createlab.sequencebuilder.programelement.model.ExpressionModel;
import edu.cmu.ri.createlab.sequencebuilder.programelement.model.ForkModel;
import edu.cmu.ri.createlab.sequencebuilder.programelement.model.LinkModel;
import edu.cmu.ri.createlab.sequencebuilder.programelement.model.LoopableConditionalModel;
import edu.cmu.ri.createlab.sequencebuilder.programelement.model.ProgramElementModel;
import edu.cmu.ri.createlab.sequencebuilder.programelement.model.SavedSequenceModel;

/**
 * <p>
 * <code>ProgramElementModelTransferable</code> is a {@link Transferable} which allows transfer of
 * {@link ProgramElementModel}s.
 * </p>
 *
 * @author Chris Bartley (bartley@cmu.edu)
 */
public final class ProgramElementModelTransferable implements Transferable
   {
   public static final DataFlavor COUNTER_LOOP_MODEL_DATA_FLAVOR = new DataFlavor(CounterLoopModel.class, "CounterLoopModel");
   public static final DataFlavor LOOPABLE_CONDITIONAL_MODEL_DATA_FLAVOR = new DataFlavor(LoopableConditionalModel.class, "LoopableConditionalModel");
   public static final DataFlavor FORK_MODEL_DATA_FLAVOR = new DataFlavor(ForkModel.class, "ForkModel");
   public static final DataFlavor LINK_MODEL_DATA_FLAVOR = new DataFlavor(LinkModel.class, "LinkModel");

   public static final DataFlavor SAVED_SEQUENCE_MODEL_DATA_FLAVOR = new DataFlavor(SavedSequenceModel.class, "SavedSequenceModel");
   public static final DataFlavor EXPRESSION_MODEL_DATA_FLAVOR = new DataFlavor(ExpressionModel.class, "ExpressionModel");

   private static final Set<DataFlavor> CONTAINER_PROGRAM_ELEMENT_DATA_FLAVORS = Collections.unmodifiableSet(new HashSet<DataFlavor>(Arrays.asList(COUNTER_LOOP_MODEL_DATA_FLAVOR, LOOPABLE_CONDITIONAL_MODEL_DATA_FLAVOR, FORK_MODEL_DATA_FLAVOR)));

   @SuppressWarnings("PublicStaticCollectionField")
   public static final Set<DataFlavor> NON_CONTAINER_PROGRAM_ELEMENT_DATA_FLAVORS = Collections.unmodifiableSet(new HashSet<DataFlavor>(Arrays.asList(SAVED_SEQUENCE_MODEL_DATA_FLAVOR, EXPRESSION_MODEL_DATA_FLAVOR, LINK_MODEL_DATA_FLAVOR)));

   @SuppressWarnings("PublicStaticCollectionField")
   public static final Set<DataFlavor> PROGRAM_ELEMENT_DATA_FLAVORS;

   static
      {
      final Set<DataFlavor> programElementDataFlavors = new HashSet<DataFlavor>();
      programElementDataFlavors.addAll(CONTAINER_PROGRAM_ELEMENT_DATA_FLAVORS);
      programElementDataFlavors.addAll(NON_CONTAINER_PROGRAM_ELEMENT_DATA_FLAVORS);
      PROGRAM_ELEMENT_DATA_FLAVORS = Collections.unmodifiableSet(programElementDataFlavors);
      }

   private final ProgramElementModel programElement;
   private final DataFlavor dataFlavor;
   private final DataFlavor[] supportedDataFlavors;

   public ProgramElementModelTransferable(final ProgramElementModel programElement)
      {
      this.programElement = programElement;
      dataFlavor = new DataFlavor(programElement.getClass(), programElement.getClass().getName());
      supportedDataFlavors = new DataFlavor[]{dataFlavor};
      }

   @Override
   public DataFlavor[] getTransferDataFlavors()
      {
      return supportedDataFlavors.clone();
      }

   @Override
   public boolean isDataFlavorSupported(final DataFlavor flavor)
      {
      return dataFlavor.equals(flavor);
      }

   @Override
   public Object getTransferData(final DataFlavor dataFlavor) throws UnsupportedFlavorException
      {
      if (!isDataFlavorSupported(dataFlavor))
         {
         throw new UnsupportedFlavorException(dataFlavor);
         }
      return programElement;
      }
   }
