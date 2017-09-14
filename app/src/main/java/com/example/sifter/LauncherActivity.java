package com.example.sifter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class LauncherActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);
        Intent intent;
        SharedPreferences pref =
                getSharedPreferences("my_preferences", Context.MODE_PRIVATE);
        Boolean loggedIn = pref.getBoolean("isLoggedIn", false);
        if (loggedIn) {
            intent = new Intent(this, GmailActivity.class);
        } else {
            intent = new Intent(this, MainActivity.class);
        }
        startActivity(intent);
    }
}
