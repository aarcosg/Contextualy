package es.us.contextualy.observable.google.awareness;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.android.gms.awareness.Awareness;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.PlaceLikelihood;

import java.util.List;

import rx.Observable;
import rx.Subscriber;

public class PlacesObservable implements Observable.OnSubscribe<List<PlaceLikelihood>> {

    private static final String TAG = PlacesObservable.class.getCanonicalName();

    private final Context mContext;
    private final GoogleApiClient mGoogleApiClient;

    public PlacesObservable(Context context, GoogleApiClient googleApiClient){
        this.mContext = context;
        this.mGoogleApiClient = googleApiClient;
    }

    @Override
    public void call(Subscriber<? super List<PlaceLikelihood>> subscriber) {
        if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Awareness.SnapshotApi.getPlaces(mGoogleApiClient)
                    .setResultCallback(placesResult -> {
                        if (placesResult.getStatus().isSuccess()) {
                            List<PlaceLikelihood> places = placesResult.getPlaceLikelihoods();
                            subscriber.onNext(places);
                            subscriber.onCompleted();
                        } else {
                            Log.e(TAG, "Could not get current places.");
                            subscriber.onCompleted();
                        }
                    });
        }else{
            subscriber.onCompleted();
        }
    }
}
