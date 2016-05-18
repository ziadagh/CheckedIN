package com.checkedin.custom;

import android.app.ActionBar;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;

import com.checkedin.R;
import com.checkedin.utils.TouchEffect;

/**
 * This is a common activity that all other activities of the app can extend to
 * inherit the common behaviors like setting a Theme to activity.
 */
public class CustomActivity extends FragmentActivity implements OnClickListener {

    public static final TouchEffect TOUCH = new TouchEffect();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupActionBar();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home)
            finish();
        return super.onOptionsItemSelected(item);
    }

    /**
     * This method will setup the top title bar (Action bar) content and display
     * values. It will also setup the custom background theme for ActionBar. You
     * can override this method to change the behavior of ActionBar for
     * particular Activity
     */
    protected void setupActionBar() {
        ActionBar ab = getActionBar();
        if (ab == null)
            return;

        ab.setDisplayHomeAsUpEnabled(true);
        ab.setHomeButtonEnabled(true);
        ab.setBackgroundDrawable(getResources().getDrawable(R.drawable.actionbar_bg));
        ab.setIcon(R.mipmap.logo);
    }

    /**
     * Set the touch and click listener for a View.
     *
     * @param id the id of view
     * @return the view
     */
    public View setTouchNClick(int id) {
        View v = setClick(id);
        v.setOnTouchListener(TOUCH);
        return v;
    }

    /**
     * Set the click listener for a View.
     *
     * @param id the id of view
     * @return the view
     */
    public View setClick(int id) {
        View v = findViewById(id);
        v.setOnClickListener(this);
        return v;
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
    }
}
