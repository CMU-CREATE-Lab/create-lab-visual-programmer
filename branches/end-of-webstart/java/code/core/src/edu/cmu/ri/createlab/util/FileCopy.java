package edu.cmu.ri.createlab.util;

/**
 * Created by IntelliJ IDEA.
 * User: jcross1
 * Date: 3/2/12
 * Time: 12:02 AM
 * To change this template use File | Settings | File Templates.
 */
import edu.cmu.ri.createlab.userinterface.util.DialogHelper;

import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.PropertyResourceBundle;

public class FileCopy {

    private Component parentComponent;
    
    public FileCopy(Component parent){
        parentComponent = parent;
    }
    
    private static final PropertyResourceBundle RESOURCES = (PropertyResourceBundle)PropertyResourceBundle.getBundle(FileCopy.class.getName());
    
    
    public void copy(String fromFileName, String toFileName)
            throws IOException {
        File fromFile = new File(fromFileName);
        File toFile = new File(toFileName);

        String fromFileShortName = fromFile.getName();
        
        if (!((fromFile.exists()&&fromFile.isFile())&&fromFile.canRead())){
            DialogHelper.showErrorMessage(RESOURCES.getString("dialog.title.copy-error"), fromFileShortName + " " + RESOURCES.getString("dialog.message.soure-file"));
            throw new IOException("FileCopy: Source File Error Encountered: " + fromFileShortName);
        }


        if (toFile.isDirectory())
            toFile = new File(toFile, fromFile.getName());

        if (toFile.exists()) {
            if (!toFile.canWrite()){
                DialogHelper.showErrorMessage(RESOURCES.getString("dialog.title.copy-error"), fromFileShortName + " " + RESOURCES.getString("dialog.message.no-write"));
                throw new IOException("FileCopy: "
                        + "Destination file is unwriteable: " + toFileName);
            }

            if (!(DialogHelper.showYesNoDialog(RESOURCES.getString("dialog.title.copy-error"), fromFileShortName + " " + RESOURCES.getString("dialog.message.overwrite-question"))))
                throw new IOException("FileCopy: "
                        + "existing file was not overwritten.");
        }
        else {
            String parent = toFile.getParent();
            if (parent == null)
                parent = System.getProperty("user.dir");
            File dir = new File(parent);
            if (!(dir.exists()&&dir.canWrite())||dir.isFile()){
                DialogHelper.showErrorMessage(RESOURCES.getString("dialog.title.copy-error"), fromFileShortName + " " + RESOURCES.getString("dialog.message.destination-file"));
                throw new IOException("FileCopy: "
                        + "destination directory cannot be written to: " + parent);
            }

        }

        FileInputStream from = null;
        FileOutputStream to = null;
        try {
            from = new FileInputStream(fromFile);
            to = new FileOutputStream(toFile);
            byte[] buffer = new byte[4096];
            int bytesRead;

            while ((bytesRead = from.read(buffer)) != -1)
                to.write(buffer, 0, bytesRead); // write
        } finally {
            if (from != null)
                try {
                    from.close();
                } catch (IOException e) {
                    ;
                }
            if (to != null)
                try {
                    to.close();
                } catch (IOException e) {
                    ;
                }
        }
    }
}