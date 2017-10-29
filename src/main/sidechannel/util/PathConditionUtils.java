package sidechannel.util;

import gov.nasa.jpf.symbc.numeric.Comparator;
import gov.nasa.jpf.symbc.numeric.Constraint;
import gov.nasa.jpf.symbc.numeric.IntegerExpression;
import gov.nasa.jpf.symbc.numeric.PathCondition;
import sidechannel.ConcreteSideChannelListener;

/**
 * Some utilities to process path conditions
 *
 * @author Quoc-Sang Phan <sang.phan@sv.cmu.edu>
 *
 */
public class PathConditionUtils {
	
	public static String cleanExpr(String str) {

		String tmp1 = str.replaceAll("\\[(.*?)\\]", ""); // remove e.g.
															// [-1000000]
		String tmp2 = tmp1.replaceAll("CONST_", "");

		return tmp2;
	}
	
	public static void appendHead(PathCondition pc, Constraint c){
		IntegerExpression left = (IntegerExpression) c.getLeft();
		IntegerExpression right = (IntegerExpression) c.getRight();
		Comparator comp = c.getComparator();
		pc._addDet(comp,left,right);
	}
	
	public static String toStringFormat(PathCondition pc, int mode){
		switch (mode){
		case ConcreteSideChannelListener.LATTE_MODE:
			return cleanExpr(pc.header.toString());
		case ConcreteSideChannelListener.SMT_LIB2_MODE:
			return pc.prefix_notation();
		default:
			// wrong mode
			assert false;
		}
		// you should not reach this point
		assert false;
		return null;
	}
	
	public static String project(String pc){
		//TODO do the projection on h
		String[] tokens = pc.split("&&\n");
		StringBuilder sb = new StringBuilder();
		for(String token : tokens){
			if(token.contains("h")){
				sb.append(token + " &&\n");
			}
		}
		int len = sb.length();
		sb.delete(len - 4, len);
		// System.out.println("\n\nProjection is " + sb.toString() + "\n\n");
		return sb.toString();
	}
}
