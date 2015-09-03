package edu.cmu.ri.createlab.visualprogrammer;

import java.awt.Color;
import java.awt.Container;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.usb.UsbDevice;
import javax.usb.UsbDeviceDescriptor;
import javax.usb.UsbException;
import javax.usb.UsbHostManager;
import javax.usb.UsbHub;
import jssc.SerialPortList;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SystemUtils;

/*
 * Hummingbird Firmware Burner - A simple application to upload Hummingbird or custom firmware to the Hummingbird Duo
 * @author Justin Lee
 */

public class HummingbirdFirmwareBurner extends JFrame
   {
   private JFrame frame;
   private final JButton browseButton = new JButton("Browse");
   private JTextField filePath;
   private JLabel statusLabel;
   private JLabel customLabel;
   private JRadioButton revertToHummingbirdModeRadioButton;
   private JRadioButton uploadCustomFirmwareAdvancedRadioButton;

   static int instance = 0;

   public HummingbirdFirmwareBurner()
      {
      initUI();

      browseButton.addActionListener(new ActionListener()
      {
      @Override
      public void actionPerformed(ActionEvent e)
         {

         JFileChooser chooser = new JFileChooser();
         chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
         chooser.setAcceptAllFileFilterUsed(false);
         chooser.setFileFilter(new FileNameExtensionFilter("Microcontroller Firmware Files (.hex)", "hex")); //only .hex files accepted
         int choice = chooser.showOpenDialog(HummingbirdFirmwareBurner.this);
         if (choice == JFileChooser.APPROVE_OPTION)
            {
            filePath.setText(chooser.getSelectedFile().getAbsolutePath());
            }
         }
      });

      frame.addWindowListener(new WindowAdapter()
      {
      @Override
      public void windowOpened(WindowEvent e)
         {
         filePath.setVisible(false);
         browseButton.setVisible(false);
         customLabel.setVisible(false);
         (new DuoChecker()).start();
         }
      });

      ActionListener hider = new ActionListener()
      {
      @Override
      public void actionPerformed(ActionEvent e)
         {
         if (uploadCustomFirmwareAdvancedRadioButton.isSelected())
            {
            filePath.setVisible(true);
            browseButton.setVisible(true);
            customLabel.setVisible(true);
            }
         else
            {
            filePath.setVisible(false);
            browseButton.setVisible(false);
            customLabel.setVisible(false);
            }
         }
      };

      uploadCustomFirmwareAdvancedRadioButton.addActionListener(hider);
      revertToHummingbirdModeRadioButton.addActionListener(hider);
      }

   private void initUI()
      {
      //set up frame
      frame = new JFrame("Hummingbird Firmware Burner");
      frame.setSize(560, 180);
      frame.setResizable(false);
      frame.setLocationRelativeTo(null);
      frame.setLayout(new GridLayout(0, 1));
      frame.addWindowListener(new WindowAdapter()
      {
      public void windowClosing(WindowEvent windowEvent)
         {
         System.exit(0);
         }
      });

      //set up container and panels
      final Container container = new Container();
      container.setName("container");
      container.setLayout(new GridLayout(0, 1));

      JPanel informationPanel = new JPanel();
      informationPanel.setName("informationPanel");
      informationPanel.setLayout(new FlowLayout());

      JPanel statusPanel = new JPanel();
      statusPanel.setName("statusPanel");
      statusPanel.setLayout(new FlowLayout());
      statusPanel.setBackground(Color.DARK_GRAY);

      JPanel radioButtonsPanel = new JPanel();
      radioButtonsPanel.setName("radioButtonsPanel");
      radioButtonsPanel.setLayout(new GridLayout(0, 1));

      JPanel hidePanel = new JPanel();
      hidePanel.setName("hidePanel");
      hidePanel.setLayout(new FlowLayout());

      //set up the lables
      final String instructions = "<html> Make sure your Hummingbird Duo is connected to your computer via the USB cable. <br>" +
                                  "Then, select an option and press the Reset button on your Hummingbird Duo.</html>";

      final JLabel label = new JLabel(instructions);

      statusLabel = new JLabel("Status: Not Ready ");
      statusLabel.setFont(new Font("Arial", Font.PLAIN, 18));
      statusLabel.setForeground(Color.red);

      customLabel = new JLabel("File not yet selected. Browse for custom firmware:");

      //Set up radio buttons
      revertToHummingbirdModeRadioButton = new JRadioButton("Revert to Hummingbird Mode (Regular Option)");
      uploadCustomFirmwareAdvancedRadioButton = new JRadioButton("Upload Custom Firmware (Advanced Option)");

      final ButtonGroup radioButtons = new ButtonGroup();
      radioButtons.add(revertToHummingbirdModeRadioButton);
      radioButtons.add(uploadCustomFirmwareAdvancedRadioButton);

      //set up JTextField
      filePath = new JTextField();
      filePath.setColumns(15);

      frame.add(container);

      container.add(informationPanel);
      container.add(statusPanel);
      container.add(radioButtonsPanel);
      container.add(hidePanel);

      informationPanel.add(label);
      statusPanel.add(statusLabel);
      radioButtonsPanel.add(revertToHummingbirdModeRadioButton);
      radioButtonsPanel.add(uploadCustomFirmwareAdvancedRadioButton);
      hidePanel.add(customLabel);
      hidePanel.add(filePath);
      hidePanel.add(browseButton);

      //Set Visability
      frame.setVisible(true);
      browseButton.setVisible(false);
      customLabel.setVisible(false);
      filePath.setVisible(false);
      revertToHummingbirdModeRadioButton.setSelected(true);
      pack();
      }

   class DuoChecker extends Thread
      {
      private String[] ports = SerialPortList.getPortNames();

      public void run()
         {
         boolean isDoneBurning = false;
         while (!isDoneBurning && instance == 1)
            {
            if (uploadCustomFirmwareAdvancedRadioButton.isSelected())
               {
               if (filePath.getText().equals(""))
                  { //no file chosen yet
                  customLabel.setText("File not yet selected. Browse for custom firmware:");
                  }
               else if ((new File(filePath.getText())).exists() && filePath.getText().substring(filePath.getText().length() - 4).equals(".hex"))
                  { //test for valid custom firmware file
                  customLabel.setText("Valid File Selected. Ready for Upload.");
                  }
               else
                  { //File does not exist or is not a .hex file
                  customLabel.setText("Invalid file selected. Browse for custom firmware:");
                  }
               }
            try
               {
               boolean duo = deviceFound((short)0x2354, (short)0x2333, UsbHostManager.getUsbServices().getRootUsbHub()); //Hummingbird in Arduino mode VID & PID
               boolean hummingbird = deviceFound((short)0x2354, (short)0x2222, UsbHostManager.getUsbServices().getRootUsbHub()); //Hummingbird VID & PID
               boolean leonardo = deviceFound((short)0x2341, (short)0x8036, UsbHostManager.getUsbServices().getRootUsbHub()); //Arduiono Leonardo VID & PID
               String[] newPorts;
               if (duo)
                  { // Hummingbird In Arduino Mode USB exists
                  statusLabel.setForeground(Color.GREEN);
                  statusLabel.setText("Status: Hummingbird in Arduino Mode Connected");
                  }
               else if (leonardo)
                  {//Leonardo USB exist
                  statusLabel.setForeground(Color.CYAN);
                  statusLabel.setText("Status: Hummingbird in Arduino Leonardo Mode Connected");
                  }
               else if (hummingbird)
                  { //Hummingbird USB exists
                  statusLabel.setForeground(Color.GREEN);
                  statusLabel.setText("Status: Hummingbird Connected");
                  frame.dispose();
                  }
               else if (!(Arrays.equals(ports, SerialPortList.getPortNames())))
                  { //test for different set of serial ports
                  boolean found = false;
                  for (int i = 0; i < 5; i++)
                     { //check for bootloader presence a few times since it sometimes takes a second to show up
                     if (deviceFound((short)0x2341, (short)0x0036, UsbHostManager.getUsbServices().getRootUsbHub()))
                        { //Arduino Leonardo bootloader VID & PID - backwards compatibility with beta units
                        found = true;
                        break;
                        }
                     else if (deviceFound((short)0x2354, (short)0x2444, UsbHostManager.getUsbServices().getRootUsbHub()))
                        { //Hummingbird Duo bootloader VID & PID
                        found = true;
                        break;
                        }
                     Thread.sleep(500);
                     }
                  newPorts = SerialPortList.getPortNames(); //refresh list of serial ports
                  if (newPorts.length >= ports.length && found)
                     { //check for new serial port or different serial port
                     String comport = "";
                     if (ports.length == 0)
                        { //different lists of ports and first list is empty
                        if (newPorts.length > 0) //prevent any weird array out of bounds errors that might show up
                           {
                           comport = newPorts[0]; //new serial port must be bootloader
                           }
                        }
                     else
                        {
                        for (String newPort : newPorts)
                           {
                           if (!Arrays.asList(ports).contains(newPort))
                              { //find changed or new Serial port
                              comport = newPort; //bootloader serial port found
                              break;
                              }
                           }
                        }
                     String firmwareFile = "";
                     if (revertToHummingbirdModeRadioButton.isSelected())
                        { //default firmware
                        try
                           {
                           URL url = new URL("http://www.hummingbirdkit.com/sites/default/files/HummingbirdV2.hex");
                           File file = new File("HummingbirdV2.hex");
                           FileUtils.copyURLToFile(url, file, 2000, 2000);
                           firmwareFile = file.getPath();
                           }
                        catch (Exception e)
                           {
                           System.err.println("Error downloading Hummingbird firmware. Trying offline version.");
                           firmwareFile = "HummingbirdV2.hex";
                           }
                        }
                     else if (uploadCustomFirmwareAdvancedRadioButton.isSelected())
                        { //custom firmware
                        firmwareFile = filePath.getText(); //get file browsed for by user
                        if (!(new File(firmwareFile)).exists())
                           {
                           firmwareFile = ""; //make sure user has entered valid file location
                           }
                        }
                     if (!comport.equals("") && !firmwareFile.equals(""))
                        {
                        statusLabel.setForeground(Color.GREEN);
                        if (revertToHummingbirdModeRadioButton.isSelected())
                           {
                           statusLabel.setText("Status: Reset Detected. Trying to revert to Hummingbird mode.");
                           }
                        else
                           {
                           statusLabel.setText("Status: Reset Detected. Attempting to upload custom firmware.");
                           }
                        Process p;
                        String error = "";
                        try
                           {
                           String avrdude = "avrdude";
                           String avrconf = "avrdude.conf";
                           if (SystemUtils.IS_OS_LINUX)
                              {
                              final String arch = System.getProperty("sun.arch.data.model", "");
                              if (arch.equals("64"))
                                 {
                                 avrdude = "./avrdude64";
                                 }
                              else
                                 {
                                 avrdude = "./avrdude";
                                 }
                              }
                           else if (SystemUtils.IS_OS_MAC_OSX)
                              {
                              avrdude = "./avrdude_mac";
                              avrconf = "./avrdude.conf";
                              }
                           String[] command = {avrdude, "-p", "atmega32u4", "-P", comport, "-c", "avr109", "-C", avrconf, "-b", "9600", "-U", "flash:w:" + firmwareFile + ":i"};

                           //run avrdude
                           p = Runtime.getRuntime().exec(command);
                           BufferedReader reader = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                           String currentLine;

                           while ((currentLine = reader.readLine()) != null)
                              { //Blocking read from avrdude error stream
                              error += currentLine + "\n";
                              }
                           }
                        catch (Exception e)
                           { //problem running avrdude
                           System.err.println("Error running avrdude");
                           e.printStackTrace();
                           }

                        if (error.indexOf("done.  Thank you.") == -1)
                           { //Check for lack of success message
                           System.err.println(error);
                           JOptionPane.showMessageDialog(null, "Error uploading firmware. Please try again." + error);
                           }
                        else if (revertToHummingbirdModeRadioButton.isSelected())
                           {
                           JOptionPane.showMessageDialog(null, "Done! The status LED should be slowly fading in and out.");
                           isDoneBurning = true;
                           frame.dispose();
                           }
                        else
                           {
                           JOptionPane.showMessageDialog(null, "Done uploading custom firmware.");
                           isDoneBurning = true;
                           frame.dispose();
                           }
                        }
                     }
                  }
               else
                  {
                  statusLabel.setForeground(Color.YELLOW);
                  statusLabel.setText("Status: Reset Button Pressed or No Hummingbird Duo Found");
                  }
               ports = SerialPortList.getPortNames();
               Thread.sleep(500);
               }
            catch (UsbException usbEx)
               {
               statusLabel.setText("Status: Error with USB connection");
               usbEx.printStackTrace();
               }
            catch (InterruptedException iEx)
               {
               iEx.printStackTrace();
               }
            }
         instance--;
         }

      public boolean deviceFound(short vid, short pid, UsbHub hub)
         {
         for (UsbDevice device : (List<UsbDevice>)hub.getAttachedUsbDevices())
            { //iterate through all USB devices
            UsbDeviceDescriptor descriptor = device.getUsbDeviceDescriptor();
            if ((descriptor.idVendor() == vid && descriptor.idProduct() == pid) || //matching device VID & PID
                (device.isUsbHub() && deviceFound(vid, pid, (UsbHub)device))) //if device is hub, search devices in hub
               {
               return true;
               }
            }
         return false; //return false if no devices found
         }
      }

   public static void burn()
      {

      instance++;
      if (instance == 1)
         {
         EventQueue.invokeLater(new Runnable()
         {
         @Override
         public void run()
            {
            HummingbirdFirmwareBurner burner = new HummingbirdFirmwareBurner();
            }
         });
         }
      }
   }
