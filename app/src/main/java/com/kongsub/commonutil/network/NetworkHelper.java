package com.kongsub.commonutil.network;

import static android.content.ContentValues.TAG;

import android.os.AsyncTask;
import android.util.Log;
import androidx.annotation.NonNull;

import com.kongsub.commonutil.common.EventListener;
import com.kongsub.commonutil.file.FileDownloadHelper;

import org.json.JSONObject;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class NetworkHelper {
    private String headerURL;
    private EventListener eventListener;
    private String response = "";

    // Constructor
    public NetworkHelper(String headerURL) {
        this.headerURL = headerURL;
    }

    /**
     * get Method
     * @param tailURL connection 할 url end point
     * @return
     * @throws IOException
     */
    public String getTask(String tailURL) throws IOException {
        URL url = new URL(createURL(tailURL));
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        try {
            connection.setRequestMethod("GET");
            connection.setDoInput(true);
            connection.setDoOutput(false);

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                InputStream in = connection.getInputStream();
                Log.d(TAG, "Success to Connect Network");
                return readStream(in);
            }
            else {
                Log.d(TAG, "Failed to Connect Network");
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        } finally {
            connection.disconnect();
        }
        return null;
    }

    /**
     * post Method
     * @param tailURL connection 할 url end point
     * @param params post query
     * @return
     * @throws IOException
     */
    public String postTask(String tailURL, HashMap<String, String> params) {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(createURL(tailURL));
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setRequestProperty("Accept-Charset","UTF-8");
            connection.setDoInput(true);
            connection.setDoOutput(true);

            // send params
            OutputStream out = new BufferedOutputStream(connection.getOutputStream());
            writeStream(out, createQuery(params, "UTF-8"));
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // get result
                InputStream in = new BufferedInputStream(connection.getInputStream());
                Log.d(TAG, "Success to Connect Network");
                return readStream(in);
            }
            else {
                Log.d(TAG, "Failed to Connect Network");
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        } finally {
            connection.disconnect();
        }
        return null;
    }

    /**
     * post Method
     * @param tailURL connection 할 url end point
     * @param params post json
     * @return
     * @throws IOException
     */
    public String postTask(String tailURL, JSONObject params) {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(createURL(tailURL));
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json; utf-8");
            connection.setRequestProperty("Accept", "application/json");
            connection.setDoInput(true);
            connection.setDoOutput(true);

            // send params
            OutputStream out = new BufferedOutputStream(connection.getOutputStream());
            writeStream(out, params.toString());
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // get result
                InputStream in = new BufferedInputStream(connection.getInputStream());
                Log.d(TAG, "Success to Connect Network");
                return readStream(in);
            }
            else {
                Log.d(TAG, "Failed to Connect Network");
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        } finally {
            connection.disconnect();
        }
        return null;
    }


    public void BackgroundGet(String tailURL, EventListener eventListener) {
        Disposable backgroundGet;
        //onPreExecute
        this.eventListener = eventListener;
        backgroundGet = Observable.fromCallable(() -> {
                    //doInBackground
                    response = getTask(tailURL);
                    return true;
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((result) -> {
                    //onPostExecute
                    eventListener.onEvent(true, response);
                });
    }

    public void BackgroundPost(String tailURL, JSONObject params, EventListener eventListener) {
        Disposable backgroundPost;
        //onPreExecute
        this.eventListener = eventListener;
        backgroundPost = Observable.fromCallable(() -> {
                    //doInBackground
                    response = postTask(tailURL, params);
                    return true;
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((result) -> {
                    //onPostExecute
                    eventListener.onEvent(true, response);
                });
    }

    /**
     *
     * @param params Query prams
     * @param charset ex) UTF-8
     * @return
     */
    private String createQuery(HashMap<String, String> params, String charset){
        if (params == null || params.size() == 0) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        try {
            for (String key : params.keySet()) {
                String value = params.get(key);
                if (key != null && value != null) {
                    builder.append(String.format("%s=%s&", URLEncoder.encode(key, charset), URLEncoder.encode(value, charset)));
                }
            }
        }
        catch (UnsupportedEncodingException e) {
            Log.d(TAG, "Fail to generate query");
        }

        // 마지막의 & 를 삭제
        int length = builder.length();
        if (length > 0) {
            builder.deleteCharAt(length - 1);
        }

        return builder.toString();
    }

    /**
     * creating url
     * @param tailURL
     * @return
     */
    private String createURL(@NonNull String tailURL){
        if (!tailURL.startsWith("http")) {
            if (tailURL.startsWith("/")) {
                return headerURL + tailURL;
            }
            else {
                return  headerURL + "/" + tailURL;
            }
        }
        return tailURL;
    }

    /**
     * send params using the OutputStream
     * @param out
     * @param params
     * @throws IOException
     */
    private void writeStream(@NonNull OutputStream out, @NonNull String params) throws IOException {
        byte[] outputInBytes = params.getBytes(StandardCharsets.UTF_8);
        out.write(outputInBytes);
        out.flush();
        out.close();
    }

    /**
     * read result values using the InputStream
     * @param in
     * @return
     * @throws IOException
     */
    @NonNull
    private String readStream(InputStream in) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        String line;
        StringBuffer response = new StringBuffer();
        while ((line = br.readLine()) != null) {
            response.append(line);
            response.append('\r');
        }
        br.close();

        String res = response.toString();
        res = res.trim();
        return res;
    }
}






/*
public void asyncGet(String tailURL, EventListener eventListener) {
        this.eventListener = eventListener;

        final AsyncGet task = new AsyncGet();
        task.execute(tailURL);
    }

    private class AsyncGet extends AsyncTask<String, String, Boolean> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(String... params) {
            try {
                response = getTask(params[0]);
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
            isSuccess = true;
            return true;
        }
        @Override
        protected void onProgressUpdate(String... progress) {
            super.onProgressUpdate(progress);
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            eventListener.onEvent(isSuccess, response);
        }
    }
 */