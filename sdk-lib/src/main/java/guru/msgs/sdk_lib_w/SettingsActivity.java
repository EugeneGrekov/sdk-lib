package guru.msgs.sdk_lib_w;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceFragmentCompat;

import com.guru.managed.notifications.R;
import java.util.Map;

import guru.msgs.sdk_lib.BuildConfig;
import guru.msgs.sdk_lib.utils.config.Configurations;
import guru.msgs.sdk_lib.utils.config.ServiceSettings;
import guru.msgs.sdk_lib.utils.contactpicker.ContactPreference;


public class SettingsActivity extends AppCompatActivity {
    final static public String TAG = "GURU.Settings";



    
    


    static public class AppSettings extends ServiceSettings {
        public int minutes;
    }///

    static public AppSettings GetSettings(Context ctx) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(ctx);
        AppSettings settings = new AppSettings();

        Map tmp123 = sharedPreferences.getAll();

        try {
            String strMinutes = sharedPreferences.getString(ctx.getString(R.string.set_value_minutes_key), "");
            settings.minutes = Integer.parseInt(strMinutes);
        } catch (Exception e) {}
        if (settings.minutes<=0) {
            settings.minutes = Configurations.DEFAULT_MINUTES_AMMOUNT;
        }

        settings.non_blocked_calendar = sharedPreferences.getBoolean( ctx.getString(R.string.set_non_blocked_calendar), Configurations.DEFAULT_NON_BLOCKED_CALENDAR);
        settings.non_blocked_urgent = sharedPreferences.getBoolean( ctx.getString(R.string.set_non_blocked_urgent), Configurations.DEFAULT_NON_BLOCKED_URGENT);

        settings.dnd_set = sharedPreferences.getBoolean( ctx.getString(R.string.set_dnd_set), Configurations.DEFAULT_DND_SET);
        settings.dnd_bypath = sharedPreferences.getBoolean( ctx.getString(R.string.set_dnd_bypath), Configurations.DEFAULT_DND_BYPATH);
        settings.dnd_ride_on = sharedPreferences.getBoolean( ctx.getString(R.string.set_dnd_ride_on), Configurations.DEFAULT_DND_RIDE_ON);

        for (int i = 1; i<Configurations.DEFAULT_WORDS_AMMOUNT; i++) {
            String word = sharedPreferences.getString(String.format("word%02d", i), "");
            String include = sharedPreferences.getString(String.format("include%02d", i), "");
            if (!"".equals(word)) {
                settings.words.add(word);
                settings.contain.add(include);
            }
        }//for

        for (int i = 1; i<Configurations.DEFAULT_CONTACT_AMMOUNT; i++) {
            String contact = sharedPreferences.getString(String.format("contact%02d", i), "");
            if (!"".equals(contact)) {
                settings.contact_names.add(contact);
            }
        }//for

        settings.set_ans_is = sharedPreferences.getBoolean( ctx.getString(R.string.set_ans_is), Configurations.DEFAULT_SET_ANS_IS);
        settings.set_ans_resp_gen = sharedPreferences.getString( ctx.getString(R.string.set_ans_resp_gen), ctx.getString(R.string.val_ans_resp_gen) );
        settings.set_ans_resp_time = sharedPreferences.getString( ctx.getString(R.string.set_ans_resp_time), ctx.getString(R.string.val_ans_resp_time) );


        if (BuildConfig.DEBUG)
            Log.i(TAG, "GET "+ settings.toString() );
        return settings;
    }//

    static protected void SetDefaults(Context ctx) {

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(ctx);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(ctx.getString( R.string.set_value_minutes_key), Integer.toString(Configurations.DEFAULT_MINUTES_AMMOUNT) );
        editor.putBoolean( ctx.getString(R.string.set_non_blocked_calendar), Configurations.DEFAULT_NON_BLOCKED_CALENDAR);
        editor.putBoolean( ctx.getString(R.string.set_non_blocked_urgent), Configurations.DEFAULT_NON_BLOCKED_URGENT);
        editor.putBoolean( ctx.getString(R.string.set_dnd_set), Configurations.DEFAULT_DND_SET);
        editor.putBoolean( ctx.getString(R.string.set_dnd_bypath), Configurations.DEFAULT_DND_BYPATH);
        editor.putBoolean( ctx.getString(R.string.set_dnd_ride_on), Configurations.DEFAULT_DND_RIDE_ON);

        editor.putString("word01", ctx.getString(R.string.val_word01) );
        editor.putString("include01", ctx.getString(R.string.val_include01) );
        editor.putString("word02", ctx.getString(R.string.val_word02) );
        editor.putString("include02", ctx.getString(R.string.val_include02) );
        editor.putString("word03", ctx.getString(R.string.val_word03) );
        editor.putString("include03", ctx.getString(R.string.val_include03) );
        editor.putString("word04", ctx.getString(R.string.val_word04) );
        editor.putString("include04", ctx.getString(R.string.val_include04) );
        editor.putString("word05", ctx.getString(R.string.val_word05) );
        editor.putString("include05", ctx.getString(R.string.val_include05) );
        editor.putString("word06", ctx.getString(R.string.val_word06) );
        editor.putString("include06", ctx.getString(R.string.val_include06) );
        editor.putString("word07", ctx.getString(R.string.val_word07) );
        editor.putString("include07", ctx.getString(R.string.val_include07) );
        editor.putString("word08", ctx.getString(R.string.val_word08) );
        editor.putString("include08", ctx.getString(R.string.val_include08) );
        editor.putString("word09", ctx.getString(R.string.val_word09) );
        editor.putString("include09", ctx.getString(R.string.val_include09) );

        editor.putBoolean( ctx.getString(R.string.set_ans_is), Configurations.DEFAULT_SET_ANS_IS);
        editor.putString( ctx.getString(R.string.set_ans_resp_gen), ctx.getString(R.string.val_ans_resp_gen) );
        editor.putString( ctx.getString(R.string.set_ans_resp_time), ctx.getString(R.string.val_ans_resp_time) );

        editor.apply();
        editor.commit();
    }//

    static public void SetDefaultsIfNeed(Activity activity) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity);
        String strMinutes = sharedPreferences.getString(activity.getString(R.string.set_value_minutes_key), "");
        if ("".equals(strMinutes)) {
            if (BuildConfig.DEBUG)
                Log.i(TAG, "SetDefaultsIfNeed: Set Defaults");
            SetDefaults(activity);
        } else {
            if (BuildConfig.DEBUG)
                Log.i(TAG, "SetDefaultsIfNeed: Not Need To Set Defaults");
        }
    }//


/////////////////////////////////////////

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, new SettingsFragment())
                    .commit();
        }
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }//



    static public class SettingsFragment extends PreferenceFragmentCompat {
        class Ask {
            ContactPreference mPreference;
            ActivityResultLauncher<Intent> mStartForContact = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    new ActivityResultCallback<ActivityResult>() {
                        @Override
                        public void onActivityResult(ActivityResult result) {
                            Intent intent = result.getData();
                            if (intent==null) {
                                mPreference.setText("");
                                return;
                            }
                            String strName = intent.getStringExtra(ContactPickerSampleActivity.INTENT_RES_NAME);
                            if (strName==null) {
                                mPreference.setText("");
                                return;
                            }
                            mPreference.setText(strName);
                        }
                    });
            Ask(ContactPreference aPreference) {
                mPreference = aPreference;
                mPreference.setOnPreferenceClickListener(preference -> {
                    mStartForContact.launch(new Intent(getContext(), ContactPickerSampleActivity.class));
                    return true;
                });
            }
        }

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
            new Ask((ContactPreference)findPreference("contact01"));
            new Ask((ContactPreference)findPreference("contact02"));
            new Ask((ContactPreference)findPreference("contact03"));
            new Ask((ContactPreference)findPreference("contact04"));
            new Ask((ContactPreference)findPreference("contact05"));
            new Ask((ContactPreference)findPreference("contact06"));
            new Ask((ContactPreference)findPreference("contact07"));
            new Ask((ContactPreference)findPreference("contact08"));
            new Ask((ContactPreference)findPreference("contact09"));
        }
    }//



}///