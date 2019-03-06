package com.sonymobile.ibeaconservice.Service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

import com.sonymobile.ibeaconservice.IBeaconApplication;
import com.sonymobile.ibeaconservice.MainActivity;
import com.sonymobile.ibeaconservice.R;
import com.sonymobile.ibeaconservice.Utils.Constants;
import com.sonymobile.ibeaconservice.Utils.IBeaconUtils;

import java.nio.ByteBuffer;

import static com.sonymobile.ibeaconservice.Utils.Constants.Intents.SERVICE_OPEN_APP_INTENT;

public class IBeaconService extends Service {
    private static final String NOTIFICATION_CHANNEL_ID = "com.sonymobile.ibeacon.notification_id";
    private static final String CHANNEL_NAME = "iBeacon Service";
    private boolean mIsAdvertising;
    private ByteBuffer mAdvertisingBytes;
    private IBeaconUtils mBeaconUtils;

    @Override
    public void onCreate() {
        super.onCreate();
        mBeaconUtils = new IBeaconUtils();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent.getAction() != null) {
            if (intent.getAction().equals(Constants.Intents.SERVICE_INTENT_FOREGROUND)) {
                byte[] adBytes = intent.getByteArrayExtra(Constants.Extras.ADVERTISING_BYTES);

                mAdvertisingBytes = ByteBuffer.wrap(adBytes);

                //23 as a random id number. Who cares?
                startForeground(23, createNotification());
                startBroadcasting();
                ((IBeaconApplication)getApplication()).mRunning = true;
            }
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        stopBroadcasting();
        ((IBeaconApplication)getApplication()).mRunning = false;
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     *Create a notification showing that the service is running, with a button that
     * starts our main activity again.
     *
     * @return the notification
     */
    private Notification createNotification() {
        Intent openAppIntent = new Intent(this,MainActivity.class);

        //need to create a notification channel in Android 8.1 and beyond.
        //TODO test this on earlier versions may need modification
        NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_NONE);
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        assert manager != null;
        manager.createNotificationChannel(chan);
        //end 8.1 specific code

        openAppIntent.setAction(SERVICE_OPEN_APP_INTENT);
        openAppIntent.putExtra(Constants.Extras.ADVERTISING_BYTES, mAdvertisingBytes.array());

        PendingIntent pOpenAppIntent = PendingIntent.getActivity(this, 0,openAppIntent,PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentTitle("iBeacon background service")
                .setContentText("message")
                .addAction(new NotificationCompat.Action(android.R.drawable.btn_star, "Settings", pOpenAppIntent));

        return builder.build();
    }

    private void startBroadcasting() {
        if (!mIsAdvertising) {
            mIsAdvertising = true;
            mBeaconUtils.startAdvertising(mAdvertisingBytes);
        } else {
            System.out.println("towa already advertising!");
        }
    }

    private void stopBroadcasting() {
        if (mIsAdvertising) {
            mBeaconUtils.stopAdvertising();
        } else {
            System.out.println("towa trying to stop, no broadcast!");
        }
    }
}