package seedsoft.apps.com.appseedsoft.NO_USE;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v7.app.AlertDialog;
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
import android.content.DialogInterface.OnClickListener;

import seedsoft.apps.com.appseedsoft.GPSTracker.GPSTracker;
import seedsoft.apps.com.appseedsoft.R;
import uk.co.alt236.bluetoothlelib.device.BluetoothLeDevice;
import uk.co.alt236.bluetoothlelib.device.beacon.BeaconType;
import uk.co.alt236.bluetoothlelib.device.beacon.BeaconUtils;
import uk.co.alt236.bluetoothlelib.device.beacon.ibeacon.IBeaconDevice;

public class MainActivity1 extends AppCompatActivity {
    JSONObject obj;
    private static final String LOG_TAG = "MainActivity1";
    private ProgressDialog progressDialog_connect;
    private  int n = 0 ;
    private BluetoothManager btManager;
    private BluetoothAdapter btAdapter;
    private Handler scanHandler = new Handler();
    private int scan_interval_ms = 10000;
    private boolean isScanning = false;
    TextView status;
    Button btnOpen;
    GPSTracker gps;

    //=====================mqtt
    private static final String TAG = "MQTT_NAT";
    private String subscribeTopic = "/device/door/id001/status/";
    private String content      = "Hello CloudMQTT";
    private int qos             = 1;
//    private String broker       = "tcp://m10.cloudmqtt.com:17923";
//    private String clientId     = MqttClient.generateClientId();
//    private String username     = "jhrhggnz";
//    private String password     = "Qm1oj5Uj7soQ";    6,20

    private String broker       = "tcp://188.166.188.78:1883";
    private String clientId     = MqttClient.generateClientId();
    private String username     = "seedsoft";
    private String password     = "seedsoft";
    private String topicMobile  = "/device/mobile/";
    private String topicMobile_becon = "/device/mobile_beacon/";
    boolean st = true;
    private String deviceID = "001";
    private TextView tv;
    MqttAndroidClient mqttAndroidClient;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("Seed Soft");
        setTitleColor(getResources().getColor(R.color.bootstrap_brand_info));

        progressDialog_connect = new ProgressDialog(this);
        openConnect(n);
        tv = (TextView) findViewById(R.id.txt_state);
        btnOpen = (Button) findViewById(R.id.btn_Open);
        status = (TextView) findViewById(R.id.state);
        status.setEnabled(false);
        tv.setText("Wellcome.");
        setupMQTT();
        // init BLE
        btManager = (BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE);
        btAdapter = btManager.getAdapter();
        scanHandler.post(scanRunnable);
        btnOpen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                obj = new JSONObject();
                try {
                    gps = new GPSTracker(MainActivity1.this);
                    if(!gps.canGetLocation()){
                        gps.showSettingsAlert();
                    }else{
                        obj.put("latitude",gps.getLatitude());
                        obj.put("longitude",gps.getLongitude());
                    }
                    obj.put("mobile_id","001");
                    obj.put("device_id","001");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                publishMessage(topicMobile,obj.toString());
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {

        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();

        if (id == R.id.action_exit)
        {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private Runnable scanRunnable = new Runnable()
    {
        @Override
        public void run() {

            if (isScanning) {
                if (btAdapter != null) {
                    tv.setText("Stop Scan.");
                    btAdapter.stopLeScan(leScanCallback);
                }
            } else {
                if (btAdapter != null) {
                    tv.setText("Scannig beacon.");
                    btAdapter.startLeScan(leScanCallback);
                }
            }
            isScanning = !isScanning;
            scanHandler.postDelayed(this, scan_interval_ms);

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
                    obj.put("device_id","001");
                    obj.put("rssi",""+strRSSI.substring(1));
                    obj.put("mac_beacon_mobile",""+strMac.toLowerCase());
                    gps = new GPSTracker(MainActivity1.this);
                    if(!gps.canGetLocation()){
                        gps.showSettingsAlert();
                    }else{
                        obj.put("latitude",gps.getLatitude());
                        obj.put("longitude",gps.getLongitude());
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
//                scan = iBeaconDevice.getName()+ "  TXPOWER = "+TxPower+" RSSI = "+rssi +
//                        "Distance = "+calculateDistance(TxPower,rssi)+"\n";
                if (Rssi_int < 80 && TxPower == -59 && st == true){
                    publishMessage(topicMobile_becon, String.valueOf(obj));
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
                    openConnect(1);
                    addToHistory(serverURI);
                    subscribeToTopic();
                }else {
                    addToHistory("Connected to "+serverURI);
                    Toast.makeText(MainActivity1.this,"Connected to "+serverURI.substring(6,20), Toast.LENGTH_SHORT).show();
                    openConnect(n+=1);
                    gps = new GPSTracker(MainActivity1.this);
                    if (!gps.canGetLocation()){
                        Intent in = new Intent(Settings.ACTION_LOCALE_SETTINGS);
                        startActivity(in);
                    }
                    publishMessage("/device/mobile/con/"
                            ,"connect from mobile id "+deviceID+"  Location is "+gps.getLatitude()
                            +","+gps.getLongitude());
                    status.setText("Close");
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
//                Toast.makeText(MainActivity1.this, "Yo!!!!!!!!", Toast.LENGTH_SHORT).show();
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

            // THIS DOES NOT WORK!
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
//        Toast.makeText(this, ""+in, Toast.LENGTH_SHORT).show();
        if(stated==1){
            Toast.makeText(MainActivity1.this, "Door open", Toast.LENGTH_SHORT).show();
            st = false;
            status.setText("Open");
        }else {
            st = true;
            Toast.makeText(MainActivity1.this, "Door Close", Toast.LENGTH_SHORT).show();
            status.setText("Close");
        }

    }
    private void openConnect(int state){
        if(state == 0){
            progressDialog_connect.setTitle("Connect mqtt");
//            progressDialog_connect.setIcon(R.drawable.imslogo11);
            progressDialog_connect.setMessage("");
            progressDialog_connect.show();
        }else if(state ==1){
            progressDialog_connect.hide();
        }else {
            n = 3;
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        if(!btAdapter.isEnabled()){
            Intent enable = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enable,1);
        }
        if(!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)){
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        AlertDialog.Builder dialog2 = new AlertDialog.Builder(MainActivity1.this);
        dialog2.setTitle("Exit");
//        dialog2.setIcon(R.drawable.imslogo11);
        dialog2.setCancelable(true);
        dialog2.setMessage("Do you want to exit?");
        dialog2.setPositiveButton("Yes", new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                finish();
                dialog.dismiss();
            }
        });
        dialog2.setNegativeButton("No", new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dialog2.show();
    }
}
