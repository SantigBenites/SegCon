import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Client {

	public static void main(String[] args) {
		
		try {
			Socket clientSocket = new Socket("localhost", 23456);
			ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
			ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());
			
			FileOutputStream fileOut = new FileOutputStream("ClientUsers.txt");
			
			out.writeObject("Santiago");
			
			Boolean resultado = (Boolean)in.readObject();
			
			System.out.println("Acess has: " +  ((resultado) ? "granted" : "denied"));
		
			
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		

	}

}