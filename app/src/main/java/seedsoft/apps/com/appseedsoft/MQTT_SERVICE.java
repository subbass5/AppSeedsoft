package seedsoft.apps.com.appseedsoft;


import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
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
import org.json.JSONObject;

import seedsoft.apps.com.appseedsoft.Fragment.Fragment_Dashbord;
import seedsoft.apps.com.appseedsoft.GPSTracker.GPSTracker;

public class MQTT_SERVICE{
    public static final String TAGMQTT = "MQTT_Seed-soft";
    public String subscribeTopic = "/device/door/";
    public static final  String broker       = "tcp://188.166.188.78:1883";
    public static final  String clientId     = MqttClient.generateClientId();
    public static final  String username     = "seedsoft";
    public static final  String password     = "seedsoft";
    Context context;
    MqttAndroidClient mqttAndroidClient;
    GPSTracker gps;
    JSONObject obj = new JSONObject();
    SharedPreferences pref ;
    SharedPreferences.Editor editor;
    public MQTT_SERVICE(Context context){
        this.context = context;
        gps = new GPSTracker(context);
        mqttAndroidClient = new MqttAndroidClient(context,broker,clientId);


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
//        Log.d(TAGMQTT,mainText);
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
                    Log.d("Message", topic + " : " +msg);
                    Log.e("Message topic ",topic);
                    Bundle b = new Bundle();
                    b.putString("keymsg",msg);
                    Fragment_Dashbord da = new Fragment_Dashbord();
                    da.setArguments(b);
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

    public void setDisconnected(){
        try {
            mqttAndroidClient.disconnect();
        } catch (MqttException e) {
            e.printStackTrace();
        }

    }


}
