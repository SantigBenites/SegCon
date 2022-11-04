import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.processing.RoundEnvironment;

import java.util.Collection;

public class User implements Serializable{

    private Double balance = 100.0;
    private String name;
    private Map<String, Request> requests;
    private Map<String, Group> groups;

    public User(String name) {
        this.name = name;
        this.requests = new HashMap<String, Request>();
        this.groups = new HashMap<String, Group>();
    }

    public String getName(){
        return this.name;
    }

    public Double getBalance(){
        return this.balance;
    }

    /**
     * recebe um valore atualiza o balance do user de acordo com o mesmo
     * 
     * @param valor o valor a ser transacionado, se for positivo adiciona à conta, se for negativo subtrai
     * @return o novo balance
     */
    public Double mexeGitos(Double valor) {
        this.balance += valor;
        return balance;
    }

    /**
     * 
     * @param id id a procurar
     * @return true se id existir no mapa, false caso contrario
     */
    public boolean hasRequest(String id) {
        return requests.containsKey(id);
    }

    public void addRequest (Request req) {
        requests.put(req.getId(), req);
    }
    
    public Collection<Request> getRequests() {
        return requests.values();
    }

    /**
     * 
     * @param id id a procurar
     * @return o request com id = id
     */
    public Request getRequest(String id) {
        return requests.get(id);
    }

    public void remRequest(String id) {
        requests.remove(id);
    }

    /**

    */
    public void addGroup(Group group){
        this.groups.put(group.getId(), group);
    }

    public Group getGroup(String id) {
        return groups.get(id);
    }

    public Collection<Group> getGroups(){
        return groups.values();
    }

    @Override
    public String toString() {
        return "{userName: " + this.name + ", balance: " + this.balance + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User that = (User) o;
        return this.name.equals(that.name);
    }

}
