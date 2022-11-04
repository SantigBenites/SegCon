import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class CatalogoUsers implements Serializable{

	//colors
	public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    
    private ArrayList<User> userList;
	private String workingDir = System.getProperty("user.dir");

	private static CatalogoUsers INSTANCE = null;

	private CatalogoUsers() {
		userList = new ArrayList<User>();
		loadFromFile();

		printctlg();
	}
	
	public static CatalogoUsers getInstance() {
		if(INSTANCE == null) {
			INSTANCE =  new CatalogoUsers();
		}
		return INSTANCE;
	}

    public void loadFromFile() {
		
		File f = new File(workingDir + "/resources/usersCat.cif");
		Print.yellow("CatalogoUsers: encontrado ficheiro? " + f.exists());
		if(f.exists()) { 
			this.userList = Autentication.unSealUsers();
		}else{
			userList = new ArrayList<User>();	
		}
    
    }

	/**
	 * Guarda o estado atual do catalogo para o ficheiro
	 */
	public void saveToFile(){
    	Autentication.sealCat(userList, "usersCat");
	}

    public User getUser(String userName) {
		for(User i: userList) {
			if(i.getName().equals(userName)) {
				return i;
			}
		}
		return null;
	}

	public void setGuitos (Double d) {
		
	}

	public boolean exists(String userName) {
		for(User i: userList) {
			if(i.getName().equals(userName)) {
				return true;
			}
		}
		return false;
	}
	
	public void addUser(User currentUser) {
		CatalogoUsers.getInstance().userList.add(currentUser);
		System.out.println(ANSI_YELLOW + "CatalogoUsers: Adicionado user ao catalogo " + ANSI_RESET);
		INSTANCE.saveToFile();
	}

	public void printctlg() {
		Print.yellow("Lista de Users no catalogo: ");
		for (User user : userList) {
			Print.yellow(user.toString());
		}
	}



}
