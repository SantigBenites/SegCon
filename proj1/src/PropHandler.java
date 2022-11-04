import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PropHandler {
    
    private ArrayList<String[]> usersList;
    private String workingDir = System.getProperty("user.dir");
    private File userFile = new File(workingDir + "/resources/users.txt");

    private static PropHandler INSTANCE = null;

    private PropHandler() throws IOException {

        File stored = new File(workingDir + "/resources/users.cif");
        Print.error(workingDir + "/resources/users.cif");

        usersList = new ArrayList<>();
        Print.error("a inicializar prophandler" + stored.exists());
        if (stored.exists()) {
            usersList = Autentication.decriptUsersFile();
        }

    }

    public static PropHandler getInst() throws IOException{
        if(INSTANCE == null) {
			INSTANCE =  new PropHandler();
		}
		return INSTANCE;
    }

    public void printig() {
        for (String[] p : usersList) {
            System.out.println(Arrays.toString(p));
        }
    }
    /*
    for (String[] p : usersList) {
                out.write();
            }
            p[0] + ":" + p[1] + "\n"
    */
    public void store(){
        StringBuilder builder = new StringBuilder();
        for (String[] p : usersList) {
            builder.append(p[0] + ":" + p[1] + "\n");
        }
        Autentication.encriptUsersFile(builder.toString().getBytes());
    }
    //[username,password],[username2,password]
    public String getPW(String userName) {
        String res = null;
        for(String[] par : INSTANCE.usersList ){
            if(par[0].equals(userName) ){
                res = par[1];
            }
        }
        return res;
    }

    public void addUser(String userName, String passWord) {
        String[] newUser = {userName,passWord};
        INSTANCE.usersList.add(newUser);
        INSTANCE.store();
    }

    public boolean exists(String userName) {
        for (String[] u : usersList) {
            if (u[0].equals(userName))
                return true;
        } 
        return false;
    }

    

    public static Byte[] toObjects(byte[] bytesPrim) {

        Byte[] bytes = new Byte[bytesPrim.length];
        int i = 0;
        for (byte b : bytesPrim) bytes[i++] = b; //Autoboxing
        return bytes;
    
    }
}
