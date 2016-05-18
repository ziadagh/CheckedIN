package com.checkedin.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import android.widget.ListView;
import android.widget.Toast;

import com.checkedin.Classes.MyAdapter;
import com.checkedin.Login;
import com.checkedin.R;
import com.checkedin.custom.CustomFragment;
import com.example.ibrahim.myapplication.backend.userApi.UserApi;
import com.example.ibrahim.myapplication.backend.userApi.model.User;
import com.example.ibrahim.myapplication.backend.userApi.model.UserCollection;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.googleapis.services.AbstractGoogleClientRequest;
import com.google.api.client.googleapis.services.GoogleClientRequestInitializer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class ListAvailable extends CustomFragment implements View.OnClickListener {
    public static ArrayList<User> availableUsers = new ArrayList<>();
    ListView userList;
    View v;
    MyAdapter adapter;
    final Handler handler = new Handler();
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_list_available, null);
        userList = (ListView) v.findViewById(R.id.listavailable_list);
        Toast.makeText(v.getContext(), getShared().getString("Current Location", "").trim(), Toast.LENGTH_LONG).show();
        EndpointsListAvailable task = new EndpointsListAvailable();
        task.execute(getShared().getString("Current Location", ""));
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                adapter.notifyDataSetChanged();
                handler.postDelayed(this, 60 * 200);
            }
        }, 60 * 200);
        return v;
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

    public class EndpointsListAvailable extends AsyncTask<String, Void, ArrayList<User>> {
        private UserApi myApiService = null;

        @Override
        protected ArrayList<User> doInBackground(String... params) {
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
            ArrayList<User> list = new ArrayList<>();
            while (true) {
                try {
                    String location = params[0];
                    UserCollection users = myApiService.getUsersByLocation(location).execute();
                    if (users != null) {
                        List<User> userList = users.getItems();
                        list.addAll(userList);
                    }
                    break;
                } catch (IOException e) {
                }
            }
            return list;
        }

        @Override
        protected void onPostExecute(final ArrayList<User> result) {
            if (result != null) {
                for (int i = 0; i < result.size(); i++) {
                    if (result.get(i).getUsername().equals(getShared().getString("username", "")))
                        result.remove(i);
                }
                adapter = new MyAdapter(v.getContext(), result);
                userList.setAdapter(adapter);
                userList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        ProfileUser fr = new ProfileUser();
                        FragmentManager fm = getFragmentManager();
                        FragmentTransaction ft = fm.beginTransaction();
                        Bundle args = new Bundle();
                        args.putString("username", result.get(position).getUsername());
                        fr.setArguments(args);
                        ft.replace(R.id.content_frame, fr);
                        //ft.addToBackStack("List Available");
                        ft.commit();
                    }
                });

            }
        }
    }
}