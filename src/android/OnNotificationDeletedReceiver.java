package org.apache.cordova.firebase;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import static android.content.Context.MODE_PRIVATE;
import static org.apache.cordova.firebase.FirebasePluginMessagingService.SHARED_PREFERENCES_REF;

public class OnNotificationDeletedReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle data = intent.getExtras();

        String groupName = data.getString("groupName");

        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_REF, MODE_PRIVATE);

        sharedPreferences
                .edit()
                .putInt(groupName, 0)
                .apply();
    }
}