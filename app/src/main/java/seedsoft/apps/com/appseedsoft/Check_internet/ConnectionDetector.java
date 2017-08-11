package seedsoft.apps.com.appseedsoft.Check_internet;

import android.app.Service;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by Dell3421 on 15/7/2560.
 */

public class ConnectionDetector {
    Context context;
    public ConnectionDetector(Context context){
        this.context = context;

    }

    public boolean isConnected(){
        ConnectivityManager connectivity = (ConnectivityManager)
                context.getSystemService(Service.CONNECTIVITY_SERVICE);
        if(connectivity != null){
            NetworkInfo info = connectivity.getActiveNetworkInfo();
            if (info != null){
                if (info.getState()==NetworkInfo.State.CONNECTED){
                    return true;
                }
            }

        }

        return false;
    }
}
