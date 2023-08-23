package com.tailorstore.productionphoto;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import androidx.exifinterface.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;

public class UploadActivity extends AppCompatActivity {

    Button cancel, upload;
    ImageView picture;
    TextView code;
    String productCode = "";
    String imageFilePath = null;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        cancel = findViewById(R.id.cancelButton);
        picture = findViewById(R.id.imageView);
        code = findViewById(R.id.productCodeText);
        upload = findViewById(R.id.uploadButton);

        Intent i = getIntent();
        productCode = i.getStringExtra("productCode");
        imageFilePath = i.getStringExtra("imageFilePath");

        code.setText("Product: " + productCode.substring(3, 10));

        if (imageFilePath != null){
            scaleImage();
            previewImage();
        } else {
            Toast.makeText(getApplicationContext(),
                    "Sorry, file path is missing!", Toast.LENGTH_LONG).show();
        }

        final SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        String[] arraySpinner = new String[] {
                "#inproduction", "#test"
        };

        Spinner spinnerTag = findViewById(R.id.spinner);
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, arraySpinner);
        spinnerTag.setAdapter(spinnerAdapter);

        String tag = sharedPref.getString("tag", "");
        int spinnerPosition = spinnerAdapter.getPosition(tag);
        spinnerTag.setSelection(spinnerPosition);

        cancel.setOnClickListener(v -> {
            productCode = "";
            code.setText(productCode);
            Intent i1 = new Intent(UploadActivity.this, MainActivity.class);
            startActivity(i1);
        });

        upload.setOnClickListener(v -> {
            upload.setEnabled(false);
            String tag1 = spinnerTag.getSelectedItem().toString();
            Toast.makeText(UploadActivity.this, "Tag: "+ tag1, Toast.LENGTH_SHORT).show();
        });
    }

    private void previewImage() {
        Bitmap bitmap = BitmapFactory.decodeFile(imageFilePath);
        picture.setImageBitmap(bitmap);
    }

    private void scaleImage() {
        int rotate = 0;
        try {
            ExifInterface exifInterface = new ExifInterface(imageFilePath);
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotate = 270;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotate = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotate = 90;
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        final Bitmap b = BitmapFactory.decodeFile(imageFilePath);
        double ratio = 1024.0 / b.getWidth();

        Bitmap out = Bitmap.createScaledBitmap(b, 1024, (int) (b.getHeight() * ratio), false);

        if(rotate > 0) {
            Matrix matrix = new Matrix();
            matrix.postRotate(rotate);
            out = Bitmap.createBitmap(out, 0, 0, out.getWidth(), out.getHeight(), matrix, true);
        }

        File file = new File(imageFilePath);

        FileOutputStream fOut;
        try {
            fOut = new FileOutputStream(file);
            out.compress(Bitmap.CompressFormat.JPEG, 90, fOut);
            fOut.flush();
            fOut.close();
            b.recycle();
            out.recycle();
        } catch (Exception e) {
            Log.e("UploadActivity", e.getMessage());
        }
    }

//    private String encodeImageToBase64(Bitmap imageBitmap) {
//        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
//        byte[] imageBytes = baos.toByteArray();
//        return Base64.encodeToString(imageBytes, Base64.DEFAULT);
//    }
}