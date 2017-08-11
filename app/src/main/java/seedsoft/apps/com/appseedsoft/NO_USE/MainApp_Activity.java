package seedsoft.apps.com.appseedsoft.NO_USE;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

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

import seedsoft.apps.com.appseedsoft.Check_internet.ConnectionDetector;
import seedsoft.apps.com.appseedsoft.Fragment.Fragment_CheckIn;
import seedsoft.apps.com.appseedsoft.Fragment.Fragment_Dashbord;
import seedsoft.apps.com.appseedsoft.Fragment.Fragment_History;
import seedsoft.apps.com.appseedsoft.Detail_mobile.Detail_mobile;
import seedsoft.apps.com.appseedsoft.GPSTracker.GPSTracker;
import seedsoft.apps.com.appseedsoft.Location.Current_Location;
import seedsoft.apps.com.appseedsoft.Login_Activity;

import seedsoft.apps.com.appseedsoft.Profile_login.Profile_login;
import seedsoft.apps.com.appseedsoft.R;
import uk.co.alt236.bluetoothlelib.device.BluetoothLeDevice;
import uk.co.alt236.bluetoothlelib.device.beacon.BeaconType;
import uk.co.alt236.bluetoothlelib.device.beacon.BeaconUtils;
import uk.co.alt236.bluetoothlelib.device.beacon.ibeacon.IBeaconDevice;

public class MainApp_Activity  extends AppCompatActivity {
    JSONObject obj;
    Bundle bundle;
    private String passwordLogin;
    private static final String LOG_TAG = "MainActivity1";
    private ProgressDialog progressDialog_connect;
    private  int n = 0 ;
    private BluetoothManager btManager;
    private BluetoothAdapter btAdapter;
    private Handler scanHandler = new Handler();
    private int scan_interval_ms = 10000;
    private boolean isScanning = false;
    private GPSTracker gps;

    //=====================mqtt
    public static final String TAG = "MQTT_NAT";
    public String subscribeTopic = "/device/door/id001/status/";
    public String broker       = "tcp://188.166.188.78:1883";
    public String clientId     = MqttClient.generateClientId();
    public String username     = "seedsoft";
    public String password     = "seedsoft";
    public String topicMobile  = "/device/mobile/";
    public String topicMobile_becon = "/device/mobile_beacon/";
    public String Devicename,mac,ipaddr ;
    public String Api_key,strJson;
    boolean st = true;
    private String deviceID = "001";
    private TextView tv;
    MqttAndroidClient mqttAndroidClient;
    private Detail_mobile data_mobile;
    private Fragment fragment;
    private FragmentManager fragmentManager;
    private ConnectionDetector connectionInternet;
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    Profile_login profile ;
    Detail_mobile dt;
    Current_Location currentLocation;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_main_app);
        setTitle(getResources().getString(R.string.app_name));

        connectionInternet = new ConnectionDetector(getApplicationContext());
        pref = getApplicationContext().getSharedPreferences(Login_Activity.MyPREFERENCES, 0);
        editor = pref.edit();
        profile = new Profile_login(pref.getString("Obj_json",null));
        data_mobile = new Detail_mobile();
        bundle = getIntent().getExtras();
        dt = new Detail_mobile();
        Api_key = pref.getString("api_key",null);

        fragmentManager = getSupportFragmentManager();
        fragment = new Fragment_CheckIn(Api_key);

        // init BLE
        btManager = (BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE);
        btAdapter = btManager.getAdapter();
        Devicename = btAdapter.getName();
        mac = data_mobile.getMac();
        scanHandler.post(scanRunnable);


        setupMQTT();
        final FragmentTransaction transaction = fragmentManager.openTransaction();
        transaction.add(R.id.content,fragment).commit();
        BottomNavigationView bottomNavigationView = (BottomNavigationView)
                findViewById(R.id.navigation);
        Menu menu = bottomNavigationView.getMenu();
        MenuItem menuItem = menu.getItem(1);
        menuItem.setChecked(true);
        bottomNavigationView.setOnNavigationItemSelectedListener
                (new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                switch (item.getItemId()){
                    case R.id.action_dashboard:
                        fragment = new Fragment_Dashbord();
                        break;
                    case R.id.action_checkin:
                        fragment = new Fragment_CheckIn(Api_key);
                        break;
                    case R.id.action_history:
                        fragment = new Fragment_History(Api_key);
                        break;
                }
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
            scanHandler.postDelayed(scanRunnable, scan_interval_ms);
        }
    };

    private BluetoothAdapter.LeScanCallback leScanCallback = new BluetoothAdapter.LeScanCallback()
    {
        @Override
        public void onLeScan(final BluetoothDevice device, final int rssi, final byte[] scanRecord)
        {
            obj = new JSONObject();
            final BluetoothLeDevice devicele = new BluetoothLeDevice(device,rssi,scanRecord,System.currentTimeMillis());
            String scan = null;
            String strRSSI = String.valueOf(rssi);
            String strMac = String.valueOf(device.getAddress());
            int Rssi_int = Integer.parseInt(strRSSI.substring(1));
            if(BeaconUtils.getBeaconType(devicele) == BeaconType.IBEACON ){
                final IBeaconDevice iBeaconDevice = new IBeaconDevice(devicele);
                int TxPower = iBeaconDevice.getCalibratedTxPower();
                try {
                    obj.put("mobile_id",deviceID);
                    obj.put("hostname",Devicename);
                    obj.put("macAddress",mac);
                    obj.put("staff_id",profile.getID());
                    obj.put("device_id",deviceID);
                    obj.put("id_Location",pref.getString("idLocation",null));
                    obj.put("rssi",""+strRSSI.substring(1));
                    obj.put("mac_beacon_mobile",""+strMac.toLowerCase());
                    gps = new GPSTracker(MainApp_Activity.this);
                    if(!gps.canGetLocation()){
                        gps.showSettingsAlert();
                    }else{
                        obj.put("latitude",gps.getLatitude());
                        obj.put("longitude",gps.getLongitude());
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (Rssi_int < 77 && st == true){
                    try {
                        publishMessage(topicMobile_becon, String.valueOf(obj));
                    }catch (Exception E){
                        Log.e("Error send becon",E.toString());
                    }
                }
            }

        }
    };

    public void setupMQTT(){
        mqttAndroidClient = new MqttAndroidClient(getApplicationContext(),broker,clientId);
        mqttAndroidClient.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {
                if (reconnect){
                    addToHistory(serverURI);
                    subscribeToTopic();
                }else {
                    addToHistory("Connected to "+serverURI);
                    gps = new GPSTracker(MainApp_Activity.this);
                    obj = new JSONObject();
                    try {
                        obj.put("Mobile_beacon",deviceID);
                        if (!gps.canGetLocation()){
                            gps.showSettingsAlert();
                        }
                        obj.put("staff_id",profile.getID());
                        obj.put("hostname",Devicename);
                        obj.put("macAddress",mac);
                        obj.put("Latitude ",gps.getLatitude());
                        obj.put("Longitude",gps.getLongitude());
                        obj.put("CerentTime",data_mobile.getTime());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    publishMessage("/device/mobile/con/",obj.toString());
                }
            }
            @Override
            public void connectionLost(Throwable cause) {
                addToHistory("The Connection was lost.");
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                String msg = new String(message.getPayload());
                addToHistory("Incoming message: " + new String(message.getPayload()));
                setState(msg);

            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });

        //setup mqtt
        MqttConnectOptions option = new MqttConnectOptions();
        option.setUserName(username);
        option.setPassword(password.toCharArray());
        option.setAutomaticReconnect(true);
        option.setCleanSession(false);

        try{
            mqttAndroidClient.connect(option, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    DisconnectedBufferOptions disconnectBufferOption = new DisconnectedBufferOptions();
                    disconnectBufferOption.setBufferEnabled(true);
                    disconnectBufferOption.setBufferSize(100);
                    disconnectBufferOption.setPersistBuffer(false);
                    disconnectBufferOption.setDeleteOldestMessages(false);
                    mqttAndroidClient.setBufferOpts(disconnectBufferOption);
                    subscribeToTopic();
                }
                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    addToHistory("Failed to connect to "+ broker);
                }
            });

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void addToHistory(String mainText){
        Log.d(TAG,mainText);
    }

    public void subscribeToTopic(){
        try {
            mqttAndroidClient.subscribe(subscribeTopic, 0, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    addToHistory("Subscribed!");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    addToHistory("Failed to subscribe");
                }
            });
            mqttAndroidClient.subscribe(subscribeTopic, 0, new IMqttMessageListener() {
                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    // message Arrived!
                    String msg = new String(message.getPayload());
                    setState(msg);
                    System.out.println("Message12: " + topic + " : " +msg);
                }
            });

        } catch (MqttException ex){
            System.err.println("Exception whilst subscribing");
            ex.printStackTrace();
        }
    }

    public void publishMessage(String topic,String state){
        try {
            MqttMessage message = new MqttMessage();
            message.setPayload(state.getBytes());
            mqttAndroidClient.publish(topic, message);
            addToHistory("Message Published");
            if(!mqttAndroidClient.isConnected()){
                addToHistory(mqttAndroidClient.getBufferedMessageCount() + " messages in buffer.");
            }
        } catch (MqttException e) {
            System.err.println("Error Publishing: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void setState(String in){
        int stated = new Integer(in);
        if(stated==1){
            Toast.makeText(MainApp_Activity.this, "Door open", Toast.LENGTH_SHORT).show();
            st = false;
        }else {
            st = true;
            Toast.makeText(MainApp_Activity.this, "Door Close", Toast.LENGTH_SHORT).show();
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


    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        AlertDialog.Builder dialog2 = new AlertDialog.Builder(MainApp_Activity.this);
        dialog2.setTitle("Exit");
        dialog2.setIcon(R.drawable.logoapps2);
        dialog2.setCancelable(true);
        dialog2.setMessage("Do you want to exit?");
        dialog2.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
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


    private void checkInternet(){
        if (!connectionInternet.isConnected()){
            Toast.makeText(this, "Not Connect Internet.", Toast.LENGTH_SHORT).show();
            final AlertDialog.Builder builder = new AlertDialog.Builder(MainApp_Activity.this);
            final AlertDialog OptionDialog = builder.create();
            builder.setMessage("Not Connect Internet.");
            builder.setPositiveButton("Close Apps", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    OptionDialog.dismiss();
                    scanHandler.removeCallbacks(scanRunnable);
                    finish();
                }
            });
            builder.show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkInternet();
        fragment =new  Fragment_CheckIn(Api_key);

//        Toast.makeText(getApplicationContext(), ""+url+api, Toast.LENGTH_SHORT).show();

    }

}
