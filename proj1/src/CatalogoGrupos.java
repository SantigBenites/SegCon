import java.io.ByteArrayOutputStream;
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

public class CatalogoGrupos implements Serializable{
    
	//colors
	public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLUE = "\u001B[34m";

    private ArrayList<Group> groupList;
	private String workingDir = System.getProperty("user.dir");
	
	private static CatalogoGrupos INSTANCE = null;

	private CatalogoGrupos() {
		groupList = new ArrayList<Group>();
		loadFromFile();
		printctlg();
	}
	
	public static CatalogoGrupos getInstance() {
		if(INSTANCE == null) {
			INSTANCE =  new CatalogoGrupos();
		}
		return INSTANCE;
	}


	public void loadFromFile() {
		File f = new File(workingDir + "/resources/groupsCat.cif");
		Print.blue("CatalogoGroups: encontrado ficheiro? " + f.exists());
		if(f.exists()) { 
			this.groupList = Autentication.unSealGroups();
		}else{
			groupList = new ArrayList<Group>();	
		}
    }

	public void saveToFile(){
		Autentication.sealCat(groupList, "groupsCat");
	}

	public boolean exists(String groupId) {
        for (Group i : groupList) {
            if (i.getId().equals(groupId))
                return true;
        } 
        return false;
    }

    public Group getGroup(String groupId) {
		for(Group i: groupList) {
			if(i.getId().equals(groupId)) {
				return i;
			}
		}
		return null;
	}

	public List<Group> getAllOwnerGroups(String username){

		List<Group> ownerGroups = new ArrayList<Group>();

		for(Group group : groupList){
			if(group.userIsOwner(username)){
				ownerGroups.add(group);
			}
		}
		return ownerGroups;
	}

	public List<Group> getAllMemberGroups(String username){

		List<Group> memberGroups = new ArrayList<Group>();

		for(Group group : groupList){
			if(group.userExists(username)){
				memberGroups.add(group);
			}
		}
		return memberGroups;
	}

	public void addUserToGroup(String groupId, User user){
		Group g = INSTANCE.getGroup(groupId);
        g.addUser(user);
        INSTANCE.saveToFile();
    }
	
	public void addGroup(Group newGroup) {
		CatalogoGrupos.getInstance().groupList.add(newGroup);
		System.out.println(ANSI_BLUE + "CatalogoGrupos: Adicionado grupo ao catalogo " + ANSI_RESET);
		INSTANCE.saveToFile();
	}

	public void printctlg() {
		System.out.println(ANSI_BLUE + "Lista de Users no catalogo: " + ANSI_RESET);
		for (Group group : groupList) {
			System.out.println(ANSI_BLUE + group.toString() + ANSI_RESET);
		}
	}
}
