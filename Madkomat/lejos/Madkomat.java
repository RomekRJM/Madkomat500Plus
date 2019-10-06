import lejos.nxt.Button;
import lejos.nxt.ButtonListener;
import lejos.nxt.Motor;

public class Madkomat implements ButtonListener {
    
    public void buttonPressed(Button button) {
        if (Button.ENTER.equals(button)) {
            rotateBackward(540, 300);
            System.out.println("ENTER");
        } else if (Button.RIGHT.equals(button)) {
            rotateBackward(540, 1500);
            System.out.println("RIGHT ");
        }
    }
    
    public void buttonReleased(Button button) {
    }
    
    public static void sleepCalmly(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException exc) {
            
        }
    }
    
    public void rotateBackward(int speed, long timeInMilis) {
        Motor.A.setSpeed(speed);
        Motor.A.backward();
        sleepCalmly(timeInMilis);
        Motor.A.stop();
    }
    
    public static void main(String[] args) {
        Madkomat madkomat = new Madkomat();
        Button.ENTER.addButtonListener(madkomat);
        Button.RIGHT.addButtonListener(madkomat);
        
        Button.ESCAPE.waitForPressAndRelease();
    }
}