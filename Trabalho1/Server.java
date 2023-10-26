import javax.swing.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;


public class Server extends Thread {

    private static ArrayList<BufferedWriter> clientes;
    private static ServerSocket server;
    private String nome;
    private Socket con;
    private InputStream in;
    private InputStreamReader inr;
    private BufferedReader bfr;

    public Server(Socket con) {
        this.con = con;
        try {
            in = con.getInputStream();
            inr = new InputStreamReader(in);
            bfr = new BufferedReader(inr);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {

        try {

            String msg;
            OutputStream ou = this.con.getOutputStream();
            Writer ouw = new OutputStreamWriter(ou);
            BufferedWriter bfw = new BufferedWriter(ouw);
            clientes.add(bfw);
            nome = msg = bfr.readLine();
            System.out.println(msg);
            while (!"Sair".equalsIgnoreCase(msg) && msg != null) {
                if (msg.startsWith("Download")) {
                    String[] parts = msg.split(" ");
                    String requestedFileName = parts[1];
                    if(!sendFileToClient(requestedFileName, bfw)){
                        bfw.write("FileNotFound");
                        bfw.flush();
                        bfw.close();
                    }

                } else {
                    msg = bfr.readLine();
                    sendToAll(bfw, msg);
                    System.out.println(msg);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();

        }
    }

    public void sendToAll(BufferedWriter bwSaida, String msg) {
        BufferedWriter bwS;

        for (BufferedWriter bw : clientes) {
            bwS = (BufferedWriter) bw;
            if (!(bwSaida == bwS)) {
                try {
                    bw.write(nome + " -> " + msg + "\r\n");
                    bw.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    public boolean sendFileToClient(String fileName, BufferedWriter requestingClient) {
        try {
            String filePath = "/home/caio-/UTFPR/UTFPR2023.2/Redes/javaSocket/";
            File file = new File(filePath + fileName);
            if (file.exists() && file.isFile()) {
                String fileInfo = "File found!;" + file.getName() + ";" + file.length() + " bytes;";

                String hash = calculateSHA256(file);
                fileInfo += hash + ";";

                byte[] fileBytes = Files.readAllBytes(file.toPath());
                String fileContent = Base64.getEncoder().encodeToString(fileBytes);

                fileInfo += fileContent;
                requestingClient.write(fileInfo);
                requestingClient.newLine();
                requestingClient.flush();

                requestingClient.write("File sent successfully.");
                requestingClient.newLine();
                requestingClient.flush();
                return true;

            } else {
                System.out.println("File not found on the server.");
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
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

    private String encodeBase64(String in) {
        return Base64.getEncoder().encodeToString(in.getBytes());
    }

    public static void main(String[] args) {

        try {
            //Cria os objetos necessário para instânciar o servidor
            JLabel lblMessage = new JLabel("Porta do Servidor:");
            JTextField txtPorta = new JTextField("12345");
            Object[] texts = {lblMessage, txtPorta};
            JOptionPane.showMessageDialog(null, texts);
            server = new ServerSocket(Integer.parseInt(txtPorta.getText()));
            clientes = new ArrayList<BufferedWriter>();
            JOptionPane.showMessageDialog(null, "Servidor ativo na porta: " +
                    txtPorta.getText());

            while (true) {
                System.out.println("Aguardando conexão...");
                Socket con = server.accept();
                System.out.println("Cliente conectado...");
                Thread t = new Server(con);
                t.start();
            }

        } catch (Exception e) {

            e.printStackTrace();
        }
    }
}


