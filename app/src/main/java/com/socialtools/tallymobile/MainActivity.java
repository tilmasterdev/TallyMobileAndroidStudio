package com.socialtools.tallymobile;

import static android.content.ContentValues.TAG;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;

import com.google.firebase.auth.FirebaseAuth;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.socialtools.tallymobile.Utils.ApiCaller;
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
    private static final String API_URL = "https://tally-mobile-core-api.vercel.app/api/v1/users/getuser";
    private static final String DYNAMIC_VALUE = "123abc456";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferenceManager = new PreferenceManager(this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
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

        binding.itemCreateBtn.setOnClickListener(v->{


          //  startActivity(new Intent(getApplicationContext(),CreateItemActivity.class));
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        getUserDetails();
    }

    private void getUserDetails(){

        new ApiCaller().fetchDataFromApi(this, Constants.KEY_GET_USER_DATA_API + preferenceManager.getString(Constants.KEY_AUTH_ID),
                Request.Method.GET, null,loadingDialog, response -> {
                    try {

                        // Parse the JSON response
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
                        }else{

                            String errorMessage = jsonResponse.getString("message");
                            Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();

                        }

                    }catch (JSONException e){
                        e.printStackTrace();
                        Log.e(TAG, "Error parsing JSON response: " + e.getMessage());
                    }

                    loadingDialog.dismiss();
                },
                error -> {
                    Log.d(TAG, "Response: "+error);
                    loadingDialog.dismiss();
                });







        /*database.collection(Constants.KEY_COLLECTION_USERS)
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
                });*/
    }

}