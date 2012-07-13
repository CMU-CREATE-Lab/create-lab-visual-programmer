package edu.cmu.ri.createlab.visualprogrammer.lookandfeel;

import javax.swing.UIManager;
import javax.swing.plaf.synth.SynthLookAndFeel;

/**
 * <p>
 * <code>VisualProgrammerLookAndFeelLoader</code> is a helper class for loading the Visual Programmer look and feel.
 * </p>
 *
 * @author Chris Bartley (bartley@cmu.edu)
 */
public final class VisualProgrammerLookAndFeelLoader
   {
   private static final VisualProgrammerLookAndFeelLoader INSTANCE = new VisualProgrammerLookAndFeelLoader();

   public static VisualProgrammerLookAndFeelLoader getInstance()
      {
      return INSTANCE;
      }

   private VisualProgrammerLookAndFeelLoader()
      {
      // private to prevent instantiation
      }

   public void loadLookAndFeel()
      {

              // If Nimbus is not available, you can set the GUI to another look and feel.try
              try {
                  final SynthLookAndFeel lookAndFeel = new SynthLookAndFeel();
                  lookAndFeel.load(VisualProgrammerLookAndFeelLoader.class.getResourceAsStream("/edu/cmu/ri/createlab/visualprogrammer/lookandfeel/VisualProgrammerLookAndFeel.xml"), VisualProgrammerLookAndFeelLoader.class);
                  UIManager.setLookAndFeel(lookAndFeel);
              }
              catch (Exception ex)
              {
                  ex.printStackTrace();
              }

      }
   }
