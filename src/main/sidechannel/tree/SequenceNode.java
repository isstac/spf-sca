package sidechannel.tree;

/**
*
* @author Quoc-Sang Phan <sang.phan@sv.cmu.edu>
*
*/
public class SequenceNode extends Node{

	private boolean deadEnd = false;
	
	public SequenceNode(int depth, int id, Node parent){
		super(depth, id, parent);
	}
	
	public void setDeadEnd(){
		deadEnd = true;
	}
	
	@Override
	public boolean isDeadEnd() {
		return deadEnd;
	}

	@Override
	public boolean isWinningNode() {
		return false;
	}

	@Override
	public boolean isInput() {
		return false;
	}

	@Override
	public boolean isOutput() {
		return true;
	}

	@Override
	public boolean isSelected() {
		return false;
	}

}
