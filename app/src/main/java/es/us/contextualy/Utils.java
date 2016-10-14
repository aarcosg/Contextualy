package es.us.contextualy;

import android.content.Context;
import android.content.pm.PackageManager;
import android.widget.Toast;

public class Utils {
    private static final String TAG = Utils.class.getCanonicalName();

    public static String getAppVersion(Context context){
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static void showToast(Context context, String message){
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }
}
