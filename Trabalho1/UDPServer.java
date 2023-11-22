import java.io.*;
import java.net.*;
import java.util.*;
import java.security.*;

/*Server to process ping requests over UDP */

public class UDPServer {
    private static final double LOSS_RATE = 0.3;
    private static final int AVERAGE_DELAY = 100; // milliseconds

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
                byte[] buf = request.getData();
                DatagramPacket reply = new DatagramPacket(buf, buf.length, clientHost, clientPort);
                socket.send(reply);

                System.out.println("   Reply sent.");
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

    public static byte[] calculateChecksum(String filePath) throws IOException, NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        FileInputStream fis = new FileInputStream(filePath);
        byte[] dataBytes = new byte[1024];
        int nread = 0;
        while ((nread = fis.read(dataBytes)) != -1) {
            md.update(dataBytes, 0, nread);
        }
        fis.close();
        return md.digest();
    }

    // https://stackoverflow.com/questions/9655181/how-to-convert-a-byte-array-to-a-hex-string-in-java
    public static String bytesToHex(byte[] bytes) {
        StringBuffer result = new StringBuffer();
        for (byte byt : bytes) result.append(Integer.toString((byt & 0xff) + 0x100, 16).substring(1));
        return result.toString();
    }

    public static void transferData(File file, DatagramSocket socket, InetAddress clientHost, int clientPort) throws IOException {
        FileInputStream fis = new FileInputStream(file);
        byte[] buffer = new byte[1024];
        int nread = 0;
        int numPackets = (int) Math.ceil(file.length() / 1024.0);
        int numPacketsSent = 0;
        while ((nread = fis.read(buffer)) != -1 && numPacketsSent < numPackets) {
            DatagramPacket reply = new DatagramPacket(buffer, buffer.length, clientHost, clientPort);
            socket.send(reply);
            System.out.println("Sent packet " + numPacketsSent++ + reply.getData().toString());
        }
        fis.close();
    }
}