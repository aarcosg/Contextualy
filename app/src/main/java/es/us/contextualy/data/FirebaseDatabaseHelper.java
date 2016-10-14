package es.us.contextualy.data;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import es.us.contextualy.model.Context;
import es.us.contextualy.model.User;

public class FirebaseDatabaseHelper {

    private static final String TAG = FirebaseDatabaseHelper.class.getCanonicalName();

    private static final String REF_USERS = "_users";
    private static final String REF_CONTEXTS = "_contexts";

    private static FirebaseDatabaseHelper sInstance;
    private FirebaseDatabase mFirebaseDatabase;

    private FirebaseDatabaseHelper() {}

    public static FirebaseDatabaseHelper getInstance(){
        if(sInstance == null){
            sInstance = new FirebaseDatabaseHelper();
            sInstance.mFirebaseDatabase = FirebaseDatabase.getInstance();
        }
        return sInstance;
    }

    public FirebaseDatabase getFirebaseDatabase(){
        return sInstance.mFirebaseDatabase;
    }

    public User writeNewUser(String deviceId, String instanceId){
        String userId = mFirebaseDatabase.getReference().child(REF_USERS).push().getKey();
        User user = new User(userId, deviceId, instanceId, new Date().getTime());
        Map<String, Object> userValues = user.toMap();
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put(getEndpoint(REF_USERS) + userId, userValues);
        mFirebaseDatabase.getReference().updateChildren(childUpdates);
        return user;
    }

    public void updateInstanceIdToken(String userId, String token){
        Map<String, Object> updateValues = new HashMap<>();
        updateValues.put("instanceId", token);
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put(getEndpoint(REF_USERS) + userId, updateValues);
        mFirebaseDatabase.getReference().updateChildren(childUpdates);
    }

    public void writeNewContext(String userId, Long activityType, String activityName, GeoLocation location,
                                Double battery, boolean headphone, List<String> weatherConditions,
                                List<String> places){
        String contextId = mFirebaseDatabase.getReference().child(REF_CONTEXTS).push().getKey();
        Context context = new Context(contextId, userId, activityType, activityName, battery,
                headphone, weatherConditions, places, new Date().getTime());
        GeoFire geoFire = new GeoFire(mFirebaseDatabase.getReference());
        Map<String, Object> contextValues = context.toMap();
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put(getEndpoint(REF_CONTEXTS) + contextId, contextValues);
        mFirebaseDatabase.getReference().updateChildren(childUpdates);
        geoFire.setLocation(getEndpoint(REF_CONTEXTS) + contextId, location);
    }

    private String getEndpoint(String reference){
        return "/" + reference + "/";
    }

}
