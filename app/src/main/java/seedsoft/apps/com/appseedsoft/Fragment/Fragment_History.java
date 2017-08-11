package seedsoft.apps.com.appseedsoft.Fragment;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.picasso.Picasso;

import seedsoft.apps.com.appseedsoft.Profile_login.Profile_login;
import seedsoft.apps.com.appseedsoft.R;

public class Fragment_History extends Fragment{
    private final String obj;
    private ProgressDialog progress;
    TextView tvName,tvDepartment,tvPosition;
    Profile_login profile_login;
    Button logout;
    ImageView img;
    ListView listActivity;
    private ProgressDialog progressDialog;
    String url = "http://128.199.196.236/api/staff?api_token=";
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    String api_key_profile;

    public Fragment_History(String obj){
        this.obj = obj;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
       View v = inflater.inflate(R.layout.fragment_history,container,false);
         pref = getContext().getSharedPreferences("User", 0); // 0 - for private mode
         editor = pref.edit();

//        img = (ImageView) v.findViewById(R.id.img_profile_history);
//        tvName = (TextView) v.findViewById(R.id.txt_name_user_history);
//        tvDepartment = (TextView) v.findViewById(R.id.txt_Department);
//        tvPosition = (TextView) v.findViewById(R.id.txt_Position);
//        logout = (Button) v.findViewById(R.id.btn_logout);
        api_key_profile = pref.getString("api_key",null).toString();

        final WebView mWebView = (WebView) v.findViewById(R.id.web_history);
        mWebView.setWebChromeClient(new WebChromeClient());

        final WebSettings settings = mWebView.getSettings();
        settings.setDomStorageEnabled(true);
        settings.setJavaScriptEnabled(true);

        mWebView.setVerticalScrollBarEnabled(true);

        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(final WebView view, final String url) {
                super.shouldOverrideUrlLoading(view, url);
                return false;
            }
            @Override
            public void onPageFinished(final WebView view, final String url) {
                super.onPageFinished(view, url);
                mWebView.requestLayout();
            }

        });
        mWebView.loadUrl("http://188.166.188.78/apps_intime/apps_history.php?api="+api_key_profile);
        Log.e("API_Key",api_key_profile);

//        logout.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent goLogin = new Intent(getContext(), Login_Activity.class);
//                editor.clear();
//                editor.commit();
//                startActivity(goLogin);
//                getActivity().finish();
//
//            }
//        });

//        new GetData(v.getContext()).execute(url+obj);

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    private class GetData extends AsyncTask<String,Void,String> {

        Context context;
        public GetData(Context context){
            this.context = context;

        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progress= new ProgressDialog(this.context);
            progress.setMessage("Loading");
            progress.show();
        }

        @Override
        protected String doInBackground(String... params){
            final String url = params[0];
            Log.d("Task",url);
            try {
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder().url(url).build();
                Response response = client.newCall(request).execute();
                return response.body().string();

            }catch (Exception e){

            }
            return null;
        }
        protected void onPostExecute(String result){
            super.onPostExecute(result);
            progress.dismiss();

            profile_login = new Profile_login(result);
            tvName.setText(profile_login.getNameStaff());
            tvDepartment.setText(profile_login.getDepartment());
            tvPosition.setText(profile_login.getPosition());
            Picasso.with(getActivity().getApplicationContext())
                    .load(profile_login.getPathProfile())
                    .resize(150, 150)
                    .centerCrop()
                    .into(img);
        }
    }

}
