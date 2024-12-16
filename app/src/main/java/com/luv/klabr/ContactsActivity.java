package com.luv.klabr;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.io.Serializable;
import java.util.ArrayList;

public class ContactsActivity extends AppCompatActivity {

    BottomNavigationView navView;
    RecyclerView myContactsList;
    ImageView findPeopleBTN;
    private DatabaseReference contactsRef, usersRef;
    private FirebaseAuth mAuth;
    private String currentUserId, userName = "", profileImage = "", calledBy = "";
    ArrayList<String> friends = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);

        navView = findViewById(R.id.nav_view);
        navView.setOnNavigationItemSelectedListener(navigationItemSelectedListener);

        contactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts");
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();

        findPeopleBTN = findViewById(R.id.find_people_btn);
        myContactsList = findViewById(R.id.contact_list);
        myContactsList.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        findPeopleBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent findIntent = new Intent(ContactsActivity.this, FindPeopleActivity.class);
                startActivity(findIntent);
            }
        });
    }

    private BottomNavigationView.OnNavigationItemSelectedListener navigationItemSelectedListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

            switch (menuItem.getItemId()){

                case R.id.navigation_home :
                    Intent mainIntent = new Intent(ContactsActivity.this, ContactsActivity.class);
                    startActivity(mainIntent);
                    break;

                case R.id.navigation_settings :
                    Intent settingsIntent = new Intent(ContactsActivity.this, SettingsActivity.class);
                    startActivity(settingsIntent);
                    break;

                case R.id.navigation_library :
                    Intent libraryIntent = new Intent(ContactsActivity.this, LibraryActivity.class);
                    startActivity(libraryIntent);

                case R.id.navigation_notifications :
                    Intent notificationsIntent = new Intent(ContactsActivity.this, NotificationsActivity.class);
                    startActivity(notificationsIntent);
                    break;

                case R.id.navigation_logout :

                    new AlertDialog.Builder(ContactsActivity.this)
                            .setTitle("Are you sure you want to log out?")
                            .setMessage("This will take you to the home screen")
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                    FirebaseAuth.getInstance().signOut();

                                    Intent logOutIntent = new Intent(ContactsActivity.this, RegistrationActivity.class);
                                    startActivity(logOutIntent);
                                    finish();
                                }
                            })
                            .setNegativeButton("No", null)
                            .show();
                    break;
            }
            return true;
        }
    };

    @Override
    protected void onStart() {
        super.onStart();

        checkForReceivingCall();

        validateUser();

        FirebaseRecyclerOptions<Contacts> options = new  FirebaseRecyclerOptions.Builder<Contacts>().setQuery(contactsRef.child(currentUserId), Contacts.class).build();

        FirebaseRecyclerAdapter<Contacts, ContactsViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Contacts, ContactsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final ContactsViewHolder holder, int i, @NonNull Contacts contacts) {
                final String listUserId = getRef(i).getKey();

                usersRef.child(listUserId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()){

                            userName = dataSnapshot.child("name").getValue().toString();
                            profileImage = dataSnapshot.child("image").getValue().toString();

                            holder.usernameTxt.setText(userName);
                            Picasso.get().load(profileImage).into(holder.profileImageView);
                            friends.add(userName);
                        }

                        holder.callBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intent = new Intent(ContactsActivity.this, CallingActivity.class);
                                intent.putExtra("visit_user_id", listUserId);
                                startActivity(intent);
                            }
                        });

                        holder.messageBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

                                databaseReference.child("Users").child(mAuth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        String currentUser = dataSnapshot.child("name").getValue().toString();

                                        Intent intent = new Intent(ContactsActivity.this, ChatActivity.class);
                                        intent.putExtra("username", userName);
                                        intent.putExtra("current", currentUser);
                                        startActivity(intent);
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                            }
                        });
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

            @NonNull
            @Override
            public ContactsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.contact_design, parent, false);

                ContactsViewHolder viewHolder = new ContactsViewHolder(view);
                return viewHolder;
            }
        };

        myContactsList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }

    public static class ContactsViewHolder extends RecyclerView.ViewHolder {

        TextView usernameTxt;
        Button callBtn, messageBtn;
        ImageView profileImageView;

        public ContactsViewHolder(@NonNull View itemView) {

            super(itemView);

            usernameTxt = itemView.findViewById(R.id.name_contact);
            callBtn = itemView.findViewById(R.id.call_btn);
            profileImageView = itemView.findViewById(R.id.image_contact);
            messageBtn = itemView.findViewById(R.id.message_btn);

        }

    }

    private void validateUser() {

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        reference.child("Users").child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (!dataSnapshot.exists()){

                    Intent intent = new Intent(ContactsActivity.this, SettingsActivity.class);
                    startActivity(intent);
                    finish();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void checkForReceivingCall() {

        usersRef.child(currentUserId).child("Ringing").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.hasChild("ringing")){

                    calledBy = dataSnapshot.child("ringing").getValue().toString();

                    Intent intent = new Intent(ContactsActivity.this, CallingActivity.class);
                    intent.putExtra("visit_user_id", calledBy);
                    startActivity(intent);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
