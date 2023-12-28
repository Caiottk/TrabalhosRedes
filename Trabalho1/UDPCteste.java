import java.io.*;
import java.net.*;
import java.security.MessageDigest;

public class UDPCteste {
    public static void main(String[] args) {
        DatagramSocket clientSocket = null;

        try {
            clientSocket = new DatagramSocket();

            InetAddress serverAddress = InetAddress.getByName("localhost"); // Endereço do servidor
            int serverPort = 9876; // Porta do servidor

            String request = "GET /arquivo.txt";
            byte[] requestData = request.getBytes();

            DatagramPacket requestPacket = new DatagramPacket(requestData, requestData.length, serverAddress, serverPort);
            clientSocket.send(requestPacket);

            byte[] receiveData = new byte[1024];

            // Receber o arquivo
            DatagramPacket filePacket = new DatagramPacket(receiveData, receiveData.length);
            clientSocket.receive(filePacket);
            byte[] fileContent = filePacket.getData();

            // Receber o hash SHA-256
            DatagramPacket hashPacket = new DatagramPacket(receiveData, receiveData.length);
            clientSocket.receive(hashPacket);
            byte[] receivedHash = hashPacket.getData();

            // Calcular o hash do arquivo recebido
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] fileHash = digest.digest(fileContent);

            // Verificar a integridade do arquivo
            boolean integrity = MessageDigest.isEqual(receivedHash, fileHash);
            if (integrity) {
                System.out.println("Arquivo recebido com sucesso.");
                // Aqui você pode fazer o processamento do arquivo
            } else {
                System.out.println("Erro: Arquivo corrompido. Solicitando novamente.");
                // Aqui você pode implementar a lógica para solicitar os pacotes perdidos novamente
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (clientSocket != null) {
                clientSocket.close();
            }
        }
    }
}
