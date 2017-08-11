package seedsoft.apps.com.appseedsoft.Accesstime;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Accesstime {
    String objAccesstime;
    List<String> accesstime = new ArrayList<String>();
    JSONObject obj  = new JSONObject();

    public Accesstime(String objAccesstime){
        this.objAccesstime = objAccesstime;

        Gson gson = new Gson();
        Type collationType = new TypeToken<Collection<Accesstime_>>(){}.getType();
        Collection<Accesstime_> emums = gson.fromJson(objAccesstime,collationType);
        Accesstime_[] accesstimes = emums.toArray(new Accesstime_[emums.size()]);

        Log.d("Accesstime Size",""+accesstimes.length);

        for (int i = 0; i < accesstimes.length;i++ ) {
            try {
                obj.put("timein",accesstimes[i].getTimein());
                obj.put("timeout",accesstimes[i].getTimeout());
                obj.put("day",accesstimes[i].getDay());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            accesstime.add(obj.toString());
        }

    }

    public List<String> getAccesstime() {
        return accesstime;
    }

}