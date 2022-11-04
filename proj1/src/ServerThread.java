import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;


/**
 * Segundo ponto de execução da aplicação servidor,
 * para cada cliente é criada uma nova thread de forma a poder  
 * acomodar varios clientes.
 * 
 * Este codigo recebe o user e pw do cliente e autentica o mesmo
 * Em caso de sucesso passa a execução da logica da aplicação para
 * a classe ServerApp
 */
class ServerThread extends Thread {

	public User currentUser;
	private Socket socket = null;
	private PropHandler pHandler;
	private CatalogoUsers catalogoUsers;

	ServerThread(Socket inSoc) {
		socket = inSoc;
		System.out.println("	Nova thread com id: " + this.getId());
	}
	
	@Override
	public void run(){
		try {
			ObjectOutputStream outStream = new ObjectOutputStream(socket.getOutputStream());
			ObjectInputStream inStream = new ObjectInputStream(socket.getInputStream());

			catalogoUsers = CatalogoUsers.getInstance();
			
			pHandler = PropHandler.getInst();

			String user = (String) inStream.readObject();

			System.out.println("	thread: " + this.getId() + " recebeu user: " + user);

			if (Autentication.authenticate(user, inStream, outStream)) {

				//User was autenticated
				System.out.println("Autentication sucessfull");
				outStream.writeObject("Autentication was sucessefull");
				outStream.writeObject("Login sucessufull");
				currentUser = catalogoUsers.getUser(user);
				ServerApp menu = new ServerApp(currentUser, socket);
				menu.run(inStream, outStream);
					
			} else {
				//Authenticantion wasn't sucessfull
				outStream.writeObject("Invalid Autentication");
				return;
			}

			outStream.close();
			inStream.close();
			
			socket.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
