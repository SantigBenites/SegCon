import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;

/**
 * Main entry point to the serverside application
 * 
 * Este pedaço de codigo trata apenas de: 
 * 		criar e manter o listening socket
 * 		criar as threads para cada um dos clientes (conecoes)
 */
public class Server{

	private static final String workingDir = System.getProperty("user.dir");

	/**
	 * 
	 * @param args porto = args[0], default port = 45678
	 */
	public static void main(String[] args) throws Exception{
		Print.system("servidor: main");
		Print.system("Número de args: " + args.length);
		Server server = new Server();
		server.startServer(args);
	}
	
	public void startServer(String[] args) throws Exception {
		
		if (args.length < 3 || args.length > 4) {
			Print.error("Número de argumentos invalidos, deve correr com:");
			Print.error("TrokosServer <port> <password-cifra> <keystore> <password-keystore>");
		}
		int offset = 0;
		int port = 45678;

		try {
			port = Integer.parseInt(args[0]);
		} catch (NumberFormatException e) {
			// se não for dado o porto
			port = 45678;
			offset = 1;
		}
		
		String cipherPW   = 				   args[1 - offset];
		String keystore   = workingDir + "/" + args[2 - offset];
		String keystorePW = 				   args[3 - offset];

		Print.system("Server: Porto em uso: " + port);

		Autentication.setPW(cipherPW);
		
		System.setProperty("javax.net.ssl.keyStore", keystore);
        System.setProperty("javax.net.ssl.keyStorePassword", keystorePW);

		if(!BlockChainHandler.verifyBlockChain()){
			Print.error("A integridade da blockchain foi compremetida, a sair");
			System.exit(-1);
		}
        
		//Create ssl socket
        ServerSocketFactory ssf = SSLServerSocketFactory.getDefault();
		SSLServerSocket sSoc = (SSLServerSocket) ssf.createServerSocket(port);
        
		try {
			//sSoc = (SSLServerSocket) ssf.createServerSocket(port);
			sSoc.setReuseAddress(true);

			// Handle Ctrl + c gracefully by closing the socket
			final SSLServerSocket servSocClone = sSoc;

			Runtime.getRuntime().addShutdownHook(new Thread(){
				public void run(){
					System.out.println("Server Shutdown Hook is running !");
					try {
						servSocClone.close();	
					} catch (IOException e) {}
				}
		  	});
			
		} catch (IOException e) {
			System.err.println(e.getMessage());
			System.exit(-1);
		}

		CatalogoUsers.getInstance();
		
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

}
