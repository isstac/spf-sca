package engagement4.collab.dstructs;

import engagement4.collab.dstructs.objs.*;

public class PrintNodeCallBack implements TreeNodeCallBack
{
    @Override
    public void nodeCallBack(final TempIndexNode tnode) {
        final int[] data = (int[])tnode.getUserObject();
        for (int i = 0; i < data.length; ++i) {}
        if (tnode instanceof DataNode) {}
        if (tnode instanceof MedianNode) {}
    }
}
