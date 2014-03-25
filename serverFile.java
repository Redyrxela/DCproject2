// CS6263 Project2 Spring 2014
// Alex Ryder
// This program is a simple peer to peer file sharing program with a server to lookup files
//
// This is a simple object that the server uses to keep track of files by name. it will store
// node objects in an array list to keep track of which clients host each file
import java.util.ArrayList;

/**
 * Created by redyrxela on 3/15/14.
 */
public class serverFile
{
    String fName;
    ArrayList<node> hosts;
}
