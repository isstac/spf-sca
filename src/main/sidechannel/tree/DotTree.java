package sidechannel.tree;

import gov.nasa.jpf.Config;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Map;

import sidechannel.tree.Node;

/**
 *
 * @author Quoc-Sang Phan <sang.phan@sv.cmu.edu>
 *
 */
public class DotTree {
	
	private StringBuilder dotTree;
	private boolean firstTime = true;
	
	public DotTree(){
		dotTree = new StringBuilder("digraph G {\n");
	}
	
	public void addNode(Node node){
		dotTree.append(node.id + "[label=" + '"' + node.value + '"' + "];\n");
		if(node.isOutput()){
			dotTree.append(node.id + "[shape=box];");
		}
		if(node.id == 0){
			return;
		}
		if(node.isWinningNode()){
			dotTree.append(node.id + "[style=filled, fillcolor=green];");
		}
		if(node.isDeadEnd()){
			dotTree.append(node.id + "[style=filled, fillcolor=red];");
		}
		dotTree.append("\t" + node.parent.id + " -> " + node.id + ";\n");
	}
	
	public void colorSelectedNodes(Map<Integer,Integer> selectedNodes){
		selectedNodes.forEach((k,v)->{
			dotTree.append(v + "[style=filled, fillcolor=darkolivegreen4];");
		});
	}
	
	public void printTreeToFile(Config conf){
		if(firstTime){
			dotTree.append("}\n");
			firstTime = false;
		}
		String tmpDir = conf.getProperty("sidechannel.tmpDir","build/tmp");
		String fileName = tmpDir + "/tree.dot";
		Writer writer = null;
		try {
			writer = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(fileName), "utf-8"));
			writer.write(dotTree.toString());
		} catch (IOException ex) {
			// report
		} finally {
			try {
				writer.close();
			} catch (Exception ex) {
			}
		}
		
		String pdfFileName = tmpDir + "/tree.pdf";
		String command1 = "dot -Tpdf " + fileName + " -o " + pdfFileName;
		// String command2 = "pdfcrop " + pdfFileName + " " + pdfFileName;
		try {
			Runtime.getRuntime().exec(command1);
			// Runtime.getRuntime().exec(command2);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
