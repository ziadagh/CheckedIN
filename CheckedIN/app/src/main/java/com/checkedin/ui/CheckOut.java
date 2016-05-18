package com.checkedin.ui;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;

import com.checkedin.R;
import com.checkedin.custom.CustomFragment;

public class CheckOut extends CustomFragment {

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        CheckIn fragment = new CheckIn();
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        fragmentTransaction.replace(R.id.content_frame, fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }
}