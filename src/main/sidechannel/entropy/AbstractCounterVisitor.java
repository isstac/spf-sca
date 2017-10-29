package sidechannel.entropy;

import java.math.BigInteger;
import java.util.Set;

/**
 *
 * @author Quoc-Sang Phan <sang.phan@sv.cmu.edu>
 *
 */
public interface AbstractCounterVisitor<Cost, SymbolicPath> {

	public abstract void visit(BigInteger result, Cost cost, Set<SymbolicPath> paths);
}
