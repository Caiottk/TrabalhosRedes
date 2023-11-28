import java.io.*;
import java.net.*;
import java.util.*;

/*Client to generate a ping requests over UDP */

public class UDPClient {
    private static final int TIMEOUT = 1000; // milliseconds

    public static void main(String[] args) {
        // Get command line argument.
        if (args.length != 2) {
            System.out.println("Required arguments: host port");
            return;
        }
        // Get the port number to access.
        int port = Integer.parseInt(args[1]);
        // Server to ping
        Inet4Address server;
        try {
            server = (Inet4Address) Inet4Address.getByName(args[0]);

            // Create a datagram socket for receiving and sending UDP packets
            DatagramSocket socket = new DatagramSocket();

            int sequence_number = 0;
            while (sequence_number < 10) {

                // Timestamp
                Date date = new Date();
                long time_send = date.getTime();

                // Create string to send, and transfer it to a byte array.
                String str_send = "PING " + sequence_number + " " + time_send + " \r\n";
                byte[] buf = new byte[1024];
                buf = str_send.getBytes();

                // Create a datagram packet to hold incomming UDP packet.
                DatagramPacket request = new DatagramPacket(buf, buf.length, server, port);

                try {
                    // Send the ping request.
                    socket.send(request);
                    // Set a receive timeout, 1000 milliseconds.
                    socket.setSoTimeout(TIMEOUT);

                    // Create a datagram packet to hold incomming UDP packet.
                    DatagramPacket reply = new DatagramPacket(new byte[1024], 1024);

                    // Block until the host receives a UDP packet.
                    socket.receive(reply);

                    date = new Date();
                    long time_receveid = date.getTime();
                    // Print the recieved data.
                    printData(reply, time_receveid - time_send);

                } catch (IOException e) {
                    System.out.println("Timeout for packet " + sequence_number);
                }

                // Sequence number of the last ping packet received.
                sequence_number++;
            }
        } catch (UnknownHostException e) {
            System.out.println("Host not found");
        } catch (SocketException e) {
            System.out.println("Socket error");

        }
    }

    /* Print ping data to the standard output stream. */
    private static void printData(DatagramPacket request, long delayTime) throws IOException {
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
        System.out.println("Received from "
                + request.getAddress().getHostAddress()
                + ": " + new String(line)
                + " Delay: "
                + delayTime
                + "ms");
    }

    private static void ReliableUDPSender(){
        
    }

}