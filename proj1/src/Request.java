import java.io.Serializable;

public class Request implements Serializable {
    
    private String id;
    public Double value;
    public User requestor;
    public User requestee;
    private uIdService idService = uIdService.getInst();
    private GroupRequest grpReq = null;

    public Request(Double value, User requestor, User requestee) {
        this.value = value;
        this.requestor = requestor;
        this.requestee = requestee;
        this.id = idService.createReqID();
        //requestee.addRequest(this);
    }   

    public Request(Double value, User requestor, User requestee, GroupRequest grpReq ) {
        this.value = value;
        this.requestor = requestor;
        this.requestee = requestee;
        this.id = idService.createReqID();
        this.grpReq = grpReq;
    }

    public GroupRequest getGrpReq() {
        return grpReq;
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

    public User getRequestee(){
        return this.requestee;
    }

    @Override
    public String toString() {
        return "id: " + id + " valor: " + value + " requestor: " + requestor.getName() + "\n";
    }
}
