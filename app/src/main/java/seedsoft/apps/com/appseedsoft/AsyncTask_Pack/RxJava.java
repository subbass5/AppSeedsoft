package seedsoft.apps.com.appseedsoft.AsyncTask_Pack;

import android.util.Log;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import rx.Observable;
import rx.Observer;
import rx.Scheduler;
import rx.Subscriber;
import rx.functions.Action1;
import rx.schedulers.Schedulers;


/**
 * Created by Kritsana on 8/15/2017.
 */

public class RxJava {
    String result;
    Observable<String> feedDataAPI;
    public RxJava(final String url, final String apikey){

       feedDataAPI = Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                try{
                    String response = feeddata(url,apikey);
                    subscriber.onNext(response);
                    result = response;

                }catch (Exception e){
                    Log.e("Error at RxJava class",e.getMessage());
                }
            }
        }).subscribeOn(Schedulers.newThread());

    }

    public Observable<String> getFeedDataAPI() {
        return feedDataAPI;
    }

    private String feeddata(final String url, final String api_key){
        String links = url+api_key;
        try {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder().url(links).build();
            Response response = client.newCall(request).execute();
            return response.body().string();

        }catch (Exception e){
            Log.e("Error at RxJava ",e.getMessage());
            return "";
        }
    }


}
