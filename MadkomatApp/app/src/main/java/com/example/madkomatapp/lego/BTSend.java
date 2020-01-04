package com.example.madkomatapp.lego;

import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import lejos.pc.comm.NXTComm;
import lejos.pc.comm.NXTCommLogListener;
import lejos.pc.comm.NXTConnector;

public class BTSend extends Thread {
    static final String TAG = "BTSend";
    private NXTConnector conn;
    DataOutputStream dos;
    DataInputStream dis;

    private static final int PROCEED = 8;
    private static final int SUCCESS = 16;

    public static enum CONN_TYPE {
        LEJOS_PACKET, LEGO_LCP
    }

    public BTSend() {
        super();
    }

    public void closeConnection() {
        try {
            Log.d(TAG, "BTSend run loop finished and closing");

            dis.close();
            dos.close();
            conn.getNXTComm().close();
        } catch (Exception e) {
        } finally {
            dis = null;
            dos = null;
            conn = null;
        }
    }

    public static NXTConnector connect(final CONN_TYPE connection_type) {
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

        conn = connect(CONN_TYPE.LEJOS_PACKET);

        dos = new DataOutputStream(conn.getOutputStream());
        dis = new DataInputStream(conn.getInputStream());

        try {
            dos.writeInt(PROCEED);
            dos.flush();
            Log.i(TAG, "sent PROCEED signal");

            if (dis.readInt() == SUCCESS) {
                Log.i(TAG, "got SUCCESS message");
            }
        } catch (IOException e) {
            Log.e(TAG, "Error ... ", e);
        }

        closeConnection();
        Log.i(TAG, "BTSend finished it's run");
    }

}
