package com.kongsub.commonutil;

import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.kongsub.commonutil.network.NetworkHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {
    NetworkHelper networkHelper = new NetworkHelper("http://10.0.2.2:8080");
    Button get;
    TextView getResult;
    String getResultString;

    Button post;
    EditText postData;
    TextView postResult;
    String postResultString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        get = findViewById(R.id.get);
        getResult = findViewById(R.id.get_result);

        post = findViewById(R.id.post);
        postData = findViewById(R.id.post_data);
        postResult = findViewById(R.id.post_result);

        get.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final GET getTask = new GET();
                getTask.execute();
            }
        });

        post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final POST postTask = new POST();
                postTask.execute();
            }
        });
    }

    private class GET extends AsyncTask<String, String, Boolean> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(String... params) {
            try {
                getResultString = networkHelper.get("api/get");
            } catch (IOException e) {
                e.printStackTrace();
            }
            return true;
        }
        @Override
        protected void onProgressUpdate(String... progress) {
            super.onProgressUpdate(progress);
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            getResult.setText(getResultString);
        }
    }

    private class POST extends AsyncTask<String, String, Boolean> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(String... params) {
            try {
                JSONObject json = new JSONObject();
                json.put("id", postData.getText());

                HashMap<String, String> hm = new HashMap<>();
                hm.put("id", String.valueOf(postData.getText()));

                postResultString = networkHelper.post("api/post", hm);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return true;
        }
        @Override
        protected void onProgressUpdate(String... progress) {
            super.onProgressUpdate(progress);
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            postResult.setText(postResultString);
        }
    }

}