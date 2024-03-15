package com.socialtools.tallymobile;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.socialtools.tallymobile.Utils.Constants;
import com.socialtools.tallymobile.Utils.LoadingDialog;
import com.socialtools.tallymobile.Utils.PreferenceManager;
import com.socialtools.tallymobile.databinding.ActivityCreateItemBinding;


import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;

public class CreateItemActivity extends AppCompatActivity {

    private ActivityCreateItemBinding binding;
    private String productName,partNumber,uom,hsnCode,mrp,gst,purchasePrice,sellPrice,productDescription;
    private FirebaseFirestore database;
    private PreferenceManager preferenceManager;
    private LoadingDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCreateItemBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
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
        HashMap<String,Object> product = new HashMap<>();
        product.put(Constants.KEY_PRODUCT_NAME,productName);
        product.put(Constants.KEY_PRODUCT_DESCRIPTION,productDescription);
        product.put(Constants.KEY_PRODUCT_PART_NUMBER,partNumber);
        product.put(Constants.KEY_PRODUCT_UOM,uom);
        product.put(Constants.KEY_PRODUCT_HSN_CODE,hsnCode);
        product.put(Constants.KEY_PRODUCT_MRP,mrp);
        product.put(Constants.KEY_PRODUCT_GST,gst);
        product.put(Constants.KEY_IS_SYNCED,false);
        product.put(Constants.KEY_DATE,new Date());
        product.put(Constants.KEY_PRODUCT_PURCHASE_PRICE,purchasePrice);
        product.put(Constants.KEY_PRODUCT_SELLS_PRICE,sellPrice);


        database.collection(Constants.KEY_COLLECTION_COMPANY)
                .document(preferenceManager.getString(Constants.KEY_TALLY_ID))
                .collection(Constants.KEY_COLLECTION_ITEMS)
                .add(product)
                .addOnSuccessListener(documentReference -> {
                    String generatedId = documentReference.getId();
                    database.collection(Constants.KEY_COLLECTION_COMPANY)
                            .document(preferenceManager.getString(Constants.KEY_TALLY_ID))
                            .collection(Constants.KEY_COLLECTION_ITEMS)
                            .document(generatedId)
                            .update(Constants.KEY_PRODUCT_ID,generatedId)
                            .addOnSuccessListener(aVoid->{
                                loading(false);
                                Toast.makeText(this, "Item created successfully", Toast.LENGTH_SHORT).show();
                            }).addOnFailureListener(e-> {
                                loading(false);
                                Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                            });

                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                    // Handle errors here
                });

    }

}