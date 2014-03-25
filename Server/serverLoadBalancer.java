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
        slResponder SLR = new slResponder();
        SLR.start();

        while(true)
        {
            try {
                byte[] senddata = "@@@".getBytes();            //prepare valid client data for sending to server
                DatagramSocket clientN = new DatagramSocket(); //create a new socket
                clientN.setBroadcast(true);                    //enable broadcasting

                try { //send to the highest order broadcast address
                    DatagramPacket sendPacket = new DatagramPacket(senddata, senddata.length, InetAddress.getByName("255.255.255.255"), 6789);
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
                            DatagramPacket sendPacket = new DatagramPacket(senddata, senddata.length, broadcast, 6789);
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
                   tempSL.ipAddress=receivePacket.getAddress().getHostAddress();
                   tempSL.numClients = Integer.parseInt(temp.substring(3,temp.length()));
                   boolean found = false;
                   for(int i =0; i< Main.servers.size(); ++i)
                   {
                       if(Main.servers.get(i).ipAddress==tempSL.ipAddress)
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
                System.out.println("failed on something major");
            }
        }
    }

    class slResponder extends Thread
    {

            @Override
            public void run()
            {
                try
                {
                    socket = new DatagramSocket(6789); //listen on a specific port
                    socket.setBroadcast(true);         //we want to listen to broadcasts
                    String myaddy;             //Originally was going to send an IP, but decided to use 3 chars just
                    while (true)                       // so the client could authenticate the type of message it recieved.
                    {   //forever
                        myaddy = "%%%"+Main.numClients;
                        byte[] recBuffer = new byte[15000];       //buffer for received information
                        DatagramPacket packet = new DatagramPacket(recBuffer,recBuffer.length); //packet for received information
                        socket.receive(packet); //receive the broadcast message
                        String message = new String(packet.getData()).trim();//trim any extra chars off the end \n etc
                        if(checkInetAddress(packet.getAddress()))
                        {
                            System.out.println("gecko");
                            if(message.equals("@@@")) //if the message is a valid signal from a client
                            {
                                byte[] sendData = myaddy.getBytes(); // make a packet that is the correct response of %%%
                                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length,packet.getAddress(),packet.getPort());
                                socket.send(sendPacket); //send response to client
                            }
                        }
                        else
                            System.out.println("echo");
                    }
                }
                catch(IOException except) //rare occurance of problems output them to console
                {
                    System.out.println("Something went wrong: "+except);
                }
            }

        public boolean checkInetAddress (InetAddress addr)
        {
            try
            {
                Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
                while (interfaces.hasMoreElements()) //for all network interfaces
                {
                    NetworkInterface tmp = (NetworkInterface) interfaces.nextElement();
                    if (tmp.isLoopback() || !tmp.isUp())//if its a 127.0.0.1 (local address) or not connected
                        continue;                                                 //skip it
                    for (Enumeration addresses = tmp.getInetAddresses(); addresses.hasMoreElements (); )
                    {
                        if (addr.equals ((InetAddress) addresses.nextElement ()))
                            return false;
                    }
                }
            }catch(Exception e){}
            return true;
        }

    }

}


