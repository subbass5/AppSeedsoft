package seedsoft.apps.com.appseedsoft.Location;

import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Dell3421 on 13/7/2560.
 */

public class Current_Location {
    private final String objLocation;
    private final String objCurrent;
    private double lat_cur ,longi_cur,distance ;
    private String idLocation,namelocation ,latitude,longitude;
    private double distance_sidework;
    JSONObject jsonObject;
    private Map<String,Object> location = new HashMap<String,Object>();

    public Current_Location(String objLocation,String objCurrent){

        this.objCurrent = objCurrent;
        this.objLocation = objLocation;
        Gson gson = new Gson();
        Type collationType = new TypeToken<Collection<Location>>(){}.getType();
        Collection<Location> emums = gson.fromJson(objLocation,collationType);
        Location[] re_lo = emums.toArray(new Location[emums.size()]);  //le_lo = Location result


//        locationsResult[0].getDistance();
        try {
            jsonObject = new JSONObject(objCurrent);
            lat_cur = (double) jsonObject.get("lat");
            longi_cur = (double) jsonObject.get("longi");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        for (int j = 0 ;j < re_lo.length;j++){

            location.put(re_lo[j].getId(),re_lo[j].getName());

             distance = Distance(lat_cur,longi_cur,re_lo[j].getLat(),re_lo[j].getLong(),'K');
//            Log.d("Location",""+distance);
            if (distance <= 1){
                idLocation = re_lo[j].getId();
                namelocation = re_lo[j].getName();
                latitude = ""+re_lo[j].getLat();
                longitude = ""+re_lo[j].getLong();
                distance_sidework = distance;
            }
        }
//        Log.e("Data GSON",""+location.get("596709ab18b00b2b4e568632"));
    }

    public String getNamelocation(){
      return namelocation;
    }
    public String getLatitude(){
        return latitude;
    }
    public String getLongitude(){
        return longitude;
    }
    public double getDistance(){
        return distance_sidework;
    }
    public String getIdLocation(){
        return idLocation;
    }
    public Map<String,Object> getLocationAll(){
        return location;
    }


    private double Distance(double lat1, double lon1, double lat2, double lon2, char unit) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        if (unit == 'K') {
            dist = dist * 1.609344;
        } else if (unit == 'N') {
            dist = dist * 0.8684;
        }
        return (dist);
    }

    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    private double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }

}
