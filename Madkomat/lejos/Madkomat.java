import lejos.nxt.Button;
import lejos.nxt.ButtonListener;
import lejos.nxt.LCD;
import lejos.nxt.Motor;

public class Madkomat implements ButtonListener {

    private final ATM atm;
    private final BTServer btServer;
    private static final String EMPTY_LINE = "             ";

    public Madkomat() {
        atm = new ATM();
        btServer = new BTServer();

        Button.ENTER.addButtonListener(this);
        Button.RIGHT.addButtonListener(this);
        Button.LEFT.addButtonListener(this);
        Button.ESCAPE.addButtonListener(this);
    }

    public void buttonPressed(Button button) {
        if (Button.RIGHT.equals(button) || Button.LEFT.equals(button)) {
            atm.loadBanknotes();
        } else if (Button.ESCAPE.equals(button)) {
            System.exit(0);
        }
    }

    public void buttonReleased(Button button) {
    }

    public void start() {

        while(true) {
            clearScreen();
            LCD.drawString("Awaiting connection...", 0, 2);
            LCD.drawString("ESCAPE to exit", 0, 4);

            btServer.initConnection();

            if (btServer.receiveSignalAndClose() == BTServer.Signal.PROCEED) {
                clearScreen();
                LCD.drawString("Giving money!", 0, 2);
                atm.serve();
            }

            btServer.closeConnection();
        }
    }

    public void clearScreen() {
        for(int i=0; i<6; ++i) {
            LCD.drawString(EMPTY_LINE, 0, i);
        }
    }
    
    public static void main(String[] args) {
        new Madkomat().start();
    }
}