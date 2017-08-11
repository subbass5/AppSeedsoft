package seedsoft.apps.com.appseedsoft;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.speech.tts.Voice;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import seedsoft.apps.com.appseedsoft.Accesstime.Accesstime;
import seedsoft.apps.com.appseedsoft.AsyncTask_Pack.GetDataAPI;
import seedsoft.apps.com.appseedsoft.Check_internet.ConnectionDetector;
import seedsoft.apps.com.appseedsoft.Detail_mobile.Detail_mobile;
import seedsoft.apps.com.appseedsoft.Detail_mobile.MyDbHelper;
import seedsoft.apps.com.appseedsoft.Fragment.Fragment_CheckIn;
import seedsoft.apps.com.appseedsoft.Fragment.Fragment_Dashbord;
import seedsoft.apps.com.appseedsoft.Fragment.Fragment_History;
import seedsoft.apps.com.appseedsoft.Fragment.Fragment_Profile;
import seedsoft.apps.com.appseedsoft.Fragment.Fragment_dashboardV2;
import seedsoft.apps.com.appseedsoft.GPSTracker.GPSTracker;
import seedsoft.apps.com.appseedsoft.History.History;
import seedsoft.apps.com.appseedsoft.Location.Current_Location;
import seedsoft.apps.com.appseedsoft.Profile_login.Profile_login;

import uk.co.alt236.bluetoothlelib.device.BluetoothLeDevice;
import uk.co.alt236.bluetoothlelib.device.beacon.BeaconType;
import uk.co.alt236.bluetoothlelib.device.beacon.BeaconUtils;
import uk.co.alt236.bluetoothlelib.device.beacon.ibeacon.IBeaconDevice;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    JSONObject obj;

    private static final String LOG_TAG = "MainActivity1";
    private BluetoothManager btManager;
    private BluetoothAdapter btAdapter;
    private Handler scanHandler = new Handler();
    private int scan_interval_ms = 10000;
    private boolean isScanning = false;
    private GPSTracker gps;

    //=====================mqtt
    public static final String TAG = "MQTT_Seed-soft";
    public static final String subscribeTopic = "/device/door/";
    public static final  String broker       = "tcp://188.166.188.78:1883";
    public static final  String clientId     = MqttClient.generateClientId();
    public static final  String username     = "seedsoft";
    public static final  String password     = "seedsoft";
    public static final  String topicMobile  = "/device/mobile/";
    public static final  String topicMobile_becon = "/device/mobile_beacon/";
    public static final  String topicCheckin = "/auth/mobile/";
    public static String url = "http://128.199.196.236/api/staff?api_token=";

    public String Devicename,mac,Api_key,timein,timeout;

    boolean st = true;

    private Detail_mobile data_mobile;
    private Fragment fragment;
    private FragmentManager fragmentManager;
    private ConnectionDetector connectionInternet;

    SharedPreferences pref;
    SharedPreferences.Editor editor;
    Profile_login profile ;
    TextView txt_name_user;
    Menu menu ;
    MenuItem menuItem ;
    BottomNavigationView bottomNavigationView;

    FragmentTransaction transaction;
    ActionBarDrawerToggle toggle;
    DrawerLayout drawer;
    NavigationView navigationView;
    Current_Location currentLocation;
    Detail_mobile dt;

    //sqlite
    SQLiteDatabase mDb;
    MyDbHelper mHelper;
    Cursor mCursor ;

    // access times
    List<String> accesstimes ;
    String link ;

    MQTT_SERVICE mqtt_service;
    //alert
    private String timeAlert ="";
    byte count= 1;
    ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        setTitle(getResources().getString(R.string.app_name));

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        connectionInternet = new ConnectionDetector(getApplicationContext());
        txt_name_user = (TextView) findViewById(R.id.name_user);

        pref = getApplicationContext().getSharedPreferences(Login_Activity.MyPREFERENCES, 0);
        editor = pref.edit();


        data_mobile = new Detail_mobile();
        Api_key = pref.getString(Login_Activity.API,null);
//        Log.d("API",Api_key);
        link = "http://128.199.196.236/api/staff/acesstime?api_token="+Api_key;
        profile = new Profile_login(pref.getString(Login_Activity.JSON_OBJ,null));

        fragmentManager = getSupportFragmentManager();
        fragment = new Fragment_CheckIn(Api_key);
        transaction = fragmentManager.openTransaction();
        transaction.add(R.id.content,fragment).commit();

        dt = new Detail_mobile();
        // init BLE
        btManager = (BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE);
        btAdapter = btManager.getAdapter();
        Devicename = btAdapter.getName();
        mac = data_mobile.getMac();
        scanHandler.post(scanRunnable);
//        setupMQTT();
        mqtt_service = new MQTT_SERVICE(MainActivity.this);

        bottomNavigationView = (BottomNavigationView)
                findViewById(R.id.navigation);
        menu = bottomNavigationView.getMenu();
        menuItem = menu.getItem(1);
        menuItem.setChecked(true);


        bottomNavigationView.setOnNavigationItemSelectedListener
                (new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem item) {
                        switch (item.getItemId()){
                            case R.id.action_dashboard:
                                fragment = new Fragment_dashboardV2();
                                navigationView.getMenu().getItem(0).setCheckable(true);
                                menuItem = navigationView.getMenu().findItem(R.id.action_dash);
                                menuItem.setCheckable(true);
                                menuItem.setChecked(true);
                                break;

                            case R.id.action_checkin:
                                fragment = new Fragment_CheckIn(Api_key);
                                navigationView.getMenu().getItem(1).setCheckable(true);
                                menuItem = navigationView.getMenu().findItem(R.id.action_check);
                                menuItem.setCheckable(true);
                                menuItem.setChecked(true);
                                break;

                            case R.id.action_history:
                                fragment = new Fragment_History(Api_key);
                                navigationView.getMenu().getItem(2).setCheckable(true);
                                menuItem = navigationView.getMenu().findItem(R.id.action_his);
                                menuItem.setCheckable(true);
                                menuItem.setChecked(true);
                                break;
                        }
                        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
                        drawer.closeDrawer(GravityCompat.START);
                        final FragmentTransaction transaction1 = fragmentManager.beginTransaction();
                        transaction1.replace(R.id.content,fragment).commit();
                        return true;
                    }
                });

    }


    private Runnable scanRunnable = new Runnable()
    {
        @Override
        public void run() {
            checkInternet();
            if (isScanning) {
                if (btAdapter != null) {
                    btAdapter.stopLeScan(leScanCallback);
                }
            } else {
                if (btAdapter != null) {
                    btAdapter.startLeScan(leScanCallback);
                }
            }
            isScanning = !isScanning;
            try{
                feedDataTimework();
                String feed = feeddataHistory(url+Api_key);
                profile = new Profile_login(feed);
                timeAlert = getTimeAlert(profile.getID());
//                Log.e("timealert",timeAlert);

                if(!TextUtils.isEmpty(timein) && !TextUtils.isEmpty(timeout)) {
                    boolean state1 = dt.getLengthTime(timein, timeout);
                    String arrayLocation = pref.getString(Login_Activity.USER_LOCATION, null);
                    long t = dt.diffTime(dt.getDateFromat("HH:mm"), timein);
                    long timeIndatabase = Integer.parseInt(timeAlert);
                    if (t == timeIndatabase && t != -1 && getStatusCheckin().equals("Check Out")) {
                        if (count < 2) {
                            alertTime();
                            count++;
                        }
                    }
                    try {
                        currentLocation = new Current_Location(arrayLocation, getGPS());
                        if (state1 == false && TextUtils.isEmpty(currentLocation.getNamelocation())) {
                            checkOut();
                        }
                    } catch (Exception e) {
                        Log.e("ERROR Main", "" + e);
                    }
                }

            }catch (Exception e){
                Log.e("Error",e.toString());
            }

            scanHandler.postDelayed(scanRunnable, scan_interval_ms);
    }
    };

    private void checkOut(){

        String time = dt.getDateFromat("dd/MM/yyyy HH:mm");
        String keytime="";
        String state_check = "";
        String idLocation = "";
//        Log.d("Auto logout","auto logout.");

            try {
                profile = new Profile_login(feeddataHistory(url+Api_key));
                History history = new History(profile.getHistory_Array());
                List<String> value = history.getValue();
                JSONObject historyJson = new JSONObject(value.get(0).toString());
                state_check = historyJson.getString("state");
                keytime = historyJson.getString("key_time");
                idLocation = historyJson.getString("location_id");
//                Toast.makeText(getApplicationContext(), ""+state_check, Toast.LENGTH_SHORT).show();
                JSONObject objs = new JSONObject();

                if(!TextUtils.isEmpty(state_check) && state_check.equals("Check In")){
                        objs.put("keyStaff",profile.getID());
                        objs.put("state","Check Out");
                        objs.put("id_location",idLocation);
                        objs.put("lat",""+gps.getLatitude());
                        objs.put("key_time",keytime);
                        objs.put("long",""+gps.getLongitude());
                        objs.put("time",time);
                        mqtt_service.publishMessage(topicCheckin,objs.toString());
                        Log.d("LOGOUTAUTO",state_check+":"+keytime);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }



    private String getGPS(){
        JSONObject json  = new JSONObject();
        gps = new GPSTracker(getApplicationContext());

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
    }

    private String getStatusCheckin(){
        String state_check ="";

        if (profile.getHistory_Array()!=null){
            History history = new History(profile.getHistory_Array());
            List<String> value = history.getValue();
            try {
                JSONObject historyJson = new JSONObject(value.get(0).toString());
                state_check =  historyJson.getString("state");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return state_check;
        }else{
            return "";
        }
    }


    private void checkInternet(){
        if (!connectionInternet.isConnected()){
            Toast.makeText(this, "Not Connect Internet.", Toast.LENGTH_SHORT).show();
        }
    }

    private void confrimLogout(){

            final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("คำเตือน");
            builder.setIcon(R.drawable.ap);
            builder.setMessage("คุณต้องการ Log Out ?");
            builder.setPositiveButton("OK.", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent goLogin = new Intent(MainActivity.this, Login_Activity.class);
                    btAdapter.stopLeScan(leScanCallback);
                    mqtt_service.setDisconnected();
                    editor.clear();
                    editor.commit();
                    startActivity(goLogin);
                    finish();
                }
            });
        builder.setNegativeButton("Cancel.", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
            builder.show();
        }


     private  void alertTime(){
         String contentString = "ใกล้ถึงเวลางานแล้ว";
         NotificationCompat.Builder builder =
                 new NotificationCompat.Builder(this)
                         .setSmallIcon(R.drawable.ap)
                         .setColor(getResources().getColor(R.color.bootstrap_brand_secondary_fill))
                         .setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE)
                         .setStyle(new NotificationCompat.BigTextStyle().bigText(contentString))
                         .setPriority(2)
                         .setContentTitle("คำเตือน")
                         .setContentText(contentString)
                         .setAutoCancel(true);

         NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
         manager.notify(0, builder.build());

     }

     private void  setTimeSpiner(){

         String[] s  = {"5 นาที","10 นาที","15 นาที","20 นาที","25 นาที","30 นาที"};
         final ArrayAdapter<String> adapter  = new ArrayAdapter<String>(MainActivity.this,
                 android.R.layout.simple_spinner_dropdown_item,s);
         final Spinner sp = new Spinner(MainActivity.this);
         sp.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                 ViewGroup.LayoutParams.WRAP_CONTENT));
         sp.setAdapter(adapter);
         AlertDialog.Builder bulider = new AlertDialog.Builder(MainActivity.this);
         bulider.setView(sp);
         bulider.setTitle("เลือกเวลาแจ้งเตือน");

         bulider.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
             @Override
             public void onClick(DialogInterface dialog, int which) {
                 dialog.dismiss();
             }
         });
         bulider.setPositiveButton("OK", new DialogInterface.OnClickListener() {
             @Override
             public void onClick(DialogInterface dialog, int which) {
//                 final JSONObject objstting = new JSONObject();
                 String str = sp.getSelectedItem().toString();
                 String[] strSub = str.split(" ");
                 mHelper = new MyDbHelper(MainActivity.this);
                 mDb = mHelper.getWritableDatabase();
                 gps  = new GPSTracker(MainActivity.this);
                 mCursor = mDb.rawQuery("SELECT *  FROM "+MyDbHelper.TABLE_NAME_ALERT_TIME +" WHERE "+MyDbHelper.COL_STAFF_ID_2 +" = '"
                         +profile.getID()+"' ORDER BY "+MyDbHelper.COL_Time +" DESC LIMIT 1;" ,null);
                 mCursor.moveToFirst();

                 if(mCursor.getCount()>0){
                     mCursor = mDb.rawQuery("UPDATE "+MyDbHelper.TABLE_NAME_ALERT_TIME
                             +" SET "+MyDbHelper.COL_Time+" = "+strSub[0]+
                             " WHERE "+MyDbHelper.COL_STAFF_ID_2+" == '"+profile.getID()+"';",null);
                     mCursor.moveToFirst();
                     Toast.makeText(MainActivity.this, "Update data successfully.", Toast.LENGTH_SHORT).show();

                 }else {
                     mCursor = mDb.rawQuery("INSERT INTO "+MyDbHelper.TABLE_NAME_ALERT_TIME+" ("+MyDbHelper.COL_STAFF_ID_2
                             +","+MyDbHelper.COL_Time+") VALUES('"+profile.getID()+"','"+strSub[0]+"')",null);
                     mCursor.moveToFirst();
                 }
             }
         });
         bulider.show();

     }

     private String getTimeAlert(String id){
         String timeAlert = "";
         mHelper = new MyDbHelper(MainActivity.this);
         mDb = mHelper.getWritableDatabase();
         mCursor = mDb.rawQuery("SELECT *  FROM "+MyDbHelper.TABLE_NAME_ALERT_TIME +" WHERE "
                 +MyDbHelper.COL_STAFF_ID_2 +" == '"+id
                 +"' ORDER BY "+MyDbHelper.COL_Time +
                 " DESC LIMIT 1;" ,null);

         mCursor.moveToFirst();
         if(mCursor.getCount()>0){
             while (!mCursor.isAfterLast()){
                 timeAlert = mCursor.getString(mCursor.getColumnIndex(MyDbHelper.COL_Time));
                 mCursor.moveToNext();
             }
             return timeAlert;
         }else {
             return "-1";
         }
     }

     private void feedDataTimework(){
         try {
             String gets =  new GetDataAPI(getApplicationContext()).execute(link).get();
//             Log.e("get",""+gets);
             if(!TextUtils.isEmpty(gets)){
                 JSONObject obj = new JSONObject(gets);
                 Accesstime accesstime = new Accesstime(obj.getString("accesstime"));
                 accesstimes = accesstime.getAccesstime();
                 int numberDay =  dt.dayNumber()-2;
                 if(accesstimes.size()>0 && numberDay < accesstimes.size() && numberDay >= 0){
                     obj = new JSONObject(accesstimes.get(numberDay));
                     timein = obj.getString("timein");
                     timeout = obj.getString("timeout");
                 }
             }else {
                 Log.e("GETS TIME","is null");
             }
         } catch (InterruptedException e) {
             Log.e("get",e.toString());
         } catch (ExecutionException e) {
             Log.e("get",e.toString());
         }catch (JSONException e) {
             Log.e("getJSON ERROR",e.toString());
         }
     }

     private String feeddataHistory(String urls){
         String feed = "";
         try {
             feed = new GetDataAPI(getApplicationContext()).execute(urls).get();
             Log.e("URL",urls);
         } catch (InterruptedException e) {
             e.printStackTrace();
             showDialog(e.toString());
         } catch (ExecutionException e) {
             e.printStackTrace();
             showDialog(e.toString());
         }
         return feed;
     }

     private void showDialog(String dialog){
         Toast.makeText(getApplicationContext(), ""+dialog, Toast.LENGTH_SHORT).show();
    }


    private BluetoothAdapter.LeScanCallback leScanCallback = new BluetoothAdapter.LeScanCallback()
    {
        @Override
        public void onLeScan(final BluetoothDevice device, final int rssi, final byte[] scanRecord)
        {
//            Toast.makeText(MainActivity.this, "Scanning", Toast.LENGTH_SHORT).show();
            obj = new JSONObject();
            final BluetoothLeDevice devicele = new BluetoothLeDevice(device,rssi,scanRecord,System.currentTimeMillis());
            String scan = null;
            String strRSSI = String.valueOf(rssi);
            String strMac = String.valueOf(device.getAddress());
            int Rssi_int = Integer.parseInt(strRSSI.substring(1));
            if(BeaconUtils.getBeaconType(devicele) == BeaconType.IBEACON ){

                if (Rssi_int < 77 && st == true){
                    try {
                        obj.put("hostname",Devicename);
                        obj.put("macAddress",mac);
                        obj.put("staff_id",profile.getID());
                        obj.put("id_Location",pref.getString("idLocation",null));
                        obj.put("rssi",""+strRSSI.substring(1));
                        obj.put("mac_beacon_mobile",""+strMac.toLowerCase());
                        gps = new GPSTracker(MainActivity.this);
                        if(!gps.canGetLocation()){
                            gps.showSettingsAlert();
                        }else{
                            obj.put("latitude",gps.getLatitude());
                            obj.put("longitude",gps.getLongitude());
                        }
                        mqtt_service.publishMessage(topicMobile_becon, String.valueOf(obj));
                    } catch (JSONException e) {
                        Log.e("Error send JSON",e.toString());
                    }catch (Exception E){
                        Log.e("Error send becon",E.toString());
                    }
                }
            }
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main2, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            editor.clear();
            editor.commit();
            finish();
            return true;

        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.action_dash) {

            fragment = new Fragment_dashboardV2();
            menu = bottomNavigationView.getMenu();
            menuItem = menu.getItem(0);
            menuItem.setChecked(true);

        } else if (id == R.id.action_check) {

            fragment = new Fragment_CheckIn(Api_key);
            menu = bottomNavigationView.getMenu();
            menuItem = menu.getItem(1);
            menuItem.setChecked(true);

        } else if (id == R.id.action_his) {

            fragment = new Fragment_History(Api_key);

            menu = bottomNavigationView.getMenu();
            menuItem = menu.getItem(2);
            menuItem.setChecked(true);
        } else if (id == R.id.action_logout) {
            confrimLogout();

        }else if (id == R.id.action_profile) {
            fragment = new Fragment_Profile();

        }else if(id == R.id.action_settime){
//            setTimeAlert();
            setTimeSpiner();
        }

        final FragmentTransaction transaction1 = fragmentManager.beginTransaction();
        transaction1.replace(R.id.content,fragment).commit();

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    @Override
    protected void onResume() {
        super.onResume();

        String provider = Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
        if(!provider.contains("gps")){ //if gps is disabled
            final Intent poke = new Intent();
            poke.setClassName("com.android.settings", "com.android.settings.widget.SettingsAppWidgetProvider");
            poke.addCategory(Intent.CATEGORY_ALTERNATIVE);
            poke.setData(Uri.parse("3"));
            sendBroadcast(poke);
        }

        feedDataTimework();
//        profile = new Profile_login(feeddataHistory(url));
    }

    @Override
    protected void onStop() {
        count = 1;
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        scanHandler.removeCallbacksAndMessages(scanRunnable);
        super.onDestroy();

    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder dialog2 = new AlertDialog.Builder(MainActivity.this);
        dialog2.setTitle("Exit");
        dialog2.setIcon(R.drawable.logoapps2);
        dialog2.setCancelable(true);
        dialog2.setMessage("Do you want to exit?");
        dialog2.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                scanHandler.removeCallbacksAndMessages(null);
                btAdapter.stopLeScan(leScanCallback);
                mqtt_service.setDisconnected();
                editor.clear();
                editor.commit();

                finish();
                dialog.dismiss();
            }
        });

        dialog2.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dialog2.show();
    }
}

