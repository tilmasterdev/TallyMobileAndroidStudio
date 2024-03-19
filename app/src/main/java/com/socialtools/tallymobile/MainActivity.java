package com.socialtools.tallymobile;

import static android.content.ContentValues.TAG;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;

import com.google.firebase.auth.FirebaseAuth;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.socialtools.tallymobile.Utils.ApiCaller;
import com.socialtools.tallymobile.Utils.ApiHandler;
import com.socialtools.tallymobile.Utils.Constants;
import com.socialtools.tallymobile.Utils.LoadingDialog;
import com.socialtools.tallymobile.Utils.PreferenceManager;
import com.socialtools.tallymobile.databinding.ActivityMainBinding;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private PreferenceManager preferenceManager;
    private LoadingDialog loadingDialog;
    GoogleSignInClient googleSignInClient;
    FirebaseFirestore database;
    private ApiHandler apiHandler ;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferenceManager = new PreferenceManager(this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        apiHandler = new ApiHandler(this);
        loadingDialog= new LoadingDialog(this);
        database = FirebaseFirestore.getInstance();
        GoogleSignInOptions options = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(MainActivity.this, options);
        getUserDetails();


        binding.username.setText(preferenceManager.getString(Constants.KEY_NAME));

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

        binding.fabButton.setOnClickListener(v->{

           startActivity(new Intent(getApplicationContext(),CreateItemActivity.class));

        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        getUserDetails();
    }

    private void getUserDetails(){


        apiHandler.makeApiCall(Constants.KEY_GET_USER_DATA_API + preferenceManager.getString(Constants.KEY_AUTH_ID),
                Request.Method.GET, null, new ApiHandler.ApiResponseListener() {
                    @Override
                    public void onSuccess(JSONObject response) {
                        // Handle success response
                        try {

                            boolean success = response.getBoolean(Constants.KEY_SUCCESS);
                            String message = response.getString("message");

                        if (success) {

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
                            loadingDialog.dismiss();
                        }else{
                            loadingDialog.dismiss();
                            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
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

}