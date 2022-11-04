/*
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;  
import java.util.HashMap;  
import java.util.Map;

import javax.imageio.ImageIO;
import javax.naming.spi.DirStateFactory.Result;

import com.google.zxing.LuminanceSource;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.MultiFormatWriter;  
import com.google.zxing.NotFoundException;  
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.client.j2se.MatrixToImageWriter;  
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;  



public class QRCode  {  

    private String workingDir = System.getProperty("user.dir");
    private String clientId;// criou a request e vai receber o money
    private int amount;
    private String qrcode;


    public QRCode(String clientId, int amount, String qrcode) {
        this.clientId = clientId;
        this.amount= amount;
        this.qrcode=qrcode;
    }

//static function that creates QR Code  
	public static void generateQRcode(String data, String path, String charset, Map map, int h, int w) throws WriterException, IOException  {  
		//the BitMatrix class represents the 2D matrix of bits  
		//MultiFormatWriter is a factory class that finds the appropriate Writer subclass for the BarcodeFormat requested and encodes the barcode with the supplied contents.  
		BitMatrix matrix = new MultiFormatWriter().encode(new String(data.getBytes(charset), charset), BarcodeFormat.QR_CODE, w, h);  
		MatrixToImageWriter.writeToFile(matrix, path.substring(path.lastIndexOf('.') + 1), new File(path));  
	}  
//main() method  
    public static void main(String args[]) throws WriterException, IOException, NotFoundException  {  
        //data that we want to store in the QR code  
        String str= "THE HABIT OF PERSISTENCE IS THE HABIT OF VICTORY.";  
        //path where we want to get QR Code  
        String path = System.getProperty("user.dir") + "/src/qrcode.png";  
        //Encoding charset to be used  
        String charset = "UTF-8";  
        Map<EncodeHintType, ErrorCorrectionLevel> hashMap = new HashMap<EncodeHintType, ErrorCorrectionLevel>();  
        //generates QR code with Low level(L) error correction capability  
        hashMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);  
        //invoking the user-defined method that creates the QR code  
        generateQRcode(str, path, charset, hashMap, 200, 200);//increase or decrease height and width accodingly   
        //prints if the QR code is generated   
        System.out.println("QR Code created successfully.");  


 

        //-------------------------------------------testing reading
        Map<EncodeHintType, ErrorCorrectionLevel> hintMap = new HashMap<EncodeHintType, ErrorCorrectionLevel>();  
        //generates QR code with Low level(L) error correction capability  
        hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);  
        System.out.println("Data stored in the QR Code is: \n"+ readQRcode(path, charset, hintMap));  
         
    }  



//user-defined method that reads the QR code  
	public static String readQRcode(String path, String charset, Map map) throws FileNotFoundException, IOException, NotFoundException  {  
		BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(new BufferedImageLuminanceSource(ImageIO.read(new FileInputStream(path)))));  
		com.google.zxing.Result rslt = new MultiFormatReader().decode(binaryBitmap);  
		return ((com.google.zxing.Result) rslt).getText();  
	}  



} 

*/