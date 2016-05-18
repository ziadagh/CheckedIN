package com.checkedin.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.checkedin.Classes.AgeCalculator;
import com.checkedin.Login;
import com.checkedin.MainActivity;
import com.checkedin.R;
import com.checkedin.custom.CustomFragment;
import com.example.ibrahim.myapplication.backend.messaging.Messaging;
import com.example.ibrahim.myapplication.backend.registration.Registration;
import com.example.ibrahim.myapplication.backend.registration.model.RegistrationRecord;
import com.example.ibrahim.myapplication.backend.userApi.UserApi;
import com.example.ibrahim.myapplication.backend.userApi.model.User;
import com.example.ibrahim.myapplication.backend.userApi.model.UserCollection;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.googleapis.services.AbstractGoogleClientRequest;
import com.google.api.client.googleapis.services.GoogleClientRequestInitializer;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class ProfileUser extends CustomFragment {
    TextView fname, lname, username, age, currentCity, bio;
    static ImageView pp, img2, img3;
    static ArrayList<String> imgs;
    User user;
    static View v;
    SharedPreferences pref;
    Button user1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final String user_name = getArguments().getString("username");
        final View v = inflater.inflate(R.layout.fragment_profile_user, null);
        getActivity().getActionBar().setTitle(user_name);

        user1 = (Button) v.findViewById(R.id.profileuser_like_button);
        fname = (TextView) v.findViewById(R.id.profileuser_fname);
        lname = (TextView) v.findViewById(R.id.profile_user_lname);
        bio = (TextView) v.findViewById(R.id.profileuser_bio);
        username = (TextView) v.findViewById(R.id.profileuser_username);
        age = (TextView) v.findViewById(R.id.profile_user_age);
        currentCity = (TextView) v.findViewById(R.id.profileuser_current_city);

        pp = (ImageView) v.findViewById(R.id.img1);
        img2 = (ImageView) v.findViewById(R.id.img2);
        img3 = (ImageView) v.findViewById(R.id.img3);

        EndpointsGetInfo task1 = new EndpointsGetInfo();
        task1.execute(user_name);
        EndpointsCheckLiked task2 = new EndpointsCheckLiked();
        task2.execute(new Pair<>(getShared().getString("username", ""), user_name));

        ImagesAsync task3 = new ImagesAsync();
        task3.execute(user_name);

        if (user1.isClickable()) {
            user1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    user1.setBackgroundDrawable(getResources().getDrawable(R.drawable.btn_liked_red));
                    EndpointsLike task = new EndpointsLike();
                    task.execute(new Pair<>(getShared().getString("username", ""), user_name));
                    LikeAsyncTask task1 = new LikeAsyncTask();
                    task1.execute(new Pair<>(user_name, getShared().getString("username", "")));
                }
            });
        }
        return v;
    }

    /**
     * This task stores that user a liked user b in database and checks if there's a match
     */
    public class EndpointsLike extends AsyncTask<Pair<String, String>, Void, String> {
        private UserApi myApiService = null;
        String likedUser;

        @Override
        protected String doInBackground(Pair<String, String>... params) {
            if (myApiService == null) {  // Only do this once
                UserApi.Builder builder = new UserApi.Builder(AndroidHttp.newCompatibleTransport(), new AndroidJsonFactory(), null)
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
                    String username = params[0].first;
                    likedUser = params[0].second;
                    myApiService.insertUserLiked(likedUser, username).execute();
                    UserCollection likedUserLikedMe = myApiService.getUsersILiked(likedUser).execute();
                    if (likedUserLikedMe != null) {
                        List<User> list = likedUserLikedMe.getItems();
                        if (list != null) {
                            User user = myApiService.getUserByUsername(username).execute();
                            for (int i = 0; i < list.size(); i++) {
                                if (list.get(i).getUsername().equals(user.getUsername()))
                                    return "Match";
                            }
                        }
                    }
                    break;
                } catch (IOException e) {
                    return e.getMessage();
                }
            }
            return "Success";
        }

        @Override
        protected void onPostExecute(String result) {
            if (result.equals("Match")) {
                Match fragment = new Match();
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                Bundle args = new Bundle();
                args.putString("username", likedUser);
                fragment.setArguments(args);
                fragmentTransaction.replace(R.id.content_frame, fragment);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
            }
        }
    }

    public class EndpointsGetInfo extends AsyncTask<String, Void, User> {
        private UserApi myApiService = null;

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
            User user;
            while (true) {
                try {
                    String username = params[0];
                    user = myApiService.getUserByUsername(username).execute();
                    break;
                } catch (IOException e) {
                }
            }
            return user;
        }

        @Override
        protected void onPostExecute(User result) {
            username.setText(result.getUsername());
            fname.setText(result.getFname());
            lname.setText(result.getLname());
            if (result.getBio().equals("null")) bio.setText("");
            else bio.setText(result.getBio());
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
            String user_age = "";
            try {
                Date d = sdf.parse(result.getDob());
                user_age += AgeCalculator.calculateAge(d);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            age.setText(user_age.substring(0, 2) + "");
            currentCity.setText(result.getCurrentCity());
        }
    }

    public SharedPreferences getShared() {
        if (Login.sharedPreferences != null) {
            return this.getActivity().getSharedPreferences(Login.MyPreferences, Context.MODE_PRIVATE);
        } else {
            return this.getActivity().getSharedPreferences(Signup.MyPreferences, Context.MODE_PRIVATE);
        }
    }

    public class EndpointsCheckLiked extends AsyncTask<Pair<String, String>, Void, String> {
        private UserApi myApiService = null;

        @Override
        protected String doInBackground(Pair<String, String>... params) {
            if (myApiService == null) {
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
                    String username = params[0].first;
                    String likedUser = params[0].second;
                    UserCollection userILiked = myApiService.getUsersILiked(username).execute();
                    if (userILiked != null) {
                        List<User> list = userILiked.getItems();
                        User user = myApiService.getUserByUsername(likedUser).execute();
                        if (list == null) return "Not Liked";
                        for (int i = 0; i < list.size(); i++) {
                            if (list.get(i).getUsername().equals(user.getUsername()))
                                return "Liked";
                        }
                    }
                    return "Not Liked";
                } catch (IOException e) {
                }
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (s.equals("Liked")) {
                user1.setBackgroundDrawable(getResources().getDrawable(R.drawable.btn_liked_red));
                user1.setClickable(false);
            }
        }
    }

    public static class LikeAsyncTask extends AsyncTask<Pair<String, String>, Void, String> {
        private static Messaging messaging = null;
        private static Registration registration = null;

        @Override
        protected String doInBackground(Pair<String, String>... params) {
            if (registration == null) {  // Only do this once
                Registration.Builder builder = new Registration.Builder(AndroidHttp.newCompatibleTransport(),
                        new AndroidJsonFactory(), null)
                        .setRootUrl("https://2-dot-gleaming-abacus-123320.appspot.com/_ah/api")
                        .setGoogleClientRequestInitializer(new GoogleClientRequestInitializer() {
                            @Override
                            public void initialize(AbstractGoogleClientRequest<?> abstractGoogleClientRequest) throws IOException {
                                abstractGoogleClientRequest.setDisableGZipContent(true);
                            }
                        });
                registration = builder.build();
            }
            if (messaging == null) {  // Only do this once
                Messaging.Builder builder = new Messaging.Builder(AndroidHttp.newCompatibleTransport(),
                        new AndroidJsonFactory(), null)
                        .setRootUrl("https://2-dot-gleaming-abacus-123320.appspot.com/_ah/api")
                        .setGoogleClientRequestInitializer(new GoogleClientRequestInitializer() {
                            @Override
                            public void initialize(AbstractGoogleClientRequest<?> abstractGoogleClientRequest) throws IOException {
                                abstractGoogleClientRequest.setDisableGZipContent(true);
                            }
                        });
                messaging = builder.build();
            }
            RegistrationRecord regId = null;
            String message = null;
            while (true) {
                try {
                    String username = params[0].first;
                    regId = registration.getRegistrationID(username).execute();
                    String user = params[0].second;
                    message = user + " Liked You";
                    messaging.sendMessageTo(message, regId.getRegId()).execute();
                    break;
                } catch (IOException e) {
                }
            }
            return message;
        }
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
            User user = null;
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
            if (strings == null){
                setDefaultImage();
            }else if(strings.get(0)==null){
                setDefaultImage();
            }else if(strings.get(0).equals("null")||strings.get(0).equals("")){
                setDefaultImage();
            }
            else {
                pp.setImageBitmap(BitmapFactory.decodeFile(strings.get(0)));
                img2.setImageBitmap(BitmapFactory.decodeFile(strings.get(1)));
                img3.setImageBitmap(BitmapFactory.decodeFile(strings.get(2)));
            }
        }
    }

    public static void setDefaultImage() {
        pp.setImageResource(R.drawable.default_user);
        img2.setImageResource(R.drawable.default_user);
        img3.setImageResource(R.drawable.default_user);
    }
}