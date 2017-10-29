package sidechannel.entropy;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Set;

import org.antlr.runtime.ANTLRFileStream;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.TokenStream;

import gov.nasa.jpf.Config;
import modelcounting.domain.ProblemSetting;
import modelcounting.domain.VarDomain;
import modelcounting.grammar.ProblemSettingsLexer;
import modelcounting.grammar.ProblemSettingsParser;
import sidechannel.util.SymbolicVariableCollector;

/**
 * Calculate probabilities and Shannon Entropy using Latte
 *
 * @author Quoc-Sang Phan <sang.phan@sv.cmu.edu>
 *
 */
public class EntropyCalculator <Cost,SymbolicPath> implements AbstractCounterVisitor<Cost,SymbolicPath> {

	protected Config conf;
	protected SymbolicVariableCollector collector;
	protected int count = 1;
	protected double domain = 1;
	protected double domainH = 1;
	protected double leakage = 0;
	protected boolean first = true;
	protected boolean printProb = false;
	
	public EntropyCalculator(Config conf, SymbolicVariableCollector collector){
		this.conf = conf;
		this.collector = collector;
		initDomain();
	}
	
	public double getDomain(){
		return domain;
	}
	
	public double getDomainH(){
		return domainH;
	}
	
	private void initDomain(){
		String problemSettingsPath = conf
				.getProperty("symbolic.reliability.problemSettings");
		if (problemSettingsPath == null) {
			initDomainForVarList();
		} else{
			initDomainFromProblemSettings(problemSettingsPath);
		}
	}
	
	private void initDomainForVarList(){
		int MIN = Integer.parseInt(conf.getProperty("symbolic.min_int", String.valueOf(Integer.MIN_VALUE)));
		int MAX = Integer.parseInt(conf.getProperty("symbolic.max_int", String.valueOf(Integer.MAX_VALUE)));
		
		String strMinHigh = conf.getProperty("sidechannel.min_high");
		String strMaxHigh = conf.getProperty("sidechannel.max_high");
		
		int min_high = (strMinHigh == null) ? MIN : Integer.parseInt(strMinHigh);
		int max_high = (strMaxHigh == null) ? MAX : Integer.parseInt(strMaxHigh);
				
		// if each variable has domain D, then n variables has domain D^n
		// domain = Math.pow(MAX - MIN + 1, collector.getListOfVariables().size()); // domain of the input
		
		for(String var : collector.getListOfVariables()){
			if(var.charAt(0) == 'h'){
				int val = max_high - min_high + 1;
				domain *= val;
				domainH *= val;
			} else{
				domain *= MAX - MIN + 1;
			}
		}
		
		System.out.println(">>>>> The total domain is " + domain);
		System.out.println(">>>>> The domain of the secret is " + domainH);
	}
	
	private void initDomainFromProblemSettings(String problemSettingsPath){
		Set<String> collected = collector.getListOfVariables();
		try {
			CharStream psStream = new ANTLRFileStream(problemSettingsPath);
			ProblemSettingsLexer psLexer = new ProblemSettingsLexer(psStream);
			TokenStream psTokenStream = new CommonTokenStream(psLexer);
			ProblemSettingsParser psParser = new ProblemSettingsParser(psTokenStream);
			ProblemSetting output = psParser.problemSettings();
			ArrayList<VarDomain> vars = output.getDomain().toVarsAndDomains();
			boolean adjust = false;
			for(VarDomain vd : vars){
				String var = vd.var;
				if(!collected.contains(var)){
					adjust = true;
					continue;
				}
				long val = vd.upperBound - vd.lowerBound + 1;
				domain *= val;
				if(var.charAt(0) == 'h'){
					domainH *= val;
				}
			}
			if(adjust){
				adjustUserProfile(vars);
			}
		} catch (IOException | RecognitionException e) {
			e.printStackTrace();
		}
		
	}
	
	private void adjustUserProfile(ArrayList<VarDomain> vars) {
		Set<String> collected = collector.getListOfVariables();
		StringBuilder sb = new StringBuilder();
		sb.append("domain{\n");

		for (VarDomain vd : vars) {
			if(!collected.contains(vd.var)){
				continue;
			}
			sb.append("\t" + vd.var + " : " + vd.lowerBound + "," + vd.upperBound + ";\n");
		}

		sb.append("};\n\n");
		sb.append("usageProfile{\n\t");
		
		for (VarDomain vd : vars) {
			if(!collected.contains(vd.var)){
				continue;
			}
			sb.append(vd.var + "==" + vd.var);
			sb.append(" && ");
		}
		// if(sb.charAt(sb.length() - 2) == '&' ){
			sb.delete(sb.length() - 4, sb.length());
		// }
		sb.append(" : 100/100;\n};");
		
		String tmpDir = conf.getProperty("symbolic.reliability.tmpDir");
		String target = conf.getProperty("target");
		String upFile = tmpDir + "/" + target + ".up";
		Writer writer = null;

		try {
			writer = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(upFile), "utf-8"));
			writer.write(sb.toString());
			conf.setProperty("symbolic.reliability.problemSettings", upFile);
		} catch (IOException ex) {
			// report
		} finally {
			try {
				writer.close();
			} catch (Exception ex) {
			}
		}
	}
	
	@Override
	public void visit(BigInteger result, Cost cost, Set<SymbolicPath> paths) {
		double block = result.doubleValue();
		leakage += block * (Math.log(domain) - Math.log(block));
		// If print probabilities
		if(printProb){
			System.out.println("\n=====");
			for (SymbolicPath s : paths) {
			    System.out.println("PC is: \n" + s);
			}
			System.out.println("The cost of block " + count +" is " 
					+ cost);	
			System.out.println("The size of block " + count +" is " + block);
			System.out.println("The probability of block " + count +" is " + (block / domain));
			System.out.println("=====\n");
		}
		++count;
	}
	
	public double getLeakage(){
		if(first){
			leakage = leakage / (Math.log(2) * domain);
			first = false;
		}
		return leakage;
	}
	
	/* compute multiple-entropies
	public double computeEntropiesOfObservables(HashMap<Cost, HashSet<String>> obsrv, SymbolicVariableCollector collector) {
		double leakage = 0;

		int MIN = Integer.parseInt(conf.getProperty("symbolic.min_int", String.valueOf(Integer.MIN_VALUE)));
		int MAX = Integer.parseInt(conf.getProperty("symbolic.max_int", String.valueOf(Integer.MAX_VALUE)));
		
		// if each variable has domain D, then n variables has domain D^n
		double domain = Math.pow(MAX - MIN + 1, collector.getListOfVariables().size()); // domain of the input

		Iterator<Map.Entry<Cost, HashSet<String>>> it = obsrv.entrySet()
				.iterator();
		ModelCounter counter = new ModelCounter(conf,collector);
		
		int count = 0;
		
		ArrayList<Long> lst = new ArrayList<Long>();
		
		while (it.hasNext()) {
			Map.Entry<Cost, HashSet<String>> pair = (Map.Entry<Cost, HashSet<String>>) it
					.next();
			HashSet<String> paths = pair.getValue();
			
			long block = counter.count(paths);
			leakage += block * (Math.log(domain) - Math.log(block));
			lst.add(block);
			if(DEBUG){
				System.out.println("\n=====");
				for (String s : paths) {
				    System.out.println("PC is: " + s);
				}
				System.out.println("the block " + count +" is " + block);
				System.out.println("the block probability " + count +" is " + (block / domain));
				System.out.println("=====\n");
				count++;
			}
		}
		
		// Collections.sort(lst);
		double N = 0;
		for (int i = 0; i < lst.size(); i++){
			long block = lst.get(i);
			N += ((block + 1) * block) / (2 * domain);
		}
		
		if(DEBUG){
			System.out.println("the domain is " + domain);
			System.out.println("the expected number of guesses is " + N);
		}
		
		leakage = leakage / (Math.log(2) * domain);

		cleanDirectory();
		
		return leakage;
	}
	
	//*/

}
