package engagement1.gabfeed.webserver;

import gov.nasa.jpf.symbc.Debug;

public class Driver {
	
	public static void main(String args[]){

		int type = Integer.parseInt(args[0]);
		int size = Integer.parseInt(args[1]);
		User user = createUser(type, size);
		String input = Debug.makeSymbolicString("l",size);
		if(user != null){
			user.matches("abc",input);
		}
	}
	
	public static User createUser(int type, int size){
		String password = Debug.makeSymbolicString("h", size);
		User user = null;
		switch(type){
		case 1:
			user = new User1 ("id","abc",password);
			break;
		case 2:
			user = new User2 ("id","abc",password);
			break;
		case 3:
			user = new User3 ("id","abc",password);
			break;
		case 4:
			user = new User4 ("id","abc",password);
			break;
		case 5:
			user = new User5 ("id","abc",password);
			break;
		default:
			return null;
		}
		return user;
	}
}
