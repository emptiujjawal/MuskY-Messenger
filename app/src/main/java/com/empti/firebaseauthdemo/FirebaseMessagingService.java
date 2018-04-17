package com.empti.firebaseauthdemo;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.google.firebase.messaging.RemoteMessage;

/**
 * Created by Emptii on 07-08-2017.
 */

public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        String notificationtitle = remoteMessage.getNotification().getTitle();
        String notificationbody = remoteMessage.getNotification().getBody();
        String clickaction = remoteMessage.getNotification().getClickAction();
        String userid = remoteMessage.getData().get("from_user_id");

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.mipmap.muskyicon)
                        .setContentTitle(notificationtitle)
                        .setContentText(notificationbody);

        Intent resultIntent = new Intent(clickaction);
        resultIntent.putExtra("userid",userid);
        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        this,
                        0,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        mBuilder.setContentIntent(resultPendingIntent);


        int mNotificationId =(int) System.currentTimeMillis();

                              NotificationManager mNotifyMgr =
                                 (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

                                mNotifyMgr.notify(mNotificationId, mBuilder.build());


    }
}
