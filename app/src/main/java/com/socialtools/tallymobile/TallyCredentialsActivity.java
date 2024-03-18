package com.socialtools.tallymobile;

import static android.content.ContentValues.TAG;


import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.socialtools.tallymobile.Utils.ApiHandler;
import com.socialtools.tallymobile.Utils.Constants;
import com.socialtools.tallymobile.Utils.LoadingDialog;
import com.socialtools.tallymobile.Utils.PreferenceManager;
import com.socialtools.tallymobile.databinding.ActivityTallyCredentialsBinding;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.Objects;

public class TallyCredentialsActivity extends AppCompatActivity {
    private ActivityTallyCredentialsBinding binding;
    private FirebaseAuth auth;
    private String tallyId,password,confirmPassword;
    private FirebaseFirestore database ;
    private PreferenceManager preferenceManager;
    private GoogleSignInClient googleSignInClient;
    private LoadingDialog loadingDialog;
    private ApiHandler apiHandler ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTallyCredentialsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        auth = FirebaseAuth.getInstance();
        database = FirebaseFirestore.getInstance();
        loadingDialog = new LoadingDialog(this);
        apiHandler = new ApiHandler(this);
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
                      //  uploadDataAsEmployee();
                }).show();
    }



    private void uploadDataAsEmployee(){


        if(auth.getCurrentUser()!=null) {
            JSONObject requestBody = new JSONObject();
            try {
                requestBody.put(Constants.KEY_TALLY_ID, tallyId);
                requestBody.put(Constants.KEY_PASSWORD, password);
                requestBody.put(Constants.KEY_USER_ID,preferenceManager.getString(Constants.KEY_USER_ID));

            } catch (JSONException e) {
                e.printStackTrace();
            }

          /*  new ApiCaller().fetchDataFromApi(this, Constants.KEY_CREATE_COMPANY_API,
                    Request.Method.POST, requestBody, loadingDialog,
                    response -> {


                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            boolean success = jsonResponse.getBoolean(Constants.KEY_SUCCESS);

                            loadingDialog.dismiss();

                            if (success) {

                                JSONObject companyData = jsonResponse.getJSONObject("data").getJSONObject("company");
                                JSONObject userData = jsonResponse.getJSONObject("data").getJSONObject(Constants.KEY_USER);

                                preferenceManager.putString(Constants.KEY_TALLY_ID, companyData.getString(Constants.KEY_TALLY_ID));
                                preferenceManager.putString(Constants.KEY_PASSWORD, companyData.getString(Constants.KEY_PASSWORD));
                                preferenceManager.putString(Constants.KEY_COMPANY_ID, companyData.getString(Constants.KEY_MONGO_ID));
                                preferenceManager.putString(Constants.KEY_COMPANY_OWNER_ID, companyData.getString(Constants.KEY_COMPANY_OWNER_ID));

                                preferenceManager.putString(Constants.KEY_USER_ID, userData.getString(Constants.KEY_MONGO_ID));
                                preferenceManager.putString(Constants.KEY_NAME, userData.getString(Constants.KEY_NAME));
                                preferenceManager.putString(Constants.KEY_AUTH_ID, userData.getString(Constants.KEY_AUTH_ID));
                                preferenceManager.putString(Constants.KEY_PROFILE_IMAGE, userData.getString(Constants.KEY_PROFILE_IMAGE));
                                preferenceManager.putString(Constants.KEY_USER_EMAIL, userData.getString(Constants.KEY_USER_EMAIL));
                                preferenceManager.putString(Constants.KEY_TALLY_ID, userData.getString(Constants.KEY_TALLY_ID));
                                preferenceManager.putString(Constants.KEY_JOINING_DATE, userData.getString(Constants.KEY_CREATE_AT));
                                preferenceManager.putString(Constants.KEY_IS_ADMIN, userData.getString(Constants.KEY_IS_ADMIN));
                                preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true);

                                Toast.makeText(this, "Signing Successful", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                                finish();


                            } else {

                                String errorMessage = jsonResponse.getString("message");


                                if (errorMessage.equals("No such company found with this tally serial number:" + tallyId)){

                                    new MaterialAlertDialogBuilder(this)
                                            .setCancelable(false)
                                            .setTitle("Company not found!")
                                            .setMessage("No company found with your\nTally.NET ID: "+tallyId+"\nPlease create new company.")

                                            .setPositiveButton("Create New Company", (dialog, which) -> uploadDataAsAdmin()).setNegativeButton("Cancel", (dialog, which) -> {

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

                                }else {
                                    Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
                                }






                            }


                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }


                    },
                    error -> {
                        Toast.makeText(this, error.getMessage(), Toast.LENGTH_SHORT).show();
                        loadingDialog.dismiss();
                    });*/



            apiHandler.makeApiCall(Constants.KEY_CREATE_EMPLOYEE_API,
                    Request.Method.POST, requestBody, new ApiHandler.ApiResponseListener() {
                        @Override
                        public void onSuccess(JSONObject response) {
                            // Handle success response
                            try {

                                boolean success = response.getBoolean(Constants.KEY_SUCCESS);

                                if (success) {

                                    JSONObject companyData = response.getJSONObject("data").getJSONObject("company");
                                    JSONObject userData = response.getJSONObject("data").getJSONObject(Constants.KEY_USER);

                                    preferenceManager.putString(Constants.KEY_TALLY_ID, companyData.getString(Constants.KEY_TALLY_ID));
                                    preferenceManager.putString(Constants.KEY_PASSWORD, companyData.getString(Constants.KEY_PASSWORD));
                                    preferenceManager.putString(Constants.KEY_COMPANY_ID, companyData.getString(Constants.KEY_MONGO_ID));
                                    preferenceManager.putString(Constants.KEY_COMPANY_OWNER_ID, companyData.getString(Constants.KEY_COMPANY_OWNER_ID));

                                    preferenceManager.putString(Constants.KEY_USER_ID, userData.getString(Constants.KEY_MONGO_ID));
                                    preferenceManager.putString(Constants.KEY_NAME, userData.getString(Constants.KEY_NAME));
                                    preferenceManager.putString(Constants.KEY_AUTH_ID, userData.getString(Constants.KEY_AUTH_ID));
                                    preferenceManager.putString(Constants.KEY_PROFILE_IMAGE, userData.getString(Constants.KEY_PROFILE_IMAGE));
                                    preferenceManager.putString(Constants.KEY_USER_EMAIL, userData.getString(Constants.KEY_USER_EMAIL));
                                    preferenceManager.putString(Constants.KEY_TALLY_ID, userData.getString(Constants.KEY_TALLY_ID));
                                    preferenceManager.putString(Constants.KEY_JOINING_DATE, userData.getString(Constants.KEY_CREATE_AT));
                                    preferenceManager.putString(Constants.KEY_IS_ADMIN, userData.getString(Constants.KEY_IS_ADMIN));
                                    preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true);

                                    Toast.makeText(getApplicationContext(), "Signing Successful", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                                    finish();


                                } else {

                                    String errorMessage = response.getString("message");


                                    if (errorMessage.equals("No such company found with this tally serial number:" + tallyId)){

                                        new MaterialAlertDialogBuilder(getApplicationContext())
                                                .setCancelable(false)
                                                .setTitle("Company not found!")
                                                .setMessage("No company found with your\nTally.NET ID: "+tallyId+"\nPlease create new company.")

                                                .setPositiveButton("Create New Company", (dialog, which) -> uploadDataAsAdmin()).setNegativeButton("Cancel", (dialog, which) -> {

                                                    FirebaseAuth.getInstance().addAuthStateListener(firebaseAuth -> {
                                                        if (firebaseAuth.getCurrentUser() == null) {
                                                            googleSignInClient.signOut().addOnSuccessListener(unused -> {
                                                                preferenceManager.clear();
                                                                startActivity(new Intent(TallyCredentialsActivity.this, GoogleLoginActivity.class));
                                                                finish();
                                                            });
                                                        }
                                                    });

                                                    auth.signOut();

                                                }).show();

                                    }else {
                                        Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_SHORT).show();
                                    }

                                }
                            } catch (JSONException e) {

                                loadingDialog.dismiss();
                                e.printStackTrace();
                                Toast.makeText(getApplicationContext(), "Error parsing JSON response", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onError(VolleyError error) {

                            loadingDialog.dismiss();
                            new MaterialAlertDialogBuilder(getApplicationContext())
                                    .setCancelable(false)
                                    .setTitle("Company not found!")
                                    .setMessage("No company found with your\nTally.NET ID: "+tallyId+"\nPlease create new company.")

                                    .setPositiveButton("Create New Company", (dialog, which) -> uploadDataAsAdmin()).setNegativeButton("Cancel", (dialog, which) -> {

                                        FirebaseAuth.getInstance().addAuthStateListener(firebaseAuth -> {
                                            if (firebaseAuth.getCurrentUser() == null) {
                                                googleSignInClient.signOut().addOnSuccessListener(unused -> {
                                                    preferenceManager.clear();
                                                    startActivity(new Intent(TallyCredentialsActivity.this, GoogleLoginActivity.class));
                                                    finish();
                                                });
                                            }
                                        });

                                        auth.signOut();

                                    }).show();
                            // Handle error
                            Toast.makeText(getApplicationContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });


        }else {
            loadingDialog.dismiss();
            Toast.makeText(this, "NO auth token found", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(getApplicationContext(),GoogleLoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent);
            finish();
        }




        /*loadingDialog.show();
        database.collection(Constants.KEY_COLLECTION_COMPANY)
                .document(tallyId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            String tallyPass = document.getString(Constants.KEY_PASSWORD);
                            if (Objects.equals(tallyPass, password)){
                                if(auth.getCurrentUser()!=null){
                                    HashMap<String, Object> tallyData = new HashMap<>();
                                    tallyData.put(Constants.KEY_TALLY_ID,tallyId);
                                    tallyData.put(Constants.KEY_PASSWORD,password);
                                    database.collection(Constants.KEY_COLLECTION_USERS)
                                            .document(Objects.requireNonNull(auth.getUid())).update(tallyData)
                                            .addOnSuccessListener(aVoid -> {
                                                HashMap<String, Object> userData = new HashMap<>();
                                                userData.put(Constants.KEY_IS_ADMIN,false);
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
                });*/


    }
    
    private void uploadDataAsAdmin(){

        if(auth.getCurrentUser()!=null){


            JSONObject requestBody = new JSONObject();
            try {
                requestBody.put(Constants.KEY_TALLY_ID, tallyId);
                requestBody.put(Constants.KEY_PASSWORD, password);
                requestBody.put(Constants.KEY_USER_ID,preferenceManager.getString(Constants.KEY_USER_ID));

            } catch (JSONException e) {
                e.printStackTrace();
            }

            apiHandler.makeApiCall(Constants.KEY_CREATE_COMPANY_API,
                    Request.Method.POST, requestBody, new ApiHandler.ApiResponseListener() {
                        @Override
                        public void onSuccess(JSONObject response) {
                            // Handle success response
                            try {

                                boolean success = response.getBoolean(Constants.KEY_SUCCESS);

                                if(success){

                                    JSONObject companyData = response.getJSONObject("data").getJSONObject("company");
                                    JSONObject userData = response.getJSONObject("data").getJSONObject(Constants.KEY_USER);

                                    preferenceManager.putString(Constants.KEY_TALLY_ID,companyData.getString(Constants.KEY_TALLY_ID));
                                    preferenceManager.putString(Constants.KEY_PASSWORD,companyData.getString(Constants.KEY_PASSWORD));
                                    preferenceManager.putString(Constants.KEY_COMPANY_ID,companyData.getString(Constants.KEY_MONGO_ID));
                                    preferenceManager.putString(Constants.KEY_COMPANY_OWNER_ID,companyData.getString(Constants.KEY_COMPANY_OWNER_ID));

                                    preferenceManager.putString(Constants.KEY_USER_ID,userData.getString(Constants.KEY_MONGO_ID));
                                    preferenceManager.putString(Constants.KEY_NAME,userData.getString(Constants.KEY_NAME));
                                    preferenceManager.putString(Constants.KEY_AUTH_ID,userData.getString(Constants.KEY_AUTH_ID));
                                    preferenceManager.putString(Constants.KEY_PROFILE_IMAGE,userData.getString(Constants.KEY_PROFILE_IMAGE));
                                    preferenceManager.putString(Constants.KEY_USER_EMAIL,userData.getString(Constants.KEY_USER_EMAIL));
                                    preferenceManager.putString(Constants.KEY_TALLY_ID,userData.getString(Constants.KEY_TALLY_ID));
                                    preferenceManager.putString(Constants.KEY_JOINING_DATE,userData.getString(Constants.KEY_CREATE_AT));
                                    preferenceManager.putBoolean(Constants.KEY_IS_ADMIN,userData.getBoolean(Constants.KEY_IS_ADMIN));
                                    preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true);

                                    Toast.makeText(getApplicationContext(), "Signing Successful", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(getApplicationContext(),MainActivity.class));
                                    finish();


                                }else {
                                    loadingDialog.dismiss();
                                    String errorMessage = response.getString("message");
                                    Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_SHORT).show();

                                }
                            } catch (JSONException e) {
                                // Handle JSON parsing error
                                loadingDialog.dismiss();
                                e.printStackTrace();
                                Toast.makeText(getApplicationContext(), "Error parsing JSON response", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onError(VolleyError error) {
                            // Handle error
                            loadingDialog.dismiss();
                            Toast.makeText(getApplicationContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

           /* new ApiCaller().fetchDataFromApi(this, Constants.KEY_CREATE_COMPANY_API,
                    Request.Method.POST, requestBody, loadingDialog,
                    response -> {


                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            boolean success = jsonResponse.getBoolean(Constants.KEY_SUCCESS);

                            loadingDialog.dismiss();

                            if(success){

                                JSONObject companyData = jsonResponse.getJSONObject("data").getJSONObject("company");
                                JSONObject userData = jsonResponse.getJSONObject("data").getJSONObject(Constants.KEY_USER);

                                preferenceManager.putString(Constants.KEY_TALLY_ID,companyData.getString(Constants.KEY_TALLY_ID));
                                preferenceManager.putString(Constants.KEY_PASSWORD,companyData.getString(Constants.KEY_PASSWORD));
                                preferenceManager.putString(Constants.KEY_COMPANY_ID,companyData.getString(Constants.KEY_MONGO_ID));
                                preferenceManager.putString(Constants.KEY_COMPANY_OWNER_ID,companyData.getString(Constants.KEY_COMPANY_OWNER_ID));

                                preferenceManager.putString(Constants.KEY_USER_ID,userData.getString(Constants.KEY_MONGO_ID));
                                preferenceManager.putString(Constants.KEY_NAME,userData.getString(Constants.KEY_NAME));
                                preferenceManager.putString(Constants.KEY_AUTH_ID,userData.getString(Constants.KEY_AUTH_ID));
                                preferenceManager.putString(Constants.KEY_PROFILE_IMAGE,userData.getString(Constants.KEY_PROFILE_IMAGE));
                                preferenceManager.putString(Constants.KEY_USER_EMAIL,userData.getString(Constants.KEY_USER_EMAIL));
                                preferenceManager.putString(Constants.KEY_TALLY_ID,userData.getString(Constants.KEY_TALLY_ID));
                                preferenceManager.putString(Constants.KEY_JOINING_DATE,userData.getString(Constants.KEY_CREATE_AT));
                                preferenceManager.putString(Constants.KEY_IS_ADMIN,userData.getString(Constants.KEY_IS_ADMIN));
                                preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true);

                                Toast.makeText(this, "Signing Successful", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(getApplicationContext(),MainActivity.class));
                                finish();


                            }else {

                                String errorMessage = jsonResponse.getString("message");
                                Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();

                            }


                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }


                    },
                    error -> {
                        Toast.makeText(this, error.getMessage(), Toast.LENGTH_SHORT).show();
                        loadingDialog.dismiss();
                    });*/

            /*database.collection(Constants.KEY_COLLECTION_USERS)
                    .document(Objects.requireNonNull(auth.getUid())).update(tallyData)
                    .addOnSuccessListener(aVoid -> {

                        HashMap<String, Object> tallyDataInCompany= new HashMap<>();
                        tallyDataInCompany.put(Constants.KEY_TALLY_ID,tallyId);
                        tallyDataInCompany.put(Constants.KEY_PASSWORD,password);
                        database.collection(Constants.KEY_COLLECTION_COMPANY)
                                .document(tallyId)
                                .set(tallyDataInCompany).addOnSuccessListener(aVoid1->{

                                    HashMap<String, Object> userData= new HashMap<>();
                                    userData.put(Constants.KEY_USER_ID,auth.getUid());
                                    userData.put(Constants.KEY_IS_ADMIN,true);

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
                    });*/
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
