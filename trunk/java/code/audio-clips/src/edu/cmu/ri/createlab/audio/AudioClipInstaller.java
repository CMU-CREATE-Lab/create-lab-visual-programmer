package edu.cmu.ri.createlab.audio;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.security.CodeSource;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.apache.log4j.Logger;

/**
 * <p>
 * <code>AudioClipInstaller</code> extracts all the audio files contained in the jar which contains this class.
 * </p>
 *
 * @author Chris Bartley (bartley@cmu.edu)
 */
public final class AudioClipInstaller
   {
   private static final Logger LOG = Logger.getLogger(AudioClipInstaller.class);

   private static final AudioClipInstaller INSTANCE = new AudioClipInstaller();

   private static final String CLIPS_PATH = "edu/cmu/ri/createlab/audio/clips/";
   private static final int BUFFER_SIZE = 4096;

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
         final CodeSource src = getClass().getProtectionDomain().getCodeSource();
         if (src != null)
            {
            final URL jar = src.getLocation();
            if (LOG.isDebugEnabled())
               {
               LOG.debug("AudioClipInstaller.install(): src = [" + src + "]");
               LOG.debug("AudioClipInstaller.install(): jar = [" + jar + "]");
               }

            ZipInputStream zipInputStream = null;
            try
               {
               zipInputStream = new ZipInputStream(jar.openStream());
               ZipEntry zipEntry;

               while ((zipEntry = zipInputStream.getNextEntry()) != null)
                  {
                  final String entryName = zipEntry.getName();
                  if (entryName != null)
                     {
                     if (entryName.startsWith(CLIPS_PATH) && entryName.length() > CLIPS_PATH.length())
                        {
                        final String destinationFileName = entryName.substring(CLIPS_PATH.length());
                        final File destinationFile = new File(destinationDirectory, destinationFileName);

                        // assume that if the file contains a dot, then it's an audio file, otherwise it's a directory
                        if (destinationFileName.contains("."))
                           {
                           copyFileFromJar("/" + entryName, destinationFile);
                           }
                        else
                           {
                           //noinspection ResultOfMethodCallIgnored
                           destinationFile.mkdirs();
                           }
                        }
                     }

                  zipInputStream.closeEntry();
                  }
               }
            catch (IOException e)
               {
               LOG.error("IOException while trying to extract the audio files from the jar", e);
               }
            finally
               {
               if (zipInputStream != null)
                  {
                  try
                     {
                     zipInputStream.close();
                     }
                  catch (IOException ignored)
                     {
                     // nothing we can really do here, so just log the error
                     LOG.error("AudioClipInstaller.install(): IOException while closing the zipInputStream");
                     }
                  }
               }
            }
         else
            {
            LOG.error("CodeSource is null!");
            }
         }
      }

   /** Copy a file from the jar to the given <code>destinationFile</code>. */
   private void copyFileFromJar(final String resourceName, final File destinationFile)
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
            if (destinationFile.exists())
               {
               LOG.info("AudioClipInstaller.copyFileFromJar(): file [" + destinationFile + "] already exists, so I won't overwrite it.");
               }
            else
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
