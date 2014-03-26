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
import java.net.InetAddress;
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
                if(message.equals("###SERVER###"))
                {
                    socket.receive(packet);   //read an incoming packet
                    System.out.println("File request received from a server");
                    message = new String(packet.getData()).trim();
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
                else
                {
                    boolean found=false;
                    for(int i = 0; i<Main.clientfiles.size();++i)         //for all of our files on record
                    {
                        if(Main.clientfiles.get(i).fName.equals(message)) //if the message send matches a file name
                        {                                                 //respond with the first host in the list(ip and port)
                            String temp = Main.clientfiles.get(i).hosts.get(0).IPaddress+":"+Main.clientfiles.get(i).hosts.get(0).port;
                            byte[] sendData = temp.getBytes();
                            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length,packet.getAddress(),packet.getPort());
                            socket.send(sendPacket);
                            System.out.println("Responded with "+Main.clientfiles.get(i).hosts.get(0).IPaddress+":"+Main.clientfiles.get(i).hosts.get(0).port);
                            found = true;
                            break;
                        }
                    }
                    if(!found) //it not listed on this server
                    {
                        String temp = "###SERVER###";
                        byte[] sendData = temp.getBytes();
                        for(int i = 0; i<Main.servers.size();++i)//ask all servers
                        {
                            if (Main.servers.get(i).idNum!=Main.serverID)
                            {
                                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName(Main.servers.get(i).serverIP),6666);
                                socket.send(sendPacket);
                                sendData = requestedFile.getBytes();
                                sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName(Main.servers.get(i).serverIP),6666);
                                socket.send(sendPacket);
                            }
                        }
                        socket.receive(packet);   //read an incoming packet i hope its correct i'm not going to check it!
                        message = new String(packet.getData()).trim();
                        sendData = message.getBytes();
                        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, clientIP,clientPort);
                        socket.send(sendPacket);       //forward anything recieved back to the client... it had better be correct
                                                        //if i have time i'll put the server sync stuff into another thread on a diff port


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
                                                                                                             