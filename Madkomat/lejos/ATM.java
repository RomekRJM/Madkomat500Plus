import lejos.nxt.Button;
import lejos.nxt.ButtonListener;
import lejos.nxt.Motor;

public class ATM implements ButtonListener {

    private static final int HATCH_MOVEMENT_SPEED = 540;
    private static final int HATCH_MOVEMENT_DURATION = 300;
    private static final Motor HATCH_MOTOR = Motor.A;

    private static final int BANKNOTE_ROLLER_MOVEMENT_SPEED = 540;
    private static final int BANKNOTE_ROLLER_MOVEMENT_DURATION = 200;
    private static final Motor BANKNOTE_ROLLER_MOTOR = Motor.C;

    public void openHatch() {
        rotateMotor(HATCH_MOTOR, HATCH_MOVEMENT_SPEED, false, HATCH_MOVEMENT_DURATION);
    }

    public void closeHatch() {
        rotateMotor(HATCH_MOTOR, HATCH_MOVEMENT_SPEED, true, HATCH_MOVEMENT_DURATION);
    }

    public void passBanknotes() {
        rotateMotor(BANKNOTE_ROLLER_MOTOR, BANKNOTE_ROLLER_MOVEMENT_SPEED, true,
                BANKNOTE_ROLLER_MOVEMENT_DURATION);
    }

    public void buttonPressed(Button button) {
        if (Button.ENTER.equals(button)) {
            openHatch();
            passBanknotes();
            closeHatch();
        }
    }

    public void buttonReleased(Button button) {
    }

    public static void sleepCalmly(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ignored) {
        }
    }

    public static void main(String[] args) {
        ATM atm = new ATM();
        Button.ENTER.addButtonListener(madkomat);
        Button.ESCAPE.waitForPressAndRelease();
    }
}