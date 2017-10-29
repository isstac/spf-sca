package sidechannel.singlerun.noise;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Set;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.PropertyListenerAdapter;
import gov.nasa.jpf.search.Search;
import sidechannel.common.GlobalVariables;
import sidechannel.util.SymbolicDomain;

/**
*
* @author Quoc-Sang Phan <sang.phan@sv.cmu.edu>
*
*/
public class ProfileSettingsBuilder extends PropertyListenerAdapter {
	
	Config conf;
	
	public ProfileSettingsBuilder(Config config) {
		conf = config;
	}
	
	@Override
	public void searchFinished(Search search) {
		Set<String> vars = GlobalVariables.domains.keySet();
		int size = vars.size();
		if(size < 1){
			return;
		}
		StringBuilder sb = new StringBuilder();
		sb.append("domain{\n");
		for(String var : vars){
			SymbolicDomain domain = GlobalVariables.domains.get(var);
			sb.append("\t" + var + " : " + domain.min + "," + domain.max + ";\n");
		}
		sb.append("};\n\n");
		sb.append("usageProfile{\n\t");

		int count = 0;
		
		for (String var: vars){
			sb.append(var + "==" + var);
			count++;
			if (count < size)
				sb.append(" && ");
			
		}
		sb.append(" : 100/100;\n};");
		
		String tmpDir = conf.getProperty("symbolic.reliability.tmpDir","build/tmp");
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
}
