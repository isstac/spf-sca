package sidechannel.util;

import java.io.PrintWriter;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPF;
import gov.nasa.jpf.PropertyListenerAdapter;
import gov.nasa.jpf.jvm.bytecode.JVMReturnInstruction;
import gov.nasa.jpf.symbc.numeric.PCChoiceGenerator;
import gov.nasa.jpf.symbc.numeric.PCParser;
import gov.nasa.jpf.symbc.numeric.PathCondition;
import gov.nasa.jpf.symbc.numeric.solvers.ProblemZ3BitVector;
import gov.nasa.jpf.vm.ChoiceGenerator;
import gov.nasa.jpf.vm.ClassInfo;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.MethodInfo;
import gov.nasa.jpf.vm.SystemState;
import gov.nasa.jpf.vm.ThreadInfo;
import gov.nasa.jpf.vm.VM;

public class PCTransformer extends PropertyListenerAdapter{
	
	private int index = 0;
	private Config conf = null;
	
	public PCTransformer (Config config, JPF jpf){
		conf = config;
		// jpf.getReporter().getPublishers().clear();
	}
	
	@Override
	public void instructionExecuted(VM vm, ThreadInfo currentThread,
			Instruction nextInstruction, Instruction executedInstruction) {

		PathCondition pc;

		SystemState ss = vm.getSystemState();
		if (!ss.isIgnored()) {
						
			// TODO: add temporary costs, or final cost if run = num_run
			if (executedInstruction instanceof JVMReturnInstruction) {
								
				MethodInfo mi = executedInstruction.getMethodInfo();
				ClassInfo ci = mi.getClassInfo();
				if (null != ci) {
					
					// String className = ci.getName();
					String methodName = mi.getName();
					// if (className.equals(conf.getProperty("target")) &&
					// methodName.equals("main")) {
					if (methodName.equals("main")) {

						ChoiceGenerator<?> cg = vm
								.getLastChoiceGeneratorOfType(PCChoiceGenerator.class);
						if (cg != null) {
							pc = ((PCChoiceGenerator) cg).getCurrentPC();
							if (pc != null) {
								/*
								HashSet<String> setOfSymVar = new HashSet<String>();
								SymbolicVariableCollector collector = new SymbolicVariableCollector(setOfSymVar);	
								collector.collectVariables(pc);
								SmtLib2Utils<Long> utils = new BitVectorUtils<Long>(conf, collector);
								String fileName = conf.getProperty("sidechannel.tmpDir","build/tmp") + "/PC" + ++index + ".smt2";
								utils.convertPCtoSMTLibv2(PathConditionUtils.toStringFormat(
										pc, ConcreteSideChannelListener.SMT_LIB2_MODE), fileName);
								//*/
								//pc.solve();

								System.out.println("------------------------------------------");
								System.out.println(pc);
								ProblemZ3BitVector pb = new ProblemZ3BitVector();
								pb = (ProblemZ3BitVector) PCParser.parse(pc,pb);
								String formula = pb.printSMTLibv2String();
								formula += "\n\n(check-sat)\n\n(get-model)";
								System.out.println(formula);
								String fileName = conf.getProperty("sidechannel.tmpDir","build/tmp") + "/PC" + ++index + ".smt2";
								try {
									// String fileName = config.getProperty("sidechannel.smt2","build/tmp/outputZ3bitvec.smt2");
									PrintWriter writer = new PrintWriter(fileName);
									writer.println(formula);
									writer.close();
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
						}
					}
				}
			}
		}
	}
}
