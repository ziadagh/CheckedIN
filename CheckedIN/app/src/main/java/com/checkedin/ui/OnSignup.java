package com.checkedin.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.checkedin.MainActivity;
import com.checkedin.R;

public class OnSignup extends Activity implements View.OnClickListener {
    Button checkin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_on_signup);
        //profile = (Button) findViewById(R.id.onsignup_editprofile_button);
        checkin = (Button) findViewById(R.id.onsignup_checkin_button);

        //profile.setOnClickListener(this);
        checkin.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.onsignup_checkin_button:
                startActivity(new Intent(this, MainActivity.class));
                finish();
                break;
            /*case R.id.onsignup_editprofile_button:
                Intent i = new Intent(OnSignup.this, Profile.class);
                startActivity(i);*/
        }
    }
}
