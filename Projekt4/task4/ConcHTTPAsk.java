import java.net.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import tcpclient.TCPClient;
import java.io.IOException;

public class ConcHTTPAsk
{
    public static void main(String[] args)
    {
        try
        {
            int port = Integer.parseInt(args[0]);
            ServerSocket serverSocket = new ServerSocket(port);
            handleRequests(serverSocket);
        } catch (IOException e) {
            System.err.println("starting Error : " + e.getMessage());
        }
    }

    private static void handleRequests(ServerSocket serverSocket)
    {
        while (true)
        {
            try
            {
                Socket clientSocket = serverSocket.accept();
                Runnable clientHandler = new MyRunnable(clientSocket);
                new Thread(clientHandler).start();
            } catch (IOException e)
            {
                System.err.println("Connection Error : " + e.getMessage());
            }
        }
    }
}
