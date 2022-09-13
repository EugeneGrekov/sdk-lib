package guru.msgs.sdk_lib_w;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import guru.msgs.sdk_lib.BuildConfig;
import guru.msgs.sdk_lib.utils.communication.PayloadService;

public class ToExport {

    static private boolean is_initiated = false;
    public static final String TAG = "GURU.ToExport";

    static public String GetServiceName() {
        return NotificationManagmentService.class.getSimpleName();
    }

    static public void Init(Context ctx, byte[] certificate) throws Exception {
         is_initiated = true;
    }//


    static boolean mBound = false;
    static private ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            mBound = true;
            if (BuildConfig.DEBUG)
                Log.d(TAG, "onServiceConnected()");
        }
        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
            if (BuildConfig.DEBUG)
                Log.d(TAG, "onServiceDisconnected()");
        }
    };

    public static void Start(Context ctx, boolean is_quiet) throws Exception {
        if (!is_initiated) {
            throw new Exception("Not initiated!");
        }
        int mode = is_quiet ? PayloadService.QUIET : PayloadService.TRANSPARENT;
        Intent intent = new Intent(ctx, NotificationManagmentService.class);
        intent.putExtra(PayloadService.ASK_MODE, mode);
//        ctx.bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
        ctx.startService(intent);
        if (BuildConfig.DEBUG)
            Log.d(TAG, "startService()");
    }

    public static void Stop(Context ctx) throws Exception {
        if (!is_initiated) {
            throw new Exception("Not initiated!");
        }
        Intent intent = new Intent(ctx, NotificationManagmentService.class);
        ctx.stopService(intent);
//        ctx.unbindService(mServiceConnection);
        if (BuildConfig.DEBUG)
            Log.d(TAG, "stopService()");
    }

    public static void sendCommand(Context ctx, boolean is_quiet ) throws Exception {
        if (!is_initiated) {
            throw new Exception("Not initiated!");
        }
        int mode = is_quiet ? PayloadService.QUIET : PayloadService.TRANSPARENT;
        Intent intent = new  Intent( PayloadService.ASK_INTENT);
        intent.putExtra(PayloadService.ASK_MODE, mode);
        ctx.sendBroadcast(intent);
        if (BuildConfig.DEBUG)
            Log.d(TAG, "sendCommand() is_quiet="+ is_quiet);
    }//


    static public class AskNotification {

        private static final String ENABLED_NOTIFICATION_LISTENERS = "enabled_notification_listeners";
        private static final String ACTION_NOTIFICATION_LISTENER_SETTINGS = "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS";


        static public boolean isEnabled(Context ctx){
            String pkgName = ctx.getPackageName();
            final String flat = Settings.Secure.getString(ctx.getContentResolver(), ENABLED_NOTIFICATION_LISTENERS);
            if (!TextUtils.isEmpty(flat)) {
                final String[] names = flat.split(":");
                for (String name : names) {
                    final ComponentName cn = ComponentName.unflattenFromString(name);
                    if (cn != null) {
                        if (TextUtils.equals(pkgName, cn.getPackageName())) {
                            return true;
                        }
                    }
                }
            }
            return false;
        }

        public void Run(Context ctx) {
            if(!isEnabled(ctx)){
                ctx.startActivity(new Intent(ACTION_NOTIFICATION_LISTENER_SETTINGS));
            }
        }
    }

}///
