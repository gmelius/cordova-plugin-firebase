package org.apache.cordova.firebase;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebChromeClient;

import com.gmelius.gmailapp.R;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaArgs;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaPreferences;
import org.apache.cordova.CordovaResourceApi;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.CordovaWebViewEngine;
import org.apache.cordova.ICordovaCookieManager;
import org.apache.cordova.PluginEntry;
import org.apache.cordova.PluginManager;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import nl.xservices.plugins.GooglePlus;

import static android.content.Context.MODE_PRIVATE;
import static android.content.Context.NOTIFICATION_SERVICE;
import static org.apache.cordova.firebase.FirebasePluginMessagingService.SHARED_PREFERENCES_REF;

public class OnNotificationActionArchive extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        Bundle data = intent.getExtras();

        String groupName = data.getString("groupName");
        String threadId = data.getString("threadId");
        String accountName = data.getString("email");
        int id = data.getInt("notificationId", -1);

        // Set notification count at 0
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_REF, MODE_PRIVATE);
        sharedPreferences
                .edit()
                .putInt(groupName, 0)
                .apply();

        // Delete notification
        if (id != -1) {
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
            notificationManager.cancel(id);
        }

        /*
        // Create new event
        ArchiveEmailEvent event = new ArchiveEmailEvent(threadId);
        // Publish event in EventBus
        EventBus.getDefault().post(event);
        */

        JSONArray archiveArgs = new JSONArray();
        JSONObject archiveRequest = new JSONObject();

        JSONArray silentLoginArgs = new JSONArray();
        JSONObject silentLoginRequest = new JSONObject();

        GooglePlus gapi = new GooglePlus();

        String webClientId = context.getString(R.string.webClientId);

        try {

            archiveRequest.put("requestMethod", "POST");
            archiveRequest.put("requestUrl", "{userId}/threads/" + threadId + "/modify");
            archiveRequest.put("urlParams", new JSONObject().put("id", threadId).put("userId", "me"));
            archiveRequest.put("body", new JSONObject().put("removeLabelIds", new JSONArray().put("INBOX")));

            silentLoginRequest.put("url", "");
            silentLoginRequest.put("scopes", "profile https://www.googleapis.com/auth/calendar https://www.googleapis.com/auth/gmail.settings.basic https://mail.google.com/");
            silentLoginRequest.put("webClientId", webClientId);
            silentLoginRequest.put("prompt", true);
            silentLoginRequest.put("accountName", accountName);

            archiveArgs.put(archiveRequest);
            silentLoginArgs.put(silentLoginRequest);

            CordovaArgs cordovaArgsArchive = new CordovaArgs(archiveArgs);
            CordovaArgs cordovaArgsSilentLogin = new CordovaArgs(silentLoginArgs);

            CordovaInterface cordovaInterface = new CordovaInterface() {
                @Override
                public void startActivityForResult(CordovaPlugin command, Intent intent, int requestCode) {
                }

                @Override
                public void setActivityResultCallback(CordovaPlugin plugin) {
                }

                @Override
                public Activity getActivity() {
                    return null;
                }

                @Override
                public Object onMessage(String id, Object data) {
                    return null;
                }

                @Override
                public ExecutorService getThreadPool() {
                    return null;
                }

                @Override
                public void requestPermission(CordovaPlugin plugin, int requestCode, String permission) {
                }

                @Override
                public void requestPermissions(CordovaPlugin plugin, int requestCode, String[] permissions) {
                }

                @Override
                public boolean hasPermission(String permission) {
                    return false;
                }
            };

            CordovaWebView cordovaWebView = new CordovaWebView() {
                @Override
                public void init(CordovaInterface cordova, List<PluginEntry> pluginEntries, CordovaPreferences preferences) {
                }

                @Override
                public boolean isInitialized() {
                    return false;
                }

                @Override
                public View getView() {
                    return null;
                }

                @Override
                public void loadUrlIntoView(String url, boolean recreatePlugins) {

                }

                @Override
                public void stopLoading() {

                }

                @Override
                public boolean canGoBack() {
                    return false;
                }

                @Override
                public void clearCache() {

                }

                @Override
                public void clearCache(boolean b) {

                }

                @Override
                public void clearHistory() {

                }

                @Override
                public boolean backHistory() {
                    return false;
                }

                @Override
                public void handlePause(boolean keepRunning) {

                }

                @Override
                public void onNewIntent(Intent intent) {

                }

                @Override
                public void handleResume(boolean keepRunning) {

                }

                @Override
                public void handleStart() {

                }

                @Override
                public void handleStop() {

                }

                @Override
                public void handleDestroy() {

                }

                @Override
                public void sendJavascript(String statememt) {

                }

                @Override
                public void showWebPage(String url, boolean openExternal, boolean clearHistory, Map<String, Object> params) {

                }

                @Override
                public boolean isCustomViewShowing() {
                    return false;
                }

                @Override
                public void showCustomView(View view, WebChromeClient.CustomViewCallback callback) {

                }

                @Override
                public void hideCustomView() {

                }

                @Override
                public CordovaResourceApi getResourceApi() {
                    return null;
                }

                @Override
                public void setButtonPlumbedToJs(int keyCode, boolean override) {

                }

                @Override
                public boolean isButtonPlumbedToJs(int keyCode) {
                    return false;
                }

                @Override
                public void sendPluginResult(PluginResult cr, String callbackId) {
                    if (callbackId.equals("id-trySilentLogin")) {
                        CallbackContext callbackContext2 = new CallbackContext("id-archive", this);

                        try {
                            gapi.execute("callGoogleApi", cordovaArgsArchive , callbackContext2);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }

                @Override
                public PluginManager getPluginManager() {
                    return null;
                }

                @Override
                public CordovaWebViewEngine getEngine() {
                    return null;
                }

                @Override
                public CordovaPreferences getPreferences() {
                    return null;
                }

                @Override
                public ICordovaCookieManager getCookieManager() {
                    return null;
                }

                @Override
                public String getUrl() {
                    return null;
                }

                @Override
                public Context getContext() {
                    return context;
                }

                @Override
                public void loadUrl(String url) {

                }

                @Override
                public Object postMessage(String id, Object data) {
                    return null;
                }
            };

            CallbackContext callbackContext = new CallbackContext("id-trySilentLogin", cordovaWebView);


            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        gapi.initialize(cordovaInterface, cordovaWebView);

                        gapi.execute("trySilentLogin", cordovaArgsSilentLogin , callbackContext);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            }).start();


        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
}