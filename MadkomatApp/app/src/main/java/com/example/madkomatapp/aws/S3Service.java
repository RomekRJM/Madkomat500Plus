package com.example.madkomatapp.aws;

import android.app.IntentService;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.Serializable;

public class S3Service extends IntentService {

    private AwsUtils awsUtils;
    private TransferUtility transferUtility;
    private String s3Bucket;

    public final static String NOTIFICATION = "S3Service";
    public final static String INTENT_FILE_PATH = "filePath";
    public final static String INTENT_TRANSFER_OPERATION = "transferOperation";
    public final static String INTENT_TRANSFER_STATE = "transferState";

    public enum TransferOperation implements Serializable {
        TRANSFER_OPERATION_UPLOAD, TRANSFER_OPERATION_DOWNLOAD
    }

    private final static String TAG = S3Service.class.getSimpleName();

    public S3Service() {
        super("S3Service");
    }

    @Override
    public void onCreate() {
        super.onCreate();

        awsUtils = new AwsUtils();
        transferUtility = awsUtils.getTransferUtility(this);
        s3Bucket = awsUtils.getS3Bucket(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        final TransferOperation transferOperation = (TransferOperation) intent.getSerializableExtra(INTENT_TRANSFER_OPERATION);
        final String filePath = intent.getStringExtra(INTENT_FILE_PATH);
        final String key = StringUtils.substringAfterLast(filePath, "/");

        assert transferOperation != null;
        switch (transferOperation) {
            case TRANSFER_OPERATION_DOWNLOAD:
                handleDownload(key, filePath);
                break;
            case TRANSFER_OPERATION_UPLOAD:
                handleUpload(key, filePath);
                break;
        }
    }

    private void handleUpload(String key, String filePath) {
        Log.d(TAG, "Uploading " + key);
        TransferObserver transferObserver = transferUtility.upload(key, new File(filePath));
        transferObserver.setTransferListener(new UploadListener());
    }

    private void handleDownload(String key, String filePath) {
        int retries = 0;

        do {

            if (awsUtils.keyExists(getBaseContext(), s3Bucket, key)) {
                break;
            }

            Log.d(TAG, "Waiting for S3 key to be accessible: " + key);

            try {
                Thread.sleep(500);
            } catch (InterruptedException ignored) {
            }

        } while (++retries < 25);

        Log.d(TAG, "Downloading " + key);
        TransferObserver transferObserver = transferUtility.download(s3Bucket, key, new File(filePath));
        transferObserver.setTransferListener(new DownloadListener());
    }

    private class DownloadListener implements TransferListener {

        @Override
        public void onError(int id, Exception e) {
            Log.e(TAG, "onError: " + id, e);
        }

        @Override
        public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
            Log.d(TAG, String.format("onProgressChanged: %d, total: %d, current: %d",
                    id, bytesTotal, bytesCurrent));
        }

        @Override
        public void onStateChanged(int id, TransferState state) {
            Log.d(TAG, "onStateChanged: " + id + ", " + state);

            if (TransferState.COMPLETED.equals(state)) {
                publishResults(TransferOperation.TRANSFER_OPERATION_DOWNLOAD, state);
            }
        }
    }

    private class UploadListener implements TransferListener {

        @Override
        public void onError(int id, Exception e) {
            Log.e(TAG, "onError: " + id, e);
        }

        @Override
        public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
            Log.d(TAG, String.format("onProgressChanged: %d, total: %d, current: %d",
                    id, bytesTotal, bytesCurrent));
        }

        @Override
        public void onStateChanged(int id, TransferState state) {
            Log.d(TAG, "onStateChanged: " + id + ", " + state);
        }
    }

    private void publishResults(TransferOperation outputPath, TransferState state) {
        Intent intent = new Intent(NOTIFICATION);
        intent.putExtra(INTENT_TRANSFER_OPERATION, TransferOperation.TRANSFER_OPERATION_DOWNLOAD);
        intent.putExtra(INTENT_TRANSFER_STATE, state);
        sendBroadcast(intent);
    }
}