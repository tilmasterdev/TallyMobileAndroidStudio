package com.socialtools.tallymobile;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.socialtools.tallymobile.Utils.Constants;
import com.socialtools.tallymobile.Utils.LoadingDialog;
import com.socialtools.tallymobile.Utils.PreferenceManager;
import com.socialtools.tallymobile.databinding.ActivityTallyCredentialsBinding;

import java.util.HashMap;
import java.util.Objects;

public class TallyCredentialsActivity extends AppCompatActivity {
    private ActivityTallyCredentialsBinding binding;
    private FirebaseAuth auth;
    private String tallyId,password,confirmPassword;
    private FirebaseFirestore database ;
    private PreferenceManager preferenceManager;
    private GoogleSignInClient googleSignInClient;
    private LoadingDialog loadingDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTallyCredentialsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        auth = FirebaseAuth.getInstance();
        database = FirebaseFirestore.getInstance();
        loadingDialog = new LoadingDialog(this);
        preferenceManager=new PreferenceManager(getApplicationContext());
        GoogleSignInOptions options = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, options);
       binding.validateBtn.setOnClickListener(v-> checkValidSignUpDetails());

    }





    private void checkValidSignUpDetails() {
        tallyId = Objects.requireNonNull(binding.editTextTallyId.getText()).toString().trim();
        password = Objects.requireNonNull(binding.editTextPassword.getText()).toString().trim();
        confirmPassword = Objects.requireNonNull(binding.editTextConfirmPassword.getText()).toString().trim();

        if (!tallyId.isEmpty()) {
            binding.textInputTallyId.setError(null);
            if (!password.isEmpty()) {
                binding.textInputPassword.setError(null);
                if (!confirmPassword.isEmpty()) {
                    binding.textInputConfirmPassword.setError(null);

                    if (password.equals(confirmPassword)){
                        binding.textInputConfirmPassword.setError(null);
                        showDialog();

                    }else {
                        binding.textInputConfirmPassword.setError("Password & Confirm Password not matched");
                    }
                } else {
                    binding.textInputConfirmPassword.setError("Please Enter Email Address");
                }
            } else {
                binding.textInputPassword.setError("Please Enter Email Address");
            }
        } else {
            binding.textInputPassword.setError("Please Enter Your Name");
        }
    }

    private void showDialog(){
        new MaterialAlertDialogBuilder(this)
                .setTitle("Account Type?")
                .setMessage("Please Select Your Account Type.")

                .setPositiveButton("Create new Company", (dialog, which) -> {
                    uploadDataAsAdmin();

                }).setNegativeButton("SignIn as Employee", (dialog, which) -> {
                        uploadDataAsEmployee();
                }).show();
    }



    private void uploadDataAsEmployee(){
        loadingDialog.show();
        database.collection(Constants.KEY_COLLECTION_COMPANY)
                .document(tallyId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            String tallyPass = document.getString(Constants.KEY_TALLY_PASSWORD);
                            if (Objects.equals(tallyPass, password)){
                                if(auth.getCurrentUser()!=null){
                                    HashMap<String, Object> tallyData = new HashMap<>();
                                    tallyData.put(Constants.KEY_TALLY_ID,tallyId);
                                    tallyData.put(Constants.KEY_TALLY_PASSWORD,password);
                                    database.collection(Constants.KEY_COLLECTION_USERS)
                                            .document(Objects.requireNonNull(auth.getUid())).update(tallyData)
                                            .addOnSuccessListener(aVoid -> {
                                                HashMap<String, Object> userData = new HashMap<>();
                                                userData.put(Constants.KEY_IS_SUPER_ADMIN,false);
                                                userData.put(Constants.KEY_USER_ID,auth.getUid());

                                                database.collection(Constants.KEY_COLLECTION_COMPANY)
                                                        .document(tallyId)
                                                        .collection(Constants.KEY_COLLECTION_EMPLOYEE)
                                                        .document(auth.getUid())
                                                        .set(userData)
                                                        .addOnSuccessListener(aVoid1->{
                                                              preferenceManager.putString(Constants.KEY_TALLY_ID,tallyId);
                                                              preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN,true);
                                                              Toast.makeText(this, "Signing Successful", Toast.LENGTH_SHORT).show();
                                                              loadingDialog.dismiss();
                                                              startActivity(new Intent(getApplicationContext(),MainActivity.class));
                                                              finish();

                                                        }).addOnFailureListener(e-> {
                                                            loadingDialog.dismiss();
                                                            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();}
                                                        );

                                            })
                                            .addOnFailureListener(e -> {
                                                loadingDialog.dismiss();
                                                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                                            });
                                }else {
                                    loadingDialog.dismiss();
                                    Toast.makeText(this, "NO auth token found", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(getApplicationContext(),GoogleLoginActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                                    startActivity(intent);
                                    finish();
                                }


                            }else {
                                loadingDialog.dismiss();
                                Toast.makeText(this, "Incorrect Tally Password. Try Again!!!", Toast.LENGTH_SHORT).show();
                            }

                        } else {
                            loadingDialog.dismiss();
                            new MaterialAlertDialogBuilder(this)
                                    .setCancelable(false)
                                    .setTitle("Company not found!")
                                    .setMessage("No company found with your\nTally.NET ID: "+tallyId+"\nPlease create new company.")

                                    .setPositiveButton("Create New Company", (dialog, which) -> {
                                        uploadDataAsAdmin();

                                    }).setNegativeButton("Cancel", (dialog, which) -> {

                                        FirebaseAuth.getInstance().addAuthStateListener(firebaseAuth -> {
                                            if (firebaseAuth.getCurrentUser() == null) {
                                                googleSignInClient.signOut().addOnSuccessListener(unused -> {
                                                    preferenceManager.clear();
                                                    startActivity(new Intent(this, GoogleLoginActivity.class));
                                                    finish();
                                                });
                                            }
                                        });

                                        auth.signOut();

                                    }).show();

                        }
                    } else {
                        // Handle failures
                        loadingDialog.dismiss();
                        Log.d(TAG, "Error: ", task.getException());
                    }
                });


    }







    private void uploadDataAsAdmin(){
        loadingDialog.show();
        HashMap<String, Object> tallyData= new HashMap<>();
        tallyData.put(Constants.KEY_TALLY_ID,tallyId);
        tallyData.put(Constants.KEY_TALLY_PASSWORD,password);



        if(auth.getCurrentUser()!=null){
            database.collection(Constants.KEY_COLLECTION_USERS)
                    .document(Objects.requireNonNull(auth.getUid())).update(tallyData)
                    .addOnSuccessListener(aVoid -> {

                        HashMap<String, Object> tallyDataInCompany= new HashMap<>();
                        tallyDataInCompany.put(Constants.KEY_TALLY_ID,tallyId);
                        tallyDataInCompany.put(Constants.KEY_TALLY_PASSWORD,password);
                        database.collection(Constants.KEY_COLLECTION_COMPANY)
                                .document(tallyId)
                                .set(tallyDataInCompany).addOnSuccessListener(aVoid1->{

                                    HashMap<String, Object> userData= new HashMap<>();
                                    userData.put(Constants.KEY_USER_ID,auth.getUid());
                                    userData.put(Constants.KEY_IS_SUPER_ADMIN,true);

                                    database.collection(Constants.KEY_COLLECTION_COMPANY)
                                            .document(tallyId)
                                            .collection(Constants.KEY_COLLECTION_EMPLOYEE)
                                            .document(Objects.requireNonNull(auth.getUid()))
                                            .set(userData).addOnSuccessListener(aVoid2->{
                                                preferenceManager.putString(Constants.KEY_TALLY_ID,tallyId);
                                                preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN,true);
                                                loadingDialog.dismiss();
                                                Toast.makeText(this, "Signing Successful", Toast.LENGTH_SHORT).show();
                                                startActivity(new Intent(getApplicationContext(),MainActivity.class));
                                                finish();

                                            }).addOnFailureListener(e->{
                                                loadingDialog.dismiss();
                                                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                            });

                                }).addOnFailureListener(e-> {
                                    loadingDialog.dismiss();
                                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                });

                          })
                    .addOnFailureListener(e -> {
                        loadingDialog.dismiss();
                        Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }else {
            loadingDialog.dismiss();
            Toast.makeText(this, "NO auth token found", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(getApplicationContext(),GoogleLoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent);
            finish();
        }

    }
}
