package com.example.madkomatapp.lego;

import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import lejos.pc.comm.NXTComm;
import lejos.pc.comm.NXTCommLogListener;
import lejos.pc.comm.NXTConnector;

public class BTSend extends Thread {
    private static final String TAG = "BTSend";

    private static final int PROCEED = 8;
    private static final int SUCCESS = 16;

    public enum CONN_TYPE {
        LEJOS_PACKET, LEGO_LCP
    }

    public BTSend() {
        super();
    }

    private static NXTConnector connect(final CONN_TYPE connection_type) {
        Log.d(TAG, " about to add LEJOS listener ");

        NXTConnector conn = new NXTConnector();
        conn.setDebug(true);
        conn.addLogListener(new NXTCommLogListener() {

            public void logEvent(String arg0) {
                Log.e(TAG + " NXJ log:", arg0);
            }

            public void logEvent(Throwable arg0) {
                Log.e(TAG + " NXJ log:", arg0.getMessage(), arg0);
            }
        });

        switch (connection_type) {
            case LEGO_LCP:
                conn.connectTo("btspp://NXT", NXTComm.LCP);
                break;
            case LEJOS_PACKET:
                conn.connectTo("btspp://");
                break;
        }

        return conn;

    }

    @Override
    public void run() {
        Log.d(TAG, "BTSend run");

        NXTConnector conn = connect(CONN_TYPE.LEJOS_PACKET);

        try (
                DataOutputStream dos = new DataOutputStream(conn.getOutputStream());
                DataInputStream dis = new DataInputStream(conn.getInputStream())
        ) {
            dos.writeInt(PROCEED);
            dos.flush();
            Log.i(TAG, "sent PROCEED signal");

            if (dis.readInt() == SUCCESS) {
                Log.i(TAG, "got SUCCESS message");
            }
        } catch (IOException e) {
            Log.e(TAG, "Error ... ", e);
        } finally {
            Log.i(TAG, "Closing bluetooth connection");

            try {
                conn.getNXTComm().close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            conn = null;
        }

        Log.i(TAG, "BTSend finished it's run");
    }

}
