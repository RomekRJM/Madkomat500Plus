package com.example.madkomatapp.lego;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.Nullable;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import lejos.pc.comm.NXTComm;
import lejos.pc.comm.NXTCommLogListener;
import lejos.pc.comm.NXTConnector;

public class NXTService extends IntentService {
    public static final String NOTIFICATION = "NXTService";
    public static final String INTENT_COMMUNICATION_STATE = "communicationState";
    public static final String INTENT_ERROR = "error";
    private static final String TAG = "NXTService";

    public NXTService() {
        super(NOTIFICATION);
    }

    enum Signal {
        PROCEED, SUCCESS, UNKNOWN
    }

    enum CONN_TYPE {
        LEJOS_PACKET, LEGO_LCP
    }

    public enum CommunicationState {
        SUCCESS, FAILURE
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

        try {
            switch (connection_type) {
                case LEGO_LCP:
                    conn.connectTo("btspp://NXT", NXTComm.LCP);
                    break;
                case LEJOS_PACKET:
                    conn.connectTo("btspp://");
                    break;
            }
        } catch (NullPointerException e) {
            Log.e(TAG, "Error ... ", e);
            return null;
        }

        return (conn.getNXTComm() != null) ? conn : null;
    }

    @Override
    public void onHandleIntent(@Nullable Intent intent) {
        Log.d(TAG, "NXTService run");
        NXTConnector conn;

        conn = connect(CONN_TYPE.LEJOS_PACKET);
        if (conn == null) {
            publishResults(CommunicationState.FAILURE,
                    "Can't communicate with NXT - is it connected");
            return;
        }

        try (
                DataOutputStream dos = new DataOutputStream(conn.getOutputStream());
                DataInputStream dis = new DataInputStream(conn.getInputStream())
        ) {
            dos.writeInt(Signal.PROCEED.ordinal());
            dos.flush();
            Log.i(TAG, "sent PROCEED signal");

            if (dis.readInt() == Signal.SUCCESS.ordinal()) {
                Log.i(TAG, "got SUCCESS message");
            }
        } catch (IOException e) {
            publishResults(CommunicationState.FAILURE,
                    "Got error while talking to NXT: " + e.getMessage());
        } finally {
            Log.i(TAG, "Closing bluetooth connection");

            try {
                conn.getNXTComm().close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        publishResults(CommunicationState.SUCCESS);
        Log.i(TAG, "NXTService finished it's run");
    }

    private void publishResults(CommunicationState state) {
        publishResults(CommunicationState.SUCCESS, null);
    }

    private void publishResults(CommunicationState state, String error) {
        Intent intent = new Intent(NOTIFICATION);
        intent.putExtra(INTENT_COMMUNICATION_STATE, state);
        intent.putExtra(INTENT_ERROR, error != null ? error : "");
        sendBroadcast(intent);
    }

}
