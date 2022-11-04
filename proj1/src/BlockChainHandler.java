import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class BlockChainHandler {

    Block currentBlock;
    String workingDir = System.getProperty("user.dir") + "/";

    private static BlockChainHandler INSTANCE = null;

    private BlockChainHandler(){
        currentBlock = loadCurrentBlock();
    }
	
	public static BlockChainHandler getInstance() {
		if(INSTANCE == null) {
			INSTANCE = new BlockChainHandler();
		}
		return INSTANCE;
	}

    public Block loadCurrentBlock(){

        int max = getCurrentBlockIndex();

        try {
            File f = new File(workingDir + "blockChain/block_" + max + ".blk");
            if (!f.exists()) {
                // se não existir fichero com numero max significa que não existe nenhum ficheiro
                f.createNewFile();
                Block firstBlock = new Block(0);
                firstBlock.hash = new byte[32];
                currentBlock = firstBlock;

                ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(f));
                oos.writeObject(currentBlock);
                oos.close();

                return currentBlock;
            }

            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f));
            currentBlock = (Block) ois.readObject();
            ois.close();

            if (currentBlock.numberOfTransactions == 5) {
                currentBlock.sign = getSignature();

                Block nBlock = new Block(currentBlock.blockID + 1);
                nBlock.setHash(currentBlock);
                
                currentBlock = nBlock;
                return currentBlock;
            }

            return currentBlock;

        } catch (Exception e) {
            Print.error("Erro ao carregar ultimo bloco.");
            e.printStackTrace();
            return null;
        }
    }

    public void addToCurrentBlock(Pair<Transaction, byte[]> pair){

        if(currentBlock.transactions.size() < 5){
            //Block still has space
            currentBlock.addTransaction(pair.getF(), pair.getL());
            saveCurrentBlock();

        }else{
            //Block is full
            
            saveCurrentBlock();
            this.currentBlock = newBlock(currentBlock);
            currentBlock.addTransaction(pair.getF(), pair.getL());
            saveCurrentBlock();
        }

    }

    private Block newBlock(Block current) {
        try {
            long newIndex = current.blockID + 1;
            Block result = new Block(newIndex);
            result.setHash(current);
            File f = new File(workingDir + "blockChain/block_" + newIndex + ".blk");
            f.createNewFile();

            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(f));
            oos.writeObject(currentBlock);
            oos.close();
            
            return result;    
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        
    }

    private void saveCurrentBlock(){
        
        int max = getCurrentBlockIndex();
        
    	try {
    	    FileOutputStream   fop = new FileOutputStream(workingDir + "blockChain/block_" + max + ".blk");
    	    ObjectOutputStream oos = new ObjectOutputStream(fop);
            // assinar o bloco no estado atual e guardar essa informação
            currentBlock.sign = null;
            currentBlock.setSign(getSignature());
    	    oos.writeObject(currentBlock);
			oos.close();
			
    	} catch (IOException e) {
			e.printStackTrace();
		}

	}

    public void setCurrentBlockHash(){
        currentBlock.setHash(getPreviousBlock(currentBlock.blockID));
    }

    public Block getPreviousBlock(long currentBlockIndex){

        try{
            long index = currentBlockIndex-1;
            FileInputStream fis = new FileInputStream(workingDir + "blockChain/block_" + index + ".blk");
            ObjectInputStream ois = new ObjectInputStream(fis);

            Block newBlock = (Block) ois.readObject();
            ois.close();
            return newBlock;
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }

    }

    public byte[] getSignature(){
        try{
            Signature s = Signature.getInstance("MD5withRSA");
		    s.initSign(Autentication.getServerPrivateKey());

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream blockOOS = new ObjectOutputStream(bos);
            blockOOS.writeObject(currentBlock);
            blockOOS.flush();
            byte [] data = bos.toByteArray();

            s.update(data);
            return s.sign();
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public int getCurrentBlockIndex(){
        String workingDir = System.getProperty("user.dir") + "/";
        File[] files = new File(workingDir + "blockChain").listFiles();

        List<Integer> index = new ArrayList<Integer>();
        for(File f : files){
            index.add(Integer.parseInt(f.getName().replaceAll("[^0-9]", "")));
        }

        int max = 0;

        if(index.size() > 0){
            max = index.stream().max(Integer::compare).get();
        }

        return max;
    }

    public static byte[] getSigOfBlock(Block b) {
        try{
            Signature s = Signature.getInstance("MD5withRSA");
		    s.initSign(Autentication.getServerPrivateKey());

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream blockOOS = new ObjectOutputStream(bos);
            blockOOS.writeObject(b);
            blockOOS.flush();
            byte [] data = bos.toByteArray();

            s.update(data);
            return s.sign();
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public static Boolean verifyBlockChain () {
        String workingDir = System.getProperty("user.dir") + "/";

        File dir = new File(workingDir + "blockChain");
        dir.mkdir();

        File[] files = new File(workingDir + "blockChain").listFiles();

        // se existirem menos que 2 ficheiros não é possivel verificar a blockchain, não é uma cadeia.
        if (files.length < 2) {
            return true;
        }
        
        List<File> filesList = Arrays.stream(files).sorted((f1, f2) -> f1.getName().compareTo(f2.getName())).collect(Collectors.toList());
        List<Block> blockChain = new ArrayList<>();

        for (File f : filesList) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f))) {
                blockChain.add((Block) ois.readObject());
            } catch (Exception e) {
            }
        }

        if (!Arrays.equals(blockChain.get(0).hash, new byte[32])) {
            System.out.println(blockChain.get(0).hash);
            Print.error("O primeiro bloco tem uma hash diferente do que apenas 0");
            return false;
        }

        int i = 1;
        boolean mismatch = false;

        while(!mismatch && i < blockChain.size()) {
            byte [] calculatedHash = null;
            try {
                MessageDigest md = MessageDigest.getInstance("SHA-256");
    
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                ObjectOutputStream blockOOS = new ObjectOutputStream(bos);
                blockOOS.writeObject(blockChain.get(i-1));
                blockOOS.flush();
                byte [] data   = bos.toByteArray();
                calculatedHash = md.digest(data);
            } catch (NoSuchAlgorithmException | IOException e) {
                e.printStackTrace();
            }

            byte[] storedHash = blockChain.get(i).hash;
            
            if (!Arrays.equals(calculatedHash, storedHash)) {
                mismatch = true;
                Print.error("A blockchain é invalida, \n");
                Print.error("expecificamente o hash guardado no bloco nr " + i);
                Print.error("não corresponde ao hash do bloco anterior, " + (i-1));
            } else {
                Print.green("storedHash " + i + " igual ao calculatedHash " + i);
            }

            Signature s = null;
            try{
                //Obtaining Signature instance from class
                s = Signature.getInstance("MD5withRSA");
                s.initVerify(Autentication.getServerPublicKey());

                byte[] signCopy = blockChain.get(i).sign.clone();
                blockChain.get(i).sign = null;

                //Getting byte[] input stream from block object
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                ObjectOutputStream blockOOS = new ObjectOutputStream(bos);
                blockOOS.writeObject(blockChain.get(i));
                blockOOS.flush();

                //Obtained bytes pertaining to block object
                byte [] data = bos.toByteArray();
                s.update(data);
                if(!s.verify(signCopy)){
                    Print.error("A assinature do block com index " + i + " encontra-se errada\n");
                    mismatch = true;
                }
                blockChain.get(i).sign = signCopy;
            }catch(Exception e){
                e.printStackTrace();
            }

            for (Pair<Transaction, byte[]> transaction : blockChain.get(i).transactions) {
                Print.yellow("" + Autentication.valRandomTransaction(transaction));
            }
            i++;
        }

        return !mismatch;
    }

}
