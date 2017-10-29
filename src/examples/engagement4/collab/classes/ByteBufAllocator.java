package engagement4.collab.classes;

/**
 * @author Quoc-Sang Phan
 *
 * Model of a class from the netty library
 */
public class ByteBufAllocator {

	 public ByteBuf directBuffer(int initialCapacity){
		 ByteBuf buf = new ByteBuf(initialCapacity);
		 return buf;
	 }
}
