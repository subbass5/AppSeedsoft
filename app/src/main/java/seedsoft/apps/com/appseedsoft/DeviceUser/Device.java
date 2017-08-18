package seedsoft.apps.com.appseedsoft.DeviceUser;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import seedsoft.apps.com.appseedsoft.History.History_GET;
import seedsoft.apps.com.appseedsoft.Location.Location;


public class Device {
    private String strJson;
    private List<String> listUID = new ArrayList<>();
    private List<String> listLocationId = new ArrayList<>();

    public Device(String strJson){
        this.strJson = strJson;

//        Log.e("strIn",strJson);
        Gson gson = new Gson();
        Type collationType = new TypeToken<Collection<RFID>>(){}.getType();
        Collection<RFID> emums = gson.fromJson(strJson,collationType);
        RFID[] result = emums.toArray(new RFID[emums.size()]);
//        Log.e("RESULT",""+result.length);
        for (int i  = 0;i<result.length;i++){
            listUID.add("UID: "+result[i].getUid());
            listLocationId.add(result[i].getLocationId());
        }

    }
    public List<String> getUID(){
        return listUID;
    }
    public List<String> getLocationID(){
        return listLocationId;
    }

}
