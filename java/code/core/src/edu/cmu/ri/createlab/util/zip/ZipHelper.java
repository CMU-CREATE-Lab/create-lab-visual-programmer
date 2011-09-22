package edu.cmu.ri.createlab.util.zip;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.apache.log4j.Logger;

/**
 * <p>
 * <code>ZipHelper</code> helps unzip zip files.
 * </p>
 *
 * @author Chris Bartley (bartley@cmu.edu)
 */
public class ZipHelper
   {
   private static final Logger LOG = Logger.getLogger(ZipHelper.class);

   private static final ZipHelper INSTANCE = new ZipHelper();

   private static final int BUFFER_SIZE = 4096;

   public static ZipHelper getInstance()
      {
      return INSTANCE;
      }

   public static void main(final String[] args)
      {
      // Start by getting the directory containing the jar file in which this class lives.
      final String locationStr = ZipHelper.class.getProtectionDomain().getCodeSource().getLocation().toString();
      if (locationStr != null && locationStr.length() > 6)
         {
         LOG.debug("ZipHelper.main(): parsing location [" + locationStr + "]");
         final int slashPosition = locationStr.indexOf('/');
         final File location = new File(locationStr.substring(slashPosition > 0 ? slashPosition : 0));   // do substring() to chop off the leading file:, leaving us with an absolute path
         LOG.debug("ZipHelper.main(): location [" + location + "]");

         final File parentDirectory = location.isFile() ? location.getParentFile() : location;

         final File audioClipZipFile = new File(parentDirectory, "audio_clips.zip");

         ZipHelper.getInstance().unzip(audioClipZipFile, new File("/Users/chris/audiotest"));
         }
      else
         {
         LOG.error("ExpressionBuilder.unzipAudioFiles(): Failed to obtain the path to this class's jar file [" + locationStr + "]");
         }
      }

   /**
    * Unzips the given <code>sourceZip</code> zip file, saving the contents to the directory specified by
    * <code>destinationDirectory</code>.  This method will create the destination directory if necessary. Existing files
    * will not be overwritten.  This method does nothing if the given files are null, do not exist, or don't denote a
    * zip file and directory, respectively.
    *
    * @return <code>true</code> if the unzip was successful, <code>false</code> otherwise
    */
   public boolean unzip(final File sourceZip, final File destinationDirectory)
      {
      if (sourceZip != null && sourceZip.isFile())
         {
         ZipFile zipFile = null;
         try
            {
            zipFile = new ZipFile(sourceZip);
            return unzip(zipFile, destinationDirectory);
            }
         catch (IOException e)
            {
            LOG.error("ZipHelper.unzip(): IOException while trying to open the zip file [" + sourceZip + "]", e);
            }
         finally
            {
            if (zipFile != null)
               {
               try
                  {
                  zipFile.close();
                  }
               catch (IOException e)
                  {
                  LOG.error("ZipHelper.unzip(): IOException while trying to close the zip file [" + sourceZip + "]", e);
                  }
               }
            }
         }
      else
         {
         LOG.error("ZipHelper.unzip(): Invalid zip file [" + sourceZip + "]");
         }
      return false;
      }

   /**
    * Unzips the given <code>sourceZip</code> zip file, saving the contents to the directory specified by
    * <code>destinationDirectory</code>.  This method will create the destination directory if necessary. Existing files
    * will not be overwritten.  This method does nothing if the given files are null, do not exist, or don't denote a
    * zip file and directory, respectively.  Note that this method does NOT close the zip file when it is done with it.
    *
    * @return <code>true</code> if the unzip was successful, <code>false</code> otherwise
    */
   public boolean unzip(final ZipFile zipFile, final File destinationDirectory)
      {
      if (zipFile != null)
         {
         if (destinationDirectory != null && (destinationDirectory.isDirectory() || destinationDirectory.mkdirs()))
            {
            final Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements())
               {
               final ZipEntry zipEntry = entries.nextElement();

               if (zipEntry != null)
                  {
                  final File destinationFile = new File(destinationDirectory, zipEntry.getName());

                  if (zipEntry.isDirectory())
                     {
                     //noinspection ResultOfMethodCallIgnored
                     destinationFile.mkdirs();
                     }
                  else
                     {
                     copyFileFromZip(zipFile, zipEntry, destinationFile);
                     }
                  }
               }
            return true;
            }
         else
            {
            LOG.error("ZipHelper.install(): Invalid destination directory: [" + destinationDirectory + "]");
            }
         }
      else
         {
         LOG.error("ZipHelper.install(): Zip file cannot be null");
         }
      return false;
      }

   /** Copy a file from the zip to the given <code>destinationFile</code>. */
   private void copyFileFromZip(final ZipFile zipFile, final ZipEntry zipEntry, final File destinationFile)
      {
      BufferedInputStream inputStream = null;
      BufferedOutputStream outputStream = null;
      try
         {
         // set up the input stream
         inputStream = new BufferedInputStream(zipFile.getInputStream(zipEntry));

         // make sure the parent directory exists
         final File parentDirectory = destinationFile.getParentFile();
         if (parentDirectory.isDirectory() || parentDirectory.mkdirs())
            {
            if (destinationFile.exists())
               {
               LOG.info("ZipHelper.copyFileFromZip(): file [" + destinationFile + "] already exists, so I won't overwrite it.");
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

               LOG.info("ZipHelper.copyFileFromZip(): successfully copied file [" + destinationFile + "]");
               }
            }
         else
            {
            LOG.error("ZipHelper.copyFileFromZip(): Failed to create the directory [" + parentDirectory + "]");
            }
         }
      catch (final FileNotFoundException e)
         {
         LOG.error("ZipHelper.copyFileFromZip(): Could not create the output file", e);
         }
      catch (final IOException e)
         {
         LOG.error("ZipHelper.copyFileFromZip(): IOException while reading or writing the file", e);
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
               LOG.error("ZipHelper.copyFileFromZip(): IOException while closing the outputStream");
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
               LOG.error("ZipHelper.copyFileFromZip(): IOException while closing the inputstream");
               }
            }
         }
      }

   private ZipHelper()
      {
      // private to prevent instantiation
      }
   }