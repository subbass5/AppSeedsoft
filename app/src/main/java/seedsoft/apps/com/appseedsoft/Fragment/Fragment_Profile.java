package seedsoft.apps.com.appseedsoft.Fragment;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
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

import java.util.ArrayList;
import java.util.List;

import seedsoft.apps.com.appseedsoft.DeviceUser.Device;
import seedsoft.apps.com.appseedsoft.Profile_login.Profile_login;
import seedsoft.apps.com.appseedsoft.R;

/**
 * Created by Kritsana on 7/21/2017.
 */

public class Fragment_Profile extends android.support.v4.app.Fragment {
        private ProgressDialog progress;
        TextView tvName, tvDepartment, tvPosition,tv_phone,tv_email;
        Profile_login profile_login;
        ImageView img;
        private ProgressDialog progressDialog;
        String url = "http://128.199.196.236/api/staff?api_token=";
        String testJson = "[\n" +
                "\t\t\t{\n" +
                "\t\t\t\t\"rfid_id\":\"xxxxxxx\",\n" +
                "\t\t\t\t\"uid\":\"199,66,77,88\",\n" +
                "\t\t\t\t\"location_id\":\"XXXXXXXXX\"\n" +
                "\t\t\t},\n" +
                "\t\t\t{\n" +
                "\t\t\t\t\"rfid_id\":\"xxxxxxx\",\n" +
                "\t\t\t\t\"uid\":\"199,66,77,88\",\n" +
                "\t\t\t\t\"location_id\":\"XXXXXXXXX\"\n" +
                "\t\t\t},\n" +
                "\t\t\t{\n" +
                "\t\t\t\t\"rfid_id\":\"xxxxxxx\",\n" +
                "\t\t\t\t\"uid\":\"199,66,77,88\",\n" +
                "\t\t\t\t\"location_id\":\"XXXXXXXXX\"\n" +
                "\t\t\t},\n" +
                "\t\t\t{\n" +
                "\t\t\t\t\"rfid_id\":\"xxxxxxx\",\n" +
                "\t\t\t\t\"uid\":\"199,66,77,88\",\n" +
                "\t\t\t\t\"location_id\":\"XXXXXXXXX\"\n" +
                "\t\t\t},\n" +
                "\t\t\t{\n" +
                "\t\t\t\t\"rfid_id\":\"xxxxxxx\",\n" +
                "\t\t\t\t\"uid\":\"199,66,77,88\",\n" +
                "\t\t\t\t\"location_id\":\"XXXXXXXXX\"\n" +
                "\t\t\t}\n" +
                "\n" +
                "\t   ]";

        SharedPreferences pref;
        SharedPreferences.Editor editor;
        String api_key_profile;
        List<String> listUid = new ArrayList<>();
        List<String> listLocationId = new ArrayList<>();
        ListView listDevice;

    ArrayAdapter<String> mAdapter ;

        public Fragment_Profile() {

        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.fragment_profile, container, false);
            tvName = (TextView) v.findViewById(R.id.tv_name_profile);
            tvDepartment  = (TextView) v.findViewById(R.id.tv_department_profile);
            tvPosition = (TextView) v.findViewById(R.id.tv_position_profile);
            tv_phone = (TextView) v.findViewById(R.id.tv_phone_profile);
            tv_email = (TextView) v.findViewById(R.id.tv_email_profile);
            img = (ImageView) v.findViewById(R.id.img_profile) ;
            listDevice = (ListView) v.findViewById(R.id.list_device);
            return v;
        }

    @Override
    public void onViewCreated(View view,Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        pref = getContext().getSharedPreferences("User", 0);
        editor = pref.edit();

        Device device = new Device(testJson);

        listUid = device.getUID();

        api_key_profile = pref.getString("api_key", null).toString();
        Log.e("API_Key", api_key_profile);
        mAdapter = new ArrayAdapter<>(getContext(),android.R.layout.simple_expandable_list_item_1,listUid);
        listDevice.setAdapter(mAdapter);

        new GetData(getContext()).execute(url+api_key_profile);
    }
        private class GetData extends AsyncTask<String, Void, String> {

            Context context;

            public GetData(Context context) {
                this.context = context;

            }

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                progress = new ProgressDialog(this.context);
                progress.setMessage("Loading");
                progress.show();
            }

            @Override
            protected String doInBackground(String... params) {
                final String url = params[0];
                Log.d("Task", url);
                try {
                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder().url(url).build();
                    Response response = client.newCall(request).execute();
                    return response.body().string();

                } catch (Exception e) {

                }
                return null;
            }

            protected void onPostExecute(String result) {
                super.onPostExecute(result);
                if(progress.isShowing()){
                    progress.dismiss();
                }

                profile_login = new Profile_login(result);
                tvName.setText(profile_login.getNameStaff());
                tvDepartment.setText(profile_login.getDepartment());
                tvPosition.setText(profile_login.getPosition());
                tv_email.setText(profile_login.getEmailStaff());
                tv_phone.setText(profile_login.getPhoneStaff());

                Picasso.with(getActivity().getApplicationContext())
                        .load(profile_login.getPathProfile())
                        .resize(150, 150)
                        .centerCrop()
                        .into(img);
            }
        }
    }


