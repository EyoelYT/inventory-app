package com.zybooks.inventoryapp;

import static androidx.core.content.ContextCompat.getSystemService;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.telephony.SmsManager;
import android.text.TextUtils;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.zybooks.inventoryapp.inventoryschemas.InventoryDbHelper;
import com.zybooks.inventoryapp.inventoryschemas.InventoryTable;

import java.util.ArrayList;
import java.util.List;

public class InventoryUtils {

    public static void sendSmsForLowInventory(Context context) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            return;
        }

        // Fetch the saved phone number from SharedPreferences
        SharedPreferences sharedPreferences = context.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE);
        String phoneNumber = sharedPreferences.getString("savedPhoneNumber", "");
        if (TextUtils.isEmpty(phoneNumber)) {
            // If no phone number saved
            return;
        }

        // Fetch low stock items from the database
        List<String> lowStockItems = getLowStockItems(context);
        if (lowStockItems.isEmpty()) {
            // No low stock items to notify about
            return;
        }

        // Construct and send the SMS message
        String message = "Low stock alert.\n Low stock items include: " + TextUtils.join(", ", lowStockItems);
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(phoneNumber, null, message, null, null);
    }

    private static List<String> getLowStockItems(Context context) {
        List<String> lowStockItems = new ArrayList<>();
        InventoryDbHelper dbHelper = InventoryDbHelper.getInstance(context);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String[] projection = {
                InventoryTable.COLUMN_NAME_TITLE
        };

        // Filter the results WHERE the "quantity" < 5
        String selection = InventoryTable.COLUMN_NAME_QUANTITY + " < ?";
        String[] selectionArgs = { "5" };

        Cursor cursor = db.query(
                InventoryTable.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        while (cursor.moveToNext()) {
            String itemName = cursor.getString(
                    cursor.getColumnIndexOrThrow(InventoryTable.COLUMN_NAME_TITLE));
            lowStockItems.add(itemName);
        }
        cursor.close();

        return lowStockItems;
    }

    public static void showLowStockNotification(Context context) {
        String channelId = "low_stock_alerts";
        String channelName = "Low Stock Alerts";
        List <String> lowStockItems = getLowStockItems(context);
        int notificationId = 1;

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && notificationManager != null) {
            NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Channel for low stock alerts");
            notificationManager.createNotificationChannel(channel);
        }

        // Build the notification and issue it
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Low Stock Alert")
                .setContentText("Low stock for items: " + TextUtils.join(", ", lowStockItems))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(Notification.DEFAULT_ALL)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("Low stock for items: " + TextUtils.join(", ", lowStockItems)));

        if (notificationManager != null) {
            notificationManager.notify(notificationId, builder.build());
        }
    }
}
