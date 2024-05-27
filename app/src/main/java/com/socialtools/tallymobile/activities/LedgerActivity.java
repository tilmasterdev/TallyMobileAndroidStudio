package com.socialtools.tallymobile.activities;

import static android.content.ContentValues.TAG;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.socialtools.tallymobile.Listeners.LedgerListener;
import com.socialtools.tallymobile.Models.ItemModel;
import com.socialtools.tallymobile.Models.LedgerModel;
import com.socialtools.tallymobile.R;
import com.socialtools.tallymobile.Utils.ApiHandler;
import com.socialtools.tallymobile.Utils.Constants;
import com.socialtools.tallymobile.Utils.LoadingDialog;
import com.socialtools.tallymobile.Utils.PreferenceManager;
import com.socialtools.tallymobile.adapters.ItemAdapter;
import com.socialtools.tallymobile.adapters.LedgerAdapter;
import com.socialtools.tallymobile.databinding.ActivityLedgerBinding;
import com.socialtools.tallymobile.databinding.LedgerCreatorLayoutBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class LedgerActivity extends AppCompatActivity implements LedgerListener {
    private ActivityLedgerBinding binding;
    private PreferenceManager preferenceManager;
    private LoadingDialog loadingDialog;
    private ApiHandler apiHandler;
    private List<LedgerModel> ledgerModel;
    private LedgerCreatorLayoutBinding ledgerBinding;
    private String name, alias,parent,panNumber,gstNumber,addressLine1,addressLine2,addressLine3,stateName,countryName;
    private boolean isBillWise;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLedgerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        apiHandler = new ApiHandler(this);
        preferenceManager = new PreferenceManager(this);
        loadingDialog = new LoadingDialog(this);
        getLedgerDetails();
        init();

    }
    private void init(){
        binding.imageBack.setOnClickListener(v-> finish());
        binding.fabButton.setOnClickListener(v-> setLedger());
        binding.swipeRefreshLayout.setOnRefreshListener(this::getLedgerDetails);
        ledgerModel = new ArrayList<>();

    }

    private void setLedger(){
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);




        ledgerBinding = LedgerCreatorLayoutBinding.inflate(LayoutInflater.from(this));
        bottomSheetDialog.setContentView(ledgerBinding.getRoot());
        bottomSheetDialog.show();

        ledgerBinding.isBillWiseSwitch.setOnCheckedChangeListener(((buttonView, isChecked) -> isBillWise=isChecked));

        ledgerBinding.submitBtn.setOnClickListener(v->{
            name = Objects.requireNonNull(ledgerBinding.editTextFirst.getText()).toString().trim();
            alias = Objects.requireNonNull(ledgerBinding.editTextSecond.getText()).toString().trim();
            parent = Objects.requireNonNull(ledgerBinding.editTextThird.getText()).toString().trim();
            panNumber = Objects.requireNonNull(ledgerBinding.editTextForth.getText()).toString().toUpperCase(Locale.ROOT).trim();
            gstNumber = Objects.requireNonNull(ledgerBinding.editTextSixth.getText()).toString().toUpperCase(Locale.ROOT).trim();
            addressLine1 = Objects.requireNonNull(ledgerBinding.editTextSeventh.getText()).toString().trim();
            addressLine2 = Objects.requireNonNull(ledgerBinding.editTextEight.getText()).toString().trim();
            addressLine3 = Objects.requireNonNull(ledgerBinding.editTextNinth.getText()).toString().trim();
            stateName = Objects.requireNonNull(ledgerBinding.editTextTenth.getText()).toString().trim();
            countryName = Objects.requireNonNull(ledgerBinding.editTextEleventh.getText()).toString().trim();





            JSONObject requestBody = new JSONObject();
            try {
                requestBody.put(Constants.IS_BILL_WISE,isBillWise);
                requestBody.put(Constants.KEY_NAME, name);
                requestBody.put(Constants.KEY_ALIAS, alias);
                requestBody.put(Constants.KEY_PARENT, parent);
                requestBody.put(Constants.KEY_STATE_NAME,stateName);
                requestBody.put(Constants.KEY_COUNTRY_NAME,countryName);
                requestBody.put(Constants.KEY_ADDRESS_1, addressLine1);
                requestBody.put(Constants.KEY_ADDRESS_2,addressLine2);
                requestBody.put(Constants.KEY_ADDRESS_3, addressLine3);
                requestBody.put(Constants.KEY_PRODUCT_GST, gstNumber);
                requestBody.put(Constants.KEY_PAN, panNumber);
                requestBody.put(Constants.KEY_PRODUCT_CREATOR_ID, preferenceManager.getString(Constants.KEY_USER_ID));
                requestBody.put(Constants.KEY_COMPANY_ID, preferenceManager.getString(Constants.KEY_COMPANY_ID));

            } catch (JSONException e) {
                e.printStackTrace();
            }

            apiHandler.makeApiCall(Constants.KEY_CREATE_LEDGER_API,
                    Request.Method.POST, requestBody, new ApiHandler.ApiResponseListener() {
                        @Override
                        public void onSuccess(JSONObject response) {
                            // Handle success response
                            try {

                                boolean success = response.getBoolean(Constants.KEY_SUCCESS);
                                String errorMessage = response.getString("message");

                                if (success) {
                                    Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_SHORT).show();
                                    bottomSheetDialog.dismiss();
                                    loadingDialog.dismiss();
                                }else{

                                    loadingDialog.dismiss();
                                    Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_SHORT).show();
                                }

                            } catch (JSONException e) {
                                loadingDialog.dismiss();
                                e.printStackTrace();
                                Log.e(TAG,"Error: "+ e);
                                Toast.makeText(getApplicationContext(), "Error parsing JSON response", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onError(VolleyError error) {
                            loadingDialog.dismiss();

                            Log.e(TAG,"Error: "+error.getMessage());
                            Toast.makeText(getApplicationContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

        });


    }



    private void getLedgerDetails(){
        loadingDialog.show();
        apiHandler.makeApiCall(Constants.KEY_GET_LEDGER_DATA_API+preferenceManager.getString(Constants.KEY_COMPANY_ID),
                Request.Method.GET, null, new ApiHandler.ApiResponseListener() {
                    @Override
                    public void onSuccess(JSONObject response) {
                        try {
                            boolean success = response.getBoolean(Constants.KEY_SUCCESS);
                            String message = response.getString("message");
                            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();

                            if (success) {
                                ledgerModel.clear();
                                JSONArray items = response.getJSONArray(Constants.KEY_LEDGER);
                                for (int i = 0; i < items.length(); i++) {
                                    JSONObject item = items.getJSONObject(i);
                                    LedgerModel itemModel = new LedgerModel();
                                    itemModel.ledgerId = item.getString(Constants.KEY_MONGO_ID);
                                    itemModel.ledgerName = item.getString(Constants.KEY_NAME);
                                    itemModel.ledgerAlias = item.getString(Constants.KEY_ALIAS);
                                    itemModel.ledgerParent = item.getString(Constants.KEY_PARENT);
                                    itemModel.ledgerAddress_1 = item.getString(Constants.KEY_ADDRESS_1);
                                    itemModel.ledgerAddress_2 = item.getString(Constants.KEY_ADDRESS_2);
                                    itemModel.ledgerAddress_3 = item.getString(Constants.KEY_ADDRESS_3);
                                    itemModel.gst = item.getString(Constants.KEY_PRODUCT_GST);
                                    itemModel.pan = item.getString(Constants.KEY_PAN);
                                    itemModel.createAt = item.getString(Constants.KEY_PRODUCT_CREATE_AT);
                                    itemModel.creatorId = preferenceManager.getString(Constants.KEY_USER_ID);
                                    itemModel.isSynced = item.getBoolean(Constants.KEY_IS_SYNCED);
                                    if (!item.getBoolean(Constants.KEY_IS_SYNCED)){
                                        ledgerModel.add(itemModel);
                                    }

                                }


                                ledgerModel.sort((itemModel1, itemModel2) -> itemModel1.ledgerName
                                        .compareToIgnoreCase(itemModel2.ledgerName));

                                if (ledgerModel.size()>0){
                                    loadingDialog.dismiss();
                                    LedgerAdapter ledgerAdapter = new LedgerAdapter(ledgerModel, LedgerActivity.this,binding.fabButton);
                                    binding.ledgersRecyclerView.setVisibility(View.VISIBLE);
                                    binding.ledgersRecyclerView.setAdapter(ledgerAdapter);
                                    binding.swipeRefreshLayout.setRefreshing(false);
                                }else {
                                    loadingDialog.dismiss();
                                    showErrorMessage();
                                }


                            }else{
                                loadingDialog.dismiss();
                                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                            }

                        } catch (JSONException e) {
                            showErrorMessage();
                            loadingDialog.dismiss();
                            e.printStackTrace();
                            Toast.makeText(getApplicationContext(), "Error parsing JSON response", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onError(VolleyError error) {
                        showErrorMessage();
                        loadingDialog.dismiss();
                        Toast.makeText(getApplicationContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                });



    }
    private void showErrorMessage(){
        binding.notFoundAnimation.setVisibility(View.VISIBLE);
        binding.swipeRefreshLayout.setRefreshing(false);

    }

    @Override
    public void onLedgerClicked(LedgerModel model) {

    }
}