package guru.msgs.sdk_lib_w;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.AudioAttributes;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.Settings;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.preference.PreferenceManager;

import com.guru.managed.notifications.BuildConfig;
import com.guru.managed.notifications.R;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import guru.msgs.sdk_lib.utils.communication.PayloadService;
import guru.msgs.sdk_lib.utils.config.Configurations;
import guru.msgs.sdk_lib.utils.config.ServiceSettings;
import guru.msgs.sdk_lib.utils.dnd.SoundAlarmInf;
import guru.msgs.sdk_lib.utils.logic.AutoAnswer;
import guru.msgs.sdk_lib.utils.logic.NotificationClassificator;
import guru.msgs.sdk_lib.utils.logic.NotificationsStore;

public class NotificationManagmentService extends NotificationListenerService implements SharedPreferences.OnSharedPreferenceChangeListener
{
    public static final String TAG = "GURU.NotificationManagmentService";
    private static final String CHANNEL_ID = "restore_notifications_sdk";


    private Context mContext;
    static private AtomicInteger mNotificationId = new AtomicInteger(0);

    private NotificationsStore mNotificationsStore = new NotificationsStore();

    private int mMode = PayloadService.TRANSPARENT;

    private SaveStatistics mSaveStatistics;
    private SaveStatistics.BuildData mStatistics = new SaveStatistics.BuildData();
    private SaveStatistics.CollectData mCollectData = new SaveStatistics.CollectData();

    private ServiceSettings mSettings;
    private NotificationClassificator mNotificationClassificator;
    AutoAnswer mAutoAnswer;
    private NotificationManager mNotificationManager;
    private boolean mExplicitly = false;
    private Integer originalinterruptionFilter = null;

    private PowerManager mPowerManager;
    private PowerManager.WakeLock mWakeLock;

    private CommandBroadcastReceiver mCommandBroadcastReceiver;


    @Override
    public void onCreate() {
        super.onCreate();
        initCommandBroadcastReceiver();

        mContext = getApplicationContext();
        mNotificationManager = mContext.getSystemService(NotificationManager.class);
        createNotificationChannel(mContext);

        sendMode();

        mSaveStatistics = new SaveStatistics(getApplicationContext());

        //Start to Listen to settings
        PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).registerOnSharedPreferenceChangeListener(this);

        //Get settings
        mSettings = ServiceSettings.GetDefault(getApplicationContext());
        mNotificationClassificator = new NotificationClassificator(mSettings, mContext);
        mAutoAnswer = new AutoAnswer(mSettings, mContext);

        //DND observer
        CreateInterruptionFilterBroadcastReceiver();

        //Wake
        mPowerManager = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
        mWakeLock = mPowerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK |
                PowerManager.ACQUIRE_CAUSES_WAKEUP |
                PowerManager.ON_AFTER_RELEASE, "Guru "+ getRandom());

        if (BuildConfig.DEBUG)
            Log.d(TAG, "Service.onCreate()");
    }

    int getRandom() {
        Random rand = new Random(); //instance of random class
        return rand.nextInt(999);
    }

    @Override
    public void onDestroy() {
        if (BuildConfig.DEBUG)
            Log.d(TAG, "SERVICE.onDestroy()");

        //Restore status
        DndUnset();

        //Unregister
        PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).unregisterOnSharedPreferenceChangeListener(this);

        if (BuildConfig.DEBUG) {
            mWakeLock.release(PowerManager.RELEASE_FLAG_WAIT_FOR_NO_PROXIMITY);
        }
        else {
            try {
                mWakeLock.release(PowerManager.RELEASE_FLAG_WAIT_FOR_NO_PROXIMITY);
            } catch (Exception e) {}
        }

        unregisterReceiver(mCommandBroadcastReceiver);
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

    @Override
    public void onNotificationPosted(StatusBarNotification sbn)
    {
        if (mMode== PayloadService.TRANSPARENT) {
            return;
        }


        if (!mNotificationClassificator.isClearable(sbn)) {
            if (BuildConfig.DEBUG)
                Log.d(TAG, "NOTIFICATION PASS BECAUSE ITS UNCLEAREABLE key="+ sbn.getKey() +" extras="+ sbn.getNotification().extras.toString());
            return;
        }

        if (mNotificationClassificator.toShowByPackageName(sbn)) {
            new SoundAlarmInf(mMode, mNotificationManager, mSettings, mNotificationClassificator, mContext, mWakeLock) {
                @Override
                public void RunIt() {
                    if (mNotificationClassificator.ToRaiseOn_non_blocked_calendar()) {
                        if (BuildConfig.DEBUG)
                            Log.d(TAG, "NOTIFICATION PASS BECAUSE IT BE SHOWN BY PACKAGE NAME AND __RAISE__ key="+ sbn.getKey() +" extras="+ sbn.getNotification().extras.toString());
                        raise_quiet_mode(sbn);
                    } else {
                        if (BuildConfig.DEBUG)
                            Log.d(TAG, "NOTIFICATION PASS BECAUSE IT BE SHOWN BY PACKAGE NAME AND NOT REQIED TO RAISE key="+ sbn.getKey() +" extras="+ sbn.getNotification().extras.toString());
                        mNotificationClassificator.SaveShownTimeout(sbn);
                    }
                }
            }.PlayUrgentNotificationIfNeed(sbn);
            return;
        }

        if (mNotificationClassificator.ToShow(sbn)) {
            new SoundAlarmInf(mMode, mNotificationManager, mSettings, mNotificationClassificator, mContext, mWakeLock) {
                @Override
                public void RunIt() {
                    if (mNotificationClassificator.ToRaiseOn_non_blocked_urgent()) {
                        if (BuildConfig.DEBUG)
                            Log.d(TAG, "NOTIFICATION PASS BECAUSE ITS  Urgent AND __RAISE__ key="+ sbn.getKey() +" extras="+ sbn.getNotification().extras.toString());
                        raise_quiet_mode(sbn);
                    }
                    else {
                        if (BuildConfig.DEBUG)
                            Log.d(TAG, "NOTIFICATION PASS BECAUSE ITS  Urgent AND NOT REQIED TO RAISE key="+ sbn.getKey() +" extras="+ sbn.getNotification().extras.toString());
                        mNotificationClassificator.SaveShownTimeout(sbn);
                    }
                }
            }.PlayUrgentNotificationIfNeed(sbn);
            return;
        }

        if (mNotificationClassificator.InShownTimeoutFound(sbn)) {
            if (BuildConfig.DEBUG)
                Log.d(TAG, "NOTIFICATION TO IGNORE key="+ sbn.getKey() +" extras="+ sbn.getNotification().extras.toString());
            return;
        }

        // Save and cancel
        save(sbn);
        // maybe it need response
        mAutoAnswer.RunOnHide(sbn, mSettings, -1);
    }//


    private void raise_quiet_mode(StatusBarNotification sbn) {
        if (BuildConfig.DEBUG)
            Log.d(TAG, "NOTIFICATION ENDS QUIET PERIOD "+ sbn.getNotification().extras.toString() );
        mStatistics.msgsRaised++;
        startTransparent(sbn.getPackageName());
    }
    private void save(StatusBarNotification sbn) {
        if (BuildConfig.DEBUG)
            Log.d(TAG,"NOTIFICATION SAVED key="+ sbn.getKey() +" extras="+ sbn.getNotification().extras.toString());
        mStatistics.msgsPostponed++;
        mNotificationsStore.Save(sbn);
        cancelNotification(sbn.getKey());
    }


    private void ClearNotificationsSave() {
        StatusBarNotification[]	allNotifications = getActiveNotifications();
        if (allNotifications==null)
            return;

        for( int i=0 ; i<allNotifications.length ; i++) {
            if ( !allNotifications[i].isClearable() || mNotificationClassificator.toShowByPackageName(allNotifications[i]) ) {
                //Do nothing - leave it if it's not cleanable or important
            } else if (mNotificationClassificator.toShowByMine(allNotifications[i])) {
                //Own notification - clean all
                cancelNotification(allNotifications[i].getKey());
            } else {
                //Save
                if (Configurations.PRE_SAVE_NOTIFICATIONS)
                    mNotificationsStore.Save(allNotifications[i]);
                cancelNotification(allNotifications[i].getKey());
            }
        }
    }//

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn){
        //nothing
        return;
    }

    @Override
    public void onListenerConnected() {
        super.onListenerConnected();
        if (BuildConfig.DEBUG)
            Log.i(TAG,"onListenerConnected() Listner conneted");
        tryReconnectService();
    }

    @Override
    public void onListenerDisconnected() {
        super.onListenerDisconnected();
        if (BuildConfig.DEBUG)
            Log.i(TAG,"onListenerDisconnected()");
    }

    public void tryReconnectService() {
        toggleNotificationListenerService();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            ComponentName componentName = new ComponentName(getApplicationContext(), NotificationManagmentService.class);

            //It say to Notification Manager RE-BIND your service to listen notifications again inmediatelly!
            requestRebind(componentName);
        }
    }


    private void toggleNotificationListenerService() {
        PackageManager pm = getPackageManager();
        pm.setComponentEnabledSetting(new ComponentName(this, NotificationManagmentService.class),
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
        pm.setComponentEnabledSetting(new ComponentName(this, NotificationManagmentService.class),
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
    }

    private void restoreNotifications(String packageName)
    {
        HashMap<String, StatusBarNotification> allNotif = mNotificationsStore.getData();

        for (Map.Entry<String, StatusBarNotification> me : allNotif.entrySet()) {
            ///restore all notifications except by "packageName" STRING
            if (packageName==null || !packageName.equals(me.getKey())) {
                reSendNotification(me.getValue());
            }
        }

        mNotificationsStore.Empty();
        mNotificationClassificator.Empty();
        mAutoAnswer.Empty();
    }

    private void reSendNotification(StatusBarNotification sbn) {
        Notification savedNotification = sbn.getNotification();
        Context ctx = getApplicationContext();

        NotificationCompat.Builder builder = new NotificationCompat.Builder(ctx, savedNotification);
        builder.setChannelId(CHANNEL_ID);
        String thisGroup = sbn.getPackageName() +"@"+ sbn.getNotification().getGroup();
        builder.setGroup(thisGroup);
        builder.setAutoCancel(true);


        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(ctx);

        String tmp = savedNotification.extras.toString();
        int notificationId = mNotificationId.incrementAndGet();
        if (BuildConfig.DEBUG)
            Log.d(TAG, "RESTORE NOTIFICATION notificationId="+ notificationId +":::"+ tmp );
        notificationManager.notify(notificationId, builder.build());

    }

    static private AtomicInteger mRunNumber = new AtomicInteger(0);


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


    void sendMode(int mode) {
        Intent intent = new  Intent( PayloadService.RESP_INTENT);
        if (mode== PayloadService.TRANSPARENT || mode== PayloadService.QUIET) {
            intent.putExtra(PayloadService.RESP_MODE, mode);
        }
        if (!mExplicitly)
            intent.putExtra(PayloadService.RESP_DND, true);
        sendBroadcast(intent);
    }//

    void sendMode() {
        sendMode(mMode);
    }

    void startQuiet(boolean is_explicit) {
        if (BuildConfig.DEBUG)
            Log.d(TAG, this.getClass().getSimpleName() +" start QUIET. is_explicit="+is_explicit );

        mExplicitly = is_explicit;
        mMode = PayloadService.QUIET;
        mStatistics = new SaveStatistics.CollectData();

        //TRY TO SOLVE "Notification listener service not yet bound"
        tryReconnectService();
        //Clear all
        ClearNotificationsSave();

        //Finish on explicitly
        if (!mExplicitly) {
            sendMode();
            return;
        }

        //Set timer only if it was started explicitly
        if (mSettings.dnd_set) {
            DndSet();
        }

    }//

    void startTransparent(String packageName) {
        if (BuildConfig.DEBUG)
            Log.d(TAG, "Service start TRANSPARENT." );
        onStopQuiet(packageName);
    }

    void restartInCurrentMode() {
        if (mMode== PayloadService.QUIET) {
            if (BuildConfig.DEBUG)
                Log.d(TAG, "Settings changed. Restart QUIET mode" );

            return;
        }
    }//

    void onStopQuiet(String packageName) {
        mCollectData = mStatistics.Export();
        mSaveStatistics.Update(mCollectData);
        mStatistics = new SaveStatistics.CollectData();

        mMode = PayloadService.TRANSPARENT;
        sendMode();
        restoreNotifications(packageName);
        mExplicitly = false;
        DndUnset();
    }//


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (BuildConfig.DEBUG)
            Log.d(TAG, " Shared Preference Changed "+ key );
        mSettings = SettingsActivity.GetSettings(getApplicationContext());
        mNotificationClassificator = new NotificationClassificator(mSettings, mContext);
        mAutoAnswer = new AutoAnswer(mSettings, mContext);

        restartInCurrentMode();
    }//


    public class InterruptionFilterBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!mSettings.dnd_ride_on)
                return;

            if(intent.getAction().equals(NotificationManager.ACTION_INTERRUPTION_FILTER_CHANGED) ) {
                if (mNotificationManager.getCurrentInterruptionFilter() == Configurations.dndInterruptionFilter) {
                    if (mMode== PayloadService.TRANSPARENT) {
                        if (BuildConfig.DEBUG)
                            Log.d(TAG, "Start quiet because DND mode set on");
                        startQuiet(false);
                    }
                } else {
                    if (mMode== PayloadService.QUIET) {
                        if (BuildConfig.DEBUG)
                            Log.d(TAG, "STOP quiet because DND mode set OFF");
                        onStopQuiet(null);
                    }
                }
            }
        }//
    }///InterruptionFilterBroadcastReceiver

    void CreateInterruptionFilterBroadcastReceiver() {
        InterruptionFilterBroadcastReceiver receiver = new InterruptionFilterBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(NotificationManager.ACTION_INTERRUPTION_FILTER_CHANGED);
        registerReceiver(receiver,intentFilter);

    }

    public class CommandBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            int receivedMode = intent.getIntExtra(PayloadService.ASK_MODE, -1);
            switch (receivedMode) {
                case PayloadService.QUESTION:
                    sendMode();
                    break;
                case PayloadService.QUIET:
                    startQuiet(true);
                    break;
                case PayloadService.TRANSPARENT:
                    startTransparent(null);
                    break;
            }

        }//
    }///

    private void initCommandBroadcastReceiver() {
        mCommandBroadcastReceiver = new CommandBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(PayloadService.ASK_INTENT);
        registerReceiver(mCommandBroadcastReceiver,intentFilter);

    }


    void DndSet() {
        if (BuildConfig.DEBUG) {
            originalinterruptionFilter = mNotificationManager.getCurrentInterruptionFilter();
            mNotificationManager.setInterruptionFilter(Configurations.dndInterruptionFilter);
        }
        else {
            try {
                originalinterruptionFilter = mNotificationManager.getCurrentInterruptionFilter();
                mNotificationManager.setInterruptionFilter(Configurations.dndInterruptionFilter);
            } catch (Exception e) {}
        }
    }
    void DndUnset() {
        if (originalinterruptionFilter==null)
            return;

        if (BuildConfig.DEBUG) {
            mNotificationManager.setInterruptionFilter(originalinterruptionFilter);
        }
        else {
            try {
                mNotificationManager.setInterruptionFilter(originalinterruptionFilter);
            } catch (Exception e) {}
        }

        originalinterruptionFilter = null;
    }
    void DndPause() {
        if (mNotificationManager.getCurrentInterruptionFilter()==Configurations.dndInterruptionFilter)
            mNotificationManager.setInterruptionFilter(Configurations.ringInterruptionFilter);
    }
    void DndRestore() {
        mNotificationManager.setInterruptionFilter(Configurations.dndInterruptionFilter);
    }

}///
