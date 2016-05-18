package com.checkedin.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

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
import java.util.ArrayList;

/**
 * The Class Match is the Fragment class that shows interface for a Match between two users.
 */
public class Match extends CustomFragment {
    TextView message;
    ImageView img1,img2;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_match, null);
        final String user_name = getArguments().getString("username");
        final LinearLayout chat = (LinearLayout) v.findViewById(R.id.btnConversation);
        img1=(ImageView) v.findViewById(R.id.user);
        img2=(ImageView) v.findViewById(R.id.match);
        message = (TextView) v.findViewById(R.id.match_text);
        message.setText("You and " + user_name + " liked each other.");
        ImagesAsync task=new ImagesAsync();
        task.execute(new Pair<>(1,getShared().getString("username","")));
        ImagesAsync task1=new ImagesAsync();
        task1.execute(new Pair<>(2,user_name));
        chat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Chat fragment = new Chat();
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                Bundle args = new Bundle();
                args.putString("username", user_name);
                fragment.setArguments(args);
                fragmentTransaction.replace(R.id.content_frame, fragment);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
            }
        });

        final LinearLayout surf = (LinearLayout) v.findViewById(R.id.btnSurf);
        surf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ListAvailable fragment = new ListAvailable();
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.content_frame, fragment);
                Bundle args = new Bundle();
                args.putString("username", user_name);
                fragment.setArguments(args);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
            }
        });
        return v;
    }

    public class ImagesAsync extends AsyncTask<Pair<Integer,String>, Void, String> {
        private UserApi myApiService = null;
        Integer i;
        @Override
        protected String doInBackground(Pair<Integer,String>... params) {
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
            User user;
           i=params[0].first;
            while (true) {
                try {
                    user = myApiService.getProfilePic(params[0].second).execute();
                    break;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return user.getProfilepic();
        }

        @Override
        protected void onPostExecute(String s) {
            if(i==1)
            img1.setImageBitmap(BitmapFactory.decodeFile(s));
            else
                img2.setImageBitmap(BitmapFactory.decodeFile(s));
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

}