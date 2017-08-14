package seedsoft.apps.com.appseedsoft.Location.Device;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Kritsana on 8/11/2017.
 */

public class Device_ {

    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("device_id")
    @Expose
    private String deviceId;
    @SerializedName("device_name")
    @Expose
    private String deviceName;
    @SerializedName("device_mac")
    @Expose
    private String deviceMac;
    @SerializedName("device_ip")
    @Expose
    private Object deviceIp;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getDeviceMac() {
        return deviceMac;
    }

    public void setDeviceMac(String deviceMac) {
        this.deviceMac = deviceMac;
    }

    public Object getDeviceIp() {
        return deviceIp;
    }

    public void setDeviceIp(Object deviceIp) {
        this.deviceIp = deviceIp;
    }
}
