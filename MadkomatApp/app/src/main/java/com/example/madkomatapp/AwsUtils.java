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
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Region;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;

import org.json.JSONException;

import java.io.File;
import java.util.concurrent.CountDownLatch;

public class AwsUtils {

    public static final String NAME = AwsUtils.class.getSimpleName();
    private AWSCredentialsProvider mobileClient;
    private AmazonS3Client s3Client;
    private String s3Bucket;

    public boolean keyExists(Context context, String bucket, String key) {
        return getS3Client(context).doesObjectExist(bucket, key);
    }

    public TransferUtility getTransferUtility(Context context) {
        TransferNetworkLossHandler.getInstance(context);

        return TransferUtility.builder()
                .context(context)
                .awsConfiguration(new AWSConfiguration(context))
                .s3Client(getS3Client(context))
                .build();
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

    public String getS3Bucket(Context context) {
        if (s3Bucket == null) {
            try {
                s3Bucket = new AWSConfiguration(context)
                        .optJsonObject("S3TransferUtility")
                        .getString("Bucket");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return s3Bucket;
    }
}
