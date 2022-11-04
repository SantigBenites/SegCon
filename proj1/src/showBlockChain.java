import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class showBlockChain {
    

    
    public static void main(String[] args) {

        String workingDir = System.getProperty("user.dir") + "/";
        File[] files = new File(workingDir + "blockChain").listFiles();
        
        List<File> filesList = Arrays.stream(files).sorted((f1, f2) -> f1.getName().compareTo(f2.getName())).collect(Collectors.toList());
        List<Block> blockChain = new ArrayList<>();

        for (File f : filesList) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f))) {
                blockChain.add((Block) ois.readObject());
            } catch (Exception e) {
            }
        }

        for(Block b: blockChain){
            Print.blue("Block ID -> " + b.blockID);
            Print.blue("Number of Transactions ->" + b.numberOfTransactions);

            for(Pair<Transaction, byte[]> pair : b.transactions){
                Transaction t = pair.getF();
                Print.red("Originator : " + t.originator + " Recipient : " + t.recipient + " Amount : " + t.amount);
            }
            System.out.println();
        }


    }

}
