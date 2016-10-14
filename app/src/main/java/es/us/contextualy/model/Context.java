package es.us.contextualy.model;


import com.firebase.geofire.GeoLocation;
import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Context {

    public String id;
    public String userId;
    public Long activityType;
    public String activityName;
    public GeoLocation location;
    public Double battery;
    public boolean headphone;
    public List<String> weatherConditions;
    public List<String> places;
    public Long timestamp;

    public Context() {
    }

    public Context(Long activityType, String activityName, GeoLocation location, Double battery, boolean headphone,
                   List<String> weatherConditions, List<String> places, Long timestamp) {
        this.activityType = activityType;
        this.activityName = activityName;
        this.location = location;
        this.battery = battery;
        this.headphone = headphone;
        this.weatherConditions = weatherConditions;
        this.places = places;
        this.timestamp = timestamp;
    }

    public Context(String id, String userId, Long activityType, String activityName, Double battery,
                   boolean headphone, List<String> weatherConditions, List<String> places, Long timestamp) {
        this.id = id;
        this.userId = userId;
        this.activityType = activityType;
        this.activityName = activityName;
        this.battery = battery;
        this.headphone = headphone;
        this.weatherConditions = weatherConditions;
        this.places = places;
        this.timestamp = timestamp;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("id", id);
        result.put("userId", userId);
        result.put("activityType", activityType);
        result.put("activityName", activityName);
        result.put("location", location);
        result.put("battery", battery);
        result.put("headphone", headphone);
        result.put("weatherConditions", weatherConditions);
        result.put("places", places);
        result.put("timestamp", timestamp);
        return result;
    }
}
