package seedsoft.apps.com.appseedsoft;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.Callable;

import rx.Observable;
import rx.Observer;
import rx.Scheduler;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import seedsoft.apps.com.appseedsoft.AsyncTask_Pack.RxJava;
import seedsoft.apps.com.appseedsoft.Check_internet.ConnectionDetector;
import seedsoft.apps.com.appseedsoft.GPSTracker.GPSTracker;


public class Login_Activity extends AppCompatActivity{
    Button btnLogin,btnRegister;
    EditText et_username,et_password;
    private ProgressDialog progress;
    TextView tv_connect;
    JSONObject objJson;
    private Handler scanHandler = new Handler();
    ConnectionDetector checkconnectInternet;
    public static final String URL_AUTH ="http://128.199.196.236/api/login";
//Session android
    GPSTracker gps;
    SharedPreferences sharedpreferences;
    public static final String MyPREFERENCES = "User" ;
    public static final String USER = "username";
    public static final String PASS = "password";
    public static final String API = "api_key";
    public static final String JSON_OBJ = "json_obj";
    public static final String URL_ = "urls";
    public static final String ID_LOCATION = "id_location";
    public static final String USER_LOCATION = "location_user";
    public static final String Keycard = "keycard";
    public static final int TIME_INTERVAL = 2500;

    private static BluetoothAdapter bAdapter;
    private static BluetoothManager bManager;
    private static  int REQUEST_ENABLE_BT = 0;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_login);
        getSupportActionBar().hide();
        btnLogin = (Button) findViewById(R.id.btn_login);
        tv_connect = (TextView) findViewById(R.id.txt_connected);
        checkconnectInternet = new ConnectionDetector(this);
        sharedpreferences = getSharedPreferences(MyPREFERENCES,Context.MODE_PRIVATE);
        et_username = (EditText) findViewById(R.id.et_user);
        et_password = (EditText) findViewById(R.id.et_password);
        gps = new GPSTracker(Login_Activity.this);

        bManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bAdapter = bManager.getAdapter();


        scanHandler.post(scanRunnable);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                initLogin("user","1234");

                String username = et_username.getText().toString().trim();
                String password = et_password.getText().toString().trim();
                if (username.length() == 0 && password.length()== 0){
                    et_username.setText("");
                    et_password.setText("");
                    Toast.makeText(Login_Activity.this, "Please enter username and password.", Toast.LENGTH_SHORT).show();
                }else if (username.length() < 4 && password.length()< 8){
                    et_username.setText("");
                    et_password.setText("");
                    Toast.makeText(Login_Activity.this, "Username  and Password is short.", Toast.LENGTH_SHORT).show();
                }else if(username.length() > 0 ){
                        final String USER = username;
                        final String PASS = password;
                    initLogin(USER,PASS);
                }
            }
        });

    }

    private Runnable scanRunnable = new Runnable()
    {
        @Override
        public void run() {
            checkInternet();
            scanHandler.postDelayed(scanRunnable, 3000);
        }
    };

    @Override
    public void onBackPressed() {
        AlertDialog.Builder dialog2 = new AlertDialog.Builder(Login_Activity.this);
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
        if (checkconnectInternet.isConnected()){
            tv_connect.setText("");
            btnLogin.setEnabled(true);
            btnLogin.setBackgroundTintList( ColorStateList.valueOf(getResources().getColor(R.color.bootstrap_brand_success)));
        }else {
            tv_connect.setText("Please connect to internet.");
            tv_connect.setTextColor(getResources().getColor(R.color.bootstrap_brand_warning));
            btnLogin.setEnabled(false);
            btnLogin.setBackgroundTintList( ColorStateList.valueOf(getResources().getColor(R.color.bootstrap_brand_danger)));
        }
    }


    private void initLogin(final String user,final String pass ){

        progress = new ProgressDialog(Login_Activity.this);
        progress.setMessage("Loading....");
        progress.setIcon(R.drawable.ap);
        progress.show();

        final String url = URL_AUTH;
        final Observable<String> loginJson = Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                String result = loginSystem(url,user,pass);
                subscriber.onNext(result);
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());

        loginJson.subscribe(new Observer<String>() {
            @Override
            public void onCompleted() {
                et_username.setText(null);
                et_password.setText(null);
            }

            @Override
            public void onError(Throwable e) {
                Toast.makeText(Login_Activity.this, "Log in fail.", Toast.LENGTH_SHORT).show();
                Log.e("onError at rxjava",e.getMessage());
                et_username.setText(null);
                et_password.setText(null);

            }

            @Override
            public void onNext(String s) {

                if(progress.isShowing()) progress.dismiss();

                if (s.length() > 70) {
                    goMain(s);
                } else {
                    Toast.makeText(getApplicationContext(), "Loin fail.", Toast.LENGTH_SHORT).show();
                    et_username.setText(null);
                    et_password.setText(null);
                }
            }
        });

    }

    private void goMain(String api){
        if(!TextUtils.isEmpty(api)){
            try {
                JSONObject object = new JSONObject(api);
                String url = "http://128.199.196.236/api/staff?api_token=";
                final String api_token = object.getString("api_token");
                final RxJava rx = new RxJava(url,api_token);

                   rx.getFeedDataAPI().subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<String>() {
                    @Override
                    public void call(String s) {
                        Log.d("Res",s);
                        Intent goMain = new Intent(Login_Activity.this,MainActivity.class);
                        SharedPreferences.Editor editor = sharedpreferences.edit();
                        editor.putString(USER,et_username.getText().toString());
                        editor.putString(PASS,et_password.getText().toString());
                        editor.putString(API,api_token);
                        editor.putString(URL_,URL_AUTH);
                        editor.putString(JSON_OBJ,s);
                        editor.commit();
                        startActivity(goMain);
                        finish();
                    }
                });

            } catch (JSONException e) {
                Log.e("Eror at json goMain()",e.getMessage());

            }catch (Exception e){
                Log.e("Eror at goMain()",e.getMessage());
            }
        }
    }


    private String loginSystem(final String url ,final String user,final String password){

        try {
            URL urls = new URL(url);
            HttpURLConnection connection = (HttpURLConnection)urls.openConnection();
            String urlParameters = "username="+user+"&password="+password;
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            DataOutputStream dStream = new DataOutputStream(connection.getOutputStream());
            dStream.writeBytes(urlParameters);
            dStream.flush();
            dStream.close();
            BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line = "";
            final StringBuilder responseOutput = new StringBuilder();
            while((line = br.readLine()) != null ) {
                responseOutput.append(line);
            }
            br.close();
            return  responseOutput.toString();

        } catch (MalformedURLException e) {
            Log.e("Error at class login",e.getMessage());
            return null;
        } catch (IOException e) {
            Log.e("Error at class login",e.getMessage());
            return null;
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        checkInternet();

        if(Build.VERSION.SDK_INT < 18)
          {
              Toast.makeText(Login_Activity.this, "Android Version Not Support.", Toast.LENGTH_SHORT).show();

          }else{
            if (!bAdapter.isEnabled()){

                Intent request_openBT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(request_openBT,REQUEST_ENABLE_BT);

                        }else{
                         AlertDialog.Builder builder = new AlertDialog.Builder(Login_Activity.this);
                                LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                                     if( !locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ) {
                                            builder.setMessage("กรุณาเปิด GPS."); // Want to enable?
                                            builder.setPositiveButton("เปิด", new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    Intent onGPS = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                                    startActivity(onGPS);
                                                }
                                            });
                                            builder.setNegativeButton("ปิด Apps", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    finish();
                                                }
                                            });
                                            builder.create().show();
                                            return;
                                        }else{

                                            final String USER = sharedpreferences.getString(Login_Activity.USER,null);
                                            final String PASS = sharedpreferences.getString(Login_Activity.PASS,null);
                                            if (!TextUtils.isEmpty(USER) && !TextUtils.isEmpty(PASS)){
                                                initLogin(USER,PASS);
                                            }

                                     }
                        }
                }
    }
}
