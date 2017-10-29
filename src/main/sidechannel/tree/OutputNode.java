package sidechannel.tree;

/**
 *
 * @author Quoc-Sang Phan <sang.phan@sv.cmu.edu>
 *
 */
public class OutputNode extends Node {

	public long size;
	public boolean leaf;
	
	public OutputNode(int depth, int id, Node parent){
		super(depth, id, parent);
		leaf = false;
		// size = Integer.MAX_VALUE;
	}

	@Override
	public boolean isInput() {
		return false;
	}

	@Override
	public boolean isOutput() {
		return true;
	}
	
	public boolean isWinningNode(){
		return leaf;
	}
	
	public void setLeafNode(){
		leaf = true;
	}

	@Override
	public boolean isDeadEnd() {
		return false;
	}
	
	@Override
	public boolean isSelected() {
		return false;
	}
	
	/*
	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append(id + "[label=" + '"' + value + '"' + "];\n");
		sb.append(id + "[shape=box];");
		if(parent != null){
			sb.append(parent.id + " -> " + id + ";\n");
		} else {
			return sb.toString();
		}
		if(leaf){
			sb.append(id + "[style=filled, fillcolor=green];\n");
		}
		return sb.toString();
	}
	//*/
}
