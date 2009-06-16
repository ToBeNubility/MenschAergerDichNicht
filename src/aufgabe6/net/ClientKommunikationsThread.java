package aufgabe6.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import javax.swing.JOptionPane;

import aufgabe6.Gui;
import aufgabe6.net.Nachricht.KEYS;
import aufgabe6.net.Nachricht.NACHRICHTEN_TYP;


/**
 * regelt die clientseitige Kommunikation
 *
 */
public class ClientKommunikationsThread implements Runnable
{
    private Socket socket;
    private InputStream input;
    private ObjectInputStream ois = null;
    private ObjectOutputStream output;
    private boolean abbrechen;
    private Client client;
    
    /**
     * Konstruktur, initialisiert Socket und Port
     * @param socket
     */
    public ClientKommunikationsThread(Socket socket, Client client) {
        this.abbrechen = false;
        this.socket = socket;
        this.client = client;
        try {
	        input = this.socket.getInputStream();
	        output = new ObjectOutputStream(this.socket.getOutputStream());
        } catch (Exception e) {
            System.err.println("Konnte auf dem Client einen Kommunikationsthread nicht starten");
            e.printStackTrace();
        }
        Nachricht n = new Nachricht(this.client.getName(), NACHRICHTEN_TYP.SPIELER_PLUS_MINUS);
        n.setValue(KEYS.SPIELER_NAME, Gui.getGui().getSpielerNamensFeldInhalt());
        this.sendeNachricht(n);
    }

    /**
     * wartet auf Nachrichten und liest sie ein
     */
    @Override
    public void run() {
        while (!this.abbrechen && socket.isConnected()) {
            try {
                if (input.available() > 0) {
                	if (ois==null)
                		ois = new ObjectInputStream(input);
                    Nachricht newNachricht = (Nachricht)ois.readObject();
                    
                    verarbeiteNachricht(newNachricht);
                }
            } catch (ClassNotFoundException e) {
                System.err.println("Fehler beim Lesen einer Nachricht: Class not found");
                e.printStackTrace();
            } catch (IOException e) {
                System.err.println("Fehler beim Lesen einer Nachricht: IO");
                e.printStackTrace();
            }
        }
		try
		{
			ois.close();
			socket.close();
			aufgabe6.Gui.getGui().entferneSpielfeld();
			
		} catch (IOException e){
			e.printStackTrace();
		}
	}
    
    /**
     * sendet eine Nachricht
     * @param theNachricht die zu sendende Nachricht
     */
    public void sendeNachricht(Nachricht theNachricht) {
        try {
        	output.writeObject(theNachricht);
        	output.flush();
        } catch (IOException e) {
            System.err.println("Nachricht konnte nicht gesendet werden");
        }
    }
    
    /**
     * sendet an den Server, dass die Figur an Position x bewegt werden soll
     * @param x die Position der zu bewegenden Figur
     */
    public void sendeBewegungsAufforderung(int x)
    {
        Nachricht n = new Nachricht(this.client.getName(),NACHRICHTEN_TYP.BEWEGUNGS_AUFFORDERUNG);
        n.setValue(KEYS.FIGUREN_POSITION, ""+x);
        System.out.println("new Value (bewegung): "+ n.getValue(KEYS.FIGUREN_POSITION));
        this.sendeNachricht(n);
    }
    
    /**
     * sendet eine Trennen-Nachricht an den Server und löscht die aktuelle ClientSicht
     */
    public void sendeTrennen() {
        Nachricht trennNachricht = new Nachricht(this.client.getName(),NACHRICHTEN_TYP.SPIELER_PLUS_MINUS);
        trennNachricht.setValue(KEYS.SPIELER_NUMMER, String.valueOf(-(this.client.getClientRelevanteDaten().getMeineNummer() + 1)));
        trennNachricht.setValue(KEYS.SPIELER_NAME, Gui.getGui().getSpielerNamensFeldInhalt());
        this.sendeNachricht(trennNachricht);
    }
    
    /**
     * leitet die Nachricht an alle wichtigen Instanzen weiter und schreibt ins Log
     * @param theNachricht die zu verarbeitende Nachricht
     */
    private void verarbeiteNachricht(Nachricht theNachricht) {
    	switch (theNachricht.getNachrichtenTyp()) {
    	case SPIELER_PLUS_MINUS:
    		if (theNachricht.getValue(KEYS.FIGUREN)!=null)
    		{
    			Client.getInstance().getClientRelevanteDaten().verarbeiteNachricht(theNachricht);
    			Gui.getGui().repaintSpielfeld();
    		}
    		else
    			this.abbrechen = true;
    		break;
    	case SPIELER_X_WUERFELT_Y:
    		Client.getInstance().getClientRelevanteDaten().verarbeiteNachricht(theNachricht);
    		Gui.getGui().repaintSpielfeld();
    		Gui.getGui().appendToTextPane(theNachricht.getLogMessage());
    		break;
    	case UNGUELTIGER_ZUG:
    		// TODO fuehre letzte Operation nochmal aus (wenn ich selbst zuletzt dran gewesen, nochmal Klick erforderlich, ansonsten warten)
    		Gui.getGui().appendToTextPane(theNachricht.getLogMessage());
    		break;
    	case SPIELER_X_HAT_GEWONNEN:
    		Gui.getGui().appendToTextPane(theNachricht.getLogMessage());
    		JOptionPane.showMessageDialog(null, theNachricht.getLogMessage(), "Mensch aergere dich nicht : Spielende", JOptionPane.INFORMATION_MESSAGE);
    		this.sendeTrennen();
			break;
    	}
    }
}
