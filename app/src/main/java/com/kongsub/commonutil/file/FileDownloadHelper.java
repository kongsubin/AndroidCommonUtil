package com.kongsub.commonutil.file;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
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

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class FileDownloadHelper {
    private final String TAG = "FileDownload";

    String resultMessage = "";
    private String requestUrl;
    private Activity downActivity;
    private int URL_RESULT_CODE;
    private Uri fileUri;
    private EventListener eventListener;

    public enum FILE_TYPE { PDF(0), DOC(1), TXT(2), EXEL(3), CSV(4);
        private final int i;
        FILE_TYPE(int i) {
            this.i = i;
        }
        public int getIndex() {
            return i;
        }
    }
    private String[] extension = {".pdf", ".docx", ".txt", ".xlsx", ".csv"};
    // private String[] mimeType = {"application/pdf", "application/msword", "text/plain", "application/vnd.ms-excel", "text/csv"};

    // 1. constructor
    public FileDownloadHelper(String requestUrl, int URL_RESULT_CODE){
        this.requestUrl = requestUrl;
        this.URL_RESULT_CODE = URL_RESULT_CODE;
    }

    // 2. create file
    public void createFile(Activity activity, String fileName, FILE_TYPE type){
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.putExtra(Intent.EXTRA_TITLE, extensionFile(fileName, type));
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        activity.startActivityForResult(intent, URL_RESULT_CODE);
    }

    // 3. setting activity - call in onActivityResult
    public void setDownActivity(Activity activity){
        this.downActivity = activity;
    }

    // 4. async download
    public void BackgroundDownloadFromUri(Uri fileUri, EventListener eventListener) {
        Disposable backgroundDownloadFromUri;
        //onPreExecute
        this.eventListener = eventListener;
        backgroundDownloadFromUri = Observable.fromCallable(() -> {
                    //doInBackground
                    resultMessage = downloadFromUri(fileUri);
                    return true;
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((result) -> {
                    //onPostExecute
                    if (eventListener != null) {
                        eventListener.onEvent(true, resultMessage);
                    }
                });
    }



    /** file download task - call in onActivityResult
     * @param fileUri 파일을 저장할 경로
     * @return
     */
    public String downloadFromUri(Uri fileUri){
        int count;

        ParcelFileDescriptor pfd = null;
        try {
            pfd = downActivity.getContentResolver().openFileDescriptor(fileUri, "w");
        } catch (FileNotFoundException e) {
            resultMessage = "File Not Found Exception, Pleas Check your file uri";
            return resultMessage;
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
        return resultMessage;
    }

    private String extensionFile(String fileName, FILE_TYPE type){
        return fileName + extension[type.getIndex()];
    }
}