package com.tailorstore.productionphoto;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class FileUploadTask {
    private Context context;
    private String filePath;
    private String data;
    private ProgressBar progressBar;
    private AlertDialog dialog;
    private String errorMessage = "";

    public FileUploadTask(Context context, String filePath, String data) {
        this.context = context;
        this.filePath = filePath;
        this.data = data;
    }

    public void execute() {
        progressBar = new ProgressBar(context);
        dialog = new AlertDialog.Builder(context)
                .setTitle("Uploading...")
                .setView(progressBar)
                .setCancelable(false)
                .create();
        dialog.show();

        new Thread(this::doInBackground).start();
    }

    private void doInBackground() {
        try {
            File file = new File(filePath);
            byte[] bytes = getBytesFromFile(file);

            URL url = new URL("https://www.tailorstore.com/production_image_upload"+data);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/octet-stream");
            connection.setRequestProperty("Content-Length", String.valueOf(bytes.length));
            connection.setDoOutput(true);

            connection.connect();
            OutputStream outputStream = connection.getOutputStream();
            outputStream.write(bytes);
            outputStream.flush();

            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                Intent intent = new Intent(context, MainActivity.class);
                context.startActivity(intent);
            } else {
                errorMessage = "Error uploading file: " + responseCode;
            }

        } catch (MalformedURLException e) {
            errorMessage = "Malformed url";
        } catch (IOException e) {
            errorMessage = "IO Exception";
        } catch (Exception e) {
            errorMessage = "Other "+e.getMessage();
        }

        final String finalErrorMessage = errorMessage;
        Handler handler = new Handler(context.getMainLooper());
        handler.post(() -> {
            dialog.dismiss();
            if (finalErrorMessage.length() > 0) {
                Toast.makeText(context, finalErrorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    private byte[] getBytesFromFile(File file) throws IOException {
        byte[] bytes = null;
        InputStream inputStream = new FileInputStream(file);
        int length = inputStream.available();
        bytes = new byte[length];
        inputStream.read(bytes);
        inputStream.close();
        return bytes;
    }
}