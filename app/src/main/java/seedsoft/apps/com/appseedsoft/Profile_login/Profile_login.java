package seedsoft.apps.com.appseedsoft.Profile_login;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Dell3421 on 12/7/2560.
 */

public class Profile_login {
    private  String ID,staff_id,nameStaff,emailStaff,phoneStaff,pathProfile, status,locations;
    private final String Obj;
    public Profile_login(String Obj){
        this.Obj = Obj;
        try {
            JSONObject object = new JSONObject(Obj);
            ID = new String(object.getString("id"));
            staff_id = new String(object.getString("staff_id"));
            nameStaff = new String(object.getString("name"));
            emailStaff = new String(object.getString("email"));
            phoneStaff = new String(object.getString("phone"));
            pathProfile = new String(object.getString("img"));
            status = new String(object.getString("status"));
            locations = new String(object.getString("location"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    public String getID(){
        return ID;
    }
    public String getStaff_id(){
        return staff_id;
    }
    public String getNameStaff(){
        return nameStaff;
    }
    public String getEmailStaff(){
        return emailStaff;
    }
    public String getPhoneStaff(){
        return phoneStaff;
    }
    public String getPathProfile(){
        return pathProfile;
    }
    public String getStatus(){
        return status;
    }
    public String getLocations(){return locations;}

}
