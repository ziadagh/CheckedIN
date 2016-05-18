package com.checkedin.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Pair;
import android.view.View;
import android.widget.Button;

import com.checkedin.R;

import android.app.Activity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import com.example.ibrahim.myapplication.backend.userApi.UserApi;
import com.example.ibrahim.myapplication.backend.userApi.model.User;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.googleapis.services.AbstractGoogleClientRequest;
import com.google.api.client.googleapis.services.GoogleClientRequestInitializer;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class Signup extends Activity {
    protected EditText username;
    private EditText password;
    public static final String MyPreferences = "MyPrefs";
    private EditText email, fname, lname, dob, confPassword, currentcity;
    protected String enteredUsername;
    String result;
    String userGender = "";
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        username = (EditText) findViewById(R.id.et_username);
        password = (EditText) findViewById(R.id.et_password);
        fname = (EditText) findViewById(R.id.et_fname);
        lname = (EditText) findViewById(R.id.et_lname);
        dob = (EditText) findViewById(R.id.et_birthday);
        confPassword = (EditText) findViewById(R.id.et_confpassword);
        currentcity = (EditText) findViewById(R.id.et_currentCity);
        email = (EditText) findViewById(R.id.et_email);
        Button signUpButton = (Button) findViewById(R.id.signup_page_button);

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enteredUsername = username.getText().toString();
                String enteredPassword = password.getText().toString();
                String enteredEmail = email.getText().toString();
                String enteredFname = fname.getText().toString();
                String enteredLname = lname.getText().toString();
                String enteredconfPassword = confPassword.getText().toString();
                String enteredCurrentCity = currentcity.getText().toString();
                String enteredDob = dob.getText().toString();

                if (enteredUsername.equals("") || enteredPassword.equals("") || enteredEmail.equals("")) {
                    Toast.makeText(Signup.this, "Username or password or email must be filled", Toast.LENGTH_LONG).show();
                    return;
                }
                if (!enteredPassword.equals(enteredconfPassword)) {
                    Toast.makeText(Signup.this, "Please make sure your password and confirm password fields match", Toast.LENGTH_LONG).show();
                    return;
                }
                if (enteredUsername.length() <= 1 || enteredPassword.length() <= 1) {
                    Toast.makeText(Signup.this, "Username or password length must be greater than one", Toast.LENGTH_LONG).show();
                    return;
                }
                User user = new User();
                user.setUsername(enteredUsername);
                user.setPassword(enteredPassword);
                user.setFname(enteredFname);
                user.setLname(enteredLname);
                user.setDob(enteredDob);
                user.setSex(userGender);
                user.setCurrentCity(enteredCurrentCity);
                user.setEmail(enteredEmail);
                EndpointsAsyncTask task = new EndpointsAsyncTask();
                task.execute(new Pair<Context, User>(v.getContext(), user));
                try {
                    result = task.get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
                if (result.equals(enteredUsername)) {
                    Toast.makeText(v.getContext(), "Successfully Signed up \n Welcome " + result, Toast.LENGTH_LONG).show();
                    sharedPreferences = getSharedPreferences(MyPreferences, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("username", user.getUsername());
                    editor.putString("fname", user.getFname());
                    editor.putString("lname", user.getLname());
                    editor.putString("dob", user.getDob());
                    editor.putString("gender", user.getSex());
                    editor.putString("password", user.getPassword());
                    editor.putString("email", user.getEmail());
                    editor.putString("currentCity", user.getCurrentCity());
                    editor.commit();

                    startActivity(new Intent(v.getContext(), OnSignup.class));
                } else
                    Toast.makeText(v.getContext(), "Failed to Signed up \n Try changing Username or Email ", Toast.LENGTH_LONG).show();
            }
        });
    }

    public String RadioButtonClicked(View view) {
        boolean checked = ((RadioButton) view).isChecked();
        switch (view.getId()) {
            case R.id.radioFemale:
                if (checked)
                    userGender = "female";
                break;
            case R.id.radioMale:
                if (checked)
                    userGender = "male";
                break;
        }
        return userGender;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        return super.onOptionsItemSelected(item);
    }

    public static class EndpointsAsyncTask extends AsyncTask<Pair<Context, User>, Void, String> {
        private static UserApi myApiService = null;

        @Override
        protected String doInBackground(Pair<Context, User>... params) {
            if (myApiService == null) {  // Only do this once
                UserApi.Builder builder = new UserApi.Builder(AndroidHttp.newCompatibleTransport(),
                        new AndroidJsonFactory(), null)
                        .setRootUrl("https://gleaming-abacus-123320.appspot.com/_ah/api")
                        .setGoogleClientRequestInitializer(new GoogleClientRequestInitializer() {
                            @Override
                            public void initialize(AbstractGoogleClientRequest<?> abstractGoogleClientRequest) throws IOException {
                                abstractGoogleClientRequest.setDisableGZipContent(true);
                            }
                        });
                myApiService = builder.build();
            }
            try {
                User user = params[0].second;
                String result = myApiService.insertUser(user).execute().getUsername();
                return result;
            } catch (IOException e) {
                return e.getMessage();
            }
        }

        @Override
        protected void onPostExecute(String result) {
        }
    }
}