package es.us.contextualy.model;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

public class User {

    public String id;
    public String deviceId;
    public String instanceId;
    public Long timestamp;

    public User() {
    }

    public User(String id, String deviceId, String instanceId, Long timestamp) {
        this.id = id;
        this.deviceId = deviceId;
        this.instanceId = instanceId;
        this.timestamp = timestamp;
    }

    @Exclude
    public Map<String, Object> toMap(){
        HashMap<String, Object> result = new HashMap<>();
        result.put("id", id);
        result.put("deviceId", deviceId);
        result.put("instanceId", instanceId);
        result.put("timestamp", timestamp);
        return result;
    }
}
