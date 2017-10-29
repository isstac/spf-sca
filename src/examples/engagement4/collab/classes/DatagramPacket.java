package engagement4.collab.classes;

import java.net.InetSocketAddress;

/**
 * @author Quoc-Sang Phan
 * 
 * This is a model of the DatagramPacket from netty library
 */
public class DatagramPacket {
	
	ByteBuf buf = null;
	
	public DatagramPacket(ByteBuf buf){
		this.buf = buf;
	}
	
	public DatagramPacket(ByteBuf buf, InetSocketAddress sender){
		this.buf = buf;
	}

	public InetSocketAddress sender(){
		return null;
	}
	
	public ByteBuf content(){
		return buf;
	}
}
