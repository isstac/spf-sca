package engagement1.lawdb;

import java.util.*;

public class CheckRestrictedID
{
    ArrayList<Integer> ids;
    
    public CheckRestrictedID() {
        this.ids = new ArrayList<Integer>();
    }
    
    public void add(final int id) {
        this.ids.add((Integer)id);
    }
    
    public boolean isRestricted(final int id) {
        return this.ids.contains((Object)id);
    }
    
    public boolean remove(final Integer key) {
        return this.ids.remove((Object)key);
    }
}
