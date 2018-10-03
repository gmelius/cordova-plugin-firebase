package org.apache.cordova.firebase;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.text.TextUtils;
import android.content.ContentResolver;
import android.graphics.Color;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class FirebasePluginMessagingService extends FirebaseMessagingService {

    public static final String SHARED_PREFERENCES_REF = "notificationSharedPreferences";

    private static final String TAG = "FirebasePlugin";
    private static HashMap<Integer, NotificationCompat.InboxStyle> inboxStyles = null;
    SharedPreferences sharedPreferences = null;

    /**
     * Get a string from resources without importing the .R package
     *
     * @param name Resource Name
     * @return Resource
     */
    private String getStringResource(String name) {
        return this.getString(
                this.getResources().getIdentifier(
                        name, "string", this.getPackageName()
                )
        );
    }

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // [START_EXCLUDE]
        // There are two types of messages data messages and notification messages. Data messages are handled
        // here in onMessageReceived whether the app is in the foreground or background. Data messages are the type
        // traditionally used with GCM. Notification messages are only received here in onMessageReceived when the app
        // is in the foreground. When the app is in the background an automatically generated notification is displayed.
        // When the user taps on the notification they are returned to the app. Messages containing both notification
        // and data payloads are treated as notification messages. The Firebase console always sends notification
        // messages. For more see: https://firebase.google.com/docs/cloud-messaging/concept-options
        // [END_EXCLUDE]

        // Pass the message to the receiver manager so any registered receivers can decide to handle it
        boolean wasHandled = FirebasePluginMessageReceiverManager.onMessageReceived(remoteMessage);
        if (wasHandled) {
            Log.d(TAG, "Message was handled by a registered receiver");

            // Don't process the message in this method.
            return;
        }


        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        String title;
        String text;
        String id;
        String sound = null;
        String lights = null;
        String icon = null;
        String tag;
        String typeNotif;
        Map<String, String> data = remoteMessage.getData();

        if (remoteMessage.getNotification() != null) {
            title = remoteMessage.getNotification().getTitle();
            text = remoteMessage.getNotification().getBody();
            id = remoteMessage.getMessageId();
            icon = remoteMessage.getNotification().getIcon();
            tag = remoteMessage.getNotification().getTag();
            typeNotif = data.get("typeNotif");
        } else {
            title = data.get("title");
            text = data.get("text");
            id = data.get("id");
            sound = data.get("sound");
            lights = data.get("lights"); //String containing hex ARGB color, miliseconds on, miliseconds off, example: '#FFFF00FF,1000,3000'
            icon = data.get("icon");
            tag = data.get("tag");
            typeNotif = data.get("typeNotif");

            if (TextUtils.isEmpty(text)) {
                text = data.get("body");
            }
        }

        if (TextUtils.isEmpty(id)) {
            Random rand = new Random();
            int n = rand.nextInt(50) + 1;
            id = Integer.toString(n);
        }

        Log.d(TAG, "From: " + remoteMessage.getFrom());
        Log.d(TAG, "Notification Message id: " + id);
        Log.d(TAG, "Notification Message Title: " + title);
        Log.d(TAG, "Notification Message Body/Text: " + text);
        Log.d(TAG, "Notification Message Sound: " + sound);
        Log.d(TAG, "Notification Message Lights: " + lights);
        Log.d(TAG, "Notification Message Icon: " + icon);

        // TODO: Add option to developer to configure if show notification when app on foreground
        if (!TextUtils.isEmpty(text) || !TextUtils.isEmpty(title) || (!data.isEmpty())) {
            boolean showNotification = (FirebasePlugin.inBackground() || !FirebasePlugin.hasNotificationsCallback()) && (!TextUtils.isEmpty(text) || !TextUtils.isEmpty(title));
            sendNotification(id, title, text, data, showNotification, sound, lights, icon, tag, typeNotif);
        }

    }

    private void sendNotification(String id, String title, String messageBody, Map<String, String> data, boolean showNotification, String sound, String lights, String icon, String tag, String typeNotif) {
        Bundle bundle = new Bundle();
        for (String key : data.keySet()) {
            bundle.putString(key, data.get(key));
        }

        if (showNotification) {
            int summaryId = tag.hashCode() + typeNotif.hashCode();
            String groupName = tag + typeNotif;

            sharedPreferences = this.getSharedPreferences(SHARED_PREFERENCES_REF, MODE_PRIVATE);

            messageBody = Html.fromHtml(messageBody).toString();

            // Intent for notification open
            Intent intent = new Intent(this, OnNotificationOpenReceiver.class);
            intent.putExtras(bundle);
            intent.putExtra("groupName", groupName);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(this, id.hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT);

            // Intent for notification deleted
            Intent intent2 = new Intent(this, OnNotificationDeletedReceiver.class);
            intent2.putExtra("groupName", groupName);
            PendingIntent deletePendingIntent = PendingIntent.getBroadcast(this, id.hashCode(), intent2, 0);

            // Intent for action reply
            Intent intent3 = new Intent(this, OnNotificationActionReply.class);
            intent3.putExtras(bundle);
            intent3.putExtra("groupName", groupName);
            intent3.putExtra("notificationId", id.hashCode());
            PendingIntent actionReplyPendingIntent = PendingIntent.getBroadcast(this, id.hashCode(), intent3, 0);

            // Intent for action archive
            Intent intent4 = new Intent(this, OnNotificationActionArchive.class);
            intent4.putExtras(bundle);
            intent4.putExtra("groupName", groupName);
            intent4.putExtra("notificationId", id.hashCode());
            PendingIntent actionArchivePendingIntent = PendingIntent.getBroadcast(this, id.hashCode(), intent4, 0);

            String channelId = this.getStringResource("default_notification_channel_id");
            String channelName = this.getStringResource("default_notification_channel_name");
            Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);


            int count = sharedPreferences.getInt(groupName, 0) + 1;

            if (inboxStyles == null) {
                inboxStyles = new HashMap<>();
                count = 1;
            }

            sharedPreferences
                    .edit()
                    .putInt(groupName, count)
                    .apply();

            NotificationCompat.InboxStyle inboxStyle = inboxStyles.get(summaryId);

            // If inboxStyle don't exist, create it and add it to the HashMap<>
            // Or if notification have been deleted, recreate inboxStyle
            if (inboxStyle == null || count == 1) {
                inboxStyle = new NotificationCompat.InboxStyle()
                        .setSummaryText(tag);
                inboxStyles.put(summaryId, inboxStyle);
            }

            Spannable sb = new SpannableString(title + " " + messageBody);
            sb.setSpan(new ForegroundColorSpan(Color.WHITE), 0, title.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            inboxStyle.addLine(sb);


            // Create BitTextStyle for notification
            NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle()
                    .bigText(messageBody)
                    .setSummaryText(tag);

            int resID = getResources().getIdentifier("notification_icon", "drawable", getPackageName());
            // 'Basic' notification
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, channelId);
            notificationBuilder
                    .setContentTitle(title)
                    .setContentText(messageBody)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setStyle(bigTextStyle)
                    .setAutoCancel(true)
                    .setSound(defaultSoundUri)
                    .setContentIntent(pendingIntent)
                    .setDeleteIntent(deletePendingIntent)
                    .setPriority(NotificationCompat.PRIORITY_MAX)
                    .setGroup(groupName);
            if (resID != 0) {
                notificationBuilder.setSmallIcon(resID);
            } else {
                notificationBuilder.setSmallIcon(getApplicationInfo().icon);
            }

            if (typeNotif.equals("newEmail")) {
                notificationBuilder.addAction(-1, "Archive", actionArchivePendingIntent);
                notificationBuilder.addAction(-1, "Reply", actionReplyPendingIntent);
            }

            // Sumamry notification
            NotificationCompat.Builder summaryNotificationBuilder = new NotificationCompat.Builder(this, channelId)
                    .setContentTitle(count + " " + getSummaryTitle(typeNotif))
                    .setContentText(tag)
                    .setStyle(inboxStyle)
                    .setContentIntent(pendingIntent)
                    .setDeleteIntent(deletePendingIntent)
                    .setGroup(groupName)
                    .setAutoCancel(true)
                    .setGroupSummary(true);
            if (resID != 0) {
                summaryNotificationBuilder.setSmallIcon(resID);
            } else {
                summaryNotificationBuilder.setSmallIcon(getApplicationInfo().icon);
            }


            if (icon != null) {
                try {
                    URL url = new URL(icon);
                    Bitmap bitmapIcon = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                    // Make icon rounded
                    bitmapIcon = getRoundedCornerBitmap(bitmapIcon);

                    notificationBuilder.setLargeIcon(bitmapIcon);
                    summaryNotificationBuilder.setLargeIcon(bitmapIcon);
                } catch (IOException e) {

                }
            }

            if (sound != null) {
                Log.d(TAG, "sound before path is: " + sound);
                Uri soundPath = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + getPackageName() + "/raw/" + sound);
                Log.d(TAG, "Parsed sound is: " + soundPath.toString());
                notificationBuilder.setSound(soundPath);
            } else {
                Log.d(TAG, "Sound was null ");
            }

            if (lights != null) {
                try {
                    String[] lightsComponents = lights.replaceAll("\\s", "").split(",");
                    if (lightsComponents.length == 3) {
                        int lightArgb = Color.parseColor(lightsComponents[0]);
                        int lightOnMs = Integer.parseInt(lightsComponents[1]);
                        int lightOffMs = Integer.parseInt(lightsComponents[2]);

                        notificationBuilder.setLights(lightArgb, lightOnMs, lightOffMs);
                    }
                } catch (Exception e) {
                }
            }

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                int accentID = getResources().getIdentifier("accent", "color", getPackageName());
                notificationBuilder.setColor(getResources().getColor(accentID, null));
                
            }

            /* Notification notification = notificationBuilder.build();
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                int iconID = android.R.id.icon;
                int notiID = getResources().getIdentifier("notification_big", "drawable", getPackageName());
                if (notification.contentView != null) {
                    notification.contentView.setImageViewResource(iconID, notiID);
                }
            }*/

            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            // Since android Oreo notification channel is needed.
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH);
                notificationManager.createNotificationChannel(channel);
            }

            // Build and display notifiaction
            notificationManager.notify(id.hashCode(), notificationBuilder.build());
            if (count > 1) {
                notificationManager.notify(summaryId, summaryNotificationBuilder.build());
            }
        } else {
            bundle.putBoolean("tap", false);
            bundle.putString("title", title);
            bundle.putString("body", messageBody);
            FirebasePlugin.sendNotification(bundle, this.getApplicationContext());
        }
    }

    private static String getSummaryTitle (String typeNotif) {
        String text = "news";

        switch (typeNotif) {
            case "newEmail":
                text = "new mails";
                break;
            case "tracking":
                text = "new opens";
                break;
            case "notes":
                text = "new notes";
                break;
            case "campaigns":
                text = "new campaigns";
                break;
            case "assign":
                text = "new assigns";
                break;
            case "kanban":
                text = "new kanban";
                break;
        }

        return text;
    }

    /**** https://ruibm.com/2009/06/16/rounded-corner-bitmaps-on-android/ ****/
    private static Bitmap getRoundedCornerBitmap(Bitmap bitmap) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);
        final float roundPx = 10000;

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;
    }

}
