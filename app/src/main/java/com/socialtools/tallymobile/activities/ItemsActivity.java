package com.socialtools.tallymobile.activities;
import static android.content.ContentValues.TAG;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.socialtools.tallymobile.Listeners.ItemListener;
import com.socialtools.tallymobile.Models.ItemModel;
import com.socialtools.tallymobile.Utils.ApiHandler;
import com.socialtools.tallymobile.Utils.Constants;
import com.socialtools.tallymobile.Utils.LoadingDialog;
import com.socialtools.tallymobile.Utils.PreferenceManager;
import com.socialtools.tallymobile.adapters.ItemAdapter;
import com.socialtools.tallymobile.databinding.ActivityItemsBinding;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class ItemsActivity extends AppCompatActivity implements ItemListener {
    private ActivityItemsBinding binding;
    private ApiHandler apiHandler;
    private PreferenceManager preferenceManager;
    private LoadingDialog loadingDialog;
    private List<ItemModel>itemModels;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityItemsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        apiHandler = new ApiHandler(this);
        preferenceManager = new PreferenceManager(this);
        loadingDialog = new LoadingDialog(this);
        getItemsDetails();
        init();


    }

    private void init(){
        binding.imageBack.setOnClickListener(v-> finish());
        binding.fabButton.setOnClickListener(v-> startActivity(new Intent(this, CreateItemActivity.class)));
        binding.swipeRefreshLayout.setOnRefreshListener(this::getItemsDetails);
        itemModels = new ArrayList<>();

    }



    private void showErrorMessage(){
        binding.notFoundAnimation.setVisibility(View.VISIBLE);
        binding.swipeRefreshLayout.setRefreshing(false);

    }


    private void getItemsDetails(){
        loadingDialog.show();
        apiHandler.makeApiCall(Constants.KEY_GET_ITEM_DATA_API + preferenceManager.getString(Constants.KEY_COMPANY_ID),
                Request.Method.GET, null, new ApiHandler.ApiResponseListener() {
                    @Override
                    public void onSuccess(JSONObject response) {
                        // Handle success response
                        try {

                            boolean success = response.getBoolean(Constants.KEY_SUCCESS);
                            String message = response.getString("message");


                            if (success) {
                                itemModels.clear();
                                JSONArray items = response.getJSONArray(Constants.KEY_ITEMS);

                                for (int i = 0; i < items.length(); i++) {
                                    JSONObject item = items.getJSONObject(i);

                                    ItemModel itemModel = new ItemModel();
                                    itemModel.itemId = item.getString(Constants.KEY_MONGO_ID);
                                    itemModel.itemName = item.getString(Constants.KEY_PRODUCT_NAME);
                                    itemModel.itemDescription = item.getString(Constants.KEY_PRODUCT_DESCRIPTION);
                                    itemModel.itemUom = item.getString(Constants.KEY_PRODUCT_UOM);
                                    itemModel.itemPartnumber = item.getString(Constants.KEY_PRODUCT_PART_NUMBER);
                                    itemModel.itemGst = item.getString(Constants.KEY_PRODUCT_GST);
                                    itemModel.itemPp = item.getString(Constants.KEY_PRODUCT_PURCHASE_PRICE);
                                    itemModel.itemSp = item.getString(Constants.KEY_PRODUCT_SELLS_PRICE);
                                    itemModel.itemMrp = item.getString(Constants.KEY_PRODUCT_MRP);
                                    itemModel.itemHsncode = item.getString(Constants.KEY_PRODUCT_HSN_CODE);
                                    itemModel.createAt = item.getString(Constants.KEY_PRODUCT_CREATE_AT);
                                    itemModel.alterAt = item.getString(Constants.KEY_PRODUCT_ALTER_AT);
                                    itemModel.alterId = item.getString(Constants.KEY_PRODUCT_ALTER_ID);
                                    itemModel.creatorId = preferenceManager.getString(Constants.KEY_USER_ID);
                                    itemModel.isSynced = item.getBoolean(Constants.KEY_IS_SYNCED);
                                    if (!item.getBoolean(Constants.KEY_IS_SYNCED)){
                                        itemModels.add(itemModel);
                                    }

                                }
                                itemModels.sort((itemModel1, itemModel2) -> itemModel1.itemName.compareToIgnoreCase(itemModel2.itemName));
                                if (itemModels.size()>0){
                                    loadingDialog.dismiss();
                                    ItemAdapter itemsAdapter = new ItemAdapter(itemModels, ItemsActivity.this,binding.fabButton);
                                    binding.itemsRecyclerView.setVisibility(View.VISIBLE);
                                    binding.itemsRecyclerView.setAdapter(itemsAdapter);
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

                            loadingDialog.dismiss();
                            e.printStackTrace();
                            Toast.makeText(getApplicationContext(), "Error parsing JSON response", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onError(VolleyError error) {
                        Toast.makeText(getApplicationContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                });



    }
    @Override
    public void onItemClicked(ItemModel itemModel) {


    }
}