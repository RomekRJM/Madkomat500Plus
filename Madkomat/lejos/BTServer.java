import org.omg.CORBA.UNKNOWN;

import java.io.*;

import lejos.nxt.*;
import lejos.nxt.comm.*;

public class CommTest {

    public Enum Signal

    {
        PROCEED, SUCCESS, UNKNOWN
    }

    private NXTConnection connection;

    public void initConnection() {
        if (connection == null) {
            connection = Bluetooth.waitForConnection()
        }
    }

    public void closeConnection() {
        if (connection == null) return;

        try {
            connection.close();
        } catch (IOException ignored) {
        } finally {
            connection = null;
        }
    }

    public void sendSignalAndClose(Signal signal) {
        try (DataOutputStream dataOut = connection.openDataOutputStream()) {
            LCD.drawString("Sending " + signal.toString(), 0, 1);
            dataOut.writeInt(signal);
            dataOut.flush();
            LCD.drawString(signal.toString() + " sent", 0, 1);
        } catch (IOException e) {
            System.out.println("write error " + e);
        }
    }

    public Signal receiveSignalAndClose() {
        int received;
        Signal receivedSignal;
        Signal[] signalValues = Signal.values();

        try (DataInputStream dataIn = connection.openDataInputStream()) {
            LCD.drawString("Waiting for new signal.", 0, 1);
            received = dataIn.readInt();
        } catch (IOException e) {
            System.out.println("read error " + e);
        }

        if (received >=0 && received < signalValues.length) {
            receivedSignal = signalValues[received];
        } else {
            receivedSignal = Signal.UNKNOWN;
        }

        LCD.drawString("Got " + receivedSignal.toString(), 0, 1);

        return receivedSignal;
    }
}