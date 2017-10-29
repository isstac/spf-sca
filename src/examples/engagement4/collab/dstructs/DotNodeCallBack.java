package engagement4.collab.dstructs;

import java.util.*;
import engagement4.collab.dstructs.objs.*;

public class DotNodeCallBack implements TreeNodeCallBack
{
    public Map<String, String> mappings;
    public List<String> medians;
    public List<String> datan;
    public List<String> relations;
    public static int outnum;
    
    public DotNodeCallBack() {
        this.mappings = new HashMap<String, String>();
        this.medians = new ArrayList<String>();
        this.datan = new ArrayList<String>();
        this.relations = new ArrayList<String>();
        ++DotNodeCallBack.outnum;
    }
    
    @Override
    public void nodeCallBack(final TempIndexNode tnode) {
        final StringBuffer label = new StringBuffer();
        final int[] data = (int[])tnode.getUserObject();
        for (int i = 0; i < data.length; ++i) {
            label.append(":");
            label.append(data[i]);
        }
        this.mappings.put(Integer.toString(tnode.uniqueid), label.toString());
        if (tnode instanceof DataNode) {
            this.datan.add(Integer.toString(tnode.uniqueid));
        }
        else if (tnode instanceof MedianNode) {
            this.medians.add(Integer.toString(tnode.uniqueid));
        }
        if (tnode.getParent() != null) {
            final String rel = "" + tnode.getParent().uniqueid + "->" + tnode.uniqueid;
            this.relations.add(rel);
        }
    }
    
    static {
        DotNodeCallBack.outnum = 0;
    }
}
