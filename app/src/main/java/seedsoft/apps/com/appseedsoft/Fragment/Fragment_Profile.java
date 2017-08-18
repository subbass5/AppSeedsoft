package seedsoft.apps.com.appseedsoft.Fragment;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Observer;
import rx.functions.Action1;
import rx.functions.Func2;
import rx.observers.Observers;
import seedsoft.apps.com.appseedsoft.AsyncTask_Pack.RxJava;
import seedsoft.apps.com.appseedsoft.DeviceUser.Device;
import seedsoft.apps.com.appseedsoft.Location.Device.Location_Device;
import seedsoft.apps.com.appseedsoft.Login_Activity;
import seedsoft.apps.com.appseedsoft.Profile_login.Profile_login;
import seedsoft.apps.com.appseedsoft.R;

/**
 * Created by Kritsana on 7/21/2017.
 */

public class Fragment_Profile extends android.support.v4.app.Fragment {
        private ProgressDialog progress;
        TextView tvName, tvDepartment, tvPosition,tv_phone,tv_email,tv_keynumber,tv_lblaccess;
        Profile_login profile_login;
        ImageView img;

        String url = "http://128.199.196.236/api/staff?api_token=";

        SharedPreferences pref;
        SharedPreferences.Editor editor;
        String api_key_profile;
        List<String> listUid = new ArrayList<>();
        List<String> listLocationId = new ArrayList<>();
        ListView listDevice;
        String uid_rfid;
        List<String> value = new ArrayList<>();

        ArrayAdapter<String> mAdapter ;
        Location_Device device;
        RxJava rxJava;

        public Fragment_Profile(String api_key_profile) {
            this.api_key_profile = api_key_profile;

        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.fragment_profile, container, false);
            tvName = (TextView) v.findViewById(R.id.tv_name_profile);
            tvDepartment  = (TextView) v.findViewById(R.id.tv_department_profile);
            tvPosition = (TextView) v.findViewById(R.id.tv_position_profile);
            tv_phone = (TextView) v.findViewById(R.id.tv_phone_profile);
            tv_email = (TextView) v.findViewById(R.id.tv_email_profile);
            listDevice = (ListView) v.findViewById(R.id.list_device);
            tv_keynumber = (TextView) v.findViewById(R.id.tv_keynumber);
            tv_lblaccess = (TextView) v.findViewById(R.id.tv_lblaccess);

            return v;
        }

    @Override
    public void onViewCreated(View view,Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        pref = getContext().getSharedPreferences(Login_Activity.MyPREFERENCES, 0);
        editor = pref.edit();
    }

    private void init(){
        try {
//            api_key_profile = pref.getString(Login_Activity.API, null);
            rxJava = new RxJava(url, api_key_profile);
            rxJava.getFeedDataAPI().subscribe(new Action1<String>() {
                @Override
                public void call(String s) {

                    JSONObject jobj = null;
                    String jsonss = null;
                    try {
                        jobj = new JSONObject(s);
                        jsonss = jobj.getString("keycard");
                        editor.putString(Login_Activity.JSON_OBJ, s);
                        editor.putString(Login_Activity.Keycard, jsonss);
                        editor.putString(Login_Activity.API,api_key_profile);
                        editor.commit();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });

            UpdateUi();

        }catch (Exception e){

            Log.e("Error at Profile init",e.getMessage());

        }
    }

    private void UpdateUi(){
        try {
            profile_login = new Profile_login(pref.getString(Login_Activity.JSON_OBJ,null));
            tvName.setText(profile_login.getNameStaff());
            tvDepartment.setText(profile_login.getDepartment());
            tvPosition.setText(profile_login.getPosition());
            tv_email.setText(profile_login.getEmailStaff());
            tv_phone.setText(profile_login.getPhoneStaff());

                String strKeycard = pref.getString(Login_Activity.Keycard,null);
                List<String> dataName = new ArrayList<>();
                if(TextUtils.isEmpty(strKeycard)){
                    tv_keynumber.setText("ยังไม่มี RFID.");
                    tv_lblaccess.setText("");

                }else {
                    JSONArray jsonarr = new JSONArray(strKeycard);
                    JSONObject jsonObject = new JSONObject(String.valueOf(jsonarr.get(0)));
                    uid_rfid = jsonObject.getString("code");
                    device = new Location_Device(jsonObject.getString("device"));
                    value = device.getDevice();
                    Log.e("Value",""+value.size());

                    for (int i=0 ;i<value.size();i++){
                        JSONObject obj = new JSONObject(value.get(i));
                        dataName.add(obj.getString("device_name"));
                    }

                    mAdapter = new ArrayAdapter<>(getContext(),android.R.layout.simple_expandable_list_item_1,dataName);
                    listDevice.setAdapter(mAdapter);
                    tv_keynumber.setText(uid_rfid);
                    tv_lblaccess.setText("Name Access Control");
                }

        } catch (JSONException e) {
            Log.e("Eror at Profile JSON",e.getMessage());
        }catch (Exception e){
            Log.e("Eror at Profile UpdateUi",e.getMessage());
        }
    }

    @Override
    public void onResume() {
        init();
        super.onResume();
    }
}


