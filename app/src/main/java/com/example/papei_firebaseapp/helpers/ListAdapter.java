package com.example.papei_firebaseapp.helpers;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.papei_firebaseapp.R;
import com.example.papei_firebaseapp.data.viewmodels.MainViewModel;
import com.example.papei_firebaseapp.ui.incidents.Incident;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
            Button deleteBtn = (Button) v.findViewById(R.id.deleteBtn);
            if (tt1 != null) {
                tt1.setText(p.getDescription());
            }

            if (tt2 != null) {
                tt2.setText(p.getType());
            }

            if (tt3 != null) {
                tt3.setText(p.getDate());
            }

            if(MainViewModel.getIsAdmin())
            {

                if(deleteBtn!=null ) {
                    //if verified incident disable button to verify
                    if (p.isCheckedByAdmin()) {
                        deleteBtn.setEnabled(false);
                    }
                    else
                    {
                        String key = p.getIncidentUid();
                        deleteBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                DatabaseReference ref= FirebaseDatabase.getInstance().getReference().child("Incidents");
                                ref.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        Iterable<DataSnapshot> userChildren = dataSnapshot.getChildren();
                                        for (DataSnapshot user: userChildren) {
                                            Incident u = user.getValue(Incident.class);
                                            if(u.getIncidentUid()!=null){
                                                if(u.getIncidentUid().equals(key)){
                                                    user.getRef().removeValue();
                                                    Toast.makeText(getContext(), "Incident Deleted!",
                                                            Toast.LENGTH_LONG).show();


                                                }
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });

                            }
                        });
                    }
                }

            if(verifyBtn!=null )
            {
                //if verified incident disable button to verify
                if(p.isCheckedByAdmin())
                {

                    verifyBtn.setEnabled(false);
                    verifyBtn.setText(getContext().getString(R.string.verified_incident));
                }
                //if NOT verified incident set click listener to enable verification from Admin
                else
                {
                    String key = p.getIncidentUid();
                    verifyBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            DatabaseReference ref= FirebaseDatabase.getInstance().getReference().child("Incidents");
                            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    Iterable<DataSnapshot> userChildren = dataSnapshot.getChildren();

                                    for (DataSnapshot user: userChildren) {
                                        Incident u = user.getValue(Incident.class);
                                        //todo watch out set here the key value as temp incident's id
                                        String id = user.getKey();
                                        if(id!=null){


                                            if(id.equals(key)){

                                                Toast.makeText(getContext(), "Incident Verified!",
                                                        Toast.LENGTH_LONG).show();

                                                Map<String, Object> updates = new HashMap<String,Object>();
                                                updates.put("checkedByAdmin", true);
                                                updates.put("notifiedUsersId", "");
                                                //todo watch out correct id update here logic is that the incident uid is null upon creation!!
                                                ref.child(key).updateChildren(updates);


                                            }
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });

                        }
                    });
                }

            }
         }
            //user not admin hide verify incident button here
            else
            {
                verifyBtn.setVisibility(View.GONE);
                deleteBtn.setVisibility(View.GONE);
            }
        }


        return v;
    }

}