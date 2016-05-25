package edu.cmu.ri.createlab.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.zip.ZipEntry;
import edu.cmu.ri.createlab.visualprogrammer.PathManager;
import org.jetbrains.annotations.Nullable;
import org.zeroturnaround.zip.ZipEntryCallback;
import org.zeroturnaround.zip.ZipUtil;

/**
 * <p>
 * <code>ZipSave</code>  This class implements methods to read and write directly to zip folders for the Visual Programmer
 * </p>
 *
 * @author Cristina M. Morales (cristina.morales4@upr.edu)
 */
public class ZipSave
   {
   private final Lock lock = new ReentrantLock();
   private final Set<FileEventListener> eventListeners = new HashSet<FileEventListener>();
   private String destination;
   private File projectDirectory;
   private String suffix = ".xml";

   /**
    *
    * @param dest = Location of the folder that will be use. [Sequence or Expressions]
    *
    */
   public ZipSave(Destination dest)
      {
      this.destination = String.valueOf(dest);
      this.projectDirectory = PathManager.getInstance().getProjectDirectory();
      }

   /**
    * like :
    * @param listener
    */
   public void addEventListener(@Nullable final FileEventListener listener)
      {
      lock.lock();  // block until condition holds
      try
         {
         if (listener != null)
            {
            eventListeners.add(listener);
            }
         }
      finally
         {
         lock.unlock();
         }
      }

   public void removeEventListener(@Nullable final FileEventListener listener)
      {
      lock.lock();  // block until condition holds
      try
         {
         if (listener != null)
            {
            eventListeners.remove(listener);
            }
         }
      finally
         {
         lock.unlock();
         }
      }

   public void modifyEventListener(@Nullable final FileEventListener listener)
      {
      lock.lock();  // block until condition holds
      try
         {
         if (listener != null)
            {
            eventListeners.remove(listener);
            }
         }
      finally
         {
         lock.unlock();
         }
      }

   /**
    * Add new file to the zip folder
    * @param fileName = Name of the file
    * @param fileContent = content of the .xml document
    */
   public void addNewFile(String fileName, String fileContent)
      {
      ZipUtil.addEntry(projectDirectory, destination + File.separator + fileName, fileContent.getBytes());

      final Set<String> newFiles = new HashSet<String>();
      newFiles.add(fileName);
      for (final FileEventListener listener : eventListeners)
         {
         listener.handleNewFileEvent(newFiles);
         }
      }

   /**
    * Verify if a .xml document is in the zip folder
    * @param fileName = name of the .xml document
    * @return = true if the .xml document exist in the zip folder
    */
   public boolean exist(String fileName)
      {
      return ZipUtil.containsEntry(projectDirectory, destination + File.separator + fileName);
      }

   /**
    * Modify an .xml document
    * @param fileName = name of the .xml document
    * @param fileContent = new content for the .xml document
    */
   public void modifyFile(String fileName, String fileContent)
      {
      ZipUtil.replaceEntry(projectDirectory, destination + File.separator + fileName, fileContent.getBytes());

      final Set<String> modFiles = new HashSet<String>();
      modFiles.add(fileName);
      for (final FileEventListener listener : eventListeners)
         {
         listener.handleModifiedFileEvent(modFiles);
         }
      }

   /**
    * Delete a .xml from the zip
    * @param fileName = = name of the .xml document
    */
   public void deleteFile(String fileName)
      {
      final Set<String> deleteFiles = new HashSet<String>();
      deleteFiles.add(fileName);
      for (final FileEventListener listener : eventListeners)
         {
         listener.handleDeletedFileEvent(deleteFiles);
         }
      ZipUtil.removeEntry(projectDirectory, destination + File.separator + fileName);
      }

   /**
    * Get content of an .xml document from the zip folder
    * @param fileName = name of the .xml document
    * @return = content of the specified xml document
    * @throws IOException
    */
   public InputStream getFile_InputStream(String fileName) throws IOException
      {
      // convert from text inside zip to byte
      if (!exist(fileName))
         {
         throw new FileNotFoundException();
         }

      final byte[] bytes = ZipUtil.unpackEntry(projectDirectory, destination + File.separator + fileName);
      return new ByteArrayInputStream(bytes);
      }

   /**
    *
    * @param filename = full path of the .xml document
    * @return = file name without path
    */
   private String getPrettyName(String filename)
      {
      return filename.replace(String.valueOf(destination + File.separator), "");
      }

   /**
    * Gets the name of every document inside the zip folder and populates the expression & sequences list
    */
   public void forceRefresh()
      {
      final Set<String> newFiles = new HashSet<String>();
      ZipUtil.iterate(projectDirectory, new ZipEntryCallback()
                      {

                      public void process(InputStream in, ZipEntry zipEntry) throws IOException
                         {
                         if (zipEntry.getName().endsWith(suffix) && zipEntry.getName().startsWith(destination))
                            {
                            String filename = getPrettyName(zipEntry.getName());
                            newFiles.add(filename);
                            }
                         }
                      }
      );

      for (final FileEventListener listener : eventListeners)
         {
         listener.handleNewFileEvent(newFiles);
         }
      }

   public enum Destination
      {
         Sequences, Expressions
      }
   }
