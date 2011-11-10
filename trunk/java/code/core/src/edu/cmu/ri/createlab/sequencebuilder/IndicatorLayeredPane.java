package edu.cmu.ri.createlab.sequencebuilder;

/**
 * Created by IntelliJ IDEA.
 * User: jcross1
 * Date: 10/6/11
 * Time: 2:42 PM
 * To change this template use File | Settings | File Templates.
 */
import edu.cmu.ri.createlab.userinterface.util.ImageUtils;

import javax.swing.*;
import java.awt.*;

final class IndicatorLayeredPane extends JLayeredPane{

    JLabel aboveIndicator = new JLabel(ImageUtils.createImageIcon("/edu/cmu/ri/createlab/sequencebuilder/images/aboveIndicator.png"));
    JLabel belowIndicator = new JLabel(ImageUtils.createImageIcon("/edu/cmu/ri/createlab/sequencebuilder/images/belowIndicator.png"));

    int scrollWidth = 10; //Adjust to match pixel width of scrollbar
    JComponent basePanel;

    IndicatorLayeredPane (final JComponent basePanel){
        super();

        this.basePanel = basePanel;

        this.add(this.basePanel, new Integer(0));
        this.add(aboveIndicator, new Integer(1));
        this.add(belowIndicator, new Integer(2));

        this.setMinimumSize(new Dimension((int)this.basePanel.getMinimumSize().getWidth(), (int)this.basePanel.getMinimumSize().getHeight()));
        this.setPreferredSize(new Dimension((int)this.basePanel.getPreferredSize().getWidth() , (int)this.basePanel.getPreferredSize().getHeight()));
        this.setMaximumSize(new Dimension((int)this.basePanel.getMaximumSize().getWidth(), (int)this.basePanel.getMaximumSize().getHeight()));


        alignIndicators();

        setAboveIndicatorVisible(false);
        setBelowIndicatorVisible(false);
    }

     public void alignIndicators(){
         revalidate();
         final double height = this.getHeight();
         final double width = this.getWidth();
         final Dimension indDimension = aboveIndicator.getPreferredSize();

        this.setMinimumSize(new Dimension((int)this.basePanel.getMinimumSize().getWidth() , (int)this.basePanel.getMinimumSize().getHeight()));
        this.setPreferredSize(new Dimension((int)this.basePanel.getPreferredSize().getWidth()  , (int)this.basePanel.getPreferredSize().getHeight()));
        this.setMaximumSize(new Dimension((int)this.basePanel.getMaximumSize().getWidth() , (int)this.basePanel.getMaximumSize().getHeight()));

         basePanel.setBounds(0,0, this.getWidth(), this.getHeight());
         aboveIndicator.setBounds((int)((width - scrollWidth)/2 - indDimension.getWidth()/2), 5, (int)indDimension.getWidth(), (int)indDimension.getHeight());
         belowIndicator.setBounds((int)((width - scrollWidth)/2 - indDimension.getWidth()/2), (int)(height - 5 - indDimension.getHeight()), (int)indDimension.getWidth(), (int)indDimension.getHeight());

     }

     public void setAboveIndicatorVisible(final boolean aboveIndicatorVisible){
         alignIndicators();
         aboveIndicator.setVisible(aboveIndicatorVisible);
     }
    public void setBelowIndicatorVisible(final boolean belowIndicatorVisible){
        alignIndicators();
        belowIndicator.setVisible(belowIndicatorVisible);
     }

      @Override
      public void paintComponent(Graphics g){
          alignIndicators();
          super.paintComponent(g);
      }

}
