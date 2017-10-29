package sidechannel.util;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import gov.nasa.jpf.Config;

public class MathematicaUtils {

	private Config conf;
	
	public MathematicaUtils(Config conf){
		this.conf = conf;
	}
	
	public void generateMathematicaScript(HashMap<Long, HashSet<String>> obsrv){
		Iterator<Map.Entry<Long, HashSet<String>>> it = obsrv
				.entrySet().iterator();
		int count = 1;
		int obs = 1;
		StringBuilder sb = new StringBuilder();
		while (it.hasNext()) {
			Map.Entry<Long, HashSet<String>> pair = (Map.Entry<Long, HashSet<String>>) it
					.next();
			// Long cost = pair.getKey();
			HashSet<String> paths = pair.getValue();
					
			if(paths.size() <= 1){
				continue;
			}
			int start = count;
			for (String pc : paths) {
				sb.append("PC" + count + " = " + pc.replace(" = ", " == ") + ";\n");
				++count;
			}
			sb.append("\nOBS" + obs +  " = Or[");
			for(int i = start; i < count - 1; i++){
				sb.append("PC" + i + ",");
			}
			sb.append("PC" + (count - 1) + "];\n");
			sb.append("Print[BooleanConvert[FullSimplify[" + "OBS" + obs +  "]]]\n\n");
			sb.append("Print[];");
			++obs;
		}
		sb.append("Exit[];");
		System.out.println(sb.toString());
		try {
			String fileName = conf.getProperty("greedy.math","build/tmp/mathematica.m");
			PrintWriter writer = new PrintWriter(fileName);
			writer.println(sb.toString());
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/*
	 * Simpilfy using Mathematica
	 */
	public void simplify(HashMap<Long, HashSet<String>> obsrv){
		Iterator<Map.Entry<Long, HashSet<String>>> it = obsrv
				.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<Long, HashSet<String>> pair = (Map.Entry<Long, HashSet<String>>) it
					.next();
			Long cost = pair.getKey();
			HashSet<String> paths = pair.getValue();
			
			if(paths.size() <= 1){
				continue;
			}
			
			HashSet<String> newPaths = simpilfyPaths(paths);
			obsrv.put(cost, newPaths);
		}
		
	}
	
	private HashSet<String> simpilfyPaths(HashSet<String> paths){
		/*
		int count = 1;
		int obs = 1;
		StringBuilder sb = new StringBuilder();
		int start = count;
		for (String pc : paths) {
			sb.append("PC" + count + " = " + pc.replace(" = ", " == ") + ";\n");
			++count;
		}
		sb.append("\nOBS" + obs +  " = Or[");
		for(int i = start; i < count - 1; i++){
			sb.append("PC" + i + ",");
		}
		sb.append("PC" + (count - 1) + "];\n");
		sb.append("Print[BooleanMinimize[" + "PC" + (count - 1) +  "]]\n\n");
		sb.append("Print[\n];");
		//*/
		return null;
	}
}
