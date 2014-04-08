import java.io.*;
import java.net.Socket;

public class clientSender extends Thread
{
    private Socket socket;

    public clientSender(Socket clientSoc)
    {
        socket = clientSoc;
        start();
    }

    public void run()
    {
        try
        {
            BufferedReader instream = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String temp = instream.readLine();
            System.out.println((new StringBuilder()).append("trying to open a file called :").append(clientNodeGui.dirName).append(temp).toString());
            File myFile = new File((new StringBuilder()).append(clientNodeGui.dirName).append(temp).toString());
            byte buffer[] = new byte[1024];
            DataOutputStream outData = new DataOutputStream(socket.getOutputStream());
            outData.writeLong(myFile.length());
            outData.flush();
            FileInputStream fis = new FileInputStream(myFile);
            int count;
            while((count = fis.read(buffer)) > 0) 
                outData.write(buffer, 0, count);
            fis.close();
            outData.flush();
            outData.close();
        }
        catch(IOException e)
        {
            System.err.println("Problem sending file!");
        }
    }


}
