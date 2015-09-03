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
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * <p>
 * <code>DirectoryPoller</code> polls a directory at some specified interval and fires events to {@link EventListener}s
 * whenever files are created, modified, or deleted.  The poller can be configured with a {@link FileFilter} so that
 * it only watches a particular kind of file.
 * </p>
 *
 * @author Chris Bartley (bartley@cmu.edu)
 */
public final class DirectoryPoller
   {
   private static final Logger LOG = Logger.getLogger(DirectoryPoller.class);

   public interface EventListener
      {
      void handleNewFileEvent(@NotNull final Set<File> files);

      void handleModifiedFileEvent(@NotNull final Set<File> files);

      void handleDeletedFileEvent(@NotNull final Set<File> files);
      }

   private Timer pollingTimer;

   @NotNull
   private final FileProvider directoryProvider;

   @Nullable
   private final FileFilter fileFilter;

   private final long delay;

   @NotNull
   private final TimeUnit timeUnit;

   private final Lock lock = new ReentrantLock();
   private final HashMap<File, Long> fileModificationTimeMap = new HashMap<File, Long>();

   private final Set<EventListener> eventListeners = new HashSet<EventListener>();
   private ExecutorService executorService = Executors.newCachedThreadPool(new DaemonThreadFactory(this.getClass().getSimpleName()));

   /**
    * Creates a <code>DirectoryPoller</code> which polls the directory returned by the given
    * <code>directoryProvider</code> for files passing the given {@link FileFilter}.  Polling is performed at the time
    * interval specified by the given <code>delay</code> and {@link TimeUnit}.
    *
    * If the given <code>fileFilter</code> is <code>null</code>, this class includes all files found in the directory
    * returned by the <code>directoryProvider</code>.
    *
    * If the given <code>timeUnit</code> is <code>null</code>, then the <code>delay</code> value is assumed to be in
    * milliseconds.  This constructor ensures that the delay is a positive number.
    *
    * The name field is used for naming the polling thread.  If the name is <code>null</code> or empty, then the
    * default thread name is used instead.
    *
    * @throws NullPointerException if the given <code>directoryProvider</code> is <code>null</code>.
    */
   public DirectoryPoller(@SuppressWarnings("NullableProblems") @Nullable final FileProvider directoryProvider,
                          @Nullable final FileFilter fileFilter,
                          final long delay,
                          @Nullable final TimeUnit timeUnit)
      {
      if (directoryProvider == null)
         {
         throw new NullPointerException("The given FileProvider [" + directoryProvider + "] cannot be null");
         }
      this.directoryProvider = directoryProvider;
      this.fileFilter = fileFilter;
      this.delay = Math.max(1, delay);
      this.timeUnit = (timeUnit == null) ? TimeUnit.MILLISECONDS : timeUnit;
      }

   public void addEventListener(@Nullable final EventListener listener)
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

   public void removeEventListener(@Nullable final EventListener listener)
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

   public void removeAllEventListeners()
      {
      lock.lock();  // block until condition holds
      try
         {
         eventListeners.clear();
         }
      finally
         {
         lock.unlock();
         }
      }

   public void start()
      {
      lock.lock();  // block until condition holds
      try
         {
         if (LOG.isDebugEnabled())
            {
            LOG.debug("DirectoryPoller.start(): Starting DirectoryPoller [" + this + "]");
            }
         if (pollingTimer == null)
            {
            this.pollingTimer = new Timer(true);
            this.pollingTimer.scheduleAtFixedRate(new DirectoryPollingTimerTask(), 0, timeUnit.toMillis(delay));
            }
         }
      finally
         {
         lock.unlock();
         }
      }

   public void stop()
      {
      lock.lock();  // block until condition holds
      try
         {
         if (LOG.isDebugEnabled())
            {
            LOG.debug("DirectoryPoller.stop(): Stopping DirectoryPoller [" + this + "]");
            }
         if (pollingTimer != null)
            {
            pollingTimer.cancel();
            pollingTimer = null;
            fileModificationTimeMap.clear();
            }
         }
      finally
         {
         lock.unlock();
         }
      }

   public void forceRefresh()
      {
      lock.lock();  // block until condition holds
      try
         {
         if (LOG.isDebugEnabled())
            {
            LOG.debug("DirectoryPoller.forceRefresh(): Force refresh for DirectoryPoller [" + this + "]");
            }
         stop();
         start();
         }
      finally
         {
         lock.unlock();
         }
      }

   private class DirectoryPollingTimerTask extends TimerTask
      {

      public void run()
         {
         lock.lock();
         try
            {
            // make sure the directory to poll is not null
            final File directoryToPoll = directoryProvider.getFile();
            if (directoryToPoll != null)
               {
               final Set<File> newFiles = new HashSet<File>();
               final Set<File> modifiedFiles = new HashSet<File>();
               final Set<File> deletedFiles = new HashSet<File>();
               final HashSet<File> checkedFiles = new HashSet<File>();

               // scan the files and check for modification/addition
               final File[] fileList = getFileList(directoryToPoll);
               if (fileList != null)
                  {
                  for (final File file : fileList)
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
                  }

               // now check for deleted files
               final Set<File> files = new HashSet<File>(fileModificationTimeMap.keySet());
               files.removeAll(checkedFiles);
               for (final File file : files)
                  {
                  fileModificationTimeMap.remove(file);
                  deletedFiles.add(file);
                  }

               final boolean hasEventListeners = !eventListeners.isEmpty();
               final boolean hasEventsToReport = !newFiles.isEmpty() || !modifiedFiles.isEmpty() || !deletedFiles.isEmpty();

               // notify the handler of new/modified/removed files
               if (hasEventListeners && hasEventsToReport)
                  {
                  executorService.execute(
                        new Runnable()
                        {
                        public void run()
                           {
                           if (LOG.isTraceEnabled())
                              {
                              final StringBuilder s = new StringBuilder();
                              s.append("New Files:");
                              for (final File file : newFiles)
                                 {
                                 s.append(" ").append(file.getName());
                                 }
                              s.append("\nModified Files:");
                              for (final File file : modifiedFiles)
                                 {
                                 s.append(" ").append(file.getName());
                                 }
                              s.append("\nDeleted Files:");
                              for (final File file : deletedFiles)
                                 {
                                 s.append(" ").append(file.getName());
                                 }
                              LOG.trace("DirectoryPoller$DirectoryPollingTimerTask.run(): \n" + s);
                              }

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
            }
         finally
            {
            lock.unlock();
            }
         }

      private File[] getFileList(@NotNull final File directoryToPoll)
         {
         if (fileFilter != null)
            {
            return directoryToPoll.listFiles(fileFilter);
            }
         return directoryToPoll.listFiles();
         }
      }
   }
