import java.io.*;
import java.net.*;
import java.util.*;

import javax.xml.crypto.Data;

import java.security.*;

/*Server to process ping requests over UDP */

public class UDPServer {
    private static final double LOSS_RATE = 0.3;
    private static final int AVERAGE_DELAY = 100; // milliseconds
    private static final int BUFFER_SIZE = 1024; // buffer size for packet transfer

    public static void main(String[] args) {
        // Get command line argument.
        if (args.length != 1) {
            System.out.println("Required arguments: port");
            return;
        }
        int port = Integer.parseInt(args[0]);

        try {

            // Construct a random number generator for use in simulating
            // packet loss and network delay.
            Random random = new Random();

            // Create a datagram socket for receiving and sending UDP packets
            // through the port specified on the command line.
            DatagramSocket socket = new DatagramSocket(port);

            // Processing loop.
            while (true) {
                // Create a datagram packet to hold incomming UDP packet.
                DatagramPacket request = new DatagramPacket(new byte[1024], 1024);

                // Block until the host receives a UDP packet.
                socket.receive(request);

                // Print the recieved data.
                printData(request);

                // Decide whether to reply, or simulate packet loss.
                if (random.nextDouble() < LOSS_RATE) {
                    System.out.println("   Reply not sent.");
                    continue;
                }

                // Simulate network delay.
                Thread.sleep((int) (random.nextDouble() * 2 * AVERAGE_DELAY));

                // Send reply.
                InetAddress clientHost = request.getAddress();
                int clientPort = request.getPort();

                // Extract file name from request, message format: "GET <file_name>"
                String requestMessage = new String(request.getData()).trim();
                if (requestMessage.startsWith("GET")) {
                    String[] requestParts = requestMessage.split(" ");
                    if (requestParts.length == 2) {
                        String filePath = requestParts[1].substring(1); // remove the first '/' character
                        File file = new File(filePath);

                        if (file.exists()) {
                            // Send file in chunks of 1024 bytes
                            transferData(file, socket, clientHost, clientPort);
                        } else {
                            // Send error message
                            String errorMessage = "File not found";
                            DatagramPacket errorReply = new DatagramPacket(errorMessage.getBytes(),
                                    errorMessage.length(), clientHost, clientPort);
                            socket.send(errorReply);
                            System.out.println("Reply sent.");
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    /* Print ping data to the standard output stream */
    private static void printData(DatagramPacket request) throws Exception {
        // Obtain references to the packet's array of bytes.
        byte[] buf = request.getData();

        // Wrap the bytes in a byte array input stream,
        // so that you can read the data as a stream of bytes.
        ByteArrayInputStream bais = new ByteArrayInputStream(buf);

        // Wrap the byte array output stream in an input stream reader,
        // so you can read the data as a stream of characters.
        InputStreamReader isr = new InputStreamReader(bais);

        // Wrap the input stream reader in a bufferred reader,
        // so you can read the character data a line at a time.
        // (A line is a sequence of chars terminated by any combination of \r and \n.)
        BufferedReader br = new BufferedReader(isr);

        // The message data is contained in a single line, so read this line.
        String line = br.readLine();

        // Print host address and data received from it.
        System.out.println(
                "Received from " +
                        request.getAddress().getHostAddress() +
                        ": " +
                        new String(line));
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

    private String encodeBase64(String in) {
        return Base64.getEncoder().encodeToString(in.getBytes());
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