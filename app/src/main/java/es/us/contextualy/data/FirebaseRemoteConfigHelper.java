package es.us.contextualy.data;

import android.util.Log;

import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import es.us.contextualy.BuildConfig;
import es.us.contextualy.R;

public class FirebaseRemoteConfigHelper {

    private static final String TAG = FirebaseRemoteConfigHelper.class.getCanonicalName();

    private static final String REMOTE_CONFIG_CONTEXT_RECOGNITION = "is_context_recognition_on";
    private static final String REMOTE_CONFIG_DETECTION_INTERVAL_IN_MILLIS = "detection_interval_in_millis";
    private static final String REMOTE_CONFIG_STILL_RESET_INTERVAL_IN_MILLIS = "still_reset_interval_in_millis";
    private static final String REMOTE_CONFIG_STILL_CONTEXT_RESET = "is_still_context_reset_on";
    private static final String REMOTE_CONFIG_MIN_DETECTED_ACTIVITY_CONFIDENCE = "min_detected_activity_confidence";

    private static FirebaseRemoteConfigHelper sInstance;
    private FirebaseRemoteConfig mFirebaseRemoteConfig;

    private FirebaseRemoteConfigHelper(){}

    public static FirebaseRemoteConfigHelper getInstance(){
        if(sInstance == null){
            sInstance = new FirebaseRemoteConfigHelper();
            sInstance.mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
            FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                    .setDeveloperModeEnabled(BuildConfig.DEBUG)
                    .build();
            sInstance.mFirebaseRemoteConfig.setConfigSettings(configSettings);
        }
        return sInstance;
    }

    public FirebaseRemoteConfig getFirebaseRemoteConfig(){
        return mFirebaseRemoteConfig;
    }

    public void setDefaults(){
        mFirebaseRemoteConfig.setDefaults(R.xml.remote_config_defaults);
    }

    public boolean isContextRecognitionEnabled(){
        return mFirebaseRemoteConfig.getBoolean(REMOTE_CONFIG_CONTEXT_RECOGNITION);
    }

    public Long getDetectionIntervalInMillis(){
        return mFirebaseRemoteConfig.getLong(REMOTE_CONFIG_DETECTION_INTERVAL_IN_MILLIS);
    }

    public boolean isContextResetIfStillEnabled(){
        return mFirebaseRemoteConfig.getBoolean(REMOTE_CONFIG_STILL_CONTEXT_RESET);
    }

    public Long getStillResetIntervalInMillis(){
        return mFirebaseRemoteConfig.getLong(REMOTE_CONFIG_STILL_RESET_INTERVAL_IN_MILLIS);
    }

    public Long getMinDetectedActivityConfidence(){
        return mFirebaseRemoteConfig.getLong(REMOTE_CONFIG_MIN_DETECTED_ACTIVITY_CONFIDENCE);
    }

    public void fetchRemoteConfig() {
        mFirebaseRemoteConfig.fetch(0)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Fetch Succeeded");
                    // Once the config is successfully fetched it must be activated before newly fetched
                    // values are returned.
                    mFirebaseRemoteConfig.activateFetched();
                });
    }
}
