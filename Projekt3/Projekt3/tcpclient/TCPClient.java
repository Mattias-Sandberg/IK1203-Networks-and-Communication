package tcpclient;
import java.net.*;
import java.io.*;

public class TCPClient
{
    private final Integer limit;
    private final Integer timeout;
    private final boolean shutdown;

    public TCPClient(boolean shutdown, Integer timeout, Integer limit)
    {
        this.limit = limit;
        this.timeout = timeout;
        this.shutdown = shutdown;
    }
    public byte[] askServer(String hostname, int port, byte[] toServerBytes) throws IOException
    {
        Socket socket = new Socket(hostname, port); // Jag skapar en socekt for att koppla till servern.
        OutputStream outputServerReceive = socket.getOutputStream(); //Jag sander data till servern med OutputStream.
        outputServerReceive.write(toServerBytes); // Skickar byte-arrayen (toServerBytes) till server.
        InputStream inputClientReceive = socket.getInputStream(); // Jag tar emot data fran server till client fran socekt med InputStream.
        ByteArrayOutputStream BAOS = new ByteArrayOutputStream(); //// Jag skapar en BAOS som jag doper till buffer som sparar data fran InputStream.
        if (shutdown)  // Jag Stanger av utmatningen av socketen om shutdown ar sann (boolean).
        {
            socket.shutdownOutput();
        }
        // Jag Deklarerar variablerna range, bufferSize, Bufferfixed och rangeTot.
        int range;
        int bufferSize = 1024;
        byte[] bufferFixed = new byte[bufferSize];
        int rangeTot = 0;
        if (timeout != null)  //Staller in tidsgransen for socketen om timeout inte ar null.
        {
            socket.setSoTimeout(timeout);
        }
        // Loopar sa lange inputClientReceive returnerar en positiv integer som lasts in i Bufferfixed.

        try
        {


            while ((range = inputClientReceive.read(bufferFixed)) != -1)
            {
                // Skriver antalet bytes fran Bufferfixed fran position 0 till godtyckligt varde.
                rangeTot += range;

                //Adderar range till variabeln rangeTot, som haller koll pa det totala antalet bytes som har lasts in fran servern.
                if (limit != null && BAOS.size() + range >= limit) // Om limit inte ar lika med null och rangeTot ar storre eller lika med limit.
                {
                    BAOS.write(bufferFixed, 0, limit - BAOS.size());
                    break; // Om sant, da break loop for att forhindra att fler bytes lases in och lagras i buffer.
                }
                BAOS.write(bufferFixed, 0, range);
            }
        }
        catch(SocketTimeoutException ex)
        {
            System.out.println("Timeout");
        }

        // Stanger socketanslutningen.
        socket.close();
        // Returnerar innehallet i buffer som en byte-array.
        return BAOS.toByteArray();
    }
}