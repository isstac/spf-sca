package sidechannel.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Set;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.TokenStream;

import gov.nasa.jpf.Config;
import modelcounting.barvinok.BarvinokExecutor;
import modelcounting.domain.Constraint;
import modelcounting.domain.Domain;
import modelcounting.domain.Problem;
import modelcounting.domain.ProblemSetting;
import modelcounting.grammar.LinearConstraintsLexer;
import modelcounting.grammar.LinearConstraintsParser;
import modelcounting.utils.Configuration;

/**
*
* @author Quoc-Sang Phan <sang.phan@sv.cmu.edu>
*
*/
public class BarvinokUtils {
	/*
	 * Parse domain from file
	 */
	public static Domain getDomain(Config conf){
		ProblemSetting problemSettings = null;
		Domain d = null;
		String problemSettingsPath = conf
				.getProperty("symbolic.reliability.problemSettings");
		if (problemSettingsPath == null) {
			throw new RuntimeException(
					"Problem settings must be dummy or provided by file.");
		}
		try {
			problemSettings = ProblemSetting.loadFromFile(problemSettingsPath);
			d = problemSettings.getDomain();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (RecognitionException e) {
			e.printStackTrace();
		}
		return d;
	}
	
	/*
	 * Convert a Problem object to Barvinok format
	 */
	public static String toBarvinok(Problem p){
		StringBuilder sb = new StringBuilder("(");
		Set<Constraint> set = p.getConstraints().getAllConstraints();
		for(Constraint c : set){
			sb.append(c.toStringBarvinok() + " and ");
		}
		sb.delete(sb.length() - 5, sb.length());
		sb.append(")");
		return sb.toString();
	}
	
	/*
	 * Execute the Barvinok tool, and return an output
	 */
	public static String executeBarvinok(Config conf, String input){
		String line = "";
		try{
			Process process = new ProcessBuilder(conf.getProperty("symbolic.reliability.barvinokPath")).start();
			PrintWriter pOut = new PrintWriter(process.getOutputStream());
			BufferedReader pIn = new BufferedReader(new InputStreamReader(process.getInputStream()));
			
			pOut.println(input);
			pOut.flush();
			pOut.close();
			
			line = pIn.readLine();
			pIn.close();
		} catch(IOException e){
			System.out.println(">>>>> Error executing Barvinok\n");
			e.printStackTrace();
		}
		return line;
	}
	
	/*
	 * Convert a path condition in string format to Problem data structure
	 */
	public static Problem pathConditionToProblem(String path){
		Problem spfProblem = null;
		try {
			CharStream spfStream = new ANTLRStringStream(path);
			LinearConstraintsLexer spfLexer = new LinearConstraintsLexer(spfStream);
			TokenStream spfTokenStream = new CommonTokenStream(spfLexer);
			LinearConstraintsParser spfParser = new LinearConstraintsParser(spfTokenStream);
			spfProblem = spfParser.relation();
		} catch (RecognitionException e) {
			System.out.println("Cannot parse path condition:\n" + path);
			e.printStackTrace();
		}
		return spfProblem;
	}
	
	public static ArrayList<Pair> parseProjection(Config conf, ArrayList<String> high, String line){
		ArrayList<Pair> result = new ArrayList<Pair>();
		int pos = line.indexOf("{");
		line = line.substring(pos + 2, line.length() - 2);
		String[] intervals = line.split(";");
		
		Configuration configuration = new Configuration();
		configuration.setTemporaryDirectory(conf.getProperty("symbolic.reliability.tmpDir"));
		configuration.setIsccExecutablePath(conf.getProperty("symbolic.reliability.barvinokPath"));
		BarvinokExecutor executor = new BarvinokExecutor(configuration);

		for(String str: intervals){
			pos = str.indexOf(":");
			// TODO: review this
			// what happens when the cardinality doesn't depend on h at all
			if(pos < 0){
				// assert false;
				continue;
			}
			try{
				double size = Double.parseDouble(str.substring(0,pos).trim());
				double domain = executor.execute(createDomainQuery(high, str.substring(pos + 2))).doubleValue();
				result.add(new Pair(size,domain));
			} catch(Exception e){
				System.out.println("Error computing domain using Barvinok");
				e.printStackTrace();
			}
		}
		return result;
	}
	
	/*
	 * Example of domain:
	 * C := { [h1,h2] : h1 = 0 and 0 <= h2 <= 9 };
	 */
	private static String createDomainQuery(ArrayList<String> high, String line){
		String relation = "Domain";
		StringBuilder sb = new StringBuilder(relation + " := { [");
		for(String para : high){
			sb.append(para + ", ");
		}
		sb.delete(sb.length() - 2, sb.length());
		sb.append("] : ");
		sb.append(line);
		sb.append("};\ncard " + relation + ";");
		return sb.toString();
	}
	
	public static String createProjectionPrefix(String relation, ArrayList<String> parameters, ArrayList<String> variables){
		StringBuilder sb = new StringBuilder(relation + " := [");
		sb.append(varsToList(parameters));
		sb.append("] -> {[");
		sb.append(varsToList(variables));
		sb.append("] :");
		return sb.toString();
	}
	
	public static String createCountingPrefix(String relation, ArrayList<String> parameters){
		StringBuilder sb = new StringBuilder(relation + " := {[");
		sb.append(varsToList(parameters));
		sb.append("] : ");
		return sb.toString();
	}
	
	private static String varsToList(ArrayList<String> vars){
		StringBuilder sb = new StringBuilder();
		for(String para : vars){
			sb.append(para + ", ");
		}
		if(sb.length() > 0){
			sb.delete(sb.length() - 2, sb.length());
		}
		return sb.toString();
	}
	
	public static String createCountingQuery(String relation, String prefix, String constraint){
		return prefix + constraint + "};\ncard " + relation + ";";
	}
}
