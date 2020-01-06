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
                case STOP_TO_FLOAT:
                    motor.stop();
                    sleepCalmly(100);
                    motor.flt();
            }

        }
    }

    private final MotorMovement openHatch =
            new MotorMovement(540, 300, false, Motor.A, StopBehaviour.STOP);
    private final MotorMovement closeHatch =
            new MotorMovement(540, 300, true, Motor.A, StopBehaviour.STOP_TO_FLOAT);
    private final MotorMovement loadBanknote =
            new MotorMovement(300, 300, false, Motor.C, StopBehaviour.FLOAT);
    private final MotorMovement giveBanknote =
            new MotorMovement(360, 1200, false, Motor.C, StopBehaviour.FLOAT);

    public void loadBanknotes() {
        loadBanknote.perform();
    }

    public void serve() {
        openHatch.perform();
        giveBanknote.perform();
        sleepCalmly(2500);
        closeHatch.perform();
    }

    public static void sleepCalmly(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ignored) {
        }
    }
}