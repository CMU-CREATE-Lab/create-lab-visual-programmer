package edu.cmu.ri.createlab.sequencebuilder.programelement.view.dnd;

import javax.swing.TransferHandler;
import edu.cmu.ri.createlab.sequencebuilder.programelement.view.ViewEventPublisher;

/**
 * <p>
 * <code>NoDropsAllowedTransferHandler</code> is a simple {@link TransferHandler} which never allows drops.
 * </p>
 *
 * @author Chris Bartley (bartley@cmu.edu)
 */
public class NoDropsAllowedTransferHandler extends TransferHandler
   {
   @Override
   public boolean canImport(final TransferSupport transferSupport)
      {
      // clear all other displays of insertion location
      ViewEventPublisher.getInstance().publishHideInsertLocationsEvent();

      return false;
      }
   }
