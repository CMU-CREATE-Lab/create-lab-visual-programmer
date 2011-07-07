package edu.cmu.ri.createlab.sequencebuilder.programelement.view.listcell;

import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

/**
 * <p>
 * <code>ProgramElementListCellRenderer</code> is a {@link ListCellRenderer} for {@link ProgramElementListCellView}s.
 * </p>
 *
 * @author Chris Bartley (bartley@cmu.edu)
 */
public final class ProgramElementListCellRenderer extends JLabel implements ListCellRenderer
   {
   public Component getListCellRendererComponent(final JList list,
                                                 final Object value,
                                                 final int index,
                                                 final boolean isSelected,
                                                 final boolean cellHasFocus)
      {
      final ProgramElementListCellView listCellView = (ProgramElementListCellView)value;
      listCellView.setIsSelected(isSelected);
      listCellView.setIsEnabled(list.isEnabled());
      return listCellView.getComponent();
      }
   }
