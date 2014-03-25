import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class client extends Thread
{
        protected static boolean serverContinue = true;
        protected Socket socket;

        public client (Socket clientSoc)
        {
            socket = clientSoc;
            start();
        }

        public void run()
        {
            try
            {
                BufferedReader instream = new BufferedReader( new InputStreamReader(socket.getInputStream()));
                OutputStream oustream = socket.getOutputStream();

                String temp = instream.readLine();
                System.out.println("trying to open a file called :"+clientNodeGui.dirName+temp);
                File myFile = new File(clientNodeGui.dirName+temp);

                int count;
                byte[] buffer = new byte[1024];

                BufferedInputStream in = new BufferedInputStream(new FileInputStream(myFile));
                while ((count = in.read(buffer)) > 0)
                {
                    oustream.write(buffer, 0, count);
                    oustream.flush();
                }
                socket.close();
                oustream.close();
                in.close();
            }
            catch (IOException e)
            {
                System.err.println("Problems!");
                System.exit(1);
            }
        }
    }

