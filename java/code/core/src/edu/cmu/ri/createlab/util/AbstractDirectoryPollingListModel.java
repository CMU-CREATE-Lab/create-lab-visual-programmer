package edu.cmu.ri.createlab.util;

import java.io.File;
import java.util.Collections;
import java.util.Comparator;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import javax.swing.AbstractListModel;
import org.apache.commons.collections.list.TreeList;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public abstract class AbstractDirectoryPollingListModel<T> extends AbstractListModel implements DirectoryPoller.EventListener
   {
   private static final Logger LOG = Logger.getLogger(AbstractDirectoryPollingListModel.class);

   private final Lock dataSynchronizationLock = new ReentrantLock();
   private final TreeList listItems = new TreeList();
   private final SortedMap<File, T> fileToListItemMap = new TreeMap<File, T>();
   private final Comparator<T> listItemComparator;

   public AbstractDirectoryPollingListModel(final Comparator<T> listItemComparator)
      {
      this.listItemComparator = listItemComparator;
      }

   public final void handleNewFileEvent(@NotNull final Set<File> files)
      {
      if (!files.isEmpty())
         {
         for (final File file : files)
            {
            if (LOG.isTraceEnabled())
               {
               LOG.trace("AbstractDirectoryPollingListModel.handleNewFileEvent(" + file.getName() + ")");
               }
            final T listItem = createListItemInstance(file);
            if (listItem != null)
               {
               final int insertionPostion;

               dataSynchronizationLock.lock();
               try
                  {
                  // Do a binary search to figure out where to insert this list item since TreeList's add() and
                  // addAll() methods merely (and unintuitively, IMO!) append to the end of the list.
                  final int searchResult = Collections.binarySearch(listItems, listItem, listItemComparator);
                  if (searchResult >= 0)
                     {
                     if (LOG.isTraceEnabled())
                        {
                        LOG.trace("AbstractDirectoryPollingListModel.handleNewFileEvent(): File " + file.getName() + " already exists in the list, so we'll just ignore it.");
                        }
                     continue;
                     }

                  // compute the insertion position from the search result (see javadocs for binarySearch())
                  insertionPostion = -searchResult - 1;
                  listItems.add(insertionPostion, listItem);
                  fileToListItemMap.put(file, listItem);
                  }
               finally
                  {
                  dataSynchronizationLock.unlock();
                  }

               if (insertionPostion >= 0)
                  {
                  fireIntervalAdded(this, insertionPostion, insertionPostion);
                  if (LOG.isDebugEnabled())
                     {
                     LOG.debug("AbstractDirectoryPollingListModel.handleNewFileEvent(): File added: [" + file.getName() + "]");
                     }
                  }
               else
                  {
                  LOG.error("AbstractDirectoryPollingListModel.handleNewFileEvent(): Index for file [" + file + "] not found!");
                  }
               }
            else
               {
               LOG.error("AbstractDirectoryPollingListModel.handleNewFileEvent(): Could not create a list item instance for file [" + file + "].  Ignoring.");
               }
            }
         }
      }

   public final void handleModifiedFileEvent(@NotNull final Set<File> files)
      {
      if (!files.isEmpty())
         {
         for (final File file : files)
            {
            if (LOG.isTraceEnabled())
               {
               LOG.trace("AbstractDirectoryPollingListModel.handleModifiedFileEvent(" + file.getName() + ")");
               }

            int index = -1;
            dataSynchronizationLock.lock();
            try
               {
               // look up the file entry
               final T listItemForModifiedFile = fileToListItemMap.get(file);
               if (listItemForModifiedFile != null)
                  {
                  // get the index
                  index = listItems.indexOf(listItemForModifiedFile);
                  }
               }
            finally
               {
               dataSynchronizationLock.unlock();
               }

            if (index >= 0)
               {
               fireContentsChanged(this, index, index);
               if (LOG.isDebugEnabled())
                  {
                  LOG.debug("AbstractDirectoryPollingListModel.handleModifiedFileEvent(): File modified: [" + file.getName() + "]");
                  }
               }
            else
               {
               LOG.error("AbstractDirectoryPollingListModel.handleModifiedFileEvent(): Index for file [" + file + "] not found!");
               }
            }
         }
      }

   public final void handleDeletedFileEvent(@NotNull final Set<File> files)
      {
      if (!files.isEmpty())
         {
         for (final File file : files)
            {
            if (LOG.isTraceEnabled())
               {
               LOG.trace("AbstractDirectoryPollingListModel.handleDeletedFileEvent(" + file.getName() + ")");
               }

            int index = -1;
            dataSynchronizationLock.lock();
            try
               {
               final T listItemForDeletedFile = fileToListItemMap.remove(file);
               if (listItemForDeletedFile != null)
                  {
                  index = listItems.indexOf(listItemForDeletedFile);
                  listItems.remove(listItemForDeletedFile);
                  }
               }
            finally
               {
               dataSynchronizationLock.unlock();
               }

            if (index >= 0)
               {
               fireIntervalRemoved(this, index, index);
               if (LOG.isDebugEnabled())
                  {
                  LOG.debug("AbstractDirectoryPollingListModel.handleDeletedFileEvent(): File deleted: [" + file.getName() + "]");
                  }
               }
            else
               {
               LOG.error("AbstractDirectoryPollingListModel.handleDeletedFileEvent(): Index for file [" + file + "] not found!");
               }
            }
         }
      }

   public final int getSize()
      {
      dataSynchronizationLock.lock();
      try
         {
         return listItems.size();
         }
      finally
         {
         dataSynchronizationLock.unlock();
         }
      }

   public final Object getElementAt(final int index)
      {
      dataSynchronizationLock.lock();
      try
         {
         if (index >= 0 && index < listItems.size())
            {
            return listItems.get(index);
            }
         return null;
         }
      finally
         {
         dataSynchronizationLock.unlock();
         }
      }

   /**
    * Returns the index of the element matching the given <code>obj</code>.  Returns <code>-1</code> if not
    * found, or if the given <code>obj</code> is <code>null</code>.
    */
   public int indexOf(@Nullable final T obj)
      {
      if (obj != null)
         {
         return listItems.indexOf(obj);
         }
      return -1;
      }

   /**
    * Convenience method similar to {@link #getElementAt(int)} that returns the element at the specified
    * <code>index</code>, but downcasts it to the appropriate type.
    */
   public final T getNarrowedElementAt(final int index)
      {
      return (T)getElementAt(index);
      }

   protected abstract T createListItemInstance(@NotNull final File file);
   }
