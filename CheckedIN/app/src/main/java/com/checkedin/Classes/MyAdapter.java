package com.checkedin.Classes;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.checkedin.R;
import com.example.ibrahim.myapplication.backend.userApi.model.User;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MyAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<User> list;

    public MyAdapter(Context context, ArrayList<User> list) {
        this.context = context;
        this.list = list;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        User user = list.get(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.user, parent, false);
        }
        TextView fname = (TextView) convertView.findViewById(R.id.user_fname);
        TextView lname = (TextView) convertView.findViewById(R.id.user_lname);
        TextView age = (TextView) convertView.findViewById(R.id.user_age);
        ImageView pp=(ImageView) convertView.findViewById(R.id.user_img);
        fname.setText(user.getFname());
        lname.setText(user.getLname());
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        String user_age = "";
        try {
            Date d = sdf.parse(user.getDob());
            user_age += AgeCalculator.calculateAge(d);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        age.setText(user_age.substring(0, 2));
        if(user.getProfilepic()!=null) {
            if(!user.getProfilepic().equals("null"))
            pp.setImageBitmap(BitmapFactory.decodeFile(user.getProfilepic()));
        }
        return convertView;
    }
}