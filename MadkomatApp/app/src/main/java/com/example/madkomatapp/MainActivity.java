package com.example.madkomatapp;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.util.IOUtils;
import com.example.madkomatapp.aws.S3Service;
import com.example.madkomatapp.camera.CameraUtils;
import com.example.madkomatapp.face.Face;
import com.example.madkomatapp.face.RecognitionParser;
import com.example.madkomatapp.image.ImagePreview;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    // Activity request codes
    private static final int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 100;

    // key to store image path in savedInstance state
    public static final String KEY_IMAGE_STORAGE_PATH = "image_path";

    public static final int MEDIA_TYPE_IMAGE = 1;

    // Bitmap sampling size
    public static final int BITMAP_SAMPLE_SIZE = 8;

    // Gallery directory name to store the images
    public static final String GALLERY_DIRECTORY_NAME = "madkomat";

    // Image file extension
    public static final String IMAGE_EXTENSION = "jpg";

    private static String imageStoragePath;

    private TextView txtDescription;
    private static ImagePreview imgPreview;
    private Button btnCapturePicture;

    private final static String TAG = MainActivity.class.getSimpleName();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

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
        btnCapturePicture = findViewById(R.id.btnCapturePicture);

        /**
         * Capture image on button click
         */
        btnCapturePicture.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (CameraUtils.checkPermissions(getApplicationContext())) {
                    captureImage();
                } else {
                    requestCameraPermission(MEDIA_TYPE_IMAGE);
                }
            }
        });

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
    private void requestCameraPermission(final int type) {
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
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // save file url in bundle as it will be null on screen orientation
        // changes
        outState.putString(KEY_IMAGE_STORAGE_PATH, imageStoragePath);
    }

    /**
     * Restoring image path from saved instance state
     */
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
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

                imgPreview.startAnimator();


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

    /**
     * Display image from gallery
     */
    private void previewCapturedImage() {
        try {
            txtDescription.setVisibility(View.GONE);

            imgPreview.setVisibility(View.VISIBLE);

            Bitmap bitmap = CameraUtils.optimizeBitmap(BITMAP_SAMPLE_SIZE, imageStoragePath);

            imgPreview.setBackgroundImage(bitmap);

        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    private static Bitmap drawRectOnTop(Bitmap bitmap, Face face) {
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();

        int color = Color.argb(128, 255, 0, 0);

        if(face.isSmilingKid()) {
            color = Color.argb(128, 0, 255, 0);
        }

        paint.setColor(color);

        float rectX = (float)(face.getLeft() * bitmap.getWidth());
        float rectY = (float)(face.getTop() * bitmap.getHeight());
        float rectWidth = (float)(face.getWidth() * bitmap.getWidth());
        float rectHeight = (float)(face.getHeight() * bitmap.getHeight());

        canvas.drawRect(rectX, rectY, rectX + rectWidth, rectY + rectHeight, paint);
        return bitmap;
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
                .setPositiveButton("GOTO SETTINGS", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        CameraUtils.openSettings(MainActivity.this);
                    }
                })
                .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                    }
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

            Bitmap bitmap = CameraUtils.optimizeBitmap(BITMAP_SAMPLE_SIZE, imageStoragePath);
            String response = "";

            try {
                response = IOUtils.toString(new FileInputStream(new File(getJsonFilePath())));
            } catch (IOException e) {
                Log.e(TAG, e.getMessage(), e);
            }

            List<Face> faces = RecognitionParser.extractFaces(response);

            for(Face face : faces) {
                drawRectOnTop(bitmap, face);
            }

            imgPreview.setImageBitmap(bitmap);
            CameraUtils.writeBitmapToFile(imageStoragePath, bitmap);
        }
    }
}