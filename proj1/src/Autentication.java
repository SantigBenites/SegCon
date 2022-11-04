import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.AlgorithmParameters;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.KeyStore.Entry;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.SealedObject;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

public class Autentication {

    private static String masterPW = "";
    private static String privateKeyAlias = "myserver";
    private static byte[] params = null;
    private static String workingDir = System.getProperty("user.dir") + "/";

    private static final byte[] salt = { (byte) 0xc9, (byte) 0x36, (byte) 0x78, (byte) 0x99, 
                                         (byte) 0x52, (byte) 0x3e, (byte) 0xea, (byte) 0xf2 };

    public static void setPW(String pw) {
        masterPW = pw;
    }

    // retirado dos slides da tp
    // este obj vai cifrar todos os ficheiros do lado do server
    public static Cipher getCipherEncrypt() {
        try {
            PBEKeySpec keySpec = new PBEKeySpec(masterPW.toCharArray(), salt, 20);
            SecretKeyFactory kf = SecretKeyFactory.getInstance("PBEWithHmacSHA256AndAES_128");
            SecretKey key = kf.generateSecret(keySpec);

            // inicializacao da cipher em modo de encriptacao
            Cipher c = Cipher.getInstance("PBEWithHmacSHA256AndAES_128");
            c.init(Cipher.ENCRYPT_MODE, key);
            
            return c;
        } catch (Exception e) {
            Print.error("Não foi possivel gerar chave de encriptação");
            return null;
        }   
    }

    public static Cipher getCipherDecrypt(String cParams) {
        try {
            PBEKeySpec keySpec = new PBEKeySpec(masterPW.toCharArray(), salt, 20);
            SecretKeyFactory kf = SecretKeyFactory.getInstance("PBEWithHmacSHA256AndAES_128");
            SecretKey key = kf.generateSecret(keySpec);
            
            // inicializacao da cipher em modo de decriptacao
            AlgorithmParameters p = AlgorithmParameters.getInstance("PBEWithHmacSHA256AndAES_128");
            byte[] parameters = Files.readAllBytes(Paths.get(workingDir + "server/" + cParams));
            Print.error("   params?: " + parameters);
            p.init(parameters);
            Cipher d = Cipher.getInstance("PBEWithHmacSHA256AndAES_128");
            d.init(Cipher.DECRYPT_MODE, key, p);

            return d;
        } catch (Exception e) {
            e.printStackTrace();
            Print.error("Não foi possivel gerar chave de decriptação");
            return null;
        }
    }

    public static Boolean encriptUsersFile(byte[] dados) {

        workingDir = System.getProperty("user.dir") + "/";
        Cipher c = getCipherEncrypt();

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(workingDir + "resources/users.cif"));) {
            // selamos a lista do catalogo que cifra e serializa a mesma
            SealedObject catSelado = new SealedObject(dados, c);
            oos.writeObject(catSelado);
            
            // apos escrever guardar parametros de cifra no nome apropriado
            byte[] parameters = c.getParameters().getEncoded();
            Print.magenta(" params ao cifrar users.txt: " + parameters);
            try (FileOutputStream stream = new FileOutputStream(workingDir + "server/userstxt.params")) {
                stream.write(parameters);
            }

        } catch (Exception e) {
            Print.error("Erro ao encriptar o userstxt ");
            return false;
        }

        return true;
    }

    public static ArrayList<String[]> decriptUsersFile() {
        String dir = System.getProperty("user.dir") + "/resources/users.cif";
        Cipher d = getCipherDecrypt("userstxt.params");

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(dir))) {
            
            SealedObject sealedCatUsers = (SealedObject) ois.readObject();
            
            byte[] usersb = (byte[]) sealedCatUsers.getObject(d);
            String usersS = new String(usersb, StandardCharsets.UTF_8);
            Print.error(usersS);
            String[] entries = usersS.split("\n");
            
            ArrayList<String[]> result = new ArrayList<>();
            for (String entry : entries) {
                result.add(entry.split(":"));
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            Print.error("Erro ao desencriptar o catalogo de users");
            return null;
        }
    }

    public static void sealCat(ArrayList listaCat, String catName) {
        workingDir = System.getProperty("user.dir") + "/";
        Cipher c = getCipherEncrypt();

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(workingDir + "resources/" + catName + ".cif"));) {
            // selamos a lista do catalogo que cifra e serializa a mesma
            SealedObject catSelado = new SealedObject(listaCat, c);
            oos.writeObject(catSelado);
            
            // apos escrever guardar parametros de cifra no nome apropriado
            byte[] parameters = c.getParameters().getEncoded();
            Print.magenta(" params ao cifrar " + catName + ": " + parameters);
            try (FileOutputStream stream = new FileOutputStream(workingDir + "server/" + catName + ".params")) {
                stream.write(parameters);
            }

        } catch (Exception e) {
            Print.error("Erro ao encriptar o catalogo " + catName);
        }
        
    }

    public static ArrayList<User>      unSealUsers() {
        String dir = System.getProperty("user.dir") + "/resources/usersCat.cif";
        Cipher d = getCipherDecrypt("usersCat.params");

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(dir))) {
            SealedObject sealedCatUsers = (SealedObject) ois.readObject();
            return (ArrayList<User>) sealedCatUsers.getObject(d);
        } catch (Exception e) {
            e.printStackTrace();
            Print.error("Erro ao desencriptar o catalogo de users");
            return null;
        }
    }

    public static ArrayList<RequestQR> unSealQrReq() {
        String dir = System.getProperty("user.dir") + "/resources/qrReqCat.cif";
        Cipher d = getCipherDecrypt("qrReqCat.params");

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(dir))) {
            SealedObject sealedCatUsers = (SealedObject) ois.readObject();
            return (ArrayList<RequestQR>) sealedCatUsers.getObject(d);
        } catch (Exception e) {
            e.printStackTrace();
            Print.error("Erro ao desencriptar o catalogo de QRrequest");
            return null;
        }
    }

    public static ArrayList<Group>     unSealGroups() {
        String dir = System.getProperty("user.dir") + "/resources/groupsCat.cif";
        Cipher d = getCipherDecrypt("groupsCat.params");

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(dir))) {
            SealedObject sealedCatUsers = (SealedObject) ois.readObject();
            return (ArrayList<Group>) sealedCatUsers.getObject(d);
        } catch (Exception e) {
            e.printStackTrace();
            Print.error("Erro ao desencriptar o catalogo de Groups");
            return null;
        }
    }

    public static Boolean authenticate(String user, ObjectInputStream ois, ObjectOutputStream oos) throws Exception{

        PropHandler pHandler = PropHandler.getInst();

        long nonce = (new Random()).nextLong();
		oos.writeObject(nonce);
        System.out.println(nonce);
        
        Boolean userExists = pHandler.exists(user);

        oos.writeObject(userExists);

        if (!userExists) {
            //user doesnt exist
            String certificatePath = "server/" + user + "_certificate.cer";

            //Obtain values sent from user
            //Obtain original nonce
            long returnedNonce = (long) ois.readObject();

            //Obtain signed nonce
            byte[] signature = (byte[]) ois.readObject( );

            //get certificate files
            byte[] certificate = (byte[]) ois.readObject();
            File newCertificate = new File(certificatePath);
            FileOutputStream certificateFOS  = new FileOutputStream(newCertificate);
            certificateFOS.write(certificate); 

            //Obtain certificate
            FileInputStream fis = new FileInputStream(certificatePath);
            CertificateFactory cf = CertificateFactory.getInstance("X509");
            Certificate cert = cf.generateCertificate(fis);

            //Verify Signature
            Signature s = Signature.getInstance("MD5withRSA");
            s.initVerify(cert.getPublicKey());
            ByteBuffer buffer = ByteBuffer.allocate(8);

            buffer.putLong(nonce);
            s.update(buffer);

            /*
                estamos mesmo a verificar  que o user tem a chave privada da
                publica que enviou ?
                nao devia ser:
                receber o nonce e o nonce cifrado -> decifrar o nonce cifrado ->
                comparar ?
            */

            if(!(returnedNonce == nonce && s.verify(signature))){
                return false;
            }

			pHandler.addUser(user, certificatePath);
            User newUser = new User(user);
            CatalogoUsers.getInstance().addUser(newUser);
            return true;

        }else{
            //user exists
            String certificatePath = "server/" + user + "_certificate.cer";

            //Obtain signed nonce
            byte signature[] = (byte[]) ois.readObject( );

            ///Obtain certificate
            FileInputStream fis = new FileInputStream(certificatePath);
            CertificateFactory cf = CertificateFactory.getInstance("X509");
            Certificate cert = cf.generateCertificate(fis);

            //Verify Signature
            Signature s = Signature.getInstance("MD5withRSA");
            s.initVerify(cert.getPublicKey());
            ByteBuffer buffer = ByteBuffer.allocate(8);
            buffer.putLong(nonce);
            s.update(buffer);

            if(!s.verify(signature)){
                return false;
            }

            return true;

        }
    }

    public static PrivateKey getServerPrivateKey(){

        try{
            String keyStorePw = System.getProperty("javax.net.ssl.keyStorePassword");
            KeyStore kstore = KeyStore.getInstance(KeyStore.getDefaultType());
            FileInputStream kfile = new FileInputStream(System.getProperty("javax.net.ssl.keyStore")); //keystore
		    kstore.load(kfile,keyStorePw.toCharArray()); //password da keystore
		    PrivateKey privateKey = (PrivateKey) kstore.getKey(privateKeyAlias, keyStorePw.toCharArray());
            return privateKey;
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public static PublicKey getServerPublicKey(){

        try{
            String keyStorePw = System.getProperty("javax.net.ssl.keyStorePassword");
            KeyStore kstore = KeyStore.getInstance(KeyStore.getDefaultType());
            FileInputStream kfile = new FileInputStream(System.getProperty("javax.net.ssl.keyStore"));
            kstore.load(kfile,keyStorePw.toCharArray()); //password da keystore
            
            PublicKey publicKey = (PublicKey) kstore.getCertificate("myserver").getPublicKey();
            return publicKey;

        }catch(Exception e){
            Print.error("Error in getting server public key");
            e.printStackTrace();
            return null;
        }

    }
    
    public static PublicKey getUserPublicKey(String userId){
        String certificatePath = "server/" + userId + "_certificate.cer";

        try{
            FileInputStream fis = new FileInputStream(certificatePath);
            CertificateFactory cf = CertificateFactory.getInstance("X509");
            Certificate cert = cf.generateCertificate(fis);

            return cert.getPublicKey();
        }catch(Exception e){
            Print.error("Error in getting " + userId + "'s private key ");
            return null;
        }

    }

    public static Boolean validateTransaction(Pair<Transaction, byte[]> pair, User currentUser){
        Transaction newTrans = pair.getF();
        byte[] sign = pair.getL();
        
        Signature s = null;
        try{
            //Obtaining Signature instance from class
            s = Signature.getInstance("MD5withRSA");
            s.initVerify(Autentication.getUserPublicKey(currentUser.getName()));

            //Getting byte[] input stream from block object
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream blockOOS = new ObjectOutputStream(bos);
            blockOOS.writeObject(newTrans);
            blockOOS.flush();

            //Obtained bytes pertaining to block object
            byte [] data = bos.toByteArray();
            s.update(data);

            //Boolean res = s.verify(sign);

            if(s.verify(sign)){
                return true;
            }

            return false;
        }catch(Exception e){
            e.printStackTrace();
            Print.error("Error validation transaction");
            return false;
        }
    }

    public static Boolean valRandomTransaction(Pair<Transaction, byte[]> pair){
        Transaction newTrans = pair.getF();
        byte[] sign = pair.getL();

        Signature s = null;
        try{
            //Obtaining Signature instance from class
            s = Signature.getInstance("MD5withRSA");
            s.initVerify(Autentication.getUserPublicKey(newTrans.originator));

            //Getting byte[] input stream from block object
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream blockOOS = new ObjectOutputStream(bos);
            blockOOS.writeObject(newTrans);
            blockOOS.flush();

            //Obtained bytes pertaining to block object
            byte [] data = bos.toByteArray();
            s.update(data);
            if(s.verify(sign)){
                return true;
            }

            return false;
        }catch(Exception e){
            e.printStackTrace();
            Print.error("Error validation transaction");
            return false;
        }
    }

}
