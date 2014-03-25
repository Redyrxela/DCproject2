// CS6263 Project2 Spring 2014
// Alex Ryder
// This program is a simple peer to peer file sharing program with a server to lookup files
//
// This class creates a GUI on the client nodes. clint picks a directory and port number and
// then clicks update to update the server of the files it currently has. it will self
// authenticate with the server, and retrieve a list of files we dont have. a user may then
// select one of these files and click retrieve. the gui will be pauses and the file will
// be retrieved from whatever client is hosting that file.


import com.sun.javafx.property.adapter.PropertyDescriptor;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Enumeration;

/**
 * Created by redyrxela on 3/14/14.
 */
public class clientNodeGui {
    private JTextField portInput; //port input on gui
    public JList fileList;        //list of remote files on gui
    private JButton retrieveButton;
    private JPanel mainPanel;
    private JTextField directoryName; //the name of the directory to look in (field on gui)
    private JLabel Directory;
    private JButton Update;
    public JList outputWindow;        //the list of local files on gui
    private JProgressBar RBar;


    public static String dirName;     //static reference to the directory chosen on gui
    public static int port;           //static reference to the port chosen on gui

    public static ArrayList<String> files;   //remote files (Static)
    public static ArrayList<String> myFiles;  //local files (static)

        public static String serverAddy; //the ipaddress of the server

    public static boolean clientSend; //when we want the file server portion of the gui running
    public static boolean clientSenderExists;
    DefaultListModel<String> listModel = new DefaultListModel<String>(); // for adapting arraylists to jlists
    DefaultListModel<String> myListModel = new DefaultListModel<String>();


    private void createUIComponents() {
        outputWindow = new JList(myListModel);   //link jlist to list models
        fileList = new JList(listModel);
        // TODO: place custom component creation code here
    }

    public clientNodeGui() {
        $$$setupUI$$$();
        //when the update button is pushed
        Update.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                if(!clientSenderExists)
                {
                    clientSenderHandler CSH = new clientSenderHandler(); //start a file server
                    CSH.start();
                }
                try
                {
                    clientSend = false;   //the file host should stop sending
                    getFileList();        //we should get the list of files now

                } catch (Exception excep) {
                    System.out.println("we failed to get file list :" + excep);
                }
            }
        });
        //when the retrieve button is pushed
        retrieveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    if (!fileList.isSelectionEmpty()) //if they have something selected
                        getFileHandler();   //get the file!!
                } catch (Exception excep) {
                    System.out.println("failed to retrieve file :" + excep);
                }
            }
        });

        directoryName.addFocusListener(new FocusListener() {
            @Override
            public void focusLost(FocusEvent arg0) {

                if (directoryName.getText().charAt(directoryName.getText().length() - 1) != '/') //if the end of the string is not a /

                {
                    directoryName.setText(directoryName.getText() + "/"); //add the / to the end will error out without it
                }

            }

            public void focusGained(FocusEvent arg0) {
            }
        });

    }


    public static void main(String[] args) {
        port = 8888; //default port
        clientSend = true; //client is ok to start a file server
        files = new ArrayList<String>(); //init local and remote file listings
        myFiles = new ArrayList<String>();
        clientSenderExists = false;
        JFrame frame = new JFrame("clientNodeGui");             //start teh gui
        frame.setContentPane(new clientNodeGui().mainPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
        System.out.println("booting gui");

        clientbackend BEnd = new clientbackend(); //authenticate and register with server
        BEnd.start();

    }

    void getFileList() {
        fileList.setEnabled(false); //lock the file list
        port = Integer.parseInt(portInput.getText()); //get the current port number
        dirName = directoryName.getText();            //get the current directory
        clientUpdaterThread CU = new clientUpdaterThread();  //run the update thread
        CU.start();
        try
        {
            CU.join();
        }
        catch (Exception e)
        {
            System.out.println("Error waiting on the client updater : "+e);
        }

        fileList.clearSelection(); //they dont have anything selected any more

        listModel.removeAllElements(); //get rid of all gui listings
        myListModel.removeAllElements();


        for (int i = 0; i < files.size(); i++) { //get the new listing
            if (!myFiles.contains(files.get(i))) //if i dont have this file then display it
                listModel.addElement(files.get(i));

        }

        for (int i = 0; i < myFiles.size(); i++) //list files i do have
            myListModel.addElement(myFiles.get(i));

        clientSend = true;      //we are good to run the file server again
        fileList.setEnabled(true);  //we can unlock the file list
    }

    void getFileHandler() {
        fileList.setEnabled(false); //(lock the file list) whole gui is locked anyway
        System.out.println("Requesting " + fileList.getSelectedValue().toString() + " from the server");

        Thread getter = new Thread(new clientReciever());
        getter.start();

    }

    class clientReciever implements Runnable
    {
        public void run()
        {
            fileList.setEnabled(false); //(lock the file list) whole gui is locked anyway
            System.out.println("Requesting " + fileList.getSelectedValue().toString() + " from the server");

            String hostIP = ""; //we use defaults until server gives us the correct info
            int hostPort = 8888;

            try {
                byte[] senddata = fileList.getSelectedValue().toString().getBytes(); //get file name user wants
                DatagramSocket clientN = new DatagramSocket();
                clientN.setSoTimeout(10000);

                try {                             //request the file from the server
                    DatagramPacket sendPacket = new DatagramPacket(senddata, senddata.length, InetAddress.getByName(clientNodeGui.serverAddy), 6666);
                    clientN.send(sendPacket);
                    System.out.println("Requested file");
                } catch (Exception excep) {
                    System.out.println("Error in file request");
                }

                byte[] recBuffer = new byte[15000];

                DatagramPacket receivePacket = new DatagramPacket(recBuffer, recBuffer.length);
                clientN.receive(receivePacket); //get response from server

                String temp = new String(receivePacket.getData()).trim();

                //seperate the data recieved at the : (IP:port)

                String[] parts = temp.split(":");
                hostIP = parts[0];               //first half is a host IP address
                hostPort = Integer.parseInt(parts[1]);  //second half is a host port

                System.out.println("File is hosted at " + hostIP + ":" + hostPort);

                clientN.close();  //close this connection
            } catch (Exception excep) {
                System.out.println("failed on something major");
            }


            try {
                Socket socket = new Socket(InetAddress.getByName(hostIP), hostPort); //connect to the host we were told about

                System.out.println("We connected to " + socket.getInetAddress().getHostName() + ":" + socket.getPort());

                OutputStream outstreams = socket.getOutputStream();


                String fileWanted = fileList.getSelectedValue().toString() + "\n";

                //request the file from that host
                outstreams.write(fileWanted.getBytes());

                System.out.println("we asked the host for " + fileList.getSelectedValue().toString());

                //for compatibility with binary files
                byte[] buffer = new byte[1024];


                DataInputStream inData = new DataInputStream(socket.getInputStream());

                RBar.setStringPainted(true);

                long fileSize=inData.readLong();
                RBar.setMaximum((int)fileSize);

                File myFile = new File(directoryName.getText() + fileList.getSelectedValue().toString());
                FileOutputStream fos = new FileOutputStream(myFile);
                int count;

                while((count = inData.read(buffer))> 0)
                {
                    fos.write(buffer,0,count);
                    RBar.setValue(RBar.getValue()+count);

                }

                System.out.println("we actually got here!");

                //we are done close everything
                fos.close();
                outstreams.close();
                inData.close();
                socket.close();
            } catch (Exception excep) {
                System.out.println("file recieving error : " + excep);
            }

            getFileList();     //update the server and the gui to show we have the file!!
            RBar.setValue(0);
            RBar.setStringPainted(false);
            fileList.setEnabled(true);
        }

    }


    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        createUIComponents();
        mainPanel = new JPanel();
        mainPanel.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(5, 2, new Insets(0, 0, 0, 0), -1, -1));
        final JLabel label1 = new JLabel();
        label1.setText("PORT NUMBER");
        mainPanel.add(label1, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        directoryName = new JTextField();
        directoryName.setText("c:/");
        mainPanel.add(directoryName, new com.intellij.uiDesigner.core.GridConstraints(0, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        Directory = new JLabel();
        Directory.setText("DIRECTORY");
        mainPanel.add(Directory, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        portInput = new JTextField();
        portInput.setText("8888");
        mainPanel.add(portInput, new com.intellij.uiDesigner.core.GridConstraints(1, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        Update = new JButton();
        Update.setText("Update");
        Update.setToolTipText("retrieve uptodate file list of files you dont have");
        mainPanel.add(Update, new com.intellij.uiDesigner.core.GridConstraints(2, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        retrieveButton = new JButton();
        retrieveButton.setText("Retrieve");
        mainPanel.add(retrieveButton, new com.intellij.uiDesigner.core.GridConstraints(2, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("My Files");
        mainPanel.add(label2, new com.intellij.uiDesigner.core.GridConstraints(4, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JScrollPane scrollPane1 = new JScrollPane();
        mainPanel.add(scrollPane1, new com.intellij.uiDesigner.core.GridConstraints(3, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        scrollPane1.setViewportView(fileList);
        final JLabel label3 = new JLabel();
        label3.setText("Remote Files");
        mainPanel.add(label3, new com.intellij.uiDesigner.core.GridConstraints(3, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JScrollPane scrollPane2 = new JScrollPane();
        mainPanel.add(scrollPane2, new com.intellij.uiDesigner.core.GridConstraints(4, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        outputWindow.setEnabled(false);
        scrollPane2.setViewportView(outputWindow);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return mainPanel;
    }
}

