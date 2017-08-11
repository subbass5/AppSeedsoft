package seedsoft.apps.com.appseedsoft.Accesstime;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Accesstime_ {

    @SerializedName("key")
    @Expose
    private Integer key;
    @SerializedName("day")
    @Expose
    private String day;
    @SerializedName("timein")
    @Expose
    private String timein;
    @SerializedName("timeout")
    @Expose
    private String timeout;

    public Integer getKey() {
        return key;
    }

    public void setKey(Integer key) {
        this.key = key;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public String getTimein() {
        return timein;
    }

    public void setTimein(String timein) {
        this.timein = timein;
    }

    public String getTimeout() {
        return timeout;
    }

    public void setTimeout(String timeout) {
        this.timeout = timeout;
    }

}