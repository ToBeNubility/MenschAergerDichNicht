package aufgabe6.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;

import aufgabe6.net.Nachricht.KEYS;

public class ServerKommunikationsThread implements Runnable
{
    private Socket socket;
    private InputStream is;
    private OutputStream os;
    private boolean abbrechen;
    
    public ServerKommunikationsThread(Socket socket)
    {
        this.abbrechen = false;
        this.socket = socket;
        try{
//        socket.setKeepAlive(true);
        
        is=this.socket.getInputStream();
        os=this.socket.getOutputStream();
        Nachricht n = new Nachricht("foo", "bar");
        this.sendeNachricht(n);
//        sc = new Scanner(is);
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
                if (is.available() > 0)
                {
                    ObjectInputStream ois = new ObjectInputStream(is);
                    Nachricht n = (Nachricht) ois.readObject();
                    System.out.println(n.getValue(KEYS.SPIELER_NAME));
                }

            } catch (Exception e)
            {
                System.err.println("exp");
                e.printStackTrace();
            }
        }
    }
    
    public void sendeNachricht(Nachricht n)
    {
        try{
        ObjectOutputStream oos = new ObjectOutputStream(os);
        oos.writeObject(n);
        oos.flush();
        }
        catch (IOException e){
            System.err.println("Fehler beim Senden der Nachricht.");
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