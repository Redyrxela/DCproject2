import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Enumeration;

public class serverLoadBalancer extends Thread
{
    DatagramSocket socket;
    public void run()
    {

        Main.servers = new ArrayList<serverLoads>();

          try {
                byte[] senddata = "@@@".getBytes();            //prepare valid client data for sending to server
                DatagramSocket clientN = new DatagramSocket(); //create a new socket
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
                byte[] recBuffer = new byte[15000];

                DatagramPacket receivePacket = new DatagramPacket(recBuffer, recBuffer.length);
                clientN.receive(receivePacket); //receive a response, hopefully its from the server

                String temp = new String(receivePacket.getData()).trim(); //trim extra chars like \n
                serverLoads tempSL = new serverLoads();
                if(temp.substring(0, 3).equals("%%%"))//if its a server responding
                {
                   String[] parts = temp.substring(3,temp.length()).split(":");
                   tempSL.idNum = Integer.parseInt(parts[0]);
                   tempSL.numClients = Integer.parseInt(parts[1]);
                   tempSL.serverIP = receivePacket.getAddress().getHostAddress();

                   boolean found = false;
                   for(int i =0; i< Main.servers.size(); ++i)
                   {
                       if(Main.servers.get(i).idNum==tempSL.idNum)
                       {
                           Main.servers.get(i).numClients = tempSL.numClients;
                           found = true;
                           break;
                       }
                   }
                   if(!found)
                       Main.servers.add(tempSL);

                }
            }
            catch (Exception excep)
            {
                System.out.println("failed on something major : "+excep);
            }

    }



}



