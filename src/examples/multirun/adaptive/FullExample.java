package multirun.adaptive;

/**
 *
 * @author Quoc-Sang Phan <sang.phan@sv.cmu.edu>
 *
 */
public class FullExample extends AdaptiveAttackExample{

	/*
	 * There are 3 arguments: attack/defend, constraints, input
	 */
	
	protected static int[] initSecretInput(String[] args){
		assert (args.length >= 2);
		int id = Integer.parseInt(args[1]);
		return initSecretInput(id);
	}
	
	protected static int[] initPublicInput(String[] args){
		assert (args.length >= 2);
		boolean defend = args[0].equals("defend");
		int id = 0;
		if(args[0].equals("defend")){
			assert (args.length >= 3);
			id = Integer.parseInt(args[2]);
		}
		return initPublicInput(id, defend);
	}
}
