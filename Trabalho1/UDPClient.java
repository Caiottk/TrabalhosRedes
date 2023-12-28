import java.io.*;
import java.net.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/*Client to generate a ping requests over UDP */

public class UDPClient {
    private static final int TIMEOUT = 1000; // milliseconds
    private static final int BUFFER_SIZE = 1024; // buffer size for packet transfer

    private static String host;
    private static int port;
    private static String fileName;
    private static Inet4Address server;
    private static byte[] originalChecksum;

    public static void main(String[] args) {
        // Get command line argument.
        if (args.length != 3) {
            System.out.println("Required arguments: host port file");
            return;
        }

        // Get the host name to access.
        host = args[0];
        // Get the port number to access.
        port = Integer.parseInt(args[1]);
        // Get the file name to access.
        fileName = args[2];

        try {
            server = (Inet4Address) Inet4Address.getByName(host);

            // Create a datagram socket for receiving and sending UDP packets
            DatagramSocket socket = new DatagramSocket();

            // Request file
            requestFile(socket, server, port);
            // Receive file
            receiveFile(socket);

        } catch (UnknownHostException e) {
            System.out.println("Host not found: " + host);
        } catch (SocketException e) {
            System.out.println("Socket error: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("IO error: " + e.getMessage());
        }
    }

    /* Send a request to the server to get the file */
    private static void requestFile(DatagramSocket socket, Inet4Address server, int port)
            throws IOException {
        // Create a string with the request message.
        String requestMessage = "GET /" + fileName;
        // Convert the message to bytes and copy it to the datagram packet.
        byte[] requestData = requestMessage.getBytes();
        // Create a datagram packet to send as an UDP packet.
        DatagramPacket requestPacket = new DatagramPacket(requestData, requestData.length, server, port);
        // Convert the message to bytes and copy it to the datagram packet.
        socket.send(requestPacket);
    }

    /* Receive the file from the server */
    private static void receiveFile(DatagramSocket socket) throws IOException {
        FileOutputStream fos = new FileOutputStream(fileName);
        byte[] buffer = new byte[BUFFER_SIZE];
        
        Set<Integer> missingPackets = new HashSet<Integer>();
        boolean fileReceived = false;

        while(fileReceived){
            // Create a datagram packet to hold incomming UDP packet.
            DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
            // Block until the host receives a UDP packet.
            socket.receive(reply);

            // Simulate packet loss. 
            if(new Random().nextInt(3) == 0){
                System.out.println("Packet loss simulated.");
                missingPackets.add(missingPackets.size() + 1);
                continue;
            }

            
            int packetNumber = missingPackets.size() + 1;

            if(!missingPackets.contains(packetNumber)){
                // Write the received bytes to the file.
                fos.write(reply.getData(), 0, reply.getLength());
            }

            missingPackets.remove(packetNumber);
            // Check if the last packet was received
            if(reply.getLength() < BUFFER_SIZE){
                fileReceived = true;
            }
        }

        fos.close();

        // Checksum verification
        if(verifyChecksum(fileName)){
            System.out.println("File received successfully.");
            // To do : open the file
        } else {
            System.out.println("File transfer unsucessful. Requesting missing packets.");
            // Request missing packets
            requestMissingPackets(socket, missingPackets);
        }
    }

    /* Request missing packets */
    private static void requestMissingPackets(DatagramSocket socket, Set<Integer> missingPackets) throws IOException {
        for(Integer packetNumber : missingPackets){
            // Create a string with the request message.
            String requestMessage = "GET /" + fileName + " PacketNumber " + packetNumber;
            // Convert the message to bytes and copy it to the datagram packet.
            byte[] requestData = requestMessage.getBytes();
            // Create a datagram packet to send as an UDP packet.
            DatagramPacket requestPacket = new DatagramPacket(requestData, requestData.length, server, port);
            // Convert the message to bytes and copy it to the datagram packet.
            socket.send(requestPacket);
        }
    }

    private String calculateSHA256(File file) {
        try (FileInputStream fis = new FileInputStream(file)) {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                md.update(buffer, 0, bytesRead);
            }
            byte[] hashBytes = md.digest();
            StringBuilder hashStringBuilder = new StringBuilder();
            for (byte hashByte : hashBytes) {
                hashStringBuilder.append(String.format("%02x", 0xFF & hashByte));
            }
            return hashStringBuilder.toString();
        } catch (IOException | NoSuchAlgorithmException e) {
            e.printStackTrace();
            return "Error calculating hash";
        }
    }

    private byte[] decodeBase64(String in) {
        return Base64.getDecoder().decode(in);
    }

    public static void transferData(File file, DatagramSocket socket, InetAddress clientHost, int clientPort)
            throws IOException {
        FileInputStream fis = new FileInputStream(file);
        byte[] buffer = new byte[BUFFER_SIZE];
        int numPackets = (int) Math.ceil(file.length() / (double) BUFFER_SIZE);
        int numPacketsSent = 0;
        while (numPacketsSent < numPackets) {
            int bytesRead = fis.read(buffer);
            if (bytesRead == -1)
                break;

            DatagramPacket reply = new DatagramPacket(buffer, bytesRead, clientHost, clientPort);
            socket.send(reply);
            System.out.println("Sent packet " + numPacketsSent++ + "/" + numPackets);
            numPacketsSent++;
        }
        fis.close();
    }

}