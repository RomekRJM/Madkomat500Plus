package com.example.madkomatapp;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.util.IOUtils;
import com.example.madkomatapp.animatedimage.AnimationListener;
import com.example.madkomatapp.aws.S3Service;
import com.example.madkomatapp.camera.CameraUtils;
import com.example.madkomatapp.face.Face;
import com.example.madkomatapp.face.FaceBuilder;
import com.example.madkomatapp.face.RecognitionParser;
import com.example.madkomatapp.animatedimage.ImagePreview;
import com.example.madkomatapp.lego.BTClient;
import com.example.madkomatapp.lego.NXJCache;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity implements AnimationListener {

    // Activity request codes
    private static final int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 100;

    // key to store image path in savedInstance state
    private static final String KEY_IMAGE_STORAGE_PATH = "image_path";

    // Gallery directory name to store the images
    public static final String GALLERY_DIRECTORY_NAME = "madkomat";

    // Image file extension
    public static final String IMAGE_EXTENSION = "jpg";

    private static String imageStoragePath;

    private TextView txtDescription;
    private Button btnCapturePicture;
    private Button btnLeJOSConnection;

    private static ImagePreview imgPreview;
    private static List<Face> faces;

    private final static String TAG = MainActivity.class.getSimpleName();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_main);

        // Checking availability of the camera
        if (!CameraUtils.isDeviceSupportCamera(getApplicationContext())) {
            Toast.makeText(getApplicationContext(),
                    "Sorry! Your device doesn't support camera",
                    Toast.LENGTH_LONG).show();
            // will close the app if the device doesn't have camera
            finish();
        }

        txtDescription = findViewById(R.id.txt_desc);
        imgPreview = findViewById(R.id.imgPreview);
        imgPreview.setAnimationListener(this);

        btnCapturePicture = findViewById(R.id.btnCapturePicture);
        btnLeJOSConnection = findViewById(R.id.btnTestBTConnection);

        btnCapturePicture.setOnClickListener(v -> {
            if (CameraUtils.checkPermissions(getApplicationContext())) {
                captureImage();
            } else {
                requestCameraPermission();
            }
        });

        btnLeJOSConnection.setOnClickListener(v -> new BTClient().start());

        NXJCache.setup();
        restoreFromBundle(savedInstanceState);

    }

    /**
     * Restoring store image path from saved instance state
     */
    private void restoreFromBundle(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(KEY_IMAGE_STORAGE_PATH)) {
                imageStoragePath = savedInstanceState.getString(KEY_IMAGE_STORAGE_PATH);
                if (!TextUtils.isEmpty(imageStoragePath)) {
                    if (imageStoragePath.substring(imageStoragePath.lastIndexOf(".")).equals("." + IMAGE_EXTENSION)) {
                        previewCapturedImage();
                    }
                }
            }
        }
    }

    /**
     * Requesting permissions using Dexter library
     */
    private void requestCameraPermission() {
        Dexter.withActivity(this)
                .withPermissions(Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (report.areAllPermissionsGranted()) {
                            captureImage();

                        } else if (report.isAnyPermissionPermanentlyDenied()) {
                            showPermissionsAlert();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();
    }


    /**
     * Capturing Camera Image will launch camera app requested image capture
     */
    private void captureImage() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        File file = CameraUtils.getOutputMediaFile(getApplicationContext());
        if (file != null) {
            imageStoragePath = file.getAbsolutePath();
        }

        Uri fileUri = CameraUtils.getOutputMediaFileUri(getApplicationContext(), file);

        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
        intent.putExtra("android.intent.extra.CAMERA_FACING", 1);

        // start the image capture Intent
        startActivityForResult(intent, CAMERA_CAPTURE_IMAGE_REQUEST_CODE);
    }

    /**
     * Saving stored image path to saved instance state
     */
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        // save file url in bundle as it will be null on screen orientation changes
        outState.putString(KEY_IMAGE_STORAGE_PATH, imageStoragePath);
    }

    /**
     * Restoring image path from saved instance state
     */
    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        // get the file url
        imageStoragePath = savedInstanceState.getString(KEY_IMAGE_STORAGE_PATH);
    }

    /**
     * Activity result method will be called after closing the camera
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CAMERA_CAPTURE_IMAGE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // Refreshing the gallery
                CameraUtils.refreshGallery(getApplicationContext(), imageStoragePath);

                previewCapturedImage();

                beginTransferInBackground(S3Service.TransferOperation.TRANSFER_OPERATION_UPLOAD,
                        imageStoragePath);


                beginTransferInBackground(S3Service.TransferOperation.TRANSFER_OPERATION_DOWNLOAD,
                        getJsonFilePath());

                imgPreview.startAnimators();


            } else if (resultCode == RESULT_CANCELED) {
                // user cancelled Image capture
                Toast.makeText(getApplicationContext(),
                        "User cancelled image capture", Toast.LENGTH_SHORT)
                        .show();
            } else {
                // failed to capture image
                Toast.makeText(getApplicationContext(),
                        "Sorry! Failed to capture image", Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }

    private void previewCapturedImage() {
        try {
            txtDescription.setVisibility(View.GONE);

            imgPreview.setVisibility(View.VISIBLE);

            Bitmap bitmap = CameraUtils.optimizeBitmap(imageStoragePath);

            imgPreview.setBackgroundImage(bitmap);

        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    private static String getJsonFilePath() {
        return StringUtils.replace(imageStoragePath, ".jpg", ".json");
    }

    /**
     * Alert dialog to navigate to app settings
     * to enable necessary permissions
     */
    private void showPermissionsAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Permissions required!")
                .setMessage("Camera needs few permissions to work properly. Grant them in settings.")
                .setPositiveButton("GOTO SETTINGS", (dialog, which)
                        -> CameraUtils.openSettings(MainActivity.this))
                .setNegativeButton("CANCEL", (dialog, which) -> {
                }).show();
    }

    private void beginTransferInBackground(S3Service.TransferOperation operation, String filePath) {
        Intent intent = new Intent(this, S3Service.class);
        intent.putExtra(S3Service.INTENT_TRANSFER_OPERATION, operation);
        intent.putExtra(S3Service.INTENT_FILE_PATH, filePath);
        startService(intent);
    }


    public static void transferUpdated(S3Service.TransferOperation transferOperation, TransferState state) {
        if (TransferState.COMPLETED.equals(state) &&
                S3Service.TransferOperation.TRANSFER_OPERATION_DOWNLOAD.equals(transferOperation)) {
            parseResponse();
            showAnimation();
        }
    }

    private static void parseResponse() {
        String response = "";

        try {
            response = IOUtils.toString(new FileInputStream(new File(getJsonFilePath())));
        } catch (IOException e) {
            Log.e(TAG, e.getMessage(), e);
        }

        faces = RecognitionParser.extractFaces(response);
        if (faces.isEmpty()) {
            faces = Collections.singletonList(
                    new FaceBuilder()
                            .setTop(0.3001307249069214)
                            .setLeft(0.36556869745254517)
                            .setHeight(0.17334245145320892)
                            .setWidth(0.22835981845855713)
                            .setAgeRangeLow(2)
                            .setAgeRangeHigh(4)
                            .setSmiling(true)
                            .setSmilingConfidence(91.9542384768385)
                            .createFace());
        }
    }

    private static void showAnimation() {
        Bitmap bitmap = CameraUtils.optimizeBitmap(imageStoragePath);

        imgPreview.setImageBitmap(bitmap);
        imgPreview.startFaceFoundAnimation(faces);

        CameraUtils.writeBitmapToFile(imageStoragePath, bitmap);
    }

    public boolean smilingKidFound() {
        return faces.stream().anyMatch(Face::isSmilingKid);
    }

    @Override
    public void lockingFinished() {
        startForegroundAnimation(smilingKidFound() ? R.drawable.welfare : R.drawable.scam);
    }

    @Override
    public void animationFinished() {
        if (smilingKidFound()) {
            changeActiveButton();
            notifyLeJOS();
        }
    }

    public void changeActiveButton() {
        if (View.VISIBLE == btnCapturePicture.getVisibility()) {
            btnCapturePicture.setVisibility(View.GONE);
            btnLeJOSConnection.setVisibility(View.VISIBLE);
        } else {
            btnCapturePicture.setVisibility(View.VISIBLE);
            btnLeJOSConnection.setVisibility(View.GONE);
        }
    }

    private void notifyLeJOS() {
        if (isBluetoothOn()) {
            new BTClient().start();
        }
    }

    private boolean isBluetoothOn() {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(),
                    "Sorry! Your device doesn't support Bluetooth",
                    Toast.LENGTH_LONG).show();
            return false;
        } else if (!mBluetoothAdapter.isEnabled()) {
            Toast.makeText(getApplicationContext(),
                    "Please enable Bluetooth and connect to NXT first.",
                    Toast.LENGTH_LONG).show();
            return false;
        }

        return true;
    }

    private void startForegroundAnimation(int id) {
        imgPreview.setForegroundImage(BitmapFactory.decodeResource(getResources(), id));
        imgPreview.startForegroundAnimation();
    }
}