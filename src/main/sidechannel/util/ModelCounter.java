package sidechannel.util;

import gov.nasa.jpf.Config;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.math.BigInteger;
import java.util.Iterator;
import java.util.Set;

import modelcounting.analysis.SequentialAnalyzer;
import modelcounting.analysis.SequentialAnalyzerBarvinok;
import modelcounting.domain.Problem;
import modelcounting.domain.ProblemSetting;
import modelcounting.utils.BigRational;
import modelcounting.utils.Configuration;
import sidechannel.common.GlobalVariables;

import org.antlr.runtime.RecognitionException;

import com.google.common.cache.LoadingCache;

/**
 * Counting the number of input for a specific path condition
 *
 * @author Quoc-Sang Phan <sang.phan@sv.cmu.edu>
 *
 */
public class ModelCounter {

	private ProblemSetting problemSettings;
	Configuration configuration; // Model Counter's configuration
	Config conf; // JPF's configuration	
	LoadingCache<Problem, Set<Problem>> omegaCache;
	SequentialAnalyzer analyzer = null;
	SequentialAnalyzerBarvinok analyzerBarvinok = null;
	
	public ModelCounter(Config conf, SymbolicVariableCollector collector) {
		this.conf = conf;
		
		String problemSettingsPath = conf
				.getProperty("symbolic.reliability.problemSettings");
		if (problemSettingsPath == null) {
			createUserProfile(collector);
		}
		
		init();
	}
	
	private void init(){
		configuration = new Configuration();
		configuration.setTemporaryDirectory(conf
				.getProperty("symbolic.reliability.tmpDir"));
		configuration.setOmegaExectutablePath(conf
				.getProperty("symbolic.reliability.omegaPath"));
		configuration.setLatteExecutablePath(conf
				.getProperty("symbolic.reliability.lattePath"));
		configuration.setIsccExecutablePath(conf
				.getProperty("symbolic.reliability.barvinokPath"));

		problemSettings = null;
		String problemSettingsPath = conf
				.getProperty("symbolic.reliability.problemSettings");
		try {
			problemSettings = ProblemSetting.loadFromFile(problemSettingsPath);
			// System.out.println("Problem settings loaded from: " + problemSettingsPath);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (RecognitionException e) {
			e.printStackTrace();
		}
	}
	
	
	
	public LoadingCache<Problem, Set<Problem>> getOmegaCache(){
		return omegaCache;
	}

	public BigInteger countResetCache(Set<String> set) {
		BigInteger result = new BigInteger("-1");

		try {
			SequentialAnalyzer analyzer = new SequentialAnalyzer(configuration,
					problemSettings.getDomain(),
					problemSettings.getUsageProfile(), 1);
			BigRational numberOfPoints = analyzer.countPointsOfSetOfPCs(set);
			result = new BigInteger(numberOfPoints.toString());
			omegaCache = analyzer.getOmegaCache();
			analyzer.terminate();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}
	
	public BigInteger count(Set<String> set) {
		BigInteger result = new BigInteger("-1");

		try {
			if(analyzer == null){ 
				analyzer = new SequentialAnalyzer(configuration,
					problemSettings.getDomain(),
					problemSettings.getUsageProfile(), 1);
			}
			result = analyzer.countPointsOfSetOfPCs(set).bigIntegerValue();
			analyzer.terminate();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}
	
	public BigInteger countBarvinok(Set<String> set) {
		BigInteger result = new BigInteger("-1");

		try {
			if(analyzerBarvinok == null){ 
				analyzerBarvinok = new SequentialAnalyzerBarvinok(configuration,
					problemSettings.getDomain(),
					problemSettings.getUsageProfile(), 1);
			}
			result = analyzerBarvinok.countPointsOfSetOfPCs(set).bigIntegerValue();
			analyzerBarvinok.terminate();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}
	
	public BigInteger countSinglePathResetCache(String pc) {
		BigInteger result = new BigInteger("-1");

		try {
			SequentialAnalyzer analyzer = new SequentialAnalyzer(configuration,
					problemSettings.getDomain(),
					problemSettings.getUsageProfile(), 1);
			result = analyzer.countPointsOfPC(pc).bigIntegerValue();
			omegaCache = analyzer.getOmegaCache();
			analyzer.terminate();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}
	
	public BigInteger countSinglePath(String pc) {
		BigInteger result = new BigInteger("-1");

		try {
			if(analyzer == null){ 
				analyzer = new SequentialAnalyzer(configuration,
					problemSettings.getDomain(),
					problemSettings.getUsageProfile(), 1);
			}
			result = analyzer.countPointsOfPC(pc).bigIntegerValue();
			analyzer.terminate();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}
	
	public BigInteger countSinglePathBarvinok(String pc) {
		BigInteger result = new BigInteger("-1");

		try {
			if(analyzerBarvinok == null){ 
				analyzerBarvinok = new SequentialAnalyzerBarvinok(configuration,
					problemSettings.getDomain(),
					problemSettings.getUsageProfile(), 1);
			}
			result = analyzerBarvinok.countPointsOfPC(pc).bigIntegerValue();
			analyzerBarvinok.terminate();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}
	
	public static String createUserProfileString(Config conf, SymbolicVariableCollector collector){
		StringBuilder sb = new StringBuilder();
		sb.append("domain{\n");

		int MIN = Integer.parseInt(conf.getProperty("symbolic.min_int", String.valueOf(Integer.MIN_VALUE)));
		int MAX = Integer.parseInt(conf.getProperty("symbolic.max_int", String.valueOf(Integer.MAX_VALUE)));
		
		String strMinHigh = conf.getProperty("sidechannel.min_high");
		String strMaxHigh = conf.getProperty("sidechannel.max_high");
		
		int min_high = (strMinHigh == null) ? MIN : Integer.parseInt(strMinHigh);
		int max_high = (strMaxHigh == null) ? MAX : Integer.parseInt(strMaxHigh);

		Iterator<String> iter = collector.getListOfVariables().iterator();
		while (iter.hasNext()) {
			String var = iter.next();
			// first check if the domain is in the global variable
			if(GlobalVariables.domains.contains(var)){
				SymbolicDomain domain = GlobalVariables.domains.get(var);
				sb.append("\t" + var + " : " + domain.min + "," + domain.max + ";\n");
			} else if(var.charAt(0) == 'h'){
				sb.append("\t" + var + " : " + min_high + "," + max_high + ";\n");
			} else{
				sb.append("\t" + var + " : " + MIN + "," + MAX + ";\n");
			}
		}

		sb.append("};\n\n");
		sb.append("usageProfile{\n\t");

		iter = collector.getListOfVariables().iterator();
		int count = 0;
		int size = collector.size();
		while (iter.hasNext()){
			String var = iter.next();
			sb.append(var + "==" + var);
			count++;
			if (count < size)
				sb.append(" && ");
			
		}
		sb.append(" : 100/100;\n};");
		return sb.toString();
	}

	/*
	 * create a userprofile for projection on sensitive data h
	 */
	public static String createProjectedUserProfileString(Config conf, SymbolicVariableCollector collector){
		StringBuilder sb = new StringBuilder();
		sb.append("domain{\n");

		int MIN = Integer.parseInt(conf.getProperty("symbolic.min_int", String.valueOf(Integer.MIN_VALUE)));
		int MAX = Integer.parseInt(conf.getProperty("symbolic.max_int", String.valueOf(Integer.MAX_VALUE)));
		
		String strMinHigh = conf.getProperty("sidechannel.min_high");
		String strMaxHigh = conf.getProperty("sidechannel.max_high");
		
		int min_high = (strMinHigh == null) ? MIN : Integer.parseInt(strMinHigh);
		int max_high = (strMaxHigh == null) ? MAX : Integer.parseInt(strMaxHigh);

		Iterator<String> iter = collector.getListOfVariables().iterator();
		int size = 0;
		while (iter.hasNext()) {
			String var = iter.next();
			if(var.charAt(0) == 'h'){
				++size;
				if(GlobalVariables.domains.contains(var)){
					SymbolicDomain domain = GlobalVariables.domains.get(var);
					sb.append("\t" + var + " : " + domain.min + "," + domain.max + ";\n");
				} else {
					sb.append("\t" + var + " : " + min_high + "," + max_high + ";\n");
				}
			}
		}

		sb.append("};\n\n");
		sb.append("usageProfile{\n\t");

		iter = collector.getListOfVariables().iterator();
		int count = 0;
		while (iter.hasNext()){
			String var = iter.next();
			if(var.charAt(0) == 'h'){
				sb.append(var + "==" + var);
				count++;
				if (count < size)
					sb.append(" && ");
			}
		}
		sb.append(" : 100/100;\n};");
		return sb.toString();
	}
	
	public void createUserProfile(SymbolicVariableCollector collector) {
		String content = createUserProfileString(conf, collector);
		String tmpDir = conf.getProperty("symbolic.reliability.tmpDir");
		String target = conf.getProperty("target");
		String upFile = tmpDir + "/" + target + ".up";
		Environment.write(upFile, content);
		conf.setProperty("symbolic.reliability.problemSettings", upFile);
	}
}
