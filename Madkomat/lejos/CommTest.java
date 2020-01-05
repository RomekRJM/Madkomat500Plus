import java.io.*;

import lejos.nxt.*;
import lejos.nxt.comm.*;

public class CommTest {
    private static final int PROCEED = 8;
    private static final int SUCCESS = 16;

    public static void main(String[] args) {

        LCD.drawString("waiting for BT", 0, 1);
        NXTConnection connection = Bluetooth.waitForConnection();

        try (DataInputStream dataIn = connection.openDataInputStream();
             DataOutputStream dataOut = connection.openDataOutputStream()) {

            if (dataIn.readInt() == PROCEED) {
                LCD.drawString("Received PROCEED signal", 0, 1);
                LCD.drawString("Sending SUCCESS signal", 0, 1);
                dataOut.writeInt(SUCCESS);
                dataOut.flush();
                LCD.drawString("SUCCESS sent, press anything...", 0, 1);
                Button.waitForAnyPress();
            }

        } catch (IOException e) {
            System.out.println(" write error " + e);
        }
    }
}