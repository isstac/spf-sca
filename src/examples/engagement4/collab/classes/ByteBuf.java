package engagement4.collab.classes;

import java.util.ArrayList;

/**
 * @author Quoc-Sang Phan
 *
 * This is the model of the class ByteBuf in netty
 * It only makes sense within the collab application
 */
public class ByteBuf {

	private int capacity = 0;
	private byte t = 0;
	
	// number of bytes written to the buffer
	private int count = 0;
	
	ArrayList<Integer> data = new ArrayList<Integer>();
	
	public ByteBuf(int capacity){
		this.capacity = capacity;
	}
	
	public void writeInt(int arg0){
		data.add(arg0);
		count += 4;
		assert count <= capacity;
	}
	
	public void writeByte(int arg0){
		++count;
		assert count <= capacity;
	}
	
	public byte getByte(int arg0){
		assert arg0 == 0;
		return t;
	}
	
	public int size(){
		return count;
	}
	
	public void setCommand(int command){
		t = (byte) command;
	}
	
	public int getInt(int index){
		// on for the search function of CollabServer
		index = (index - 1) / 4;
		return data.get(index);
	}
	
	/*
	 * This method is here to make the CollabServer compile successful.
	 * The char is two bytes
	 */
	public char getChar(int index){
		return 0;
	}
}
