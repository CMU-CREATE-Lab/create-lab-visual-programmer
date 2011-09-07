package edu.cmu.ri.createlab.expressionbuilder.widgets;

import edu.cmu.ri.createlab.userinterface.util.ImageUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;

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
    private ImageIcon face = ImageUtils.createImageIcon("/edu/cmu/ri/createlab/expressionbuilder/widgets/images/servo_dial_face.png");
    private  Icon rotatedFace;

    public ServoDial(double angle){
        panel = new JPanel();
        panel.setBackground(Color.black);
        panel.setMinimumSize(new Dimension(25,45));
        panel.setPreferredSize(new Dimension(25, 45));
        panel.setMaximumSize(new Dimension(25, 45));

        Icon rotatedFace = new RotatedIcon(face, value);
        panel.add(new JLabel(rotatedFace));

    }

    public double getValue(){
        return value;
    }

    public void setValue(double ang){
        value = ang;
        panel.removeAll();
        rotatedFace = new RotatedIcon(face, value);
        panel.add(new JLabel(rotatedFace));
        panel.revalidate();
        panel.repaint();
    }

    public Component getComponent(){
       return panel;
    }


    //Modified from: http://tips4java.wordpress.com/2009/04/06/rotated-icon/
    public class RotatedIcon implements Icon
    {
        private Icon icon;
        private double angle;
        public RotatedIcon(Icon icon, double angle)
        {
            this.icon = icon;
            this.angle = angle;
        }


        public double setAngle()
        {
            return angle;
        }

        /**
         *  Gets the width of this icon.
         *
         *  @return the width of the icon in pixels.
         */
        @Override
        public int getIconWidth()
        {
             return icon.getIconWidth();
        }

        /**
         *  Gets the height of this icon.
         *
         *  @return the height of the icon in pixels.
         */
        @Override
        public int getIconHeight()
        {
            return icon.getIconHeight();
        }

       /**
        *  Paint the icons of this compound icon at the specified location
        *
        *  @param c The component on which the icon is painted
        *  @param g the graphics context
        *  @param x the X coordinate of the icon's top-left corner
        *  @param y the Y coordinate of the icon's top-left corner
        */
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y)
        {
            Graphics2D g2 = (Graphics2D)g.create();

            int cWidth = icon.getIconWidth() / 2;
            int cHeight = icon.getIconHeight() / 2;

            Rectangle r = new Rectangle(x, y, icon.getIconWidth(), icon.getIconHeight());
            g2.setClip(r);
            AffineTransform original = g2.getTransform();
            AffineTransform at = new AffineTransform();
            at.concatenate(original);
            at.rotate(Math.toRadians(angle - 90), x + cWidth, y + cHeight);
            g2.setTransform(at);
            icon.paintIcon(c, g2, x, y);
            g2.setTransform(original);

        }
    }



}
