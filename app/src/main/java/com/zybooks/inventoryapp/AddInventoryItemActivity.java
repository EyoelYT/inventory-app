package com.zybooks.inventoryapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.zybooks.inventoryapp.inventoryschemas.InventoryDbHelper;
import com.zybooks.inventoryapp.inventoryschemas.InventoryTable;

public class AddInventoryItemActivity extends AppCompatActivity {

    private InventoryDbHelper dbHelper;
    private EditText nameEditText, quantityEditText, descriptionEditText;
    private Button saveButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_inventory_item);
        setTitle("Add Inventory");

        dbHelper = InventoryDbHelper.getInstance(this);

        nameEditText = findViewById(R.id.name_entry);
        quantityEditText = findViewById(R.id.quantity_entry);
        descriptionEditText = findViewById(R.id.description_entry);
        saveButton = findViewById(R.id.save_button);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveInventoryItem();
            }
        });
    }

    private void saveInventoryItem() {
        String name = nameEditText.getText().toString();
        int quantity = Integer.parseInt(quantityEditText.getText().toString());
        String description = descriptionEditText.getText().toString();

        // Add item to the database
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(InventoryTable.COLUMN_NAME_TITLE, name);
        values.put(InventoryTable.COLUMN_NAME_QUANTITY, quantity);
        values.put(InventoryTable.COLUMN_NAME_DESCRIPTION, description);

        long newRowId = db.insert(InventoryTable.TABLE_NAME, null, values);

        if (newRowId == -1) {
            // Handle the error
            Toast.makeText(this, "Error saving the inventory item", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Inventory item saved", Toast.LENGTH_SHORT).show();
            finish(); // Close this activity and go back to the previous one
        }
    }


}