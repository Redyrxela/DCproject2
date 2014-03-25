// CS6263 Project2 Spring 2014
// Alex Ryder
// This program is a simple peer to peer file sharing program with a server to lookup files
//
// This class is spawned in the clientNodeGui it is a background UDP broadcasting thread that
// finds the server without knowing the IP. it does this by broadcasting to the broadcast
// address for every network interface the client has and listens for a response.
import java.io.IOException;
import java.net.*;
import java.util.Enumeration;

/**
 * Created by redyrxela on 3/14/14.
 */
public class clientbackend extends Thread
{

    public void run()
    {
    try {
        System.out.println("preparing UDP broadcast"); //debug output to console
        byte[] senddata = "$$$".getBytes();            //prepare valid client data for sending to server
        DatagramSocket clientN = new DatagramSocket(); //create a new socket
        clientN.setBroadcast(true);                    //enable broadcasting

        try { //send to the highest order broadcast address
            DatagramPacket sendPacket = new DatagramPacket(senddata, senddata.length, InetAddress.getByName("255.255.255.255"), 6785);
            clientN.send(sendPacket);
            System.out.println("broadcast to : 255.255.255.255");
        }
        catch (Exception excep) //this should never fail
        {
            System.out.println("Failed to broadcast to : 255.255.255.255");
        }
        //next load all network interfaces into a list
        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        while (interfaces.hasMoreElements()) //for all network interfaces
        {
            NetworkInterface networkInterface = interfaces.nextElement();
            if (networkInterface.isLoopback() || !networkInterface.isUp())//if its a 127.0.0.1 (local address) or not connected
                continue;                                                 //skip it
            for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) {
                InetAddress broadcast = interfaceAddress.getBroadcast();
                if (broadcast == null) //if broadcast isnt allowed
                    continue;          //skip it
                try {
                    DatagramPacket sendPacket = new DatagramPacket(senddata, senddata.length, broadcast, 6785);
                    clientN.send(sendPacket);   //send the packet to the broadcast on all valid interfaces
                    System.out.println("broadcast to : " + broadcast);
                } catch (Exception excep)
                {
                    System.out.println("Error broadcast to : rest of broadcast pools");
                }
            }
        }
        System.out.println("Finished broadcasting");
        byte[] recBuffer = new byte[15000];

        DatagramPacket receivePacket = new DatagramPacket(recBuffer, recBuffer.length);
        clientN.receive(receivePacket); //receive a response, hopefully its from the server

        String temp = new String(receivePacket.getData()).trim(); //trim extra chars like \n

        if (temp.equals("%%%"))   //if its a valid response from the server
        {
            clientNodeGui.serverAddy = receivePacket.getAddress().getHostAddress(); //get the servers address from the packet
            System.out.println("Found server at "+clientNodeGui.serverAddy+":6666"); //server uses fixed port. debug output
        }
        else
        {
            System.out.println("recieved : " + temp);  //well what the hell did we recieve!?!
        }

        clientN.close(); //we should be done here
    }
    catch (Exception excep)
    {
        System.out.println("failed on something major");
    }


    }
}