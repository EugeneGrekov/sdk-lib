package guru.msgs.notifications;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import guru.msgs.sdk_lib.ToExport;

public class MainActivity extends AppCompatActivity {


    private TextView m_textViewTransparent;
    private TextView m_textViewQuiet;
    private TextView m_textViewCounter;
    private Button m_buttonQuiet;
    private Button m_buttonTransparent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle(R.string.act_main);

        m_textViewTransparent = findViewById(R.id.textViewTransparent);
        m_textViewQuiet = findViewById(R.id.textViewQuiet);
        m_textViewCounter = findViewById(R.id.textViewCounter);
        m_buttonQuiet = findViewById(R.id.buttonQuiet);
        m_buttonTransparent = findViewById(R.id.buttonTranspanent);

        m_buttonQuiet.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                sendAsk(ActivityServicePayload.QUIET);
            }
        });

        m_buttonTransparent.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                sendAsk(ActivityServicePayload.TRANSPARENT);
            }
        });

        hideAll();
        preGetNotifications();
        preGetCalls();

        ResponseBroadcastReceiver responseBroadcastReceiver = new ResponseBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ActivityServicePayload.RESP_INTENT);
        registerReceiver(responseBroadcastReceiver,intentFilter);

        runService();
    }//onCreate

    void preGetNotifications() {
        if(!ToExport.AskNotification.isEnabled(this)) {
            mStartForResultShowNotifications.launch(new Intent(this, ShowActivity.class));
        } else {
            return;
        }
    }
    ActivityResultLauncher<Intent> mStartForResultShowNotifications = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    new ToExport.AskNotification().Run(MainActivity.this);
                }
            });



    void preGetCalls() {
        final int code = 999;
        checkPermission(this, "android.permission.READ_PHONE_STATE", code);
        checkPermission(this, "android.permission.READ_CALL_LOG", code);
        checkPermission(this, "android.permission.WAKE_LOCK", code);
    }

    static public boolean checkPermission(Activity activity, String permission, int requestCode)
    {
        // Checking if permission is not granted
        if (ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(activity, new String[] { permission }, requestCode);
            return false;
        }
        else {
            return true;
        }
    }//


    @Override
    protected void onResume() {
        super.onResume();
        sendAsk(ActivityServicePayload.QUESTION);
    }


    ///////////////////////////////////////

    private void hideAll() {
        m_textViewTransparent.setVisibility(View.GONE);
        m_textViewQuiet.setVisibility(View.GONE);
        m_textViewCounter.setVisibility(View.GONE);
        m_buttonQuiet.setVisibility(View.GONE);
        m_buttonTransparent.setVisibility(View.GONE);

    }

    private void switchUi(int mode) {
        if (mode== ActivityServicePayload.TRANSPARENT) {
            m_textViewTransparent.setVisibility(View.VISIBLE);
            m_textViewQuiet.setVisibility(View.GONE);
            m_textViewCounter.setVisibility(View.GONE);
            m_buttonQuiet.setVisibility(View.VISIBLE);
            m_buttonTransparent.setVisibility(View.GONE);
        } else {
            m_textViewTransparent.setVisibility(View.GONE);
            m_textViewQuiet.setVisibility(View.VISIBLE);
            m_textViewCounter.setVisibility(View.VISIBLE);
            m_buttonQuiet.setVisibility(View.GONE);
            m_buttonTransparent.setVisibility(View.VISIBLE);
        }
    }//

    private void setCounter(int seconds) {
        if (seconds<0)
            return;

        String res = String.format("%02d:%02d", seconds/60, seconds%60);
        m_textViewCounter.setText(res);
    }//

    private void setDND() {
        m_textViewCounter.setText(getText(R.string.by_dnd));

    }

    void runService() {
        Intent intent = new Intent(this, MyTimerService.class);
        intent.putExtra(ActivityServicePayload.ASK_MODE, ActivityServicePayload.QUESTION);
        startService(intent);
    }

    void sendAsk(int mode) {
        Intent intent = new  Intent( ActivityServicePayload.ASK_INTENT);
        intent.putExtra(ActivityServicePayload.ASK_MODE, mode);
        sendBroadcast(intent);
    }//

    public class ResponseBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            int receivedMode = intent.getIntExtra(ActivityServicePayload.RESP_MODE, -1);
            if (receivedMode>-1) {
                switchUi(receivedMode);
            }

            if (intent.getBooleanExtra(ActivityServicePayload.RESP_DND, false)) {
                setDND();
            } else {
                int receivedSeconds = intent.getIntExtra(ActivityServicePayload.RESP_SECONDS, -1);
                if (receivedSeconds>-1) {
                    setCounter(receivedSeconds);
                }
            }
        }//
    }///

}///