import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public final class WebServer {
    private static final AtomicBoolean running = new AtomicBoolean(false);
    private static final int PORT = 8080;
    private static ServerSocket server;

    public static void main(String[] args) {
        try {
            start();
        } catch (Exception e) {
            System.out.println("Error while starting the server.\n" + e);
        }
    }

    public static void start() throws Exception {
        // Establish the listen socket.
        server = new ServerSocket(PORT);
        running.set(true);
        System.out.println("Server started.\nListening for connections on port : " + PORT + " ...\n");
        // Process HTTP service requests in an infinite loop.
        while (true) {
            // Listen for a TCP connection request.
            Socket connection = server.accept();
            // Construct an object to process the HTTP request message.
            HttpRequest request = new HttpRequest(connection);

            request.run();
            

        }
    }
}

final class HttpRequest implements Runnable {
    private static final String CRLF = "\r\n";
    private Socket socket;
    private InputStream input;
    private OutputStream output;
    private StringTokenizer parse;

    public HttpRequest(Socket socket) throws Exception {
        this.socket = socket;
        this.input = socket.getInputStream();
        this.output = socket.getOutputStream();

    }

    public void run() {
        try {
            processRequest();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    private void processRequest() throws Exception {

        InputStream is = socket.getInputStream();
        DataOutputStream os = new DataOutputStream(socket.getOutputStream());
        // Get a reference to the socket's input and output streams.
        // Set up input stream filters.
        BufferedReader br = new BufferedReader(new InputStreamReader(input));
        // Get the request line of the HTTP request message.
        String requestLine = br.readLine();
        System.out.println("\n" + requestLine );

        String headerLine = null;
        while((headerLine = br.readLine()).length() != 0){
            System.out.println(headerLine);
        }

        // Extract the filename from the request line.
        parse = new StringTokenizer(requestLine);
        parse.nextToken(); // skip over the method, which should be "GET"
        String fileName = parse.nextToken();
        // Prepend a "." so that file request is within the current directory.
        fileName = "." + fileName;
        // Open the requested file.
        FileInputStream fis = null;
        boolean fileExists = true;
        try {
            fis = new FileInputStream(fileName);
        } catch (FileNotFoundException e) {
            fileExists = false;
        }
        // Construct the response message.
        String statusLine = null;
        String contentTypeLine = null;
        String entityBody = null;
        if (fileExists) {
            statusLine = "HTTP/1.1 200 OK" + CRLF;
            contentTypeLine = "Content-type: " + contentType(fileName) + CRLF;
        } else {
            statusLine = "HTTP/1.1 404 Not Found" + CRLF;
            contentTypeLine = "Content-type: /resources/html/home.html" + CRLF;
            entityBody = "<HTML>" + "<HEAD><TITLE>Not Found</TITLE></HEAD>" + "<BODY>Not Found</BODY></HTML>";
        }
        // Send the status line.
        output.write(statusLine.getBytes());
        // Send the content type line.
        output.write(contentTypeLine.getBytes());
        // Send a blank line to indicate the end of the header lines.
        output.write(CRLF.getBytes());
        // Send the entity body.
        if (fileExists) {
            sendBytes(fis, output);
            fis.close();
        } else {
            output.write(entityBody.getBytes());
        }
        // Close streams and socket.
        output.close();
        br.close();
        socket.close();
    }

    private void sendBytes(FileInputStream fis, OutputStream os) throws Exception {
        // Construct a 1K buffer to hold bytes on their way to the socket.
        byte[] buffer = new byte[1024];
        int bytes = 0;
        // Copy requested file into the socket's output stream.
        while ((bytes = fis.read(buffer)) != -1) {
            os.write(buffer, 0, bytes);
        }
    }

    private static String contentType(String fileName) throws Exception {
        if (fileName.endsWith(".htm") || fileName.endsWith(".html")) {
            return "text/html";
        }
        if (fileName.endsWith(".gif")) {
            return "image/gif";
        }
        if (fileName.endsWith(".jpeg")) {
            return "image/jpeg";
        }
        return "application/octet-stream";
    }
}
