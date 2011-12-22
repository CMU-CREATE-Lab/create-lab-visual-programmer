package edu.cmu.ri.createlab.sequencebuilder.programelement.view.standard;

import edu.cmu.ri.createlab.userinterface.util.ImageUtils;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import javax.swing.*;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
final public class InsertionHighlightArea
   {
   private static final Dimension HIGHLIGHT_DIMENSION = new Dimension(180, 40);
   private final JPanel panel = new JPanel();

  public InsertionHighlightArea()
      {
      panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

      String arrowStyle = "/edu/cmu/ri/createlab/sequencebuilder/programelement/view/images/purpleArrow.png";
      String blockStyle = "/edu/cmu/ri/createlab/sequencebuilder/programelement/view/images/addBlockPurple.png";
      String panelStyle = "purpleElement";

      JLabel spacerArrow = new JLabel(ImageUtils.createImageIcon(arrowStyle));
      JLabel spacerBlock = new JLabel(ImageUtils.createImageIcon(blockStyle));

      spacerArrow.setAlignmentX((float)0.5);
      spacerBlock.setAlignmentX((float)0.5);

      panel.setName(panelStyle);
      panel.add(spacerBlock);
      panel.add(spacerArrow);
      }

     public InsertionHighlightArea(boolean isInsideLoop)
      {
      panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

      String arrowStyle = isInsideLoop ? "/edu/cmu/ri/createlab/sequencebuilder/programelement/view/images/orangeArrow.png" : "/edu/cmu/ri/createlab/sequencebuilder/programelement/view/images/purpleArrow.png";
      String blockStyle = isInsideLoop ? "/edu/cmu/ri/createlab/sequencebuilder/programelement/view/images/addBlockOrange.png" : "/edu/cmu/ri/createlab/sequencebuilder/programelement/view/images/addBlockPurple.png";
      String panelStyle = isInsideLoop ? "orangeElement" : "purpleElement";

      JLabel spacerArrow = new JLabel(ImageUtils.createImageIcon(arrowStyle));
      JLabel spacerBlock = new JLabel(ImageUtils.createImageIcon(blockStyle));

      spacerArrow.setAlignmentX((float)0.5);
      spacerBlock.setAlignmentX((float)0.5);

      panel.setName(panelStyle);
      panel.add(spacerBlock);
      panel.add(spacerArrow);
      }


   public void setEnabled(final boolean isEnabled)
      {
      panel.setEnabled(isEnabled);
      }

   public void setVisible(final boolean isVisible)
      {
      panel.setVisible(isVisible);
      //panel.setOpaque(isVisible);
      //panel.repaint();
      }

   public JComponent getComponent()
      {
      return panel;
      }
   }
