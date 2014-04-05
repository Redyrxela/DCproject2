import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class clientUpdaterThread extends Thread
{

    public clientUpdaterThread()
    {
    }

    public void run()
    {
        File folder = new File(clientNodeGui.dirName);
        File listOfFiles[] = folder.listFiles();
        int debug = 0;
        do
        {
            if(debug >= 5)
                break;
            try
            {
                clientNodeGui.myFiles.clear();
                clientNodeGui.files.clear();
                System.out.println((new StringBuilder()).append("looking at ").append(clientNodeGui.dirName).toString());
                if(listOfFiles.length > 0)
                {
                    for(int i = 0; i < listOfFiles.length; i++)
                        if(listOfFiles[i].isFile() && !clientNodeGui.myFiles.contains(listOfFiles[i].getName()))
                            clientNodeGui.myFiles.add(listOfFiles[i].getName());

                }
                break;
            }
            catch(Exception excep)
            {
                debug++;
                System.out.println((new StringBuilder()).append("problem with file listing retry :").append(debug).append("out of 5").toString());
            }
        } while(true);
        try
        {
            Socket localSocket = new Socket(clientNodeGui.serverAddy, 6666);
            localSocket.setSoTimeout(0x186a0);
            BufferedReader Istream = new BufferedReader(new InputStreamReader(localSocket.getInputStream()));
            OutputStream Ostream = localSocket.getOutputStream();
            String temp = (new StringBuilder()).append("###").append(clientNodeGui.port).append("\n").toString();
            Ostream.write(temp.getBytes());
            for(int i = 0; i < clientNodeGui.myFiles.size(); i++)
            {
                temp = (new StringBuilder()).append((String)clientNodeGui.myFiles.get(i)).append("\n").toString();
                Ostream.write(temp.getBytes());
            }

            System.out.println("done Sending files...");
            temp = "EOF\n";
            Ostream.write(temp.getBytes());
            System.out.println("sent EOF");
            do
            {
                temp = Istream.readLine();
                if(temp.equals("EOF"))
                {
                    localSocket.close();
                    Istream.close();
                    break;
                }
                if(!clientNodeGui.files.contains(temp))
                    clientNodeGui.files.add(temp);
            } while(true);
        }
        catch(Exception excep) { }
        System.out.println("Done with update");
    }
}
