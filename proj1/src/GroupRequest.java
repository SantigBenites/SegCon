import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class GroupRequest implements Serializable {
    
    private String id;
    private Double value;
    private User requestor;
    private List<User> devedores;
    private uIdService idService = uIdService.getInst();

    private Group group;

    public GroupRequest(Double value, Group relatedGroup) {
        this.id = idService.createGrpReqID();
        this.value = value;
        this.requestor = relatedGroup.getOwner();
        this.group = relatedGroup;
        this.devedores = new ArrayList<User>(group.getMembers());

        Double dividend = value / (devedores.size() + 1);

        for (User membro : devedores) {
            Request r = new Request(dividend, requestor, membro, this);
            membro.addRequest(r);
        }
        //requestee.addRequest(this);
    }   

    public void removerDev(User u) {
        devedores.remove(u);

        if (devedores.size() == 0) {
            group.doHistory(this.id);
        }
    }

    public String getId(){
        return this.id;
    }

    public Double getValue(){
        return this.value;
    }

    public User getRequestor(){
        return this.requestor;
    }

    public List<User> getDevedores(){
        return this.devedores;
    }
    
}
