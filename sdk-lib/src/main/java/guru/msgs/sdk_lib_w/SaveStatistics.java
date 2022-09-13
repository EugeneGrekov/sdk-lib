package guru.msgs.sdk_lib_w;

import android.content.Context;
import android.content.SharedPreferences;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class SaveStatistics
{


    private SharedPreferences.Editor editor = null;
    private SharedPreferences sharedPref = null;

    //////
    public static class BuildData {
        public long msgsPostponed = 0L;
        public long msgsRaised = 0L;
        private Date mStart = new Date();
        public CollectData Export() {
            CollectData res = new CollectData();
            res.msgsPostponed = this.msgsPostponed;
            res.msgsRaised = this.msgsRaised;
            res.runTimes = 1;
            long diffInMillies = Math.abs(new Date().getTime() - mStart.getTime());
            res.runMinutes = TimeUnit.MINUTES.convert(diffInMillies, TimeUnit.MILLISECONDS);
            return res;
        }
    }///
    public static class CollectData extends BuildData{
        public long runTimes = 0L;
        public long runMinutes = 0L;
    }///
    public static class ExportData extends CollectData{
        private final static String CstartDate = "startDate";
        public String startDate;
        private final static String CrandomId = "randomId";
        public long randomId;
        private final static String CrunTimes = "runTimes";
        private final static String CrunMinutes = "runMinutes";
        private final static String CmsgsPostponed = "msgsPostponed";
        private final static String CmsgsRaised = "msgsRaised";
        private final static String C = "";
    }///

    public SaveStatistics(Context context) {
        sharedPref = context.getSharedPreferences("my_file", Context.MODE_PRIVATE);
        editor = sharedPref.edit();
        init();
    }

    public ExportData Export() {
        ExportData e = new ExportData();
        e.startDate = sharedPref.getString(ExportData.CstartDate, "");
        e.randomId = sharedPref.getLong(ExportData.CrandomId, 0L);
        e.runTimes = sharedPref.getLong(ExportData.CrunTimes, 0L);
        e.runMinutes = sharedPref.getLong(ExportData.CrunMinutes, 0L);
        e.msgsPostponed = sharedPref.getLong(ExportData.CmsgsPostponed, 0L);
        e.msgsRaised = sharedPref.getLong(ExportData.CmsgsRaised, 0L);
        return e;
    }

    private void init() {
        String fetchedDate = sharedPref.getString(ExportData.CstartDate, "");
        if (!"".equals(fetchedDate)) {
            return;
        }
        //need initi
        editor.putString(ExportData.CstartDate, new SimpleDateFormat("yyyy.MM.dd").format(new Date()) );
        editor.putLong(ExportData.CrandomId, new Random().nextLong() );
        editor.putLong(ExportData.CrunTimes ,0L);
        editor.putLong(ExportData.CrunMinutes ,0L);
        editor.putLong(ExportData.CmsgsPostponed ,0L);
        editor.putLong(ExportData.CmsgsRaised ,0L);
        editor.apply();
    }

    public void Update(CollectData d) {
        editor.putLong(ExportData.CrunTimes , sharedPref.getLong(ExportData.CrunTimes, 0L)+d.runTimes );
        editor.putLong(ExportData.CrunMinutes , sharedPref.getLong(ExportData.CrunMinutes, 0L)+d.runMinutes );
        editor.putLong(ExportData.CmsgsPostponed , sharedPref.getLong(ExportData.CmsgsPostponed, 0L)+d.msgsPostponed );
        editor.putLong(ExportData.CmsgsRaised , sharedPref.getLong(ExportData.CmsgsRaised, 0L)+d.msgsRaised );
        editor.apply();
    }


}///class SaveStatistics
