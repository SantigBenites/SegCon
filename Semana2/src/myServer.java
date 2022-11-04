/*************************
*   Seguranca e Confiabilidade 2020/21
*
*
*************************/

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;

//Servidor myServer

public class myServer{
	
	Properties users = null;

	public static void main(String[] args) {
		System.out.println("servidor: main");
		myServer server = new myServer();
		server.startServer();
	}
	
	public void load() {
		
		users = new Properties();
		InputStream input = null;
		try {
			input = new FileInputStream("users.txt");
			users.load(input);
		} catch (FileNotFoundException e) {
			System.out.print("Service not Available");
		} catch (IOException e) {
			System.out.print("File not Found");
		}
	

	}

	public void startServer (){
		ServerSocket sSoc = null;
        
		try {
			sSoc = new ServerSocket(23456);
			sSoc.setReuseAddress(true);
			
		} catch (IOException e) {
			System.err.println(e.getMessage());
			System.exit(-1);
		}
         
		while(true) {
			try {
				Socket inSoc = sSoc.accept();
				ServerThread newServerThread = new ServerThread(inSoc);
				newServerThread.start();
		    }
		    catch (IOException e) {
		        e.printStackTrace();
		    }
		    
		}
		//sSoc.close();
	}


	//Threads utilizadas para comunicacao com os clientes
	class ServerThread extends Thread {

		private Socket socket = null;

		ServerThread(Socket inSoc) {
			socket = inSoc;
			System.out.println("thread do server para cada cliente");
		}
 
		public void run(){
			try {
				ObjectOutputStream outStream = new ObjectOutputStream(socket.getOutputStream());
				ObjectInputStream inStream = new ObjectInputStream(socket.getInputStream());

				String user = null;
				String passwd = null;
			
				try {
					user = (String)inStream.readObject();
					passwd = (String)inStream.readObject();
					System.out.println("thread: depois de receber a password e o user");
					
					if(user != null) {
					
						load();
						String realPassword = users.getProperty(user);
						
						if (passwd.equals(realPassword)){
							outStream.writeObject(new Boolean(true));
						}
						else {
							users.setProperty(user, passwd);
							outStream.writeObject(new Boolean(false));
						}
					}
					
				}catch (ClassNotFoundException e1) {
					e1.printStackTrace();
				}
 			

				outStream.close();
				inStream.close();
 			
				socket.close();

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}