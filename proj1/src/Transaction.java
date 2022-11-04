import java.io.Serializable;
import java.security.Signature;

public class Transaction implements Serializable{

    public String originator;
    public String recipient;
    public Double amount;


    public Transaction(String originator, String recipient, Double amount){
        this.originator = originator; 
        this.recipient = recipient;
        this.amount = amount;
    }


}
