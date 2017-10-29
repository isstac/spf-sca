package sidechannel.util.smt;

import gov.nasa.jpf.Config;

import java.util.Collection;
import java.util.Set;

import modelcounting.domain.Constraint;
import sidechannel.util.SymbolicVariableCollector;

/**
 *
 * @author Quoc-Sang Phan <sang.phan@sv.cmu.edu>
 *
 */
public class LinearIntegerUtils<Cost> extends SmtLib2Utils<Cost>{

	public LinearIntegerUtils(Config conf, SymbolicVariableCollector collector){
		super(conf, collector);
	}
	
	@Override
	protected String formatOperator(String str){
		return str;
	}
	
	@Override
	protected StringBuilder buildDeclarations(){
		
		StringBuilder sbVar = new StringBuilder();
		// define high inputs
		for (String var : highVarsRenamed) {
			sbVar.append("(declare-fun " + var + " () Int)\n");
			if (strMinHigh != null){
				sbVar.append("(assert (>= " + var + " " + min_high  + ") )\n");
			}
			if (strMaxHigh != null){
				sbVar.append("(assert (<= " + var + " " + max_high  + ") )\n");
			}
			sbVar.append("\n");
		}
		// define low inputs
		for (String var : collector.getListOfVariables()) {
			if (var.charAt(0) == 'l') {
				sbVar.append("(declare-fun " + var + " () Int)\n");
				if (strMinLow != null){
					sbVar.append("(assert (>= " + var + " " + min_low + ") )\n");
				}
				if (strMaxLow != null){
					sbVar.append("(assert (<= " + var + " " + max_low + ") )\n");
				}
				sbVar.append("\n");
			}
		}
		return sbVar;
	}
	
	@Override
	protected String convertConstraint(Constraint c) {
		return c.toSmtlib2LinearInteger();
	}

	@Override
	public String buildParameters(Collection<String> vars) {
		//TODO assert(vars.size() > 0);
		StringBuilder sb = new StringBuilder(" ");
		for(String var : vars){
			sb.append("(" + var + " Int) ");
		}
		return sb.toString();
	}
	
	@Override
	protected String buildSorts(Set<String> vars) {
		//TODO assert(vars.size() > 0);
		StringBuilder sb = new StringBuilder(" ");
		for(int i = 0; i < vars.size(); ++i){
			sb.append(" Int");
		}
		return sb.toString();
	}

}
