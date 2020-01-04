import java.io.*;

import lejos.nxt.*;
import lejos.nxt.comm.*;

public class CommTest {
    private static final int PROCEED = 8;
    private static final int SUCCESS = 16;

    public static void main(String[] args) {

        NXTConnection connection = null;

        LCD.drawString("Press a button to start", 0, 1);
        Button.waitForAnyPress();
        LCD.drawString("waiting for BT", 0, 1);
        connection = Bluetooth.waitForConnection();

        DataInputStream dataIn = connection.openDataInputStream();
        DataOutputStream dataOut = connection.openDataOutputStream();

        try {
            if (dataIn.readInt() == PROCEED) {
                LCD.drawString("Received PROCEED signal", 0, 1);
                Button.waitForAnyPress();
                LCD.drawString("Sending SUCCESS signal", 0, 1);
                dataOut.writeInt(SUCCESS);
            }
        } catch (IOException e) {
            System.out.println(" write error " + e);
        }
    }
}