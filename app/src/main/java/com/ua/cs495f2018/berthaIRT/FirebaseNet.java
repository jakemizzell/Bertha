package com.ua.cs495f2018.berthaIRT;


import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static android.support.v4.app.NotificationCompat.DEFAULT_SOUND;
import static android.support.v4.app.NotificationCompat.DEFAULT_VIBRATE;
import static android.support.v4.app.NotificationCompat.GROUP_ALERT_ALL;


public class FirebaseNet extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";
    private static Interface.WithStringListener onRefreshHandler;

    public static void setOnRefreshHandler(Interface.WithStringListener i) {
        onRefreshHandler = i;
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        System.out.println("Message Recieved");
        Map<String, String> data = remoteMessage.getData();
        System.out.println(data);
        String title = data.get("title");

        if(title == null)
            return;
        String reportID = data.get("reportID");
        String extras = data.get("extras");
        String body = data.get("body");
        String clickAction = data.get("clickAction");
        if (title.equals("REFRESH")) {
            if (Client.cogNet.getSession() != null && Client.cogNet.getSession().isValid()) {
                Client.net.lookupGroup(getApplication(), Client.userAttributes.get("custom:groupID"), null);
                Client.net.pullReport(getApplication(), reportID, () ->
                        Client.net.pullAlerts(getApplication(), () -> onRefreshHandler.onEvent(reportID)));
            }
            return;
        }

        Intent intent = new Intent(getApplicationContext(), NewUserActivity.class);
        if (Client.cogNet.getSession().isValid() && clickAction != null) {
            intent = new Intent(clickAction);
            intent.putExtra("id", reportID);
            if(extras != null)
                intent.putExtra("frag", extras);
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_ONE_SHOT);


        String channelId = "fcm_default_channel";
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, channelId)
                .setDefaults(DEFAULT_SOUND | DEFAULT_VIBRATE)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setSmallIcon(R.drawable.bertha_appicon)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setGroupSummary(false)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId,
                    "BerthaIRT Notification Service",
                    NotificationManager.IMPORTANCE_DEFAULT);
            Objects.requireNonNull(notificationManager).createNotificationChannel(channel);
        }

        Objects.requireNonNull(notificationManager).notify(0, notificationBuilder.build());
    }

    @Override
    public void onNewToken(String token) {
        System.out.println("FCMTOKEN" + token);
        //sendRegistrationToServer(token);
    }
}