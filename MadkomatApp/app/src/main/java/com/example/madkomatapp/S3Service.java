package com.example.madkomatapp;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;

import java.io.File;
import java.io.Serializable;

public class S3Service extends Service {

    private TransferUtility transferUtility;

    final static String INTENT_FILE = "file";
    final static String INTENT_TRANSFER_OPERATION = "transferOperation";

    public enum TransferOperation implements Serializable {
        TRANSFER_OPERATION_UPLOAD, TRANSFER_OPERATION_DOWNLOAD
    }

    private final static String TAG = S3Service.class.getSimpleName();
    private String s3Bucket;

    @Override
    public void onCreate() {
        super.onCreate();

        AwsUtils util = new AwsUtils();
        transferUtility = util.getTransferUtility(this);
        s3Bucket = util.getS3Bucket(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        final TransferOperation transferOperation = (TransferOperation) intent.getSerializableExtra(INTENT_TRANSFER_OPERATION);
        final File file = (File) intent.getSerializableExtra(INTENT_FILE);
        final String key = file.getName();
        TransferObserver transferObserver;

        switch (transferOperation) {
            case TRANSFER_OPERATION_DOWNLOAD:
                Log.d(TAG, "Downloading " + key);
                transferObserver = transferUtility.download(s3Bucket, key, file);
                transferObserver.setTransferListener(new DownloadListener());
                break;
            case TRANSFER_OPERATION_UPLOAD:
                Log.d(TAG, "Uploading " + key);
                transferObserver = transferUtility.upload(key, file);
                transferObserver.setTransferListener(new UploadListener());
                break;
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private class DownloadListener implements TransferListener {

        private boolean notifyDownloadActivityNeeded = true;

        // Simply updates the list when notified.
        @Override
        public void onError(int id, Exception e) {
            Log.e(TAG, "onError: " + id, e);
            if (notifyDownloadActivityNeeded) {
                MainActivity.transferUpdated();
                notifyDownloadActivityNeeded = false;
            }
        }

        @Override
        public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
            Log.d(TAG, String.format("onProgressChanged: %d, total: %d, current: %d",
                    id, bytesTotal, bytesCurrent));
            if (notifyDownloadActivityNeeded) {
                MainActivity.transferUpdated();
                notifyDownloadActivityNeeded = false;
            }
        }

        @Override
        public void onStateChanged(int id, TransferState state) {
            Log.d(TAG, "onStateChanged: " + id + ", " + state);
            if (notifyDownloadActivityNeeded) {
                MainActivity.transferUpdated();
                notifyDownloadActivityNeeded = false;
            }
        }
    }

    private class UploadListener implements TransferListener {

        private boolean notifyUploadActivityNeeded = true;

        // Simply updates the list when notified.
        @Override
        public void onError(int id, Exception e) {
            Log.e(TAG, "onError: " + id, e);
            if (notifyUploadActivityNeeded) {
                MainActivity.transferUpdated();
                notifyUploadActivityNeeded = false;
            }
        }

        @Override
        public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
            Log.d(TAG, String.format("onProgressChanged: %d, total: %d, current: %d",
                    id, bytesTotal, bytesCurrent));
            if (notifyUploadActivityNeeded) {
                MainActivity.transferUpdated();
                notifyUploadActivityNeeded = false;
            }
        }

        @Override
        public void onStateChanged(int id, TransferState state) {
            Log.d(TAG, "onStateChanged: " + id + ", " + state);
            if (notifyUploadActivityNeeded) {
                MainActivity.transferUpdated();
                notifyUploadActivityNeeded = false;
            }
        }
    }
}