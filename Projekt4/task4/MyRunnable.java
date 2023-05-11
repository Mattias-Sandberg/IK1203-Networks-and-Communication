import java.net.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import tcpclient.TCPClient;

public class MyRunnable implements Runnable
{
    Socket socket;

public MyRunnable(Socket socket)
{
    this.socket = socket;
}
    @Override
    public void run()
    {
        //Jag deklarerar varibler
        Integer timeout = null;
        Boolean shutdown = false;
        int port = 0;
        String hostname = null;
        byte[] inp = new byte[0];
        Integer limit = null;
        //Jag anvander en try -catch
        try
        {
            while (true)
            {
                byte[] buffer = new byte[1024];

                socket.getInputStream().read(buffer);
                OutputStream output = socket.getOutputStream();

                String url = new String(buffer, StandardCharsets.UTF_8);
                String[] urlArray = new String[url.length()];

                StringBuilder stringBuild = new StringBuilder();

                urlArray = url.replaceAll("[?=&\\r\\n]", " ").split("\\s+");

                // Om url inte inehaller ask -> 404 Not Found
                if (!url.contains("ask"))
                {
                    String notFoundResponse = "HTTP/1.1 404 Not Found\r\n\r\n";
                    output.write(notFoundResponse.getBytes(StandardCharsets.UTF_8));
                    socket.close();
                    continue;
                }


                boolean hasHostname = url.contains("hostname");
                boolean hasPort = url.contains("port");
                boolean hasHTTPVersion = url.contains("HTTP/1.1");
                boolean isGetRequest = url.contains("GET");
                // Om inte ovanstaende varibler finns -> 400 Bad Request
                if (!(hasHostname && hasPort && hasHTTPVersion && isGetRequest))
                {
                    stringBuild.append("HTTP/1.1 400 Bad Request\r\n");
                    output.write(stringBuild.toString().getBytes());
                    socket.close();
                    continue;
                }
                // switch cases for urArray
                for (int i = 0; i < urlArray.length; i++)
                {
                    if (i >= 2 && urlArray[i].length() > 0)
                    {
                        switch (urlArray[i])
                        {
                            case "port":
                                try {
                                    port = Integer.parseInt(urlArray[++i]);
                                } catch (NumberFormatException e)
                                {
                                }
                                break;

                            case "timeout":
                                timeout = Integer.valueOf(urlArray[++i]);
                                break;

                            case "hostname":
                                hostname = urlArray[++i];
                                break;

                            case "shutdown":
                                shutdown = Boolean.parseBoolean(urlArray[++i]);
                                break;

                            case "limit":
                                limit = Integer.valueOf(urlArray[++i]);
                                break;

                            case "string":
                                inp = urlArray[++i].getBytes();
                                break;

                            default:
                                break;
                        }

                    }
                }
                // Om port ar mindre eller lika med null eller storre an 65535 kasta 400 Bad request
                if (port <= 0)
                {
                    stringBuild.append("HTTP/1.1 400 Bad Request\r\n\r\nPort must be greater than 0");
                    output.write(stringBuild.toString().getBytes());
                    continue;
                } else if (port > 65535)
                {
                    stringBuild.append("HTTP/1.1 400 Bad Request\r\n\r\nPort must be less than or equal to 65535");
                    output.write(stringBuild.toString().getBytes());
                    continue;
                }
                // Om hostname ar null -> 404 Not Found
                if(hostname == null)
                {
                    stringBuild.append("HTTP/1.1 404 Not Found\r\n\r\nHostnameError");
                    output.write(stringBuild.toString().getBytes());
                    continue;
                }
                StringBuilder responseBuilder = new StringBuilder();
                responseBuilder.append("HTTP/1.1 200 OK\r\n\r\n");

                TCPClient tcp = new TCPClient(shutdown, timeout, limit);
                byte[] responseBytes = tcp.askServer(hostname, port, inp);

                if (responseBytes == null || responseBytes.length == 0)
                {
                    responseBuilder.append("Empty response from server");
                } else {
                    responseBuilder.append(new String(responseBytes));
                }

                responseBuilder.append("\r\n");

                output.write(responseBuilder.toString().getBytes());
                socket.close();
            }

        } catch (Exception e)
        {
            System.out.println("Error is: " + e);
        }
    }
}
