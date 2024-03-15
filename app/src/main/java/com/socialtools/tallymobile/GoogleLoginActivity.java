package com.socialtools.tallymobile;

import static android.content.ContentValues.TAG;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;


import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.auth.api.identity.BeginSignInRequest;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.socialtools.tallymobile.Utils.Constants;
import com.socialtools.tallymobile.Utils.LoadingDialog;
import com.socialtools.tallymobile.Utils.PreferenceManager;
import com.socialtools.tallymobile.databinding.ActivityGoogleLoginBinding;

import java.util.HashMap;
import java.util.Objects;

public class GoogleLoginActivity extends AppCompatActivity {

    private ActivityGoogleLoginBinding binding;
    FirebaseAuth auth;
    GoogleSignInClient googleSignInClient;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore database;
    private String email,username,imageUrl;
    private LoadingDialog loadingDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGoogleLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager= new PreferenceManager(this);
        FirebaseApp.initializeApp(this);
        loadingDialog= new LoadingDialog(this);
        database=FirebaseFirestore.getInstance();
        GoogleSignInOptions options = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, options);
        auth = FirebaseAuth.getInstance();

        binding.googleSignIn.setOnClickListener(v->{
            loadingDialog.show();
            Intent intent = googleSignInClient.getSignInIntent();
            activityResultLauncher.launch(intent);
        });

        if (preferenceManager.getBoolean((Constants.KEY_IS_SIGNED_IN))){
            Intent intent= new Intent(getApplicationContext(),MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent);
            finish();
        }

    }


    private void uploadUserData(){
        HashMap<String, Object> userData = new HashMap<>();
        userData.put(Constants.KEY_USER_ID, auth.getUid());
        userData.put(Constants.KEY_USERNAME, username);
        userData.put(Constants.KEY_PROFILE_IMAGE,imageUrl);
        userData.put(Constants.KEY_USER_EMAIL, email);
        String userId = auth.getUid();
        if (userId != null) {
            database.collection(Constants.KEY_COLLECTION_USERS)
                    .document(userId)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                preferenceManager.putString(Constants.KEY_USER_ID, userId);
                                preferenceManager.putString(Constants.KEY_USERNAME, username);
                                preferenceManager.putString(Constants.KEY_USER_EMAIL, email);
                                preferenceManager.putString(Constants.KEY_PROFILE_IMAGE, imageUrl);
                                preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true);
                                loadingDialog.dismiss();
                                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                                finish();
                            } else {
                                // Document with userId does not exist, set user data
                                database.collection(Constants.KEY_COLLECTION_USERS)
                                        .document(userId)
                                        .set(userData)
                                        .addOnSuccessListener(aVoid1 -> {
                                            preferenceManager.putString(Constants.KEY_USER_ID, userId);
                                            preferenceManager.putString(Constants.KEY_USERNAME, username);
                                            preferenceManager.putString(Constants.KEY_USER_EMAIL, email);
                                            preferenceManager.putString(Constants.KEY_PROFILE_IMAGE, imageUrl);
                                            preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true);
                                            loadingDialog.dismiss();
                                            startActivity(new Intent(getApplicationContext(), TallyCredentialsActivity.class));
                                        })
                                        .addOnFailureListener(e -> {
                                            loadingDialog.dismiss();
                                            auth.signOut();
                                            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                        });
                            }
                        } else {
                            // Error getting document
                            uploadUserData();
                            Log.d(TAG, "Error getting document: ", task.getException());
                        }
                    });
        } else {
            Toast.makeText(this, "User ID is null", Toast.LENGTH_SHORT).show();
        }

    }











    private final ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(new
            ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode()==RESULT_OK){
            Task<GoogleSignInAccount> accountTask = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
            try {
                GoogleSignInAccount signInAccount = accountTask.getResult(ApiException.class);
                AuthCredential authCredential = GoogleAuthProvider.getCredential(signInAccount.getIdToken(), null);
                auth.signInWithCredential(authCredential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN,true);
                            username=Objects.requireNonNull(auth.getCurrentUser()).getDisplayName();
                            email=auth.getCurrentUser().getEmail();
                            imageUrl = Objects.requireNonNull(auth.getCurrentUser().getPhotoUrl()).toString();
                            uploadUserData();



                        } else {
                            loadingDialog.dismiss();
                            Toast.makeText(getApplicationContext(), "Failed to sign in: " + task.getException(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            } catch (ApiException e) {
                e.printStackTrace();
            }
        }
    });

}

/*startActivity(new Intent(getApplicationContext(),TallyCredentialsActivity.class));
                            finish();*/