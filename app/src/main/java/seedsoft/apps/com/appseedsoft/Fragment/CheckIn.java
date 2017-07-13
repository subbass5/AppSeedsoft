package seedsoft.apps.com.appseedsoft.Fragment;

import android.content.res.ColorStateList;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.ColorRes;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import seedsoft.apps.com.appseedsoft.Detail_mobile.Detail_mobile;
import seedsoft.apps.com.appseedsoft.Detail_mobile.MyDbHelper;
import seedsoft.apps.com.appseedsoft.GPSTracker;
import seedsoft.apps.com.appseedsoft.Location.Current_Location;
import seedsoft.apps.com.appseedsoft.Profile_login.Profile_login;
import seedsoft.apps.com.appseedsoft.R;

public class CheckIn  extends Fragment{
    private final String objLocation;
    private final String Password;
    String time;
    JSONObject jsonObject;
    JSONObject sendCheckin;
    GPSTracker gps;
    TextView tv1,tv_time,tv_date,tv_location,tv_state_now;
    Detail_mobile dt;

    //=====================mqtt================
    private static final String TAG = "MQTT_NAT";
    private String subscribeTopic = "/state/checkin/";
    private String broker       = "tcp://188.166.188.78:1883";
    private String clientId     = MqttClient.generateClientId();
    private String username_mqtt = "seedsoft";
    private String password     = "seedsoft";
    private String topicCheckin = "/auth/mobile/";
    private String mobileId = "001";
    boolean st = true;
    private String deviceID = "001";
    private String staff_id,state_check,time_check;
    SQLiteDatabase mDb;
    MyDbHelper myDbHelper;
    Cursor mCursor;
    MqttAndroidClient mqttAndroidClient;
    Button btnCheck;
    EditText getPassword;
    Profile_login profile;
    Current_Location currentLocation;
    
    public CheckIn(String objLocation,String Password){
        this.objLocation = objLocation;
        this.Password = Password;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_checkin,container,false);
        ImageView img_prifile = (ImageView) v.findViewById(R.id.img_profile_checkin);
        getPassword = (EditText) v.findViewById(R.id.et_getPassword);
        btnCheck = (Button) v.findViewById(R.id.btn_checkin);

        //new object data
        profile = new Profile_login(objLocation);
        dt = new Detail_mobile();
        tv1 = (TextView) v.findViewById(R.id.txt_name_user);
        tv_time = (TextView) v.findViewById(R.id.txt_time);
        tv_date = (TextView) v.findViewById(R.id.txt_date);
        tv_location = (TextView) v.findViewById(R.id.txt_local);
        tv_state_now  = (TextView) v.findViewById(R.id.txt_state_now);
        tv_state_now.setText("Wellcome to Apps ");
        tv_state_now.setTextColor(getActivity().getResources().getColor(R.color.bootstrap_brand_primary));

        currentLocation = new Current_Location(profile.getLocations(),getGPS());
        Toast.makeText(getActivity().getApplicationContext(), "Name:"+currentLocation.getNamelocation()
                +"Lat:"+currentLocation.getLatitude()
                +"Longi:"+currentLocation.getLongitude()
                +"Distance:"+currentLocation.getDistance(), Toast.LENGTH_SHORT).show();


        myDbHelper = new MyDbHelper(getActivity().getApplicationContext());
        mDb = myDbHelper.getWritableDatabase();
        mCursor = mDb.rawQuery("SELECT * FROM "+MyDbHelper.TABLE_NAME +" WHERE "+MyDbHelper.COL_STAFF_ID +"=='"+profile.getID()+"'", null);
        mCursor.moveToFirst();

        try {
            while ( !mCursor.isAfterLast() ){
                state_check = mCursor.getString(mCursor.getColumnIndex(MyDbHelper.COL_STATE));
                staff_id = mCursor.getString(mCursor.getColumnIndex(MyDbHelper.COL_STAFF_ID));
                time_check = mCursor.getString(mCursor.getColumnIndex(MyDbHelper.COL_TIME_NOW));
                Log.e("Database",""+mCursor.getCount()+"time = "+ mCursor.getString(mCursor.getColumnIndex(MyDbHelper.COL_TIME_NOW))
                        +"State = "+mCursor.getString(mCursor.getColumnIndex(MyDbHelper.COL_STATE)));
                mCursor.moveToNext();
            }

        }catch(final Exception lException) {
            Log.e("Getdata", "Exception looping Cursor: " + lException.getMessage());
        }


      if(mCursor.getCount()>0){
          if (state_check.equals(getActivity().getResources().getString(R.string.btn_checkin))){
              btnCheck.setText("Check Out");
              tv_state_now.setText("State : "+state_check+"  Time :"+time_check);
              btnCheck.setBackgroundTintList(ColorStateList.valueOf(getActivity().getResources().getColor(R.color.bootstrap_brand_danger)));
              tv_state_now.setTextColor(getActivity().getResources().getColor(R.color.bootstrap_brand_warning));


          }else{
              Toast.makeText(getActivity().getApplicationContext(), "No data in sqlite.", Toast.LENGTH_SHORT).show();
          }
      }
        mCursor.close();
        setupMQTT();
        tv_date.setText(""+dt.getDateFromat("dd/MM/yyyy"));
        tv_time.setText(""+dt.getDateFromat("HH:mm"));

        tv1.setText(""+profile.getNameStaff());
//        Toast.makeText(getActivity().getApplicationContext(), ""+password, Toast.LENGTH_SHORT).show();
        Picasso.with(getActivity().getApplicationContext())
                .load(profile.getPathProfile())
                .resize(150, 150)
                .centerCrop()
                .into(img_prifile);



        try {
            JSONArray js = new JSONArray(profile.getLocations());
            jsonObject = new JSONObject(js.get(1).toString());
            tv_location.setText(jsonObject.getString("name"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        btnCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String getpass = getPassword.getText().toString();
                if (Password.equals(""+getpass)){
                    String stete_now = getState();
                    time = dt.getDateFromat("dd-MM-yyyy HH:mm");
                    sendCheckin = new JSONObject();
                    try {
                        JSONArray js = new JSONArray(profile.getLocations());
                        jsonObject = new JSONObject(js.get(1).toString());
                        tv_location.setText(jsonObject.getString("name"));
                        sendCheckin.put("keyStaff",profile.getID());
                        sendCheckin.put("state",""+stete_now);
                        sendCheckin.put("id_location",jsonObject.getString("id"));
                        sendCheckin.put("lat",jsonObject.getString("lat"));
                        sendCheckin.put("long",jsonObject.getString("long"));
                        sendCheckin.put("time",time);
//                        Toast.makeText(getActivity().getApplicationContext(), ""+ js.get(0), Toast.LENGTH_SHORT).show();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    tv_state_now.setText("State : "+stete_now+"  Time :"+time);
                    tv_state_now.setTextColor(getActivity().getResources().getColor(R.color.bootstrap_brand_warning));
                    publishMessage(topicCheckin,sendCheckin.toString());
                    getPassword.setText("");
                }else {
                    Toast.makeText(getActivity().getApplicationContext(), "Check your password.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        return v;
    }

    private String getState(){
        String result= "";
        result = btnCheck.getText().toString();
        if (result.equals("Check In")){
            mCursor = mDb.rawQuery("INSERT INTO "+MyDbHelper.TABLE_NAME +"("+MyDbHelper.COL_STAFF_ID
                    +","+MyDbHelper.COL_TIME_NOW+","+MyDbHelper.COL_STATE+") VALUES('"+profile.getID()
                    +"','"+dt.getDateFromat("dd/MM/yyyy HH:mm")
                    +"','"+result+"')", null);
            btnCheck.setText("Check Out");
            btnCheck.setBackgroundTintList(ColorStateList.valueOf(getActivity().getResources().getColor(R.color.bootstrap_brand_danger)));
            tv_state_now.setTextColor(getActivity().getResources().getColor(R.color.bootstrap_brand_success));
            mCursor.moveToFirst();
            Toast.makeText(getActivity().getApplicationContext(), "Check In successfully.", Toast.LENGTH_SHORT).show();
            return result;
        }else {
            mCursor = mDb.rawQuery("DELETE FROM "+MyDbHelper.TABLE_NAME+" WHERE "+MyDbHelper.COL_STAFF_ID +" == '"+profile.getID()+"'",null);
            mCursor.moveToFirst();
            btnCheck.setText("Check In");
            btnCheck.setBackgroundTintList(ColorStateList.valueOf(getActivity().getResources().getColor(R.color.bootstrap_brand_info)));
            tv_state_now.setTextColor(getActivity().getResources().getColor(R.color.bootstrap_brand_warning));
            Toast.makeText(getActivity().getApplicationContext(), "Check Out successfully.", Toast.LENGTH_SHORT).show();
            return "Check Out";
        }
    }

    public void setupMQTT(){
        mqttAndroidClient = new MqttAndroidClient(getContext(),broker,clientId);
        mqttAndroidClient.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {
                if (reconnect){
                    addToHistory(serverURI);
                    subscribeToTopic();

                }else {
                    addToHistory("Connected to "+serverURI);
                    Toast.makeText(getActivity().getApplicationContext(),"Connected to "
                            +serverURI.substring(6,20), Toast.LENGTH_SHORT).show();
                    subscribeToTopic();
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
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
            }
        });

        //setup mqtt
        MqttConnectOptions option = new MqttConnectOptions();
        option.setUserName(username_mqtt);
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
                    System.out.println("Message: " + topic + " : " +msg);
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



    private String getGPS(){

        JSONObject json  = new JSONObject();
        gps = new GPSTracker(getActivity().getApplicationContext());

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
}
