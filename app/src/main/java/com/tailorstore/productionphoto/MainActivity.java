package com.tailorstore.productionphoto;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    Button takePhoto, scanQr, clearBtn;
    TextView contentTxt;
    String productCode = "";
    Uri fileUri;
    String currentPhotoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        takePhoto = findViewById(R.id.take_photo);
        scanQr = findViewById(R.id.scan_qr);
        contentTxt = findViewById(R.id.content_text);
        clearBtn = (Button) findViewById(R.id.clear_button);

        scanQr.setOnClickListener(this);
        takePhoto.setOnClickListener(this);
        clearBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                productCode = "";
                contentTxt.setText(productCode);
                enableTakePhoto();
            }
        });

        enableTakePhoto();
    }

    // take picture Launch
    private void takePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null){
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (photoFile != null){
                Uri photoURI = FileProvider.getUriForFile(this, "com.tailorstore.fileprovider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                cameraLauncher.launch(takePictureIntent);
            }
        } else {
            Toast.makeText(this, "Image capture Cancelled", Toast.LENGTH_LONG).show();
        }
    }

    private final ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == RESULT_OK){

//                        Bundle bundle = result.getData().getExtras();
//                        Bitmap bitmap = (Bitmap) bundle.get("data");
//
//                        Intent intent = new Intent(MainActivity.this,UploadActivity.class);
//                        intent.putExtra("productCode", productCode);
//                        intent.putExtra("BitmapImage", bitmap);
//                        startActivity(intent);
                        launchUploadActivity();
                    }
                }
            }
    );

    // Register the launcher and result handler
    @SuppressLint("SetTextI18n")
    private final ActivityResultLauncher<ScanOptions> barcodeLauncher = registerForActivityResult(new ScanContract(),
            result -> {
                if(result.getContents() == null) {
                    Toast.makeText(MainActivity.this, "Cancelled", Toast.LENGTH_LONG).show();
                } else {
                    if (result.getContents().length() != 26){
                        Toast.makeText(this, "Incorrect barcode ", Toast.LENGTH_LONG).show();
                    } else {
                        productCode = result.getContents();
                        contentTxt.setText("Scanned Product: " + productCode.substring(3, 10));
                        Toast.makeText(this, "Time to take nice photo! ", Toast.LENGTH_LONG).show();
                        enableTakePhoto();
                        takePictureIntent();
                    }
                }
            });

    // Launch
    public void scanQrIntent() {
        ScanOptions options = new ScanOptions();
        options.setOrientationLocked(true);
        barcodeLauncher.launch(options);
    }

    private void enableTakePhoto() {
        takePhoto.setEnabled(productCode != "");
    }

    private File createImageFile() throws IOException {
        String imageFileName = "Captured";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File imageFile = new File(storageDir, imageFileName+".jpg");

        if (imageFile.exists()){
            boolean isDeleted = imageFile.delete();
            if (isDeleted) {
                Toast.makeText(this, "Image deleted!", Toast.LENGTH_SHORT).show();
                Log.d("MainActivity", "Deleted existing file: " + imageFile.getAbsolutePath());
                currentPhotoPath = "";
            } else {
                Toast.makeText(this, "Failed to delete the image.", Toast.LENGTH_SHORT).show();
                Log.e("MainActivity", "Failed to delete existing file: " + imageFile.getAbsolutePath());
            }
        }

        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void launchUploadActivity() {
        Intent intent = new Intent(MainActivity.this,UploadActivity.class);
        intent.putExtra("productCode", productCode);
        intent.putExtra("imageFilePath", currentPhotoPath);
        startActivity(intent);
    }



    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.take_photo:
                takePictureIntent();
                break;
            case R.id.scan_qr:
                scanQrIntent();
                break;
        }

    }

    // block back button press
    @Override
    public void onBackPressed() { }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState){
        // Saving variables
        savedInstanceState.putString("productCode", productCode);
        savedInstanceState.putString("filePhotoPath", currentPhotoPath);
        // Call at the end
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onRestoreInstanceState(@NonNull Bundle savedInstanceState){
        // Call at the start
        super.onRestoreInstanceState(savedInstanceState);

        // Retrieve variables
        productCode = savedInstanceState.getString("productCode");
        currentPhotoPath = savedInstanceState.getString("filePhotoPath");
        enableTakePhoto();
    }
}