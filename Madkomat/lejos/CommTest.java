import java.io.*;

import lejos.nxt.*;
import lejos.nxt.comm.*;

public class CommTest {
    public static void main(String[] args) {
        int proceed = 10;
        NXTConnection connection = null;

        LCD.drawString("Press a button to start", 0, 1);
        Button.waitForAnyPress();
        LCD.drawString("waiting for BT", 0, 1);
        connection = Bluetooth.waitForConnection();

        DataInputStream dataIn = connection.openDataInputStream();
        try {
            if (dataIn.readInt() == proceed) {
                LCD.drawString("Received PROCEED signal", 0, 1);
                Button.waitForAnyPress();
            }
        } catch (IOException e) {
            System.out.println(" write error " + e);
        }
    }
}