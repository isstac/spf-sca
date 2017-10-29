package sidechannel.multirun.adaptive;

import java.util.Set;

/**
 * Do something with a partition
 *
 * @author Quoc-Sang Phan <sang.phan@sv.cmu.edu>
 *
 */
public interface AbstractPartitionVisitor <Cost>{

	public void visit(Integer[] lowInput, Set<Cost> allTheCosts );
}
