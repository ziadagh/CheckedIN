package com.checkedin;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager.OnBackStackChangedListener;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.checkedin.custom.CustomActivity;
import com.checkedin.ui.Chat;
import com.checkedin.ui.CheckIn;
import com.checkedin.ui.ILiked;
import com.checkedin.ui.LeftNavAdapter;
import com.checkedin.ui.LikedMe;
import com.checkedin.ui.ListAvailable;
import com.checkedin.ui.Match;
import com.checkedin.ui.Matches;
import com.checkedin.ui.Profile;
import com.checkedin.ui.RightNavAdapter;
import com.checkedin.ui.Settings;
import com.checkedin.ui.Signup;
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

/**
 * The Class MainActivity is the base activity class of the application. This
 * activity is launched after the Login and it holds all the Fragments used in
 * the app. It also creates the Navigation Drawers on left and right side.
 */
public class MainActivity extends CustomActivity {
    private DrawerLayout drawerLayout;
    private ListView drawerLeft, drawerRight;
    private ActionBarDrawerToggle drawerToggle;
    private LeftNavAdapter adapter;
    TextView userFullname;
    ImageView img;
    String chatUser;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupDrawer();
        setupContainer();
        Intent intent = new Intent(this, RegistrationIntentService.class);
        startService(intent);
    }

    /**
     * Setup the drawer layout. This method also includes the method calls for
     * setting up the Left & Right side drawers.
     */
    private void setupDrawer() {
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.drawable.ic_drawer, R.string.drawer_open, R.string.drawer_close) {
            @Override
            public void onDrawerClosed(View view) {
                setActionBarTitle();
                invalidateOptionsMenu();
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                setActionBarTitle();
                invalidateOptionsMenu();
            }
        };
        drawerLayout.setDrawerListener(drawerToggle);
        drawerLayout.closeDrawers();
        setupLeftNavDrawer();
        setupRightNavDrawer();
    }

    /**
     * Setup the left navigation drawer/slider. You can add your logic to load
     * the contents to be displayed on the left side drawer. It will also setup
     * the Header and Footer contents of left drawer. This method also apply the
     * Theme for components of Left drawer.
     */
    private void setupLeftNavDrawer() {
        drawerLeft = (ListView) findViewById(R.id.left_drawer);
        ImagesAsync task = new ImagesAsync();
        task.execute(getShared().getString("username", ""));
        View header = getLayoutInflater().inflate(R.layout.left_nav_header, null);
        img = (ImageView) header.findViewById(R.id.img);
        userFullname = (TextView) header.findViewById(R.id.leftheader_fullname);
        userFullname.setText(getShared().getString("fname", "") + " " + getShared().getString("lname", ""));
        drawerLeft.addHeaderView(header);

        adapter = new LeftNavAdapter(this, getResources().getStringArray(R.array.arr_left_nav_list));
        drawerLeft.setAdapter(adapter);
        drawerLeft.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int pos, long arg3) {
                drawerLayout.closeDrawers();
                if (pos != 0)
                    launchFragment(pos - 1);
                else
                    launchFragment(-2);
            }
        });
    }

    /**
     * Setup the right navigation drawer/slider. You can add your logic to load
     * the contents to be displayed on the right side drawer. It will also setup
     * the Header contents of right drawer.
     */
    private void setupRightNavDrawer() {
        drawerRight = (ListView) findViewById(R.id.right_drawer);
        ArrayList<User> al = new ArrayList<>();
        EndpointsMatches task = new EndpointsMatches();
        task.execute(getShared().getString("username", ""));
        try {
            al = task.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    /**
     * Setup the container fragment for drawer layout. This method will setup
     * the grid view display of main contents. You can customize this method as
     * per your need to display specific content.
     */
    private void setupContainer() {
        getSupportFragmentManager().addOnBackStackChangedListener(new OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                setActionBarTitle();
            }
        });
        launchFragment(-3);
    }

    /**
     * This method can be used to attach Fragment on activity view for a
     * particular tab position. You can customize this method as per your need.
     */
    public void launchFragment(int pos) {
        Fragment f = null;
        String title = null;

        if (pos == -1) {
            title = "Your Match";
            f = new Match();
        } else if (pos == -2) {
            title = "Profile";
            f = new Profile();
        } else if (pos == -3) {
            title = "Check In";
            f = new CheckIn();
        } else if (pos == -4) {
            title = chatUser;
            f = new Chat();
            Bundle args=new Bundle();
            args.putString("username",chatUser);
            f.setArguments(args);
        } else if (pos == 0) {
            title = "List Available";
            if(getShared().getString("Current Location","").equals("")){
                Toast.makeText(this, "Please Checkin To See The people Around You", Toast.LENGTH_LONG).show();
            }else
            f = new ListAvailable();
        } else if (pos == 1) {
            title = "Matches";
            f = new Matches();
        } else if (pos == 2) {
            title = "Liked Me";
            f = new LikedMe();
        } else if (pos == 3) {
            title = "iLiked";
            f = new ILiked();
        } else if (pos == 4) {
            title = "Settings";
            f = new Settings();
        } else if (pos == 5) {
            title = "Check In";
            f = new CheckIn();
        }
        if (f != null) {
            while (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                getSupportFragmentManager().popBackStackImmediate();
            }
            getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, f).addToBackStack(title).commit();
            if (adapter != null && pos >= 0)
                adapter.setSelection(pos);
        }
    }

    //Set the action bar title text.
    public void setActionBarTitle() {
        if (drawerLayout.isDrawerOpen(drawerLeft)) {
            getActionBar().setTitle("Main Menu");
            return;
        }
        if (drawerLayout.isDrawerOpen(drawerRight)) {
            getActionBar().setTitle(R.string.all_matches);
            return;
        }
        if (getSupportFragmentManager().getBackStackEntryCount() == 0)
            return;
        String title = getSupportFragmentManager().getBackStackEntryAt(getSupportFragmentManager().getBackStackEntryCount() - 1).getName();
        getActionBar().setTitle(title);
    }

    public void setTitle(String title){
        getActionBar().setTitle(title);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        if (drawerLayout.isDrawerOpen(drawerLeft) || drawerLayout.isDrawerOpen(drawerRight))
            menu.findItem(R.id.menu_chat).setVisible(false);
        else if (drawerLayout.isDrawerOpen(drawerRight))
            menu.findItem(R.id.menu_edit).setVisible(true);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home && drawerLayout.isDrawerOpen(drawerRight)) {
            drawerLayout.closeDrawer(drawerRight);
            return true;
        }
        if (drawerToggle.onOptionsItemSelected(item)) {
            drawerLayout.closeDrawer(drawerRight);
            return true;
        }
        if (item.getItemId() == R.id.menu_chat) {
            drawerLayout.closeDrawer(drawerLeft);
            if (!drawerLayout.isDrawerOpen(drawerRight)) {
                drawerLayout.openDrawer(drawerRight);
            } else {
                drawerLayout.closeDrawer(drawerRight);
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (getSupportFragmentManager().getBackStackEntryCount() > 1) {
                getSupportFragmentManager().popBackStackImmediate();
            }else if((getActionBar().getTitle().equals("Check In") || getActionBar().getTitle().equals("List Available"))){
                finish();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public SharedPreferences getShared() {
        if (Login.sharedPreferences != null) {
            return this.getSharedPreferences(Login.MyPreferences, Context.MODE_PRIVATE);
        } else {
            return this.getSharedPreferences(Signup.MyPreferences, Context.MODE_PRIVATE);
        }
    }

    public class EndpointsMatches extends AsyncTask<String, Void, ArrayList<User>> {
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
                    String username = params[0];
                    UserCollection users = myApiService.getMatches(username).execute();
                    if (users != null && !users.isEmpty()) {
                        List<User> userList = users.getItems();
                        if (userList == null) break;
                        list.addAll(userList);
                        break;
                    }
                } catch (IOException e) {
                }
            }
            return list;
        }

        @Override
        protected void onPostExecute(final ArrayList<User> result) {
            drawerRight.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    drawerLayout.closeDrawers();
                    chatUser=result.get(position).getUsername();
                    launchFragment(-4);
                }
            });
            drawerRight.setAdapter(new RightNavAdapter(getApplicationContext(), result));

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
        System.out.println(scaleBitmapImage);
        canvas.drawBitmap(sourceBitmap, new Rect(0, 0, sourceBitmap.getWidth(), sourceBitmap.getHeight()), new Rect(0, 0, targetWidth, targetHeight), null);
        return targetBitmap;
    }
}