package org.zeroturnaround.zip;

import java.io.File;

/**
 * Maps {@link org.zeroturnaround.zip.ZTFilePermissions} to real filesystem-specific file attributes.
 *
 * @author Viktor Karabut
 */
public interface ZTFilePermissionsStrategy
   {

   /**
    * Get {@link org.zeroturnaround.zip.ZTFilePermissions} from file.
    *
    * @param file file to get permissions for
    * @return permissions or <code>null</code> if cannot retrieve permissions info by some reason.
    */
   ZTFilePermissions getPermissions(File file);

   /**
    * Set {@link org.zeroturnaround.zip.ZTFilePermissions} to file
    *
    * @param file file to get permissions for
    * @param permissions permission
    */
   void setPermissions(File file, ZTFilePermissions permissions);
   }
