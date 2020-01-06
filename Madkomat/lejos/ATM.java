import lejos.nxt.Button;
import lejos.nxt.ButtonListener;
import lejos.nxt.Motor;
import lejos.nxt.NXTRegulatedMotor;

public class ATM {

    private enum StopBehaviour {
        STOP, FLOAT, STOP_TO_FLOAT
    }

    private class MotorMovement {
        private final int speed;
        private final int duration;
        private final boolean forward;
        private final NXTRegulatedMotor motor;
        private final StopBehaviour stopBehaviour;

        public MotorMovement(int speed, int duration, boolean forward,
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
                case STOP_TO_FLOAT:
                    motor.stop();
                    sleepCalmly(100);
                    motor.flt();
            }

        }
    }

    private final MotorMovement OPEN_HATCH =
            new MotorMovement(540, 300, false, Motor.A, StopBehaviour.STOP);
    private final MotorMovement CLOSE_HATCH =
            new MotorMovement(540, 300, true, Motor.A, StopBehaviour.STOP_TO_FLOAT);
    private final MotorMovement LOAD_BANKNOTE =
            new MotorMovement(300, 300, false, Motor.C, StopBehaviour.FLOAT);
    private final MotorMovement GIVE_BANKNOTE =
            new MotorMovement(360, 1200, false, Motor.C, StopBehaviour.FLOAT);

    public void loadBanknotes() {
        LOAD_BANKNOTE.perform();
    }

    public void serve() {
        OPEN_HATCH.perform();
        GIVE_BANKNOTE.perform();
        sleepCalmly(2500);
        CLOSE_HATCH.perform();
    }

    public static void sleepCalmly(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ignored) {
        }
    }
}