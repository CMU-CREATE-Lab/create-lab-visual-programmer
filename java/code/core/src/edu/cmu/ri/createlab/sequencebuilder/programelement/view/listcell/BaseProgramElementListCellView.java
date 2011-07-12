package edu.cmu.ri.createlab.sequencebuilder.programelement.view.listcell;

import java.awt.Color;
import java.awt.Point;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import edu.cmu.ri.createlab.sequencebuilder.ContainerView;
import edu.cmu.ri.createlab.sequencebuilder.programelement.model.ProgramElementModel;
import edu.cmu.ri.createlab.sequencebuilder.programelement.view.BaseProgramElementView;
import edu.cmu.ri.createlab.sequencebuilder.programelement.view.ProgramElementView;
import edu.cmu.ri.createlab.userinterface.GUIConstants;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * <p>
 * <code>BaseProgramElementListCellView</code> provides base functionality for {@link ProgramElementView}s for display
 * in a list.
 * </p>
 *
 * @author Chris Bartley (bartley@cmu.edu)
 */
abstract class BaseProgramElementListCellView<ModelClass extends ProgramElementModel> extends BaseProgramElementView<ModelClass> implements ProgramElementListCellView<ModelClass>
   {

   public JLabel label = new JLabel();

   protected BaseProgramElementListCellView(@NotNull final ContainerView containerView, @NotNull final ModelClass programElementModel)
      {
      super(containerView, programElementModel);
      label.setText(getName());
      label.setToolTipText(getName());
      label.setBackground(Color.WHITE);
      label.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
      label.setOpaque(true);
      }

   @Override
   @NotNull
   public final JComponent getComponent()
      {
      return label;
      }

   @Override
   public void setIsSelected(final boolean isSelected)
      {
      if (isSelected)
         {
         label.setFont(GUIConstants.FONT_NORMAL);
         label.setBackground(Color.BLUE);
         label.setForeground(Color.WHITE);
         }
      else
         {
         label.setFont(GUIConstants.FONT_NORMAL);
         label.setBackground(Color.WHITE);
         label.setForeground(Color.BLACK);
         }
      }

   @Override
   public final void setIsEnabled(final boolean isEnabled)
      {
      label.setEnabled(isEnabled);
      }

   @SuppressWarnings({"NoopMethodInAbstractClass"})
   @Override
   public final void showInsertLocationBefore()
      {
      // nothing to do--this is handled for us by the JList containing this element
      }

   @SuppressWarnings({"NoopMethodInAbstractClass"})
   @Override
   public final void showInsertLocationAfter()
      {
      // nothing to do--this is handled for us by the JList containing this element
      }

   @SuppressWarnings({"NoopMethodInAbstractClass"})
   @Override
   public final void showInsertLocation(@Nullable final Point dropPoint)
      {
      // nothing to do--this is handled for us by the JList containing this element
      }

   @SuppressWarnings({"NoopMethodInAbstractClass"})
   @Override
   public void hideInsertLocations()
      {
      // nothing to do--this is handled for us by the JList containing this element
      }

   @Override
   public final boolean isInsertLocationBefore(@Nullable final Point dropPoint)
      {
      return dropPoint != null && dropPoint.getY() <= label.getSize().getHeight() / 2;
      }

   @SuppressWarnings({"NoopMethodInAbstractClass"})
   @Override
   public final void resetViewForSequenceExecution()
      {
      // nothing to do
      }

   /** Allows subclasses to set the displayed icon. If the icon is <code>null</code>, nothing is displayed. */
   protected final void setIcon(@Nullable final Icon icon)
      {
      label.setIcon(icon);
      }

   /**
    * Allows subclasses to set the displayed text. If the value of text is <code>null</code> or the empty string,
    * nothing is displayed.
    */
   protected final void setText(@Nullable final String text)
      {
      label.setText(text);
      }
   }
