package engagement4.collab;

import engagement4.collab.utils.*;
import engagement4.collab.dstructs.objs.*;
import engagement4.collab.dstructs.*;
import java.util.*;
import java.io.*;

public class SchedulingSandbox
{
    public static final int NOVAL = -1;
    public static final int MAXPLUSONE = Integer.MAX_VALUE;
    public TempIndexNode root;
    public static final int ENDVAL = -2;
    private boolean issboxinsertionmodeenabled;
    public LogBuffer log;
    public HashMap<Integer, DataHolder> thedata;
    public boolean checkStatus;
    
    public static SchedulingSandbox populateSandbox(final EventResultSet eres) throws DuplicateKeyException, InvalidValueException {
        final List<Integer> resasints = eres.get();
        final int[] data = new int[resasints.size()];
        for (int i = 0; i < resasints.size(); ++i) {
            data[i] = resasints.get(i);
        }
        return new SchedulingSandbox(data);
    }
    
    private SchedulingSandbox() {
        this.checkStatus = false;
        this.log = new LogBuffer("logs/" + System.currentTimeMillis() + ".log");
        this.root = new DataNode();
        this.initnode((DataNode)this.root);
        this.thedata = new HashMap<Integer, DataHolder>();
    }
    
    public SchedulingSandbox(final int[] init_eventids) throws DuplicateKeyException {
        this();
        this.add(init_eventids);
    }
    
    public void initnode(final DataNode dn) {
        final int[] eventids = new int[9];
        Arrays.fill(eventids, -1);
        eventids[8] = -2;
        dn.setUserObject(eventids);
    }
    
    public void initnode(final MedianNode dn) {
        final int[] eventids = new int[0];
        Arrays.fill(eventids, Integer.MAX_VALUE);
        dn.setUserObject(eventids);
    }
    
    public void initSandbox() {
        this.issboxinsertionmodeenabled = true;
    }
    
    public void commit() {
        this.issboxinsertionmodeenabled = false;
    }
    
    public void add(final int[] eventids) throws DuplicateKeyException {
        for (int i = 0; i < eventids.length; ++i) {
            this.add(eventids[i], new NormalUserData());
        }
    }
    
    public boolean add(final int eventid, final DataHolder normalUserData) throws DuplicateKeyException {
        this.addhelper(eventid);
        this.thedata.put(eventid, normalUserData);
        return true;
    }
    
    private void addhelper(final int eventid) throws DuplicateKeyException {
        final DataNode dNode = this.getDNode(this.root, eventid);
        if (dNode != null) {
            this.addhelper(dNode, eventid);
        }
        else {
            System.out.println("dnode NULL: should not happen");
        }
    }
    
    private void addhelper(final DataNode node, final int eventid) throws DuplicateKeyException {
        final int[] eventidlist = (int[])node.getUserObject();
        for (int ind = 0; ind < eventidlist.length; ++ind) {
            if (eventidlist[ind] == eventid) {
                throw new DuplicateKeyException("key:" + eventidlist[ind] + " in index:" + ind);
            }
            if (eventidlist[ind] == -1) {
                eventidlist[ind] = eventid;
                return;
            }
            if (eventidlist[ind] == -2) {
                this.log.publish("node full");
                eventidlist[ind] = eventid;
                for (int i = 0; i <= ind; ++i) {
                    for (int j = 1; j <= ind; ++j) {
                        if (eventidlist[j - 1] > eventidlist[j]) {
                            final int temp = eventidlist[j - 1];
                            eventidlist[j - 1] = eventidlist[j];
                            eventidlist[j] = temp;
                        }
                    }
                }
                final int lval = eventidlist[ind];
                eventidlist[ind] = -2;
                this.split(node);
                this.addhelper(lval);
                this.checkStatus = true;
            }
            else {
                this.checkStatus = false;
            }
        }
    }
    
    public void split(final DataNode orignode) {
        this.log.publish("addnode");
        final DataNode dn1 = new DataNode();
        this.initnode(dn1);
        this.log.publish("addnode");
        final DataNode dn2 = new DataNode();
        this.initnode(dn2);
        final int[] eventidlist = (int[])orignode.getUserObject();
        for (int i = 0; eventidlist[i] != -2; ++i) {
            float div = 9.0f;
            try {
                div = 9.0f / i;
            }
            catch (ArithmeticException ex) {}
            if (div > 2.0f) {
                ((int[])dn1.getUserObject())[i] = eventidlist[i];
            }
            else {
                ((int[])dn2.getUserObject())[i - 4 - 1] = eventidlist[i];
            }
        }
        TempIndexNode parentnodeoforig = orignode.getParent();
        if (parentnodeoforig == null) {
            this.log.publish("addlevel");
            parentnodeoforig = new MedianNode();
            this.initnode((MedianNode)parentnodeoforig);
            this.root = parentnodeoforig;
        }
        if (parentnodeoforig instanceof MedianNode) {
            this.replace(parentnodeoforig, orignode, dn1, dn2);
        }
    }
    
    public EventResultSet getRange(final int min, final int max) {
        final List<Integer> eventids = new ArrayList<Integer>();
        this.root.takestep(this, eventids, 1, 2147483646, null, new PrintNodeCallBack());
        final EventResultSet eres = new EventResultSet(eventids);
        return eres;
    }
    
    public void split(final MedianNode tn) {
    }
    
    private void replace(final TempIndexNode pnode, final DataNode orignode, final DataNode dn1, final DataNode dn2) {
        final TempIndexNode[] children = pnode.children();
        int loc = -1;
        if (orignode != null) {
            for (int i = 0; i < children.length; ++i) {
                if (children[i].equals(orignode)) {
                    loc = i;
                }
            }
        }
        if (!this.issboxinsertionmodeenabled) {
            if (loc > -1) {
                pnode.remove(loc);
            }
            pnode.add(dn1);
            pnode.add(dn2);
            this.makeindex(pnode);
            sort(pnode, (int[])pnode.getUserObject(), pnode.children());
        }
        else {
            final MedianNode newmediannode = new MedianNode();
            this.initnode(newmediannode);
            pnode.children()[loc] = newmediannode;
            newmediannode.parent = pnode;
            newmediannode.add(dn1);
            newmediannode.add(dn2);
            this.makeindex(newmediannode);
            sort(newmediannode, (int[])newmediannode.getUserObject(), newmediannode.children());
        }
    }
    
    private static void sort(final TempIndexNode tn, final int[] vals, final TempIndexNode[] children) {
        final TempIndexNode[] newchildren = Arrays.copyOf(children, children.length);
        for (int i = vals.length - 2; i > 0; --i) {
            int first = 0;
            for (int j = 1; j <= i; ++j) {
                if (vals[j] > vals[first]) {
                    first = j;
                }
            }
            final int temp = vals[first];
            vals[first] = vals[i];
            vals[i] = temp;
            int cindex = -1;
            for (int k = 0; k < children.length; ++k) {
                final int[] cvals = (int[])children[k].getUserObject();
                if (cvals[0] == vals[i]) {
                    cindex = k;
                }
            }
            newchildren[i] = children[cindex];
        }
        tn.setChilren(newchildren);
    }
    
    public void printTree() {
        final List<Integer> ids = new ArrayList<Integer>();
        this.root.takestep(this, ids, 1, 2147483646, null, new PrintNodeCallBack());
        System.out.println("done");
    }
    
    public void printDot() throws FileNotFoundException {
        final DotNodeCallBack dCBack = new DotNodeCallBack();
        final PrintWriter dotout = new PrintWriter("tree" + DotNodeCallBack.outnum + ".dot");
        final List<Integer> ids = new ArrayList<Integer>();
        this.root.takestep(this, ids, 1, 2147483646, null, dCBack);
        dotout.println("digraph Nodes {");
        final Iterator<String> itm = dCBack.medians.iterator();
        dotout.print("node [shape=box]; ");
        while (itm.hasNext()) {
            final String next = itm.next();
            final String get = dCBack.mappings.get(next);
            dotout.print(next + "[label=\"" + get + "\"];\n");
        }
        dotout.println("");
        final Iterator<String> itn = dCBack.datan.iterator();
        dotout.print("node [shape=box, color=red]; ");
        while (itn.hasNext()) {
            final String next2 = itn.next();
            final String get2 = dCBack.mappings.get(next2);
            dotout.print(next2 + "[label=\"" + get2 + "\"];\n");
        }
        dotout.println("");
        for (final String next3 : dCBack.relations) {
            dotout.println(next3 + "; ");
        }
        dotout.println("");
        dotout.println("}");
        dotout.flush();
        dotout.close();
    }
    
    public void walkTreeCBack(final TreeNodeCallBack cb) {
        final List<Integer> ids = new ArrayList<Integer>();
        this.root.takestep(this, ids, 1, 2147483646, null, cb);
        System.out.println("done");
    }
    
    public DataNode getDNode(final TempIndexNode n, final int val) {
        if (n instanceof DataNode) {
            return this.getDNode((DataNode)n, val);
        }
        if (n instanceof MedianNode) {
            return this.getDNode((MedianNode)n, val);
        }
        return null;
    }
    
    public DataNode getDNode(final DataNode n, final int val) {
        return n;
    }
    
    public DataNode getDNode(MedianNode n, final int val) {
        if (n == null) {
            if (this.root instanceof DataNode) {
                return (DataNode)this.root;
            }
            n = (MedianNode)this.root;
        }
        if (n instanceof MedianNode) {
            final int[] childids = (int[])n.getUserObject();
            for (int i = 0; i < childids.length - 1; ++i) {
                final int data = childids[i];
                final int datapeek = childids[i + 1];
                if (val >= data && val < datapeek) {
                    final TempIndexNode[] children = n.children();
                    TempIndexNode c = children[i];
                    if (c instanceof MedianNode) {
                        c = this.getDNode((MedianNode)c, val);
                    }
                    if (c instanceof DataNode) {
                        return (DataNode)c;
                    }
                }
            }
        }
        return null;
    }
    
    private void makeindex(final TempIndexNode pnode) {
        final TempIndexNode[] childrena = pnode.children();
        final int[] newindex = new int[childrena.length + 1];
        newindex[childrena.length] = Integer.MAX_VALUE;
        for (int i = 0; i < childrena.length; ++i) {
            newindex[i] = ((int[])childrena[i].getUserObject())[0];
        }
        pnode.setUserObject(newindex);
    }
    
    public void delete(final int key) {
        final DataNode dNode = this.getDNode(this.root, key);
        final int[] klist = (int[])dNode.getUserObject();
        int deleteloc = -1;
        for (int i = 0; i < klist.length; ++i) {
            if (klist[i] == key) {
                deleteloc = i;
            }
            for (int ind = 0; ind < klist.length; ++ind) {
                if (klist[ind] == -1 || klist[ind] == -2) {
                    final int t = klist[ind - 1];
                    klist[deleteloc] = t;
                    klist[ind - 1] = -1;
                    klist[ind] = key;
                    for (int ii = 0; ii < ind; ++ii) {
                        for (int j = 0; j < ind; ++j) {
                            if (klist[ii] < klist[j + 1]) {
                                final int temp = klist[j + 1];
                                klist[j + 1] = klist[ii];
                                klist[ii] = temp;
                            }
                        }
                    }
                    return;
                }
            }
        }
    }
}
