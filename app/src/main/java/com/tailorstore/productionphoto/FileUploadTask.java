package com.tailorstore.productionphoto;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLPeerUnverifiedException;

public class FileUploadTask {
    private final Context context;
    private final String filePath;
    private final String data;
    private AlertDialog dialog;
    private String errorMessage = "";

    public FileUploadTask(Context context, String filePath, String data) {
        this.context = context;
        this.filePath = filePath;
        this.data = data;
    }

    public void execute() {
        ProgressBar progressBar = new ProgressBar(context);
        dialog = new AlertDialog.Builder(context)
                .setTitle("Uploading...")
                .setView(progressBar)
                .setCancelable(false)
                .create();
        dialog.show();

        new Thread(this::doInBackground).start();
    }

    private void doInBackground() {
        HttpURLConnection connection = null;
        try {
//            File file = new File(filePath);
//            byte[] bytes = getBytesFromFile(file);
//
//            URL url = new URL("https://www.tailorstore.com/production_image_upload" + data);
//            connection = (HttpURLConnection) url.openConnection();
//            connection.setRequestMethod("POST");
//            connection.setRequestProperty("Content-Type", "application/octet-stream");
//            connection.setRequestProperty("Content-Length", String.valueOf(bytes.length));
//            connection.setDoOutput(true);
//
//            connection.connect();
//            OutputStream outputStream = connection.getOutputStream();
//            outputStream.write(bytes);
//            outputStream.flush();
//
//            int responseCode = connection.getResponseCode();

            File file = new File(filePath);
            long fileSize = file.length();

            URL url = new URL(
                    "https://www.tailorstore.com/production_image_upload" + data
            );

            connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("POST");
            connection.setRequestProperty(
                    "Content-Type",
                    "application/octet-stream"
            );

            connection.setDoOutput(true);
            connection.setConnectTimeout(30000);
            connection.setReadTimeout(60000);
            connection.setUseCaches(false);

// Important
            connection.setFixedLengthStreamingMode(fileSize);

            connection.connect();

            try (
                    InputStream inputStream =
                            new BufferedInputStream(new FileInputStream(file));

                    OutputStream outputStream =
                            new BufferedOutputStream(connection.getOutputStream())
            ) {

                byte[] buffer = new byte[16 * 1024]; // 16 KB
                int bytesRead;

                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }

                outputStream.flush();
            }

            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                Intent intent = new Intent(context, MainActivity.class);
                context.startActivity(intent);
            } else {
                errorMessage = "Error uploading file: " + responseCode;
            }

        } catch (MalformedURLException e) {
            errorMessage = "Invalid upload URL.";
        } catch (SocketTimeoutException e) {
            errorMessage = "Connection timed out. Please check your network and try again.";
        } catch (ConnectException e) {
            errorMessage = "Unable to connect to server. Please check your network connection.";
        } catch (UnknownHostException e) {
            errorMessage = "Server address not found. Please check the URL and try again.";
        } catch (SSLHandshakeException e) {
            errorMessage = "SSL certificate error. Please contact support.";
        } catch (SSLPeerUnverifiedException e) {
            errorMessage = "SSL verification failed. Please contact support.";
        } catch (SocketException e) {
            errorMessage = "Network error: " + e.getMessage();
        } catch (SecurityException e) {
            errorMessage = "Permission error while uploading.";
        } catch (IOException e) {
            errorMessage = "Unable to upload file. Please try again. " + e.getMessage();
        } catch (Exception e) {
            errorMessage = "Unexpected upload error: " + e.getMessage();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }

        final String finalErrorMessage = errorMessage;
        Handler handler = new Handler(context.getMainLooper());
        handler.post(() -> {
            dialog.dismiss();
            if (!finalErrorMessage.isEmpty()) {
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