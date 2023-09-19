import java.io.*;
import java.net.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;

public class TCPClient {
    public static void main(String[] args) {
        final String SERVER_IP = "localhost";
        final int SERVER_PORT = 2048;

        try (
                Socket socket = new Socket(SERVER_IP, SERVER_PORT);
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                Scanner sc = new Scanner(System.in);
        ) {
            // Send a message to the server
            String userInput;
            while (true) {
                System.out.println("Enter a command (Exit/Write/Download): ");
                userInput = sc.nextLine();
                out.println(userInput);
                if ("Exit".equalsIgnoreCase(userInput)) {
                    System.out.println("Exiting...");
                    break;
                } else if ("Write".equalsIgnoreCase(userInput)) {
                    System.out.println("Enter a message to send to the server: ");
                    String message = sc.nextLine();
                    out.println(message);
                } else if ("Download".equalsIgnoreCase(userInput)) {
                    String serverResponse;
                    while ((serverResponse = in.readLine()) != null) {
                        if ("File not Found!".equals(serverResponse)) {
                            System.out.println("File not found on the server.");
                            break;
                        } else if ("File found!".equals(serverResponse)) {
                            String hash = in.readLine();
                            String fileName = in.readLine();
                            String fileSize = in.readLine();

                            System.out.println("Received file information: ");
                            System.out.println(hash);
                            System.out.println(fileName);
                            System.out.println(fileSize);

                            String line;
                            try (FileWriter fileWriter = new FileWriter(fileName)) {
                                while ((line = in.readLine()) != null) {
                                    fileWriter.write(line + "\n");
                                }
                            }

                            String calculateHash = calculateSHA256(fileName);
                            System.out.println("Calculated SHA-256 Hash" + calculateHash);

                            break;
                        } else {
                            System.out.println("Server Response: " + serverResponse);
                        }
                    }
                } else {
                    System.out.println("Unknown command: " + userInput);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String calculateSHA256(String fileName) {
        try (FileInputStream fis = new FileInputStream(fileName)) {
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


