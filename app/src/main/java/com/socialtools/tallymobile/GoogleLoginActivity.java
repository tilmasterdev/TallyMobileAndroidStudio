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

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
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
import com.socialtools.tallymobile.Utils.ApiCaller;
import com.socialtools.tallymobile.Utils.ApiHandler;
import com.socialtools.tallymobile.Utils.Constants;
import com.socialtools.tallymobile.Utils.LoadingDialog;
import com.socialtools.tallymobile.Utils.PreferenceManager;
import com.socialtools.tallymobile.databinding.ActivityGoogleLoginBinding;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Objects;

public class GoogleLoginActivity extends AppCompatActivity {

    private ActivityGoogleLoginBinding binding;
    FirebaseAuth auth;
    GoogleSignInClient googleSignInClient;
    private PreferenceManager preferenceManager;
   // private FirebaseFirestore database;
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
        //database=FirebaseFirestore.getInstance();
        apiHandler = new ApiHandler(this);
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


    private void checkUserData(){

       /* new ApiCaller().fetchDataFromApi(this, Constants.KEY_ISEXIST_USER_DATA_API  + auth.getUid(),
                Request.Method.GET, null,loadingDialog, response -> {

                    try {
                        // Parse the JSON response
                        JSONObject jsonResponse = new JSONObject(response);
                        boolean success = jsonResponse.getBoolean(Constants.KEY_SUCCESS);


                        if (success) {

                            updateUserDataToServer();
                        } else {
                            uploadUserDataToServer();

                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.e(TAG, "Error parsing JSON response: " + e.getMessage());
                    }

                },
                error -> {
                    Toast.makeText(this, "Error: "+error.getMessage(), Toast.LENGTH_LONG).show();
                    loadingDialog.dismiss();
                });*/

        apiHandler.makeApiCall(Constants.KEY_ISEXIST_USER_DATA_API+auth.getUid(),
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

        /*HashMap<String, Object> userData = new HashMap<>();
        userData.put(Constants.KEY_USER_ID, auth.getUid());
        userData.put(Constants.KEY_NAME, username);
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
        }*/

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







       /* new ApiCaller().fetchDataFromApi(this, Constants.KEY_POST_USER_DATA_API,
                Request.Method.POST, requestBody, loadingDialog,
                response -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        boolean success = jsonResponse.getBoolean(Constants.KEY_SUCCESS);

                        if(success){
                            JSONObject user = jsonResponse.getJSONObject(Constants.KEY_USER);
                            String userId = user.getString(Constants.KEY_MONGO_ID);
                            if (!userId.isEmpty()) {
                                preferenceManager.putString(Constants.KEY_USER_ID, userId);
                                loadingDialog.dismiss();
                                startActivity(new Intent(getApplicationContext(), TallyCredentialsActivity.class));
                            } else {
                                // Handle missing user ID
                                loadingDialog.dismiss();
                                Toast.makeText(this, "User ID not found in response", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            // Handle unsuccessful response
                            loadingDialog.dismiss();
                            String errorMessage = jsonResponse.getString("message");
                            Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        // Handle JSON parsing error
                        loadingDialog.dismiss();
                        e.printStackTrace();
                        Toast.makeText(this, "Error parsing JSON response", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    // Handle Volley error
                    loadingDialog.dismiss();
                    error.printStackTrace();
                    Toast.makeText(this, "Volley error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                });*/





    }


    private void updateUserDataToServer(){

        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put(Constants.KEY_PROFILE_IMAGE, imageUrl);


            new ApiCaller().fetchDataFromApi(this, Constants.KEY_UPDATE_USER_DATA_API+auth.getUid(),
                    Request.Method.PUT, requestBody, loadingDialog,
                    response -> {

                        loadingDialog.dismiss();
                        try {

                            JSONObject jsonResponse = new JSONObject(response);
                            boolean success = jsonResponse.getBoolean(Constants.KEY_SUCCESS);
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
                                loadingDialog.dismiss();
                                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                                finish();
                            }else{

                                String errorMessage = jsonResponse.getString("message");
                                Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();

                            }
                        }catch (JSONException e){
                            throw new RuntimeException(e);
                        }

                    },
                    error -> {
                        Toast.makeText(this, error.getMessage(), Toast.LENGTH_SHORT).show();
                        loadingDialog.dismiss();
                    });


        } catch (JSONException e) {
            e.printStackTrace();
        }





        apiHandler.makeApiCall(Constants.KEY_UPDATE_USER_DATA_API+auth.getUid(),
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
                                loadingDialog.dismiss();
                                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                                finish();
                            }else{
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
                        Toast.makeText(getApplicationContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });


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
                });
            } catch (ApiException e) {
                e.printStackTrace();
            }
        }
    });

}

/*startActivity(new Intent(getApplicationContext(),TallyCredentialsActivity.class));
                            finish();*/