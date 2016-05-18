package com.checkedin.custom;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

/**
 * The Class CustomFragment is the base Fragment class. You can extend your
 * Fragment classes with this class in case you want to apply common set of
 * rules for those Fragments.
 */
public class CustomFragment extends Fragment implements OnClickListener {
    /**
     * The Constant THEME.
     */
    private static final String THEME = "appTheme";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    /**
     * Set the touch and click listener for a View.
     *
     * @param v the view
     * @return the same view
     */
    public View setTouchNClick(View v) {
        v.setOnClickListener(this);
        v.setOnTouchListener(CustomActivity.TOUCH);
        return v;
    }

    @Override
    public void onClick(View v) {
    }
}
