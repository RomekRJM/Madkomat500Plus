package com.example.madkomatapp.aws;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;

import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.example.madkomatapp.MainActivity;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.Serializable;

public class S3Service extends Service {

    private AwsUtils awsUtils;
    private TransferUtility transferUtility;
    private String s3Bucket;

    public final static String INTENT_FILE_PATH = "filePath";
    public final static String INTENT_TRANSFER_OPERATION = "transferOperation";

    static Context context;

    public enum TransferOperation implements Serializable {
        TRANSFER_OPERATION_UPLOAD, TRANSFER_OPERATION_DOWNLOAD
    }

    private final static String TAG = S3Service.class.getSimpleName();

    @Override
    public void onCreate() {
        super.onCreate();

        awsUtils = new AwsUtils();
        transferUtility = awsUtils.getTransferUtility(this);
        s3Bucket = awsUtils.getS3Bucket(this);
        context = this;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        final TransferOperation transferOperation = (TransferOperation) intent.getSerializableExtra(INTENT_TRANSFER_OPERATION);
        final String filePath = intent.getStringExtra(INTENT_FILE_PATH);
        final String key = StringUtils.substringAfterLast(filePath, "/");

        switch (transferOperation) {
            case TRANSFER_OPERATION_DOWNLOAD:
                handleDownload(key, filePath);
                break;
            case TRANSFER_OPERATION_UPLOAD:
                handleUpload(key, filePath);
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

    private void handleUpload(String key, String filePath) {
        Log.d(TAG, "Uploading " + key);
        TransferObserver transferObserver = transferUtility.upload(key, new File(filePath));
        transferObserver.setTransferListener(new UploadListener());
    }

    private void handleDownload(String key, String filePath) {
        new S3Downloader().execute(key, filePath, "500", "25");
    }

    private class S3Downloader extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            String key = strings[0];
            String filePath = strings[1];
            Long retryInterval = Long.parseLong(strings[2]);
            Long maxRetries = Long.parseLong(strings[3]);

            int retries = 0;

            do {

                if (awsUtils.keyExists(context, s3Bucket, key)) {
                    break;
                }

                Log.d(TAG, "Waiting for S3 key to be accessible: " + key);

                try {
                    Thread.sleep(retryInterval);
                } catch (InterruptedException e) {
                }

            } while (retries < maxRetries);

            Log.d(TAG, "Downloading " + key);
            TransferObserver transferObserver = transferUtility.download(s3Bucket, key, new File(filePath));
            transferObserver.setTransferListener(new DownloadListener());

            return null;
        }
    }

    private class DownloadListener implements TransferListener {

        private boolean notifyDownloadActivityNeeded = true;

        // Simply updates the list when notified.
        @Override
        public void onError(int id, Exception e) {
            Log.e(TAG, "onError: " + id, e);
            if (notifyDownloadActivityNeeded) {
                notifyDownloadActivityNeeded = false;
            }
        }

        @Override
        public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
            Log.d(TAG, String.format("onProgressChanged: %d, total: %d, current: %d",
                    id, bytesTotal, bytesCurrent));
            if (notifyDownloadActivityNeeded) {
                notifyDownloadActivityNeeded = false;
            }
        }

        @Override
        public void onStateChanged(int id, TransferState state) {
            Log.d(TAG, "onStateChanged: " + id + ", " + state);
            if (notifyDownloadActivityNeeded) {
                MainActivity.transferUpdated(TransferOperation.TRANSFER_OPERATION_UPLOAD, state);
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
                notifyUploadActivityNeeded = false;
            }
        }

        @Override
        public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
            Log.d(TAG, String.format("onProgressChanged: %d, total: %d, current: %d",
                    id, bytesTotal, bytesCurrent));
            if (notifyUploadActivityNeeded) {
                notifyUploadActivityNeeded = false;
            }
        }

        @Override
        public void onStateChanged(int id, TransferState state) {
            Log.d(TAG, "onStateChanged: " + id + ", " + state);
            if (notifyUploadActivityNeeded) {
                MainActivity.transferUpdated(TransferOperation.TRANSFER_OPERATION_DOWNLOAD, state);
                notifyUploadActivityNeeded = false;
            }
        }
    }
}