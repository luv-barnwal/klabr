package com.luv.klabr;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
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

public class ChatActivity extends AppCompatActivity {

    EditText message;
    TextView name;
    ListView chatListView;
    ArrayAdapter arrayAdapter;
    String currentUser, otherUser;
    FirebaseAuth mAuth;
    DatabaseReference databaseReference;
    ArrayList<String> messages = new ArrayList<>();
    int counter = 0;

    public void sendChat(View view) {
        if (!message.getText().toString().isEmpty()) {
            int compare = currentUser.compareTo(otherUser);
            String title = "";
            if (compare < 0) {
                title = currentUser + " + " + otherUser;
            } else if (compare > 0) {
                title = otherUser + " + " + currentUser;
            }
            final String messageContent = message.getText().toString();

            Map<String, Object> messagez = new HashMap<>();
            messagez.put("sender", currentUser);
            messagez.put("message", messageContent);

            int value = counter + 1;

            databaseReference.child("Chats").child(title).child(value + "").updateChildren(messagez).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()){
                        message.setText("");
                    }
                }
            });
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        message = findViewById(R.id.chatEditText);
        name = findViewById(R.id.name);
        chatListView = findViewById(R.id.chatLstView);

        Intent intent = getIntent();
        otherUser = intent.getStringExtra("username");
        currentUser = intent.getStringExtra("current");

        name.setText("Chat with " + otherUser);

        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, messages);

        chatListView.setAdapter(arrayAdapter);

        int compare = currentUser.compareTo(otherUser);
        String title = "";
        if (compare < 0){
            title = currentUser + " + " + otherUser;
        } else if (compare > 0){
            title = otherUser + " + " + currentUser;
        }

        databaseReference.child("Chats").child(title).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    messages.clear();

                    counter = (int) snapshot.getChildrenCount();

                    for (DataSnapshot dataSnapshot : snapshot.getChildren()){

                        String messageContent = dataSnapshot.child("message").getValue().toString();
                        String sender = dataSnapshot.child("sender").getValue().toString();

                        if (!sender.equals(currentUser)){
                            messageContent = "> " + messageContent;
                        }

                        messages.add(messageContent);
                    }
                    arrayAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}