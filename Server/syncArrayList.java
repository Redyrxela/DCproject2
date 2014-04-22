/**********************************************************************************************
 * Distributed computing spring 2014 group 4 //Alex Ryder//Nick Champagne//Hue Moua//
 *                                           //Daniel Gedge//Corey Jones//
 *   Project 2 Peer2Peer client/server
 ***********************************************************************************************/
/**********************************************************************************************
 * This is an arraylist type for all of the details that the server keeps track of.
 * it is synchronized so it can be safely modified from multiple threads
 ***********************************************************************************************/
import java.util.AbstractList;
import java.util.ArrayList;

public class syncArrayList<e> extends ArrayList
{
    private ArrayList<e> hiddenList;

    public syncArrayList()
    {
        hiddenList = new ArrayList<e>();
    }
    @Override
    public synchronized boolean isEmpty()
    {
       return hiddenList.isEmpty();
    }
    public synchronized boolean contains(Object banana)
    {
        return hiddenList.contains(banana);
    }
    public synchronized boolean add(Object banana)
    {
        return hiddenList.add((e)banana);
    }
    public synchronized e remove(int i)
    {
        return hiddenList.remove(i);
    }
    public synchronized e get(int i)
    {
        return hiddenList.get(i);
    }
    public synchronized int size()
    {
        return hiddenList.size();
    }

}
