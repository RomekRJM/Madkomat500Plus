import lejos.nxt.Button;
import lejos.nxt.ButtonListener;
import lejos.nxt.Motor;
import lejos.nxt.NXTRegulatedMotor;

public class ATM implements ButtonListener {

    private static final int HATCH_MOVEMENT_SPEED = 540;
    private static final int HATCH_MOVEMENT_DURATION = 300;
    private static final NXTRegulatedMotor HATCH_MOTOR = Motor.A;

    private static final int BANKNOTE_ROLLER_MOVEMENT_SPEED = 360;
    private static final int BANKNOTE_ROLLER_MOVEMENT_DURATION = 800;
    private static final NXTRegulatedMotor BANKNOTE_ROLLER_MOTOR = Motor.C;

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

    public void rotateMotor(NXTRegulatedMotor motor, int speed, boolean forward, long timeInMilis) {
        motor.setSpeed(speed);
        if (forward) {
            motor.forward();
        } else {
            motor.backward();
        }
        sleepCalmly(timeInMilis);
        motor.stop();
    }

    public void buttonPressed(Button button) {
        if (Button.ENTER.equals(button)) {
            openHatch();
            passBanknotes();
            sleepCalmly(1000);
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
        Button.ENTER.addButtonListener(atm);
        Button.ESCAPE.waitForPressAndRelease();
    }
}