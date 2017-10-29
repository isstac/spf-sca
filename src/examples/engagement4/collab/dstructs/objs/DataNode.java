package engagement4.collab.dstructs.objs;

import java.util.*;
import engagement4.collab.dstructs.*;
import engagement4.collab.*;

public class DataNode extends TempIndexNode implements TreeHandler
{
    public static final int NodeMAX = 9;
    
    public DataNode() {
        final int[] childids = new int[9];
        this.setUserObject(childids);
    }
    
    @Override
    public void takestep(final SchedulingSandbox sb, final List results, final int low, final int high, final int dataindex, final TreeNodeCallBack tncb) {
    	final int[] objs = (int[])this.getUserObject();
        final int data = objs[dataindex];
        if (data <= high && data >= low) {
            final DataHolder dres = sb.thedata.get(data);
            if (dres != null && dres instanceof NormalUserData) {
                if (results.size() > 2500) {
                    System.out.println("That's enough! ERROR: Too many events, you must be a bot");
                    throw new CollabRuntimeException();
                }
                results.add(data);
            }
        }
    }
    
    @Override
    public void takestep(final SchedulingSandbox sb, final List results, final int low, final int high, final TreeHandler walkerimpl, final TreeNodeCallBack tncb) {
    	super.takestep(sb, results, low, high, this, tncb);
    }
}
