package seedsoft.apps.com.appseedsoft.DeviceUser;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class RFID {

    @SerializedName("rfid_id")
    @Expose
    private String rfidId;
    @SerializedName("uid")
    @Expose
    private String uid;
    @SerializedName("location_id")
    @Expose
    private String locationId;

    public String getRfidId() {
        return rfidId;
    }

    public void setRfidId(String rfidId) {
        this.rfidId = rfidId;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getLocationId() {
        return locationId;
    }

    public void setLocationId(String locationId) {
        this.locationId = locationId;
    }

}