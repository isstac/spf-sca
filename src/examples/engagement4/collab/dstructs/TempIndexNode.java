package engagement4.collab.dstructs;

import engagement4.collab.*;
import java.util.*;

public class TempIndexNode implements Walkable
{
    public TempIndexNode parent;
    private TempIndexNode[] children;
    public static int lastuniqueid;
    public int uniqueid;
    private Object m_userData;
    
    public void setChilren(final TempIndexNode[] newchildren) {
        this.children = newchildren;
    }
    
    public TempIndexNode() {
        this.children = new TempIndexNode[0];
        ++TempIndexNode.lastuniqueid;
        this.uniqueid = TempIndexNode.lastuniqueid;
    }
    
    public TempIndexNode(final Object userObject) {
        this();
        this.m_userData = userObject;
    }
    
    public TempIndexNode add(final TempIndexNode child, final int index) {
        for (int i = 0; i < this.children.length; ++i) {
            if (this.children[i].equals(child)) {
                System.out.println("child collision");
                return this.children[i];
            }
        }
        if (this.equals(child)) {
            System.out.println("this collision");
            return this;
        }
        if (index < 0 || index == this.children.length) {
            final TempIndexNode[] newChildren = new TempIndexNode[this.children.length + 1];
            System.arraycopy(this.children, 0, newChildren, 0, this.children.length);
            newChildren[this.children.length] = child;
            this.children = newChildren;
        }
        else {
            if (index > this.children.length) {
                throw new IllegalArgumentException("Cannot add child to index " + index + ".  There are only " + this.children.length + " children.");
            }
            final TempIndexNode[] newChildren = new TempIndexNode[this.children.length + 1];
            if (index > 0) {
                System.arraycopy(this.children, 0, newChildren, 0, index);
            }
            newChildren[index] = child;
            System.arraycopy(this.children, index, newChildren, index + 1, this.children.length - index);
            this.children = newChildren;
        }
        child.parent = this;
        return child;
    }
    
    public TempIndexNode add(final TempIndexNode child) {
        return this.add(child, -1);
    }
    
    public TempIndexNode remove(final int index) {
        if (index < 0 || index >= this.children.length) {
            throw new IllegalArgumentException("Cannot remove element with index " + index + " when there are " + this.children.length + " elements.");
        }
        final TempIndexNode node = this.children[index];
        node.parent = null;
        final TempIndexNode[] newChildren = new TempIndexNode[this.children.length - 1];
        if (index > 0) {
            System.arraycopy(this.children, 0, newChildren, 0, index);
        }
        if (index != this.children.length - 1) {
            System.arraycopy(this.children, index + 1, newChildren, index, this.children.length - index - 1);
        }
        this.children = newChildren;
        return node;
    }
    
    public void removeFromParent() {
        if (this.parent != null) {
            final int position = this.index();
            this.parent.remove(position);
            this.parent = null;
        }
    }
    
    public TempIndexNode getParent() {
        return this.parent;
    }
    
    public boolean isRoot() {
        return this.parent == null;
    }
    
    public TempIndexNode[] children() {
        return this.children;
    }
    
    public boolean hasChildren() {
        return this.children.length != 0;
    }
    
    public int index() {
        if (this.parent != null) {
            int i = 0;
            while (true) {
                final Object node = this.parent.children[i];
                if (this == node) {
                    break;
                }
                ++i;
            }
            return i;
        }
        return -1;
    }
    
    public int depth() {
        final int depth = this.recurseDepth(this.parent, 0);
        return depth;
    }
    
    private int recurseDepth(final TempIndexNode node, final int depth) {
        if (node == null) {
            return depth;
        }
        return this.recurseDepth(node.parent, depth + 1);
    }
    
    public void setUserObject(final Object userObject) {
        this.m_userData = userObject;
    }
    
    public Object getUserObject() {
        return this.m_userData;
    }
    
    public List search(final SchedulingSandbox sb, final int low, final int high) {
        final List res = new ArrayList();
        this.takestep(sb, res, low, high, null, null);
        return res;
    }
    
    @Override
    public void takestep(final SchedulingSandbox sb, final List results, final int low, final int high, final TreeHandler walkerimpl, final TreeNodeCallBack tncb) {
    	final int[] objs = (int[])this.getUserObject();
        for (int i = 0; i < objs.length; ++i) {
            walkerimpl.takestep(sb, results, low, high, i, tncb);
        }
        tncb.nodeCallBack(this);
    }
    
    public TempIndexNode copy(final TempIndexNode newnode) {
        try {
            newnode.children = new TempIndexNode[this.children.length];
            for (int i = 0; i < this.children.length; ++i) {
                newnode.children[i] = this.children[i];
            }
            newnode.m_userData = new int[((int[])this.m_userData).length];
            for (int i = 0; i < ((int[])this.m_userData).length; ++i) {
                ((int[])newnode.m_userData)[i] = ((int[])this.m_userData)[i];
            }
            newnode.parent = this.parent;
        }
        catch (ArrayIndexOutOfBoundsException ex) {}
        return newnode;
    }
    
    public void copyin(final TempIndexNode newnode) {
        this.children = newnode.children;
        this.m_userData = newnode.m_userData;
        this.parent = newnode.parent;
    }
    
    static {
        TempIndexNode.lastuniqueid = 0;
    }
}
