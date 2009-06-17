package aufgabe6;

public class MenschMain {

	private static Spiel dasSpiel;
    public static Spiel getDasSpiel()
    {
        return dasSpiel;
    }
    /**
     * initialisiert und startet das Spiel
     * @param args
     */
    public static void main(String[] args) {
		Gui.getGui().starteGui();
		
        dasSpiel = new Spiel();
    }
    
    public static void neuesSpiel()
    {
    	if (!dasSpiel.isAlive())
    		dasSpiel = new Spiel();
    }
    
    public static void spielNeustarten()
    {
    	while (dasSpiel.isAlive());
    	dasSpiel = new Spiel();
    }

}
