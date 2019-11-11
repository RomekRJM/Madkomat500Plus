package com.example.madkomatapp;

import android.content.Context;
import android.util.Log;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.Callback;
import com.amazonaws.mobile.client.UserStateDetails;
import com.amazonaws.mobile.config.AWSConfiguration;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferNetworkLossHandler;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Region;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;

import org.json.JSONException;

import java.io.File;
import java.util.concurrent.CountDownLatch;

public class AwsUtils {

    private static final String BUCKET = "madkomat-070093830049";

    public static final String NAME = AwsUtils.class.getSimpleName();
    private AWSCredentialsProvider mobileClient;
    private AmazonS3Client s3Client;

    public void uploadToS3(Context context, File file) {
        TransferNetworkLossHandler.getInstance(context);

        TransferUtility transferUtility =
                TransferUtility.builder()
                        .context(context)
                        .awsConfiguration(new AWSConfiguration(context))
                        .s3Client(getS3Client(context))
                        .build();

        TransferObserver observer = transferUtility.upload(
                BUCKET,
                file.getName(),
                file,
                CannedAccessControlList.Private
        );

        observer.setTransferListener(new TransferListener() {
            @Override
            public void onStateChanged(int id, TransferState state) {
                if (state.equals(TransferState.COMPLETED)) {

                } else if (state.equals(TransferState.FAILED)) {

                }

            }

            @Override
            public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {

            }

            @Override
            public void onError(int id, Exception ex) {

            }
        });
    }

    private AWSCredentialsProvider getCredProvider(Context context) {
        if (mobileClient == null) {
            final CountDownLatch latch = new CountDownLatch(1);
            AWSMobileClient.getInstance().initialize(context, new Callback<UserStateDetails>() {
                @Override
                public void onResult(UserStateDetails result) {
                    latch.countDown();
                }

                @Override
                public void onError(Exception e) {
                    Log.e(NAME, "onError: ", e);
                    latch.countDown();
                }
            });
            try {
                latch.await();
                mobileClient = AWSMobileClient.getInstance();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return mobileClient;
    }

    public AmazonS3Client getS3Client(Context context) {
        if (s3Client == null) {
            s3Client = new AmazonS3Client(getCredProvider(context));
            try {
                String regionString = new AWSConfiguration(context)
                        .optJsonObject("S3TransferUtility")
                        .getString("Region");
                s3Client.setRegion(Region.getRegion(regionString));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return s3Client;
    }
}
