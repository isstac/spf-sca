package sidechannel.util.smt;

import gov.nasa.jpf.Config;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import sidechannel.common.Common;
import sidechannel.util.SymbolicVariableCollector;

/**
 *
 * @author Quoc-Sang Phan <sang.phan@sv.cmu.edu>
 *
 */
public abstract class SmtLib2Utils<Cost> {

	protected Config conf;

	public static final int ENGINE_WMAX = 1;
	public static final int ENGINE_MAXRES = 2;
	public static final int ENGINE_BCD2 = 3;
	public static final int ENGINE_MAXHS = 4;
	public static final int ENGINE_DIFFERENCE_LOGIC = 5;
	
	public static final String OBSERVABLE_PREFIX = "OBS";

	protected SymbolicVariableCollector collector;
	
	// high inputs after renaming
	protected HashSet<String> highVarsRenamed;

	protected HashMap<String, Cost> pathConditionsToCost;

	protected String strMinHigh;
	protected String strMaxHigh;
	protected int min_high, max_high;
	protected String strMinLow;
	protected String strMaxLow;
	protected int min_low, max_low;

	public SmtLib2Utils(Config conf, SymbolicVariableCollector collector) {
		this.conf = conf;
		this.collector = collector;
		highVarsRenamed = new HashSet<String>();
		pathConditionsToCost = new HashMap<String, Cost>();
		initDomainsOfVariables();
	}

	private void initDomainsOfVariables() {
		strMinHigh = conf.getProperty("sidechannel.min_high");
		strMaxHigh = conf.getProperty("sidechannel.max_high");
		// TODO: change this to sidechannel.min_low and sidechannel.max_low ???
		strMinLow = conf.getProperty("symbolic.min_int");
		strMaxLow = conf.getProperty("symbolic.max_int");
		min_low = 0;
		max_low = 10000;
		if (strMinLow != null) {
			min_low = Integer.parseInt(strMinLow);
		}
		if (strMaxLow != null) {
			max_low = Integer.parseInt(strMaxLow);
		}

		min_high = 0;
		max_high = 10000;
		if (strMinHigh != null) {
			min_high = Integer.parseInt(strMinHigh);
		}
		if (strMaxHigh != null) {
			max_high = Integer.parseInt(strMaxHigh);
		}
	}

	public String configSmtEngine() {
		// default engine
		int engine = ENGINE_DIFFERENCE_LOGIC;
		// engine = ENGINE_MAXRES;
		// engine = ENGINE_WMAX;
		// TODO: read engine from config file
		engine = Integer.parseInt(conf.getProperty("MaxSMT","5"));

		String result = "";
		switch (engine) {
		case ENGINE_WMAX:
			result = "(set-option :opt.maxsat_engine wmax)\n\n";
			break;
		case ENGINE_MAXRES:
			result = "(set-option :opt.enable_sat false)\n\n";
			break;
		case ENGINE_BCD2:
			break;
		case ENGINE_MAXHS:
			break;
		case ENGINE_DIFFERENCE_LOGIC:
			result = "(set-option :smt.arith.solver 1) ; enables difference logic solver for sparse constraints\n(set-option :smt.arith.solver 3) ; enables difference logic solver for dense constraints\n\n";
			break;
		default:
			// TODO: wrong index, you should not reach this
			assert (false);
			break;
		}
		return result;
	}

	public HashMap<String, Cost> getPathConditionsToCost() {
		return pathConditionsToCost;
	}

	/*
	 * Format operator with the syntax of a specific theory
	 */
	abstract protected String formatOperator(String str);

	/*
	 * Declare all high and low variables
	 */
	abstract protected StringBuilder buildDeclarations();
	
	abstract public String buildParameters(Collection<String> vars);
	
	abstract protected String buildSorts(Set<String> vars);

	abstract protected String convertConstraint(
			modelcounting.domain.Constraint c);
	
	public void generateMaxSmt(HashMap<Cost, HashSet<String>> obsrv, boolean withParameters, boolean getLowInputs, boolean viewObservables) {
		StringBuilder sb = new StringBuilder();
		sb.append(configSmtEngine());
		if(withParameters){
			// TODO: check why quantifier work for tree, but parameter work for non-adaptive
			sb.append(groupPathConditionsWithParameters(obsrv));
			// sb.append(groupPathConditionsUsingQuantifiers(obsrv));
		}else {
			sb.append(groupPathConditionsWithTheSameCost(obsrv));
		}
		sb.append(makeSoftAssertionsForMaxSmt());
		sb.append("\n(check-sat)\n");
		if(getLowInputs){
			sb.append(getValuesOfLowInput());
		}
		if(viewObservables){
			sb.append(getValuesOfObservables());
		}
		String fileName = conf.getProperty("sidechannel.smt2","build/tmp/outputZ3bitvec.smt2");
		SmtLib2Utils.outputToFile(fileName, sb.toString());
	}
	
	public StringBuilder getValuesOfObservables(){
		StringBuilder sb = new StringBuilder();
		Iterator<Map.Entry<String, Cost>> it = pathConditionsToCost
				.entrySet().iterator();
		while(it.hasNext()){
			Map.Entry<String, Cost> pair = (Map.Entry<String, Cost>)it.next();
			String obs = pair.getKey();
			sb.append("(get-value ( " + obs + " ) )\n");
		}
		return sb;
	}
	
	public StringBuilder makeSoftAssertionsForMaxSmt(){
		StringBuilder sb = new StringBuilder();
		Iterator<Map.Entry<String, Cost>> it = pathConditionsToCost
				.entrySet().iterator();
		while(it.hasNext()){
			Map.Entry<String, Cost> pair = (Map.Entry<String, Cost>)it.next();
			String obs = pair.getKey();
			sb.append("(assert-soft " + obs + " :weight 1)\n");
		}
		return sb;
	}
	
	public StringBuilder getValuesOfLowInput(){
		StringBuilder sb = new StringBuilder();
		for (String var : collector.getListOfVariables()) {
			if (var.charAt(0) == 'l') {
				sb.append("(get-value ( " + var + " ) )\n");
			}
		}
		return sb;
	}
	
	public static void outputToFile(String fileName, String content){
		try {
			// String fileName = config.getProperty("sidechannel.smt2","build/tmp/outputZ3bitvec.smt2");
			PrintWriter writer = new PrintWriter(fileName);
			writer.println(content);
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void convertPCtoSMTLibv2(String pc, String fileName){
		pc = removePrefix(pc);
		StringBuilder pcConstraints = new StringBuilder();

		String PC = "PC";
		
		pcConstraints.append("(declare-fun " + PC +" () Bool)\n");
		pcConstraints.append("(assert (= " + PC + " ");
		String[] tmp = pc.split("\\s");

		for (String str : tmp) {
			if (str.length() <= 0) {
				continue;
			}

			str = formatOperator(str);

			// rename high variable
			if (str.charAt(0) == 'h') {
				// TODO: clean more efficiently with regular expression
				// or to use the method getVar ?
				int i = str.indexOf(")");
				String highVar = (i > 0) ? str.substring(0, i) : str;
				highVarsRenamed.add(highVar);
			}
			pcConstraints.append(str + " ");
			// add to the variable lists
		}
		pcConstraints.append("))\n\n");
		StringBuilder sb = buildDeclarations();
		sb.append(pcConstraints);
		sb.append("\n(check-sat)\n");
		sb.append("\n(get-model)");
		SmtLib2Utils.outputToFile(fileName, sb.toString());
	}

	public StringBuilder groupPathConditionsWithTheSameCost(
			HashMap<Cost, HashSet<String>> obsrv) {

		int pcIndex = 0;
		int highVarIndex = 1;
		StringBuilder pcConstraints = new StringBuilder();
		StringBuilder disjunctions = new StringBuilder();

		int observableIndex = 0;
		for (Map.Entry<Cost, HashSet<String>> entry : obsrv.entrySet()) {
			HashSet<String> setOfPC = entry.getValue();
			Cost cost = entry.getKey();
			ArrayList<String> assertion = new ArrayList<String>();
			for (String pc : setOfPC) {
				pc = removePrefix(pc);

				String PC;
				if(setOfPC.size() == 1){
					++observableIndex;
					PC = OBSERVABLE_PREFIX + observableIndex;
				} else{
					++pcIndex;
					PC = "PC" + pcIndex;
				}
				
				pcConstraints.append("(declare-fun " + PC +" () Bool)\n");
				pcConstraints.append("(assert (= " + PC + " ");
				String[] tmp = pc.split(" ");

				for (String str : tmp) {
					if (str.length() <= 0) {
						continue;
					}

					str = formatOperator(str);

					// rename high variable
					if (str.charAt(0) == 'h') {
						// TODO: clean more efficiently with regular expression
						// or to use the method getVar ?
						int i = str.indexOf(")");
						String highVar;
						if (i > 0) {
							highVar = str.substring(0, i) + highVarIndex;
							str = highVar + str.substring(i);
						} else {
							highVar = str + highVarIndex;
							str = highVar;
						}
						highVarsRenamed.add(highVar);
					}
					pcConstraints.append(str);
					pcConstraints.append(" ");
					// add to the variable lists
				}
				pcConstraints.append("))\n\n");
				assertion.add(PC);
				
			}
			++highVarIndex;
			int size = assertion.size();
			if(size == 1){
				pathConditionsToCost.put(assertion.get(0), cost);
			} else {
				assert(size > 1);
				++observableIndex;
				String obs = OBSERVABLE_PREFIX + observableIndex;
				pathConditionsToCost.put(obs, cost);
				disjunctions.append("(declare-fun " + obs + " () Bool)\n");
				disjunctions.append("(assert (= " + obs + " (or ");
				for (String PC : assertion) {
					disjunctions.append(PC + " ");
				}
				disjunctions.append(")))\n\n");
			}			
		}

		StringBuilder sb = buildDeclarations();
		sb.append(pcConstraints);
		sb.append(disjunctions);
		return sb;
	}

	private String removePrefix(String pc){
		// remove the prefix, e.g. "constraint # = 12"
		int pos = pc.indexOf("(");
		if (pos == -1) {
			System.out.println(pc);
		}
		return pc.substring(pos);
	}
	
	private String generateFuncAndParameters(String PC, Collection<String> pcVars){
		StringBuilder sbPC = new StringBuilder("( " + PC + " ");
		for(String var : pcVars){
			sbPC.append(var + " ");
		}
		sbPC.append(")");
		return sbPC.toString();
	}
	
	/*
	 * This method is an alternative to groupPathConditionsWithTheSameCost,
	 * it defines observables using quantifiers to provide more flexibility
	 * Of course, expressiveness comes with a cost of efficiency, 
	 * so avoid using it if there is no need to instantiate the PCs.
	 */
	public StringBuilder groupPathConditionsUsingQuantifiers(
			HashMap<Cost, HashSet<String>> obsrv) {

		int pcIndex = 0;
		int highVarIndex = 1;
		StringBuilder pcConstraints = new StringBuilder();
		StringBuilder disjunctions = new StringBuilder();

		int observableIndex = 0;
		for (Map.Entry<Cost, HashSet<String>> entry : obsrv.entrySet()) {
			HashSet<String> setOfPC = entry.getValue();
			Cost cost = entry.getKey();
			ArrayList<String> assertion = new ArrayList<String>();
			Set<String> lowInputOfObs = new HashSet<String>();
			for (String pc : setOfPC) {
				pc = removePrefix(pc);
				String PC = "PC" + ++pcIndex;
				String[] tmp = pc.split(" ");

				int index = pcConstraints.length();
				Set<String> pcVars = new HashSet<String>();
				
				for (String str : tmp) {
					if (str.length() <= 0) {
						continue;
					}

					str = formatOperator(str);

					// rename high variable
					if (str.charAt(0) == 'h') {
						// TODO: clean more efficiently with regular expression
						// or to use the method getVar ?
						int i = str.indexOf(")");
						String highVar;
						if (i > 0) {
							highVar = str.substring(0, i) + highVarIndex;
							str = highVar + str.substring(i);
						} else {
							highVar = str + highVarIndex;
							str = highVar;
						}
						highVarsRenamed.add(highVar);
					}
					
					if (str.charAt(0) == 'l') {
						// TODO: clean more efficiently with regular expression
						// or to use the method getVar ?
						int i = str.indexOf(")");
						String lowVar;
						if (i > 0) {
							lowVar = str.substring(0, i);
						} else {
							lowVar = str;
						}
						pcVars.add(lowVar);
						lowInputOfObs.add(lowVar);
					}
					
					pcConstraints.append(str);
					pcConstraints.append(" ");
					// add to the variable lists
				}
				pcConstraints.insert(index, "(define-fun " + PC + " ("+ buildParameters(pcVars) + ") Bool\n\t");
				pcConstraints.append("\n)\n\n");
				assertion.add(generateFuncAndParameters(PC, pcVars));
				
			}
			++highVarIndex;
			int size = assertion.size();
			++observableIndex;
			String obs = OBSERVABLE_PREFIX + observableIndex;
			String funcAndParas = generateFuncAndParameters(obs, lowInputOfObs);
			pathConditionsToCost.put(funcAndParas, cost);
			disjunctions.append("(declare-fun " + obs + " ("+ buildSorts(lowInputOfObs) + " ) Bool)\n");
			disjunctions.append("(assert (forall ("+ buildParameters(lowInputOfObs) + ")\n\t");
			disjunctions.append("(= \t"+ funcAndParas + " \n\t");
			if(size == 1){
				disjunctions.append("\t" + assertion.get(0) + "\n");
			} else {
				disjunctions.append("(or \n");
				for (String PC : assertion) {
					disjunctions.append("\t\t" + PC + "\n");
				}
				disjunctions.append("\t)\n");
			}			
			disjunctions.append("\t)\n))\n\n");
		}

		StringBuilder sb = new StringBuilder("(set-option :smt.macro-finder true)\n\n");
		sb.append(buildDeclarations());
		sb.append(pcConstraints);
		sb.append(disjunctions);
		return sb;
	}
	
	/*
	 * This method is an alternative to groupPathConditionsWithTheSameCost,
	 * it defines observables using the macro define-fun to provide more flexibility
	 * It may be cheaper than using quantifier, but I'm not very sure.
	 * Of course, expressiveness comes with a cost of efficiency, 
	 * so avoid using it if there is no need to instantiate the PCs.
	 */
	public StringBuilder groupPathConditionsWithParameters(
			HashMap<Cost, HashSet<String>> obsrv) {

		int pcIndex = 0;
		int highVarIndex = 1;
		StringBuilder pcConstraints = new StringBuilder();
		StringBuilder disjunctions = new StringBuilder();

		int observableIndex = 0;
		for (Map.Entry<Cost, HashSet<String>> entry : obsrv.entrySet()) {
			HashSet<String> setOfPC = entry.getValue();
			Cost cost = entry.getKey();
			ArrayList<String> assertion = new ArrayList<String>();
			Set<String> lookup = new HashSet<String>();
			ArrayList<String> pcVars = new ArrayList<String>();
			ArrayList<String> pcVarsRenamed = new ArrayList<String>();
			for (String pc : setOfPC) {
				pc = removePrefix(pc);
				String PC = "PC" + ++pcIndex;
				String[] tmp = pc.split(" ");

				int index = pcConstraints.length();
				
				for (String str : tmp) {
					if (str.length() <= 0) {
						continue;
					}

					str = formatOperator(str);

					// rename high variable
					if (str.charAt(0) == 'h') {
						// TODO: clean more efficiently with regular expression
						// or to use the method getVar ?
						int i = str.indexOf(")");
						String highVar = (i > 0) ? str.substring(0, i) : str;
						if(lookup.add(highVar)){
							pcVars.add(highVar);
							// renamed
							highVar += Common.SEPARATOR + highVarIndex;
							highVarsRenamed.add(highVar);
							pcVarsRenamed.add(highVar);
						}
					}
					
					if (str.charAt(0) == 'l') {
						// TODO: clean more efficiently with regular expression
						// or to use the method getVar ?
						int i = str.indexOf(")");
						String lowVar = (i > 0)? str.substring(0, i) : str ;
						if(lookup.add(lowVar)){
							pcVars.add(lowVar);
							pcVarsRenamed.add(lowVar);
						}
					}
					
					pcConstraints.append(str);
					pcConstraints.append(" ");
					// add to the variable lists
				}
				pcConstraints.insert(index, "(define-fun " + PC + " ("+ buildParameters(pcVars) + ") Bool\n\t");
				pcConstraints.append("\n)\n\n");
				assertion.add(generateFuncAndParameters(PC, pcVars));
				
			}
			++highVarIndex;
			int size = assertion.size();
			++observableIndex;
			String obs = OBSERVABLE_PREFIX + observableIndex;
			String funcAndParas = generateFuncAndParameters(obs, pcVarsRenamed);
			pathConditionsToCost.put(funcAndParas, cost);
			disjunctions.append("(define-fun " + obs + " ("+ buildParameters(pcVars) + " ) Bool\n");
			if(size == 1){
				disjunctions.append("\t" + assertion.get(0) + "\n");
			} else {
				disjunctions.append("\t(or \n");
				for (String PC : assertion) {
					disjunctions.append("\t\t" + PC + "\n");
				}
				disjunctions.append("\t)\n");
			}			
			disjunctions.append(")\n\n");
		}

		StringBuilder sb = new StringBuilder("(set-option :smt.macro-finder true)\n\n");
		sb.append(buildDeclarations());
		sb.append(pcConstraints);
		sb.append(disjunctions);
		return sb;
	}
}
