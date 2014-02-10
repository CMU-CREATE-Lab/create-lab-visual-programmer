package edu.cmu.ri.createlab.visualprogrammer;

import java.io.File;
import java.util.UUID;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public final class UserPreferences
   {
   private static final Logger LOG = Logger.getLogger(UserPreferences.class);

   private static final Class<UserPreferences> PREFERENCES_PACKAGE = UserPreferences.class;

   private static final String IS_BACKING_STORE_AVAILABLE_KEY = "IsBackingStoreAvailable";

   private static final String USER_ID_KEY = "UserId";
   private static final String SHOULD_REMEMBER_HOME_DIRECTORY_KEY_PREFIX = "ShouldRememberHomeDirectory_";
   private static final String HOME_DIRECTORY_KEY_PREFIX = "HomeDirectory_";

   private static final boolean SHOULD_REMEMBER_HOME_DIRECTORY_DEFAULT_VALUE = false;
   private static final String HOME_DIRECTORY_DEFAULT_VALUE = VisualProgrammerConstants.FilePaths.DEFAULT_VISUAL_PROGRAMMER_HOME_DIR.getAbsolutePath();

   // Got this from http://docs.oracle.com/javase/6/docs/technotes/guides/preferences/overview.html
   public static boolean isBackingStoreAvailable()
      {
      final Preferences prefs = Preferences.userRoot().node("<temporary>");
      try
         {
         final boolean oldValue = prefs.getBoolean(IS_BACKING_STORE_AVAILABLE_KEY, false);
         prefs.putBoolean(IS_BACKING_STORE_AVAILABLE_KEY, !oldValue);
         prefs.flush();
         }
      catch (final BackingStoreException ignored)
         {
         LOG.error("UserPreferences.isBackingStoreAvailable(): Backing store unavailable!");
         return false;
         }
      return true;
      }

   private final String shouldRememberHomeDirectoryKey;
   private final String homeDirectoryKey;

   public UserPreferences(@NotNull final VisualProgrammerDevice visualProgrammerDevice)
      {
      shouldRememberHomeDirectoryKey = SHOULD_REMEMBER_HOME_DIRECTORY_KEY_PREFIX + visualProgrammerDevice.getDeviceName();
      homeDirectoryKey = HOME_DIRECTORY_KEY_PREFIX + visualProgrammerDevice.getDeviceName();

      // initialize the preferences if necessary
      if (!hasPreferences())
         {
         initializePreferences();
         }

      LOG.debug("UserPreferences.UserPreferences(): UUID = [" + getUserId() + "]");
      }

   private boolean hasPreferences()
      {
      final Preferences prefs = Preferences.userNodeForPackage(PREFERENCES_PACKAGE);
      try
         {
         return prefs.keys().length > 0 && prefs.get(USER_ID_KEY, null) != null;
         }
      catch (final Exception e)
         {
         LOG.error("Exception while trying to check whether the user has any saved preferences", e);
         }
      return false;
      }

   private void initializePreferences()
      {
      final Preferences prefs = Preferences.userNodeForPackage(PREFERENCES_PACKAGE);
      try
         {
         prefs.clear();
         prefs.put(USER_ID_KEY, String.valueOf(UUID.randomUUID()));
         prefs.putBoolean(shouldRememberHomeDirectoryKey, SHOULD_REMEMBER_HOME_DIRECTORY_DEFAULT_VALUE);
         prefs.put(homeDirectoryKey, HOME_DIRECTORY_DEFAULT_VALUE);
         prefs.flush();
         }
      catch (final Exception e)
         {
         LOG.error("Exception while initializing preferences", e);
         }
      }

   @Nullable
   public String getUserId()
      {
      final Preferences prefs = Preferences.userNodeForPackage(PREFERENCES_PACKAGE);
      return prefs.get(USER_ID_KEY, null);
      }

   public boolean shouldRememberHomeDirectory()
      {
      final Preferences prefs = Preferences.userNodeForPackage(PREFERENCES_PACKAGE);
      return prefs.getBoolean(shouldRememberHomeDirectoryKey, SHOULD_REMEMBER_HOME_DIRECTORY_DEFAULT_VALUE);
      }

   /**
    * Sets the flag for whether the home directory should be remembered. Initializes the preferences first if necessary.
    */
   public void setShouldRememberHomeDirectory(final boolean shouldRememberHomeDirectory)
      {
      final Preferences prefs = Preferences.userNodeForPackage(PREFERENCES_PACKAGE);
      prefs.putBoolean(shouldRememberHomeDirectoryKey, shouldRememberHomeDirectory);
      flush(prefs);
      }

   @NotNull
   public File getHomeDirectory()
      {
      final Preferences prefs = Preferences.userNodeForPackage(PREFERENCES_PACKAGE);
      return new File(prefs.get(homeDirectoryKey, HOME_DIRECTORY_DEFAULT_VALUE));
      }

   public void setHomeDirectory(@Nullable final File homeDirectory)
      {
      final Preferences prefs = Preferences.userNodeForPackage(PREFERENCES_PACKAGE);
      final String homeDirectoryStr = homeDirectory == null ? "" : homeDirectory.getAbsolutePath();
      prefs.put(homeDirectoryKey, homeDirectoryStr);
      flush(prefs);
      }

   private void flush(@NotNull final Preferences prefs)
      {
      try
         {
         prefs.flush();
         }
      catch (final Exception e)
         {
         LOG.error("Exception while trying to flush the preferences.", e);
         }
      }
   }
