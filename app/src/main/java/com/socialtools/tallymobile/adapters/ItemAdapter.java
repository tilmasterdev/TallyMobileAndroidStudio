package com.socialtools.tallymobile.adapters;


import android.content.Intent;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import com.socialtools.tallymobile.Listeners.ItemListener;
import com.socialtools.tallymobile.Models.ItemModel;

import com.socialtools.tallymobile.Utils.Constants;
import com.socialtools.tallymobile.Utils.DateTimeUtils;
import com.socialtools.tallymobile.activities.CreateItemActivity;
import com.socialtools.tallymobile.databinding.ItemContainerBinding;

import java.util.List;
import java.util.Objects;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ItemViewHolder> {
    private final List<ItemModel> itemModels;
    private  final ItemListener itemListener;
    private final ExtendedFloatingActionButton fab_button;

    public ItemAdapter(List<ItemModel> itemModels, ItemListener itemListener,ExtendedFloatingActionButton fab_button) {
        this.itemModels = itemModels;
        this.itemListener = itemListener;
        this.fab_button = fab_button;
    }


    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        return new ItemAdapter.ItemViewHolder(
                ItemContainerBinding.inflate(inflater,parent,false)
        );
    }

    @Override
    public void onBindViewHolder(@NonNull ItemAdapter.ItemViewHolder holder, int position) {
        holder.setData(itemModels.get(position));
        if (getItemCount()>=5) {
            if (position == getItemCount() - 1) {
                fab_button.hide();
            } else {
                fab_button.show();
            }
        }
    }

    @Override
    public int getItemCount() {
        return itemModels.size();
    }

   class ItemViewHolder extends RecyclerView.ViewHolder {
        ItemContainerBinding binding;


        ItemViewHolder(ItemContainerBinding itemContainerBinding){
            super(itemContainerBinding.getRoot());
            binding = itemContainerBinding;
        }

        void setData(ItemModel itemModel){
            binding.textProductName.setText(itemModel.itemName);
            binding.textDescription.setText(itemModel.itemDescription);
            binding.textMrp.setText(String.format("MRP:- ₹ %s", itemModel.itemMrp));
            binding.textSellsPrice.setText(String.format("S.P:- ₹ %s", itemModel.itemSp));
            binding.textPurchasePrice.setText(String.format("P.P:- ₹ %s", itemModel.itemPp));
            binding.createDateTime.setText(String.format("Create At:- %s", DateTimeUtils.convertToReadableDateTime(itemModel.createAt)));
            binding.alterDateTime.setText(Objects.equals(itemModel.alterAt, "null") || itemModel.alterAt.isEmpty() ? "Not Yet Altered"
                    : String.format("Alter At:- %s", DateTimeUtils.convertToReadableDateTime(itemModel.alterAt)));

            binding.alterBtn.setOnClickListener(v-> {
                Intent i =new Intent(v.getContext(), CreateItemActivity.class);
                i.putExtra(Constants.KEY_ITEMS,itemModel);
                v.getContext().startActivity(i);
            });

        }
   }
}
