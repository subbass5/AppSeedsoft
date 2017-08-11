package seedsoft.apps.com.appseedsoft.History;

import android.util.Log;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import seedsoft.apps.com.appseedsoft.Location.Location;

public class History {
    String jsonObj;
    private String lo;
       public History(String jsonObj){
           this.jsonObj = jsonObj;
       }


       public List<String> getValue(){
          List<String> value = new ArrayList<>();
//           Log.e("History",jsonObj);
           Gson gson = new Gson();
           Type collationType = new TypeToken<Collection<History_GET>>(){}.getType();
           Collection<History_GET> emums = gson.fromJson(jsonObj,collationType);
           History_GET[] history_gets = emums.toArray(new History_GET[emums.size()]);

           JSONObject jsonObject = new JSONObject();
           for (int i = 0 ;i <= history_gets.length-1 ;i++){
               try {
                   jsonObject.put("name",history_gets[i].getLocationName());
                   jsonObject.put("state",history_gets[i].getState());
                   jsonObject.put("key_time",history_gets[i].getKeyTime());
                   jsonObject.put("location_id",history_gets[i].getLocationId());
                   jsonObject.put("time",history_gets[i].getTime());
               } catch (JSONException e) {
                   e.printStackTrace();
               }
               value.add(jsonObject.toString());
           }
           return value;
       }
}


