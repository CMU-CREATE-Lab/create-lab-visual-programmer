package edu.cmu.ri.createlab.sequencebuilder.programelement.view.listcell;

import javax.swing.JLabel;
import edu.cmu.ri.createlab.sequencebuilder.ContainerView;
import edu.cmu.ri.createlab.sequencebuilder.programelement.model.LinkModel;
import edu.cmu.ri.createlab.userinterface.util.ImageUtils;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

/**
 * Created by Brandon on 6/24/2016.
 *
 */
public class LinkListCellView extends BaseProgramElementListCellView<LinkModel>
   {
   private static final Logger LOG = Logger.getLogger(LinkListCellView.class);

   public LinkListCellView(@NotNull final ContainerView containerView, @NotNull final LinkModel programElementModel)
      {
      super(containerView, programElementModel);
      //setIcon(ImageUtils.createImageIcon("/edu/cmu/ri/createlab/sequencebuilder/programelement/view/images/something.png"));
      setText("Link");
      label.setVerticalTextPosition(JLabel.BOTTOM);
      label.setHorizontalTextPosition(JLabel.CENTER);
      }
   }
