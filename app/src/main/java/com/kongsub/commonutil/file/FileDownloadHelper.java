package com.kongsub.commonutil.file;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import com.kongsub.commonutil.common.EventListener;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public class FileDownloadHelper {
    public final int URL_RESULT_CODE = 4890;
    private final String TAG = "FileDownload";

    private String resultMessage = "";
    private String requestUrl;
    private Activity downActivity;
    private Uri fileUri;
    private EventListener eventListener;

    // 1. constructor
    public FileDownloadHelper(String requestUrl){
        this.requestUrl = requestUrl;
    }

    // 2. create file
    public void createFile(Activity activity){
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        activity.startActivityForResult(intent, URL_RESULT_CODE);
    }

    // 3. setting activity - call in onActivityResult
    public void setDownActivity(Activity activity){
        this.downActivity = activity;
    }

    // 4. async download
    public void asyncDownloadFromUri(Uri fileUri, EventListener eventListener){
        this.fileUri = fileUri;
        this.eventListener = eventListener;

        final AsyncDownloadFromUri downloadTask = new AsyncDownloadFromUri();
        downloadTask.execute();
    }

    private class AsyncDownloadFromUri extends AsyncTask<String, String, Boolean> {
        @Override
        protected Boolean doInBackground(String... strings) {
            return downloadFromUri(fileUri);
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);

            if (eventListener != null) {
                eventListener.onEvent(result, resultMessage);
            }
        }
    }

    /** file download task - call in onActivityResult
     * @param fileUri 파일을 저장할 경로
     * @return
     */
    public boolean downloadFromUri(Uri fileUri){
        int count;
        boolean isSuccess = false;

        ParcelFileDescriptor pfd = null;
        try {
            pfd = downActivity.getContentResolver().openFileDescriptor(fileUri, "w");
        } catch (FileNotFoundException e) {
            resultMessage = "File Not Found Exception, Pleas Check your file uri";
            return false;
        }
        InputStream input = null;
        FileOutputStream output = new FileOutputStream(pfd.getFileDescriptor());
        URLConnection connection;

        try {
            // file download
            URL url = new URL(requestUrl);
            connection = url.openConnection();
            connection.connect();

            input = new BufferedInputStream(url.openStream(), 8192);

            byte data[] = new byte[1024];

            // 생성한 파일에 write
            while ((count = input.read(data)) != -1) {
                output.write(data, 0, count);
            }
            output.flush();
            output.close();
            input.close();
            pfd.close();

            resultMessage = fileUri.toString();
            isSuccess = true;
        }
        catch (Exception e) {
            Log.i(TAG, e.toString());
            resultMessage = e.toString();
        }
        finally {
            try {
                if (output != null) {
                    output.close();
                }
                if (input != null) {
                    input.close();
                }
            }
            catch (IOException ignored) {
                resultMessage = "Network Error";
            }
        }
        return isSuccess;
    }
}