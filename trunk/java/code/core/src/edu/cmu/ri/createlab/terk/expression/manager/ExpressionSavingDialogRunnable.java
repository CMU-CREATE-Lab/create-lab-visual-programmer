package edu.cmu.ri.createlab.terk.expression.manager;

import java.awt.Component;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.PropertyResourceBundle;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import edu.cmu.ri.createlab.terk.TerkConstants;
import edu.cmu.ri.createlab.terk.expression.XmlExpression;
import edu.cmu.ri.createlab.userinterface.util.DialogHelper;
import org.apache.log4j.Logger;

/**
 * The <code>ExpressionSavingDialogRunnable</code> assists in the saving of {@link XmlExpression}s.
 *
 * @author Chris Bartley (bartley@cmu.edu)
 */
final class ExpressionSavingDialogRunnable implements Runnable
   {
   private static final Logger LOG = Logger.getLogger(ExpressionSavingDialogRunnable.class);

   private static final PropertyResourceBundle RESOURCES = (PropertyResourceBundle)PropertyResourceBundle.getBundle(ExpressionSavingDialogRunnable.class.getName());
   private static final String XML_FILE_EXTENSION = ".xml";

   private final XmlExpression expression;
   private final Component parentComponent;
   private final String stageTitle;
   private final JTextField stageField;

   ExpressionSavingDialogRunnable(final XmlExpression expression, final Component parentComponent, final String title, final JTextField field)
      {
      this.expression = expression;
      this.parentComponent = parentComponent;
      this.stageTitle = title;
      this.stageField = field;
      }

   public void run()
      {
      if (expression == null)
         {
         DialogHelper.showInfoMessage(RESOURCES.getString("dialog.title.cannot-save-empty-expression"),
                                      RESOURCES.getString("dialog.message.cannot-save-empty-expression"),
                                      parentComponent);
         }
      else
         {
         String requestedFileName = "";
         boolean promptForNewName = false;

         while (requestedFileName.length() <= 0 || promptForNewName)
            {
            int saveChoice;
            if (stageTitle.equals("Untitled"))
               {
               saveChoice = JOptionPane.YES_OPTION;
               }
            else
               {
               Object[] options = {"Save Copy",
                                   "Save and Replace", "Cancel"};
               saveChoice = JOptionPane.showOptionDialog(parentComponent,
                                                         "How would you like to save? \n Save a Copy - create new expression with new title \n Save and Replace - overwrite old expression",
                                                         "Save Options",
                                                         JOptionPane.YES_NO_CANCEL_OPTION,
                                                         JOptionPane.QUESTION_MESSAGE,
                                                         null,     //do not use a custom Icon
                                                         options,  //the titles of buttons
                                                         options[2]); //default button title
               }

            if (saveChoice == JOptionPane.YES_OPTION)
               {
               requestedFileName = JOptionPane.showInputDialog(parentComponent,
                                                               "Save copy as:",
                                                               "Save a Copy",
                                                               JOptionPane.QUESTION_MESSAGE);
               }
            else if (saveChoice == JOptionPane.NO_OPTION)
               {
               final Object[] moptions = {"Yes",
                                          "Cancel"};
               final int replace_check = JOptionPane.showOptionDialog(parentComponent,
                                                                      "Are you sure you want to replace old expression: \"" + stageTitle + "\"?\nThis will also change sequences where the expression is used.",
                                                                      "Overwrite Expression?",
                                                                      JOptionPane.YES_NO_OPTION,
                                                                      JOptionPane.QUESTION_MESSAGE,
                                                                      null,     //do not use a custom Icon
                                                                      moptions,  //the titles of buttons
                                                                      moptions[0]); //default button title

               if (replace_check == JOptionPane.YES_OPTION)
                  {
                  requestedFileName = stageTitle;
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

            // todo: improve the error checking here

            if (requestedFileName == null)
               {
               // user hit Cancel, just so just break
               break;
               }
            else if (requestedFileName.length() == 0)
               {
               DialogHelper.showInfoMessage(RESOURCES.getString("dialog.title.cannot-save-expression-empty-filename"),
                                            RESOURCES.getString("dialog.message.cannot-save-expression-empty-filename"),
                                            parentComponent);

               continue;
               }
            else
               {
               // make sure the user doesn't try anything like ../ or /
               File fileToSave = new File(TerkConstants.FilePaths.EXPRESSIONS_DIR, requestedFileName);
               //LOG.debug("Initial filepath [" + fileToSave.getAbsolutePath() + "]");
               while (!TerkConstants.FilePaths.EXPRESSIONS_DIR.equals(fileToSave.getParentFile()))
                  {
                  fileToSave = new File(TerkConstants.FilePaths.EXPRESSIONS_DIR, fileToSave.getName());
                  }
               //LOG.debug("New filepath     [" + fileToSave.getAbsolutePath() + "]");

               requestedFileName = fileToSave.getName();

               // make sure the filename ends with .xml
               final boolean needsXmlExtension = !requestedFileName.toLowerCase().endsWith(XML_FILE_EXTENSION);
               fileToSave = new File(TerkConstants.FilePaths.EXPRESSIONS_DIR, requestedFileName + (needsXmlExtension ? XML_FILE_EXTENSION : ""));

               //LOG.debug(".xml filepath    [" + fileToSave.getAbsolutePath() + "]");

               // make sure the filename isn't empty
               if (XML_FILE_EXTENSION.equalsIgnoreCase(fileToSave.getName()))
                  {
                  DialogHelper.showInfoMessage(RESOURCES.getString("dialog.title.cannot-save-expression-empty-filename"),
                                               RESOURCES.getString("dialog.message.cannot-save-expression-empty-filename"),
                                               parentComponent);

                  continue;
                  }

               if (fileToSave.exists())
                  {
                  // don't let them overwrite directories or hidden files
                  if (fileToSave.isDirectory() || fileToSave.isHidden())
                     {
                     LOG.debug("directory or hidden");
                     DialogHelper.showInfoMessage(RESOURCES.getString("dialog.title.cannot-save-expression-invalid-filename"),
                                                  RESOURCES.getString("dialog.message.cannot-save-expression-invalid-filename"),
                                                  parentComponent);

                     promptForNewName = true;
                     continue;
                     }
                  else
                     {
                     // Verify that the user wants to overwrite this file

                     if (saveChoice == JOptionPane.NO_OPTION || DialogHelper.showYesNoDialog(RESOURCES.getString("dialog.title.confirm-overwrite-expression"),
                                                                                             RESOURCES.getString("dialog.message.confirm-overwrite-expression"),
                                                                                             parentComponent))
                        {
                        if (fileToSave.canWrite())
                           {
                           saveFile(fileToSave, requestedFileName);
                           break;
                           }
                        else
                           {
                           DialogHelper.showInfoMessage(RESOURCES.getString("dialog.title.cannot-save-expression-readonly-file"),
                                                        RESOURCES.getString("dialog.message.cannot-save-expression-readonly-file"),
                                                        parentComponent);
                           promptForNewName = true;
                           }
                        }
                     else
                        {
                        promptForNewName = true;
                        }

                     continue;
                     }
                  }
               else
                  {
                  saveFile(fileToSave, requestedFileName);

                  break;
                  }
               }
            }
         }
      }

   private void saveFile(final File fileToSave, final String requestedFileName)
      {
      FileWriter fileWriter = null;
      try
         {
         fileWriter = new FileWriter(fileToSave);
         fileWriter.write(expression.toXmlDocumentStringFormatted());
         stageField.setText(requestedFileName);
         LOG.debug("ExpressionSavingDialogRunnable.saveFile(): Wrote file [" + fileToSave + "]");
         }
      catch (IOException e)
         {
         LOG.error("IOException while writing the file", e);
         DialogHelper.showErrorMessage(RESOURCES.getString("dialog.title.cannot-save-expression"),
                                       RESOURCES.getString("dialog.message.cannot-save-expression"),
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
   }
