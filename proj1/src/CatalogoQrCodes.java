import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.swing.plaf.basic.BasicInternalFrameTitlePane.CloseAction;

public class CatalogoQrCodes implements Serializable {
    
    
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_CYAN = "\033[0;36m";
    
    private ArrayList<RequestQR> qrList;
	private String workingDir = System.getProperty("user.dir");

	private static CatalogoQrCodes INSTANCE = null;

	private CatalogoQrCodes() {
		qrList = new ArrayList<RequestQR>();
		loadFromFile();
		printctlg();
	}
	
	public static CatalogoQrCodes getInstance() {
		if(INSTANCE == null) {
			INSTANCE =  new CatalogoQrCodes();
		}
		return INSTANCE;
	}

    public void loadFromFile(){
		File f = new File(workingDir + "/resources/qrReqCat.cif");
		Print.cyan("CatalogoQrcodes: encontrado ficheiro? " + f.exists());
		if(f.exists()) { 
			this.qrList = Autentication.unSealQrReq();
		}else{
			qrList = new ArrayList<RequestQR>();	
		}
    
    }

	/**
	 * Guarda o estado atual do catalogo para o ficheiro
	 */
	public void saveToFile(){
		Autentication.sealCat(qrList, "qrReqCat");
	}

    public RequestQR getRequestQR(String id) {
		for(RequestQR i: qrList) {
			if(i.getID().equals(id)) {
				return i;
			}
		}
		return null;
	}

	public boolean exists(String id) {
		for(RequestQR i: qrList) {
			if(i.getID().equals(id)) {
				return true;
			}
		}
		return false;
	}
	
	public void addRequestQR(RequestQR currentRequestQR) {
		CatalogoQrCodes.getInstance().qrList.add(currentRequestQR);
		System.out.println(ANSI_CYAN + "CatalogoQrCodes: Adicionado qrCode ao catalogo " + ANSI_RESET);
		INSTANCE.saveToFile();
	}

	public void remReqQR(RequestQR req) {
		CatalogoQrCodes.getInstance().qrList.remove(req);
		INSTANCE.saveToFile();
	}

	public void printctlg() {
		System.out.println(ANSI_CYAN + "Lista de RequestQRs no catalogo: " + ANSI_RESET);
		for (RequestQR req : qrList) {
			System.out.println(ANSI_CYAN + req.toString() + ANSI_RESET);
		}
	}
}
