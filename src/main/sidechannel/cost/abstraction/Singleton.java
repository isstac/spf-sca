package sidechannel.cost.abstraction;

/**
 *
 * @author Quoc-Sang Phan <sang.phan@sv.cmu.edu>
 *
 */
public class Singleton {
	
	public IntervalAbstraction<?> abstraction = null;
	
	// Private constructor prevents instantiation from other classes
	private Singleton() {
	}

	/**
	 * SingletonHolder is loaded on the first execution of
	 * Singleton.getInstance() or the first access to SingletonHolder.INSTANCE,
	 * not before.
	 */
	private static class SingletonHolder {
		private static final Singleton INSTANCE = new Singleton();
	}

	public static Singleton getInstance() {
		return SingletonHolder.INSTANCE;
	}
	
	public void setAbstraction(IntervalAbstraction<?> abstraction){
		this.abstraction = abstraction;
	}
	
	public Long normalize(long cost){
		if(abstraction == null){
			return cost;
		}
		return abstraction.normalize(cost);
	}
}