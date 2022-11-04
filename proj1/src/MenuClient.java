import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.SignedObject;
import java.util.Arrays;
import java.util.Scanner;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.awt.Desktop;
import javax.imageio.ImageIO;

public class MenuClient {

    Socket socket;
    String User;
    Signature sig;

    private String workingDir = System.getProperty("user.dir");

    public MenuClient(Socket ssoc, String user, Signature sig){
        this.socket = ssoc;
        this.User = user;
        this.sig = sig;
    }

    public void run(ObjectInputStream input, ObjectOutputStream output, Scanner scan) throws IOException, ClassNotFoundException, SignatureException{

        boolean keepAlive = true;
        //Scanner scan = new Scanner(System.in);
        // Possible error in nexy line
        while ((keepAlive)){

            printCommandMessage();

            System.out.println("Introduza o seu comando : ");

            String message = scan.nextLine();

            String[] command_split = message.split(" "); 
            String   command_tag   = command_split[0]; 

            if (!(command_tag.equals("o") || command_tag.equals("obtainQRcode"))) {
                // caso não seja um qrcode
                
                if (command_tag.equals("m") || command_tag.equals("makepayment")) {   
                    // caso seja makepayment
                    
                    output.writeObject("takepair");
                    
                    Transaction t = new Transaction(User, command_split[1], Double.parseDouble(command_split[2]));


                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    ObjectOutputStream blockOOS = new ObjectOutputStream(bos);
                    blockOOS.writeObject(t);
                    blockOOS.flush();

                    byte [] data = bos.toByteArray();

                    sig.update(data);

                    Pair<Transaction, byte[]> p = new Pair<>(t, sig.sign());
                    
                    // escrever a transação e assinatura da mesma
                    output.writeObject(p);
                    // escrever o comando novamente
                    output.writeObject(message);

                    //servidor

                    // resposta
                    String fromServer = (String) input.readObject();
                    if(message.equals("quit")){keepAlive = false; return;}
                    System.out.println(fromServer);
                
                } else if (command_tag.equals("p") || command_tag.equals("payrequest")) {
                    // caso seja payrequest
                    // pedimos os requests correntes
                    output.writeObject("v");

                    // servidor responde
                    String query = (String) input.readObject();

                    if (query.equals("Não tem nenhum pedido pendente, tente novamente.")) {
                        // se não tiver requests corrents, não faz mais nada
                        System.out.println(query);
                    } else {
                        // se tiver, vamos encontralo e extrair os dados pretendidos
                        String[] requests = query.split("\n");
                        String request = Arrays.stream(requests)
                                        .filter(s -> s.contains(command_split[1]))
                                        .collect(Collectors.toList())
                                        .get(0);
                        String[] reqComp = request.split(" ");

                        //criamos a transação e assinamos:
                        Transaction t = new Transaction(User, reqComp[5], Double.parseDouble(reqComp[3]));
                        ByteArrayOutputStream bos = new ByteArrayOutputStream();
                        ObjectOutputStream blockOOS = new ObjectOutputStream(bos);
                        blockOOS.writeObject(t);
                        blockOOS.flush();

                        byte [] data = bos.toByteArray();

                        sig.update(data);

                        Pair<Transaction, byte[]> p = new Pair<>(t, sig.sign());
                        // enviamos o par transação - transação assinada
                        // apenas depois de indicarmos que vamos entregar uma transação
                        output.writeObject("takepair");
                        output.writeObject(p);

                        // retomar o fluxo normal do programa
                        output.writeObject(message);

                        //servidor

                        // resposta
                        String fromServer = (String) input.readObject();
                        if(message.equals("quit")){keepAlive = false; return;}
                        System.out.println(fromServer);
                    }
                } else if (command_tag.equals("c") || command_tag.equals("confirmQRcode")) {
                    // assinala que precisa dos dados e entrega o id do request
                    output.writeObject("confQR");
                    output.writeObject(command_split[1]); //id do reqQR

                    // espera pela resposta na forma "user amount"
                    String data = (String) input.readObject();
                    
                    if (!data.equals("erro-codigo")) {
                        String[] userAmount = data.split(" ");

                        Transaction t = new Transaction(User, userAmount[0], Double.parseDouble(userAmount[1]));
                        ByteArrayOutputStream bos = new ByteArrayOutputStream();
                        ObjectOutputStream blockOOS = new ObjectOutputStream(bos);
                        blockOOS.writeObject(t);
                        blockOOS.flush();

                        byte [] yes = bos.toByteArray();

                        sig.update(yes);

                        Pair<Transaction, byte[]> p = new Pair<>(t, sig.sign());

                        // envia o par ao serv
                        output.writeObject(p);

                        // retomar o fluxo normal do programa
                        output.writeObject(message);

                        //servidor

                        // resposta
                        String fromServer = (String) input.readObject();
                        if(message.equals("quit")){keepAlive = false; return;}
                        System.out.println(fromServer);
                    } else {
                        Print.error("Indicou um codigoQR invalido");
                    }
                } else {
                    // qualquer outro comando
                    output.writeObject(message);
                    //Server occurs here
                    String fromServer = (String) input.readObject();
                    if(message.equals("quit")){keepAlive = false; return;}
                    System.out.println(fromServer);
                }

                
            
            } else {

                output.writeObject(message);
                System.out.println("sent: " + message);
                
                //Server occurs here

                //Order of messages sent are images size, image byte array and image name

                String flag = (String) input.readObject();

                if (flag.equals("good")) {
        
                    byte[] sizeAr = new byte[4];
                    input.read(sizeAr);
                    int size = ByteBuffer.wrap(sizeAr).asIntBuffer().get();

                    byte[] imageAr = new byte[size];
                    input.read(imageAr);

                    BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageAr));

                    String fileName = (String) input.readObject();

                    ImageIO.write(image, "png", new File(workingDir + "/cltQrCodes/" + fileName + ".png"));

                    Desktop.getDesktop().open(new File(workingDir + "/cltQrCodes/" + fileName + ".png"));
            
                }
                
                String fromServer = (String) input.readObject();
                System.out.println(fromServer);
                
            }
            
            
        }
        //scan.close();
    }

    private byte[] convertToBytes(Object object) throws IOException {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutputStream out = new ObjectOutputStream(bos)) {
            out.writeObject(object);
            return bos.toByteArray();
        } 
    }

    private static void printCommandMessage() {
        String s =
        Color.WHITE_BOLD + "Comandos disponiveis:" + Color.RESET + "\n" +
        Color.WHITE_BOLD + "[" + "b" + "]" + Color.RESET + "alance" + "\n" +
        Color.WHITE_BOLD + "[" + "m" + "]" + Color.RESET + "akepayment " + "<userID> <amount>" + "\n" +
        Color.WHITE_BOLD + "[" + "r" + "]" + Color.RESET + "equestpayment " + "<userID> <amount>" + "\n" +
        Color.WHITE_BOLD + "[" + "v" + "]" + Color.RESET + "iewrequests" + "\n" +
        Color.WHITE_BOLD + "[" + "p" + "]" + Color.RESET + "ayrequest " + "<reqID>" + "\n" +
        Color.WHITE_BOLD + "[" + "o" + "]" + Color.RESET + "btainQRcode " + "<amount>" + "\n" +
        Color.WHITE_BOLD + "[" + "c" + "]" + Color.RESET + "onfirmQRcode " + "<QRcode>" + "\n" +
        Color.WHITE_BOLD + "[" + "n" + "]" + Color.RESET + "ewgroup " + "<groupID>" + "\n" +
        Color.WHITE_BOLD + "[" + "a" + "]" + Color.RESET + "ddu " + "<userID> <groupID>" + "\n" +
        Color.WHITE_BOLD + "[" + "g" + "]" + Color.RESET + "roups " + "" + "\n" +
        Color.WHITE_BOLD + "[" + "d" + "]" + Color.RESET + "ividepayment " + "<groupID> <amount>" + "\n" +
        Color.WHITE_BOLD + "[" + "s" + "]" + Color.RESET + "tatuspayments " + "<groupID>" + "\n" +
        Color.WHITE_BOLD + "[" + "h" + "]" + Color.RESET + "istory " + "<groupID>" + "\n";

        System.out.println(s);
    }
    
}
