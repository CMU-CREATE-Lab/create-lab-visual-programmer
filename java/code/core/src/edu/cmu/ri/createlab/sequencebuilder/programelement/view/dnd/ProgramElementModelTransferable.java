package edu.cmu.ri.createlab.sequencebuilder.programelement.view.dnd;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import edu.cmu.ri.createlab.sequencebuilder.programelement.model.ProgramElementModel;

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
   public static final DataFlavor PROGRAM_ELEMENT_MODEL_DATA_FLAVOR = new DataFlavor(ProgramElementModel.class, "ProgramElementModel");
   private static final Set<DataFlavor> SUPPORTED_DATA_FLAVORS = Collections.unmodifiableSet(new HashSet<DataFlavor>(Arrays.asList(PROGRAM_ELEMENT_MODEL_DATA_FLAVOR)));

   private final ProgramElementModel programElement;

   public ProgramElementModelTransferable(final ProgramElementModel programElement)
      {
      this.programElement = programElement;
      }

   @Override
   public DataFlavor[] getTransferDataFlavors()
      {
      return SUPPORTED_DATA_FLAVORS.toArray(new DataFlavor[SUPPORTED_DATA_FLAVORS.size()]);
      }

   @Override
   public boolean isDataFlavorSupported(final DataFlavor dataFlavor)
      {
      return SUPPORTED_DATA_FLAVORS.contains(dataFlavor);
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
