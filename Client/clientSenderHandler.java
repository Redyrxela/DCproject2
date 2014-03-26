// CS6263 Project2 Spring 2014
// Alex Ryder
// This program is a simple peer to peer file sharing program with a server to lookup files
//
// This class is spawned from the clientNodeGui class. it waits for an incoming TCP connection
// on the port chosen in the gui. it will time out and stop occasionally to make sure its in
// sync with the gui. when a connection is made it will spawn the clientSender

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by Alex on 3/19/14.
 */
public class clientSenderHandler extends Thread
{
    ServerSocket fs;
    clientSender fileserver;
    public void run()
    {
       clientNodeGui.clientSenderExists = true;
       try
       {
         fs = new ServerSocket(clientNodeGui.port); // when we start grab the port from the gui and listen on it
       }
       catch(Exception e)
       {
            System.out.println("problem with initial file sender setup");
       }
       try
       {
           while(true)    //forever
           {
               while(clientNodeGui.clientSend) //when we want the sender working (disabled during updates)
               {
                  try
                  {
                       if(clientNodeGui.port!=fs.getLocalPort()) //if the user changed the port in the gui
                       {
                            fs.close();                   //close the old port
                            System.out.println("Opened port " + clientNodeGui.port + " for sending files");
                            fs = new ServerSocket(clientNodeGui.port); //open with new port number
                       }
                       fs.setSoTimeout(100000); //set a time out long enough to send files but short enough for the gui to be
                                                //able to update
                       fileserver = new clientSender(fs.accept()); //spawn a new thread when a client has connected
                  }
                  catch(Exception e)
                  {
                      System.out.println("Client sender handler : "+e);
                      System.out.println("(handler times out to allow port change)");
                  }
               }
           }
       }
       catch(Exception e)
       {
            System.out.println("But this should be impossible!");
       }
       clientNodeGui.clientSenderExists = false; //should never get here but if it does and update will make a new one
   }
}
