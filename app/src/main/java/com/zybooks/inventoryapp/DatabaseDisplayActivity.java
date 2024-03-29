package com.zybooks.inventoryapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.zybooks.inventoryapp.inventorymodels.Inventory;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.database.Cursor;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.zybooks.inventoryapp.inventoryschemas.InventoryDbHelper;
import com.zybooks.inventoryapp.inventoryschemas.InventoryTable;
import com.zybooks.inventoryapp.viewadapters.InventoryAdapter;

import java.util.ArrayList;
import java.util.List;

public class DatabaseDisplayActivity extends AppCompatActivity implements InventoryAdapter.OnRecyclerInventoryClickListner {

    private RecyclerView recyclerView;
    private InventoryAdapter inventoryAdapter;
    private List<Inventory> inventoryList;
    private InventoryDbHelper dbHelper;
    private static final int EDIT_INVENTORY_REQUEST = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_database_display);
        setTitle("Inventory");

        MaterialToolbar toolbar = findViewById(R.id.top_app_bar);
        setSupportActionBar(toolbar);

        dbHelper = InventoryDbHelper.getInstance(getApplicationContext());
        inventoryList = new ArrayList<>();

        recyclerView = findViewById(R.id.inventoryRecyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        InventoryAdapter.OnRecyclerInventoryClickListner clickListener = inventory -> {
            // Create an intent for the EditActivity and pass the inventory item details
            Intent editIntent = new Intent(DatabaseDisplayActivity.this, EditInventoryItemActivity.class);
            editIntent.putExtra("inventory_id", inventory.getId());
            startActivity(editIntent);
        };

        inventoryAdapter = new InventoryAdapter(this, inventoryList, this); // Error here?

        loadInventoryItems();

        recyclerView.setAdapter(inventoryAdapter);

        FloatingActionButton fabAddItem = findViewById(R.id.fab_add_item);
        fabAddItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(DatabaseDisplayActivity.this, AddInventoryItemActivity.class);
                startActivity(intent);
            }
        });
    }

    private void loadInventoryItems() {
        inventoryList.clear();

        // Get the database in readable mode
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // Define a projection specifying which columns to use after the query
        String[] projection = {
                InventoryTable._ID,
                InventoryTable.COLUMN_NAME_TITLE,
                InventoryTable.COLUMN_NAME_IMAGE,
                InventoryTable.COLUMN_NAME_QUANTITY,
                InventoryTable.COLUMN_NAME_DESCRIPTION
        };

        // Perform the query to get all items
        Cursor cursor = db.query(InventoryTable.TABLE_NAME, projection,
                null,              
                null,          
                null,         
                null,       
                null       
        );

        try {
            while (cursor.moveToNext()) {

                int id = cursor.getInt(cursor.getColumnIndexOrThrow(InventoryTable._ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(InventoryTable.COLUMN_NAME_TITLE));
                String imageUri = cursor.getString(cursor.getColumnIndexOrThrow(InventoryTable.COLUMN_NAME_IMAGE));
                int quantity = cursor.getInt(cursor.getColumnIndexOrThrow(InventoryTable.COLUMN_NAME_QUANTITY));
                String description = cursor.getString(cursor.getColumnIndexOrThrow(InventoryTable.COLUMN_NAME_DESCRIPTION));
                Inventory item = new Inventory(id, name, imageUri, quantity, description);

                inventoryList.add(item);
            }

        } finally {
            cursor.close();
        }

        // Notify the adapter that the data set has changed to refresh the RecyclerView
        inventoryAdapter.notifyDataSetChanged();
    }

    @Override
    public void onRecyclerClick(Inventory inventory) {
        // Start EditInventoryItemActivity with item details as extras
        Intent intent = new Intent(this, EditInventoryItemActivity.class);
        intent.putExtra("inventory_id", inventory.getId());
        intent.putExtra("inventory_name", inventory.getName());
        intent.putExtra("inventory_quantity", inventory.getQuantity());
        intent.putExtra("inventory_description", inventory.getDescription());
        intent.putExtra("inventory_imageUri", inventory.getImageUri());
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == EDIT_INVENTORY_REQUEST) {
            if (resultCode == RESULT_OK) {
                loadInventoryItems();
                inventoryAdapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        setTitle("Inventory");
        loadInventoryItems();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.top_app_bar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_notifications) {
            // Open the notifications setting activity
            Intent intent = new Intent(this, NotificationSettingsActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
