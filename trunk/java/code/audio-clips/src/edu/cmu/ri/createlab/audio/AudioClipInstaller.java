package edu.cmu.ri.createlab.audio;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.PropertyResourceBundle;
import java.util.Scanner;
import org.apache.log4j.Logger;

/**
 * <p>
 * <code>AudioClipInstaller</code> extracts audio files.  The set of audio files to extract is defined as a
 * comma-delimited list in the value for the <code>audio-clip.filenames</code> property in the
 * <code>AudioClipInstaller.properties</code> file.  That value is a tokenized value, supplied by Ant during the build,
 * and it built up from all files in the <code>/edu/cmu/ri/createlab/audio/clips/</code> package (and sub-packages). I
 * went with this implementation, rather than inspecting the contents of the jar since, when run under Java Web Start,
 * I found that the jar was being re-downloaded every time, just to do the file listing.  Since it's not something that
 * would ever change between builds, it makes more sense to define it as a static property.
 * </p>
 *
 * @author Chris Bartley (bartley@cmu.edu)
 */
public final class AudioClipInstaller
   {
   private static final Logger LOG = Logger.getLogger(AudioClipInstaller.class);

   private static final AudioClipInstaller INSTANCE = new AudioClipInstaller();

   private static final PropertyResourceBundle RESOURCES = (PropertyResourceBundle)PropertyResourceBundle.getBundle(AudioClipInstaller.class.getName());
   private static final int BUFFER_SIZE = 65536;
   private static final String FILENAME_DELIMITER = ",";
   private static final String CLIPS_PATH_PREFIX = "/edu/cmu/ri/createlab/audio/clips/";

   public static AudioClipInstaller getInstance()
      {
      return INSTANCE;
      }

   public static void main(final String[] args)
      {
      AudioClipInstaller.getInstance().install(new File("audio-clips"));
      }

   private AudioClipInstaller()
      {
      // private to prevent instantiation
      }

   public void install(final File destinationDirectory)
      {
      if (destinationDirectory != null)
         {
         // create a scanner to read the comma-delimited list of clip filenames
         final Scanner scanner = new Scanner(RESOURCES.getString("audio-clip.filenames"));
         scanner.useDelimiter(FILENAME_DELIMITER);

         // iterate over all the filenames and install each one
         while (scanner.hasNext())
            {
            final String destinationFileName = scanner.next();
            final File destinationFile = new File(destinationDirectory, destinationFileName);

            if (destinationFile.exists())
               {
               LOG.info("AudioClipInstaller.install(): file [" + destinationFile + "] already exists, so I won't overwrite it.");
               }
            else
               {
               copyFileFromJar(CLIPS_PATH_PREFIX + destinationFileName, destinationFile);
               }
            }
         }
      }

   /** Copy a file from the jar to the given <code>destinationFile</code>. */
   private void copyFileFromJar(final String resourceName, final File destinationFile)
      {
      if (destinationFile.exists())
         {
         LOG.info("AudioClipInstaller.copyFileFromJar(): file [" + destinationFile + "] already exists, so I won't overwrite it.");
         }
      else
         {
         BufferedInputStream inputStream = null;
         BufferedOutputStream outputStream = null;
         try
            {
            // set up the input stream
            inputStream = new BufferedInputStream(getClass().getResourceAsStream(resourceName));

            // make sure the parent directory exists
            final File parentDirectory = destinationFile.getParentFile();
            if (parentDirectory.isDirectory() || parentDirectory.mkdirs())
               {
               // set up the output stream
               outputStream = new BufferedOutputStream(new FileOutputStream(destinationFile));

               final byte[] buffer = new byte[BUFFER_SIZE];
               int bytesRead;
               while ((bytesRead = inputStream.read(buffer)) >= 0)
                  {
                  outputStream.write(buffer, 0, bytesRead);
                  }

               LOG.info("AudioClipInstaller.copyFileFromJar(): successfully copied file [" + destinationFile + "]");
               }
            else
               {
               LOG.error("AudioClipInstaller.copyFileFromJar(): Failed to create the directory [" + parentDirectory + "]");
               }
            }
         catch (final FileNotFoundException e)
            {
            LOG.error("AudioClipInstaller.copyFileFromJar(): Could not create the output file", e);
            }
         catch (final IOException e)
            {
            LOG.error("AudioClipInstaller.copyFileFromJar(): IOException while reading or writing the file", e);
            }
         finally
            {
            if (outputStream != null)
               {
               try
                  {
                  outputStream.close();
                  }
               catch (final IOException e)
                  {
                  // nothing we can really do here, so just log the error
                  LOG.error("AudioClipInstaller.copyFileFromJar(): IOException while closing the outputStream");
                  }
               }
            if (inputStream != null)
               {
               try
                  {
                  inputStream.close();
                  }
               catch (final IOException e)
                  {
                  // nothing we can really do here, so just log the error
                  LOG.error("AudioClipInstaller.copyFileFromJar(): IOException while closing the inputstream");
                  }
               }
            }
         }
      }
   }
