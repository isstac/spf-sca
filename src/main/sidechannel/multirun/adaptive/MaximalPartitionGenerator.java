package sidechannel.multirun.adaptive;

import gov.nasa.jpf.Config;

import java.util.HashMap;
import java.util.HashSet;

import sidechannel.util.SymbolicVariableCollector;

import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Expr;
import com.microsoft.z3.Model;
import com.microsoft.z3.Optimize;
import com.microsoft.z3.Status;

public class MaximalPartitionGenerator<Cost> extends AllPartitionsGenerator<Cost> {	
	
	public MaximalPartitionGenerator(Config conf, AbstractPartitionVisitor<Cost> visitor, boolean linear) {
		super(conf,visitor,linear);
	}
	
	@Override
	public void computePartitions(HashMap<Cost, HashSet<String>> obsrv, SymbolicVariableCollector collector){
		assert(oneShot);
		init(obsrv,collector);
		computeMaximalPartition();
//		ctx.close();
	}
	
	protected void computeMaximalPartition(){
		Optimize opt = ctx.mkOptimize();
		//TODO: there is a runtime error for the line above
		
		opt.Add(hardConstraints);
		for(BoolExpr c : C){
			opt.AssertSoft(c, 1, "group");
		}
		if(opt.Check() == Status.SATISFIABLE){
			Expr[] values = new Expr[inputSize];
			Model m = solver.getModel();
			Integer[] lowInput = getLowInput(m, values);
			HashSet<Cost> allTheCosts = new HashSet<Cost>();
			for(int i = 0; i < C.length; ++i){
				BoolExpr c = (BoolExpr) m.evaluate(C[i], false);
				if(c.isTrue()){
					Cost cost = indexOfCost.get(i);
					allTheCosts.add(cost);
				}
			}
			visitor.visit(lowInput, allTheCosts);
		}
	}
}
