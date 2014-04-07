import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

//clients could interupt server server communication moved it to its own port (pretty much identical code)
public class serverServerFileQuery extends Thread {
    public void run() {
        try {
            DatagramSocket socket = new DatagramSocket(4111); //UDP socket to listen on,
            String requestedFile;
            InetAddress clientIP;
            int clientPort;

            while (true)   //forever
            {
                socket.setSoTimeout(10000);
                System.out.println("Waiting for file request");
                byte[] recBuffer = new byte[15000];
                DatagramPacket packet = new DatagramPacket(recBuffer, recBuffer.length);
                socket.receive(packet);   //read an incoming packet
                clientIP = packet.getAddress();
                clientPort = packet.getPort();
                System.out.println("File request received from " + packet.getAddress().getHostAddress());
                String message = new String(packet.getData()).trim();
                requestedFile = message;

                System.out.println("File request received from a server");
                for (int i = 0; i < Main.clientfiles.size(); ++i)         //for all of our files on record
                {
                    if (Main.clientfiles.get(i).fName.equals(message)) //if the message send matches a file name
                    {                                                 //respond with the first host in the list(ip and port)
                        String temp = Main.clientfiles.get(i).hosts.get(0).IPaddress + ":" + Main.clientfiles.get(i).hosts.get(0).port;
                        byte[] sendData = temp.getBytes();
                        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, packet.getAddress(), packet.getPort());
                        socket.send(sendPacket);
                        System.out.println("Responded with " + Main.clientfiles.get(i).hosts.get(0).IPaddress + ":" + Main.clientfiles.get(i).hosts.get(0).port);
                        break;
                    }
                }


            }

        } catch (Exception excep) {
            System.out.print(excep);
        }
    }
}
