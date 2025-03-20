package de.innfactory.afps;

public class JavaStatements {
    public static void main(String[] args) {
        String ergebnis;
        if (10 > 5) {
            ergebnis = "Größer";
        } else {
            ergebnis = "Kleiner";
        }
        
        int blockErgebnis;
        {
            int x = 10;
            int y = 20;
            blockErgebnis = x + y;
        }
    }
}
