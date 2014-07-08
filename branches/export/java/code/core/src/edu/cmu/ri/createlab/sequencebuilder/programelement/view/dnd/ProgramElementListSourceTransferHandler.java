package edu.cmu.ri.createlab.sequencebuilder.programelement.view.dnd;

import java.awt.datatransfer.Transferable;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.TransferHandler;
import edu.cmu.ri.createlab.sequencebuilder.programelement.model.ProgramElementModel;
import edu.cmu.ri.createlab.sequencebuilder.programelement.view.ProgramElementView;
import edu.cmu.ri.createlab.sequencebuilder.programelement.view.ViewEventPublisher;

/**
 * <p>
 * <code>ProgramElementListSourceTransferHandler</code> is a {@link TransferHandler} functionality for all
 * {@link JList}s which need to act as a source for drag-and-drop of {@link ProgramElementModel}s.
 * </p>
 *
 * @author Chris Bartley (bartley@cmu.edu)
 */
@SuppressWarnings({"UseOfSystemOutOrSystemErr"})
public final class ProgramElementListSourceTransferHandler extends TransferHandler
   {
   @Override
   public final int getSourceActions(final JComponent c)
      {
      return TransferHandler.COPY;
      }

   @Override
   protected Transferable createTransferable(final JComponent c)
      {
      final JList theList = (JList)c;
      final Object[] values = theList.getSelectedValues();

      // the JList contains ProgramElementView instances
      final ProgramElementView view = (ProgramElementView)values[0];

      // get the model from the view, create a copy of it, and then create the transferable
      return new ProgramElementModelTransferable(view.getProgramElementModel().createCopy());
      }

   @Override
   protected void exportDone(final JComponent jComponent, final Transferable transferable, final int i)
      {
      ViewEventPublisher.getInstance().publishHideInsertLocationsEvent();
      }
   }
