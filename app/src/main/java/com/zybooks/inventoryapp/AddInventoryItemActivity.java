package com.zybooks.inventoryapp;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.zybooks.inventoryapp.inventoryschemas.InventoryDbHelper;
import com.zybooks.inventoryapp.inventoryschemas.InventoryTable;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AddInventoryItemActivity extends AppCompatActivity {
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_PERMISSIONS = 100;

    private InventoryDbHelper dbHelper;
    private EditText nameEditText, quantityEditText, descriptionEditText;
    private ImageButton imageButton;
    private Uri imageUri;
    private Button saveButton;

    private ActivityResultLauncher<Intent> mStartForResult = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        // Handle the captured image
                        if (imageUri != null) {
                            Glide.with(AddInventoryItemActivity.this).load(imageUri).into(imageButton);
                        }
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_inventory_item);
        setTitle("Add Inventory");

        dbHelper = InventoryDbHelper.getInstance(getApplicationContext());

        nameEditText = findViewById(R.id.name_entry);
        quantityEditText = findViewById(R.id.quantity_entry);
        descriptionEditText = findViewById(R.id.description_entry);
        imageButton = (ImageButton) findViewById(R.id.image_button);
        saveButton = findViewById(R.id.save_button);

        // Set up the image button to launch the camera
        imageButton.setOnClickListener(view -> {
            requestNecessaryPermissions();
        });

        // TextWatcher to enable the save button when name and quantity are not empty
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                saveButton.setEnabled(!nameEditText.getText().toString().isEmpty() && !quantityEditText.getText().toString().isEmpty());
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        };

        nameEditText.addTextChangedListener(textWatcher);
        quantityEditText.addTextChangedListener(textWatcher);

        saveButton.setOnClickListener(view -> saveInventoryItem());
    }

    private void saveInventoryItem() {
        String name = nameEditText.getText().toString();
        int quantity = Integer.parseInt(quantityEditText.getText().toString());
        String description = descriptionEditText.getText().toString();

        // Add item to the database
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(InventoryTable.COLUMN_NAME_TITLE, name);
        values.put(InventoryTable.COLUMN_NAME_IMAGE, imageUri.toString());
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

    private void launchCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                Toast.makeText(this, "Error creating image file.", Toast.LENGTH_SHORT).show();
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                imageUri = FileProvider.getUriForFile(this, "your.package.fileprovider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",   /* suffix */
                storageDir      /* directory */
        );
        // Save a file: path for use with ACTION_VIEW intents
        return image;
    }

    private void requestNecessaryPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_PERMISSIONS);
        } else {
            openCamera();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(this, "Camera and Storage permissions are necessary", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
                if (photoFile != null) {
                    imageUri = FileProvider.getUriForFile(this, "your.package.fileprovider", photoFile);
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                    mStartForResult.launch(takePictureIntent);
                }
            } catch (IOException ex) {
                // Error occurred while creating the File
                Log.d("createImageFile", "Did not create image file");
                Toast.makeText(AddInventoryItemActivity.this, "Image file could not be created due to an error", Toast.LENGTH_SHORT);
            }
        }
    }

}