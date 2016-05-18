package com.checkedin.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.checkedin.R;
import com.checkedin.model.Data;
import com.example.ibrahim.myapplication.backend.userApi.model.User;

import java.util.ArrayList;

/**
 * The Adapter class for the ListView displayed in the right navigation drawer.
 */
public class RightNavAdapter extends BaseAdapter {

    private ArrayList<User> items;
    private Context context;

    /**
     * Instantiates a new left navigation adapter.
     *
     * @param context the context of activity
     * @param items   the array of items to be displayed on ListView
     */
    public RightNavAdapter(Context context, ArrayList<User> items) {
        this.context = context;
        this.items = items;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public User getItem(int arg0) {
        return items.get(arg0);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.right_nav_item, null);
        }
        User f = getItem(position);
        TextView lbl = (TextView) convertView.findViewById(R.id.lbl1);
        lbl.setText(f.getFname());
        return convertView;
    }
}