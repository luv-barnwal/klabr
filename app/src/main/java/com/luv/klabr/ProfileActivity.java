package com.luv.klabr;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Objects;

public class ProfileActivity extends AppCompatActivity {

    private String receiverUserID = "", receiverUserImage = "", receiverUserName = "";
    private ImageView background_profile_view;
    private TextView name_profile;
    private Button addFriend, declineFriendRequest;

    private FirebaseAuth mAuth;
    private String senderUserId;
    private String currentState = "new";
    private DatabaseReference friendRequestRef, contactsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        senderUserId = mAuth.getCurrentUser().getUid();

        friendRequestRef = FirebaseDatabase.getInstance().getReference().child("Friend Requests");
        contactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts");

        receiverUserID = Objects.requireNonNull(Objects.requireNonNull(getIntent().getExtras()).get("visit_user_id")).toString();
        receiverUserImage = Objects.requireNonNull(getIntent().getExtras().get("profile_image")).toString();
        receiverUserName = Objects.requireNonNull(getIntent().getExtras().get("profile_name")).toString();

        background_profile_view = findViewById(R.id.background_profile_view);
        name_profile = findViewById(R.id.name_profile);
        addFriend = findViewById(R.id.add_friend);
        declineFriendRequest = findViewById(R.id.decline_friend_request);

        Picasso.get().load(receiverUserImage).into(background_profile_view);
        name_profile.setText(receiverUserName);

        manageClickEvents();
    }

    private void manageClickEvents() {

        friendRequestRef.child(senderUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(receiverUserID)){
                    String requestType = dataSnapshot.child(receiverUserID).child("request_type").getValue().toString();

                    if (requestType.equals("sent")){

                        currentState = "request_sent";
                        addFriend.setText("Cancel Friend Request");

                    } else if (requestType.equals("received")){

                        currentState = "request_received";
                        addFriend.setText("Accept Friend Request");
                        declineFriendRequest.setVisibility(View.VISIBLE);
                        declineFriendRequest.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                CancelFriendRequest();
                            }
                        });
                    }
                } else {

                    contactsRef.child(senderUserId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.hasChild(receiverUserID)){

                                currentState = "friends";
                                addFriend.setText("Delete Contact");
                            } else {

                                currentState = "new";
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        if (senderUserId.equals(receiverUserID)){

            addFriend.setVisibility(View.GONE);

        } else {

            addFriend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if (currentState.equals("new")){

                        SendFriendRequest();

                    } if (currentState.equals("request_sent")){

                        CancelFriendRequest();

                    } if (currentState.equals("request_received")){

                        AcceptFriendRequest();

                    } if (currentState.equals("request_sent")){

                        CancelFriendRequest();

                    }
                }
            });

        }
    }

    private void AcceptFriendRequest() {

        contactsRef.child(senderUserId).child(receiverUserID).child("Contact").setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){

                    contactsRef.child(receiverUserID).child(senderUserId).child("Contact").setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                friendRequestRef.child(senderUserId).child(receiverUserID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()){
                                            friendRequestRef.child(receiverUserID).child(senderUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()){
                                                        addFriend.setText("Delete Contact");
                                                        currentState = "friends";

                                                        declineFriendRequest.setVisibility(View.GONE);
                                                    }
                                                }
                                            });
                                        }
                                    }
                                });
                            }
                        }
                    });
                }
            }
        });

    }

    private void CancelFriendRequest() {

        friendRequestRef.child(senderUserId).child(receiverUserID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    friendRequestRef.child(receiverUserID).child(senderUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                addFriend.setText("Add Friend");
                                currentState = "new";
                            }
                        }
                    });
                }
            }
        });
    }

    private void SendFriendRequest() {

        friendRequestRef.child(senderUserId).child(receiverUserID).child("request_type").setValue("sent").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    friendRequestRef.child(receiverUserID).child(senderUserId).child("request_type").setValue("received").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){

                                currentState = "request_sent";
                                addFriend.setText("Cancel Friend Request");
                                Toast.makeText(ProfileActivity.this, "Friend Request Sent.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });
    }
}
