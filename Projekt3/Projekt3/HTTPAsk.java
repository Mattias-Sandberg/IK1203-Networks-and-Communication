import java.net.*;
import java.io.*;

public class HTTPAsk {
    public static void main(String[] args) throws Exception {
        // Check that the program is called with the correct number of arguments
        if (args.length != 2) {
            System.err.println("Usage: java HTTPAsk <server> <port>");
            System.exit(1);
        }

        // Extract the server and port arguments
        String hostname = args[0];
        int portNum = Integer.parseInt(args[1]);

        // Create the TCPClient object with the desired settings
        TCPClient tcpClient = new TCPClient(true, 5000, 1024);

        // Start listening for incoming connections
        ServerSocket serverSocket = new ServerSocket(portNum);

        // Accept incoming connections and handle each one in a separate thread
        while (true) {
            Socket clientSocket = serverSocket.accept();
            Thread clientThread = new Thread(new ClientHandler(clientSocket, tcpClient));
            clientThread.start();
        }
    }

    private static class ClientHandler implements Runnable {
        private final Socket clientSocket;
        private final TCPClient tcpClient;

        public ClientHandler(Socket clientSocket, TCPClient tcpClient) {
            this.clientSocket = clientSocket;
            this.tcpClient = tcpClient;
        }

        public void run() {
            try {
                // Set up input and output streams
                BufferedReader input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                DataOutputStream output = new DataOutputStream(clientSocket.getOutputStream());

                // Read the HTTP request from the client
                String request = input.readLine();

                // Extract the query parameters from the request
                String[] params = request.split("[?& ]");
                String hostname = null;
                int portNum = 0;
                String command = null;
                String[] arguments = null;
                for (int i = 1; i < params.length; i++) {
                    if (params[i].startsWith("hostname=")) {
                        hostname = params[i].substring(9);
                    } else if (params[i].startsWith("port=")) {
                        portNum = Integer.parseInt(params[i].substring(5));
                    } else if (params[i].startsWith("string=")) {
                        command = params[i].substring(7);
                    } else if (params[i].startsWith("arguments=")) {
                        arguments = params[i].substring(10).split(",");
                    }
                }

                // If any required parameters are missing, send a bad request response
                if (hostname == null || command == null) {
                    output.writeBytes("HTTP/1.1 400 Bad Request\r\n\r\n");
                    clientSocket.close();
                    return;
                }

                // Send the query to the server and receive the response
                String responseStr;
                if (arguments == null) {
                    responseStr = new String(tcpClient.askServer(hostname, portNum, command.getBytes()));
                } else {
                    responseStr = new String(tcpClient.askServer(hostname, portNum, (command + " " + String.join(" ", arguments)).getBytes()));
                }

                // Send the response to the client
                output.writeBytes("HTTP/1.1 200 OK\r\nContent-Type: text/plain\r\nContent-Length: " + responseStr.length() + "\r\n\r\n" + responseStr);
                clientSocket.close();
            } catch (Exception e) {
                System.err.println("Error handling client request: " + e);
            }
        }
    }
}
