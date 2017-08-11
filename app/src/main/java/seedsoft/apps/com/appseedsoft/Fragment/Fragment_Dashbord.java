package seedsoft.apps.com.appseedsoft.Fragment;


import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import seedsoft.apps.com.appseedsoft.GPSTracker.GPSTracker;
import seedsoft.apps.com.appseedsoft.Location.Current_Location;
import seedsoft.apps.com.appseedsoft.Location.GetDataLocation;
import seedsoft.apps.com.appseedsoft.Login_Activity;
import seedsoft.apps.com.appseedsoft.MQTT_SERVICE;
import seedsoft.apps.com.appseedsoft.MainActivity;
import seedsoft.apps.com.appseedsoft.Profile_login.Profile_login;
import seedsoft.apps.com.appseedsoft.R;
import seedsoft.apps.com.appseedsoft.Detail_mobile.Detail_mobile;


public class Fragment_Dashbord extends Fragment{
    private JSONObject obj;


    private GPSTracker gps;
    //=====================mqtt================
    private static final String TAG = MainActivity.TAG;
    private String subscribeTopic = "/device/door/";
    private String topicMobile  = MainActivity.topicMobile;

    //mqtt

    boolean st = true;
    public TextView txtstate_connect,state_show;
    private MqttAndroidClient mqttAndroidClient;
    private BluetoothAdapter adapter;
    private BluetoothManager manager;
    private Detail_mobile data_mobile;
    private String nameDevice,mac;
    private Current_Location location ;
    private LinearLayout setBackground_state;
    private ImageView img_state;
    private Button btnOpen;

    //session
    SharedPreferences pref;
    SharedPreferences.Editor editor;

    // data android
    Profile_login profile ;
    GetDataLocation getDataLocationAll;
    Map<String,Object> datalocation = new HashMap<String,Object>();
    List<String> idLocation = new ArrayList<String>();
    List<String> nameLocation = new ArrayList<String>();
    ArrayAdapter<String> mAdapter;
    Spinner spiner;
    private Handler scanHandler = new Handler();
    String msgStatus,idDoor;
    public Fragment_Dashbord(){

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_dashbord,container,false);

        return v;
    }

    @Override
    public void onViewCreated(View view,Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        txtstate_connect = (TextView) view.findViewById(R.id.txt_state_connect);
        btnOpen = (Button) view.findViewById(R.id.btn_open_door);
        manager = (BluetoothManager)getActivity().getSystemService(Context.BLUETOOTH_SERVICE);
        adapter = manager.getAdapter();

        pref = getContext().getSharedPreferences(Login_Activity.MyPREFERENCES, 0);
        editor = pref.edit();
        profile = new Profile_login(pref.getString(Login_Activity.JSON_OBJ,null));

        data_mobile = new Detail_mobile();
        setBackground_state = (LinearLayout) view.findViewById(R.id.background_state);
        img_state = (ImageView) view.findViewById(R.id.img_state);
        state_show = (TextView)  view.findViewById(R.id.txt_state_door);

        setBackground_state.setBackgroundColor(getResources().getColor(R.color.bootstrap_gray_light));
        img_state.setImageDrawable(getResources().getDrawable(R.drawable.doorclose));


        state_show.setText("Close");
        // data mobile
        data_mobile = new Detail_mobile();
        nameDevice = adapter.getName();
        mac = data_mobile.getMac();

        // Location
        location = new Current_Location(profile.getLocations_Array(),getGPS());
        datalocation = location.getLocationAll();
        getDataLocationAll  = new GetDataLocation(datalocation);
        idLocation = getDataLocationAll.getIDLocation();
        nameLocation = getDataLocationAll.getNameLocation();
        spiner = (Spinner) view.findViewById(R.id.sp_name);

        mAdapter = new ArrayAdapter<String>(getContext(),
                android.R.layout.simple_expandable_list_item_1,nameLocation){
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView tv = (TextView) super.getView(position,convertView,parent);
                tv.setGravity(Gravity.CENTER);
                tv.setTextColor(Color.WHITE);
                return tv;
            }
        };

        mAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        int setPosition  = mAdapter.getPosition(""+location.getNamelocation());
        spiner.setAdapter(mAdapter);
        spiner.setSelection(setPosition);


        btnOpen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                obj = new JSONObject();
                try {
                    gps = new GPSTracker(getActivity().getApplicationContext());
                    if(!gps.canGetLocation()){
                        gps.showSettingsAlert();
                    }else{
                        obj.put("latitude",gps.getLatitude());
                        obj.put("longitude",gps.getLongitude());
                    }
                    obj.put("id_Location",idLocation.get(spiner.getSelectedItemPosition()));
                    obj.put("staff_id",profile.getID());
                    obj.put("macAddress",data_mobile.getMac());
                    obj.put("time",data_mobile.getTime());

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                publishMessage(topicMobile,obj.toString());
            }
        });
    }


    @Override
    public void onResume() {
        super.onResume();
        scanHandler.post(autoreconnect);
        String msg = pref.getString("msg",null);
        mqttSetup();
        if(!TextUtils.isEmpty(msg)){
            Toast.makeText(getContext(), ""+msg, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onStop() {
        super.onStop();
        scanHandler.removeCallbacksAndMessages(autoreconnect);
    }
    public void setState(String id,String msg){
        int stated = new Integer(msg);
        String idlo = idLocation.get(spiner.getSelectedItemPosition());

        if(idlo.equals(id)){
            if(stated==1){
                st = false;
                setBackground_state.setBackgroundColor(getResources().getColor(R.color.background_open));
                img_state.setImageDrawable(getResources().getDrawable(R.drawable.dooropen));
                state_show.setText("Open");
            }else {
                st = true;
                setBackground_state.setBackgroundColor(getResources().getColor(R.color.bootstrap_gray_light));
                img_state.setImageDrawable(getResources().getDrawable(R.drawable.doorclose));
                state_show.setText("Close");
            }
        }
    }

    private String getGPS(){
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
    }


    private Runnable autoreconnect  = new Runnable() {
        @Override
        public void run() {
            Toast.makeText(getContext(), ""+idDoor+msgStatus, Toast.LENGTH_SHORT).show();

            if (!TextUtils.isEmpty(msgStatus) && !TextUtils.isEmpty(idDoor)){
                try{
                    setState(idDoor,msgStatus);
                }catch (Exception e){
                    Log.e("ERROR Set background",e.toString());
                }

                msgStatus ="";
                idDoor = "";
            }
            scanHandler.postDelayed(autoreconnect, Login_Activity.TIME_INTERVAL);
        }
    };


    public void mqttSetup(){
            Context context = getContext();
            gps = new GPSTracker(getContext());
            mqttAndroidClient = new MqttAndroidClient(context,MQTT_SERVICE.broker,MQTT_SERVICE.clientId);
            mqttAndroidClient.setCallback(new MqttCallbackExtended() {
                @Override
                public void connectComplete(boolean reconnect, String serverURI) {
                    if (reconnect){
                        addToHistory(serverURI);
                        subscribeToTopic();
                    }else {
                        addToHistory("Connected to "+serverURI);
                    }
                }
                @Override
                public void connectionLost(Throwable cause) {
                    addToHistory("The Connection was lost.");
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    addToHistory("Incoming message: " + new String(message.getPayload()));
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                }
            });

            //setup mqtt
            MqttConnectOptions option = new MqttConnectOptions();
            option.setUserName(MQTT_SERVICE.username);
            option.setPassword(MQTT_SERVICE.password.toCharArray());
            option.setAutomaticReconnect(true);
            option.setCleanSession(false);

            try{
                mqttAndroidClient.connect(option, null, new IMqttActionListener() {
                    @Override
                    public void onSuccess(IMqttToken asyncActionToken){
                        DisconnectedBufferOptions disconnectBufferOption = new DisconnectedBufferOptions();
                        disconnectBufferOption.setBufferEnabled(true);
                        disconnectBufferOption.setBufferSize(100);
                        disconnectBufferOption.setPersistBuffer(false);
                        disconnectBufferOption.setDeleteOldestMessages(false);
                        try {
                            mqttAndroidClient.setBufferOpts(disconnectBufferOption);
                        }catch (Exception e){
                            e.getStackTrace();
                        }

                        subscribeToTopic();
                    }
                    @Override
                    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                        addToHistory("Failed to connect to "+ MQTT_SERVICE.broker);
                    }
                });

            }catch (Exception e){
                e.printStackTrace();
            }
    }

    private void addToHistory(String mainText){
        Log.d(MQTT_SERVICE.TAGMQTT,mainText);
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
                public void messageArrived(String topic, MqttMessage message)  {
                    // message Arrived!
                    String msg = new String(message.getPayload());
                    JSONObject obj = null;
                    try {
                        obj = new JSONObject(msg);
                        msgStatus = obj.getString("status");
                        idDoor = obj.getString("id");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
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

}
