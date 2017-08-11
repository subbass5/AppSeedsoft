package seedsoft.apps.com.appseedsoft;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;

/**
 * Created by Dell3421 on 7/6/2017.
 */

public class Splash_Activity extends AppCompatActivity{
    Handler handler;
    Runnable runnable;
    long delay_time;
    long time = 1500L;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.splash_layout);
        getSupportActionBar().hide();
        handler = new Handler();

        runnable = new Runnable() {
            public void run() {
                Intent intent = new Intent(Splash_Activity.this, Login_Activity.class);
                startActivity(intent);
                finish();
            }
        };

    }
    public void onResume() {
        super.onResume();
        delay_time = time;
        handler.postDelayed(runnable, delay_time);
        time = System.currentTimeMillis();
    }

    public void onPause() {
        super.onPause();
        handler.removeCallbacks(runnable);
        time = delay_time - (System.currentTimeMillis() - time);
    }
}
