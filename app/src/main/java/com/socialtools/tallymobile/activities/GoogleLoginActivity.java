package com.socialtools.tallymobile.activities;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;


import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.socialtools.tallymobile.R;
import com.socialtools.tallymobile.Utils.ApiHandler;
import com.socialtools.tallymobile.Utils.Constants;
import com.socialtools.tallymobile.Utils.LoadingDialog;
import com.socialtools.tallymobile.Utils.PreferenceManager;
import com.socialtools.tallymobile.databinding.ActivityGoogleLoginBinding;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

public class GoogleLoginActivity extends AppCompatActivity {

    private ActivityGoogleLoginBinding binding;
    FirebaseAuth auth;
    GoogleSignInClient googleSignInClient;
    private PreferenceManager preferenceManager;
    private String email,username,imageUrl;
    private LoadingDialog loadingDialog;
    private  ApiHandler apiHandler ;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGoogleLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager= new PreferenceManager(this);
        FirebaseApp.initializeApp(this);
        loadingDialog= new LoadingDialog(this);
        apiHandler = new ApiHandler(this);
        GoogleSignInOptions options = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, options);
        auth = FirebaseAuth.getInstance();

        binding.googleSignIn.setOnClickListener(v->{
            Intent intent = googleSignInClient.getSignInIntent();
            activityResultLauncher.launch(intent);
        });

        if (preferenceManager.getBoolean((Constants.KEY_IS_SIGNED_IN))){
            Intent intent= new Intent(getApplicationContext(), MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent);
            finish();
        }

    }


    private void checkUserData(){

        apiHandler.makeApiCall(Constants.KEY_IS_EXIST_USER_DATA_API+auth.getUid(),
                Request.Method.GET, null, new ApiHandler.ApiResponseListener() {
                    @Override
                    public void onSuccess(JSONObject response) {

                        try {
                            boolean success = response.getBoolean(Constants.KEY_SUCCESS);

                            if(success){
                                updateUserDataToServer();
                            } else {
                                uploadUserDataToServer();
                            }
                        } catch (JSONException e) {
                            loadingDialog.dismiss();
                            e.printStackTrace();
                            Toast.makeText(getApplicationContext(), "Error parsing JSON response", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onError(VolleyError error) {
                        uploadUserDataToServer();

                    }
                });
    }



    private void uploadUserDataToServer(){

        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put(Constants.KEY_NAME, username);
            requestBody.put(Constants.KEY_USER_EMAIL, email);
            requestBody.put(Constants.KEY_PROFILE_IMAGE, imageUrl);
            requestBody.put(Constants.KEY_AUTH_ID, auth.getUid());

        } catch (JSONException e) {
            e.printStackTrace();
        }

        apiHandler.makeApiCall(Constants.KEY_POST_USER_DATA_API,
                Request.Method.POST, requestBody, new ApiHandler.ApiResponseListener() {
            @Override
            public void onSuccess(JSONObject response) {
                // Handle success response
                try {

                    boolean success = response.getBoolean(Constants.KEY_SUCCESS);

                    if(success){
                        JSONObject user = response.getJSONObject(Constants.KEY_USER);
                        String userId = user.getString(Constants.KEY_MONGO_ID);
                        if (!userId.isEmpty()) {
                            preferenceManager.putString(Constants.KEY_USER_ID, userId);
                            loadingDialog.dismiss();
                            startActivity(new Intent(getApplicationContext(), TallyCredentialsActivity.class));
                        } else {
                            // Handle missing user ID
                            loadingDialog.dismiss();
                            Toast.makeText(getApplicationContext(), "User ID not found in response", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        // Handle unsuccessful response
                        loadingDialog.dismiss();
                        String errorMessage = response.getString("message");
                        Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {

                    loadingDialog.dismiss();
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Error parsing JSON response", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(VolleyError error) {
                // Handle error
                Toast.makeText(getApplicationContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }


    private void updateUserDataToServer(){

        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put(Constants.KEY_PROFILE_IMAGE, imageUrl);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        apiHandler.makeApiCall(Constants.KEY_UPDATE_USER_DATA_API+ preferenceManager.getString(Constants.KEY_AUTH_ID),
                Request.Method.PUT, requestBody, new ApiHandler.ApiResponseListener() {
                    @Override
                    public void onSuccess(JSONObject response) {
                        // Handle success response

                        try {

                            boolean success = response.getBoolean(Constants.KEY_SUCCESS);

                            String errorMessage = response.getString("message");


                            if (success) {
                                Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_SHORT).show();
                                JSONObject userData = response.getJSONObject(Constants.KEY_USER);

                                preferenceManager.putString(Constants.KEY_TALLY_ID, userData.getString(Constants.KEY_TALLY_ID));
                                preferenceManager.putString(Constants.KEY_PASSWORD, userData.getString(Constants.KEY_PASSWORD));
                                preferenceManager.putString(Constants.KEY_COMPANY_ID, userData.getString(Constants.KEY_COMPANY_ID));
                                preferenceManager.putString(Constants.KEY_USER_ID, userData.getString(Constants.KEY_MONGO_ID));
                                preferenceManager.putString(Constants.KEY_NAME, userData.getString(Constants.KEY_NAME));
                                preferenceManager.putString(Constants.KEY_AUTH_ID, userData.getString(Constants.KEY_AUTH_ID));
                                preferenceManager.putString(Constants.KEY_PROFILE_IMAGE, userData.getString(Constants.KEY_PROFILE_IMAGE));
                                preferenceManager.putString(Constants.KEY_USER_EMAIL, userData.getString(Constants.KEY_USER_EMAIL));
                                preferenceManager.putString(Constants.KEY_JOINING_DATE, userData.getString(Constants.KEY_CREATE_AT));
                                preferenceManager.putBoolean(Constants.KEY_IS_ADMIN, userData.getBoolean(Constants.KEY_IS_ADMIN));
                                preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true);

                                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                                finish();
                            }else{
                                loadingDialog.dismiss();
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
                        Toast.makeText(getApplicationContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });


    }







    private final ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(new
            ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode()==RESULT_OK){
            loadingDialog.show();
            Task<GoogleSignInAccount> accountTask = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
            try {

                GoogleSignInAccount signInAccount = accountTask.getResult(ApiException.class);
                AuthCredential authCredential = GoogleAuthProvider.getCredential(signInAccount.getIdToken(), null);
                auth.signInWithCredential(authCredential)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            username=Objects.requireNonNull(auth.getCurrentUser()).getDisplayName();
                            email=auth.getCurrentUser().getEmail();
                            imageUrl = Objects.requireNonNull(auth.getCurrentUser().getPhotoUrl()).toString();

                            preferenceManager.putString(Constants.KEY_AUTH_ID, auth.getUid());
                            preferenceManager.putString(Constants.KEY_NAME, username);
                            preferenceManager.putString(Constants.KEY_USER_EMAIL, email);
                            preferenceManager.putString(Constants.KEY_PROFILE_IMAGE, imageUrl);
                            checkUserData();




                        } else {
                            loadingDialog.dismiss();
                            Toast.makeText(getApplicationContext(), "Failed to sign in: " + task.getException(), Toast.LENGTH_SHORT).show();
                        }
                    }


                }).addOnCanceledListener(new OnCanceledListener() {
                            @Override
                            public void onCanceled() {
                                loadingDialog.dismiss();
                            }
                        });
            } catch (ApiException e) {
                e.printStackTrace();
            }
        }
    });

}

