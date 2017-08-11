package seedsoft.apps.com.appseedsoft.Detail_mobile;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.util.Log;

import org.apache.http.ParseException;

import java.net.NetworkInterface;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;


public class Detail_mobile {

    public String getTime(){
        SimpleDateFormat ftime = new SimpleDateFormat("dd-MM-yyyy HH:mm");
        Date dt = new Date();
        String time = ftime.format(dt);
        return time;
    }
    public String dayName(){
        String weekDay = "";

        Calendar c = Calendar.getInstance();
        int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);

        if (Calendar.MONDAY == dayOfWeek) {
            weekDay = "วันจันทร์";
        } else if (Calendar.TUESDAY == dayOfWeek) {
            weekDay = "วันอังคาร";
        } else if (Calendar.WEDNESDAY == dayOfWeek) {
            weekDay = "วันพุธ";
        } else if (Calendar.THURSDAY == dayOfWeek) {
            weekDay = "วันพฤหัสบดี";
        } else if (Calendar.FRIDAY == dayOfWeek) {
            weekDay = "วันศุกร์";
        } else if (Calendar.SATURDAY == dayOfWeek) {
            weekDay = "วันเสาร์";
        } else if (Calendar.SUNDAY == dayOfWeek) {
            weekDay = "วันอาทิตย์";
        }
        return weekDay;
    }

    public int dayNumber(){

        Calendar c = Calendar.getInstance();
        int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);
        return dayOfWeek;
    }


    public String getDateFromat(String format){
        SimpleDateFormat ftime = new SimpleDateFormat(format);
        Date dt = new Date();
        String time = ftime.format(dt);
        return time;
    }

    public long diffTime(String currenttime,String timein){

        long diff = 0;
        long hr = 0;

        SimpleDateFormat format = new SimpleDateFormat("HH:mm");
        try {

            Date dateCurrent = format.parse(currenttime);
            Date timeIn  = format.parse(timein);
            diff =  timeIn.getMinutes() - dateCurrent.getMinutes();
            hr  = timeIn.getHours() - dateCurrent.getHours();
//            Log.d("Hour",""+hr);
//            System.out.println(hr);

        } catch (java.text.ParseException e) {
            e.printStackTrace();
        }

        if(hr!=0 )
            return -1;
        else
            return diff;

    }

    public String getMac(){
        try {
            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nif : all) {
                if (!nif.getName().equalsIgnoreCase("wlan0")) continue;

                byte[] macBytes = nif.getHardwareAddress();
                if (macBytes == null) {
                    return "";
                }
                StringBuilder res1 = new StringBuilder();
                for (byte b : macBytes) {
                    res1.append(Integer.toHexString(b & 0xFF) + ":");
                }

                if (res1.length() > 0) {
                    res1.deleteCharAt(res1.length() - 1);
                }
                return res1.toString();
            }
        } catch (Exception ex) {
        }
        return "02:00:00:00:00:00";
    }

    public boolean getLengthTime(String timeStart,String timeEnd){

        String dateFormat = "HH:mm";
        String startTime= timeStart;
        String endTime= timeEnd;
        String currentTime = new SimpleDateFormat(dateFormat).format(new Date());

        Calendar cStart = setTimeToCalendar(dateFormat, startTime, false);
        Calendar cEnd = setTimeToCalendar(dateFormat, endTime, true);
        Calendar cNow = setTimeToCalendar(dateFormat, currentTime, true);
        Date curDate = cNow.getTime();

        if (curDate.after(cStart.getTime()) && curDate.before(cEnd.getTime())) {
            Log.i("Status Time","Time in range");
            return true;
        } else {
            Log.i("Status Time","Time out range");
            return false;
        }
    }


    private Calendar setTimeToCalendar(String dateFormat, String date, boolean addADay) throws ParseException {
        Date time = null;
        try {
            time = new SimpleDateFormat(dateFormat).parse(date);
        } catch (java.text.ParseException e) {
            e.printStackTrace();
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(time );

        if(addADay) {
            cal.add(Calendar.DATE, 1);
        }
        return cal;
    }
}
