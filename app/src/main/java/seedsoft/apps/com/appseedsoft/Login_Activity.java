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
    public static final int TIME_INTERVAL = 2000;

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
// new Object
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
                    try{
                        new PostClass(Login_Activity.this,USER,PASS).execute(URL_AUTH);
                    }catch (Exception e){
                        Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_SHORT).show();
                    }

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


    private class PostClass extends AsyncTask<String, Void, String> {
        private final Context context;
        private final String user;
        private final String pass;


        public PostClass(Context c,String user,String pass){
            this.context = c;
            this.user = user;
            this.pass = pass;
        }
        protected void onPreExecute(){
            progress= new ProgressDialog(this.context);
            progress.setMessage("Loading");
            progress.show();
        }
        @Override
        protected String doInBackground(String... params) {
            try {

                URL url = new URL(params[0]);
                HttpURLConnection connection = (HttpURLConnection)url.openConnection();
                String urlParameters = "username="+user+"&password="+pass;
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

                e.printStackTrace();
            } catch (IOException e) {

                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(String jsonString) {
//
//            if(progress.isShowing()){
//                progress.dismiss();
//            }
            String check = "";
            if(!TextUtils.isEmpty(jsonString)){
                try {
                    objJson = new JSONObject(jsonString);
                    check = objJson.get("api_token").toString();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if(check.length()> 0 ){
                    String url = "http://128.199.196.236/api/staff?api_token=";
                    new GetData(context,check).execute(url+check);
                }else {
                    if(progress.isShowing()) progress.dismiss();
                    Toast.makeText(getApplicationContext(), "Login Fail", Toast.LENGTH_SHORT).show();
                    et_username.setText("");
                    et_password.setText("");
                }

            }else {
                if(progress.isShowing()){
                    progress.dismiss();
                }

                Toast.makeText(context, "เข้าสู่ระบบ ไม่สำเร็จ กรุณาลองใหม่อีกครั้ง", Toast.LENGTH_SHORT).show();
                et_username.setText("");
                et_password.setText("");
            }
        }
    }


    public class GetData extends AsyncTask<String,Void,String>{
        String apikey = "";
        Context context;
        public GetData(Context context,String apikey){
            this.context = context;
            this.apikey = apikey;
        }

        @Override
        protected void onPreExecute() {
//            progress= new ProgressDialog(this.context);
//            progress.setMessage("Loading");
//            progress.show();
        }

        @Override
        protected String doInBackground(String... params) {
            final String url = params[0];
//            Toast.makeText(getApplicationContext(), ""+url, Toast.LENGTH_SHORT).show();
            try {
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder().url(url).build();
                Response response = client.newCall(request).execute();
                return response.body().string();
            }catch (Exception e){

            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            scanHandler.removeCallbacks(scanRunnable);

            if(progress.isShowing()){
                progress.dismiss();
            }
            if(!TextUtils.isEmpty(s)){
                Intent goMain = new Intent(Login_Activity.this,MainActivity.class);
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putString(USER,et_username.getText().toString());
                editor.putString(PASS,et_password.getText().toString());
                editor.putString(API,apikey);
                editor.putString(URL_,URL_AUTH);
                editor.putString(JSON_OBJ,s);
                editor.commit();
                startActivity(goMain);
                finish();
            }

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
                                            final String USER = sharedpreferences.getString("username",null);
                                            final String PASS = sharedpreferences.getString("password",null);
                                            if (!TextUtils.isEmpty(USER) && !TextUtils.isEmpty(PASS))
                                                new PostClass(Login_Activity.this,USER,PASS).execute(URL_AUTH);
                                     }
                        }
                }
    }
}
