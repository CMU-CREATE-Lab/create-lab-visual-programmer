package edu.cmu.ri.createlab.visualprogrammer;

import edu.cmu.ri.createlab.CreateLabConstants;

import java.io.File;
import java.util.PropertyResourceBundle;

/**
 * <p>
 * <code>CreateLabConstants</code> defines various constants common to all CREATE Lab applications.
 * </p>
 *
 * @author Chris Bartley (bartley@cmu.edu)
 */
public class VisualProgrammerConstants
   {
   private static final PropertyResourceBundle RESOURCES = (PropertyResourceBundle)PropertyResourceBundle.getBundle(VisualProgrammerConstants.class.getName());

   public static class FilePaths
      {
      public static final File FORMER_AUDIO_DIR = CreateLabConstants.FilePaths.AUDIO_DIR;
      public static final File DEFAULT_VISUAL_PROGRAMMER_HOME_DIR = new File(CreateLabConstants.FilePaths.CREATE_LAB_HOME_DIR, RESOURCES.getString("file-path.visual-programmer-home-directory-name"));

      public static final String AUDIO_DIRECTORY_NAME = RESOURCES.getString("file-path.audio-directory-name");
      public static final String EXPRESSIONS_DIRECTORY_NAME = RESOURCES.getString("file-path.expressions-directory-name");
      public static final String SEQUENCES_DIRECTORY_NAME = RESOURCES.getString("file-path.sequences-directory-name");
      public static final String ARDUINO_DIRECTORY_NAME = RESOURCES.getString("file-path.arduino-directory-name");

      private FilePaths()
         {
         // private to prevent instantiation
         }
      }

   public static class Urls
      {
      public static final String VISUAL_PROGRAMMER_SOFTWARE_HOME = RESOURCES.getString("url.visual-programmer-software-home");
      public static final String CURRENT_VERSION_NUMBER_URL = RESOURCES.getString("url.version-number");

      private Urls()
         {
         // private to prevent instantiation
         }
      }

   private VisualProgrammerConstants()
      {
      // private to prevent instantiation
      }
   }
