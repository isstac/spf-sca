package sidechannel;

import java.util.LinkedList;

import gov.nasa.jpf.PropertyListenerAdapter;
import gov.nasa.jpf.search.Search;
import gov.nasa.jpf.vm.ChoiceGenerator;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.ThreadInfo;
import gov.nasa.jpf.vm.VM;

/**
*
* @author Quoc-Sang Phan <sang.phan@sv.cmu.edu>
*
* The listeners in JPF are notified in unordered way
* so this class wrap them up in an ordered way
*/
public class CompositeListener  extends PropertyListenerAdapter {

	LinkedList<PropertyListenerAdapter> listeners = new LinkedList<PropertyListenerAdapter>();
	
	public CompositeListener(PropertyListenerAdapter... args){
		for(PropertyListenerAdapter arg : args){
			listeners.add(arg);
		}
	}
	
	public void append(PropertyListenerAdapter listener){
		listeners.add(listener);
	}
	
	@Override
	public void instructionExecuted(VM vm, ThreadInfo currentThread,
			Instruction nextInstruction, Instruction executedInstruction) {

		// delegate
		for(PropertyListenerAdapter pla : listeners){
			pla.instructionExecuted(vm, currentThread, nextInstruction, executedInstruction);
		}
	}
	
	@Override
	public void choiceGeneratorAdvanced(VM vm, ChoiceGenerator<?> currentCG) {
		// delegate
		for(PropertyListenerAdapter pla : listeners){
			pla.choiceGeneratorAdvanced(vm, currentCG);
		}
	}

	@Override
	public void stateBacktracked(Search search) {
		// delegate
		for(PropertyListenerAdapter pla : listeners){
			pla.stateBacktracked(search);
		}
	}
	
	@Override
	public void searchFinished(Search search) {
		// delegate
		for(PropertyListenerAdapter pla : listeners){
			pla.searchFinished(search);
		}
	}
}
