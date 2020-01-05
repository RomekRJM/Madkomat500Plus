import lejos.nxt.Button;
import lejos.nxt.ButtonListener;
import lejos.nxt.Motor;
import lejos.nxt.NXTRegulatedMotor;

public class ATM implements ButtonListener {

    private enum StopBehaviour {
        STOP, FLOAT, FLOAT_TO_STOP
    }

    private class MotorMovement {
        private final int speed;
        private final int duration;
        private final boolean forward;
        private final NXTRegulatedMotor motor;
        private final StopBehaviour stopBehaviour;

        MotorMovement(int speed, int duration, boolean forward,
                      NXTRegulatedMotor motor, StopBehaviour stopBehaviour) {
            this.speed = speed;
            this.duration = duration;
            this.forward = forward;
            this.motor = motor;
            this.stopBehaviour = stopBehaviour;
        }

        public void perform() {
            motor.setSpeed(speed);
            if (forward) {
                motor.forward();
            } else {
                motor.backward();
            }

            sleepCalmly(duration);

            switch (stopBehaviour) {
                default:
                case STOP:
                    motor.stop();
                    break;
                case FLOAT:
                    motor.flt();
                    break;
                case FLOAT_TO_STOP:
                    motor.flt();
                    sleepCalmly(100);
                    motor.stop();
            }

        }
    }

    private final MotorMovement OPEN_HATCH =
            new MotorMovement(540, 300, false, Motor.A, StopBehaviour.STOP);
    private final MotorMovement CLOSE_HATCH =
            new MotorMovement(540, 300, true, Motor.A, StopBehaviour.FLOAT_TO_STOP);
    private final MotorMovement LOAD_BANKNOTE =
            new MotorMovement(300, 300, false, Motor.C, StopBehaviour.FLOAT);
    private final MotorMovement GIVE_BANKNOTE =
            new MotorMovement(360, 1200, false, Motor.C, StopBehaviour.FLOAT);

    public void buttonPressed(Button button) {
        if (Button.ENTER.equals(button)) {
            OPEN_HATCH.perform();
            GIVE_BANKNOTE.perform();
            sleepCalmly(2500);
            CLOSE_HATCH.perform();
        } else if (Button.RIGHT.equals(button) || Button.LEFT.equals(button)) {
            LOAD_BANKNOTE.perform();
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
        Button.RIGHT.addButtonListener(atm);
        Button.LEFT.addButtonListener(atm);

        Button.ESCAPE.waitForPressAndRelease();
    }
}