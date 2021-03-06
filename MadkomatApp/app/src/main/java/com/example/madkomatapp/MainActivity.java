package com.example.madkomatapp;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.util.IOUtils;
import com.example.madkomatapp.animatedimage.AnimationListener;
import com.example.madkomatapp.animatedimage.ImagePreview;
import com.example.madkomatapp.aws.S3Service;
import com.example.madkomatapp.camera.CameraUtils;
import com.example.madkomatapp.face.Face;
import com.example.madkomatapp.face.RecognitionParser;
import com.example.madkomatapp.lego.NXJCache;
import com.example.madkomatapp.lego.NXTService;
import com.example.madkomatapp.lego.NXTService.CommunicationState;
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

public class MainActivity extends AppCompatActivity implements AnimationListener {

    private static final int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 100;
    public static final String GALLERY_DIRECTORY_NAME = "madkomat";
    public static final String IMAGE_EXTENSION = "jpg";

    private String imageStoragePath;

    private TextView txtDescription;
    private Button btnCapturePicture;
    private Button btnGiveMoney;

    private LinearLayout previewPanel;
    private ImagePreview imgPreview;
    private List<Face> faces;

    private BroadcastReceiver s3BroadcastReceiver;
    private BroadcastReceiver nxtBroadcastReceiver;

    private final static String TAG = MainActivity.class.getSimpleName();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_main);

        if (!CameraUtils.isDeviceSupportCamera(getApplicationContext())) {
            Toast.makeText(getApplicationContext(),
                    "Sorry! Your device doesn't support camera",
                    Toast.LENGTH_LONG).show();
            finish();
        }

        txtDescription = findViewById(R.id.txt_desc);
        imgPreview = findViewById(R.id.imgPreview);
        imgPreview.setAnimationListener(this);
        previewPanel = findViewById(R.id.previewPanel);

        btnCapturePicture = findViewById(R.id.btnCapturePicture);
        btnGiveMoney = findViewById(R.id.btnGiveMoney);

        btnCapturePicture.setOnClickListener(v -> {
            if (CameraUtils.checkPermissions(getApplicationContext())) {
                captureImage();
            } else {
                requestCameraPermission();
            }
        });
        activateButton(btnCapturePicture);

        btnGiveMoney.setOnClickListener(v -> {
            notifyLeJOS();
            startMoneyAnimation();
        });

        NXJCache.setup();

        s3BroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Bundle bundle = intent.getExtras();
                if (bundle != null) {
                    S3Service.TransferOperation transferOperation =
                            (S3Service.TransferOperation) bundle.getSerializable(S3Service.INTENT_TRANSFER_OPERATION);
                    TransferState transferState =
                            (TransferState) bundle.getSerializable(S3Service.INTENT_TRANSFER_STATE);
                    transferUpdated(transferOperation, transferState);
                }
            }
        };

        nxtBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Bundle bundle = intent.getExtras();
                if (bundle != null) {
                    CommunicationState state = (CommunicationState)
                            bundle.getSerializable(NXTService.INTENT_COMMUNICATION_STATE);
                    String error = bundle.getString(NXTService.INTENT_ERROR);

                    if (CommunicationState.FAILURE.equals(state)) {
                        Toast.makeText(getApplicationContext(), error, Toast.LENGTH_LONG).show();
                    }
                }
            }
        };

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

    private void captureImage() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        File file = CameraUtils.getOutputMediaFile(getApplicationContext());
        if (file != null) {
            imageStoragePath = file.getAbsolutePath();
        }

        Uri fileUri = CameraUtils.getOutputMediaFileUri(getApplicationContext(), file);

        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
        intent.putExtra("android.intent.extra.CAMERA_FACING", 1);
        startActivityForResult(intent, CAMERA_CAPTURE_IMAGE_REQUEST_CODE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(s3BroadcastReceiver);
        unregisterReceiver(nxtBroadcastReceiver);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        txtDescription.setVisibility(View.VISIBLE);
        imgPreview.setVisibility(View.GONE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(s3BroadcastReceiver, new IntentFilter(S3Service.NOTIFICATION));
        registerReceiver(nxtBroadcastReceiver, new IntentFilter(NXTService.NOTIFICATION));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CAMERA_CAPTURE_IMAGE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {

                previewCapturedImage();

                beginTransferInBackground(S3Service.TransferOperation.TRANSFER_OPERATION_UPLOAD,
                        imageStoragePath);
                beginTransferInBackground(S3Service.TransferOperation.TRANSFER_OPERATION_DOWNLOAD,
                        getJsonFilePath());

                imgPreview.startAnimators();


            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(getApplicationContext(),
                        "User cancelled image capture", Toast.LENGTH_SHORT)
                        .show();
            } else {
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

            setBackgroundPreserveRatio(CameraUtils.optimizeBitmap(imageStoragePath));

        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    private String getJsonFilePath() {
        return StringUtils.replace(imageStoragePath, ".jpg", ".json");
    }

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

    public void transferUpdated(S3Service.TransferOperation transferOperation, TransferState state) {
        if (TransferState.COMPLETED.equals(state) &&
                S3Service.TransferOperation.TRANSFER_OPERATION_DOWNLOAD.equals(transferOperation)) {
            extractFacesFromResponse();
            showAnimation();
        }
    }

    private void extractFacesFromResponse() {
        String response = "";

        try {
            response = IOUtils.toString(new FileInputStream(new File(getJsonFilePath())));
        } catch (IOException e) {
            Log.e(TAG, e.getMessage(), e);
        }

        faces = RecognitionParser.extractFaces(response);
    }

    private void showAnimation() {
        imgPreview.startFaceFoundAnimation(faces);
    }

    public boolean smilingKidFound() {
        return faces.stream().anyMatch(Face::isSmilingKid);
    }

    @Override
    public void lockingFinished() {
        if (faces.isEmpty()) {
            startForegroundAnimation(R.drawable.nothing);
        } else {
            startForegroundAnimation(smilingKidFound() ? R.drawable.welfare : R.drawable.scam);
        }
    }

    @Override
    public void entranceFinished() {
        if (smilingKidFound()) {
            changeActiveButton();
        } else {
            animationFinished();
        }
    }

    @Override
    public void animationFinished() {
        restoreInitialView();
    }

    public void changeActiveButton() {
        if (View.VISIBLE == btnCapturePicture.getVisibility()) {
            deactivateButton(btnCapturePicture);
            activateButton(btnGiveMoney);
        } else {
            deactivateButton(btnGiveMoney);
            activateButton(btnCapturePicture);
        }
    }

    public void activateButton(Button button) {
        button.setVisibility(View.VISIBLE);
        button.setEnabled(true);
    }

    public void deactivateButton(Button button) {
        button.setVisibility(View.GONE);
        button.setEnabled(false);
    }

    private void notifyLeJOS() {
        if (isBluetoothOn()) {
            startService(new Intent(this, NXTService.class));
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

    private void setBackgroundPreserveRatio(Bitmap bitmap) {
        imgPreview.setBackgroundImage(bitmap);
        rescaleImagePreviewTo(bitmap, true);
    }

    private void rescaleImagePreviewTo(Bitmap bitmap, boolean preserveRatio) {
        int newHeight = previewPanel.getHeight();
        int newWidth = preserveRatio ? Math.round(
                bitmap.getWidth() * newHeight / (float) bitmap.getHeight()
        ) : previewPanel.getWidth();
        int marginTop = Math.max(0, (previewPanel.getHeight() - newHeight) / 2);
        ViewGroup.MarginLayoutParams layoutParams = new ViewGroup.MarginLayoutParams(newWidth, newHeight);
        layoutParams.setMargins(0, marginTop, 0, 0);

        imgPreview.setLayoutParams(new LinearLayout.LayoutParams(layoutParams));
        imgPreview.invalidate();
    }

    private void startForegroundAnimation(int id) {
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), id);
        imgPreview.setForegroundImage(bitmap);
        rescaleImagePreviewTo(bitmap, false);
        imgPreview.startForegroundAnimation();
    }

    public void startMoneyAnimation() {
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.b100);
        imgPreview.setForegroundImage(bitmap);
        rescaleImagePreviewTo(bitmap, false);
        imgPreview.startMoneyAnimation();
    }

    public void restoreInitialView() {
        txtDescription.setVisibility(View.VISIBLE);
        imgPreview.setVisibility(View.GONE);
        deactivateButton(btnGiveMoney);
        activateButton(btnCapturePicture);
    }
}