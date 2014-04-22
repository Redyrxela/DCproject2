/**********************************************************************************************
 * Distributed computing spring 2014 group 4 //Alex Ryder//Nick Champagne//Hue Moua//
 *                                           //Daniel Gedge//Corey Jones//
 *   Project 2 Peer2Peer client/server
 ***********************************************************************************************/

import java.io.*;
import java.net.Socket;

public class client extends Thread
{
    protected static boolean serverContinue = true;
    protected Socket socket;

    public client(Socket clientSoc)
    {
        socket = clientSoc;
        start();
    }

    public void run()
    {
        try
        {
            BufferedReader instream = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            OutputStream oustream = socket.getOutputStream();
            String temp = instream.readLine();
            System.out.println((new StringBuilder()).append("trying to open a file called :").append(clientNodeGui.dirName).append(temp).toString());
            File myFile = new File((new StringBuilder()).append(clientNodeGui.dirName).append(temp).toString());
            byte buffer[] = new byte[1024];
            BufferedInputStream in = new BufferedInputStream(new FileInputStream(myFile));
            int count;
            while((count = in.read(buffer)) > 0) 
            {
                oustream.write(buffer, 0, count);
                oustream.flush();
            }
            socket.close();
            oustream.close();
            in.close();
        }
        catch(IOException e)
        {
            System.err.println("Problems!");
            System.exit(1);
        }
    }



}
