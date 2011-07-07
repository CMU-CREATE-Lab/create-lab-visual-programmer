package edu.cmu.ri.createlab.sequencebuilder.programelement.view.standard;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
final class InsertionHighlightArea
   {
   private static final Dimension HIGHLIGHT_DIMENSION = new Dimension(180, 4);
   private final JPanel panel = new JPanel();

   InsertionHighlightArea()
      {
      panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));

      final Component rigidArea = Box.createRigidArea(HIGHLIGHT_DIMENSION);
      rigidArea.setBackground(Color.DARK_GRAY);
      panel.setBackground(Color.BLUE);
      panel.add(rigidArea);
      }

   public void setEnabled(final boolean isEnabled)
      {
      panel.setEnabled(isEnabled);
      }

   public void setVisible(final boolean isVisible)
      {
      panel.setVisible(isVisible);
      }

   public JComponent getComponent()
      {
      return panel;
      }
   }
