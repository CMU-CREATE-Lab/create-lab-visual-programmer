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

   private static final UserPreferences INSTANCE = new UserPreferences();

   private static final Class<UserPreferences> PREFERENCES_PACKAGE = UserPreferences.class;
   private static final String IS_BACKING_STORE_AVAILABLE_KEY = "IsBackingStoreAvailable";
   private static final String USER_ID_KEY = "UserId";
   private static final String HOME_DIRECTORY_KEY = "HomeDirectory";
   private static final String SHOULD_REMEMBER_HOME_DIRECTORY_KEY = "ShouldRememberHomeDirectory";

   private static final boolean SHOULD_REMEMBER_HOME_DIRECTORY_DEFAULT_VALUE = false;
   private static final String HOME_DIRECTORY_DEFAULT_VALUE = VisualProgrammerConstants.FilePaths.DEFAULT_VISUAL_PROGRAMMER_HOME_DIR.getAbsolutePath();

   public static UserPreferences getInstance()
      {
      return INSTANCE;
      }

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
      catch (final BackingStoreException e)
         {
         return false;
         }
      return true;
      }

   private UserPreferences()
      {
      // private to prevent instantiation
      }

   public void initializePreferences()
      {
      final Preferences prefs = Preferences.userNodeForPackage(PREFERENCES_PACKAGE);
      try
         {
         prefs.clear();
         prefs.put(USER_ID_KEY, String.valueOf(UUID.randomUUID()));
         prefs.putBoolean(SHOULD_REMEMBER_HOME_DIRECTORY_KEY, SHOULD_REMEMBER_HOME_DIRECTORY_DEFAULT_VALUE);
         prefs.flush();
         }
      catch (final Exception e)
         {
         LOG.error("Exception while initializing preferences", e);
         }
      }

   public boolean hasPreferences()
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

   @Nullable
   public String getUserId()
      {
      final Preferences prefs = Preferences.userNodeForPackage(PREFERENCES_PACKAGE);
      return prefs.get(USER_ID_KEY, null);
      }

   public boolean shouldRememberHomeDirectory()
      {
      final Preferences prefs = Preferences.userNodeForPackage(PREFERENCES_PACKAGE);
      final boolean aBoolean = prefs.getBoolean(SHOULD_REMEMBER_HOME_DIRECTORY_KEY, SHOULD_REMEMBER_HOME_DIRECTORY_DEFAULT_VALUE);

      return aBoolean;
      }

   /**
    * Sets the flag for whether the home directory should be remembered. Initializes the preferences first if necessary.
    */
   public void setShouldRememberHomeDirectory(final boolean shouldRememberHomeDirectory)
      {
      if (!hasPreferences())
         {
         initializePreferences();
         }
      final Preferences prefs = Preferences.userNodeForPackage(PREFERENCES_PACKAGE);
      prefs.putBoolean(SHOULD_REMEMBER_HOME_DIRECTORY_KEY, shouldRememberHomeDirectory);
      flush(prefs);
      }

   @NotNull
   public File getHomeDirectory()
      {
      final Preferences prefs = Preferences.userNodeForPackage(PREFERENCES_PACKAGE);
      final String homeDirectoryPath = prefs.get(HOME_DIRECTORY_KEY, HOME_DIRECTORY_DEFAULT_VALUE);
      return new File(homeDirectoryPath);
      }

   public void setHomeDirectory(@Nullable final File homeDirectory)
      {
      final Preferences prefs = Preferences.userNodeForPackage(PREFERENCES_PACKAGE);
      final String homeDirectoryStr = homeDirectory == null ? "" : homeDirectory.getAbsolutePath();
      prefs.put(HOME_DIRECTORY_KEY, homeDirectoryStr);
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
