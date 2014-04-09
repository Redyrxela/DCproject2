import java.util.AbstractList;
import java.util.ArrayList;

/**
 * Created by Alex on 4/6/2014.
 */
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
