package tcpclient;
import java.io.*;
import java.net.*;

public class TCPClient
{
  public TCPClient()
  {

  }
    public byte[] askServer(String hostname, int port, byte[] toServerBytes) throws IOException
    {
        Socket socket = new Socket(hostname, port); // jag skapar en socekt för att koppla till servern.

        OutputStream out = socket.getOutputStream(); //Jag sänder data till servern med OutputStream.
        out.write(toServerBytes);

        InputStream in = socket.getInputStream(); // Jag tar emot data från server till client från socekt med InputStream.
        ByteArrayOutputStream buffer = new ByteArrayOutputStream(); // Jag skapar en BAOS som jag döper till buffer som sparar data från InputStream.
        int R; // R är en variabel som sparar antalet bytes från InputStream.
        int bufferSize = 32768; // Jag deklarerar en bufferSize med storleken 2^15.
        byte[] webdata = new byte[bufferSize]; // Detta är antalet bytes som kan användas för att läsa InputStream.
        while ((R = in.read(webdata, 0, webdata.length)) != -1) // denna loop läser data från InputStream och sparar den i buffern.
        {
            buffer.write(webdata, 0, R); // buffer.write används för att skriva den läsbara datan (d) till buffern.
        }
        buffer.flush(); // Jag flushar buffern för att säkerställa att all data är skriven tillbaka till socket.
        socket.close(); // Jag stänger ned socket.
        return buffer.toByteArray(); // Jag returnerar datan som är sparad i buffern till en toByteArray.
    }
}
