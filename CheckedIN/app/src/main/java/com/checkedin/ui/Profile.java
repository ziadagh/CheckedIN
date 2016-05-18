package com.checkedin.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.checkedin.Classes.AgeCalculator;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


/**
 * The Class Profile is the Fragment class that shows Profile of a user which
 * includes basic details of user and a few images of user.
 */
public class Profile extends CustomFragment {
    TextView fname, lname, age, currentCity, bio;
    ImageView pp, img2, img3;

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_profile, null);
        getActivity().getActionBar().setTitle(getShared().getString("username", ""));
        final ImageButton edit = (ImageButton) v.findViewById(R.id.editButton);
        final String username = getShared().getString("username", "");
        fname = (TextView) v.findViewById(R.id.profile_fname);
        lname = (TextView) v.findViewById(R.id.profile_lname);
        age = (TextView) v.findViewById(R.id.profile_age);
        bio = (TextView) v.findViewById(R.id.bio);
        currentCity = (TextView) v.findViewById(R.id.current_city);

        pp = (ImageView) v.findViewById(R.id.img1);
        img2 = (ImageView) v.findViewById(R.id.img2);
        img3 = (ImageView) v.findViewById(R.id.img3);

        EndpointsGetInfo task = new EndpointsGetInfo();
        task.execute(username);

        ImagesAsync task2 = new ImagesAsync();
        task2.execute(username);

        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditProfile fragment = new EditProfile();
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                FragmentTransaction ft = fragmentManager.beginTransaction();
                Bundle bd= new Bundle();
                bd.putString("username",username);
                fragment.setArguments(bd);
                ft.replace(R.id.content_frame, fragment);
                ft.addToBackStack("Profile");
                ft.commit();
            }
        });
        return v;
    }

    public class EndpointsGetInfo extends AsyncTask<String, Void, User> {
        private UserApi myApiService = null;
        User theUser = new User();

        @Override
        protected User doInBackground(String... params) {
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
                    String username = params[0];
                    theUser = myApiService.getUserByUsername(username).execute();
                    break;
                } catch (IOException e) {
                }
            }
            return theUser;
        }

        @Override
        protected void onPostExecute(User result) {
            fname.setText(result.getFname());
            lname.setText(result.getLname());
            String user_age = "";
            SimpleDateFormat sdf = new SimpleDateFormat("dd-mm-yyyy");
            try {
                Date d = sdf.parse(result.getDob());
                user_age += AgeCalculator.calculateAge(d);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            age.setText(user_age.substring(0, 2));
            if (!(result.getBio() == null)) {
                if (!result.getBio().equals("null"))
                    bio.setText(result.getBio());
                else bio.setText("");
                currentCity.setText(result.getCurrentCity());
            }
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

    public class ImagesAsync extends AsyncTask<String, Void, ArrayList<String>> {
        private UserApi myApiService = null;

        @Override
        protected ArrayList<String> doInBackground(String... params) {
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
            ArrayList<String> images = new ArrayList<>();
            User user;
            while (true) {
                try {
                    user = myApiService.getImage(params[0]).execute();
                    break;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (user != null) {
                images.add(user.getProfilepic());
                images.add(user.getImg2());
                images.add(user.getImg3());
            }
            return images;
        }

        @Override
        protected void onPostExecute(ArrayList<String> strings) {
            super.onPostExecute(strings);
            if (strings == null) setDefaultImage();
            else if (strings.get(0) == null) {
                setDefaultImage();
            } else {
                pp.setImageBitmap(BitmapFactory.decodeFile(strings.get(0)));
                img2.setImageBitmap(BitmapFactory.decodeFile(strings.get(1)));
                img3.setImageBitmap(BitmapFactory.decodeFile(strings.get(2)));
            }
        }
    }


    public void setDefaultImage() {
        pp.setImageResource(R.drawable.default_user);
        img2.setImageResource(R.drawable.default_user);
        img3.setImageResource(R.drawable.default_user);
    }
}
