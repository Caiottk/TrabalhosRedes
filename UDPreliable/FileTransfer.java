package UDPreliable;

import java.io.*;
import java.net.*;

public class FileTransfer {
    public static void main(String[] args){
        try{
            UDPServerSocket server = new UDPServerSocket(8080);
            server.setSafeMode(true);
            server.addUDPListener(new UDPServerSocket.UDPListener(){
                @Override
                public void accept(UDPSocket socket){
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try{
                                DataInputStream in = new DataInputStream(socket.getInputStream());

                                FileOutputStream outc = new FileOutputStream(new File("/home/caio-/UTFPR/UTFPR2023.2/Redes/javaSocket/UDPreliable/celeste.png"));

                                byte[] buffer = new byte[4096];
                                int length;
                                int packageNumber = 0;

                                int lostPackage = getLostPackage(); // Obtém o número do pacote a ser perdido

                                while ((length = in.read(buffer)) >= 0) {
                                    // Verifica se o pacote atual é o pacote a ser perdido
                                    if (packageNumber == lostPackage) {
                                        // Inverte os dados no buffer
                                        for (int i = 0; i < length / 2; i++) {
                                            byte temp = buffer[i];
                                            buffer[i] = buffer[length - i - 1];
                                            buffer[length - i - 1] = temp;
                                        }
                                    }
                                    outc.write(buffer, 0, length);
                                    packageNumber++;
                                }

                                outc.flush();
                                outc.close();

                            }catch(IOException e){
                                e.printStackTrace();
                            }finally{
                                socket.close();
                                server.close();
                            }
                        }
                    }).start();

                }
            });

            Thread.sleep(1000);

            client();

        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public static void client()throws Exception {
        UDPServerSocket client = new UDPServerSocket(8070);
        client.setSafeMode(true);
        UDPSocket c = client.create(InetAddress.getLocalHost(), 8080);

        DataOutputStream out = new DataOutputStream(c.getOutputStream());

        FileInputStream inc = new FileInputStream(new File("/home/caio-/UTFPR/UTFPR2023.2/Redes/javaSocket/UDPreliable/celeste.png"));

        byte[] buffer = new byte[4096];
        int length;
        int packageNumber = 0;
        while((length = inc.read(buffer)) >= 0){
            out.write(buffer, 0, length);
        }

        inc.close();
        out.flush();

        client.close();
    }

    // Método para receber o número de pacote a ser perdido
    public static int getLostPackage() {
        int lostPackage = 0;
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
            System.out.println("Digite o número do pacote a ser perdido: ");
            lostPackage = Integer.parseInt(in.readLine());
        } catch (Exception e) {
            System.out.println("Erro ao ler o número do pacote a ser perdido");
        }
        return lostPackage;
    }



}
