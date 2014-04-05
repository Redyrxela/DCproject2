// This class is spawned in the Main class, it is a background thread that listens on a UDP
// port for a broadcast message. when the broadcast is seen it will send a message directly
// to the sender of the broadcast. this allows the client to find the server without knowing
// the IP address.

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * Created by redyrxela on 3/14/14.
 */
public class BCthread extends Thread
{
    DatagramSocket socket;  //listening socket
    @Override
    public void run()
    {
        try
        {
            socket = new DatagramSocket(6785); //listen on a specific port
            socket.setBroadcast(true);         //we want to listen to broadcasts
            String myaddy = "%%%";             //Originally was going to send an IP, but decided to use 3 chars just
            while (true)                       // so the client could authenticate the type of message it recieved.
            {   //forever
               // System.out.println("Waiting for clients");//output to console if present the current status
                byte[] recBuffer = new byte[15000];       //buffer for received information
                DatagramPacket packet = new DatagramPacket(recBuffer,recBuffer.length); //packet for received information
                socket.receive(packet); //receive the broadcast message
               // System.out.println("Packet received from "+packet.getAddress().getHostAddress());// output that we found one

                String message = new String(packet.getData()).trim();//trim any extra chars off the end \n etc

                if(message.equals("$$$")) //if the message is a valid signal from a client
                {
                    int lowestClients =100000;
                    int lowestID = 1000000;
                    boolean same = false;
                    for(int i = 0; i<Main.servers.size();++i)
                    {    //for all servers
                        if(Main.servers.get(i).idNum!=Main.serverID) //if its not me
                        {
                            if(Main.servers.get(i).numClients<lowestClients)
                            {   //do i have the lowest clients
                                lowestClients = Main.servers.get(i).numClients;
                                same = false;
                            }
                            if(Main.servers.get(i).numClients==lowestClients)
                            {
                                same=true;
                            }

                            if(Main.servers.get(i).idNum<lowestID)
                            {   //do i have the lowest clients
                                lowestID = Main.servers.get(i).idNum;
                            }
                        }
                    }
                    //if we have the lowest number
                    if(Main.numClients==lowestClients)
                    {
                        if(same)//but someone else has the same number
                        {
                            if(lowestID==Main.serverID)//but we have the lower ID number!
                            {
                                myaddy = "%%%";
                                byte[] sendData = myaddy.getBytes(); // make a packet that is the correct response of %%%
                                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length,packet.getAddress(),packet.getPort());
                                socket.send(sendPacket); //send response to client
                                System.out.println("Responded with "+myaddy);
                                Main.numClients++;

                                serverLoadBalancer SLB = new serverLoadBalancer();//now update the number of clients we have.
                                SLB.start();

                            }
                        }
                    }
                }
                else  //when we received other data in error
                {
                    if(message.equals("@@@"))
                    {
                        myaddy = "%%%"+Main.serverID+":"+Main.numClients;
                        byte[] sendData = myaddy.getBytes(); // make a packet that is the correct response of %%%
                        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length,packet.getAddress(),packet.getPort());
                        socket.send(sendPacket);

                    }
                    else
                        System.out.println("not sure what happened there we recieved :"+message);

                }
            }
        }
        catch(IOException except) //rare occurance of problems output them to console
        {
            System.out.println("Something went wrong: "+except);
        }
    }
    public static BCthread getInstance()
    {
        return BCthreadHolder.INSTANCE;
    }
    private static class BCthreadHolder
    {
        private static final BCthread INSTANCE = new BCthread();
    }

}
