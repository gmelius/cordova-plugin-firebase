package org.apache.cordova.firebase;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;

import static android.content.Context.MODE_PRIVATE;
import static android.content.Context.NOTIFICATION_SERVICE;
import static org.apache.cordova.firebase.FirebasePluginMessagingService.SHARED_PREFERENCES_REF;

public class OnNotificationOpenReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        PackageManager pm = context.getPackageManager();

        Intent launchIntent = pm.getLaunchIntentForPackage(context.getPackageName());
        launchIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

        Bundle data = intent.getExtras();
        data.putBoolean("tap", true);

        String groupName = data.getString("groupName");
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_REF, MODE_PRIVATE);
        sharedPreferences
                .edit()
                .putInt(groupName, 0)
                .apply();

        FirebasePlugin.sendNotification(data, context);

        launchIntent.putExtras(data);
        context.startActivity(launchIntent);
    }
}
