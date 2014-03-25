// CS6263 Project2 Spring 2014
// Alex Ryder
// This program is a simple peer to peer file sharing program with a server to lookup files
//
// This class monitors a UDP port and listens for clients requesting files, when it recieves
// a request it responds with the host clients ip and port number. the thread is spawned from
// the Main class


import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;

/**
 * Created by Alex on 3/19/14.
 */      
public class serverFileQuery extends Thread
{
    public void run()
    {
        try
        {
            DatagramSocket socket = new DatagramSocket(6666); //UDP socket to listen on,

            while (true)   //forever
            {
                System.out.println("Waiting for file request");
                byte[] recBuffer = new byte[15000];
                DatagramPacket packet = new DatagramPacket(recBuffer,recBuffer.length);
                socket.receive(packet);   //read an incoming packet
                System.out.println("File request received from "+packet.getAddress().getHostAddress());

                String message = new String(packet.getData()).trim();
                for(int i = 0; i<Main.clientfiles.size();++i)         //for all of our files on record
                {
                    if(Main.clientfiles.get(i).fName.equals(message)) //if the message send matches a file name
                    {                                                 //respond with the first host in the list(ip and port)
                        String temp = Main.clientfiles.get(i).hosts.get(0).IPaddress+":"+Main.clientfiles.get(i).hosts.get(0).port;
                        byte[] sendData = temp.getBytes();
                        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length,packet.getAddress(),packet.getPort());
                        socket.send(sendPacket);
                        System.out.println("Responded with "+Main.clientfiles.get(i).hosts.get(0).IPaddress+":"+Main.clientfiles.get(i).hosts.get(0).port);
                        break;
                    }
                }
            }
        }
        catch(IOException except)
        {
            System.out.println("Something went wrong: "+except);
        }
    }
}
                                                                                                             