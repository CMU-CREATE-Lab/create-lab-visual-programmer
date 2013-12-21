package edu.cmu.ri.createlab.util;

/**
 * Created by IntelliJ IDEA.
 * User: jcross1
 * Date: 3/1/12
 * Time: 8:11 PM
 * To change this template use File | Settings | File Templates.
 */

import edu.cmu.ri.createlab.userinterface.util.ImageUtils;
import edu.cmu.ri.createlab.userinterface.util.SwingUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;

public class FileDropTarget extends JPanel {

    private Collection<File> filesDropped;
    JTextArea textList = new JTextArea("", 4, 20);
    JLabel fileIcon = new JLabel(ImageUtils.createImageIcon("/edu/cmu/ri/createlab/util/images/addFile.png"), JLabel.CENTER);
    JLabel errorIcon = new JLabel(ImageUtils.createImageIcon("/edu/cmu/ri/createlab/util/images/file_error.png"), JLabel.CENTER);
    JLabel dragLabel = new JLabel("Drag Files Here");
    JLabel dropLabel = new JLabel("Drop Files Here");
    JLabel errorLabel = new JLabel("Wrong File Type");
    
    public FileDropTarget(String fileExtension) {

        // Create the label
   
        JLabel listLabel =  new JLabel("Files Added:");
        JLabel targetLabel =  new JLabel("Drag-and-Drop New \"" + fileExtension.toUpperCase() + "\" Files Below:");
        textList.setEditable(false);
        textList.setBackground(Color.LIGHT_GRAY);
        
        JPanel subpanel = new JPanel();
        subpanel.setBackground(Color.WHITE);
        subpanel.setBorder(BorderFactory.createLineBorder(Color.GRAY));

        JScrollPane scroller = new JScrollPane(textList);
        scroller.setBorder(BorderFactory.createLineBorder(Color.GRAY));

        this.setLayout(new GridBagLayout());
        subpanel.setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.weightx = .5;
        c.weighty = .5;
        c.gridx = 0;
        c.gridy = 0;
        c.anchor = GridBagConstraints.CENTER;
        c.insets = new Insets(0, 0, 5, 0);
        this.add(targetLabel,c);
        c.gridy = 1;
        this.add(subpanel, c);
        c.gridy = 2;
        this.add(listLabel,c);
        c.gridy = 3;
        c.insets = new Insets(0, 0, 0, 0);
        this.add(scroller, c);

        c.weighty = 0.0;
        c.fill = GridBagConstraints.NONE;
        c.gridy = 0;
        c.anchor = GridBagConstraints.CENTER;
        c.insets = new Insets(10, 0, 10, 0);
        subpanel.add(dropLabel, c);

        c.weighty = 0.0;
        c.fill = GridBagConstraints.NONE;
        c.gridy = 0;
        c.anchor = GridBagConstraints.CENTER;
        c.insets = new Insets(10, 0, 10, 0);
        subpanel.add(dragLabel, c);
        
        c.weighty = 0.0;
        c.fill = GridBagConstraints.NONE;
        c.gridy = 0;
        c.anchor = GridBagConstraints.CENTER;
        c.insets = new Insets(10, 0, 10, 0);
        subpanel.add(errorLabel, c);

        c.gridy = 1;
        c.insets = new Insets(0, 0, 0, 0);
        c.weighty = 0.0;
        c.anchor = GridBagConstraints.CENTER;
        subpanel.add(fileIcon, c);

        c.gridy = 1;
        c.insets = new Insets(0, 0, 0, 0);
        c.weighty = 0.0;
        c.anchor = GridBagConstraints.CENTER;
        subpanel.add(errorIcon, c);

        c.gridy = 2;
        c.insets = new Insets(0, 0, 0, 0);
        c.weighty = 1.0;
        c.anchor = GridBagConstraints.PAGE_START;
        subpanel.add(SwingUtils.createRigidSpacer(), c);

        showFileIcon(false);
        showErrorIcon(false);

        // Create the drag and drop listener
        MyDragDropListener myDragDropListener = new MyDragDropListener(fileExtension);

        // Connect the label with a drag and drop listener
        new DropTarget(subpanel, myDragDropListener);

        subpanel.setMinimumSize(new Dimension(200, 100));
        subpanel.setPreferredSize(new Dimension(200, 100));

        
        filesDropped = new LinkedList<File>();
    }

    public void showFileIcon (boolean showIcon)
    {
        dropLabel.setVisible(showIcon);
        dragLabel.setVisible(!showIcon);
        fileIcon.setVisible(showIcon);
    }

    public void showErrorIcon (boolean showIcon)
    {
        errorIcon.setVisible(showIcon);
        errorLabel.setVisible(showIcon);
        dragLabel.setVisible(!showIcon);
    }

    public Collection<File> getResults(){
     return filesDropped;
    }

    class MyDragDropListener implements DropTargetListener {

        private Collection<File> files;
        private JFileChooser chooser = new JFileChooser();
        private String fileTypeName;
        private String targetType;

        public MyDragDropListener(String fileExtension){
            try{
                File temp = File.createTempFile("CREATELabTemp",fileExtension);
                temp.deleteOnExit();
                targetType = chooser.getTypeDescription(temp);


            } catch (Exception e) {

            }
        }

        public void drop(DropTargetDropEvent event) {

            // Accept copy drops
            event.acceptDrop(DnDConstants.ACTION_COPY);

            // Get the transfer which can provide the dropped item data
            Transferable transferable = event.getTransferable();

            // Get the data formats of the dropped item
            DataFlavor[] flavors = transferable.getTransferDataFlavors();

            // Loop through the flavors
            for (DataFlavor flavor : flavors) {

                try {

                    // If the drop items are files
                    if (flavor.isFlavorJavaFileListType()) {

                        // Get all of the dropped files
                        files = (Collection)transferable.getTransferData(flavor);

                        // Loop them through

                        for (File file : files) {

                            // Print out the file path
                            fileTypeName = chooser.getTypeDescription(file);
                            //System.out.println("File path is '" + file.getPath() + "'.   File Type is:" + fileTypeName);

                            if (fileTypeName.equals(targetType))
                            {
                                //System.out.println("WAVE");
                                filesDropped.add(file);
                                textList.append(chooser.getName(file) + "\n");
                            }

                        }
                        

                }

                } catch (Exception e) {

                    // Print out the error stack
                    e.printStackTrace();

                }
            }

            // Inform that the drop is complete
            event.dropComplete(true);
            showFileIcon(false);
            showErrorIcon(false);

        }


        public void dragEnter(DropTargetDragEvent event) {

            // Get the transfer which can provide the dropped item data
            Transferable transferable = event.getTransferable();

            // Get the data formats of the dropped item
            DataFlavor[] flavors = transferable.getTransferDataFlavors();

            boolean goodFileFlag = false;
            
            // Loop through the flavors
            for (DataFlavor flavor : flavors) {

                try {

                    // If the drop items are files
                    if (flavor.isFlavorJavaFileListType()) {

                        files = (Collection)transferable.getTransferData(flavor);

                        for (File file : files) {

                            // Print out the file path
                            fileTypeName = chooser.getTypeDescription(file);

                            if (fileTypeName.equals(targetType))
                            {
                                showFileIcon(true);
                            }else if (!goodFileFlag){
                                showErrorIcon(true);
                            }

                            

                        }



                    }

                }

                catch (Exception e) {

                    // Print out the error stack
                    e.printStackTrace();

                }
            }


        }


        public void dragExit(DropTargetEvent event) {
            showFileIcon(false);
            showErrorIcon(false);
        }


        public void dragOver(DropTargetDragEvent event) {
        }


        public void dropActionChanged(DropTargetDragEvent event) {
        }

    }
}
