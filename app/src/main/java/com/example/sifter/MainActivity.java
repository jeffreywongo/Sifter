package com.example.sifter;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {

    private static EditText email;
    private static EditText password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        email = (EditText) findViewById(R.id.email);
//        password = (EditText) findViewById(R.id.password);
    }

    public void onClickGmail(View view) {
        Log.d("MESSAGE", "Gmail Button intent sent");
        Intent intent = new Intent(this, GmailActivity.class);
        startActivity(intent);
    }

//    public void onClickOtherMails(View view) {
//        Intent intent = new Intent(this, OtherMailsActivity.class);
//        startActivity(intent);
//    }

    public static String getEmail() {
        return email.getText().toString();
    }

    public static String getPassowrd() {
        return password.getText().toString();
    }

}
