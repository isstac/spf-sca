package multirun.adaptive;

/**
 *
 * @author Quoc-Sang Phan <sang.phan@sv.cmu.edu>
 *
 */
public class MiniMaxExample extends AdaptiveAttackExample{
	
	protected static int[] initSecretInput(String[] args){
		assert (args.length >= 2);
		int run = Integer.parseInt(args[0]);
		return initSecretInput(run - 1);
	}
	
	protected static int[] initPublicInput(String[] args){
		assert (args.length >= 2);
		int run = Integer.parseInt(args[0]);
		boolean defend = args[1].equals("defend");
		return initPublicInput(run, defend);
	}
}
