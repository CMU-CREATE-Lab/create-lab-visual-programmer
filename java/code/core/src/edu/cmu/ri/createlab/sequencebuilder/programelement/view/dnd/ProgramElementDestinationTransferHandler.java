package edu.cmu.ri.createlab.sequencebuilder.programelement.view.dnd;

import java.awt.Point;
import java.awt.datatransfer.Transferable;
import javax.swing.SwingWorker;
import javax.swing.TransferHandler;
import edu.cmu.ri.createlab.sequencebuilder.programelement.model.ProgramElementModel;
import edu.cmu.ri.createlab.sequencebuilder.programelement.view.ViewEventPublisher;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

/**
 * <p>
 * <code>ProgramElementDestinationTransferHandler</code> is a {@link TransferHandler} for handling drops of
 * {@link ProgramElementModel}s.
 * </p>
 *
 * @author Chris Bartley (bartley@cmu.edu)
 */
@SuppressWarnings({"UseOfSystemOutOrSystemErr"})
public abstract class ProgramElementDestinationTransferHandler extends TransferHandler
   {
   private static final Logger LOG = Logger.getLogger(ProgramElementDestinationTransferHandler.class);

   public boolean canImport(final TransferSupport transferSupport)
      {
      // we will only support drops (not clipboard paste)
      if (!transferSupport.isDrop())
         {
         return false;
         }

      // we only import ProgramElements
      if (!transferSupport.isDataFlavorSupported(ProgramElementModelTransferable.PROGRAM_ELEMENT_MODEL_DATA_FLAVOR))
         {
         return false;
         }

      // clear all other displays of insertion location
      ViewEventPublisher.getInstance().publishHideInsertLocationsEvent();

      // show the current insertion highlight
      showInsertLocation(transferSupport.getDropLocation().getDropPoint());

      return true;
      }

   public boolean importData(final TransferSupport transferSupport)
      {
      if (!transferSupport.isDrop())
         {
         return false;
         }

      // Check the data flavor
      if (!transferSupport.isDataFlavorSupported(ProgramElementModelTransferable.PROGRAM_ELEMENT_MODEL_DATA_FLAVOR))
         {
         LOG.debug("ProgramElementDestinationTransferHandler.importData(): this kind of drop is not supported");
         return false;
         }

      // Get the data for the item that is being dropped.
      final Transferable transferable = transferSupport.getTransferable();
      if (transferable != null)
         {
         try
            {
            final ProgramElementModel programElementModel = (ProgramElementModel)transferable.getTransferData(ProgramElementModelTransferable.PROGRAM_ELEMENT_MODEL_DATA_FLAVOR);

            if (programElementModel != null)
               {
               final SwingWorker<Object, Object> worker =
                     new SwingWorker<Object, Object>()
                     {
                     @Override
                     protected Object doInBackground() throws Exception
                        {
                        performImport(programElementModel, transferSupport.getDropLocation().getDropPoint());

                        return null;
                        }
                     };
               worker.execute();
               return true;
               }
            }
         catch (final Exception e)
            {
            LOG.error("ProgramElementDestinationTransferHandler.importData(): Exception while trying to get the transfer data", e);
            }
         }
      return false;
      }

   /**
    * Shows the current insert location for the component accepting the drop, based on the given {@link Point}.   This
    * executes in the Swing thread.
    */
   protected abstract void showInsertLocation(final Point dropPoint);

   /**
    * Performs the import of the given {@link ProgramElementModel} based on the given {@link Point drop point}.  This
    * executes in a worker thread.
    */
   protected abstract void performImport(@NotNull final ProgramElementModel model, @NotNull final Point dropPoint);
   }
