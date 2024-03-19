package com.socialtools.tallymobile;

import static android.content.ContentValues.TAG;


import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import android.view.View;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.google.firebase.firestore.FirebaseFirestore;
import com.socialtools.tallymobile.Utils.ApiHandler;
import com.socialtools.tallymobile.Utils.Constants;
import com.socialtools.tallymobile.Utils.LoadingDialog;
import com.socialtools.tallymobile.Utils.PreferenceManager;
import com.socialtools.tallymobile.databinding.ActivityCreateItemBinding;


import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.HashMap;
import java.util.Objects;


public class CreateItemActivity extends AppCompatActivity {

    private ActivityCreateItemBinding binding;
    private String productName,partNumber,uom,hsnCode,mrp,gst,purchasePrice,sellPrice,productDescription;
    private FirebaseFirestore database;
    private PreferenceManager preferenceManager;
    private LoadingDialog loadingDialog;
    private ApiHandler apiHandler ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCreateItemBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        apiHandler = new ApiHandler(this);
        database = FirebaseFirestore.getInstance();
        loadingDialog = new LoadingDialog(this);
        preferenceManager=new PreferenceManager(this);
        binding.createItemBtn.setOnClickListener(v-> checkValidSignUpDetails());


    }


    private void loading(Boolean isLoading){
        if(isLoading){
            loadingDialog.show();
            binding.createItemBtn.setVisibility(View.INVISIBLE);
            binding.pBar1.setVisibility(View.VISIBLE);
        }else{
            loadingDialog.dismiss();
            binding.pBar1.setVisibility(View.INVISIBLE);
            binding.createItemBtn.setVisibility(View.VISIBLE);
        }
    }



    private void checkValidSignUpDetails() {
        productName = Objects.requireNonNull(binding.editTextProductName.getText()).toString().trim();
        partNumber = Objects.requireNonNull(binding.editTextPathNumber.getText()).toString().trim();
        uom = Objects.requireNonNull(binding.editTextUom.getText()).toString().trim();
        hsnCode = Objects.requireNonNull(binding.editTextHsnCode.getText()).toString().trim();
        mrp = Objects.requireNonNull(binding.editTextMrp.getText()).toString().trim();
        gst = Objects.requireNonNull(binding.editTextGst.getText()).toString().trim();
        purchasePrice = Objects.requireNonNull(binding.editTextPurchasePrice.getText()).toString().trim();
        sellPrice = Objects.requireNonNull(binding.editTextSellPrice.getText()).toString().trim();
        productDescription = Objects.requireNonNull(binding.editTextDescription.getText()).toString().trim();


        if (!productName.isEmpty()) {
            binding.textInputProductName.setError(null);
            if (!partNumber.isEmpty()) {
                binding.textInputPathNumber.setError(null);
                if (!uom.isEmpty()) {
                    binding.textInputUom.setError(null);
                    if (!hsnCode.isEmpty()) {
                        binding.textInputHsnCode.setError(null);
                        if (!mrp.isEmpty()){
                            binding.textInputMrp.setError(null);
                            if (!gst.isEmpty()){
                                binding.textInputPurchasePrice.setError(null);
                                if(!purchasePrice.isEmpty()){
                                    binding.textInputPurchasePrice.setError(null);
                                    if(sellPrice!=null){
                                        binding.textInputSellPrice.setError(null);
                                        if(!productDescription.isEmpty()){
                                            binding.textInputDescription.setError(null);

                                            createItemInServer();

                                        }else{
                                            binding.textInputDescription.setError("Please enter product description");
                                        }
                                    }else{
                                        binding.textInputSellPrice.setError("Please enter sell price");
                                    }
                                }else{
                                    binding.textInputPurchasePrice.setError("Please enter purchase price");
                                }

                            }else{
                                binding.textInputGst.setError("Please Enter GST percentage");
                            }
                        }else {
                            binding.textInputMrp.setError("Please Enter MRP");
                        }
                    } else {
                        binding.textInputHsnCode.setError("Please Enter HSN code");
                    }
                } else {
                    binding.textInputUom.setError("Please Enter Unit");
                }
            } else {
                binding.textInputPathNumber.setError("Please Enter Path number");
            }
        } else {
            binding.textInputProductName.setError("Please Enter Product Name");
        }
    }



    private void createItemInServer(){
        loading(true);

        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put(Constants.KEY_PRODUCT_NAME, productName);
            requestBody.put(Constants.KEY_PRODUCT_DESCRIPTION, productDescription);
            requestBody.put(Constants.KEY_PRODUCT_PART_NUMBER, partNumber);
            requestBody.put(Constants.KEY_PRODUCT_UOM, uom);
            requestBody.put(Constants.KEY_PRODUCT_GST,gst);
            requestBody.put(Constants.KEY_PRODUCT_HSN_CODE, hsnCode);
            requestBody.put(Constants.KEY_PRODUCT_MRP, mrp);
            requestBody.put(Constants.KEY_PRODUCT_PURCHASE_PRICE, purchasePrice);
            requestBody.put(Constants.KEY_PRODUCT_SELLS_PRICE, sellPrice);
            requestBody.put(Constants.KEY_COMPANY_ID, preferenceManager.getString(Constants.KEY_COMPANY_ID));
            requestBody.put(Constants.KEY_PRODUCT_CREATOR_ID, preferenceManager.getString(Constants.KEY_USER_ID));

        } catch (JSONException e) {
            e.printStackTrace();
        }

        apiHandler.makeApiCall(Constants.KEY_CREATE_ITEM_API,
                Request.Method.POST, requestBody, new ApiHandler.ApiResponseListener() {
                    @Override
                    public void onSuccess(JSONObject response) {
                        // Handle success response
                        try {

                            boolean success = response.getBoolean(Constants.KEY_SUCCESS);
                            String errorMessage = response.getString("message");

                            if (success) {
                                Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_SHORT).show();
                                loading(false);
                                loadingDialog.dismiss();
                            }else{
                                loading(false);
                                loadingDialog.dismiss();
                                Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_SHORT).show();
                            }

                        } catch (JSONException e) {
                            loading(false);
                            loadingDialog.dismiss();
                            e.printStackTrace();
                            Toast.makeText(getApplicationContext(), "Error parsing JSON response", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onError(VolleyError error) {
                        // Handle error
                        loadingDialog.dismiss();
                        loading(false);

                        Toast.makeText(getApplicationContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();

    }

}