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
public class BitVectorUtils<Cost> extends SmtLib2Utils<Cost>{

	public static int bitLength = 32;
	
	public BitVectorUtils(Config conf, SymbolicVariableCollector collector){
		super(conf, collector);
	}

	/*
	 * signedConvert: default is "signed", e.g. signed less or equal
	 * TODO: need to generalize?
	 */
	@Override
	protected String formatOperator(String str){
		
		switch(str){
		case ">":
			str = "bvsgt";
			break;
		case "<":
			str = "bvslt";
			break;
		case "<=":
			str = "bvsle";
			break;
		case ">=":
			str = "bvsge";
			break;
		case "&":
			str = "bvand";
			break;
		case "*":
			str = "bvmul";
			break;
		case "/":
			str = "bvsdiv";
			break;
		case "+":
			str = "bvadd";
			break;
		case "-":
			str = "bvsub";
			break;
		case "%":
			str = "bvsmod";
			break;
		case ">>>":
			// TODO: check this
			// (simplify (bvlshr #xf0 #x03)) ; unsigned (logical) shift right
			str = "bvlshr";
			break;
		case ">>":
			// TODO: check this
			// (simplify (bvashr #xf0 #x03)) ; signed (arithmetical) shift right
			str = "bvashr";
			break;
		case "<<":
			str = "bvshl";
			break;
		case "rem":
			str = "bvsrem";
			break;
		default:
			// check if it is a number
			try{
				String str1 = str.replace(")", "");
				int num = Integer.parseInt(str1);
				// String str2 = "(_ bv" + num + " " + bitLength + ")";
				// String str2 = "#x" + Integer.toHexString(num);
				// TODO: this only works for 32 bits
				String str2 = "#x" + String.format("%08X", num & 0xFFFFFFFF);
				str = str.replaceAll(str1, str2);
			}catch(NumberFormatException e){
				// e.printStackTrace();
				// System.out.println("Format error at: " + str);
			}
			break;
		}
		return str;
	}
	
	@Override
	protected StringBuilder buildDeclarations(){
		
		StringBuilder sbVar = new StringBuilder();
		// define high inputs
		for (String var : highVarsRenamed) {
			sbVar.append("(declare-fun " + var + " () (_ BitVec " + bitLength + "))\n");
			// if (strMinHigh != null){
				sbVar.append("(assert (bvsge " + var +" (_ bv" + min_high + " " + bitLength + ")) )\n");
			// }
			// if (strMaxHigh != null){
				sbVar.append("(assert (bvsle " + var +" (_ bv" + max_high + " " + bitLength + ")) )\n");
			// }
			sbVar.append("\n");
		}
		// define low inputs
		for (String var : collector.getListOfVariables()) {
			if (var.charAt(0) == 'l') {
				sbVar.append("(declare-fun " + var + " () (_ BitVec " + bitLength + "))\n");
				// if (strMinLow != null){
					sbVar.append("(assert (bvsge " + var +" (_ bv" + min_low + " " + bitLength + ")) )\n");
				// }
				// if (strMaxLow != null){
					sbVar.append("(assert (bvsle " + var +" (_ bv" + max_low + " " + bitLength + ")) )\n");
				// }
				sbVar.append("\n");
			}
		}
		return sbVar;
	}
	
	@Override
	protected String convertConstraint(Constraint c) {
		return c.toSmtlib2BitVector();
	}

	@Override
	public String buildParameters(Collection<String> vars) {
		//TODO assert(vars.size() > 0);
		StringBuilder sb = new StringBuilder(" ");
		for(String var : vars){
			sb.append("(" + var + " (_ BitVec " + bitLength + ")) ");
		}
		return sb.toString();
	}
	
	@Override
	protected String buildSorts(Set<String> vars) {
		//TODO assert(vars.size() > 0);
		StringBuilder sb = new StringBuilder(" ");
		for(int i = 0; i < vars.size(); ++i){
			sb.append(" (_ BitVec " + bitLength + ")");
		}
		return sb.toString();
	}
}
