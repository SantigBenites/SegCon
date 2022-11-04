import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.io.Serializable;

/**
 * uIdService
 * 
 * O trabalho desta classe é fornecer id's unicos para os requests
 * os requests são os unicos objetos que têm um id que não é fornecido pelo user,
 * e os id's dos mesmos server para a sua identificação, por isso têm de ser
 * unicos.
 * 
 * Dado que threads diferentes não podem gerar id's iguais, esta classe tem
 * de ser implementada como um singleton
 */
public class uIdService implements Serializable{

    private static uIdService INSTANCE = null;

    public static uIdService getInst() {
        if(INSTANCE == null) {
			INSTANCE =  new uIdService();
		}
		return INSTANCE;
    }

    public String createReqID(){
        String strUUID = UUID.randomUUID().toString().replace("-", "");
        String s = strUUID.substring(strUUID.length()-8).toUpperCase();
        return s;
    }

    public String createQrID(){
        String strUUID = UUID.randomUUID().toString().replace("-", "");
        String s = strUUID.substring(strUUID.length()-8).toUpperCase();
        return s;
    }

    public String createGrpReqID(){
        String strUUID = UUID.randomUUID().toString().replace("-", "");
        String s = strUUID.substring(strUUID.length()-8).toUpperCase();
        return s;
    }

}