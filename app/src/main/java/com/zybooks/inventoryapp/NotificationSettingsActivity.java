package com.zybooks.inventoryapp;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import android.text.Editable;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.Manifest;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.zybooks.inventoryapp.inventoryschemas.InventoryDbHelper;
import com.zybooks.inventoryapp.inventoryschemas.InventoryTable;

public class NotificationSettingsActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_SEND_SMS = 123;

    private TextView phoneNumberPrompt;
    private EditText phoneNumberEntry;
    private Button requestPermissionButton;
    private TextView textNotificationPreferences;
    private Button savePhoneNumberButton;
    private SharedPreferences sharedPreferences;
    InventoryDbHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_settings);

        sharedPreferences = getSharedPreferences("AppPreferences", MODE_PRIVATE);
        dbHelper = InventoryDbHelper.getInstance(getApplicationContext());

        savePhoneNumberButton = findViewById(R.id.button_save_phone_number);
        savePhoneNumberButton.setOnClickListener(v -> savePhoneNumber());
        savePhoneNumberButton.setEnabled(false);

        phoneNumberPrompt = findViewById(R.id.phone_number_prompt);
        phoneNumberEntry = findViewById(R.id.phone_number_entry);
        requestPermissionButton = findViewById(R.id.button_request_permission);
        textNotificationPreferences = findViewById(R.id.text_notification_preferences);

        // Display the saved phone number if it exists
        phoneNumberEntry.setText(sharedPreferences.getString("savedPhoneNumber", ""));
        phoneNumberEntry.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Enable the save phone number button only if the entered number is 10 digits long
                savePhoneNumberButton.setEnabled(s.length() == 10);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        requestPermissionButton.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, PERMISSION_REQUEST_SEND_SMS);
            } else {
                Toast.makeText(this, "Go to settings to disable SMS permissions", Toast.LENGTH_LONG).show();
                disableSmsFeature();
            }
        });

        updateUiBasedOnPermission();
    }

    private void savePhoneNumber() {
        String phoneNumber = phoneNumberEntry.getText().toString();
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("savedPhoneNumber", phoneNumber);
        editor.apply();
        Toast.makeText(this, "Phone number saved", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantedResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantedResults);
        if (requestCode == PERMISSION_REQUEST_SEND_SMS) {
            if (grantedResults.length > 0 && grantedResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableSmsFeature();
            } else {
                disableSmsFeature();
            }
        }
        updateUiBasedOnPermission();
    }

    private void updateUiBasedOnPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
            enableSmsFeature();
        } else {
            // If permission is denied, update UI accordingly
            // UI is updated only in case of denial, not when permissions are not yet requested
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.SEND_SMS)) {
                disableSmsFeature();
            }
        }
    }

    private void enableSmsFeature() {
        phoneNumberPrompt.setVisibility(View.VISIBLE);
        phoneNumberEntry.setVisibility(View.VISIBLE);
        savePhoneNumberButton.setVisibility(View.VISIBLE);
        requestPermissionButton.setText(getString(R.string.disable_sms_notifications));
    }

    private void disableSmsFeature() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivity(intent);

        // Update UI
        phoneNumberPrompt.setVisibility(View.GONE);
        phoneNumberEntry.setVisibility(View.GONE);
        savePhoneNumberButton.setVisibility(View.GONE);
        requestPermissionButton.setText(getString(R.string.enable_sms_notifications));
    }

    public void sendSmsForLowInventory() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            return; // Permission not granted
        }

        String phoneNumber = phoneNumberEntry.getText().toString();
        if (TextUtils.isEmpty(phoneNumber)) return; // No number provided

        // Get all inventory names with quantity less than 5
        List<String> lowStockItems = getLowStockItems();

        // Construct the message
        String message = "Low stock alert for items: " + TextUtils.join(", ", lowStockItems);

        // Check if there are any low stock items to notify about
        if (!lowStockItems.isEmpty()) {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNumber, null, message, null, null);
        }
    }

    private List<String> getLowStockItems() {
        List<String> lowStockItems = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                InventoryTable.COLUMN_NAME_TITLE // Assuming you have a title column
        };

        // Filter results WHERE "quantity" < 5
        String selection = InventoryTable.COLUMN_NAME_QUANTITY + " < ?";
        String[] selectionArgs = { "5" };

        Cursor cursor = db.query(
                InventoryTable.TABLE_NAME,   // The table to query
                projection,             // The columns to return
                selection,              // The columns for the WHERE clause
                selectionArgs,          // The values for the WHERE clause
                null,                   // Don't group the rows
                null,                   // Don't filter by row groups
                null               // The sort order
        );

        while (cursor.moveToNext()) {
            String itemName = cursor.getString(
                    cursor.getColumnIndexOrThrow(InventoryTable.COLUMN_NAME_TITLE));
            lowStockItems.add(itemName);
        }
        cursor.close();

        return lowStockItems;
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUiBasedOnPermission();
    }
}