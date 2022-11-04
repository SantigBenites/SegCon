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

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

import javax.imageio.ImageIO;

/*
Request
tem um qrcode
currUser
*/
public class RequestQR implements Serializable{

    private String id;
    private User requestor;
    private Double amount;
    private uIdService idService = uIdService.getInst();

    public RequestQR(Double amount, User requestor) {
        this.amount = amount;
        this.requestor = requestor;

        this.id = idService.createQrID();

    }

    public String getID() {
        return id;
    }
     
    public User getRequestor() {
        return requestor;
    }

    public Double getAmount() {
        return amount;
    }

    @Override
    public String toString(){
        return this.requestor.getName() + " " + this.getID() + " " + this.getAmount();
    }

    public static void generateQRcode(String data, String path, String charset, int h, int w) throws WriterException, IOException  {  

        /*
        Map<EncodeHintType, ErrorCorrectionLevel> map = new HashMap<EncodeHintType, ErrorCorrectionLevel>();  
        //generates QR code with Low level(L) error correction capability  
        map.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
        */

        //the BitMatrix class represents the 2D matrix of bits  
        //MultiFormatWriter is a factory class that finds the appropriate Writer subclass for the BarcodeFormat requested and encodes the barcode with the supplied contents.  
        BitMatrix matrix = new MultiFormatWriter().encode(new String(data.getBytes(charset), charset), BarcodeFormat.QR_CODE, w, h);  
        MatrixToImageWriter.writeToFile(matrix, path.substring(path.lastIndexOf('.') + 1), new File(path));  
    }  
}
