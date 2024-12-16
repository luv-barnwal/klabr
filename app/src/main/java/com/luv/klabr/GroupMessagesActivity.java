package com.luv.klabr;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

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

public class GroupMessagesActivity extends AppCompatActivity {

    EditText message;
    TextView name;
    ListView chatListView;
    ArrayAdapter arrayAdapter;
    String currentUser;
    FirebaseAuth mAuth;
    DatabaseReference databaseReference;
    ArrayList<String> messages = new ArrayList<>();
    int counter = 0;
    String groupName;

    public void sendChat(View view){
        final String messageContent = message.getText().toString();

        Map<String, Object> messagez = new HashMap<>();
        messagez.put("sender", currentUser);
        messagez.put("message", messageContent);

        int value = counter + 1;

        databaseReference.child("GroupChats").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    if (snapshot.child("Group").getValue().toString().equals(groupName)) {
                        snapshot.getRef().child("Messages").child(value + "").updateChildren(messagez).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()){
                                    message.setText("");
                                }
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_messages);

        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        message = findViewById(R.id.sendMessage);
        name = findViewById(R.id.groupName);
        chatListView = findViewById(R.id.chatLstView);

        Intent intent = getIntent();
        groupName = intent.getStringExtra("group");
        currentUser = intent.getStringExtra("name");

        name.setText(groupName);

        arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, messages);

        chatListView.setAdapter(arrayAdapter);

        databaseReference.child("GroupChats").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    if (dataSnapshot.child("Group").getValue().toString().equals(groupName)) {
                            messages.clear();

                            counter = (int) dataSnapshot.child("Messages").getChildrenCount();

                            for (DataSnapshot dSnapshot : dataSnapshot.getChildren()) {

                                String messageContent = dSnapshot.child("message").getValue().toString();
                                String sender = dSnapshot.child("sender").getValue().toString();

                                if (!sender.equals(currentUser)) {
                                    messageContent = messageContent + "\nSent by: " + sender;
                                } else {
                                    messageContent = messageContent + "\nSent by: You";
                                }

                                messages.add(messageContent);
                            }
                            arrayAdapter.notifyDataSetChanged();
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
}