package com.example.madkomatapp.lego;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import lejos.pc.comm.NXTComm;
import lejos.pc.comm.NXTCommLogListener;
import lejos.pc.comm.NXTConnector;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

public class BTSend extends Thread {
    static final String TAG = "BTSend";
    private NXTConnector conn;
    Handler mUIMessageHandler;
    DataOutputStream dos;
    DataInputStream dis;

    public static enum CONN_TYPE {
        LEJOS_PACKET, LEGO_LCP
    }

    public BTSend(Handler mUIMessageHandler) {

        super();
        this.mUIMessageHandler = mUIMessageHandler;
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
        Looper.prepare();

        conn = connect(CONN_TYPE.LEJOS_PACKET);

        dos = new DataOutputStream(conn.getOutputStream());
        dis = new DataInputStream(conn.getInputStream());

        int x;
        for (int i = 0; i < 100; i++) {

            try {
                dos.writeInt((i * 30000));
                dos.flush();
                Log.i(TAG, "sent:" + i * 30000);
                yield();
                x = dis.readInt();
                if (x > 0) {

                    Log.i(TAG, "got: " + x);
                }
                Log.d(TAG, "sent:" + i * 30000 + " got:" + x);
                Log.i(TAG, "sent:" + i * 30000 + " got:" + x);
                yield();
            } catch (IOException e) {
                Log.e(TAG, "Error ... ", e);

            }

        }

        closeConnection();
        Looper.loop();
        Looper.myLooper().quit();
        Log.i(TAG, "BTSend finished it's run");
    }

}
