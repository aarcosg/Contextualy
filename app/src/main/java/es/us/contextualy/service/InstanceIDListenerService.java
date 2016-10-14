package es.us.contextualy.service;

import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import es.us.contextualy.data.FirebaseDatabaseHelper;
import es.us.contextualy.data.SharedPreferencesHelper;
import es.us.contextualy.model.User;

public class InstanceIDListenerService extends FirebaseInstanceIdService {

    private static final String TAG = InstanceIDListenerService.class.getCanonicalName();

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is also called
     * when the InstanceID token is initially generated, so this is where
     * you retrieve the token.
     */
    // [START refresh_token]
    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        String userId = SharedPreferencesHelper.getInstance(this).getUserId();
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        SharedPreferencesHelper preferencesHelper = SharedPreferencesHelper.getInstance(this);
        Log.d(TAG, "Refreshed token: " + refreshedToken);
        if(TextUtils.isEmpty(userId)){
            // Save new user to Firebase database
            String deviceID = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
            User user = FirebaseDatabaseHelper.getInstance().writeNewUser(deviceID, refreshedToken);
            preferencesHelper.saveUserId(user.id);
            preferencesHelper.saveInstanceId(user.instanceId);
        }else{
            // Update instance id token
            FirebaseDatabaseHelper.getInstance().updateInstanceIdToken(userId, refreshedToken);
            preferencesHelper.saveInstanceId(refreshedToken);
        }
    }
}
