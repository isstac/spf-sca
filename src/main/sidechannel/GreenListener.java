package sidechannel;

import java.math.BigInteger;
import java.util.LinkedList;
import java.util.List;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPF;
import gov.nasa.jpf.PropertyListenerAdapter;
import gov.nasa.jpf.jvm.bytecode.JVMReturnInstruction;
import gov.nasa.jpf.search.Search;
import gov.nasa.jpf.symbc.SymbolicInstructionFactory;
import gov.nasa.jpf.symbc.numeric.PCChoiceGenerator;
import gov.nasa.jpf.symbc.numeric.PathCondition;
import gov.nasa.jpf.symbc.numeric.solvers.SolverTranslator;
import gov.nasa.jpf.vm.ChoiceGenerator;
import gov.nasa.jpf.vm.ClassInfo;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.MethodInfo;
import gov.nasa.jpf.vm.SystemState;
import gov.nasa.jpf.vm.ThreadInfo;
import gov.nasa.jpf.vm.VM;
import za.ac.sun.cs.green.Instance;
import za.ac.sun.cs.green.util.Reporter;


public class GreenListener extends PropertyListenerAdapter {

	private Config config;

	private List<PathCondition> leafPathConditions;
	
	

	public GreenListener(Config conf, JPF jpf) {
		//jpf.getReporter().getPublishers().clear(); // Do we want this?
		config = conf;
		leafPathConditions = new LinkedList<PathCondition>();
	}

	
	@Override
	public void instructionExecuted(VM vm, ThreadInfo currentThread,
			Instruction nextInstruction, Instruction executedInstruction) {

		PathCondition pc = null;
		SystemState ss = vm.getSystemState();
		if (!ss.isIgnored()) {

			if (executedInstruction instanceof JVMReturnInstruction) {
				MethodInfo mi = executedInstruction.getMethodInfo();
				ClassInfo ci = mi.getClassInfo();
				if (ci != null) {
					String methodName = mi.getName();
			
					if (methodName.equals("main")) {

						String currentPC = null;
						ChoiceGenerator<?> cg = vm.getLastChoiceGeneratorOfType(PCChoiceGenerator.class);
						if (cg != null) {
							pc = ((PCChoiceGenerator) cg).getCurrentPC();
							if (pc != null) {
								handleLeafPC(pc);
							}
						}
					}
				}
			}
		}
	}


	@Override
	public void searchConstraintHit(Search search) {
		PathCondition pc = null;
		SystemState ss = search.getVM().getSystemState();
		if (!ss.isIgnored()) {
			String currentPC = null;
			ChoiceGenerator<?> cg = search.getVM().getLastChoiceGeneratorOfType(PCChoiceGenerator.class);
			if (cg != null) {
				pc = ((PCChoiceGenerator) cg).getCurrentPC();
				if (pc != null) {
					handlePseudoLeafPC(pc);
				}
			}
		}
	}

	protected void handleLeafPC(PathCondition pc) {
		System.out.println("\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~ Leaf! ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
//		System.out.println("PC: " + pc.toString());
//		System.out.println("SPC: " + pc.spc.toString());
		leafPathConditions.add(pc);
	}
	
	protected void handlePseudoLeafPC(PathCondition pc) {
		System.out.println("\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~ Pseudoleaf! ~~~~~~~~~~~~~~~~");
//		System.out.println("PC: " + pc.toString());
//		System.out.println("SPC: " + pc.spc.toString());
		leafPathConditions.add(pc);
	}


	@Override
	public void searchFinished(Search search) {
		System.out.println("\n\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
		System.out.println("|---------------------- SEARCH FINISHED! ---------------------------|");
		System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n\n");

		// Iterate the leaf PCs that were collected and count their models
		for(PathCondition pc : leafPathConditions) {
			BigInteger count = countModels(pc);
			System.out.println("Model count for this PC: " + count.toString());
		}

		// Show Green's final report
		System.out.println("\n");
		SymbolicInstructionFactory.greenSolver.shutdown();
		SymbolicInstructionFactory.greenSolver.report(new Reporter() {
			@Override
			public void report(String context, String message) {
				System.out.println(context + "::" + message);
			}
		});
	}


	protected BigInteger countModels(PathCondition pc) {
		String modelCountMode = config.getProperty("model_count.mode");
		if(modelCountMode.startsWith("abc")) {
			return countModelsWithABC(pc);
		} else {
			throw new RuntimeException("Only abc supported for now! We'll add LattE later on.");
		}
	}
	
	
	protected BigInteger countModelsWithABC(PathCondition pc) {
					
		BigInteger count = BigInteger.ZERO;
		Instance instance;

		if(pc.header == null) {
			System.out.println("\n~~~~~ Counting models of a purely string leaf PC ~~~~~~~~~~~~~~~~~~~~");
			System.out.println(pc.spc.toString());
			instance = SolverTranslator.createStringInstance(pc.spc.header);
		} else if(pc.spc.header == null) {
			System.out.println("\n~~~~~ Counting models of a purely numeric leaf PC ~~~~~~~~~~~~~~~~~~~");
			System.out.println(pc.toString());
			instance = SolverTranslator.createInstance(pc.header);
		} else {
			System.out.println("\n~~~~~ Counting models of a mixed (string/numeric) leaf PC ~~~~~~~~~~~");
			System.out.println(pc.toString());
			System.out.println(pc.spc.toString());
			// Translate both constraints to Instances
			Instance instance_num = SolverTranslator.createInstance(pc.header);
			Instance instance_str = SolverTranslator.createStringInstance(pc.spc.header);
			// Merge them into a single Instance
			// Make sure to put the one with the recently added constraint on the left side!
			if(! pc.spc.isRecentlyAddedConstraintKnown()) {
				throw new RuntimeException("We would expect this to be known at this point! What happened?");
			}
			if(pc.spc.isRecentlyAddedConstraintNumeric()) {
				instance = Instance.merge(instance_num, instance_str);
			} else {
				instance = Instance.merge(instance_str, instance_num);
			}
		}

		
		count = (BigInteger) instance.request("count");
		
		if(count == null) {
			throw new RuntimeException("Something went wrong when trying to count using Green!");
		}
	
		return count;
	}
	
}
