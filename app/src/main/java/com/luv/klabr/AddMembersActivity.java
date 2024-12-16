package com.luv.klabr;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class AddMembersActivity extends AppCompatActivity {

    ListView friendsListView;
    ArrayList<String> friends = new ArrayList<>();
    ArrayAdapter arrayAdapter;
    FirebaseAuth mAuth;
    DatabaseReference databaseReference;
    Button continues;
    TextView names;
    ArrayList<String> selectedFriends = new ArrayList<>();
    String groupName, current;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_members);

        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        databaseReference.child("Users").child(mAuth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                current = dataSnapshot.child("name").getValue().toString();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        friendsListView = findViewById(R.id.friendsListView);
        continues = findViewById(R.id.createGrp);
        names = findViewById(R.id.names);

        Intent intent = getIntent();
        Bundle args = intent.getBundleExtra("friends");
        groupName = intent.getStringExtra("group");

        friends = (ArrayList<String>) args.getSerializable("ARRAYLIST");
        arrayAdapter = new ArrayAdapter(AddMembersActivity.this, android.R.layout.simple_list_item_1, friends);
        friendsListView.setAdapter(arrayAdapter);

        continues.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(selectedFriends.size() == 0){
                    Toast.makeText(AddMembersActivity.this, "Please select members to be added to your group", Toast.LENGTH_SHORT).show();
                } else {
                    String uuid = UUID.randomUUID().toString();
                    selectedFriends.add(current);
                    Map<String, Object> groupChat = new HashMap<>();
                    groupChat.put("Group", groupName);
                    groupChat.put("Members", selectedFriends);
                    groupChat.put("Messages", new ArrayList<String>());
                    databaseReference.child("GroupChats").child(uuid).child(groupName).setValue(groupChat).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                Intent newIntent = new Intent(AddMembersActivity.this, GroupMessagesActivity.class);
                                newIntent.putExtra("group", groupName);
                                newIntent.putExtra("name", current);
                                startActivity(newIntent);
                            }
                        }
                    });
                }
            }
        });

        friendsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (!selectedFriends.contains(friends.get(position))){
                    selectedFriends.add(friends.get(position));
                    names.append("\n" + friends.get(position));
                }
            }
        });
    }
}