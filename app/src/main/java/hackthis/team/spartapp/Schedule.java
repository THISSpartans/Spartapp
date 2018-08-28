package hackthis.team.spartapp;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Fragment;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.webkit.ValueCallback;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ArrayAdapter;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;

import org.apache.commons.lang3.StringEscapeUtils;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Timer;
import java.util.TimerTask;

public class Schedule extends Fragment {

    private Activity mActivity;

    public boolean isLoggedIn = true;

    private CastratedDate browsingTime; //the date for the exhibited schedule
    private CastratedDate focusTime; //the date that contains the next period (will be enlarged)
                                        //this variable sets to the next school day
    private CastratedDate realTime; //the current time

    //private LoginActivity ls = null;

    private HashMap<String, Subject[]> subjectTable;

    View.OnClickListener FETCH = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //todo: code for log in
            Intent login = new Intent(mActivity, LoginActivity.class);
            startActivity(login);
        }
    };

    View.OnClickListener LEFT = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            browsingTime.change(Calendar.MONTH, -1);
            updateTitleBar();
            load();
        }
    };

    View.OnClickListener RIGHT = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            browsingTime.change(Calendar.MONTH, 1);
            updateTitleBar();
            load();
        }
    };

    View.OnClickListener DATE = new View.OnClickListener() {
        public void onClick(View v) {
            browsingTime.set(Calendar.DATE, (int)v.getTag());
            //RadioGroup rg = (RadioGroup)getView().findViewById(R.id.date_radio_group);
            autoscroll();
            load();
        }
    };

    RadioGroup.LayoutParams date_params;

    //todo i forgot the actual time, change before release!
    final int[] regularPeriodBeginning = {805, 845, 955, 1035, 1115, 1305, 1355, 1435};
    final int[] wednesdayPeriodBeginning = {805, 845, 955, 1035, 1115, 1305, 1355, 1435};

    //the last length of the date radio group before its update, used in updateTitleBar()
    int last_date_length;


    //find schedule for browsingTime and display on screen, called when fragment is initialized and when date picker gets clicked
    public void load(){
        Log.d("spartapp_log",browsingTime.date+" "+browsingTime.month_name());

        focusTime = getFocusedDate();

        //todo replace this with actual period reader

        int month = browsingTime.month;
        int yr =  browsingTime.year;
        int day = browsingTime.date;
        String m = month<10?"0"+Integer.toString(month):Integer.toString(month);
        String d = day<10?"0"+Integer.toString(day):Integer.toString(day);
        Subject[] subs = subjectTable.get(Integer.toString(yr)+"-"+m+"-"+d);
        /*
                {
                new Subject ("Period 1", "Mr. 1", "Room 1"), null,
                new Subject ("Period 2", "Mr. 2", "Room 2"), null,
                new Subject ("Period 3", "Mr. 3", "Room 3"), null,
                new Subject ("Period 4", "Mr. 4", "Room 4"), null,
        };*/

        for(int i = 0; i < 8; i++){
            if(subs[i] != null){
                List<ClassPeriod> periods = new ArrayList<>(8);
                for(int j = 0; j < 8; j++){
                    if(subs[j] != null)
                        periods.add(new ClassPeriod(subs[j], j));
                }
                Log.d("spartapp_schedule", CastratedDate.getHourMinute()+" b:"+browsingTime+" f:"+focusTime+" r:"+realTime);
                if(browsingTime.equals(focusTime)){
                    //for today
                    if(focusTime.equals(realTime)){
                        int[] beginning = realTime.get(Calendar.DAY_OF_WEEK)==Calendar.WEDNESDAY ? wednesdayPeriodBeginning : regularPeriodBeginning;
                        int temp = CastratedDate.getHourMinute();
                        for(ClassPeriod c : periods){
                            if(beginning[c.period] > temp){
                                c.focus = true;
                                break;
                            }
                        }
                    }
                    else{
                        //set focus to next period of next school day
                        periods.get(0).focus = true;
                    }
                }

                //convert periods into listview items
                ArrayAdapter<ClassPeriod> adapter = new PeriodAdapter(mActivity, R.layout.period_small, periods);
                ListView lv = (ListView) getView().findViewById(R.id.schedule_list);
                lv.setAdapter(adapter);

                return;
            }
        }

        //this part is reached when all subjects for browsingTime is null
        //print screen for empty day
    }

    public void onAttach(Context context) {
        super.onAttach(context);
        Log.d("SCHE", "onattach");
        mActivity = (Activity) context;

        browsingTime = new CastratedDate();
        realTime = new CastratedDate();

        date_params = new RadioGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        date_params.setMargins(15,0,15,0);

        try {
            subjectTable = getSchedule();
            Log.d("SCHE", "got");
        }
        catch(Exception e){
            Log.d("ERR", "getSchedule failed");
        }
    }

    /**
     * 取自https://blog.csdn.net/ljcitworld/article/details/77528585
     */
    @Override
    public void onStart() {
        super.onStart();

        if(getUserVisibleHint()) {
            ((RadioGroup)getView().findViewById(R.id.date_radio_group)).getChildAt(browsingTime.date-1).callOnClick();
            load();
        }

    }


    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.schedule, container, false);

        browsingTime = new CastratedDate();

        ImageButton f = (ImageButton) root.findViewById(R.id.schedule_fetch);
        f.setOnClickListener(FETCH);
        ImageButton l = (ImageButton) root.findViewById(R.id.schedule_left);
        l.setOnClickListener(LEFT);
        ImageButton r = (ImageButton) root.findViewById(R.id.schedule_right);
        r.setOnClickListener(RIGHT);

        TextView m = (TextView) root.findViewById(R.id.schedule_month);
        m.setText(month_text());

        RadioGroup rg = (RadioGroup) root.findViewById(R.id.date_radio_group);
        for(int i = 0; i < browsingTime.month_length(); i++){
            RadioButton rb = (RadioButton) mActivity.getLayoutInflater().inflate(R.layout.date_button, null);
            rb.setOnClickListener(DATE);
            rb.setText(Integer.toString(i+1));
            rb.setTag(i+1);
            rb.setLayoutParams(date_params);
            rg.addView(rb, i);
        }

        ((RadioButton)rg.getChildAt(browsingTime.date-1)).setChecked(true);
        HorizontalScrollView scroll = (HorizontalScrollView) root.findViewById(R.id.date_scroll);
        scroll.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                                                                   @Override
                                                                   public void onGlobalLayout() {
                                                                       autoscroll();
                                                                   }
                                                               });
        scroll.setSmoothScrollingEnabled(true);
        last_date_length = browsingTime.month_length();

        return root;
    }

    private void autoscroll(){
        HorizontalScrollView date_scroll = (HorizontalScrollView)getView().findViewById(R.id.date_scroll);
        RadioGroup rg = (RadioGroup) getView().findViewById(R.id.date_radio_group);
        date_scroll.smoothScrollTo(rg.getChildAt(browsingTime.date-1).getLeft()
                + rg.getChildAt(browsingTime.date-1).getMeasuredWidth()/2
                - date_scroll.getMeasuredWidth()/2, 0);
    }

    private CastratedDate getFocusedDate(){

        int endTime = (browsingTime.get(Calendar.DAY_OF_WEEK) == Calendar.WEDNESDAY) ?
                wednesdayPeriodBeginning[7] : regularPeriodBeginning[7];
        CastratedDate temp = new CastratedDate();
        if(CastratedDate.getHourMinute() > endTime){
            temp.change(Calendar.DATE, 1);
        }

        //todo read schedule of date c and return if it is has classes, currently it only returns today or tomorrow
        //if(/*after school time*/) d.change(Calendar.DATE, 1);
        //while(isVacation(d)){d.change(Calendar.DATE, 1);}
        return temp;
    }

    //append or remove number buttons to fit the new month length
    public void updateTitleBar(){
        View root = this.getView();

        TextView m = (TextView) root.findViewById(R.id.schedule_month);
        m.setText(month_text());

        RadioGroup rg = (RadioGroup) root.findViewById(R.id.date_radio_group);

        if(browsingTime.month_length() > last_date_length){
            for(int i = last_date_length; i < browsingTime.month_length(); i++){
                RadioButton rb = (RadioButton) mActivity.getLayoutInflater().inflate(R.layout.date_button, null);
                rb.setOnClickListener(DATE);
                rb.setText(Integer.toString(i+1));
                rb.setTag(i+1);
                rb.setLayoutParams(date_params);
                rg.addView(rb, i);
            }
        }
        else{
            if(browsingTime.month_length() < last_date_length){
                for(int i = last_date_length - 1; i >= browsingTime.month_length(); i--){
                    rg.removeViewAt(i);
                }
            }
            ((RadioButton)rg.getChildAt(browsingTime.month_length()-1)).setChecked(true);
        }

        last_date_length = browsingTime.month_length();
    }

    public void update(){
        //todo update the times and refresh UI if needed
    }

    /**
     * This method converts dp unit to equivalent pixels, depending on device density.
     *
     * @param dp A value in dp (density independent pixels) unit. Which we need to convert into pixels
     * @param context Context to get resources and device specific display metrics
     * @return A float value to represent px equivalent to dp depending on device density
     */
    public static float convertDpToPixel(float dp, Context context){
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float px = dp * ((float)metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        return px;
    }

    private String month_text(){return browsingTime.year+" "+browsingTime.month_name();}

    public HashMap<String, Subject[]> getSchedule() throws Exception{
        Log.d("INIT", "getSchedule() called");
        HashMap<String, Subject[]> schedule = new HashMap<>(6);

        HashMap<String, Integer> dateDay = getDateDayPairs();

        HashMap<Integer, Subject[]> weeklySchedule = readWeeklySchedule();

        for(Map.Entry<String, Integer> keyValuePair : dateDay.entrySet()){
            String date = keyValuePair.getKey();
            Integer day = keyValuePair.getValue();
            Log.d("Demo",date+" is day "+day);
            if(day != -1)
                schedule.put(date, weeklySchedule.get(day));
            else
                schedule.put(date, null);
        }
        Log.d("INIT", "getSchedule() returned");
        return schedule;
    }

    public HashMap<String, Integer> getDateDayPairs()throws AVException, ParseException {
        Log.d("INIT", "getDateDayPairs() called");

        HashMap<String, Integer> dateDay;
        try{
            dateDay = readDateDayPairs();
            Log.d("INIT_AVO", "DateDayPairs read");
        }catch(IOException e){
            dateDay = fetchDateDayPairs("2017-08-21", true);
            Log.d("INIT_AVO", "DateDayPairs defaulted");
        }
        return dateDay;
    }

    public HashMap<String, Integer> fetchDateDayPairs(String startOfYear, boolean def) throws ParseException, AVException {
        HashMap<String, Integer> dateDay = new HashMap<>(0);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar c = Calendar.getInstance();
        c.setTime(sdf.parse(startOfYear));
        List<AVObject> schoolDays = null;
        for(AVObject schoolDay : schoolDays){
            String time = sdf.format(c.getTime());
            Integer day = -1;
            if(!def) day = schoolDay.getInt("dayInCycle");
            dateDay.put(time, day);
            c.add(Calendar.DATE, 1);
            Log.d("WKD_", time  + " " + day);
        }
        return dateDay;
    }


    public HashMap<String, Integer> readDateDayPairs() throws IOException{
        try {
            FileInputStream f = this.getContext().openFileInput("date_day.dat");
            ObjectInputStream s = new ObjectInputStream(f);
            HashMap<String, Integer> dateDay = (HashMap<String, Integer>)s.readObject();
            s.close();
            Log.d("WKD", "input success");
            return dateDay;
        }
        catch(ClassNotFoundException e){Log.d("WKD", "classnotfound");}
        return null;
    }

    public HashMap<Integer, Subject[]> readWeeklySchedule() throws Exception{
        Log.d("HTML_IN", "called");
        FileInputStream f = this.getContext().openFileInput("week_schedule.dat");
        Log.d("HTML_IN", "found");
        BufferedReader in = new BufferedReader(new InputStreamReader(f));
        Log.d("HTML_IN", "buffer on");
        HashMap<Integer, Subject[]> schedule = new HashMap<>(0);
        String line;
        int dayInCycle = 1;
        while((line = in.readLine())!=null){
            StringTokenizer tizer = new StringTokenizer(line, "?");
            Subject[] daySchedule = new Subject[8];
            for(int period = 0; period < 8; period ++){
                String name = tizer.nextToken();
                String teacher = tizer.nextToken();
                String room = tizer.nextToken();
                Subject subject = new Subject(name, teacher, room);
                Log.d("HTML_IN",subject.name() + "," + subject.teacher() + "," + subject.room() + ",");
                daySchedule[period] = subject;
            }
            schedule.put(dayInCycle, daySchedule);
            dayInCycle ++;
        }
        in.close();
        return schedule;
    }


}
