package com.example.papei_firebaseapp.helpers;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.example.papei_firebaseapp.R;
import com.example.papei_firebaseapp.ui.incidents.Incident;

import java.util.List;

public class ListAdapter extends ArrayAdapter<Incident> {

    private int resourceLayout;
    private Context mContext;

    public ListAdapter(Context context, int resource, List<Incident> items) {
        super(context, resource, items);
        this.resourceLayout = resource;
        this.mContext = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View v = convertView;

        if (v == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(mContext);
            v = vi.inflate(resourceLayout, null);
        }

        Incident p = getItem(position);

        if (p != null) {
            TextView tt1 = (TextView) v.findViewById(R.id.id);
            TextView tt2 = (TextView) v.findViewById(R.id.categoryId);
            TextView tt3 = (TextView) v.findViewById(R.id.description);
            Button verifyBtn = (Button) v.findViewById(R.id.verifyBtn);


            if (tt1 != null) {
                tt1.setText(p.getDescription());
            }

            if (tt2 != null) {
                tt2.setText(p.getType());
            }

            if (tt3 != null) {
                tt3.setText(p.getDate());
            }
        }

        return v;
    }

}