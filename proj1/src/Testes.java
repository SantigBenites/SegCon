import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class Testes {
    

    public static void main(String[] args) {

        System.setProperty("javax.net.ssl.keyStore", "server/keystore");
        System.setProperty("javax.net.ssl.keyStorePassword", "changeit");

        Random rd = new Random();
        byte[] arr = new byte[256];
        rd.nextBytes(arr);
        Transaction t = new Transaction("user1", "user2", 10.0);
        Pair<Transaction, byte[]> pair = new Pair<Transaction, byte[]>(t,arr);

        User u = new User("user1");

        System.out.println(Autentication.validateTransaction(pair,u));
    }
}
