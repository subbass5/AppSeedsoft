package seedsoft.apps.com.appseedsoft.Location;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.Collection;

/**
 * Created by Dell3421 on 13/7/2560.
 */

public class Current_Location {
    private final String objLocation;
    private final String objCurrent;
    private double lat_cur ,longi_cur,distance ;
    private String namelocation ,latitude,longitude,distance_sidework;
    JSONObject jsonObject;

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
             distance = Distance(lat_cur,longi_cur,re_lo[j].getLat(),re_lo[j].getLong(),'K');
            if (distance < 1){
                namelocation = re_lo[j].getName();
                latitude = ""+re_lo[j].getLat();
                longitude = ""+re_lo[j].getLong();
                distance_sidework = ""+distance+"Km.";
            }else {
                namelocation = "You are out of place.";
                latitude = ""+lat_cur;
                longitude = ""+longi_cur;
                distance_sidework = ""+distance+"Km.";
            }
        }
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
    public String getDistance(){
        return distance_sidework;
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
