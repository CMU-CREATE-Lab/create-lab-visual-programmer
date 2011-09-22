package edu.cmu.ri.createlab.visualprogrammer;

import java.io.File;
import edu.cmu.ri.createlab.CreateLabConstants;

/**
 * <p>
 * <code>CreateLabConstants</code> defines various constants common to all CREATE Lab applications.
 * </p>
 *
 * @author Chris Bartley (bartley@cmu.edu)
 */
public class VisualProgrammerConstants
   {
   public static class FilePaths
      {
      public static final File AUDIO_DIR = CreateLabConstants.FilePaths.AUDIO_DIR;

      private FilePaths()
         {
         // private to prevent instantiation
         }
      }

   private VisualProgrammerConstants()
      {
      // private to prevent instantiation
      }
   }
