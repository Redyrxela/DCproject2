// CS6263 Project2 Spring 2014
// Alex Ryder
// This program is a simple peer to peer file sharing program with a server to lookup files
//
// This class sends a file requested by another client to them, it uses a TCP connection and the file name
// it is spawned from the clientSenderHandler

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by Alex on 3/19/14.
 */
public class clientSender extends Thread
{
    private  Socket socket;

    public clientSender (Socket clientSoc)
    {
        socket = clientSoc;
        start();
    }
    public void run()  //if we are here a client is connected and about to request a file
    {
        try
        {
            BufferedReader instream = new BufferedReader( new InputStreamReader(socket.getInputStream()));

            String temp = instream.readLine(); //get the name of the file requested
            System.out.println("trying to open a file called :"+clientNodeGui.dirName+temp);
            File myFile = new File(clientNodeGui.dirName+temp); //open the file they requested

            int count;
            byte[] buffer = new byte[1024];

            //input from the file and immediately output it out the TCP socket

            //for compatibility with binary files

            DataOutputStream outData = new DataOutputStream(socket.getOutputStream());

            //send file size

            outData.writeLong(myFile.length());
            outData.flush();


            FileInputStream fis = new FileInputStream(myFile);

            while((count = fis.read(buffer))> 0)
            {
                outData.write(buffer,0,count);
            }
            fis.close();
            outData.flush();
            outData.close();
        }
        catch (IOException e)
        {
            System.err.println("Problem sending file!");
        }

    }
}









