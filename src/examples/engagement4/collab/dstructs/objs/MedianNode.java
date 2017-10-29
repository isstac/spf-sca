package engagement4.collab.dstructs.objs;

import engagement4.collab.*;
import java.util.*;
import engagement4.collab.dstructs.*;

public class MedianNode extends TempIndexNode implements TreeHandler
{
    public MedianNode() {
        final int[] childids = { Integer.MAX_VALUE };
        this.setUserObject(childids);
    }
    
    @Override
    public void takestep(final SchedulingSandbox sb, final List results, final int low, final int high, final int dataindex, final TreeNodeCallBack tncb) {
    	final int[] childids = (int[])this.getUserObject();
        final int data = childids[dataindex];
        if (data == Integer.MAX_VALUE) {
            return;
        }
        final int datapeek = childids[dataindex + 1];
        if (high >= data && datapeek >= low) {
            final TempIndexNode[] cnodes = this.children();
            final TempIndexNode cn = cnodes[dataindex];
            cn.takestep(sb, results, low, high, null, tncb);
        }
    }
    
    @Override
    public void takestep(final SchedulingSandbox sb, final List results, final int low, final int high, final TreeHandler walkerimpl, final TreeNodeCallBack tncb) {
    	super.takestep(sb, results, low, high, this, tncb);
    }
}
