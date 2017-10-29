package sidechannel.util;

import java.util.HashSet;

import gov.nasa.jpf.symbc.numeric.ConstraintExpressionVisitor;
import gov.nasa.jpf.symbc.string.StringConstraint;
import gov.nasa.jpf.symbc.string.StringPathCondition;
import gov.nasa.jpf.symbc.string.StringSymbolic;

/**
 * A visitor to collect all symbolic string variables
 *
 * @author Quoc-Sang Phan <sang.phan@sv.cmu.edu>
 *
 */
public class StringVariableCollector extends ConstraintExpressionVisitor{

public HashSet<String> setOfSymVar;
	
	public StringVariableCollector(HashSet<String> set){
		setOfSymVar = set;
	}
	
	@Override
	public void preVisit(StringSymbolic expr) {
		String name = cleanSymbol(expr.toString());
		setOfSymVar.add(name);
	}

	public void collectVariables(StringPathCondition spc){
		StringConstraint c = spc.header;
		while(c != null){
			c.accept(this);
			c = c.and();
		}
	}
	
	private static String cleanSymbol(String str) {
		return str.replaceAll("\\[(.*?)\\]", ""); // remove e.g. [-1000000]
	}
	
	public HashSet<String> getListOfVariables(){
		return setOfSymVar;
	}
	
	public int size(){
		return setOfSymVar.size();
	}
}
