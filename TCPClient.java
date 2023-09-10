import java.io.*;
import java.net.*;

public class TCPClient {
    public static void main(String[] args) {
        final String SERVER_IP = "localhost";
        final int SERVER_PORT = 2048;

        try (
                Socket socket = new Socket(SERVER_IP, SERVER_PORT);
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))
        ) {
            // Send a message to the server
            String message = "Hello, Server!";
            out.println(message);

            // Receive and print the server's response
            String serverResponse = in.readLine();
            System.out.println("Server says: " + serverResponse);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
