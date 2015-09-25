package edu.cmu.ri.createlab.util;

import java.util.Set;
import org.jetbrains.annotations.NotNull;

/**
 * <p>
 * <code>FileEventListener</code> does something...
 * </p>
 *
 * @author Chris Bartley (bartley@cmu.edu)
 */
public interface FileEventListener
   {
   void handleNewFileEvent(@NotNull final Set<String> files);

   void handleModifiedFileEvent(@NotNull final Set<String> files);

   void handleDeletedFileEvent(@NotNull final Set<String> files);

   /*void handleNewFileEvent();

   void handleModifiedFileEvent();

   void handleDeletedFileEvent();*/
   }