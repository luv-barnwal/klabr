package com.luv.klabr;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.util.ArrayList;

public class GroupChatsActivity extends AppCompatActivity {

    ListView friendsListView;
    ArrayList<String> friends = new ArrayList<>();
    ArrayList<String> groups = new ArrayList<>();
    ArrayAdapter arrayAdapter;
    FirebaseAuth mAuth;
    DatabaseReference databaseReference;
    String currentUser;
    Button create;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chats);

        friendsListView = findViewById(R.id.groupsListView);
        create = findViewById(R.id.create);

        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        databaseReference.child("Users").child(mAuth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                currentUser = dataSnapshot.child("name").getValue().toString();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        Intent intent = getIntent();
        Bundle args = intent.getBundleExtra("friends");

        friends = (ArrayList<String>) args.getSerializable("ARRAYLIST");

        databaseReference.child("GroupChats").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
//                    for(DataSnapshot snap : snapshot.child("Members").getChildren()){
//                        for (int i = 0; i < snap.getChildrenCount(); i++) {
//                            if (snap.child(String.valueOf(i)).getValue().toString().equals(currentUser)) {
                                groups.add(snapshot.child("Group").getValue().toString());
//                            }
//                        }
//                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, groups);
        friendsListView.setAdapter(arrayAdapter);

        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(GroupChatsActivity.this);
                builder.setTitle("Group Name");
                final EditText input = new EditText(GroupChatsActivity.this);
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                builder.setView(input);
                builder.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent createIntent = new Intent(GroupChatsActivity.this, AddMembersActivity.class);
                        createIntent.putExtra("group", input.getText().toString());
                        Bundle argus = new Bundle();
                        argus.putSerializable("ARRAYLIST",(Serializable)friends);
                        createIntent.putExtra("friends", argus);
                        startActivity(createIntent);
                    }
                });
                builder.setNegativeButton("Cancel", null);
                builder.show();
            }
        });
    }
}