package com.example.locationmonitoring.util;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.telephony.SmsManager;
import android.widget.Toast;

public class SmsUtils {

    public static void sendSms(Context context, String phoneNumber, String message) {
        SmsManager smsManager = SmsManager.getDefault();

        // Create a PendingIntent for the sent status
        PendingIntent sentIntent = PendingIntent.getBroadcast(context, 0,
                new Intent("SMS_SENT"), PendingIntent.FLAG_UPDATE_CURRENT);

        // Create a PendingIntent for the delivered status
        PendingIntent deliveredIntent = PendingIntent.getBroadcast(context, 0,
                new Intent("SMS_DELIVERED"), PendingIntent.FLAG_UPDATE_CURRENT);

        // Register a BroadcastReceiver for the sent status
        context.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if ("SMS_SENT".equals(action)) {
                    switch (getResultCode()) {
                        case Activity.RESULT_OK:
                            // SMS sent successfully
                            Toast.makeText(context, "SMS sent", Toast.LENGTH_SHORT).show();
                            break;
                        default:
                            // Handle other result codes as needed
                            Toast.makeText(context, "SMS sending failed", Toast.LENGTH_SHORT).show();
                            break;
                    }
                }
            }
        }, new IntentFilter("SMS_SENT"));

        // Register a BroadcastReceiver for the delivered status
        context.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if ("SMS_DELIVERED".equals(action)) {
                    switch (getResultCode()) {
                        case Activity.RESULT_OK:
                            // SMS delivered successfully
                            Toast.makeText(context, "SMS delivered", Toast.LENGTH_SHORT).show();
                            break;
                        default:
                            // Handle other result codes as needed
                            Toast.makeText(context, "SMS delivery failed", Toast.LENGTH_SHORT).show();
                            break;
                    }
                }
            }
        }, new IntentFilter("SMS_DELIVERED"), Manifest.permission.SEND_SMS, null);

        // Send the SMS
        smsManager.sendTextMessage(phoneNumber, null, message, sentIntent, deliveredIntent);
    }
}
