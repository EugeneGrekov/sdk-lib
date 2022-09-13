package guru.msgs.notifications;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

import guru.msgs.sdk_lib_w.SaveStatistics;


public class OpenSurvay
{

    private final SaveStatistics.ExportData mStatistics;
    private final Activity mActivity;

    public OpenSurvay(Activity Activity) {
        mActivity = Activity;
        mStatistics = new SaveStatistics(Activity).Export();

    }

    public void Open1() {
        String url = "https://docs.google.com/forms/d/e/1FAIpQLSd5srsHUvA0qok0HxvasyJjd1PveCsFBEq198kXyzpwM-jrGA/viewform?usp=pp_url&entry.2052329166="+ mStatistics.randomId;
        Uri uri = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        if (intent.resolveActivity(mActivity.getPackageManager()) != null) {
            mActivity.startActivity(intent);
        }
    }//

    public void Open2() {
        String url = "https://docs.google.com/forms/d/e/1FAIpQLSe-NdkIRxaasw7rl-DJOerJQtRBy0cO9IkZ2fU_FDbgCK99Fg/viewform?usp=pp_url&entry.818809616="+ mStatistics.randomId +
                "&entry.349437088="+ mStatistics.runTimes +
                "&entry.1545156922="+ mStatistics.runMinutes +
                "&entry.641189897="+ mStatistics.msgsPostponed +
                "&entry.1624109679="+ mStatistics.msgsRaised +
                "&entry.1974526433="+ mStatistics.startDate;
        Uri uri = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        if (intent.resolveActivity(mActivity.getPackageManager()) != null) {
            mActivity.startActivity(intent);
        }
    }


}///
