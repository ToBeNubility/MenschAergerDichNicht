package aufgabe6.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Scanner;

public class ServerKommunikationsThread implements Runnable
{
    private Socket socket;
    private InputStream is;
    private OutputStream os;
    private boolean abbrechen;
    private Scanner sc;
    
    public ServerKommunikationsThread(Socket socket)
    {
        this.abbrechen = false;
        this.socket = socket;
        try{
//        socket.setKeepAlive(true);
        
        is= this.socket.getInputStream();
        os=this.socket.getOutputStream();
        sc = new Scanner(is);
        } catch (Exception e)
        {
            System.err.println("Konnte auf dem Server einen Kommunikationsthread nicht starten");
        }
        
    }

    @Override
    public void run()
    {
        String s;
     //dauerhaft auf Nachrichten vom Client warten
        while (!this.abbrechen)
        {
            try
            {
                if (sc.hasNextLine())
                {
                    s = sc.nextLine();
                    if (s.compareTo("antworte")==0)
                        this.sendString("meine Antwort");
                    System.out.println(s);
                }
            } catch (Exception e)
            {
                System.err.println("exp");
                e.printStackTrace();
            }
        }
    }
    
    public void sendString(String s)
    {
        try
        {
        os.write(s.getBytes());
        }
        catch (IOException e)
        {
            System.err.println("Konnte nicht ");
        }
    }

}
