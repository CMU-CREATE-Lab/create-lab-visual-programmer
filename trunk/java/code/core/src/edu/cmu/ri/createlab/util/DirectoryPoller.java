package edu.cmu.ri.createlab.util;

import java.io.File;
import java.io.FileFilter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import edu.cmu.ri.createlab.util.thread.DaemonThreadFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * <p>
 * <code>DirectoryPoller</code> polls a directory at some specified interval and fires events to {@link EventListener}s
 * whenever files are created, modified, or deleted.  The poller can be configured with a {@link FileFilter} that that
 * it only watches a particular kind of file.
 * </p>
 *
 * @author Chris Bartley (bartley@cmu.edu)
 */
public final class DirectoryPoller
   {
   public interface EventListener
      {
      void handleNewFileEvent(final Set<File> files);

      void handleModifiedFileEvent(final Set<File> files);

      void handleDeletedFileEvent(final Set<File> files);
      }

   private Timer pollingTimer;

   @NotNull
   private final File directory;

   @Nullable
   private final FileFilter fileFilter;

   private final long delay;

   @NotNull
   private final TimeUnit timeUnit;

   private final Lock dataSynchronizationLock = new ReentrantLock();
   private final HashMap<File, Long> fileModificationTimeMap = new HashMap<File, Long>();

   private final Set<EventListener> eventListeners = new HashSet<EventListener>();
   private ExecutorService executorService = Executors.newCachedThreadPool(new DaemonThreadFactory(this.getClass().getSimpleName()));

   /**
    * Creates a <code>DirectoryPoller</code> which polls the given <code>directory</code> for files passing the given
    * {@link FileFilter}.  Polling is performed at the time interval specified by the given <code>delay</code> and
    * {@link TimeUnit}.
    *
    * If the given <code>fileFilter</code> is <code>null</code>, this class includes all files found in the
    * <code>directory</code>.
    *
    * If the given <code>timeUnit</code> is <code>null</code>, then the <code>delay</code> value is assumed to be in
    * milliseconds.
    *
    * @throws IllegalArgumentException if the given <code>directory</code> is <code>null</code>, is not a directory,
    * or does not exist.
    */
   public DirectoryPoller(@Nullable final File directory,
                          @Nullable final FileFilter fileFilter,
                          final long delay,
                          @Nullable final TimeUnit timeUnit)
      {
      if (directory == null || !directory.isDirectory())
         {
         throw new IllegalArgumentException("The given directory [" + directory + "] either does not exist or is not a directory");
         }
      this.directory = directory;
      this.fileFilter = fileFilter;
      this.delay = delay;
      this.timeUnit = (timeUnit == null) ? TimeUnit.MILLISECONDS : timeUnit;
      }

   public void addEventListener(@Nullable final EventListener listener)
      {
      if (listener != null)
         {
         eventListeners.add(listener);
         }
      }

   public void removeEventListener(@Nullable final EventListener listener)
      {
      if (listener != null)
         {
         eventListeners.remove(listener);
         }
      }

   public void start()
      {
      if (pollingTimer == null)
         {
         final TimerTask task = new DirectoryPollingTimerTask();

         this.pollingTimer = new Timer("AbstractDirectoryPollingListModel_Timer_" + directory.getAbsolutePath(), true);
         this.pollingTimer.scheduleAtFixedRate(task, 0, timeUnit.toMillis(delay));
         }
      }

   public void stop()
      {
      if (pollingTimer != null)
         {
         pollingTimer.cancel();
         pollingTimer = null;
         }
      }

   private class DirectoryPollingTimerTask extends TimerTask
      {
      public void run()
         {
         final Set<File> newFiles = new HashSet<File>();
         final Set<File> modifiedFiles = new HashSet<File>();
         final Set<File> deletedFiles = new HashSet<File>();

         dataSynchronizationLock.lock();
         try
            {
            final HashSet<File> checkedFiles = new HashSet<File>();

            // scan the files and check for modification/addition
            for (final File file : getFileList())
               {
               checkedFiles.add(file);

               final Long fileModificationTime = fileModificationTimeMap.get(file);
               if (fileModificationTime == null)
                  {
                  // new file
                  fileModificationTimeMap.put(file, file.lastModified());
                  newFiles.add(file);
                  }
               else if (fileModificationTime != file.lastModified())
                  {
                  // modified file
                  fileModificationTimeMap.put(file, file.lastModified());
                  modifiedFiles.add(file);
                  }
               }

            // now check for deleted files
            final Set<File> files = new HashSet<File>(fileModificationTimeMap.keySet());
            files.removeAll(checkedFiles);
            for (final File file : files)
               {
               fileModificationTimeMap.remove(file);
               deletedFiles.add(file);
               }
            }
         finally
            {
            dataSynchronizationLock.unlock();
            }

         // notify the handler of new/modified/removed files
         if (!eventListeners.isEmpty() &&
             (!newFiles.isEmpty() || !modifiedFiles.isEmpty() || !deletedFiles.isEmpty()))
            {
            executorService.execute(
                  new Runnable()
                  {
                  public void run()
                     {
                     if (!newFiles.isEmpty())
                        {
                        for (final EventListener listener : eventListeners)
                           {
                           listener.handleNewFileEvent(newFiles);
                           }
                        }
                     if (!modifiedFiles.isEmpty())
                        {
                        for (final EventListener listener : eventListeners)
                           {
                           listener.handleModifiedFileEvent(modifiedFiles);
                           }
                        }
                     if (!deletedFiles.isEmpty())
                        {
                        for (final EventListener listener : eventListeners)
                           {
                           listener.handleDeletedFileEvent(deletedFiles);
                           }
                        }
                     }
                  });
            }
         }

      private File[] getFileList()
         {
         if (fileFilter != null)
            {
            return directory.listFiles(fileFilter);
            }
         return directory.listFiles();
         }
      }
   }
