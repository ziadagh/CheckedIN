package com.checkedin.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

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

public class EditProfile extends CustomFragment {
    EditText fname, dob, currentCity, bio, lname;
    String efname, edob, ecurrentCity, ebio, elname;
    ImageView img1, img2, img3;
    static View v;
    String encodedImg1, encodedImg2, encodedImg3;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final String user_name = getArguments().getString("username");
        v = inflater.inflate(R.layout.fragment_profile_edit, null);
        getActivity().getActionBar().setTitle(user_name);
        Button edit = (Button) v.findViewById(R.id.editprofile_save_button);

        fname = (EditText) v.findViewById(R.id.editprofile_fname);
        lname = (EditText) v.findViewById(R.id.editprofile_lname);
        dob = (EditText) v.findViewById(R.id.editprofile_dob);
        bio = (EditText) v.findViewById(R.id.editprofile_bio);
        currentCity = (EditText) v.findViewById(R.id.editprofile_currentcity);

        final SharedPreferences pref = getShared();
        fname.setHint(pref.getString("fname", ""));
        lname.setHint(pref.getString("lname", ""));
        dob.setHint(pref.getString("dob", ""));
        bio.setHint(pref.getString("bio", ""));
        currentCity.setHint(pref.getString("currentCity", ""));

        img1 = (ImageView) v.findViewById(R.id.img1);
        img1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(galleryIntent, 1);
            }
        });
        img2 = (ImageView) v.findViewById(R.id.img2);
        img2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(galleryIntent, 2);
            }
        });
        img3 = (ImageView) v.findViewById(R.id.img3);
        img3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(galleryIntent, 3);
            }
        });
        ImagesAsync task2 = new ImagesAsync();
        task2.execute(user_name);

        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                efname = fname.getText().toString().trim();
                edob = dob.getText().toString().trim();
                ecurrentCity = currentCity.getText().toString().trim();
                ebio = bio.getText().toString().trim();
                elname = lname.getText().toString().trim();

                if (efname.equals("") || efname.equals("null"))
                    efname = pref.getString("fname", "");
                if (elname.equals("") || elname.equals("null"))
                    elname = pref.getString("lname", "");
                if (ebio.equals("") || ebio.equals("null")) ebio = pref.getString("bio", "");
                if (edob.equals("") || edob.equals("null")) edob = pref.getString("dob", "");
                if (ecurrentCity.equals("") || ecurrentCity.equals("null"))
                    ecurrentCity = pref.getString("currentCity", "");

                SharedPreferences.Editor editor = pref.edit();
                editor.putString("fname", efname);
                editor.putString("lname", elname);
                editor.putString("bio", ebio);
                editor.putString("dob", edob);
                editor.putString("currentCity", ecurrentCity);
                editor.commit();

                User user = new User();
                user.setUsername(pref.getString("username", ""));
                user.setPassword(pref.getString("password", ""));
                user.setDob(pref.getString("dob", ""));
                user.setSex(pref.getString("gender", ""));
                user.setBio(pref.getString("bio", ""));
                user.setCurrentCity(pref.getString("currentCity", ""));
                user.setFname(pref.getString("fname", ""));
                user.setLname(pref.getString("lname", ""));
                user.setEmail(pref.getString("email", ""));

                EndpointsEditProfile task = new EndpointsEditProfile();
                task.execute(user);

                InsertImagesAsync task1 = new InsertImagesAsync();
                ArrayList<String> encodedImgs = new ArrayList<>();
                encodedImgs.add(encodedImg1);
                encodedImgs.add(encodedImg2);
                encodedImgs.add(encodedImg3);
                task1.execute(new Pair<>(encodedImgs, getShared().getString("username", "")));
                editor.putString("profilepic", encodedImg1);
                editor.putString("image2", encodedImg2);
                editor.putString("image3", encodedImg3);
                editor.commit();

                Profile fragment = new Profile();
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

                fragmentTransaction.replace(R.id.content_frame, fragment);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
            }
        });
        return v;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            // When an Image is picked
            if (requestCode == 1 && resultCode == getActivity().RESULT_OK && data != null) {
                // Get the Image from data
                Uri selectedImage = data.getData();
                String[] filePathColumn = {MediaStore.Images.Media.DATA};

                Cursor cursor = getActivity().getContentResolver().query(selectedImage, filePathColumn, null, null, null);
                cursor.moveToFirst();

                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                encodedImg1 = cursor.getString(columnIndex);
                cursor.close();

                ImageView imgView = (ImageView) v.findViewById(R.id.img1);
                imgView.setImageBitmap(BitmapFactory.decodeFile(encodedImg1));
            } else if (requestCode == 2 && resultCode == getActivity().RESULT_OK && null != data) {
                Uri selectedImage = data.getData();
                String[] filePathColumn = {MediaStore.Images.Media.DATA};

                Cursor cursor = getActivity().getContentResolver().query(selectedImage, filePathColumn, null, null, null);
                cursor.moveToFirst();

                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                encodedImg2 = cursor.getString(columnIndex);
                cursor.close();

                ImageView imgView = (ImageView) v.findViewById(R.id.img2);
                imgView.setImageBitmap(BitmapFactory.decodeFile(encodedImg2));
            } else if (requestCode == 3 && resultCode == getActivity().RESULT_OK && null != data) {
                Uri selectedImage = data.getData();
                String[] filePathColumn = {MediaStore.Images.Media.DATA};

                Cursor cursor = getActivity().getContentResolver().query(selectedImage, filePathColumn, null, null, null);
                cursor.moveToFirst();

                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                encodedImg3 = cursor.getString(columnIndex);
                cursor.close();

                ImageView imgView = (ImageView) v.findViewById(R.id.img3);
                imgView.setImageBitmap(BitmapFactory.decodeFile(encodedImg3));
            } else {
                Toast.makeText(getActivity(), "You didn't pick an Image", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(getActivity(), "Something went wrong", Toast.LENGTH_LONG).show();
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

    public class EndpointsEditProfile extends AsyncTask<User, Void, String> {
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
                    myApiService.updateUser(user).execute();
                    return "Success";
                } catch (IOException e) {
                }
            }
        }

        @Override
        protected void onPostExecute(String result) {
        }
    }

    public static class EndpointsGetInfo extends AsyncTask<String, Void, User> {
        private static UserApi myApiService = null;

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
            User theUser = new User();
            try {
                String username = params[0];
                theUser = myApiService.getUserByUsername(username).execute();
            } catch (IOException e) {
            }
            return theUser;
        }

        @Override
        protected void onPostExecute(User result) {
        }
    }

    public static class InsertImagesAsync extends AsyncTask<Pair<ArrayList<String>, String>, Void, String> {
        ArrayList<String> imgs;
        String uname;
        private static UserApi myApiService = null;

        @Override
        protected String doInBackground(Pair<ArrayList<String>, String>... params) {
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

            imgs = params[0].first;
            uname = params[0].second;
            if (imgs != null) {
                while (true) {
                    try {
                        myApiService.insertImage("" + imgs.get(1), "" + imgs.get(2), "" + imgs.get(0), uname).execute();
                        break;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            return "Success";
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
                img1.setImageBitmap(BitmapFactory.decodeFile(strings.get(0)));
                img2.setImageBitmap(BitmapFactory.decodeFile(strings.get(1)));
                img3.setImageBitmap(BitmapFactory.decodeFile(strings.get(2)));
            }
        }
    }

    public void setDefaultImage() {
        img1.setImageResource(R.drawable.default_user);
        img2.setImageResource(R.drawable.default_user);
        img3.setImageResource(R.drawable.default_user);
    }
}