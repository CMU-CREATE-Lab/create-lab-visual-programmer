package edu.cmu.ri.createlab.sequencebuilder.programelement.view.listcell;

import edu.cmu.ri.createlab.sequencebuilder.ContainerView;
import edu.cmu.ri.createlab.sequencebuilder.programelement.model.LoopableConditionalModel;
import edu.cmu.ri.createlab.userinterface.GUIConstants;
import edu.cmu.ri.createlab.userinterface.util.ImageUtils;
import org.jdom.Parent;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

/**
 * <p>
 * <code>ExpressionListCellView</code> helps render a {@link LoopableConditionalModel} as a cell in a list.
 * </p>
 *
 * @author Chris Bartley (bartley@cmu.edu)
 */
public final class LoopableConditionalListCellView extends BaseProgramElementListCellView<LoopableConditionalModel>
   {
   public LoopableConditionalListCellView(@NotNull final ContainerView containerView, @NotNull final LoopableConditionalModel model)
      {
      super(containerView, model);
      setIcon(ImageUtils.createImageIcon("/edu/cmu/ri/createlab/sequencebuilder/programelement/view/images/loopable-conditional-icon.png"));
      setText("Sensor");
      label.setVerticalTextPosition(JLabel.BOTTOM);
      label.setHorizontalTextPosition(JLabel.CENTER);
      }

     @Override
    public final void setIsSelected(final boolean isSelected)
      {
      if (isSelected)
         {
         label.setFont(GUIConstants.FONT_NORMAL);
         label.setBackground(Color.WHITE);
         label.setForeground(Color.BLACK);
         }
      else
         {
         label.setFont(GUIConstants.FONT_NORMAL);
         label.setBackground(Color.WHITE);
         label.setForeground(Color.BLACK);
         }
      }

   }
