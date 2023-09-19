import java.io.*;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class TCPServer {
    public static void main(String[] args) {
        final int PORT = 2048;
        ExecutorService executor = Executors.newFixedThreadPool(10); // Number of threads

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server is running on port " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Cliente conectado: " + clientSocket.getInetAddress().getHostAddress());

                // Create a new thread to handle the client
                Runnable clientHandler = new ClientHandler(clientSocket);
                executor.execute(clientHandler);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class ClientHandler implements Runnable {
    private Socket clientSocket;

    public ClientHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        try (
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)
        ) {
            String clientMessage;
            while ((clientMessage = in.readLine()) != null) {
                System.out.println("Received from client: " + clientMessage);

                if ("Exit".equalsIgnoreCase(clientMessage)) {
                    System.out.println("Client disconnected: " + clientSocket);
                } else if ("Write".equalsIgnoreCase(clientMessage)) {
                    String message = in.readLine();
                    System.out.println("Received message from client: " + message);
                    out.println("Message received.");
                } else if ("Download".equalsIgnoreCase(clientMessage)) {
                    String fileName = in.readLine();
                    System.out.println("Received request to download file: " + fileName);
                    File file = new File(fileName);
                    if (!file.exists()) {
                        out.println("File not found!");
                    } else {
                        out.println("File found!");
                        String hash = calculateSHA256(file);
                        out.println("SHA-256 Hash: " + hash);
                        out.println("File Name: " + file.getName());
                        out.println("File size: " + file.length() + "bytes.");

                        try (BufferedReader fileReader = new BufferedReader((new FileReader(fileName)))) {
                            String line;
                            while ((line = fileReader.readLine()) != null) {
                                out.println(line);
                            }
                        }
                    }
                } else {
                    out.println("Unknown command: " + clientMessage);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                clientSocket.close();
                System.out.println("Client disconnected: " + clientSocket);
            } catch (IOException e) {
                e.printStackTrace();
            }
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
}
