package edu.cmu.ri.createlab.sequencebuilder.programelement.view.dnd;

import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.util.Set;
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
   private final boolean willSupportImportOfProgramElementContainers;

   public ProgramElementDestinationTransferHandler()
      {
      this(true);
      }

   public ProgramElementDestinationTransferHandler(final boolean willSupportImportOfProgramElementContainers)
      {
      this.willSupportImportOfProgramElementContainers = willSupportImportOfProgramElementContainers;
      }

   public boolean canImport(final TransferSupport transferSupport)
      {
      // clear all other displays of insertion location
      ViewEventPublisher.getInstance().publishHideInsertLocationsEvent();

      // we will only support drops (not clipboard paste)
      if (!transferSupport.isDrop())
         {
         return false;
         }

      // make sure the data flavor is one we support
      if (!isDataFlavorSupported(transferSupport))
         {
         return false;
         }

      // show the current insertion highlight
      showInsertLocation(transferSupport.getDropLocation().getDropPoint());

      return true;
      }

   /** Returns an unmodifiable {@link Set} of the supported {@link DataFlavor}s. */
   public Set<DataFlavor> getSupportedDataFlavors()
      {
      return willSupportImportOfProgramElementContainers ?
             ProgramElementModelTransferable.PROGRAM_ELEMENT_DATA_FLAVORS :
             ProgramElementModelTransferable.NON_CONTAINER_PROGRAM_ELEMENT_DATA_FLAVORS;
      }

   private boolean isDataFlavorSupported(final TransferSupport transferSupport)
      {
      for (final DataFlavor dataFlavor : getSupportedDataFlavors())
         {
         if (transferSupport.isDataFlavorSupported(dataFlavor))
            {
            return true;
            }
         }
      return false;
      }

   public boolean importData(final TransferSupport transferSupport)
      {
      if (!transferSupport.isDrop())
         {
         return false;
         }

      // make sure the data flavor is one we support
      if (!isDataFlavorSupported(transferSupport))
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
            // TODO: I don't really like this...
            final ProgramElementModel programElementModel = (ProgramElementModel)transferable.getTransferData(transferable.getTransferDataFlavors()[0]);

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
