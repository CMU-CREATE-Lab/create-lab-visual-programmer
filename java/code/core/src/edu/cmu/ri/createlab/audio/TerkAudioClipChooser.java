package edu.cmu.ri.createlab.audio;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import javax.swing.*;

import edu.cmu.ri.createlab.CreateLabConstants;
import edu.cmu.ri.createlab.userinterface.GUIConstants;
import edu.cmu.ri.createlab.userinterface.util.ImageUtils;
import edu.cmu.ri.createlab.userinterface.util.SwingUtils;
import edu.cmu.ri.createlab.util.FileCopy;
import edu.cmu.ri.createlab.util.FileDropTarget;
import edu.cmu.ri.createlab.visualprogrammer.PathManager;
import edu.cmu.ri.createlab.visualprogrammer.VisualProgrammerConstants;
import org.apache.log4j.Logger;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public final class TerkAudioClipChooser implements AudioClipChooser
   {
   private static final Logger LOG = Logger.getLogger(TerkAudioClipChooser.class);

   private static final PropertyResourceBundle RESOURCES = (PropertyResourceBundle)PropertyResourceBundle.getBundle(TerkAudioClipChooser.class.getName());

   public static File convertFilenameToFile(final String filename)
      {
      if (filename != null)
         {
         return new File(CreateLabConstants.FilePaths.AUDIO_DIR, filename);
         }
      return null;
      }

   public static final FileFilter WAV_FILE_FILTER =
         new FileFilter()
         {
         public boolean accept(final File pathname)
            {
            return pathname != null && pathname.exists() && pathname.isFile() && pathname.getName().toLowerCase().endsWith(".wav");
            }
         };

   public static void main(final String[] args)
      {
      //Schedule a job for the event-dispatching thread: creating and showing this application's GUI.
      SwingUtilities.invokeLater(
            new Runnable()
            {
            public void run()
               {
               final JFrame jFrame = new JFrame("TerkAudioClipChooser");

               // add the root panel to the JFrame
               final TerkAudioClipChooser audioControlPanel = new TerkAudioClipChooser();
               jFrame.add(audioControlPanel.getComponent());

               // set various properties for the JFrame
               jFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
               //jFrame.setBackground(Color.WHITE);
               jFrame.setResizable(true);
               jFrame.pack();
               jFrame.setLocationRelativeTo(null);// center the window on the screen
               jFrame.setVisible(true);
               }
            });
      }

   private final JPanel panel = new JPanel();
   private FileComboBoxModel clipComboBoxModel = new FileComboBoxModel();
   private final JComboBox clipComboBox = new JComboBox(clipComboBoxModel);
   private final JButton refreshButton = SwingUtils.createButton(RESOURCES.getString("button.label.refresh"), true);
   private final JButton importButton = new JButton(RESOURCES.getString("button.label.import"), ImageUtils.createImageIcon(RESOURCES.getString("button.icon.import")));
   private final Set<AudioClipChooserEventListener> audioClipChooserEventListeners = new HashSet<AudioClipChooserEventListener>();

   public TerkAudioClipChooser()
      {
      clipComboBox.setFont(GUIConstants.FONT_NORMAL);
      //clipComboBox.setBackground(Color.WHITE);
      //clipComboBox.setMinimumSize(clipComboBox.getPreferredSize());
      clipComboBox.setPreferredSize(new Dimension(clipComboBox.getPreferredSize().width, refreshButton.getPreferredSize().height));
      clipComboBox.setMinimumSize(clipComboBox.getPreferredSize());
      clipComboBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, refreshButton.getPreferredSize().height));
      clipComboBox.addActionListener(
            new ActionListener()
            {
            public void actionPerformed(final ActionEvent e)
               {
               for (final AudioClipChooserEventListener listener : audioClipChooserEventListeners)
                  {
                  listener.handleSelectedFileChange();
                  }
               }
            }
      );

/*      refreshButton.setFocusable(false);
      refreshButton.setMnemonic(KeyEvent.VK_R);

      refreshButton.addActionListener(
            new ActionListener()
            {
            public void actionPerformed(final ActionEvent e)
               {
               clipComboBoxModel.refreshModel();
               //clipComboBox.setMinimumSize(clipComboBox.getPreferredSize());
               clipComboBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, clipComboBox.getPreferredSize().height));
               }
            }
      );*/

      final File audioDirectory = VisualProgrammerConstants.FilePaths.AUDIO_DIR;

      importButton.setFocusable(false);


      importButton.addActionListener(
         new ActionListener()
         {
             public void actionPerformed(final ActionEvent e)
             {
                 FileDropTarget drop = new FileDropTarget(".wav");
                 int selection = JOptionPane.showConfirmDialog(null, drop, "Import Audio Clips", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

                 if (selection==JOptionPane.OK_OPTION){
                     Collection<File> new_files = drop.getResults();
                     System.out.println(new_files);
                     
                     FileCopy copier = new FileCopy(panel);
                     
                     for (File file : new_files){
                         try{
                             copier.copy(file.getAbsolutePath(), audioDirectory.getAbsolutePath());

                         }
                         catch(IOException ex){
                            LOG.debug(ex);
                         }
                         
                     }
                     
                 }

                 clipComboBoxModel.refreshModel();
                 clipComboBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, clipComboBox.getPreferredSize().height));
             }
         }
      );

      final GroupLayout layout = new GroupLayout(panel);
      panel.setLayout(layout);
      //panel.setBackground(Color.WHITE);

      layout.setAutoCreateGaps(true);
      layout.setAutoCreateContainerGaps(true);

      layout.setHorizontalGroup(
              layout.createSequentialGroup()
                      .addComponent(clipComboBox)
                              //.addComponent(refreshButton)
                      .addComponent(importButton)
      );
      layout.setVerticalGroup(
              layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                      .addComponent(clipComboBox)
                              //.addComponent(refreshButton)
                      .addComponent(importButton)

      );

      panel.setName("clipChooser");
      }

   public Component getComponent()
      {
      return panel;
      }

   public void setEnabled(final boolean isEnabled)
      {
      clipComboBox.setEnabled(isEnabled);
      refreshButton.setEnabled(isEnabled);
      }

   public boolean isFileSelected()
      {
      return (clipComboBox.getSelectedIndex() > 0);
      }

   public File getSelectedFile()
      {
      if (SwingUtilities.isEventDispatchThread())
         {
         return getSelectedFileWorkhorse();
         }
      else
         {
         final File[] file = new File[1];
         try
            {
            SwingUtilities.invokeAndWait(
                  new Runnable()
                  {
                  public void run()
                     {
                     file[0] = getSelectedFileWorkhorse();
                     }
                  });

            LOG.debug("TerkAudioClipChooser.getSelectedFile(): returning [" + file[0] + "]");
            return file[0];
            }
         catch (InterruptedException e)
            {
            LOG.error("InterruptedException in TerkAudioClipChooser.getSelectedFile()", e);
            }
         catch (InvocationTargetException e)
            {
            LOG.error("InvocationTargetException in TerkAudioClipChooser.getSelectedFile()", e);
            }
         }
      return null;
      }

   private File getSelectedFileWorkhorse()
      {
      if (isFileSelected())
         {
         return convertFilenameToFile((String)clipComboBox.getSelectedItem());
         }
      return null;
      }

   public String getSelectedFilePath()
      {
      final File file = getSelectedFile();

      if (file != null)
         {
         return file.getName();
         }

      return null;
      }

   public void setSelectedFilePath(final String path)
      {
      if (SwingUtilities.isEventDispatchThread())
         {
         setSelectedFilePathWorkhorse(path);
         }
      else
         {
         SwingUtilities.invokeLater(
               new Runnable()
               {
               public void run()
                  {
                  setSelectedFilePathWorkhorse(path);
                  }
               });
         }
      }

   private void setSelectedFilePathWorkhorse(final String path)
      {
      // try to find the given item in the list
      clipComboBox.setSelectedIndex(0);

      if (path != null)
         {
         for (int i = 0; i < clipComboBox.getItemCount(); i++)
            {
            final String item = (String)clipComboBox.getItemAt(i);
            if (path.equals(item))
               {
               clipComboBox.setSelectedIndex(i);
               break;
               }
            }
         }

      for (final AudioClipChooserEventListener listener : audioClipChooserEventListeners)
         {
         listener.handleSelectedFileChange();
         }
      }

   public void addFilePathFieldActionListener(final ActionListener listener)
      {
      // no need to do anything here
      }

   public void addAudioClipChooserEventListener(final AudioClipChooserEventListener listener)
      {
      if (listener != null)
         {
         audioClipChooserEventListeners.add(listener);
         }
      }

   private final class FileComboBoxModel extends DefaultComboBoxModel
      {
      private FileComboBoxModel()
         {
         refreshModel();
         }

      private void refreshModel()
         {
         this.removeAllElements();

         // first item should be blank
         this.addElement("");

         final File audioDirectory = CreateLabConstants.FilePaths.AUDIO_DIR;
         if (audioDirectory.exists() && audioDirectory.isDirectory())
            {
            final File[] files = audioDirectory.listFiles(WAV_FILE_FILTER);
            for (final File file : files)
               {
               this.addElement(file.getName());
               }
            }
         }
      }
   }
