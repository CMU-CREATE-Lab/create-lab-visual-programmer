package edu.cmu.ri.createlab.expressionbuilder.widgets;

import edu.cmu.ri.createlab.userinterface.util.ImageUtils;
import edu.cmu.ri.createlab.userinterface.util.SwingUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;

/**
 * Created by IntelliJ IDEA.
 * User: jcross1
 * Date: 9/7/11
 * Time: 3:07 PM
 * To change this template use File | Settings | File Templates.
 */
public class ServoDial {
    private double value;
    private JPanel panel;
    private Dial dial = new Dial(0);

    public ServoDial(double angle){
        panel = new JPanel();
        panel.setBackground(Color.black);
        panel.setMinimumSize(new Dimension(21,35));
        panel.setPreferredSize(new Dimension(21,35));
        panel.setMaximumSize(new Dimension(21,35));

        this.value = angle;

        dial.setValue(value);

        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.NONE;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridheight = 1;
        gbc.gridwidth = 1;
        gbc.weighty = 1.0;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(1, 0, 0, 0);
        gbc.anchor = GridBagConstraints.PAGE_START;
        panel.add(dial,gbc);
    }

    public double getValue(){
        return value;
    }

    public void setValue(double ang){
        value = ang;
        panel.removeAll();

        //TODO: Make the Dial reusable, instead of needing to make new with each change.
        //TODO: Incorperate properly with Swing and Non-swing thread as appropriate

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.NONE;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridheight = 1;
        gbc.gridwidth = 1;
        gbc.weighty = 1.0;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(1, 0, 0, 0);
        gbc.anchor = GridBagConstraints.PAGE_START;
        panel.add(new Dial(value),gbc);
        panel.revalidate();
        panel.repaint();
    }

    public Component getComponent(){
       return panel;
    }


  public class Dial extends JPanel {

      private Ellipse2D.Double circle = new Ellipse2D.Double(1, 1, 17, 17);
      private Ellipse2D.Double inner_circle = new Ellipse2D.Double(7, 7, 5, 5);
      private Line2D.Double indicator = new Line2D.Double(9, 9, 2, 9);
      private double value;
      private RenderingHints renderHints = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      public Dial(double value)
      {
          super();
          this.value = value;
          this.setMinimumSize(new Dimension(19,19));
          this.setPreferredSize(new Dimension(19,19));
          this.setBackground(Color.black);

          renderHints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);


          indicator.setLine(9.5, 9.5, 9.5 + 7*Math.cos(Math.toRadians(value+180)), 9.5+7*Math.sin(Math.toRadians(value+180)));

      }

      public void paintComponent(Graphics g) {
        clear(g);
        Graphics2D g2d = (Graphics2D)g;
        g2d.setRenderingHints(renderHints);
        g2d.setColor(Color.white);
        g2d.fill(circle);
        g2d.setColor(new Color(255, 130, 28));
        g2d.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2d.draw(indicator);
        g2d.setColor(Color.black);
        g2d.fill(inner_circle);
      }

      public void setValue(double value)
      {
        //this.value = value;
        //indicator.setLine(11,11, 11 + 9*Math.sin(Math.toRadians(value-180)), 11+9*Math.cos(Math.toRadians(value-180)));
        //this.repaint();
      }

      public double getValue ()
      {
        return value;
      }

      protected void clear(Graphics g) {
        super.paintComponent(g);
      }

}

}
