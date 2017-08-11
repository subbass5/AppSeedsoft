package seedsoft.apps.com.appseedsoft.AsyncTask_Pack;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.speech.tts.Voice;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;


public class GetDataAPI extends AsyncTask<String,Voice,String> {
    private Context context;

    public GetDataAPI(Context context){
        this.context = context;

    }

    @Override
    protected void onPreExecute() {
//        super.onPreExecute();


    }

    @Override
        protected String doInBackground(String... params){
            final String url = params[0];

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


    }
}
