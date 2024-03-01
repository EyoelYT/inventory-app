package com.zybooks.inventoryapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import android.os.Handler;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.zybooks.inventoryapp.inventoryschemas.InventoryDbHelper;
import com.zybooks.inventoryapp.inventoryschemas.InventoryTable;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class EditInventoryItemActivity extends AppCompatActivity {
    private boolean backButtonPressedOnce = false;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private EditText nameEditText, quantityEditText, descriptionEditText;
    private ImageButton imageButton;
    private Button btnSave, btnDelete, btnGoBack;
    private InventoryDbHelper dbHelper;
    private long inventoryId;
    private Uri photoURI;
    String imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_inventory_item);

        dbHelper = InventoryDbHelper.getInstance(this);
        inventoryId = getIntent().getLongExtra("inventory_id", -1);

        nameEditText = findViewById(R.id.editInventoryName);
        quantityEditText = findViewById(R.id.editInventoryQuantity);
        descriptionEditText = findViewById(R.id.editInventoryDescription);
        imageButton = findViewById(R.id.inventoryImage);

        btnSave = findViewById(R.id.btnSave);
        btnDelete = findViewById(R.id.btnDelete);
        btnGoBack = findViewById(R.id.btnGoBack);

        // Populate the edit texts with existing values in the database
        loadInventoryItem();

        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                btnSave.setEnabled(true); // Enable save button when text changes
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        };

        // Enable the save button when an edit text is changed
        nameEditText.addTextChangedListener(watcher);
        quantityEditText.addTextChangedListener(watcher);
        descriptionEditText.addTextChangedListener(watcher);

        btnSave.setOnClickListener(v -> saveInventoryItem());
        btnDelete.setOnClickListener(v -> deleteInventoryItem());
        btnGoBack.setOnClickListener(v -> finish());

        imageButton.setOnClickListener(v -> onChangeImageClicked(v));
    }

    public void onChangeImageClicked(View view) {
        // Intent to capture an image or pick from gallery
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create file for the image
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the file
                Log.d("Error", "Error occured while creating file for changing image");
            }
            // Continue going to the camera only if the file was successfully created
            if (photoFile != null) {
                photoURI = FileProvider.getUriForFile(this,
                        "com.zybooks.inventoryapp.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    private void loadInventoryItem() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String[] projection = {
                InventoryTable._ID,
                InventoryTable.COLUMN_NAME_TITLE,
                InventoryTable.COLUMN_NAME_IMAGE,
                InventoryTable.COLUMN_NAME_QUANTITY,
                InventoryTable.COLUMN_NAME_DESCRIPTION
        };

        String selection = InventoryTable._ID + " = ?";
        String[] selectionArgs = { String.valueOf(inventoryId) };

        Cursor cursor = db.query(InventoryTable.TABLE_NAME, projection, selection, selectionArgs,
                         null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            String name = cursor.getString(cursor.getColumnIndexOrThrow(InventoryTable.COLUMN_NAME_TITLE));
            imageUri = cursor.getString(cursor.getColumnIndexOrThrow(InventoryTable.COLUMN_NAME_IMAGE));
            int quantity = cursor.getInt(cursor.getColumnIndexOrThrow(InventoryTable.COLUMN_NAME_QUANTITY));
            String description = cursor.getString(cursor.getColumnIndexOrThrow(InventoryTable.COLUMN_NAME_DESCRIPTION));

            // Populate the edit texts with default values
            nameEditText.setText(name);
            quantityEditText.setText(String.valueOf(quantity));
            descriptionEditText.setText(description);

            if (imageUri != null && !imageUri.equals("placeholder_image")) {
                Glide.with(this).load(Uri.parse(imageUri)).into(imageButton);
            } else {
                imageButton.setImageResource(R.drawable.inventory_placeholder);
            }

            cursor.close();
        }
    }

    private void saveInventoryItem() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(InventoryTable.COLUMN_NAME_TITLE, nameEditText.getText().toString());
        values.put(InventoryTable.COLUMN_NAME_QUANTITY, Integer.parseInt(quantityEditText.getText().toString()));
        values.put(InventoryTable.COLUMN_NAME_DESCRIPTION, descriptionEditText.getText().toString());

        if (imageUri != null) {
            values.put(InventoryTable.COLUMN_NAME_IMAGE, imageUri);
        } else {
            imageButton.setImageResource(R.drawable.inventory_placeholder);
        }

        String selection = InventoryTable._ID + " = ?";
        String[] selectionArgs = { String.valueOf(inventoryId) };

        int count = db.update(InventoryTable.TABLE_NAME, values, selection, selectionArgs);

        if (count > 0) {
            Toast.makeText(this, "Inventory updated", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Update failed", Toast.LENGTH_SHORT).show();
        }

        finish();
    }


    private void deleteInventoryItem() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String selection = InventoryTable._ID + " = ?";
        String[] selectionArgs = { String.valueOf(inventoryId) };

        int deletedRows = db.delete(InventoryTable.TABLE_NAME, selection, selectionArgs);

        if (deletedRows > 0) {
            Toast.makeText(this, "Inventory deleted", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Deletion failed", Toast.LENGTH_SHORT).show();
        }

        finish();
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir
        );

        // Save a file path for use with ACTION_VIEW intents
        imageUri = image.getAbsolutePath();
        return image;
    }

    @Override
    public void onBackPressed() {
        if (!backButtonPressedOnce) {
            backButtonPressedOnce = true;
            new Handler().postDelayed(() -> backButtonPressedOnce = false, 200); // 200 ms
            super.onBackPressed();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            imageUri = photoURI.toString();
            Glide.with(this).load(photoURI).into(imageButton);
            btnSave.setEnabled(true);
        }
    }

    @Override
    public void finish() {
        super.finish();

        Intent intent = new Intent(this, DatabaseDisplayActivity.class);

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

}
