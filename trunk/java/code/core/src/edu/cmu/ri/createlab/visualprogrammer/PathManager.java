package edu.cmu.ri.createlab.visualprogrammer;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import edu.cmu.ri.createlab.util.DirectoryPoller;
import edu.cmu.ri.createlab.util.FileProvider;
import edu.cmu.ri.createlab.xml.XmlFilenameFilter;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * <p>
 * <code>PathManager</code> is a singleton which helps manage filesystem paths for the Visual Programmer. This class
 * helps other Visual Programmer classes obtain the correct directories for tasks such as saving/loading expressions
 * and sequences.  These paths returned vary based on the {@link VisualProgrammerDevice} to which the Visual Programmer
 * is currently connected since the device name is included in the path.  This class provides an easy way to obtain
 * those path names.
 * </p>
 *
 * @author Chris Bartley (bartley@cmu.edu)
 */
public final class PathManager
   {
   private static final Logger LOG = Logger.getLogger(PathManager.class);

   private static final PathManager INSTANCE = new PathManager();
   public static final FileProvider EXPRESSIONS_DIRECTORY_FILE_PROVIDER =
         new FileProvider()
         {
         @Override
         public File getFile()
            {
            return INSTANCE.getExpressionsDirectory();
            }
         };
   public static final FileProvider SEQUENCES_DIRECTORY_FILE_PROVIDER =
         new FileProvider()
         {
         @Override
         public File getFile()
            {
            return INSTANCE.getSequencesDirectory();
            }
         };

   public static PathManager getInstance()
      {
      return INSTANCE;
      }

   private final Lock lock = new ReentrantLock();
   private File expressionsDirectory = null;
   private File sequencesDirectory = null;
   private DirectoryPoller expressionsDirectoryPoller = null;
   private DirectoryPoller sequencesDirectoryPoller = null;
   private final Set<DirectoryPoller.EventListener> expressionsDirectoryPollerEventListeners = new HashSet<DirectoryPoller.EventListener>();
   private final Set<DirectoryPoller.EventListener> sequencesDirectoryPollerEventListeners = new HashSet<DirectoryPoller.EventListener>();

   private PathManager()
      {
      // private to prevent instantiation
      }

   /**
    * Returns the expressions directory for the current {@link VisualProgrammerDevice}.  Returns <code>null</code> if
    * the {@link VisualProgrammerDevice} has not been set, or was set to <code>null</code>.
    *
    * @see #setVisualProgrammerDevice(VisualProgrammerDevice)
    */
   @Nullable
   public File getExpressionsDirectory()
      {
      lock.lock();  // block until condition holds
      try
         {
         return expressionsDirectory;
         }
      finally
         {
         lock.unlock();
         }
      }

   /**
    * Returns the sequences directory for the current {@link VisualProgrammerDevice}.  Returns <code>null</code> if
    * the {@link VisualProgrammerDevice} has not been set, or was set to <code>null</code>.
    *
    * @see #setVisualProgrammerDevice(VisualProgrammerDevice)
    */
   @Nullable
   public File getSequencesDirectory()
      {
      lock.lock();  // block until condition holds
      try
         {
         return sequencesDirectory;
         }
      finally
         {
         lock.unlock();
         }
      }

   public void registerExpressionsDirectoryPollerEventListener(final DirectoryPoller.EventListener listener)
      {
      registerDirectoryPollerEventListener(expressionsDirectoryPoller, expressionsDirectoryPollerEventListeners, listener);
      }

   public void unregisterExpressionsDirectoryPollerEventListener(final DirectoryPoller.EventListener listener)
      {
      unregisterDirectoryPollerEventListener(expressionsDirectoryPoller, expressionsDirectoryPollerEventListeners, listener);
      }

   public void registerSequencesDirectoryPollerEventListener(final DirectoryPoller.EventListener listener)
      {
      registerDirectoryPollerEventListener(sequencesDirectoryPoller, sequencesDirectoryPollerEventListeners, listener);
      }

   public void unregisterSequencesDirectoryPollerEventListener(final DirectoryPoller.EventListener listener)
      {
      unregisterDirectoryPollerEventListener(sequencesDirectoryPoller, sequencesDirectoryPollerEventListeners, listener);
      }

   private void registerDirectoryPollerEventListener(@Nullable final DirectoryPoller directoryPoller,
                                                     @NotNull final Set<DirectoryPoller.EventListener> listeners,
                                                     @Nullable final DirectoryPoller.EventListener listener)
      {
      if (listener != null)
         {
         lock.lock();  // block until condition holds
         try
            {
            listeners.add(listener);
            if (LOG.isDebugEnabled())
               {
               LOG.debug("PathManager.registerDirectoryPollerEventListener(): There are now [" + listeners.size() + "] listeners to the DirectoryPoller [" + directoryPoller + "]");
               }
            if (directoryPoller != null)
               {
               directoryPoller.addEventListener(listener);
               }
            }
         finally
            {
            lock.unlock();
            }
         }
      }

   private void unregisterDirectoryPollerEventListener(@Nullable final DirectoryPoller directoryPoller,
                                                       @NotNull final Set<DirectoryPoller.EventListener> listeners,
                                                       @Nullable final DirectoryPoller.EventListener listener)
      {
      if (listener != null)
         {
         lock.lock();  // block until condition holds
         try
            {
            listeners.remove(listener);
            if (directoryPoller != null)
               {
               directoryPoller.removeEventListener(listener);
               }
            }
         finally
            {
            lock.unlock();
            }
         }
      }

   public void forceExpressionsDirectoryPollerRefresh()
      {
      forceDirectoryPollerRefresh(expressionsDirectoryPoller);
      }

   public void forceSequencesDirectoryPollerRefresh()
      {
      forceDirectoryPollerRefresh(sequencesDirectoryPoller);
      }

   private void forceDirectoryPollerRefresh(@Nullable final DirectoryPoller directoryPoller)
      {
      if (LOG.isDebugEnabled())
         {
         LOG.debug("PathManager.forceDirectoryPollerRefresh(): Force refresh for DirectoryPoller [" + directoryPoller + "]");
         }
      if (directoryPoller != null)
         {
         directoryPoller.forceRefresh();
         }
      }

   /**
    * Sets the current {@link VisualProgrammerDevice}.  If non-<code>null</code>, then the expressions and sequences
    * directories are created if necessary.
    */
   public void setVisualProgrammerDevice(@Nullable final VisualProgrammerDevice visualProgrammerDevice)
      {
      lock.lock();  // block until condition holds
      try
         {
         if (LOG.isDebugEnabled())
            {
            LOG.debug("PathManager.setVisualProgrammerDevice(): " + visualProgrammerDevice);
            }

         if (visualProgrammerDevice == null)
            {
            this.expressionsDirectory = null;
            this.sequencesDirectory = null;
            shutdownDirectoryPoller(expressionsDirectoryPoller);
            shutdownDirectoryPoller(sequencesDirectoryPoller);
            this.expressionsDirectoryPoller = null;
            this.sequencesDirectoryPoller = null;
            }
         else
            {
            this.expressionsDirectory = getDirectory(visualProgrammerDevice, VisualProgrammerConstants.FilePaths.EXPRESSIONS_DIRECTORY_NAME);
            this.sequencesDirectory = getDirectory(visualProgrammerDevice, VisualProgrammerConstants.FilePaths.SEQUENCES_DIRECTORY_NAME);
            shutdownDirectoryPoller(expressionsDirectoryPoller);
            shutdownDirectoryPoller(sequencesDirectoryPoller);
            this.expressionsDirectoryPoller = new DirectoryPoller(EXPRESSIONS_DIRECTORY_FILE_PROVIDER,
                                                                  new XmlFilenameFilter(),  // TODO: beef this up to validate expressions
                                                                  1,
                                                                  TimeUnit.SECONDS);
            this.sequencesDirectoryPoller = new DirectoryPoller(SEQUENCES_DIRECTORY_FILE_PROVIDER,
                                                                new XmlFilenameFilter(),  // TODO: beef this up to validate sequences
                                                                1,
                                                                TimeUnit.SECONDS);

            if (LOG.isDebugEnabled())
               {
               LOG.debug("PathManager.setVisualProgrammerDevice(): adding [" + expressionsDirectoryPollerEventListeners.size() + "] listeners to the expressions DirectoryPoller");
               }
            for (final DirectoryPoller.EventListener listener : expressionsDirectoryPollerEventListeners)
               {
               this.expressionsDirectoryPoller.addEventListener(listener);
               }

            if (LOG.isDebugEnabled())
               {
               LOG.debug("PathManager.setVisualProgrammerDevice(): adding [" + sequencesDirectoryPollerEventListeners.size() + "] listeners to the sequences DirectoryPoller");
               }
            for (final DirectoryPoller.EventListener listener : sequencesDirectoryPollerEventListeners)
               {
               this.sequencesDirectoryPoller.addEventListener(listener);
               }
            this.expressionsDirectoryPoller.start();
            this.sequencesDirectoryPoller.start();
            }
         }
      finally
         {
         lock.unlock();
         }
      }

   /**
    * Removes all {@link DirectoryPoller.EventListener event listeners} from the given {@link DirectoryPoller} and then
    * calls {@link DirectoryPoller#stop()} on it.  Does nothing if the given {@link DirectoryPoller} is <code>null</code>.
    */
   private void shutdownDirectoryPoller(@Nullable final DirectoryPoller directoryPoller)
      {
      if (directoryPoller != null)
         {
         directoryPoller.removeAllEventListeners();
         directoryPoller.stop();
         }
      }

   @SuppressWarnings({"ResultOfMethodCallIgnored"})
   private static File getDirectory(final VisualProgrammerDevice visualProgrammerDevice, final String subdirectoryName)
      {
      final File directory = new File(new File(VisualProgrammerConstants.FilePaths.VISUAL_PROGRAMMER_HOME_DIR, visualProgrammerDevice.getDeviceName()), subdirectoryName);
      directory.mkdirs();
      return directory;
      }
   }
