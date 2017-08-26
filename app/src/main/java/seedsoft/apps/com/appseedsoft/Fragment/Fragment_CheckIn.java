package seedsoft.apps.com.appseedsoft.Fragment;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.Voice;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.picasso.Picasso;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.concurrent.ExecutionException;

import rx.functions.Action1;
import seedsoft.apps.com.appseedsoft.AsyncTask_Pack.RxJava;
import seedsoft.apps.com.appseedsoft.Detail_mobile.Detail_mobile;

import seedsoft.apps.com.appseedsoft.Detail_mobile.MyDbHelper;
import seedsoft.apps.com.appseedsoft.GPSTracker.GPSTracker;
import seedsoft.apps.com.appseedsoft.AsyncTask_Pack.GetDataAPI;
import seedsoft.apps.com.appseedsoft.History.History;
import seedsoft.apps.com.appseedsoft.Location.Current_Location;
import seedsoft.apps.com.appseedsoft.Login_Activity;
import seedsoft.apps.com.appseedsoft.MQTT_SERVICE;
import seedsoft.apps.com.appseedsoft.MainActivity;
import seedsoft.apps.com.appseedsoft.Profile_login.Profile_login;
import seedsoft.apps.com.appseedsoft.R;

public class Fragment_CheckIn extends Fragment{
    private final String apiKey;
    String time;
    JSONObject sendCheckin;
    GPSTracker gps;
    Detail_mobile dt;

    //=====================mqtt================
    private static final String TAG = MainActivity.TAG;
    private String subscribeTopic = "/state/checkin/";
    private String broker       = MainActivity.broker;
    private String clientId     = MqttClient.generateClientId();
    private String username_mqtt = MainActivity.username;
    private String password     = MainActivity.password;
    private String topicCheckin = MainActivity.topicCheckin;

    private String state_check,time_check,name_current,lat_current,long_current,idLocation;
    private Handler scanHandler = new Handler();
    private int scan_interval_ms = 20000;
    private Profile_login profile;
    private Current_Location currentLocation;
    private ImageView img_prifile;

    private ProgressDialog progress;

    private Button btnCheck;

    SharedPreferences sharedpreferences;
    public static final String MyPREFERENCES = "User" ;
    String keytime = "";
    String getKeytime = "";
    String feedData= "";
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    //DATA BASE
    SQLiteDatabase mDb;
    MyDbHelper mHelper;
    Cursor mCursor;

    private TextView tv1,tv_time,tv_date,tv_location,tv_state_now,tv_day;
    public static final String url = "http://128.199.196.236/api/staff?api_token=";

    MQTT_SERVICE mqtt_service;
    RxJava rxJava;

    public Fragment_CheckIn(String apiKey){
        this.apiKey = apiKey;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_checkin,container,false);

        return v;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        img_prifile = (ImageView) view.findViewById(R.id.img_profile_checkin);
        btnCheck = (Button) view.findViewById(R.id.btn_checkin);
        tv1 = (TextView) view.findViewById(R.id.txt_name_user);
        tv_time = (TextView) view.findViewById(R.id.txt_time);
        tv_date = (TextView) view.findViewById(R.id.txt_date);
        tv_location = (TextView) view.findViewById(R.id.txt_local);
        tv_state_now  = (TextView) view.findViewById(R.id.txt_state_now);
        tv_day = (TextView) view.findViewById(R.id.txt_day);

    }

    private void init(){

        dt = new Detail_mobile();
        mqtt_service = new MQTT_SERVICE(getContext());

        // get  session
        pref = getContext().getSharedPreferences(Login_Activity.MyPREFERENCES, 0);
        editor = pref.edit();

        mHelper = new MyDbHelper(getContext());
        mDb = mHelper.getWritableDatabase();
        sharedpreferences = getActivity().getSharedPreferences(MyPREFERENCES,Context.MODE_PRIVATE);
        editor = sharedpreferences.edit();

        rxJava = new RxJava(url,apiKey);
        rxJava.getFeedDataAPI().subscribe(new Action1<String>() {
            @Override
            public void call(String s) {
//                Log.e("Data",s);
                editor.putString(Login_Activity.JSON_OBJ,s);
                editor.commit();
            }
        });

        setUiFragment();

    }

    private void setUiFragment(){
        String data = pref.getString(Login_Activity.JSON_OBJ,null);
        if(!TextUtils.isEmpty(data)){
            profile = new Profile_login(data);
            currentLocation = new Current_Location(profile.getLocations_Array(),getGPS());
            idLocation = currentLocation.getIdLocation();
            name_current = currentLocation.getNamelocation();
            lat_current = currentLocation.getLatitude();
            long_current = currentLocation.getLongitude();

            if(!TextUtils.isEmpty(name_current)){
                tv_state_now.setText("Wellcome.");
                tv_state_now.setTextColor(getActivity().getResources().getColor(R.color.bootstrap_brand_primary));
                tv_location.setText(currentLocation.getNamelocation());
            }else {
                tv_location.setText("คุณอยู่นอกพื้นที่");
                tv_state_now.setText("Wellcome.");
                tv_state_now.setTextColor(getActivity().getResources().getColor(R.color.bootstrap_brand_info));
                btnCheck.setEnabled(false);
                btnCheck.setBackgroundTintList(ColorStateList.valueOf(getActivity().getResources().getColor(R.color.bootstrap_brand_warning)));
            }

            tv_date.setText(""+dt.getDateFromat("dd/MM/yyyy"));
            tv_day.setText(""+dt.dayName());
            tv_time.setText(""+dt.getDateFromat("HH:mm"));
            tv1.setText(""+profile.getNameStaff());

            // put data to session
            editor.putString(Login_Activity.JSON_OBJ,data);
            editor.putString(Login_Activity.ID_LOCATION,idLocation);
            editor.putString(Login_Activity.USER_LOCATION,profile.getLocations_Array());
            editor.commit();

            Picasso.with(getActivity().getApplicationContext())
                    .load(profile.getPathProfile())
                    .resize(150, 150)
                    .centerCrop()
                    .into(img_prifile);

            // history
            if (profile.getHistory_Array()!=null){
                History history = new History(profile.getHistory_Array());
                List<String> value = history.getValue();
                try {
                    JSONObject historyJson = new JSONObject(value.get(0).toString());
                    state_check =  historyJson.getString("state");
                    time_check = historyJson.getString("time");
                    getKeytime = historyJson.getString("key_time");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                upDateUI(state_check,time_check);
            }
            btnCheck.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    alertConfirm();
                }
            });

        }else{

            tv_date.setText(""+dt.getDateFromat("dd/MM/yyyy"));
            tv_day.setText(""+dt.dayName());
            tv_time.setText(""+dt.getDateFromat("HH:mm"));
            tv1.setText("Unknow People");
            Toast.makeText(getContext(), "Server error.", Toast.LENGTH_SHORT).show();
            Log.e("Data from server","is Error.");
            btnCheck.setEnabled(false);
            btnCheck.setBackgroundTintList(ColorStateList.valueOf(getActivity().getResources().getColor(R.color.bootstrap_brand_danger)));
        }
    }

    private Runnable scanRunnable = new Runnable()
    {
        @Override
        public void run() {
            try {
                tv_date.setText(""+dt.getDateFromat("dd/MM/yyyy"));
                tv_day.setText(""+dt.dayName());
                tv_time.setText(""+dt.getDateFromat("HH:mm"));

                scanHandler.postDelayed(scanRunnable, scan_interval_ms);
            }catch (Exception e){
                Log.e("Error at ScanRunnable",e.toString());
            }
        }
    };


    public void alertConfirm(){
      try{
          feedData = pref.getString(Login_Activity.JSON_OBJ,null);
          profile = new Profile_login(feedData);
        if (profile.getHistory_Array()!=null) {
            History history = new History(profile.getHistory_Array());
            List<String> value = history.getValue();
            try {
                JSONObject historyJson = new JSONObject(value.get(0).toString());
                state_check = historyJson.getString("state");
                time_check = historyJson.getString("time");
                getKeytime = historyJson.getString("key_time");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        AlertDialog.Builder builder =
                new AlertDialog.Builder(getContext());
        builder.setMessage("คุณต้องการ "+btnCheck.getText()+" หรือไม่ ?");
        builder.setPositiveButton("ใช่", new DialogInterface.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            public void onClick(DialogInterface dialog, int id) {
                String state = btnCheck.getText().toString();
                time = ""+tv_date.getText().toString()+" "+tv_time.getText().toString();

                if(state.equals("Check In")){
                    keytime = profile.getID()+"|"+dt.getDateFromat("yyyyMMddHHmm");
                }
                if(state.equals("Check Out")){
                    keytime = getKeytime;
                }
                sendCheckin = new JSONObject();
                try {
                    sendCheckin.put("keyStaff",profile.getID());
                    sendCheckin.put("state",""+state);
                    sendCheckin.put("id_location",idLocation);
                    sendCheckin.put("lat",lat_current);
                    sendCheckin.put("key_time",keytime);
                    sendCheckin.put("long",long_current);
                    sendCheckin.put("time",time);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                mqtt_service.publishMessage(topicCheckin,sendCheckin.toString());
                Toast.makeText(getContext(), ""+btnCheck.getText()+" เสร็จสิ้น", Toast.LENGTH_SHORT).show();
                upDateUI(state,time);
                dialog.dismiss();
            }
        });
        builder.setNegativeButton("ไม่ใช่", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();

      }catch (Exception e){
          Log.e("Error at alertConfirm()",e.toString());
      }
    }


    private void upDateUI(String status ,String time){
        try{
            if (status.equals("Check In")) {
                btnCheck.setText("Check Out");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    btnCheck.setBackgroundTintList(ColorStateList.valueOf(getActivity().getResources().getColor(R.color.bootstrap_brand_danger)));
                }
                tv_state_now.setTextColor(getActivity().getResources().getColor(R.color.bootstrap_brand_success));
                tv_state_now.setText("Status : " + status + "  Time :" + time);
            } else {
                btnCheck.setText("Check In");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    btnCheck.setBackgroundTintList(ColorStateList.valueOf(getActivity().getResources().getColor(R.color.bootstrap_brand_info)));
                }
                tv_state_now.setText("Status : " + status + "  Time :" + time);
                tv_state_now.setTextColor(getActivity().getResources().getColor(R.color.bootstrap_brand_warning));
//                Toast.makeText(getActivity().getApplicationContext(), "Check Out successfully.", Toast.LENGTH_SHORT).show();
            }
        }catch (Exception e){
            Log.e("Error at upDateUI()",e.toString());
        }

    }

    private String getGPS(){
        try{
                JSONObject json  = new JSONObject();
                gps = new GPSTracker(getContext());

                if (!gps.canGetLocation()){
                    gps.showSettingsAlert();
                }
                try {
                    json.put("lat",gps.getLatitude());
                    json.put("longi",gps.getLongitude());

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                    return json.toString();

        }catch(Exception e){

            Log.e("Error at getGPS()",e.toString());
            return null;
        }
    }

    @Override
    public void onStop() {
        try {
            mqtt_service.unSubscribe();
            scanHandler.removeCallbacksAndMessages(scanRunnable);
        }catch (Exception e){
            Log.e("Error at Onstop checkin",e.getMessage());
        }

        super.onStop();
    }


    @Override
    public void onResume() {
        try{
            init();
            scanHandler.post(scanRunnable);
        }catch (Exception e){
            Log.e("Error at onResume Checkin",e.toString());
        }
        super.onResume();
    }
}
