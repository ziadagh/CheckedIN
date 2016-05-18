package com.checkedin.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
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
import java.util.concurrent.Semaphore;

public class ILiked extends CustomFragment {
    public ArrayList<User> ilikedList = new ArrayList<>();
    ListView userList;
    MyAdapter adapter;
    View v;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_iliked, null);
        ILikedAsyncTask task = new ILikedAsyncTask();
        task.execute(new Pair<>(getShared().getString("username", ""), v));
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

    public class ILikedAsyncTask extends AsyncTask<Pair<String, View>, Void, ArrayList<User>> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        private UserApi myApiService = null;
        View context;

        @Override
        protected ArrayList<User> doInBackground(Pair<String, View>... params) {
            context = params[0].second;
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
                    String username = params[0].first;
                    UserCollection users = myApiService.getUsersILiked(username).execute();
                    if (users != null && !users.isEmpty()) {
                        List<User> userList = users.getItems();
                        if (userList == null) break;
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
            super.onPostExecute(result);
            userList = (ListView) v.findViewById(R.id.iliked_list);
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
                    ft.commit();
                }
            });
        }
    }
}