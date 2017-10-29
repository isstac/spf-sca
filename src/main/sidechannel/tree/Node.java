package sidechannel.tree;


/**
* A node in the attack tree
*
* @author Quoc-Sang Phan <sang.phan@sv.cmu.edu>
*
*/
public abstract class Node {
	
	public int depth;
	public int id;
	public String value;
	public Node parent;
	
	public Node(int depth, int id, Node parent){
		this.depth = depth;
		this.id = id;
		this.parent = parent;
	}
	
	public int getDepth(){
		return depth;
	}
	
	public abstract boolean isDeadEnd();
	public abstract boolean isWinningNode();
	public abstract boolean isInput();
	public abstract boolean isOutput();
	public abstract boolean isSelected();
}
