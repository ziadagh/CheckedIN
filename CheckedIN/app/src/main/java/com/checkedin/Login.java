package com.checkedin;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.checkedin.custom.CustomActivity;
import com.checkedin.ui.Signup;
import com.example.ibrahim.myapplication.backend.userApi.UserApi;
import com.example.ibrahim.myapplication.backend.userApi.model.User;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.googleapis.services.AbstractGoogleClientRequest;
import com.google.api.client.googleapis.services.GoogleClientRequestInitializer;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class Login extends CustomActivity implements View.OnClickListener {
    protected EditText username;
    private EditText password;
    protected String enteredUsername, enteredPassword, result;
    public static final String MyPreferences = "MyPrefs";
    public static SharedPreferences sharedPreferences;
    public static User user;

    private ProgressBar loginProgress;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        username = (EditText) findViewById(R.id.etUsername);
        password = (EditText) findViewById(R.id.etPassword);
        Button loginButton = (Button) findViewById(R.id.login_button);
        TextView registerButton = (TextView) findViewById(R.id.signup_button);

        // initialize progress bar
        loginProgress = (ProgressBar) findViewById(R.id.login_progressBar);
        loginProgress.setVisibility(View.GONE);


        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enteredUsername = username.getText().toString();
                enteredPassword = password.getText().toString();
                if (enteredUsername.equals("") || enteredPassword.equals("")) {
                    Toast.makeText(v.getContext(), "Username or password must be filled", Toast.LENGTH_LONG).show();
                    return;
                }
                if (enteredUsername.length() <= 1 || enteredPassword.length() <= 1) {
                    Toast.makeText(v.getContext(), "Username or password length must be greater than one", Toast.LENGTH_LONG).show();
                    return;
                }
                loginProgress.setVisibility(View.VISIBLE);

                User user = new User();
                user.setUsername(enteredUsername);
                user.setPassword(enteredPassword);
                EndpointsAsyncTaskLogin login = new EndpointsAsyncTaskLogin();
                    try {
                        result = login.execute(user).get();
                     } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                if (result.equals("Success")) {
                    Toast.makeText(v.getContext(), "Welcome " + user.getFname(), Toast.LENGTH_LONG).show();
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
                    loginProgress.setVisibility(View.GONE);
                    startActivity(new Intent(v.getContext(), MainActivity.class));
                    finish();
                } else {
                    Toast.makeText(v.getContext(), "Login failed, check Username or Password", Toast.LENGTH_LONG).show();
                    loginProgress.setVisibility(View.GONE);
                }
            }
        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Login.this, Signup.class);
                startActivity(intent);
                finish();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        // getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //no inspection SimplifiableIfStatement
        //if (id == R.id.action_settings) {
        //  return true;
        //}
        return super.onOptionsItemSelected(item);
    }

    public class EndpointsAsyncTaskLogin extends AsyncTask<User, Void, String> {
        private UserApi myApiService = null;


        @Override
        protected String doInBackground(User... params) {
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
            while (true) {
                try {
                    User user = params[0];
                    User u = myApiService.getUserByUsername(user.getUsername()).execute();
                    if (u != null && u.getPassword().equals(user.getPassword())) {
                        params[0].setFname(u.getFname());
                        params[0].setLname(u.getLname());
                        params[0].setEmail(u.getEmail());
                        params[0].setId(u.getId());
                        params[0].setBio(u.getBio());
                        params[0].setSex(u.getSex());
                        params[0].setDob(u.getDob());
                        Login.user = u;
                        return "Success";
                    } else return "Fail";
                } catch (IOException e) {
                }
            }
        }
    }
}