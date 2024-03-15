package com.socialtools.tallymobile;


import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.socialtools.tallymobile.Utils.Constants;
import com.socialtools.tallymobile.Utils.PreferenceManager;
import com.socialtools.tallymobile.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private PreferenceManager preferenceManager;
    GoogleSignInClient googleSignInClient;
    FirebaseFirestore database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferenceManager = new PreferenceManager(this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        database = FirebaseFirestore.getInstance();
        GoogleSignInOptions options = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(MainActivity.this, options);
        getUserDetails();


        binding.username.setText(preferenceManager.getString(Constants.KEY_USERNAME));

        Glide.with(binding.profileImg.getContext())
                .load(preferenceManager.getString(Constants.KEY_PROFILE_IMAGE))
                .into(binding.profileImg);

        binding.imgLogOut.setOnClickListener(v->{
            FirebaseAuth.getInstance().addAuthStateListener(firebaseAuth -> {
                if (firebaseAuth.getCurrentUser() == null) {
                    googleSignInClient.signOut().addOnSuccessListener(unused -> {
                        preferenceManager.clear();
                        startActivity(new Intent(MainActivity.this, GoogleLoginActivity.class));
                        finish();
                    });
                }
            });
            FirebaseAuth.getInstance().signOut();
        });

        binding.itemCreateBtn.setOnClickListener(v->{
            startActivity(new Intent(getApplicationContext(),CreateItemActivity.class));
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        getUserDetails();
    }

    private void getUserDetails(){
        database.collection(Constants.KEY_COLLECTION_USERS)
                .document(preferenceManager.getString(Constants.KEY_USER_ID))
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()){
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()){
                            Toast.makeText(this, "updated", Toast.LENGTH_SHORT).show();
                            preferenceManager.putString(Constants.KEY_TALLY_ID,document.getString(Constants.KEY_TALLY_ID));
                            preferenceManager.putString(Constants.KEY_TALLY_PASSWORD,document.getString(Constants.KEY_TALLY_PASSWORD));
                            preferenceManager.putString(Constants.KEY_PROFILE_IMAGE,document.getString(Constants.KEY_PROFILE_IMAGE));
                        }
                    }
                }).addOnFailureListener(e->{
                    Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                });
    }


    /*private void logout() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userId = preferenceManager.getString(Constants.KEY_USER_ID);
        if (userId != null) {
            DocumentReference docRef = db.collection(Constants.KEY_COLLECTION_USERS).document(userId);
            docRef.update(Constants.KEY_FCM_TOKEN, FieldValue.delete())
                    .addOnSuccessListener(aVoid -> {
                        preferenceManager.clear();
                        auth.signOut();
                        Intent serviceIntent = new Intent(this, LiveLocationService.class);
                        stopService(serviceIntent);
                        Intent i = new Intent(getApplicationContext(), SignInActivity.class);
                        startActivity(i);
                        finish();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(getApplicationContext(),
                                    "Failed to Logout", Toast.LENGTH_SHORT).show());
        }
    }*/
}