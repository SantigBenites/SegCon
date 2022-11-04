# Projeto 1 - Fase 1: 
    
## Grupo 16

1. Santiago Benites 54392
2. Beatriz Rosa     55313
3. João Ferreira    55312

**Todos os comandos, sejam eles de compilação ou execução devem ser
feitos *sempre* da pasta "proj1"**

## CLASS:

    - Compilar:
        javac -cp ".:./lib/*" -d bin src/*.java

    - Correr Servidor:
        java -cp "lib/*:bin/" Server [porto]

    - Correr Cliente:
        java -cp "lib/*:bin/" Trokos 127.0.0.1[:porto] <username> <password>

## JAR:

    - Compilar Servidor:
        javac -cp ".:./lib/*" -d bin src/*.java
        jar -cvfm Server.jar Server.mf ./bin ./lib ./cltQrCodes ./srvQrCodes ./resources
    
    - Compilar Cliente:
        javac -cp ".:./lib/*" -d bin src/*.java
        jar -cvfm Client.jar Client.mf ./bin ./lib ./cltQrCodes ./srvQrCodes ./resources

    - Correr Servidor:
        java -jar Server.jar
    
    - Correr Cliente:
        java -jar Client.jar 127.0.0.1[:porto] <username> <password>