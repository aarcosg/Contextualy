package es.us.contextualy.data;


import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.awareness.Awareness;
import com.google.android.gms.awareness.fence.AwarenessFence;
import com.google.android.gms.awareness.fence.DetectedActivityFence;
import com.google.android.gms.awareness.fence.FenceQueryRequest;
import com.google.android.gms.awareness.fence.FenceState;
import com.google.android.gms.awareness.fence.FenceStateMap;
import com.google.android.gms.awareness.fence.FenceUpdateRequest;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallbacks;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.DetectedActivity;

import java.util.Date;

import es.us.contextualy.R;
import es.us.contextualy.observable.google.GoogleAPIClientObservable;
import es.us.contextualy.receiver.FenceReceiver;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class AwarenessApiHelper {

    private static final String TAG = AwarenessApiHelper.class.getCanonicalName();
    private static final String ACTION_NEW_FENCE_STATE = "es.us.contextualy.ACTION_NEW_FENCE_STATE";
    private static final int REQUEST_CODE_FENCE_STATE = 1;
    public static final String MAIN_FENCE_KEY = "awarenessMainFenceKey";

    private static AwarenessApiHelper sInstance;
    private Context mContext;

    private AwarenessApiHelper(){}

    public static AwarenessApiHelper getInstance(Context context){
        if(sInstance == null){
            sInstance = new AwarenessApiHelper();
            sInstance.mContext = context.getApplicationContext();
        }
        if(sInstance.mContext == null){
            sInstance.mContext = context.getApplicationContext();
        }
        return sInstance;
    }

    public Observable<GoogleApiClient> getGoogleApiClientAwarenessObservable(){
        return GoogleAPIClientObservable.create(mContext, Awareness.API);
    }

    public void updateMainAwarenessFence(@Nullable AwarenessFence timeFence){
        AwarenessFence activitiesFence = getDetectedActivityFences();
        getGoogleApiClientAwarenessObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        googleApiClient -> registerFence(
                                googleApiClient,
                                AwarenessApiHelper.MAIN_FENCE_KEY,
                                timeFence != null ? AwarenessFence.and(activitiesFence, timeFence) : activitiesFence)
                        , throwable -> Log.e(TAG, throwable.getClass() + " @updateMainAwarenessFence")
                );
    }

    private void registerFence(final GoogleApiClient googleApiClient,
                                     final String fenceKey, final AwarenessFence fence) {
        Log.i(TAG,"@registerFence");
        if(FirebaseRemoteConfigHelper.getInstance().isContextRecognitionEnabled()){
            Log.d(TAG, "Context recognition enabled in remote config");
            Awareness.FenceApi.updateFences(
                    googleApiClient,
                    new FenceUpdateRequest.Builder()
                            .addFence(fenceKey, fence, getPendingIntent())
                            .build())
                    .setResultCallback(status -> {
                        if(status.isSuccess()) {
                            Log.i(TAG, "Fence was successfully registered.");
                            queryFence(googleApiClient, fenceKey);
                        } else {
                            Log.e(TAG, "Fence could not be registered: " + status);
                        }
                    });
        }else{
            Log.d(TAG, "Context recognition disabled in remote config");
        }
    }

    public void unregisterFence(final GoogleApiClient googleApiClient, final String fenceKey) {
        Awareness.FenceApi.updateFences(
                googleApiClient,
                new FenceUpdateRequest.Builder()
                        .removeFence(fenceKey)
                        .build()).setResultCallback(new ResultCallbacks<Status>() {
                            @Override
                            public void onSuccess(@NonNull Status status) {
                                Log.i(TAG, "Fence " + fenceKey + " successfully removed.");
                            }

                            @Override
                            public void onFailure(@NonNull Status status) {
                                Log.i(TAG, "Fence " + fenceKey + " could NOT be removed.");
                            }
                        });
    }

    private void queryFence(final GoogleApiClient googleApiClient, final String fenceKey) {
        Awareness.FenceApi.queryFences(googleApiClient,
                FenceQueryRequest.forFences(fenceKey))
                .setResultCallback(fenceQueryResult -> {
                    if (!fenceQueryResult.getStatus().isSuccess()) {
                        Log.e(TAG, "Could not query fence: " + fenceKey);
                        return;
                    }
                    FenceStateMap map = fenceQueryResult.getFenceStateMap();
                    for (String fenceKey1 : map.getFenceKeys()) {
                        FenceState fenceState = map.getFenceState(fenceKey1);
                        Log.i(TAG, "Fence " + fenceKey1 + ": "
                                + fenceState.getCurrentState()
                                + ", was="
                                + fenceState.getPreviousState()
                                + ", lastUpdateTime="
                                + new Date(fenceState.getLastFenceUpdateTimeMillis()));
                    }
                });
    }

    private PendingIntent getPendingIntent(){
        Intent fenceIntent = new Intent(mContext, FenceReceiver.class);
        fenceIntent.setAction(ACTION_NEW_FENCE_STATE);
        return PendingIntent.getBroadcast(mContext, REQUEST_CODE_FENCE_STATE, fenceIntent, 0);
    }

    private AwarenessFence getDetectedActivityFences(){
        return DetectedActivityFence.during(
                DetectedActivity.STILL,
                DetectedActivity.IN_VEHICLE,
                DetectedActivity.ON_BICYCLE,
                DetectedActivityFence.ON_FOOT,
                DetectedActivityFence.WALKING,
                DetectedActivityFence.RUNNING);
    }

    public String getActivityString(int detectedActivityType) {
        Resources resources = mContext.getResources();
        switch(detectedActivityType) {
            case DetectedActivity.IN_VEHICLE:
                return resources.getString(R.string.in_vehicle);
            case DetectedActivity.ON_BICYCLE:
                return resources.getString(R.string.on_bicycle);
            case DetectedActivity.ON_FOOT:
                return resources.getString(R.string.on_foot);
            case DetectedActivity.RUNNING:
                return resources.getString(R.string.running);
            case DetectedActivity.WALKING:
                return resources.getString(R.string.walking);
            case DetectedActivity.STILL:
                return resources.getString(R.string.still);
            default:
                return resources.getString(R.string.unknown);
        }
    }
}
