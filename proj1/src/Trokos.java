import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.security.Certificate;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.Scanner;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.net.SocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.swing.plaf.basic.BasicInternalFrameTitlePane.SystemMenuBar;

public class Trokos {

	private static final String workingDir = System.getProperty("user.dir");
	public static void main(String[] args) {
		// ex 127.0.0.1:45678 users/user_2.truststore users/user_2.keystore keystorepassword user_2
		String IP = "";
		int port = 45678;
		String userID = "";

		// user id
		try{
			userID = args[4];
		} catch (ArrayIndexOutOfBoundsException e){
			Print.error("Número de argumentos invalido, deve correr da forma:");
			Print.error("Trokos <serverAddress> <truststore> <keystore> <password-keystore> <userID>");
			e.printStackTrace();
			System.exit(-1);
		}

		// ip:porto
		String[] IpAndHost = args[0].split(":");
		IP = IpAndHost[0];
		System.out.println(IP);

		try {
			port = Integer.parseInt(IpAndHost[1]);	
		} catch (Exception e) {
			port = 45678;
		}

		// truststore keystore keystorepw
		String truststore = workingDir + "/" + args[1];
		String keystore   = workingDir + "/" + args[2];
		String keystorePW =                    args[3];

		try {
			Scanner sc = new Scanner(System.in);
			// set truststore
			System.setProperty("javax.net.ssl.trustStore", truststore);
        	System.setProperty("javax.net.ssl.trustStoreType", "JCEKS");

			// set keystore
			System.setProperty("javax.net.ssl.keyStore", keystore);
        	System.setProperty("javax.net.ssl.keyStorePassword", keystorePW);
			System.setProperty("javax.net.ssl.keyStoreType", "JCEKS");

        	SocketFactory sf = SSLSocketFactory.getDefault();
        	SSLSocket clientSocket = (SSLSocket) sf.createSocket(IP, port);

			//Initial Communication
			ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());
			ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
			out.writeObject(userID);

			//Autentication
			long nonce = (long) in.readObject();
			Boolean flagUserExists = (Boolean) in.readObject();
			System.out.println(flagUserExists);

			Signature s = null;

			if(!flagUserExists){
				//User doesnt exist in the user base
				try {

					//PublicKey certificate
					//This creates a get certificate file
					FileInputStream kfile = new FileInputStream(keystore); //keystore
					KeyStore kstore = KeyStore.getInstance("JCEKS");
					kstore.load(kfile, keystorePW.toCharArray()); //password da keystore
					java.security.cert.Certificate cert = kstore.getCertificate("userkeys"); //alias da keypair
					
					//Keys Pair
					PrivateKey privateKey = (PrivateKey) kstore.getKey("userkeys", keystorePW.toCharArray());
  					PublicKey publicKey = cert.getPublicKey();
					
					//Get PrivateKey(nonce)
					s = Signature.getInstance("MD5withRSA");
					s.initSign(privateKey);
					ByteBuffer buffer = ByteBuffer.allocate(8);
            		buffer.putLong(nonce);
            		s.update(buffer);


					//Sending to server (nonce,PrivateKey(nonce),certificate)
					out.writeObject(nonce);
					System.out.print(nonce + " ");
					out.writeObject(s.sign());
					System.out.print(s.sign()+ " ");
					out.writeObject(cert.getEncoded());
					System.out.println(cert.getEncoded()+ " ");

				}catch (NoSuchAlgorithmException | 
						InvalidKeyException | 
						KeyStoreException | 
						SignatureException | 
						UnrecoverableKeyException | 
						CertificateException e) {
					e.printStackTrace();
				}

			}else{
				//User exists in the user base

				try {
					
					//PublicKey certificate
					//This creates a get certificate file
					FileInputStream kfile = new FileInputStream(keystore); //keystore
					KeyStore kstore = KeyStore.getInstance("JCEKS");
					kstore.load(kfile, keystorePW.toCharArray()); //password da keystore
					
					//Keys Pair
					PrivateKey privateKey = (PrivateKey) kstore.getKey("userkeys", keystorePW.toCharArray());
					
			  		//Get PrivateKey(nonce)
					s = Signature.getInstance("MD5withRSA");
					s.initSign(privateKey);
					ByteBuffer buffer = ByteBuffer.allocate(8);
            		buffer.putLong(nonce);
            		s.update(buffer);

					//Sending to server (PrivateKey(nonce))
					out.writeObject(s.sign());
					System.out.println("Sent to server");

				} catch (Exception e) {
					//TODO: handle exception
				}

			}

			String validationMessage = (String) in.readObject();

			if(!validationMessage.equals("Autentication was sucessefull")){
				System.out.println("Validation Error");
				return;
			}
			

			//Return from Server
			String fromServer = (String) in.readObject();
			System.out.println("From server is " + fromServer);
			if(fromServer.equals("Login sucessufull") || fromServer.equals("User added to database")){
				MenuClient menu = new MenuClient(clientSocket, userID, s);
				menu.run(in,out,sc);
			}
			
			sc.close();
			out.close();
			in.close();

		}catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}

