package com.socialtools.tallymobile.adapters;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.socialtools.tallymobile.Listeners.ItemListener;
import com.socialtools.tallymobile.Listeners.LedgerListener;
import com.socialtools.tallymobile.Models.ItemModel;
import com.socialtools.tallymobile.Models.LedgerModel;
import com.socialtools.tallymobile.Utils.Constants;
import com.socialtools.tallymobile.Utils.DateTimeUtils;
import com.socialtools.tallymobile.activities.CreateItemActivity;
import com.socialtools.tallymobile.databinding.ItemContainerBinding;
import com.socialtools.tallymobile.databinding.ItemLedgerContainerBinding;

import java.util.List;
import java.util.Objects;

public class LedgerAdapter extends RecyclerView.Adapter<LedgerAdapter.LedgerViewHolder> {
    private final List<LedgerModel> ledgerModel;
    private final LedgerListener ledgerListener;
    private final ExtendedFloatingActionButton fab_button;

    public LedgerAdapter(List<LedgerModel> ledgerModel, LedgerListener ledgerListener, ExtendedFloatingActionButton fab_button) {
        this.ledgerModel = ledgerModel;
        this.ledgerListener = ledgerListener;
        this.fab_button = fab_button;
    }


    @NonNull
    @Override
    public LedgerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        return new LedgerAdapter.LedgerViewHolder(ItemLedgerContainerBinding.inflate(inflater,parent,false)
        );
    }

    @Override
    public void onBindViewHolder(@NonNull LedgerAdapter.LedgerViewHolder holder, int position) {
        holder.setData(ledgerModel.get(position));
        if (getItemCount()>=6){
            if (position == getItemCount() - 1) {
                fab_button.hide();
            } else {
                fab_button.show();
            }
        }

    }

    @Override
    public int getItemCount() {
        return ledgerModel.size();
    }

    class LedgerViewHolder extends RecyclerView.ViewHolder {
        ItemLedgerContainerBinding binding;


        LedgerViewHolder(ItemLedgerContainerBinding itemLedgerContainerBinding) {
            super(itemLedgerContainerBinding.getRoot());
            binding = itemLedgerContainerBinding;
        }

        void setData(LedgerModel model) {
            binding.textLedgerName.setText(String.format("Ledger Name:- %s",model.ledgerName));
            binding.textAlias.setText(String.format("Alias:- %s",model.ledgerAlias));
            binding.textParent.setText(String.format("Parent:- %s",model.ledgerParent));
            binding.createDateTime.setText(String.format("Create At:- %s", DateTimeUtils.convertToReadableDateTime(model.createAt)));

        }
    }

}