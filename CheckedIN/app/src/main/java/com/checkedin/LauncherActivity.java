package com.checkedin;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.checkedin.ui.Signup;

public class LauncherActivity extends AppCompatActivity {
    SharedPreferences SharedPref;

    public SharedPreferences getShared() {
        SharedPreferences shared;
        if (Login.sharedPreferences != null) {
            shared = this.getSharedPreferences(Login.MyPreferences, Context.MODE_PRIVATE);
        } else {
            shared = this.getSharedPreferences(Signup.MyPreferences, Context.MODE_PRIVATE);
        }
        return shared;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPref = getShared();
        Log.w("test", SharedPref.getString("username", "") + SharedPref.getString("username", "").length());
        if (SharedPref.getString("username", "").length() == 0) {
        Intent intent = new Intent(this, SplashScreen.class);
        startActivity(intent);
        finish();
        } else {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }
}