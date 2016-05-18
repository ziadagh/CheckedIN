package com.checkedin.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.Rect;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;

import com.checkedin.Classes.GPSTracker;
import com.checkedin.Classes.GooglePlace;
import com.checkedin.Login;
import com.checkedin.R;
import com.checkedin.custom.CustomFragment;
import com.example.ibrahim.myapplication.backend.userApi.UserApi;
import com.example.ibrahim.myapplication.backend.userApi.model.User;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.googleapis.services.AbstractGoogleClientRequest;
import com.google.api.client.googleapis.services.GoogleClientRequestInitializer;

import android.provider.Settings;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.ByteArrayBuffer;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class CheckIn extends CustomFragment implements View.OnClickListener {
    ArrayList<GooglePlace> venuesList;

    // google key
    final String GOOGLE_KEY = "AIzaSyBq3mxBK0ycXMpOJJLEph_aI-npjBqXImg";

    private GoogleApiClient client;
    private GPSTracker tracker;
    Button checkin;
    static ImageView img;
    String lat, longt;
    ListView places;
    ArrayAdapter<String> myAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_checkin, null);
        checkin = (Button) v.findViewById(R.id.findmatch_checkinbtn);


        img = (ImageView) v.findViewById(R.id.img);

        checkin.setOnClickListener(this);

        ImagesAsync task2 = new ImagesAsync();
        task2.execute(getShared().getString("username", ""));

        places = (ListView) v.findViewById(android.R.id.list);
        client = new GoogleApiClient.Builder(getActivity())
                .addApi(AppIndex.API)
                .addApi(LocationServices.API)
                .build();

        return v;
    }

    public SharedPreferences getShared() {
        if (Login.sharedPreferences != null) {
            return this.getActivity().getSharedPreferences(Login.MyPreferences, Context.MODE_PRIVATE);
        } else {
            return this.getActivity().getSharedPreferences(Signup.MyPreferences, Context.MODE_PRIVATE);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.findmatch_checkinbtn:
                tracker = new GPSTracker(getActivity());
                if (!tracker.canGetLocation()) {
                    displayPromptForEnablingGPS(getActivity());
                    break;
                } else {
                    if (tracker.canGetLocation()) {
                        double latitude = tracker.getLatitude();
                        double longitude = tracker.getLongitude();
                        lat = "" + latitude;
                        longt = "" + longitude;
                    }
                    new googleplaces().execute();
                    break;
                }
        }
    }

    public static void displayPromptForEnablingGPS(final Activity activity) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        final String action = Settings.ACTION_LOCATION_SOURCE_SETTINGS;
        final String message = "Enable either GPS or any other location"
                + " service to find current location.  Click OK to go to"
                + " location services settings to let you do so.";
        builder.setMessage(message).setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface d, int id) {
                activity.startActivity(new Intent(action));
                d.dismiss();
            }
        })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface d, int id) {
                        d.cancel();
                    }
                });
        builder.create().show();
    }

    @Override
    public void onStart() {
        super.onStart();
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW,
                "GooglePlacesExample Page",
                Uri.parse("http://host/path"),
                Uri.parse("android-app://com.checkedin/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW,
                "Checked IN Page",
                Uri.parse("http://host/path"),
                Uri.parse("android-app://com.checkedin/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }



    private class googleplaces extends AsyncTask<View, Void, String> implements DialogInterface.OnClickListener {
        String temp;

        @Override
        protected String doInBackground(View... urls) {
            temp = makeCall("https://maps.googleapis.com/maps/api/place/search/json?location=" + lat + "," + longt + "&radius=75&sensor=true&key=" + GOOGLE_KEY);
            return temp;
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onPostExecute(String result) {
            if (temp == null) {
            } else {
                venuesList = (ArrayList<GooglePlace>) parseGoogleParse(temp);
                List<String> listTitle = new ArrayList<String>();
                for (int i = 0; i < venuesList.size() - 1; i++) {
                    listTitle.add(i, "\n" + venuesList.get(i).getName());
                }
                myAdapter = new ArrayAdapter<>(getActivity(), R.layout.place_item, listTitle);
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
                alertDialog.setAdapter(myAdapter, this);
                alertDialog.setTitle("Are You In:");
                alertDialog.show();
            }
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            AsyncTaskCheckin task = new AsyncTaskCheckin();
            User u = new User();
            u.setUsername(getShared().getString("username", ""));
            String location = myAdapter.getItem(which);
            task.execute(new Pair<>(u, location));
            String result = null;
            while(true) {
                try {
                    result = task.get();
                    break;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
            if (result.equals("Success")) {
                SharedPreferences.Editor editor = getShared().edit();
                editor.putString("Current Location", location);
                editor.putString("Status", "Checked In");
                editor.commit();
                ListAvailable f = new ListAvailable();
                Bundle loc = new Bundle();
                loc.putString("Location", myAdapter.getItem(which));
                f.setArguments(loc);
                FragmentManager fm = getActivity().getSupportFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();
                ft.replace(R.id.content_frame, f);
                ft.addToBackStack(null);
                ft.commit();
            }
        }
    }

    public static String makeCall(String url) {
        StringBuffer buffer_string = new StringBuffer(url);
        String replyString = "";
        HttpClient httpclient = new DefaultHttpClient();
        HttpGet httpget = new HttpGet(buffer_string.toString());
        try {
            HttpResponse response = httpclient.execute(httpget);
            InputStream is = response.getEntity().getContent();
            BufferedInputStream bis = new BufferedInputStream(is);
            ByteArrayBuffer baf = new ByteArrayBuffer(20);
            int current;
            while ((current = bis.read()) != -1) {
                baf.append((byte) current);
            }
            replyString = new String(baf.toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return replyString.trim();
    }

    private static ArrayList<GooglePlace> parseGoogleParse(final String response) {
        ArrayList<GooglePlace> temp = new ArrayList<>();
        try {
            JSONObject jsonObject = new JSONObject(response);
            if (jsonObject.has("results")) {
                JSONArray jsonArray = jsonObject.getJSONArray("results");
                for (int i = 0; i < jsonArray.length(); i++) {
                    GooglePlace poi = new GooglePlace();
                    if (jsonArray.getJSONObject(i).has("name")) {
                        poi.setName(jsonArray.getJSONObject(i).optString("name"));
                    }
                    temp.add(poi);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
        return temp;
    }

    public static class AsyncTaskCheckin extends AsyncTask<Pair<User, String>, Void, String> {
        private static UserApi myApiService = null;

        @Override
        protected String doInBackground(Pair<User, String>... params) {
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

            try {
                User user = params[0].first;
                String location = params[0].second;
                myApiService.insertUserLocation(location, user).execute();
                return "Success";
            } catch (IOException e) {
                return e.getMessage();
            }
        }

        @Override
        protected void onPostExecute(String result) {
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
            }
            return images;
        }

        @Override
        protected void onPostExecute(ArrayList<String> strings) {
            if (strings==null){
                setDefaultImage();
            }
            else {
                System.out.println(strings);
                if(BitmapFactory.decodeFile(strings.get(0))!=null){
                    img.setImageBitmap(getRoundedShape(BitmapFactory.decodeFile(strings.get(0))));
                }
                else setDefaultImage();
            }
        }
    }
    public void setDefaultImage() {
        img.setImageResource(R.mipmap.deafult_circle);
    }
    public Bitmap getRoundedShape(Bitmap scaleBitmapImage) {
        int targetWidth = 90;
        int targetHeight = 90;
        Bitmap targetBitmap = Bitmap.createBitmap(targetWidth, targetHeight,Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(targetBitmap);
        Path path = new Path();
        path.addCircle(((float) targetWidth - 1) / 2, ((float) targetHeight - 1) / 2,
                (Math.min(((float) targetWidth), ((float) targetHeight)) / 2), Path.Direction.CCW);
        canvas.clipPath(path);
        Bitmap sourceBitmap = scaleBitmapImage;
        canvas.drawBitmap(sourceBitmap, new Rect(0, 0, sourceBitmap.getWidth(), sourceBitmap.getHeight()), new Rect(0, 0, targetWidth, targetHeight), null);
        return targetBitmap;
    }
}