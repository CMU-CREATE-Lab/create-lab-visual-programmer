package edu.cmu.ri.createlab.visualprogrammer.lookandfeel;

import javax.swing.UIManager;
import javax.swing.plaf.synth.SynthLookAndFeel;
import java.awt.*;

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
          setNimbusColorDefaults();
          try {
              for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                  if ("Nimbus".equals(info.getName())) {
                      UIManager.setLookAndFeel(info.getClassName());
                      break;
                  }
              }
          } catch (Exception e) {

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

   private void setNimbusColorDefaults(){
       //UIManager.put("nimbusBase", new Color(255,255,255));
       //UIManager.put("nimbusBlueGrey", new Color(255,255,255));
       //UIManager.put("control", new Color(214,217,223));
   }



   }
