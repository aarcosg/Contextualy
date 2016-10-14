package es.us.contextualy.service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import es.us.contextualy.R;
import es.us.contextualy.ui.MainActivity;

public class FCMListenerService extends FirebaseMessagingService {

    private static final String TAG = FCMListenerService.class.getCanonicalName();
    private static final String EXTRA_NOTIFICATION_ID = "extra_notification_id";
    private static final int NOTIFICATION_ID = 1;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d(TAG, "onMessageReceived");
        super.onMessageReceived(remoteMessage);
        if(remoteMessage.getNotification() != null){
            //Message from Firebase dashboard
            showNotification(remoteMessage.getNotification().getBody());
        }
    }

    // Put the message into a notification and post it.
    // This is just one simple example of what you might choose to do with
    // a GCM message.
    private void showNotification(String msg) {
        Intent tasksIntent = new Intent(this, MainActivity.class);
        tasksIntent.putExtra(EXTRA_NOTIFICATION_ID, NOTIFICATION_ID);

        PendingIntent resultPendingIntent = PendingIntent.getActivity(this, 0, tasksIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.cast_ic_notification_small_icon)
                        .setLargeIcon(BitmapFactory.decodeResource(this.getResources(), R.mipmap.ic_launcher))
                        .setContentTitle(getResources().getString(R.string.app_name))
                        .setAutoCancel(true)
                        .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                        .setLights(0x009900, 300, 1000)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(msg))
                        .setContentIntent(resultPendingIntent);

        NotificationManager notificationManager = (NotificationManager)
                this.getSystemService(android.content.Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }
}
