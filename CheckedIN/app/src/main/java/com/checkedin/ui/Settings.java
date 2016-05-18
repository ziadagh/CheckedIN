package com.checkedin.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import com.checkedin.Login;
import com.checkedin.R;
import com.checkedin.custom.CustomFragment;
import com.example.ibrahim.myapplication.backend.userApi.UserApi;
import com.example.ibrahim.myapplication.backend.userApi.model.User;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.googleapis.services.AbstractGoogleClientRequest;
import com.google.api.client.googleapis.services.GoogleClientRequestInitializer;

import java.io.IOException;

/**
 * The Class Settings is the Fragment class that shows a few options related to
 * Settings and it's launched when user taps on Settings option in left
 * navigation panel.
 */
public class Settings extends CustomFragment implements View.OnClickListener {
    Button logout, delete;
    protected static UserApi api;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_settings, null);
        logout = (Button) v.findViewById(R.id.settings_logout_linearlayout);
        delete = (Button) v.findViewById(R.id.settings_delete_linearlayout);

        logout.setOnClickListener(this);
        delete.setOnClickListener(this);
        return v;
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.settings_delete_linearlayout:
                DeleteAccountAsyncTask task = new DeleteAccountAsyncTask();
                task.execute(getShared().getString("username", ""));
                Intent i = new Intent(getActivity(), Login.class);
                startActivity(i);
                break;
            case R.id.settings_logout_linearlayout:
                SharedPreferences.Editor editor = getShared().edit();
                editor.clear();
                editor.commit();
                this.startActivity(new Intent(this.getContext(), Login.class));
                this.getActivity().finish();
        }
    }

    public SharedPreferences getShared() {
        SharedPreferences shared;
        if (Login.sharedPreferences != null) {
            shared = this.getActivity().getSharedPreferences(Login.MyPreferences, Context.MODE_PRIVATE);
        } else {
            shared = this.getActivity().getSharedPreferences(Signup.MyPreferences, Context.MODE_PRIVATE);
        }
        return shared;
    }

    public class DeleteAccountAsyncTask extends AsyncTask<String, Void, Void> {
        private UserApi myApiService = null;

        @Override
        protected Void doInBackground(String... params) {
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
                    myApiService.deleteUser(params[0]).execute();
                    break;
                } catch (IOException e) {
                    Log.d("Status", "Delete Account Failed");
                }
            }
            return null;
        }
    }
}