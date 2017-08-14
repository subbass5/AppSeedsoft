package seedsoft.apps.com.appseedsoft.Location.Device;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import seedsoft.apps.com.appseedsoft.Location.Location;

/**
 * Created by Kritsana on 8/14/2017.
 */

public class Location_Device {
    String objStr;
    List<String> value = new ArrayList<>();

    public Location_Device(String objStr){
        this.objStr = objStr;
    }

    public List<String> getDevice(){

        JSONObject objJson = new JSONObject();
        Gson gson = new Gson();
        Type collationType = new TypeToken<Collection<Device_>>(){}.getType();
        Collection<Device_> emums = gson.fromJson(objStr,collationType);
        Device_[] result = emums.toArray(new Device_[emums.size()]);  //le_lo = Location result

        if(result.length>0){
            for (int i = 0 ; i< result.length;i++){

                try {

                    objJson.put("id",result[i].getId());
                    objJson.put("device_id",result[i].getDeviceId());
                    objJson.put("device_name",result[i].getDeviceName());
                    objJson.put("device_ip",result[i].getDeviceIp());
                    objJson.put("device_mac",result[i].getDeviceMac());

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                value.add(objJson.toString());
            }

        }

        return value;
    }

}
