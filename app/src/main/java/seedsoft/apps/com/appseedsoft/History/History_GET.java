package seedsoft.apps.com.appseedsoft.History;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class History_GET {

    @SerializedName("time")
    @Expose
    private String time;
    @SerializedName("state")
    @Expose
    private String state;
    @SerializedName("key_time")
    @Expose
    private String keyTime;
    @SerializedName("location_id")
    @Expose
    private String locationId;
    @SerializedName("location_name")
    @Expose
    private String locationName;

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getKeyTime() {
        return keyTime;
    }

    public void setKeyTime(String keyTime) {
        this.keyTime = keyTime;
    }

    public String getLocationId() {
        return locationId;
    }

    public void setLocationId(String locationId) {
        this.locationId = locationId;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

}