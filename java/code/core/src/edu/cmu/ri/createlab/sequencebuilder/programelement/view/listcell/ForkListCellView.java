package edu.cmu.ri.createlab.sequencebuilder.programelement.view.listcell;

import javax.swing.JLabel;
import edu.cmu.ri.createlab.sequencebuilder.ContainerView;
import edu.cmu.ri.createlab.sequencebuilder.programelement.model.ForkModel;
import edu.cmu.ri.createlab.userinterface.util.ImageUtils;
import org.jetbrains.annotations.NotNull;

/**
 * Created by Brandon Price (baprice@andrew.cmu.edu) on 6/1/2016.
 *
 */
public class ForkListCellView  extends BaseProgramElementListCellView<ForkModel>
   {
   public ForkListCellView(@NotNull final ContainerView containerView, @NotNull final ForkModel programElementModel)
      {
      super(containerView, programElementModel);
      setIcon(ImageUtils.createImageIcon("/edu/cmu/ri/createlab/sequencebuilder/programelement/view/images/threading-structure-icon.png"));
      setText("Do Both");
      label.setVerticalTextPosition(JLabel.BOTTOM);
      label.setHorizontalTextPosition(JLabel.CENTER);
      }
   }
