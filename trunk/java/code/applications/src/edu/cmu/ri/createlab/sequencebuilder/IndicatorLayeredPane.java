package edu.cmu.ri.createlab.sequencebuilder;

import edu.cmu.ri.createlab.userinterface.util.ImageUtils;

import javax.swing.*;
import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: jcross1
 * Date: 10/6/11
 * Time: 11:13 AM
 * To change this template use File | Settings | File Templates.
 */
public class IndicatorLayeredPane extends JLayeredPane{

    JLabel aboveIndicator = new JLabel(ImageUtils.createImageIcon("/edu/cmu/ri/createlab/sequencebuilder/images/aboveIndicator.png"));
    JLabel belowIndicator = new JLabel(ImageUtils.createImageIcon("/edu/cmu/ri/createlab/sequencebuilder/images/belowIndicator.png"));
    JComponent basePanel;

    IndicatorLayeredPane (final JComponent basePanel){
        super();

        this.basePanel = basePanel;

        this.add(this.basePanel, new Integer(1));
        this.add(aboveIndicator, new Integer(2));
        this.add(belowIndicator, new Integer(3));

        this.setMinimumSize(this.basePanel.getMinimumSize());
        this.setPreferredSize(this.basePanel.getPreferredSize());
        this.setMaximumSize(this.basePanel.getMaximumSize());

        final double height = this.getHeight();
        final double width = this.getWidth();

        final Dimension indDimension = aboveIndicator.getPreferredSize();

        this.basePanel.setBounds(0,0, this.getWidth(), this.getHeight());
        aboveIndicator.setBounds((int)(width/2 - indDimension.getWidth()/2), 5, (int)indDimension.getWidth(), (int)indDimension.getHeight());
        belowIndicator.setBounds((int)(width/2 - indDimension.getWidth()/2), (int)(height - 5 - indDimension.getHeight()), (int)indDimension.getWidth(), (int)indDimension.getHeight());

        setAboveIndicatorVisible(false);
        setBelowIndicatorVisible(false);
    }

     public void alignIndicators(){
         final double height = this.getHeight();
         final double width = this.getWidth();
         final Dimension indDimension = aboveIndicator.getPreferredSize();

         basePanel.setBounds(0,0, this.getWidth(), this.getHeight());
         aboveIndicator.setBounds((int)(width/2 - indDimension.getWidth()/2), 5, (int)indDimension.getWidth(), (int)indDimension.getHeight());
         belowIndicator.setBounds((int)(width/2 - indDimension.getWidth()/2), (int)(height - 5 - indDimension.getHeight()), (int)indDimension.getWidth(), (int)indDimension.getHeight());
     }

     public void setAboveIndicatorVisible(final boolean aboveIndicatorVisible){
         aboveIndicator.setVisible(aboveIndicatorVisible);
     }
    public void setBelowIndicatorVisible(final boolean belowIndicatorVisible){
         belowIndicator.setVisible(belowIndicatorVisible);
     }


      @Override
      public void paintComponent(Graphics g){
          alignIndicators();
          super.paintComponent(g);
      }

}
