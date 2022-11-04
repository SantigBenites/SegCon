import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;

public class Block implements Serializable{
    
    byte[] hash;
    long blockID;
    int numberOfTransactions = 0;
    List<Pair<Transaction, byte[]>> transactions;
    byte[] sign;

    public Block(long blockID){
        this.transactions = new ArrayList<Pair<Transaction,byte[]>>();
        this.blockID = blockID;
    }

    public void addTransaction(Transaction newTrans, byte[] newSign){
        
        this.transactions.add(new Pair<Transaction,byte[]>(newTrans, newSign));
        this.numberOfTransactions++;
        Print.error("numberOfTransactions = " + numberOfTransactions);
    }

    public void setHash(Block previousBlock){

        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream blockOOS = new ObjectOutputStream(bos);
            blockOOS.writeObject(previousBlock);
            blockOOS.flush();
            byte [] data = bos.toByteArray();
            this.hash = md.digest(data);

        } catch (NoSuchAlgorithmException | IOException e) {

            e.printStackTrace();
        }

    }

    public void setSign(byte[] sign){
        this.sign = sign;
    }

}

