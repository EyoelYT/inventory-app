package com.zybooks.inventoryapp.viewadapters;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

import com.bumptech.glide.Glide;
import com.zybooks.inventoryapp.R;
import com.zybooks.inventoryapp.inventorymodels.Inventory;

import java.util.List;

public class InventoryAdapter extends RecyclerView.Adapter<InventoryAdapter.InventoryViewHolder> {

    public interface OnRecyclerInventoryClickListner {
        void onRecyclerClick(Inventory inventory);
    }

    private List<Inventory> inventoryList;
    private OnRecyclerInventoryClickListner listner;

    public InventoryAdapter(List<Inventory> inventoryList, OnRecyclerInventoryClickListner listner) {
        this.inventoryList = inventoryList;
        this.listner = listner;
    }

    // Creates the small empty recycler view activity
    @NonNull
    @Override
    public InventoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.inventory_item_layout, parent, false);
        return new InventoryViewHolder(itemView);
    }

    // Populates the recycler view
    @Override
    public void onBindViewHolder(@NonNull InventoryViewHolder holder, int position) {

        Inventory item = inventoryList.get(position);

        holder.itemView.setOnClickListener(view -> {listner.onRecyclerClick(item);});
        holder.nameTextView.setText(item.getName());
        holder.quantityTextView.setText(String.valueOf(item.getQuantity()));
        holder.descriptionTextView.setText(item.getDescription());

        // Set image using Glide
        if (item.getImageUri() != null && !item.getImageUri().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(Uri.parse(item.getImageUri())) // Make sure to parse the URI
                    .into(holder.imageView);
        } else {
            holder.imageView.setImageResource(R.drawable.inventory_placeholder); // A default placeholder if there's no image
        }
    }

    @Override
    public int getItemCount() {
        return inventoryList.size();
    }

    static class InventoryViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView, quantityTextView, descriptionTextView;
        ImageView imageView;

        InventoryViewHolder(View view) {
            super(view);
            nameTextView = view.findViewById(R.id.inventory_name);
            quantityTextView = view.findViewById(R.id.inventory_quantity);
            descriptionTextView = view.findViewById(R.id.inventory_description);
            imageView = view.findViewById(R.id.inventory_image);
        }
    }
}
