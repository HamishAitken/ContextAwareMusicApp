package com.example.contextawaremusicapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

public class CallbackActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_callback); // Link to your layout file if needed

        // Handle the intent from Spotify redirect
        Intent intent = getIntent();
        Uri data = intent.getData();

        if (data != null && data.toString().startsWith("myapp://callback")) {
            // Extract the authorization code or access token from the URI
            // The exact data you need depends on the Spotify authorization flow you're using
            String code = data.getQueryParameter("code"); // For Authorization Code Flow
            // Or
            String accessToken = data.getQueryParameter("access_token"); // For Implicit Grant Flow

            // TODO: Use the code or access token to make further requests to Spotify

            // Optionally, start another activity or handle the result directly here
        }
    }
}