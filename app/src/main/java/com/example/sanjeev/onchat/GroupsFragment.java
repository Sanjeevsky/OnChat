package com.example.sanjeev.onchat;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


/**
 * A simple {@link Fragment} subclass.
 */
public class GroupsFragment extends Fragment {
    private View groupFragmentView;
    private ListView listView;
    private ArrayAdapter<String> arrayAdapter;
    private ArrayList<String> list_of_groups=new ArrayList<>();
    private DatabaseReference groupRef;

    public GroupsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        groupFragmentView= inflater.inflate(R.layout.fragment_groups, container, false);
        groupRef=FirebaseDatabase.getInstance().getReference().child("Groups");

        InitializeFields();
        RetrieveAndDisplayGroups();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String currentGroupName=parent.getItemAtPosition(position).toString();
                Intent groupChatIntent=new Intent(getContext(),GroupChatActivity.class);
                groupChatIntent.putExtra("groupName",currentGroupName);
                startActivity(groupChatIntent);
            }
        });
        return groupFragmentView;
    }



    private void InitializeFields() {

        listView=(ListView)groupFragmentView.findViewById(R.id.list_view);
        arrayAdapter=new ArrayAdapter<String>(getContext(),android.R.layout.simple_list_item_1,list_of_groups);
        listView.setAdapter(arrayAdapter);
    }

    private void RetrieveAndDisplayGroups() {
    groupRef.addValueEventListener(new ValueEventListener() {
        Set<String> set=new HashSet<>();
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            Iterator iterator=dataSnapshot.getChildren().iterator();
            while (iterator.hasNext())
            {
                set.add(((DataSnapshot)iterator.next()).getKey());
                }
            list_of_groups.clear();
            list_of_groups.addAll(set);
            arrayAdapter.notifyDataSetChanged();
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    });

    }
}
