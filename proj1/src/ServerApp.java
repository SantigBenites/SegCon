import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.RecursiveTask;

import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

/**
 * 
 * Esta classe é o 3º e (esperemos) final ponto de execução da aplicação
 * do lado do servidor, implementa o loop infinito que caracteriza a aplicação:
 *  - o servidor recebe um input (o comando) do cliente,
 *  - processa esse comando
 *  - envia de volta uma resposta
 * 
 */
public class ServerApp {

    // cores
    public static final String ANSI_RESET = "\u001B[0m";

    public static final String ANSI_PURPLE_BG = "\u001B[45m";

    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";

    public static final String GREEN_BL = "\033[1;32m";

    // Catalogos
    private uIdService idService = uIdService.getInst();
    private CatalogoUsers catUsers = CatalogoUsers.getInstance();
    private CatalogoGrupos catGrupos = CatalogoGrupos.getInstance();
    private CatalogoQrCodes catQrCodes = CatalogoQrCodes.getInstance();

    // BlockChain
    private BlockChainHandler bch = BlockChainHandler.getInstance();

    private User currentUser;
    private String result = "";
    private Socket ClientSocket;
    
    private String[] args = null;
    private Pair<Transaction, byte[]> tsact = null;

    private ObjectOutputStream outputStrm = null;
    private String workingDir = System.getProperty("user.dir");

    public ServerApp(User CurrentUser, Socket ClientSocket){
        this.currentUser = CurrentUser;
        this.ClientSocket = ClientSocket;
    }

    public void run(ObjectInputStream input, ObjectOutputStream output) throws IOException,ClassNotFoundException{

        outputStrm = output;
        boolean keepAlive = true;

        //System.out.println(CurrentUser.getBalance()); 

        while (keepAlive) {
            String message = "";
            try {
                message = (String)input.readObject();
            } catch (Exception e) {
                //TODO: handle exception
            }
            //String message = (String)input.readObject();
            Print.red("         message: " + message);
            HashMap<String, Runnable> commands = new HashMap<>();

            //necessario verificar se foi introduzido algo ? se calhar se o user só fizer enter maybe ?
            String[] m = message.split(" ");
            String comand = m[0];

            if (comand.equals("takepair")) {
                // receber excepcionalmente este par
                tsact = (Pair<Transaction, byte[]>) input.readObject();
                // retomar fluxo do programa
                message = (String) input.readObject();
                m = message.split(" ");
                comand = m[0];

                if (!(Autentication.validateTransaction(tsact, currentUser))) {
                    comand = "transactionerr";
                }
            }

            if (comand.equals("confQR")) {
                String idReq = (String) input.readObject();

                // se o codigoqr existir enviar os dados ao clt para assinar a transação
                if (catQrCodes.exists(idReq)) {
                    RequestQR req = catQrCodes.getRequestQR(idReq);
                    output.writeObject(req.getRequestor().getName() + " " + req.getAmount());
                    
                    // receber excepcionalmente este par
                    tsact = (Pair<Transaction, byte[]>) input.readObject();
                    // retomar fluxo do programa
                    message = (String) input.readObject();
                    m = message.split(" ");
                    comand = m[0];

                    if (!(Autentication.validateTransaction(tsact, currentUser))) {
                        comand = "transactionerr";
                    }
                } else {
                    output.writeObject("erro-codigo");
                }
            }

            args = Arrays.copyOfRange(m, 1, m.length);

            //System.out.println(Arrays.toString(args));

            commands.put("b", () -> balance());
            commands.put("balance", () -> balance());
            commands.put("m", () -> makepayment());
            commands.put("makepayment", () -> makepayment());
            commands.put("r", () -> requestpayment());
            commands.put("requestpayment", () -> requestpayment());
            commands.put("v", () -> viewrequests());
            commands.put("viewrequests", () -> viewrequests());
            commands.put("p", () -> payrequest());
            commands.put("payrequest", () -> payrequest());
            commands.put("o", () -> obtainQRcode());
            commands.put("obtainQRcode", () -> obtainQRcode());
            commands.put("c", () -> confirmQRcode());
            commands.put("confirmQRcode", () -> confirmQRcode());
            commands.put("n", () -> newgroup());
            commands.put("newgroup", () -> newgroup());
            commands.put("a", () -> addu());
            commands.put("addu", () -> addu());
            commands.put("g", () -> groups());
            commands.put("groups", () -> groups());
            commands.put("d", () -> dividepayment());
            commands.put("dividepayment", () -> dividepayment());
            commands.put("s", () -> statuspayments());
            commands.put("statuspayments", () -> statuspayments());
            commands.put("h", () -> history());
            commands.put("history", () -> history());

            if(message.equals("quit")){keepAlive = false; return;}

            if(commands.containsKey(comand)){
                commands.get(comand).run();
                System.out.println("        feito run");
                output.writeObject(result);
            } else if (comand.equals("transactionerr")) {
                output.writeObject("Transação invalida, assinatura errada!");
            } else {
                output.writeObject(comand + " was unsucessful");
                //TODO add error for invalid message hello
            }

        }

    }

    public void balance(){

        if (args.length != 0) {
            result = wrongArgNumber(0, "balance", ""); return;}

        
        result = String.valueOf(this.currentUser.getBalance());
    }

    /**
     * Tenta fazer um pagamento 
     * 
     * Casos de falha:
     *  - args.length != 2
     *  - args[0] não é um userId valido
     *  - args[1] < currentUser.getbalance()
     */
    public void makepayment(){

        if (args.length != 2) {
            result = wrongArgNumber(2, "makepayment", "<userID> <amount>"); return;}
            
        if (!catUsers.exists(args[0])) {
            result = ANSI_RED + "<userID> invalido, tente novamente\n" + ANSI_RESET; return;}
        
        if (currentUser.equals(catUsers.getUser(args[0]))) {
            result = ANSI_RED + "Está a tentar fazer uma transferencia para sí proprio. Abortado.\n" + ANSI_RESET; return;}

        Double upvalue = 0.0;

        try {
            upvalue = Double.parseDouble(args[1]);
        } catch (NumberFormatException e) {
            result = ANSI_RED + "Valor inserido não é um número, tente novamente.\n" + ANSI_RESET; 
            return;
        }

        if (upvalue <= 0) {
            result = ANSI_RED + "Valor invalido inserido, tente novamente.\n" + ANSI_RESET; return;}

        if (currentUser.getBalance() < upvalue) {
            result = ANSI_RED + "Fundos Insuficientes, tente novamente.\n" + ANSI_RESET; return;}
        
        Print.cyan(tsact.getF().recipient);
        Print.cyan(tsact.getL().toString());
        bch.addToCurrentBlock(tsact);
        
        currentUser.mexeGitos(-upvalue);
        catUsers.getUser(args[0]).mexeGitos(upvalue);
        
        result = ANSI_GREEN + "Transferencia feita com sucesso." + ANSI_RESET;
        catUsers.saveToFile();
    }

    /**
     * Tenta fazer um pagamento 
     * 
     * Casos de falha:
     *  - args.length != 2
     *  - args[0] não é um userId valido
     */
    public void requestpayment(){

        System.out.println(ANSI_RED + "Entrou no requestpayment" + ANSI_RESET);

        if (args.length != 2) {
            result = wrongArgNumber(2, "requestpayment", "<userID> <amount>"); return;
        }
            
        if (!catUsers.exists(args[0])) {
            result = ANSI_RED + "<userID> invalido, tente novamente\n" + ANSI_RESET; return;
        }
        
        Double value = 0.0;

        try {
            value = Double.parseDouble(args[1]);
        } catch (NumberFormatException e) {
            result = ANSI_RED + "Valor inserido não é um número,tente novamente.\n" + ANSI_RESET; 
            return;
        }
    
        if (value <= 0) {
            result = ANSI_RED + "Valor invalido inserido, tente novamente.\n" + ANSI_RESET; return;
        }


        User requestee =  catUsers.getUser(args[0]);
        Request newReq = new Request(value, currentUser, requestee);
        requestee.addRequest(newReq);
        result = "Operacao requestpayment efetuada com sucesso";

        catUsers.saveToFile();
        
    }

    /**
     * Coloca em result uma string que representa os 
     * 
     * Casos de falha:
     *  - args.length != 0
     */
    public void viewrequests(){

        //System.out.println(ANSI_RED + "Entrou no viewrequests" + ANSI_RESET);

        StringBuilder r = new StringBuilder();

        if (args.length != 0) {
            result = wrongArgNumber(0, "viewrequests", ""); return;
        }
        
        Collection<Request> requests = currentUser.getRequests();

        if (requests.size() == 0) {
            result = "Não tem nenhum pedido pendente, tente novamente.\n"; return;
        }

        r.append("Pedidos Pendentes:\n");

        for (Request req : requests) {
            r.append(req.toString());
        }
        

        result = r.toString();
    }

    /**
     * Tenta pagar um request que o current user tenha pendente
     * 
     * Casos de falha:
     *  - args.length != 1
     *  - args[0] não é um dos requestId do current user
     *  - o current user não tem gitos para pagar
     */
    public void payrequest(){
        
        if (args.length != 1) {
            result = wrongArgNumber(1, "payrequest", "<reqID>"); return;
        }

        if (!currentUser.hasRequest(args[0])) {
            result = ANSI_RED + "Request não existe ou não lhe pertence, tente novamente.\n" + ANSI_RESET; return;
        }

        Request r = currentUser.getRequest(args[0]);

        if (currentUser.getBalance() < r.value) {
            result = ANSI_RED + "Falta de fundos, tente novamente.\n" + ANSI_RESET; return;
        }

        currentUser.mexeGitos(-r.value);
        r.requestor.mexeGitos(r.value);

        currentUser.remRequest(r.getId());

        if (r.getGrpReq() != null) {
            GroupRequest grpReq = r.getGrpReq();
            
            grpReq.removerDev(currentUser);
        }

        Print.cyan(tsact.getF().recipient);
        Print.cyan(tsact.getL().toString());
        bch.addToCurrentBlock(tsact);

        result = "Operacao payrequest efetuada com sucesso";

        catGrupos.saveToFile();
        catUsers.saveToFile();
    }

    public void obtainQRcode(){

        System.out.println(ANSI_RED + "         ARGS: " + Arrays.toString(args) + ANSI_RESET);

        if(args.length != 1)
            result = wrongArgNumber(1, "obtainQRcode", "<amount>");

        Double amount = 0.0;

        try {
            amount = Double.parseDouble(args[0]);
            outputStrm.writeObject("good");
        } catch (NumberFormatException | IOException e) {
            try {
                outputStrm.writeObject("bad");
            } catch (Exception e0) {
                e0.printStackTrace();
            }
            result = ANSI_RED + "Valor inserido não é um número, tente novamente.\n" + ANSI_RESET; 
            return;
        }

        
    
        if (amount < 0) {
            result = ANSI_RED + "Valor negativo inserido, tente novamente.\n" + ANSI_RESET; return;
        }

        RequestQR reqQR = new RequestQR(amount, this.currentUser);
        catQrCodes.addRequestQR(reqQR);

        String path = workingDir + "/srvQrCodes/" + reqQR.getID() + ".png";

        try {
            RequestQR.generateQRcode(reqQR.getID(), path, "UTF-8", 200, 200);    
            
            BufferedImage image = ImageIO.read(new File(path));

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ImageIO.write(image, "png", byteArrayOutputStream);

            byte[] size = ByteBuffer.allocate(4).putInt(byteArrayOutputStream.size()).array();
            outputStrm.write(size);
            outputStrm.write(byteArrayOutputStream.toByteArray());

            outputStrm.writeObject(reqQR.getID());

            outputStrm.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    
        System.out.println("QR Code created successfully.");

        catQrCodes.saveToFile();
        catUsers.saveToFile();
        
        result = "Qr code criado com sucesso: " + reqQR.getID();
    }

    /**
     * Remove um qr code e tenta saldar o mesmo
     * 
     * Casos de Falha:
     *  - args.length != 1
     *  - args[0] não é um qrcodeID valido
     * 
     * Casos extra
     *  Caso o current user não tiver balance suficiente para pagar
     *  o qrRequest é retirado na mesma do catalogo, 
     */
    public void confirmQRcode(){

        if (args.length != 1) {
            result = wrongArgNumber(1, "confirmQRcode", "<QRcode>"); return;}
            
        if (!catQrCodes.exists(args[0])) {
            result = ANSI_RED + "<QRcode> invalido, tente novamente\n" + ANSI_RESET; return;}
        
        RequestQR rqr = catQrCodes.getRequestQR(args[0]);
        
        if (rqr.getAmount() <= currentUser.getBalance()) {
            String requestorName = rqr.getRequestor().getName();
            catUsers.getUser(requestorName).mexeGitos(rqr.getAmount());
            currentUser.mexeGitos(-rqr.getAmount());
             
            // se a transação for bem sucedida então coloca na blockchain
            bch.addToCurrentBlock(tsact);
        }

        catQrCodes.remReqQR(rqr);
        
        // Delete file:

        File qrImage = new File(workingDir + "/srvQrCodes/" + rqr.getID() + ".png");
        qrImage.delete();

        result = "QR code pago com sucesso: " + args[0];
        catUsers.saveToFile();
        catQrCodes.saveToFile();
    }

    public void newgroup(){

        if (args.length != 1) {
            result = wrongArgNumber(1, "newgroup", ""); return;
        }

        String id = args[0];

        if(catGrupos.getGroup(id) == null){
            
            Group newgroup = new Group(id,currentUser);
            catGrupos.addGroup(newgroup);
            currentUser.addGroup(newgroup);
            result = "New group successfully created";return;

        }
         
        result = "New group unsuccessfully created";
        

    }


    /**
     * 
     * Espera args[0] um user valido e diferente do owner do
     * args[1] um grupo valido cujo owner é o current user
     */
    public void addu(){

        if (args.length != 2) {
            result = wrongArgNumber(2, "addu", "<userID> <groupID>"); return;
        }

        if (!catUsers.exists(args[0])) {
            result = ANSI_RED + "O user fornecido não existe\n" + ANSI_RESET; return;
        }

        if (!catGrupos.exists(args[1])) {
            result = ANSI_RED + "O grupo fornecido não existe\n" + ANSI_RESET; return;
        }

        if (!catGrupos.getGroup(args[1]).getOwner().equals(currentUser)) {
            result = ANSI_RED + "Não é o dono deste grupo\n" + ANSI_RESET; return;
        }

        if (catGrupos.getGroup(args[1]).getOwner().getName().equals(args[0])) {
            result = ANSI_RED + "Não se pode adicionar a si ao grupo\n" + ANSI_RESET; return;
        }

        String userId = args[0];
        String groupId = args[1];

        User user = catUsers.getUser(userId);
        catGrupos.addUserToGroup(groupId, user);
        user.addGroup(catGrupos.getGroup(groupId));
        result = "User " + userId + " added to group " + groupId;

    }

    public void groups(){

        if (args.length != 0) {
            result = wrongArgNumber(0, "groups", ""); return;
        }

        StringBuilder StrB = new StringBuilder();

        //List of groups of which the user is owner
        List<Group> ownerGroups = catGrupos.getAllOwnerGroups(currentUser.getName());
        if(ownerGroups.size() > 0){
            StrB.append("Groups which the user is owner\n\n");
            for(Group group:ownerGroups){
                StrB.append(group.toString()).append("\n");
            }
            StrB.append("\n");
        }else{
            StrB.append("This user isnt owner of any groups\n\n");
        }

        //List of groups of which the user is member
        List<Group> memberGroups = catGrupos.getAllMemberGroups(currentUser.getName());
        if(memberGroups.size() > 0){
            StrB.append("Groups which the user is member\n\n");
            for(Group group:memberGroups){
                StrB.append(group.toString()).append("\n");
            }
            StrB.append("\n");
        }else{
            StrB.append("This user isnt member of any groups\n\n");
        }

        result = StrB.toString();
        

    }

    /**
     * Divide o pagamento de um valor sobre os membros de um grupo
     * 
     * 
     *  Casos de falha:
     *  - args.length != 2
     *  - args[0] não é um groupId valido
     *  - current user não é o owner do grupo com id args[0]
     *  - args[1] é um double positivo
     * 
     */
    public void dividepayment(){

        if (args.length != 2) {
            result = wrongArgNumber(2, "dividepayment", "<groupID> <amount>"); return;
        }

        if (!catGrupos.exists(args[0])) {
            result = ANSI_RED + "Grupo não existe, tente novamente.\n" + ANSI_RESET; return;
        }

        Group g = catGrupos.getGroup(args[0]);

        if (!g.getOwner().equals(currentUser)) {
            result = ANSI_RED + "Não é o dono deste grupo, tente novamente.\n" + ANSI_RESET; return;
        }

        if (g.getMembers().size() == 0) {
            result = ANSI_RED + "Este grupo encontra-se vazio, tente novamente."; return;
        }

        Double value = 0.0;

        try {
            value = Double.parseDouble(args[1]);
        } catch (NumberFormatException e) {
            result = ANSI_RED + "Valor inserido não é um número, tente novamente.\n" + ANSI_RESET; 
            return;
        }
    
        if (value <= 0) {
            result = ANSI_RED + "Valor invalido inserido, tente novamente.\n" + ANSI_RESET; return;
        }

        result = "Operacao dividepayment efetuada com sucesso";

        g.newGroupReq(value);

        catGrupos.saveToFile();
        catUsers.saveToFile();
        
    }

    /**Apresenta o estado de cada GroupRequest num certo grupo
     * 
     * Casos de falha
     *  - args[0] não é um groupId valido
     *  - current user não é o owner do grupo com id args[0]
     */
    public void statuspayments(){

        if (args.length != 1) {
            result = wrongArgNumber(1, "statuspayments", "<groupID>"); return;
        }

        if (!catGrupos.exists(args[0])) {
            result = ANSI_RED + "Grupo não existe, tente novamente\n" + ANSI_RESET; return;
        }

        Group g = catGrupos.getGroup(args[0]);

        if (!g.getOwner().equals(currentUser)) {
            result = ANSI_RED + "Não é o dono deste grupo, tente novamente.\n" + ANSI_RESET; return;
        }

        if (g.getGrpReqs().size() ==0) {
            result = ANSI_RED + "Este grupo não tem nada a apresentar, tente novamente.\n" + ANSI_RESET; return;
        }

        StringBuilder strB = new StringBuilder();

        strB.append("For group " + g.getId() + " Requests: \n\n");

        for (GroupRequest grpReq : g.getGrpReqs()) {
            strB.append("Request of id " + grpReq.getId() + "\n");
            List<User> dev = grpReq.getDevedores();
            for (User user : dev) {
                strB.append(user.getName() + "\n");
            }
            strB.append("\n");
        }

        result = strB.toString();
    }

    /**
     * 
     * 
     */
    public void history(){
        if (args.length != 1) {
            result = wrongArgNumber(1, "history", "<groupID>"); return;
        }

        if (!catGrupos.exists(args[0])) {
            result = ANSI_RED + "Grupo não existe, tente novamente.\n" + ANSI_RESET; return;
        }

        Group g = catGrupos.getGroup(args[0]);

        if (!g.getOwner().equals(currentUser)) {
            result = ANSI_RED + "Não é o dono deste grupo, tente novamente.\n" + ANSI_RESET; return;
        }

        StringBuilder r = new StringBuilder();

        r.append("Historico de pagamentos do grupo " + g.getId() + "\n");

        for (String[] e : g.getHistory()) {
            r.append("  id: " + e[0] + " - valor: " + e[1] + "\n");
        }

        result = r.toString();


    }

    public String wrongArgNumber(int n, String comandName, String inputs) {
        StringBuilder r = new StringBuilder();
        r.append(ANSI_RED + "Número de argumentos invalido! " + ANSI_RESET);
        r.append("São esperados" + " " + n + " " + "argumentos para este comando na forma: \n");
        r.append(GREEN_BL + comandName.charAt(0) + ANSI_RESET);
        r.append(ANSI_GREEN + comandName.substring(1) + " " + inputs + ANSI_RESET);
        r.append("\n");

        return r.toString();
    }

}