import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.security.MessageDigest;

public class UDPSteste {
    public static void main(String[] args) {
        DatagramSocket serverSocket = null;

        try {
            serverSocket = new DatagramSocket(9876); // Porta do servidor

            byte[] receiveData = new byte[1024];

            while (true) {
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                serverSocket.receive(receivePacket);

                String clientRequest = new String(receivePacket.getData(), 0, receivePacket.getLength());
                if (clientRequest.trim().equals("GET /arquivo.txt")) {
                    // Carregar o arquivo
                    File file = new File("lore.txt");
                    byte[] fileContent = Files.readAllBytes(file.toPath());

                    // Calcular o hash SHA-256 do arquivo
                    MessageDigest digest = MessageDigest.getInstance("SHA-256");
                    byte[] hash = digest.digest(fileContent);

                    InetAddress clientAddress = receivePacket.getAddress();
                    int clientPort = receivePacket.getPort();

                    // Enviar o arquivo como resposta
                    DatagramPacket sendPacket = new DatagramPacket(fileContent, fileContent.length, clientAddress, clientPort);
                    serverSocket.send(sendPacket);

                    // Enviar o hash SHA-256
                    DatagramPacket hashPacket = new DatagramPacket(hash, hash.length, clientAddress, clientPort);
                    serverSocket.send(hashPacket);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (serverSocket != null) {
                serverSocket.close();
            }
        }
    }
}
