package sidechannel.tree;

/**
 *
 * @author Quoc-Sang Phan <sang.phan@sv.cmu.edu>
 *
 */
public class InputNode extends Node {

	private boolean deadEnd = false;
	public boolean selected = false;
	
	public InputNode(int depth, int id, Node parent){
		super(depth, id, parent);
	}
	
	public void setDeadEnd(){
		deadEnd = true;
	}
	
	public boolean isDeadEnd(){
		return deadEnd;
	}
	
	@Override
	public boolean isInput() {
		return true;
	}

	@Override
	public boolean isOutput() {
		return false;
	}

	@Override
	public boolean isWinningNode() {
		return false;
	}

	@Override
	public boolean isSelected() {
		return selected;
	}

	/*
	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append(id + "[label=" + '"' + value + '"' + "];\n");
		if(deadEnd){
			sb.append(id + "[style=filled, fillcolor=red];\n");
		}
		sb.append(parent.id + " -> " + id + ";\n");
		return sb.toString();
	}
	//*/
}
