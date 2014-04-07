// This class monitors a UDP port and listens for clients requesting files, when it recieves
// a request it responds with the host clients ip and port number. the thread is spawned from
// the Main class


import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;

/**
 * Created by Alex on 3/19/14.
 */      
public class serverFileQuery extends Thread
{
    byte[] sendData;
    DatagramPacket sendPacket;
    public void run()
    {
        try
        {
            DatagramSocket ssFQ = new DatagramSocket(); //create a new socket
            DatagramSocket socket = new DatagramSocket(6666); //UDP socket to listen on,
            String requestedFile;
            InetAddress clientIP;
            int clientPort;
            while (true)   //forever
            {
                socket.setSoTimeout(10000);
                System.out.println("Waiting for file request");
                byte[] recBuffer = new byte[15000];
                DatagramPacket packet = new DatagramPacket(recBuffer,recBuffer.length);
                socket.receive(packet);   //read an incoming packet
                clientIP = packet.getAddress();
                clientPort = packet.getPort();
                System.out.println("File request received from "+packet.getAddress().getHostAddress());
                String message = new String(packet.getData()).trim();
                requestedFile = message;

                    boolean found=false;
                    for(int i = 0; i<Main.clientfiles.size();++i)         //for all of our files on record
                    {
                        if(Main.clientfiles.get(i).fName.equals(message)) //if the message send matches a file name
                        {                                                 //respond with the first host in the list(ip and port)
                            String temp = Main.clientfiles.get(i).hosts.get(0).IPaddress+":"+Main.clientfiles.get(i).hosts.get(0).port;
                            sendData = temp.getBytes();
                            sendPacket = new DatagramPacket(sendData, sendData.length,packet.getAddress(),packet.getPort());
                            socket.send(sendPacket);
                            System.out.println("Responded with "+Main.clientfiles.get(i).hosts.get(0).IPaddress+":"+Main.clientfiles.get(i).hosts.get(0).port);
                            found = true;
                            break;
                        }
                    }
                    if(!found) //it not listed on this server
                    {
                        for(int i = 0; i<Main.servers.size();++i)//ask all servers
                        {
                            if (Main.servers.get(i).idNum!=Main.serverID) //ask all servers for the file name
                            {
                                sendData = requestedFile.getBytes();
                                sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName(Main.servers.get(i).serverIP),4111);
                                ssFQ.send(sendPacket); //blast it out this port
                            }
                        }
                        ssFQ.receive(packet);   //read an incoming packet i hope its correct i'm not going to check it!
                        message = new String(packet.getData()).trim();
                        sendData = message.getBytes();
                        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, clientIP,clientPort);
                        socket.send(sendPacket);       //forward anything recieved back to the client... it had better be correct

                    }

            }
        }
        catch(IOException except)
        {
            System.out.println("Something went wrong: "+except);
        }
    }
}
                                                                                                             