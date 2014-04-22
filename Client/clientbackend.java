/**********************************************************************************************
 * Distributed computing spring 2014 group 4 //Alex Ryder//Nick Champagne//Hue Moua//
 *                                           //Daniel Gedge//Corey Jones//
 *   Project 2 Peer2Peer client/server
 ***********************************************************************************************/
/**********************************************************************************************
 * This file opens as the client starts and associates with a server.
 ***********************************************************************************************/

import java.io.PrintStream;
import java.net.*;
import java.util.*;

public class clientbackend extends Thread
{

    public clientbackend()
    {
    }

    public void run()
    {
        Date date = new Date();
        long initTime;
        long endTime;
        initTime = System.currentTimeMillis( );
        try
        {
            System.out.println("preparing UDP broadcast");
            byte senddata[] = "$$$".getBytes();
            DatagramSocket clientN = new DatagramSocket();
            clientN.setBroadcast(true);
            try
            {
                DatagramPacket sendPacket = new DatagramPacket(senddata, senddata.length, InetAddress.getByName("255.255.255.255"), 6785);
                clientN.send(sendPacket);
                System.out.println("broadcast to : 255.255.255.255");
            }
            catch(Exception excep)
            {
                System.out.println("Failed to broadcast to : 255.255.255.255");
            }
            for(Enumeration interfaces = NetworkInterface.getNetworkInterfaces(); interfaces.hasMoreElements();)
            {
                NetworkInterface networkInterface = (NetworkInterface)interfaces.nextElement();
                if(!networkInterface.isLoopback() && networkInterface.isUp())
                {
                    Iterator i$ = networkInterface.getInterfaceAddresses().iterator();
                    while(i$.hasNext()) 
                    {
                        InterfaceAddress interfaceAddress = (InterfaceAddress)i$.next();
                        InetAddress broadcast = interfaceAddress.getBroadcast();
                        if(broadcast != null)
                            try
                            {
                                DatagramPacket sendPacket = new DatagramPacket(senddata, senddata.length, broadcast, 6785);
                                clientN.send(sendPacket);
                                System.out.println((new StringBuilder()).append("broadcast to : ").append(broadcast).toString());
                            }
                            catch(Exception excep)
                            {
                                System.out.println("Error broadcast to : rest of broadcast pools");
                            }
                    }
                }
            }

            System.out.println("Finished broadcasting");
            byte recBuffer[] = new byte[15000];
            DatagramPacket receivePacket = new DatagramPacket(recBuffer, recBuffer.length);
            clientN.receive(receivePacket);
            String temp = (new String(receivePacket.getData())).trim();
            if(temp.substring(0, 3).equals("%%%"))
            {
                clientNodeGui.serverAddy = receivePacket.getAddress().getHostAddress();
                System.out.println((new StringBuilder()).append("Found server at ").append(clientNodeGui.serverAddy).append(":6666").toString());
            } else
            {
                System.out.println((new StringBuilder()).append("recieved : ").append(temp).toString());
            }
            clientN.close();
        }
        catch(Exception excep)
        {
            System.out.println("failed on something major");
        }
        endTime = System.currentTimeMillis( );
        System.out.println("Associating with the server took "+(endTime-initTime)+" ms to complete");
    }
}
