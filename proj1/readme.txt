Projeto 1 - Fase 1: 
    
    Grupo: 16

        Santiago Benites 54392
        Beatriz Rosa 55313
        João Ferreira 55312


Todos os comandos, sejam eles de compilação ou execução devem ser
feitos *sempre* da pasta "proj1"

CUIDADO: correr o servidor e cliente 1x por class antes de empacotar como jar

Por CLASS:

    - Compilar:
        javac -cp ".:./lib/*" -d bin src/*.java

    - Correr Servidor:
        java -cp "lib/*:bin/" Server [porto] pwCifra keystore keystorepw
    ex: java -cp "lib/*:bin/" Server cifrapw server/keystore changeit

    - Correr Cliente:
        java -cp "lib/*:bin/" Trokos 127.0.0.1[:porto] <username> <password>
    ex: java -cp "lib/*:bin/" Trokos 127.0.0.1 client/TrokosPub.truststore client/user1.keystore changeit user1
    ex: java -cp "lib/*:bin/" Trokos 127.0.0.1 client/TrokosPub.truststore client/user2.keystore changeit user2

    - Correr showBlockChain
    ex: java -cp "lib/*:bin/" showBlockChain

Por JAR:

    - Compilar Servidor:
        javac -cp ".:./lib/*" -d bin src/*.java
        jar -cvfm Server.jar Server.mf ./bin ./blockChain ./certificates ./client ./cltQrCodes ./lib ./resources ./server ./srvQrCodes readme.txt
    
    - Compilar Cliente:
        javac -cp ".:./lib/*" -d bin src/*.java
        jar -cvfm Client.jar Client.mf ./bin ./blockChain ./certificates ./client ./cltQrCodes ./lib ./resources ./server ./srvQrCodes readme.txt

    - Correr Servidor:
        java -jar Server.jar <password> <keystore> <keystorepw>
    ex: java -jar Server.jar cifrapw server/keystore changeit
    
    - Correr Cliente:
        java -jar Client.jar 127.0.0.1[:porto] <truststore> <keystore> <keystorepw> <username>
    ex: java -jar Client.jar 127.0.0.1 client/TrokosPub.truststore client/user1.keystore changeit user1
