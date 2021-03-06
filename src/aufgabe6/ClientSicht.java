package aufgabe6;

import java.io.Serializable;
import java.util.Arrays;

import aufgabe6.net.Client;
import aufgabe6.net.Nachricht;
import aufgabe6.net.Nachricht.KEYS;

/**
 * beinhaltet die Informationen ueber das Spiel, die für den Client relevant sind.
 * kann vom Server erstellt und dem Client übersendet werden, damit dieser den aktuellen Spielstand kennt
 * @author rainer
 *
 */
public class ClientSicht implements Serializable {
	private static final long serialVersionUID = 1L;
	private int[][] spielerFiguren = null;
	private String[] spielerName = null;
	private byte meineNummer = -1;
	private boolean istSpielGestartet = false;
	private byte aktuellerSpieler = -1;
	private byte letzteWuerfelZahl = -1;
	private boolean zugAusstehend = false;
	
	public boolean istSpielGestartet() {
		return istSpielGestartet;
	}

	/**
	 * initialisiert eine neue Clientview
	 */
	public ClientSicht() {
		this.spielerFiguren = new int[4][4];
		for (int itSpieler = 0; itSpieler < 4; itSpieler++)
			Arrays.fill(spielerFiguren[itSpieler], -2);
		
		this.spielerName = new String[4];
		Arrays.fill(spielerName, "");
	}
	
	/**
	 * erzeuge ClientSicht aus einem Spieler-Vektor (vom Server aufzurufen)
	 * @param theSpieler der Spielervektor
	 */
	public ClientSicht(Spieler[] theSpieler) {
		spielerFiguren = new int[4][4];
		
		for (int itSpieler = 0; itSpieler < theSpieler.length; itSpieler++) {
			if (theSpieler[itSpieler] != null) {
				for (int itFigur = 0; itFigur < 4; itFigur++)
					spielerFiguren[itSpieler][itFigur] = theSpieler[itSpieler].getFiguren().get(itFigur).getPosition();
			} else {
				Arrays.fill(spielerFiguren[itSpieler], -2);
			}
		}
	}
	
	/**
	 * verarbeite eine Nachricht und aktualisiere die ClientSicht
	 * @param theNachricht die zu verarbeitende Nachricht
	 */
	public void verarbeiteNachricht(Nachricht theNachricht) {
		switch (theNachricht.getNachrichtenTyp()) {
		case SPIELER_PLUS_MINUS:
			String derName = theNachricht.getValue(Nachricht.KEYS.SPIELER_NAME);
			Byte dieNummer = Byte.parseByte(theNachricht.getValue(Nachricht.KEYS.SPIELER_NUMMER));
			
			if (dieNummer < 0) {		// Spieler soll geloescht werden
				dieNummer++;			// Spielernummern muessen bei SPIELER_PLUS_MINUS so kodiert werden, dass -0 nicht erreicht wird, sie reichen also von 1 bis 4 (bzw. -4 bis -1) statt von 0 bis 3
				this.spielerName[-dieNummer] = "";
				Arrays.fill(this.spielerFiguren[-dieNummer], -2);
				
				if (-dieNummer == this.meineNummer) {	// ich selbst soll geloescht werden
		    		Gui.getGui().appendToTextPane("Sie haben das Spiel verlassen.");
			        Client.getInstance().loescheClientRelevanteDaten();
				} else
		    		Gui.getGui().appendToTextPane(derName + " hat das Spiel verlassen.");
			} else {					// Spieler soll hinzugefuegt werden
				dieNummer--;			// Spielernummern muessen bei SPIELER_PLUS_MINUS so kodiert werden, dass -0 nicht erreicht wird, sie reichen also von 1 bis 4 (bzw. -4 bis -1) statt von 0 bis 3
				if (this.meineNummer == -1) {
					this.meineNummer = dieNummer;
		    		Gui.getGui().appendToTextPane("Sie sind dem Spiel als Spieler " + (dieNummer + 1) + " beigetreten.");
				} else
		    		Gui.getGui().appendToTextPane(theNachricht.getLogMessage());

				this.spielerName[dieNummer] = derName;
				this.spielerFiguren = figurenFromString(theNachricht.getValue(KEYS.FIGUREN));
			}
			break;
		case SPIELER_X_WUERFELT_Y:
			//byte dieSpielerNummer = Byte.parseByte(theNachricht.getValue(Nachricht.KEYS.SPIELER_NUMMER));
			//byte wuerfelZahl = Byte.parseByte(theNachricht.getValue(Nachricht.KEYS.WUERFELZAHL))
			aktuellerSpieler = Byte.parseByte(theNachricht.getValue(KEYS.SPIELER_NUMMER));
			letzteWuerfelZahl = Byte.parseByte(theNachricht.getValue(KEYS.WUERFELZAHL));
			
			String nachrichtFiguren = theNachricht.getValue(Nachricht.KEYS.FIGUREN);
			if (!this.istSpielGestartet)
				this.istSpielGestartet = true;
			
			this.spielerFiguren = figurenFromString(nachrichtFiguren);
			zugAusstehend = true;
			break;
		}
	}
	
	/**
	 * wandelt die Figuren dieser ClientSicht in einen String um, der versendet werden kann
	 */
	public String toString() {
		String retString = "";
		
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 4; j++)
				retString += spielerFiguren[i][j] + ",";
			retString += ";";
		}

		return retString;
	}
	
	/**
	 * wandelt einen vom Server erhaltenen String in ein Figuren-Array um
	 * @param theString der vom Server erhaltene String
	 * @return das Figuren-Array, das aus dem String erstellt wurde
	 */
	private int[][] figurenFromString(String theString) {
		int[][] retArr = new int[4][4];
		
		String[] splitString = theString.split(";");
		
		for (int i = 0; i < splitString.length; i++) {
			String[] splitString2 = splitString[i].split(",");
			
			for (int j = 0; j < splitString2.length; j++)
				retArr[i][j] = Integer.parseInt(splitString2[j]);
		}
		
		return retArr;
	}
	
	/**
	 * getter fuer die Figuren aller Spieler
	 * @return die Figuren aller Spieler
	 */
	public int[][] getSpielerFiguren() {
		return spielerFiguren;
	}
	
	/**
	 * getter fuer die Spielernummer
	 * @return die Spielernummer des Clients
	 */
	public byte getMeineNummer() {
		return meineNummer;
	}
	
	/**
	 * getter fuer den aktuellen Spieler
	 * @return die Nummer des aktuellen Spielers
	 */
	public byte getAktuellerSpieler() {
		return this.aktuellerSpieler;
	}
	
	/**
	 * getter fuer das letzte Wuerfelergebnis
	 * @return die zuletzt gewuerfelte Zahl
	 */
	public byte letzteWuerfelZahl() {
		return this.letzteWuerfelZahl;
	}
	
	/**
	 * gibt an, ob der Client an der Reihe ist
	 * @return true, wenn man selbst der aktuelle Spieler ist, sonst false
	 */
	public boolean binDran() {
		return this.aktuellerSpieler == this.meineNummer;
	}

	/**
	 * setzt, ob von diesem Client ein Zug erwartet wird
	 * @param zugAusstehend ob ein Zug erwartet wird
	 */
	public void setZugAusstehend(boolean zugAusstehend)
	{
		this.zugAusstehend = zugAusstehend;
	}

	/**
	 * erfragt, ob von diesem Client ein Zug erwartet wird
	 * @return true, wenn ein Zug erwartet wird, sonst false
	 */
	public boolean isZugAusstehend()
	{
		return zugAusstehend;
	}
}
