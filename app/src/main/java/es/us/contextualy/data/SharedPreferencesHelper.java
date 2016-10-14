package es.us.contextualy.data;


import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class SharedPreferencesHelper {

    private static final String TAG = SharedPreferencesHelper.class.getCanonicalName();

    private static final String PROPERTY_USER_ID = "user_id";
    private static final String PROPERTY_LAST_ACTIVITY = "last_activity";
    private static final String PROPERTY_LAST_CANDIDATE_ACTIVITY = "last_candidate_activity";
    private static final String PROPERTY_LAST_NOTIFICATION_TIME = "last_notification_time";
    private static final String PROPERTY_INSTANCE_ID = "instance_id";

    private static SharedPreferencesHelper sInstance;
    private SharedPreferences mPreferences;

    private SharedPreferencesHelper(){}

    public static SharedPreferencesHelper getInstance(Context context){
        if(sInstance == null){
            sInstance = new SharedPreferencesHelper();
            sInstance.mPreferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        }
        return sInstance;
    }

    public SharedPreferences getSharedPreferences(){
        return mPreferences;
    }

    public String getUserId(){
        return mPreferences.getString(PROPERTY_USER_ID, "");
    }

    public void saveUserId(String userId){
        mPreferences.edit().putString(PROPERTY_USER_ID, userId).commit();
    }

    public String getInstanceId(){
        return mPreferences.getString(PROPERTY_INSTANCE_ID, "");
    }

    public void saveInstanceId(String instanceIdToken){
        mPreferences.edit().putString(PROPERTY_INSTANCE_ID, instanceIdToken).commit();
    }

    public String getLastActivity(){
        return mPreferences.getString(PROPERTY_LAST_ACTIVITY, "");
    }

    public void saveLastActivity(String activity){
        mPreferences.edit().putString(PROPERTY_LAST_ACTIVITY, activity).commit();
    }

    public String getLastCandidateActivity(){
        return mPreferences.getString(PROPERTY_LAST_CANDIDATE_ACTIVITY, "");
    }

    public void saveLastCandidateActivity(String activity){
        mPreferences.edit().putString(PROPERTY_LAST_CANDIDATE_ACTIVITY, activity).commit();
    }

    public Long getLastNotificationTime(){
        return mPreferences.getLong(PROPERTY_LAST_NOTIFICATION_TIME, 0L);
    }

    public void saveLastNotificationTime(Long time){
        mPreferences.edit().putLong(PROPERTY_LAST_NOTIFICATION_TIME, time).commit();
    }

    public void resetActivityRecognitionProperties(){
        mPreferences.edit()
                .remove(PROPERTY_LAST_ACTIVITY)
                .remove(PROPERTY_LAST_CANDIDATE_ACTIVITY)
                .remove(PROPERTY_LAST_NOTIFICATION_TIME)
                .commit();
    }

    public void saveNewActivityRecognized(String activity){
        mPreferences.edit()
                .putString(PROPERTY_LAST_ACTIVITY, activity)
                .remove(PROPERTY_LAST_CANDIDATE_ACTIVITY)
                .commit();
    }

}
