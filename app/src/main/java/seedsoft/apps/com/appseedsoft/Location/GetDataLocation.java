package seedsoft.apps.com.appseedsoft.Location;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GetDataLocation {

    Map<String,Object> objectMap = new HashMap<String,Object>();

    List<String> idLocation = new ArrayList<String>();
    List<String> nameLocation = new ArrayList<String>();

    public GetDataLocation(Map<String,Object> objectMap){
        this.objectMap = objectMap;

        for (Map.Entry<String, Object> entry : objectMap.entrySet()) {
            idLocation.add(entry.getKey());
            nameLocation.add(""+entry.getValue());
        }
    }
    public List<String> getIDLocation(){
        return idLocation;
    }

    public List<String> getNameLocation(){
        return nameLocation;
    }

}
