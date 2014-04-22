/**********************************************************************************************
 * Distributed computing spring 2014 group 4 //Alex Ryder//Nick Champagne//Hue Moua//
 *                                           //Daniel Gedge//Corey Jones//
 *   Project 2 Peer2Peer client/server
 ***********************************************************************************************/
/**********************************************************************************************
 * This file file is part of the loadbalancing, it udp broadcasts the servers current load
 * so other servers can stay up to date
 ***********************************************************************************************/
import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Enumeration;

public class serverLoadBalancer extends Thread
{
    DatagramSocket clientN;
    public void run()
    {
          try {
                String temp = "@@@"+Main.serverID+":"+Main.numClients;
                byte[] senddata = temp.getBytes();            //prepare valid client data for sending to server
                System.out.println("blasting out to all servers!");
                clientN = new DatagramSocket(); //create a new socket
                clientN.setBroadcast(true);                    //enable broadcasting

                try { //send to the highest order broadcast address
                    DatagramPacket sendPacket = new DatagramPacket(senddata, senddata.length, InetAddress.getByName("255.255.255.255"), 6785);
                    clientN.send(sendPacket);
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

                        } catch (Exception excep)
                        {
                            System.out.println("Error broadcast to : rest of broadcast pools");
                        }
                    }
                }
            }
            catch (Exception excep)
            {
                System.out.println("failed on something major : "+excep);
            }
            clientN.close();
    }



}



