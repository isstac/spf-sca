package engagement4.collab.driver;

import engagement4.collab.CollabServer;
import engagement4.collab.SchedulingSandbox;
import engagement4.collab.classes.ByteBuf;
import engagement4.collab.classes.ChannelHandlerContext;
import engagement4.collab.classes.DatagramPacket;
import engagement4.collab.dstructs.objs.AuditorData;
import gov.nasa.jpf.symbc.Debug;

/**
 * @author Quoc-Sang Phan
 *
 *         For search operation
 */
public class Driver {

	static final int SESSIONID = 1000;
	static final int SEARCH_MIN = 0;
	static final int SEARCH_MAX = 100;

	public static void main(String[] args) throws Exception {
		testSearchForTimingChannel();
	}
	
	public static void testSearchForTimingChannel() throws Exception{
		int[] init_eventids = { 64, 85 };
		SchedulingSandbox sandbox = new SchedulingSandbox(init_eventids);
		int h = Debug.makeSymbolicInteger("h");
		
		// To avoid DuplicateKeyException:
		Debug.assume(h != 64 && h != 85 && h != -1);
		
		sandbox.add(h, new AuditorData());
		CollabServer server = new CollabServer();
		server.addSchedulingSandbox(SESSIONID, sandbox);

		int capacity = 13;
		ByteBuf buf = new ByteBuf(capacity);
		buf.setCommand(11);
		buf.writeInt(SESSIONID);
		buf.writeInt(SEARCH_MIN);
		buf.writeInt(SEARCH_MAX);
		DatagramPacket packet = new DatagramPacket(buf);

		ChannelHandlerContext ctx = new ChannelHandlerContext();

		server.channelRead0(ctx, packet);
	}
}
