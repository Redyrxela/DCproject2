/**********************************************************************************************
 * Distributed computing spring 2014 group 4 //Alex Ryder//Nick Champagne//Hue Moua//
 *                                           //Daniel Gedge//Corey Jones//
 *   Project 2 Peer2Peer client/server
 ***********************************************************************************************/
/**********************************************************************************************
 * This file spawns with the client, it is a server lister that responds to file requests
 ***********************************************************************************************/

import java.io.PrintStream;
import java.net.ServerSocket;

public class clientSenderHandler extends Thread
{
    ServerSocket fs;
    clientSender fileserver;

    public clientSenderHandler()
    {
    }

    public void run()
    {
        try
        {
            fs = new ServerSocket(clientNodeGui.port);
        }
        catch(Exception e)
        {
            System.out.println("problem with initial file sender setup");
        }
        try
        {
            do
            {
                while(!clientNodeGui.clientSend) ;
                try
                {
                    if(clientNodeGui.port != fs.getLocalPort())
                    {
                        fs.close();
                        System.out.println((new StringBuilder()).append("Opened port ").append(clientNodeGui.port).append(" for sending files").toString());
                        fs = new ServerSocket(clientNodeGui.port);
                    }
                    fs.setSoTimeout(0x186a0);
                    fileserver = new clientSender(fs.accept());
                }
                catch(Exception e)
                {
                    System.out.println((new StringBuilder()).append("Client sender handler : ").append(e).toString());
                    System.out.println("(handler times out to allow port change)");
                }
            } while(true);
        }
        catch(Exception e)
        {
            System.out.println("But this should be impossible!");
        }
        clientNodeGui.clientSenderExists = false;
    }


}
