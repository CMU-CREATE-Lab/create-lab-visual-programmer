package edu.cmu.ri.createlab.xml;

import java.awt.Component;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.PropertyResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JOptionPane;
import edu.cmu.ri.createlab.userinterface.util.DialogHelper;
import edu.cmu.ri.createlab.util.FileProvider;
import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jetbrains.annotations.NotNull;

/**
 * <p>
 * The <code>SaveXmlDocumentDialogRunnable</code> assists in the saving of XML documents.
 * </p>
 * <p>The following properties must be defined in the {@link PropertyResourceBundle} given to the constructor:</p>
 * <ul>
 *    <li><code>dialog.title.cannot-save-document</code></li>
 *    <li><code>dialog.message.cannot-save-empty-document</code></li>
 *    <li><code>dialog.message.cannot-save-document-empty-filename</code></li>
 *    <li><code>dialog.message.cannot-save-document-invalid-filename</code></li>
 *    <li><code>dialog.message.cannot-save-document-readonly-file</code></li>
 *    <li><code>dialog.message.cannot-save-document-because--destination-directory-is-null</code></li>
 *    <li><code>dialog.message.cannot-save-document</code></li>
 *    <li><code>dialog.title.confirm-overwrite-document</code></li>
 *    <li><code>dialog.message.confirm-overwrite-document</code></li>
 *    <li><code>button.label.save-a-copy</code></li>
 *    <li><code>button.label.save-and-replace</code></li>
 *    <li><code>button.label.cancel</code></li>
 *    <li><code>dialog.title.save-options</code></li>
 *    <li><code>dialog.message.save-options</code></li>
 *    <li><code>dialog.title.save-a-copy</code></li>
 *    <li><code>dialog.message.save-a-copy</code></li>
 *    <li><code>dialog.title.overwrite-document</code></li>
 *    <li><code>dialog.message.overwrite-document</code></li>
 * </ul>
 * @author Chris Bartley (bartley@cmu.edu)
 */
public abstract class SaveXmlDocumentDialogRunnable implements Runnable
   {
   private static final Logger LOG = Logger.getLogger(SaveXmlDocumentDialogRunnable.class);

   public interface EventHandler
      {
      void handleSuccessfulSave(@NotNull final String savedFilenameWithoutExtension);
      }

   private static final String XML_FILE_EXTENSION = ".xml";

   private final String xmlDocumentString;
   private final String filename;
   private final FileProvider destinationDirectoryFileProvider;
   private final Component parentComponent;
   private final PropertyResourceBundle resources;

   public SaveXmlDocumentDialogRunnable(final Document xmlDocument,
                                        final String filename,
                                        final FileProvider destinationDirectoryFileProvider,
                                        final Component parentComponent,
                                        final PropertyResourceBundle resources)
      {
      this(xmlDocument == null ? null : XmlHelper.writeDocumentToStringFormatted(xmlDocument),
           filename,
           destinationDirectoryFileProvider,
           parentComponent,
           resources);
      }

   public SaveXmlDocumentDialogRunnable(final String xmlDocumentString,
                                        final String filename,
                                        final FileProvider destinationDirectoryFileProvider,
                                        final Component parentComponent,
                                        final PropertyResourceBundle resources)
      {
      if (destinationDirectoryFileProvider == null)
         {
         throw new NullPointerException("The destination directory FileProvider may not be null");
         }
      this.xmlDocumentString = xmlDocumentString;
      this.filename = filename;
      this.destinationDirectoryFileProvider = destinationDirectoryFileProvider;
      this.parentComponent = parentComponent;
      this.resources = resources;
      }

   public final void run()
      {
      if (xmlDocumentString == null)
         {
         DialogHelper.showInfoMessage(resources.getString("dialog.title.cannot-save-document"),
                                      resources.getString("dialog.message.cannot-save-empty-document"),
                                      parentComponent);
         }
      else
         {
         final File destinationDirectory = destinationDirectoryFileProvider.getFile();
         if (destinationDirectory == null)
            {
            DialogHelper.showInfoMessage(resources.getString("dialog.title.cannot-save-document"),
                                         resources.getString("dialog.message.cannot-save-document-because--destination-directory-is-null"),
                                         parentComponent);
            }
         else
            {
            // make sure the directory exists, just in case
            // noinspection ResultOfMethodCallIgnored
            destinationDirectory.mkdirs();

            String requestedFileName = "";
            boolean promptForNewName = false;

            while (requestedFileName.length() <= 0 || promptForNewName)
               {
               final int saveChoice;
               if ("Untitled".equals(filename))
                  {
                  saveChoice = JOptionPane.YES_OPTION;
                  }
               else
                  {
                  final Object[] options = {resources.getString("button.label.save-a-copy"),
                                            resources.getString("button.label.save-and-replace"),
                                            resources.getString("button.label.cancel")};
                  saveChoice = JOptionPane.showOptionDialog(parentComponent,
                                                            resources.getString("dialog.message.save-options"),
                                                            resources.getString("dialog.title.save-options"),
                                                            JOptionPane.YES_NO_CANCEL_OPTION,
                                                            JOptionPane.QUESTION_MESSAGE,
                                                            null,     //do not use a custom Icon
                                                            options,  //the titles of buttons
                                                            options[2]); //default button title
                  }

               if (saveChoice == JOptionPane.YES_OPTION)
                  {
                  requestedFileName = JOptionPane.showInputDialog(parentComponent,
                                                                  resources.getString("dialog.message.save-a-copy"),
                                                                  resources.getString("dialog.title.save-a-copy"),
                                                                  JOptionPane.QUESTION_MESSAGE);
                  }
               else if (saveChoice == JOptionPane.NO_OPTION)
                  {
                  final Object[] moptions = {"Yes",
                                             "Cancel"};
                  final int replace_check = JOptionPane.showOptionDialog(parentComponent,
                                                                         MessageFormat.format(resources.getString("dialog.message.overwrite-document"), filename),
                                                                         resources.getString("dialog.title.overwrite-document"),
                                                                         JOptionPane.YES_NO_OPTION,
                                                                         JOptionPane.QUESTION_MESSAGE,
                                                                         null,         //do not use a custom Icon
                                                                         moptions,     //the titles of buttons
                                                                         moptions[0]); //default button title

                  if (replace_check == JOptionPane.YES_OPTION)
                     {
                     requestedFileName = filename;
                     }
                  else
                     {
                     break;
                     }
                  }
               else
                  {
                  break;
                  }

               if (requestedFileName == null)
                  {
                  // user hit Cancel, so just break
                  break;
                  }
               else if (requestedFileName.length() == 0)
                  {
                  DialogHelper.showInfoMessage(resources.getString("dialog.title.cannot-save-document"),
                                               resources.getString("dialog.message.cannot-save-document-empty-filename"),
                                               parentComponent);
                  }
               else
                  {
                  // make sure the user doesn't try anything like ../ or /
                  File fileToSave = new File(destinationDirectory, requestedFileName);
                  while (!destinationDirectory.equals(fileToSave.getParentFile()))
                     {
                     fileToSave = new File(destinationDirectory, fileToSave.getName());
                     }

                  requestedFileName = fileToSave.getName();

                  // make sure the filename ends with .xml
                  final boolean needsXmlExtension = !requestedFileName.toLowerCase().endsWith(XML_FILE_EXTENSION);
                  fileToSave = new File(destinationDirectory, requestedFileName + (needsXmlExtension ? XML_FILE_EXTENSION : ""));

                  // make sure the filename isn't empty
                  if (XML_FILE_EXTENSION.equalsIgnoreCase(fileToSave.getName()))
                     {
                     DialogHelper.showInfoMessage(resources.getString("dialog.title.cannot-save-document"),
                                                  resources.getString("dialog.message.cannot-save-document-empty-filename"),
                                                  parentComponent);

                     continue;
                     }


                  //Regular Expression for finding non-AlphaNumeric Characters    
                  Pattern alphaNum = Pattern.compile("[^a-z0-9 _-]", Pattern.CASE_INSENSITIVE);
                  Matcher alphaNumTest = alphaNum.matcher(requestedFileName);





                  if (fileToSave.exists())
                     {
                     // don't let them overwrite directories or hidden files
                     if (fileToSave.isDirectory() || fileToSave.isHidden())
                        {
                        DialogHelper.showInfoMessage(resources.getString("dialog.title.cannot-save-document"),
                                                     resources.getString("dialog.message.cannot-save-document-invalid-filename"),
                                                     parentComponent);

                        promptForNewName = true;
                        }
                     else
                        {
                        // Verify that the user wants to overwrite this file

                        if (saveChoice == JOptionPane.NO_OPTION || DialogHelper.showYesNoDialog(resources.getString("dialog.title.confirm-overwrite-document"),
                                                                                                resources.getString("dialog.message.confirm-overwrite-document"),
                                                                                                parentComponent))
                           {
                           if (fileToSave.canWrite())
                              {
                              saveFile(fileToSave);
                              break;
                              }
                           else
                              {
                              DialogHelper.showInfoMessage(resources.getString("dialog.title.cannot-save-document"),
                                                           resources.getString("dialog.message.cannot-save-document-readonly-file"),
                                                           parentComponent);
                              promptForNewName = true;
                              }
                           }
                        else
                           {
                           promptForNewName = true;
                           }
                        }
                     }
                  else if (alphaNumTest.find()){
                      DialogHelper.showInfoMessage(resources.getString("dialog.title.cannot-save-document"),
                              resources.getString("dialog.message.cannot-save-alphanumeric"),
                              parentComponent);
                      promptForNewName = true;
                  }
                  else
                     {
                     saveFile(fileToSave);

                     break;
                     }
                  }
               }
            }
         }
      }

   private void saveFile(final File fileToSave)
      {
      FileWriter fileWriter = null;
      try
         {
         fileWriter = new FileWriter(fileToSave);
         fileWriter.write(xmlDocumentString);
         if (LOG.isDebugEnabled())
            {
            LOG.debug("SaveXmlDocumentDialogRunnable.saveFile(): Wrote file [" + fileToSave + "]");
            }
         final int extensionPosition = fileToSave.getName().lastIndexOf(XML_FILE_EXTENSION);
         final String filenameWithoutExtension;
         if (extensionPosition < 0)
            {
            filenameWithoutExtension = fileToSave.getName();
            }
         else
            {
            filenameWithoutExtension = fileToSave.getName().substring(0, extensionPosition);
            }
         performUponSuccessfulSave(filenameWithoutExtension);
         }
      catch (IOException e)
         {
         LOG.error("IOException while writing the file", e);
         DialogHelper.showErrorMessage(resources.getString("dialog.title.cannot-save-document"),
                                       resources.getString("dialog.message.cannot-save-document"),
                                       parentComponent);
         }
      finally
         {
         if (fileWriter != null)
            {
            try
               {
               fileWriter.close();
               }
            catch (IOException e)
               {
               LOG.error("Failed to close the file writer.  Oh well.", e);
               }
            }
         }
      }

   protected abstract void performUponSuccessfulSave(final String savedFilenameWithoutExtension);
   }
