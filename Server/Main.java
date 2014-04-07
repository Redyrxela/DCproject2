
// This class is the main server class that controls all server functions. it keeps track of
// which clients have which files.


import javax.swing.*;
import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Random;



public class Main extends Thread
{
    protected Socket clientSocket;
    public static syncArrayList<serverFile> clientfiles;  //list of client files and the host who have them
    public static int numClients;
    public static int serverID;
    public static syncArrayList<serverLoads> servers;


    public static void main(String[] args) throws IOException
    {
        servers = new syncArrayList<serverLoads>();
        Random rand = new Random();
        serverID = rand.nextInt(5000);
        numClients = 0;
        ServerSocket serverSocket = null;
        BCthread clientConnector = new BCthread(); //spawn the client broadcast authenticator thread
        serverFileQuery sFQ = new serverFileQuery(); //spawn the thread that responds to file queries
        sFQ.start();
        clientConnector.start();

        serverServerFileQuery sSFQ = new serverServerFileQuery();

        sSFQ.start();

        serverLoadBalancer SLB = new serverLoadBalancer();
        SLB.start();

        GuiRunner gr = new GuiRunner();
        gr.start();

        clientfiles = new syncArrayList<serverFile>(); //start as a new list
        try
        {
            serverSocket = new ServerSocket(6666);         //create a socet for clients to connect to
            System.out.println ("data socket created");
            try
            {
                while (true)                              //forever
                {
                    System.out.println ("Waiting for a connection....");
                    new Main(serverSocket.accept());      //if they connect to us spawn thread
                }
            }
            catch (IOException execp)
            {
                System.out.println("something went wrong accepting connection : "+execp);
            }
        }
        catch (IOException execp)
        {
            System.out.println("something went wrong creating port: " + execp);
        }
    }

    private Main(Socket clientSoc)
    {
        clientSocket = clientSoc;  //with new connection start thread
        start();
    }

    public void run()
    {

        try {
            BufferedReader Istream = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));


            String temp = Istream.readLine(); //get the first message
            OutputStream Ostream = clientSocket.getOutputStream();

            if (!temp.equals("SER")) { //server will never send its file list dont try and receive

                node tempNode = new node(); //temporary new representation of a client node
                tempNode.IPaddress = (clientSocket.getInetAddress().getHostAddress()); //get the ip address of this client

                if (temp.substring(0, 3).equals("###"))//if its the port number
                {
                    tempNode.port = Integer.parseInt(temp.substring(3, temp.length())); //get the port number
                } else
                    tempNode.port = 8888; //some how we errored and didnt get the port first... so use the default

                System.out.println("We have a client messaging from " + tempNode.IPaddress + ":" + tempNode.port);

                serverFile tempSF; //create a temporary new file representation

                clientSocket.setSoTimeout(100000);

                //to account for deleted files, remove all records of this client
                for (int i = 0; i < clientfiles.size(); ++i)                     //for all file names
                {
                    for (int j = 0; j < clientfiles.get(i).hosts.size(); ++j)  //for all clients that host it
                    {
                        if (clientfiles.get(i).hosts.get(j).IPaddress.equals(tempNode.IPaddress)) //if this client is a host
                        {
                            clientfiles.get(i).hosts.remove(j); //remove them as a host
                        }
                    }
                    if (clientfiles.get(i).hosts.isEmpty())//if no one hosts this file remove it
                    {
                        clientfiles.remove(i);
                    }
                }

                while (true)   //while there are still files
                {
                    tempSF = new serverFile();    //temp new server file
                    tempSF.hosts = new ArrayList<node>(); //with a new arraylist initialization
                    tempSF.fName = Istream.readLine();    //get the file name

                    //    System.out.println("The client sent : "+tempSF.fName);

                    if (tempSF.fName.equals("EOF"))         //if its the end of the list break the loop
                    {
                        break;
                    }

                    tempSF.hosts.add(tempNode);           //this new file obviously came from our temp client node

                    boolean existing = false;             //if its an existing file name or not

                    for (int i = 0; i < clientfiles.size(); ++i)   //for all file names
                    {
                        if (clientfiles.get(i).fName.equals(tempSF.fName)) //if we have the file name already
                        {
                            //    System.out.println("We already have this file on record");
                            existing = true;           //it already exists
                            if (!clientfiles.get(i).hosts.contains(tempNode)) //if we dont have this client listed (we shouldnt)
                            {
                                //        System.out.println("but not on this host");
                                clientfiles.get(i).hosts.add(tempNode);     //add this host to the list for that file
                                break;
                            } else          //else it already existed.... some how?
                            {
                                //      System.out.println("We already have this file and host on record");
                                break;
                            }
                        }
                    }
                    if (!existing)        //if we didnt find it add it.
                    {
                        //        System.out.println("We had no record, adding it now");
                        clientfiles.add(tempSF);
                    }
                }


                temp = "";

                for (int i = 0; i < servers.size(); ++i) //get the list from another server
                {
                    if ((serverID != servers.get(i).idNum) && (!servers.get(i).serverIP.equals(clientSocket.getInetAddress().getHostAddress()))) {//if its not me or the server requesting the file
                        Socket otherServers = new Socket(servers.get(i).serverIP, 6666);
                        BufferedReader inpstream = new BufferedReader(new InputStreamReader(otherServers.getInputStream()));
                        OutputStream oupstream = otherServers.getOutputStream();
                        temp = "SER\n";
                        System.out.println("We are asking the server at "+servers.get(i).serverIP+" for their file list");
                        oupstream.write(temp.getBytes()); //trigger the server to send us its list back
                        while (true) {
                            temp = inpstream.readLine(); //read each received line
                            if (temp.equals("EOF")) //if its the end from the server break the while loop
                                break;
                            temp+="\n";
                            System.out.print("Received : "+temp);
                            Ostream.write(temp.getBytes());//forward it on to the client!
                        }
                        Ostream.flush();
                        inpstream.close();
                        oupstream.close();
                        otherServers.close();
                    }
                }
            }
            for(int i = 0; i<clientfiles.size(); ++i)       //for all files,
            {
                temp = clientfiles.get(i).fName+"\n";
                System.out.print("Sending file name: " + temp);
                Ostream.write(temp.getBytes()); //tell the client the list we have on record
            }


            System.out.println("Finished sending out list to the client");
            temp = "EOF\n";                                               //send end of list escape sequence
            Ostream.write(temp.getBytes());

            Istream.close();                                            //close everything
            Ostream.close();

            System.out.println("sent an EOF");
            clientSocket.close();
        }
        catch (Exception e)
        {
            System.err.println("Problem with Server :"+e);

        }

    }

}
