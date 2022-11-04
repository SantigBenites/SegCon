import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

public class Group implements Serializable{
    
    private String id;
    private User owner;
    private HashMap<String, GroupRequest> requestMap;
    private ArrayList<User> members;
    private ArrayList<String[]> history;

    public Group(String groupId, User owner){
        this.owner = owner;
        this.id = groupId;
        requestMap = new HashMap<String, GroupRequest>();
        members = new ArrayList<User>();
        history = new ArrayList<String[]>();
    }

    public String getId(){
        return this.id;
    }

    public User getOwner(){
        return this.owner;
    }

    public void addUser(User u){
        members.add(u);
    }

    public void newGroupReq(Double amount) {
        GroupRequest greq = new GroupRequest(amount, this);

        requestMap.put(greq.getId(), greq);
    }

    public GroupRequest getGrpReq(String id) {
        return requestMap.get(id);
    }

    public Collection<GroupRequest> getGrpReqs() {
        return requestMap.values();
    }

    public void doHistory(String id) {
        GroupRequest grpReq = requestMap.get(id);
        String[] h = {grpReq.getId(), Double.toString(grpReq.getValue())};
        history.add(h);

        requestMap.remove(id);
    }

    public ArrayList<String[]> getHistory() {
        return history;
    }

    public boolean userExists(String username){

        CatalogoUsers CU = CatalogoUsers.getInstance();
        User user = CU.getUser(username);

        for(User u : members){
            if(u.equals(user)){
                return true;
            }
        }
        return false;
    }

    public ArrayList<User> getMembers() {
        return this.members;
    }

    public boolean userIsOwner(String username){

        CatalogoUsers CU = CatalogoUsers.getInstance();
        User user = CU.getUser(username);

        if (this.owner.equals(user)) {
            return true;
        }else{
            return false;
        }
        
    }


    @Override
    public String toString() {
        StringBuilder StrB = new StringBuilder();
        StrB.append("\nOwner = " + this.owner.getName() + "\nID = " + this.id + "\nMembers:\n\n");
        for(User u : members){
            StrB.append(u.getName());
            StrB.append("\n");
        }   
        return StrB.toString();
    }


}
