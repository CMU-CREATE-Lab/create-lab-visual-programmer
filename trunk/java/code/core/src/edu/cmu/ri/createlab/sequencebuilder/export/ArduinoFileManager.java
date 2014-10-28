package edu.cmu.ri.createlab.sequencebuilder.export;

import java.io.File;
import java.io.IOException;

/**
 * Created by c3morales on 07-11-14.
 */
public class ArduinoFileManager
   {
   private File mainFile;
   private File newArduinoFile;
   private String prettyName;

   public ArduinoFileManager(final File mainFile, final File newArduinoFileLoc)
      {

      try
         {
         this.mainFile = mainFile;
         this.prettyName = filterName();
         this.newArduinoFile = new File(newArduinoFileLoc, prettyName);
         makeNewDir();
         }
      catch (final IOException e)
         {
         e.printStackTrace();
         }
      }

   private void makeNewDir() throws IOException
      {
      while (newArduinoFile.exists())
         {
         if (prettyName.contains("_") && isNumber(prettyName.substring(prettyName.lastIndexOf("_") + 1)))
            {
            final String expression = prettyName.substring(prettyName.lastIndexOf("_"));
            final int num = Integer.valueOf(prettyName.substring(prettyName.lastIndexOf("_") + 1));
            prettyName = prettyName.replace(expression, "_" + (num + 1));
            newArduinoFile = new File(newArduinoFile.getParent(), prettyName);
            }
         else
            {
            prettyName += "_1";
            newArduinoFile = new File(newArduinoFile.getParent(), prettyName);
            }
         }
      newArduinoFile.mkdirs();
      }

   public void deleteDir()
      {
      newArduinoFile.delete();
      }

   private boolean isNumber(final String str)
      {
      try
         {
         Integer.parseInt(str);
         return true;
         }
      catch (final NumberFormatException e)
         {
         return false;
         }
      }

   private String filterName()
      {
      return mainFile.getName().replace(" ", "").replace("-", "").replace(".xml", "");
      }

   public String getArduinoFileName()
      {
      return prettyName + ".ino";
      }

   public File getArduinoFile()
      {
      return newArduinoFile;
      }

   public File getMainFile()
      {
      return mainFile;
      }
   }
