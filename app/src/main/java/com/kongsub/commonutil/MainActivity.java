package com.kongsub.commonutil;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.kongsub.commonutil.common.EventListener;
import com.kongsub.commonutil.databinding.ActivityMainBinding;
import com.kongsub.commonutil.file.FileDownloadHelper;
import com.kongsub.commonutil.network.NetworkHelper;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {
    private final String URL = "http://10.0.2.2:8080";
    private final String FILE_DOWN_URL = "/pdf.pdf";
    private final int URL_RESULT_CODE = 4020;
    private ActivityMainBinding binding;

    //get post
    NetworkHelper networkHelper = new NetworkHelper(URL);

    //file
    FileDownloadHelper fileDownloadHelper = new FileDownloadHelper(URL + FILE_DOWN_URL, URL_RESULT_CODE);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater()); // 1
        setContentView(binding.getRoot()); // 2

        binding.get.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                networkHelper.BackgroundGet("/api/get", new EventListener() {
                    @Override
                    public void onEvent(boolean isSuccess, String resultMessage) {
                        if(isSuccess)
                            binding.getResult.setText(resultMessage);
                    }
                });
            }
        });

        binding.post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JSONObject json = new JSONObject();
                try {
                    json.put("id", binding.postData.getText());
                    networkHelper.BackgroundPost("/api/post", json, new EventListener() {
                        @Override
                        public void onEvent(boolean isSuccess, String resultMessage) {
                            if(isSuccess)
                                binding.postResult.setText(resultMessage);
                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        //1. setting activity
        fileDownloadHelper.setDownActivity(this);
        binding.fileDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //2. create file
                fileDownloadHelper.createFile(MainActivity.this, "test", FileDownloadHelper.FILE_TYPE.PDF);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        switch (requestCode) {
            case URL_RESULT_CODE:
                Uri fileUri = intent.getData();
                fileDownloadHelper.BackgroundDownloadFromUri(fileUri, new EventListener() {
                    @Override
                    public void onEvent(boolean isSuccess, String resultMessage) {
                        Toast.makeText(getApplicationContext(), "file download complete!", Toast.LENGTH_SHORT).show();;
                    }
                });
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + requestCode);
        }
    }
}