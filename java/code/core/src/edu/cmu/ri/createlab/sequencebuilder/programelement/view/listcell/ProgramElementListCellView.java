package edu.cmu.ri.createlab.sequencebuilder.programelement.view.listcell;

import edu.cmu.ri.createlab.sequencebuilder.programelement.model.ProgramElementModel;
import edu.cmu.ri.createlab.sequencebuilder.programelement.view.ProgramElementView;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public interface ProgramElementListCellView<ModelClass extends ProgramElementModel> extends ProgramElementView<ModelClass>
   {
   void setIsSelected(final boolean isSelected);
   }