// CS6263 Project2 Spring 2014
// Alex Ryder
// This program is a simple peer to peer file sharing program with a server to lookup files
//
// This class is spawned from the clientNodeGui when the update button is pressed. it will
// parse the directory chosen in the gui and retrieves all file names, then sends the list
// to the server. the server will reply with an updated list of all files on all clients.

import javax.swing.*;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Created by redyrxela on 3/15/14.
 */
public class clientUpdaterThread extends Thread
{
    public void run()
    {
        File folder = new File(clientNodeGui.dirName); //in this directory
        File[] listOfFiles = folder.listFiles();       //get everyghing in the directory
        int debug = 0;                                 //had some issues with this on a slower computer
        ArrayList<String> filer;                       //list of files
        while(debug<5)     //occasionally had a file error always seems to work within 5
        {
            try
            {
                clientNodeGui.myFiles.clear(); //get rid of our current list of files locally
                clientNodeGui.files.clear();   //get rid of our current list of remote files

                System.out.println("looking at "+clientNodeGui.dirName); //debug where are we looing?

                if (listOfFiles.length > 0) { //if there are files
                    for (int i = 0; i < listOfFiles.length; i++)
                    {
                        if (listOfFiles[i].isFile())
                        { //if it is a file and not a directory
                          //  System.out.println("Found a file called "+listOfFiles[i].getName());
                            if (!clientNodeGui.myFiles.contains(listOfFiles[i].getName()))//if we dont already have it in the list (we shouldnt)
                            {
                                clientNodeGui.myFiles.add(listOfFiles[i].getName());//add its name to the list
                            }
                        }
                    }
                }
                break;
            }
            catch(Exception excep)
            {
                debug++;
                System.out.println("problem with file listing retry :"+debug+"out of 5");//output which debug number if failed on
            }
        }
        try {

            Socket localSocket = new Socket(clientNodeGui.serverAddy, 6666); //connect to the servers tcp port

            localSocket.setSoTimeout(100000);  //timeout incase something goes wrong
            BufferedReader Istream = new BufferedReader(new InputStreamReader(localSocket.getInputStream()));
            OutputStream Ostream = localSocket.getOutputStream();

            String temp = "###"+clientNodeGui.port+"\n"; //tell the server our current port number!
            Ostream.write(temp.getBytes());

            for(int i = 0; i<clientNodeGui.myFiles.size(); ++i)   //for all files
            {
                temp = clientNodeGui.myFiles.get(i)+"\n";
                Ostream.write(temp.getBytes()); //tell the server what files i have
            }
            System.out.println("done Sending files...");
            temp = "EOF\n";
            Ostream.write(temp.getBytes()); //send EOF as escape sequence to notify server thats the end of the list

            System.out.println("sent EOF");

            while(true)     //while the server still is sending files
            {
                temp = Istream.readLine(); //get what the server sends

                //System.out.println("received : "+temp);
                if(temp.equals("EOF")) //if its the end of files on the server
                {
                    localSocket.close();  //close everything
                    Istream.close();
                    break;                //break the infinite loop
                }
                else
                {
                    if(!clientNodeGui.files.contains(temp)) //if we dont have that file in the list (we shouldnt)
                    {
                        clientNodeGui.files.add(temp);     //add it to the list!!
                    }
                }

            }
        } catch (Exception excep)
        {
        }
        System.out.println("Done with update");
    }

}
