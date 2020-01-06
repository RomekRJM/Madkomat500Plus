import java.io.*;

import lejos.nxt.*;
import lejos.nxt.comm.*;

public class BTServer {

    public enum Signal {
        PROCEED, SUCCESS, UNKNOWN
    }

    private NXTConnection connection;

    public void initConnection() {
        if (connection == null) {
            connection = Bluetooth.waitForConnection();
        }
    }

    public void closeConnection() {
        if (connection == null) return;
        
        connection.close();
        connection = null;
    }

    public void sendSignalAndClose(Signal signal) {
        try (DataOutputStream dataOut = connection.openDataOutputStream()) {
            LCD.drawString("Sending " + signal.toString(), 0, 1);
            dataOut.writeInt(signal.ordinal());
            dataOut.flush();
            LCD.drawString(signal.toString() + " sent", 0, 1);
        } catch (IOException e) {
            System.out.println("write error " + e);
        }
    }

    public Signal receiveSignalAndClose() {
        int received = -1;
        Signal receivedSignal = Signal.UNKNOWN;
        Signal[] signalValues = Signal.values();

        try (DataInputStream dataIn = connection.openDataInputStream()) {
            LCD.drawString("Waiting for new signal.", 0, 1);
            received = dataIn.readInt();
        } catch (IOException e) {
            System.out.println("read error " + e);
        }

        if (received >= 0 && received < signalValues.length) {
            receivedSignal = signalValues[received];
        }

        LCD.drawString("Got " + receivedSignal.toString(), 0, 1);

        return receivedSignal;
    }
}