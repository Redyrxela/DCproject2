/**********************************************************************************************
 * Distributed computing spring 2014 group 4 //Alex Ryder//Nick Champagne//Hue Moua//
 *                                           //Daniel Gedge//Corey Jones//
 *   Project 2 Peer2Peer client/server
 ***********************************************************************************************/
/**********************************************************************************************
 * This is a simple object that the server uses to keep track of files by name. it will store
 * node objects in an array list to keep track of which clients host each file
 ***********************************************************************************************/

import java.util.ArrayList;


public class serverFile
{
    String fName;
    ArrayList<node> hosts;
}
