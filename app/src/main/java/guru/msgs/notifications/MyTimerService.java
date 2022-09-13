package guru.msgs.notifications;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.provider.Settings;
import android.service.notification.NotificationListenerService;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import java.util.concurrent.atomic.AtomicInteger;

import guru.msgs.sdk_lib_w.ToExport;


public class MyTimerService extends NotificationListenerService
{
    public static final String TAG = "GURU.MyTimerService";
    private static final String CHANNEL_ID = "guru_application";


    private Context mContext;
    static private AtomicInteger mNotificationId = new AtomicInteger(0);

    static private CountDownTimer mCountDownTimer;
    private int mMode = ActivityServicePayload.TRANSPARENT;
    private int mSecondsRemain = -1;
    private int mSecondsWillRun;

    private NotificationManager mNotificationManager;
    private boolean mExplicitly = false;

    private ActivityAskBroadcastReceiver mActivityAskBroadcastReceiver;


    @Override
    public void onCreate() {
        super.onCreate();
        initActivityAskBroadcastReceiver();

        mContext = getApplicationContext();
        mNotificationManager = mContext.getSystemService(NotificationManager.class);
        createNotificationChannel(mContext);

        //
        try {
            Log.d(TAG, "Start "+ ToExport.GetServiceName() );
            ToExport.Init(mContext, "My certificate".getBytes());
            ToExport.Start(mContext,false);
        } catch (Exception e) {
            e.printStackTrace();
            this.stopSelf();
        }

        sendMode();
        //Show Green on begin
        setActionNotification();


        if (BuildConfig.DEBUG)
            Log.d(TAG, "MyTimerService,onCreate()");
    }

    @Override
    public void onDestroy() {
        if (BuildConfig.DEBUG)
            Log.d(TAG, "MyTimerService,onDestroy()");

        //Stop service
        try {
            ToExport.Stop(mContext);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //Unregister
        unregisterReceiver(mActivityAskBroadcastReceiver);

        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
//        return super.onStartCommand(intent, flags, startId);
        return Service.START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return super.onBind(intent);
    }

    private void setActionNotification() {
        int mode = this.mMode;
        Context ctx = getApplicationContext();

        Intent intent = new Intent( ActivityServicePayload.ASK_INTENT);


        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID);

        if (mode == ActivityServicePayload.QUIET) {
            intent.putExtra(ActivityServicePayload.ASK_MODE, ActivityServicePayload.TRANSPARENT);
            builder.setColor(Color.RED);
            builder.setContentTitle(getString(R.string.mode_quiet));
            builder.setContentText(getString(R.string.switch_transparent));
            builder.setSmallIcon(R.drawable.ic_24r);
            builder.setSilent(true);

        } else {
            intent.putExtra(ActivityServicePayload.ASK_MODE, ActivityServicePayload.QUIET);
            builder.setColor(Color.GREEN);
            String tmpTitle = getString(R.string.mode_transparent);
/*
            if (mCollectData.runMinutes>0)
                tmpTitle += getString(R.string.add_saved_minutes, (int)mCollectData.runMinutes);
*/
            builder.setContentTitle(tmpTitle);

            builder.setContentText(getString(R.string.switch_quiet));
            builder.setSmallIcon(R.drawable.ic_24g);

/*
            //check is need restore sound
            builder.setSilent( !Configurations.PLAY_RESTORE_SOUND || mNotificationsStore.isEmpty() );
*/

        }
        PendingIntent snoozePendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setTicker("GURU")
                .setContentIntent(snoozePendingIntent)
                .addAction(R.drawable.ic_lnch_background, "Change Mode", snoozePendingIntent);




        int notificationId = mNotificationId.incrementAndGet();
        startForeground(notificationId, builder.build());

    }//


    public void createNotificationChannel(Context ctx) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = ctx.getString(R.string.channel_name);
            String description = ctx.getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            AudioAttributes att = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build();
            channel.setSound(Settings.System.DEFAULT_NOTIFICATION_URI, att);

            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            mNotificationManager.createNotificationChannel(channel);
            if (BuildConfig.DEBUG)
                Log.d(TAG, "CREATE NOTIFICATIONS CHANNEL "+ name +" created!");
        }
    }//


    void sendMode(int mode, int seconds) {
        Intent intent = new  Intent( ActivityServicePayload.RESP_INTENT);
        if (mode== ActivityServicePayload.TRANSPARENT || mode== ActivityServicePayload.QUIET) {
            intent.putExtra(ActivityServicePayload.RESP_MODE, mode);
        }
        if (mExplicitly && seconds>=0)
            intent.putExtra(ActivityServicePayload.RESP_SECONDS, seconds);
        if (!mExplicitly)
            intent.putExtra(ActivityServicePayload.RESP_DND, true);
        sendBroadcast(intent);
    }//

    void sendMode() {
        sendMode(mMode, mSecondsRemain);
    }

    void startQuiet(boolean is_explicit) {
        if (BuildConfig.DEBUG)
            Log.d(TAG, this.getClass().getSimpleName() +" start QUIET. is_explicit="+is_explicit );

        try {
            ToExport.sendCommand(mContext, true);
        } catch (Exception e) {
            e.printStackTrace();
        }

        mExplicitly = is_explicit;
        mMode = ActivityServicePayload.QUIET;
        //Show Red
        setActionNotification();

        //Finish on explicitly
        if (!mExplicitly) {
            sendMode();
            return;
        }

        mSecondsWillRun = Configurations.DEFAULT_MINUTES_AMMOUNT *60;
        mSecondsRemain = mSecondsWillRun;
        final long startTime = System.currentTimeMillis();

        if (mCountDownTimer!=null) {
            mCountDownTimer.cancel();
            mCountDownTimer = null;
        }

        mCountDownTimer = new CountDownTimer(mSecondsWillRun *1000*12, 1000) {

            public void onTick(long millisUntilFinished) {
                final long now = System.currentTimeMillis();
                mSecondsRemain = mSecondsWillRun - (int)((now-startTime) / 1000);
                sendMode();
                if (mSecondsRemain <=0 && mCountDownTimer!=null) {
                    if (BuildConfig.DEBUG)
                        Log.d(TAG,"Timer finish QUIET mode");
                    mCountDownTimer.cancel();
                    onStopQuiet(null);

                }
            }

            public void onFinish() {
                if (BuildConfig.DEBUG)
                    Log.d(TAG,"SYSTEM Timer finish QUIET mode");
                onStopQuiet(null);
            }
        }.start();

    }//

    void startTransparent(String packageName) {
        if (BuildConfig.DEBUG)
            Log.d(TAG, "Service start TRANSPARENT." );


        if (mCountDownTimer!=null)
            mCountDownTimer.cancel();
        onStopQuiet(packageName);
    }

    void restartInCurrentMode() {
        if (mMode== ActivityServicePayload.QUIET) {
            if (BuildConfig.DEBUG)
                Log.d(TAG, "Settings changed. Restart QUIET mode" );

            if (mExplicitly)
                mSecondsWillRun = Configurations.DEFAULT_MINUTES_AMMOUNT *60;

            return;
        }
    }//

    void onStopQuiet(String packageName) {
        try {
            ToExport.sendCommand(mContext, false);
        } catch (Exception e) {
            e.printStackTrace();
        }

        mMode = ActivityServicePayload.TRANSPARENT;
        mSecondsRemain = -1;
        sendMode();
        //Switch to GREEN
        setActionNotification();
        //
        mExplicitly = false;
    }//


    public class ActivityAskBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            int receivedMode = intent.getIntExtra(ActivityServicePayload.ASK_MODE, -1);
            switch (receivedMode) {
                case ActivityServicePayload.QUESTION:
                    sendMode();
                    break;
                case ActivityServicePayload.QUIET:
                    startQuiet(true);
                    break;
                case ActivityServicePayload.TRANSPARENT:
                    startTransparent(null);
                    break;
            }

        }//
    }///

    private void initActivityAskBroadcastReceiver() {
        mActivityAskBroadcastReceiver = new ActivityAskBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ActivityServicePayload.ASK_INTENT);
        registerReceiver(mActivityAskBroadcastReceiver,intentFilter);

    }


}///
