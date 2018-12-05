package hackthis.team.spartapp;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MotionEventCompat;
import android.text.Layout;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.DatePicker;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;


import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

public class Schedule extends RefreshableFragment {

    private Activity mActivity;

    private CastratedDate browsingTime; //the date for the exhibited schedule
    private CastratedDate focusTime; //the date that contains the next period (will be enlarged)
                                        //this variable sets to the next school day

    private HashMap<String, Subject[]> subjectTable;

    ArrayAdapter<ClassPeriod> adapter;

    ImageView listBackground;
    ListView lv;

    public int unitHeight;

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
            Calendar cal = Calendar.getInstance();
            cal.set(browsingTime.year, browsingTime.month, 1);
            if(browsingTime.month == new CastratedDate().month) {
                browsingTime.set(Calendar.DATE,new CastratedDate().date);
            }
            else {
                browsingTime.set(Calendar.DATE, cal.getActualMaximum(Calendar.DATE));
            }
            updateTitleBar();
            load();
        }
    };

    View.OnClickListener RIGHT = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            browsingTime.change(Calendar.MONTH, 1);
            if(browsingTime.month == new CastratedDate().month) {
                browsingTime.set(Calendar.DATE,new CastratedDate().date);
            }
            else {
                browsingTime.set(Calendar.DATE, 1);
            }
            updateTitleBar();
            load();
        }
    };
    PopupWindow popup;
    CalendarView expcal;
    LinearLayout cal;
    Boolean expanded = false;

    View.OnClickListener EXPAND = new View.OnClickListener(){
        @Override
        public void onClick(View v){
            if(expanded) {
                popup.showAsDropDown(mActivity.findViewById(R.id.expand_calendar));
                ((Button)(mActivity.findViewById(R.id.expand_calendar)))
                        .setBackground(ContextCompat.getDrawable(mActivity, R.drawable.arrow_down));
                popup.dismiss();
                expanded = false;
            }else {
                popup.showAsDropDown(mActivity.findViewById(R.id.expand_calendar));
                ((Button)(mActivity.findViewById(R.id.expand_calendar)))
                        .setBackground(ContextCompat.getDrawable(mActivity, R.drawable.arrow_up));
                expanded = true;
            }
        }
    };

    View.OnClickListener DATE = new View.OnClickListener() {
        public void onClick(View v) {
            browsingTime.set(Calendar.DATE, (int)v.getTag());
            //RadioGroup rg = (RadioGroup)getView().findViewById(R.id.date_radio_group);
            autoscroll();
            load();

            Calendar cal = Calendar.getInstance();
            cal.set(browsingTime.year, browsingTime.month, browsingTime.date);
            expcal.setDate(cal.getTimeInMillis());
        }
    };

    String school;

    RadioGroup.LayoutParams date_params;
    LinearLayout.LayoutParams wk_params;

    HashMap<String, int[]> regularPeriodBeginning;
    HashMap<String, int[]> wednesdayPeriodBeginning;

    //the last length of the date radio group before its update, used in updateTitleBar()
    int last_date_length;

    //find schedule for browsingTime and display on screen, called when fragment is initialized and when date picker gets clicked
    public void load(){
        LogUtil.d("spartapp_log",browsingTime.date+" "+browsingTime.month_name());

        LogUtil.d("sche_time",browsingTime.toString());

        int month = browsingTime.month + 1;
        int yr =  browsingTime.year;
        int day = browsingTime.date;
        String m = month<10?"0"+Integer.toString(month):Integer.toString(month);
        String d = day<10?"0"+Integer.toString(day):Integer.toString(day);
        LogUtil.d("TIME", Integer.toString(yr)+"-"+m+"-"+d);
        Subject[] subs = subjectTable.get(Integer.toString(yr)+"-"+m+"-"+d);

        //LogUtil.d("SCHEDULE", subjectTable.get("2018-09-13")[0].name());

        if(subs != null) {
                if(school.equals("THIS")) {
                    //for THIS
                    for (int i = 1; i < subs.length; i+=2) {
                        if (subs[i] != null && subs[i-1]!=null && subs[i].equals(subs[i-1]))
                            subs[i] = null;
                    }
                }
                else if (school.equals("ISB")){
                    //todo any modifications for isb?
                    if(browsingTime.get(Calendar.DAY_OF_WEEK) == Calendar.WEDNESDAY)
                        subs[2] = null;
                }
                for (Subject i : subs) {
                    if (i != null) {
                        List<ClassPeriod> periods = new ArrayList<>(8);
                        for (int j = 0; j < subs.length; j++) {
                            if (subs[j] != null)
                                periods.add(new ClassPeriod(subs[j], j));
                        }
                        LogUtil.d("spartapp_schedule", CastratedDate.getHourMinute() + " b:" + browsingTime + " f:" + focusTime + " r:" + new CastratedDate().toString());
                        if (browsingTime.equals(focusTime)) {
                            //for today
                            if (focusTime.equals(new CastratedDate())) {
                                int[] beginning = (new CastratedDate()).get(Calendar.DAY_OF_WEEK) ==
                                        Calendar.WEDNESDAY ? wednesdayPeriodBeginning.get(school) :
                                        regularPeriodBeginning.get(school);
                                int temp = CastratedDate.getHourMinute();
                                for (ClassPeriod c : periods) {
                                    if (beginning[c.period] > temp) {
                                        c.focus = true;
                                        break;
                                    }
                                }
                            } else {
                                //set focus to first period of next school day
                                periods.get(0).focus = true;
                            }
                        }

                        //convert periods into listview items
                        adapter = new PeriodAdapter(mActivity, R.layout.period_small, periods);
                        listBackground.setImageDrawable(null);
                        lv.setAdapter(adapter);

                        break;
                    }
                }
            }
        else{
            ArrayList<ClassPeriod> empty = new ArrayList<>(0);
            adapter = new PeriodAdapter(mActivity, R.layout.period_small, empty);
            LogUtil.d("SCHE",Integer.toString(browsingTime.get(Calendar.DAY_OF_WEEK)));
            if(browsingTime.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY
                    || browsingTime.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
                listBackground.setImageResource(R.drawable.weekend);
            }
            else{
                listBackground.setImageResource(R.drawable.vacation);
            }
            lv.setAdapter(adapter);
        }
    }


    public void onAttach(Context context) {
        super.onAttach(context);
        LogUtil.d("SCHE", "onattach");
        mActivity = (Activity) context;

        browsingTime = new CastratedDate();

        //todo make sure these are correct

        regularPeriodBeginning = new HashMap<>(2);
        wednesdayPeriodBeginning = new HashMap<>(2);

        regularPeriodBeginning.put("THIS", new int[] {815, 855, 950, 1035, 1115, 1300, 1355, 1435});
        wednesdayPeriodBeginning.put("THIS", new int[] {815, 835, 900, 920, 1045, 1150, 1300, 1320});

        regularPeriodBeginning.put("ISB", new int[] {815, 950, 1155, 1225, 1400});
        wednesdayPeriodBeginning.put("ISB",new int[] {815, 945, 1110, 1305});





        SharedPreferences sp = getActivity().getSharedPreferences("clubs", Context.MODE_PRIVATE);
        school = sp.getString("school", "");


        date_params = new RadioGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        date_params.setMargins(25,0,25,0);
        wk_params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        wk_params.setMargins(25, 0, 25, 0);

        try {
            subjectTable = getSchedule();
            LogUtil.d("SCHE", "got");
        }
        catch(Exception e){
            LogUtil.d("ERR", "getSchedule failed");
        }

        focusTime = getFocusedDate();
        LogUtil.d("focustime",focusTime.toString());

    }

    /**
     * 取自https://blog.csdn.net/ljcitworld/article/details/77528585
     */
    @Override
    public void onStart() {
        super.onStart();


        //SharedPreferences sp = getActivity().getSharedPreferences("clubs", Context.MODE_PRIVATE);

        //school = sp.getString("school", "");

        refresh();
    }

    public void refresh(){

        //if(getUserVisibleHint()) {
        focusTime = getFocusedDate();

        browsingTime = new CastratedDate();

        LogUtil.d("sche_onstart","onstart called "+browsingTime.toString());

        updateTitleBar();

        //((RadioGroup)getView().findViewById(R.id.date_radio_group)).getChildAt(browsingTime.date-1).callOnClick();
        load();
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.schedule, container, false);

        ImageButton f = (ImageButton) root.findViewById(R.id.schedule_fetch);
        f.setOnClickListener(FETCH);
        final ImageButton l = (ImageButton) root.findViewById(R.id.schedule_left);
        l.setOnClickListener(LEFT);
        final ImageButton r = (ImageButton) root.findViewById(R.id.schedule_right);
        r.setOnClickListener(RIGHT);
        final Button excal = (Button) root.findViewById(R.id.expand_calendar);
        excal.setOnClickListener(EXPAND);

        cal = (LinearLayout)inflater.inflate(R.layout.calendar_popup, null);
        popup = new PopupWindow(cal, ViewGroup.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.WRAP_CONTENT);
        popup.setContentView(cal);
        //popup.setHeight(500);
        popup.setOutsideTouchable(false);

        setCalendarToMonth();

        TextView m = (TextView) root.findViewById(R.id.schedule_month);
        m.setText(month_text());

        listBackground = (ImageView) root.findViewById(R.id.schedule_list_background);

        LinearLayout daywk = (LinearLayout) root.findViewById(R.id.days_wk);
        RadioGroup rg = (RadioGroup) root.findViewById(R.id.date_radio_group);
        for(int i = 0; i < browsingTime.month_length(); i++){
            RadioButton rb = (RadioButton) mActivity.getLayoutInflater().inflate(R.layout.date_button, null);
            rb.setOnClickListener(DATE);
            rb.setText(Integer.toString(i+1));
            rb.setTag(i+1);
            rb.setLayoutParams(date_params);
            rg.addView(rb, i);

            TextView dwk = (TextView) mActivity.getLayoutInflater().inflate(R.layout.date_wk, null);
            dwk.setText(CastratedDate.dayInWeek(browsingTime.year, browsingTime.month, i+1).substring(0,1));
            dwk.setLayoutParams(wk_params);
            daywk.addView(dwk);
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


        //fix week day labels
        final HorizontalScrollView wkday = (HorizontalScrollView)root.findViewById(R.id.day_scroll);
        wkday.setOnTouchListener( new View.OnTouchListener(){
            @Override
            public boolean onTouch(View v, MotionEvent event){
                return true;
            }
        });

        final HorizontalScrollView mday = (HorizontalScrollView)root.findViewById(R.id.date_scroll);
        mday.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {
                int scrollX = mday.getScrollX();
                Log.d("RESCROL", Integer.toString(scrollX));
                wkday.setScrollX(scrollX);
            }
        });

        /*
        CalendarView wkcal = (CalendarView)root.findViewById(R.id.weekdaylabel);
        wkcal.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View v, MotionEvent event){
                return true;
            }
        });


        ScrollView datepicker = (ScrollView)root.findViewById(R.id.datepickerscroll);
        wkday.setOnTouchListener( new View.OnTouchListener(){
            @Override
            public boolean onTouch(View v, MotionEvent event){
                return true;
            }
        });


        CalendarView cal = (CalendarView)root.findViewById(R.id.datepicker);
        cal.setOnDateChangeListener( new CalendarView.OnDateChangeListener(){
            public void onSelectedDayChange(CalendarView calendarView, int year, int month, int dayOfMonth){
                browsingTime.set(Calendar.DATE, dayOfMonth);
                //autoscroll?
                load();
            }
        });
        MyScrollView dpscroll = (MyScrollView)root.findViewById(R.id.datepickerscroll);
*/

        lv = (ListView) root.findViewById(R.id.schedule_list);
        lv.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                unitHeight = (int)(lv.getHeight()*0.25);
                lv.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                refresh();
            }
        });
        lv.setOnTouchListener(new OnSwipeTouchListener(mActivity) {
            /*
            public void onSwipeTop(){
                if(expanded) {
                    excal.callOnClick();
                }
            }

            public void onSwipeBottom(){
                if(!expanded) {
                    excal.callOnClick();
                }
            }
            */

            public void onSwipeRight() {
                if(!expanded) {
                    //decrease date
                    int month = browsingTime.month;
                    browsingTime.change(Calendar.DATE, -1);
                    if(browsingTime.month != month)
                        updateTitleBar();
                    else
                        autoscroll();
                    load();
                }
                else{
                    l.callOnClick();
                }
            }

            public void onSwipeLeft() {
                //increase date
                if(!expanded) {
                    int month = browsingTime.month;
                    browsingTime.change(Calendar.DATE, 1);
                    if(browsingTime.month != month)
                        updateTitleBar();
                    else
                        autoscroll();
                    load();
                }
                else{
                    r.callOnClick();
                }
            }
        });

        return root;
    }
    private void autoscroll(){
        HorizontalScrollView date_scroll = (HorizontalScrollView)getView().findViewById(R.id.date_scroll);
        RadioGroup rg = (RadioGroup) getView().findViewById(R.id.date_radio_group);
        RadioButton rb = (RadioButton) rg.getChildAt(browsingTime.date-1);
        LogUtil.d("SCROLL",browsingTime.date + " "+ rb.isChecked());
        if(!rb.isChecked())
            rb.setChecked(true);

        date_scroll.smoothScrollTo(rg.getChildAt(browsingTime.date-1).getLeft()
                + rg.getChildAt(browsingTime.date-1).getMeasuredWidth()/2
                + rg.getChildAt(browsingTime.date-1).getPaddingLeft()
                - date_scroll.getMeasuredWidth()/2, 0);
        LogUtil.d("SCROLL", "autoscrolled");
        LogUtil.d("SCROLL", browsingTime.toString());
    }

    private CastratedDate getFocusedDate(){

        int endTime = (browsingTime.get(Calendar.DAY_OF_WEEK) == Calendar.WEDNESDAY) ?
                wednesdayPeriodBeginning.get(school)[wednesdayPeriodBeginning.get(school).length-1]
                : regularPeriodBeginning.get(school)[regularPeriodBeginning.get(school).length-1];
        CastratedDate temp = new CastratedDate();
        if(CastratedDate.getHourMinute() > endTime){
            temp.change(Calendar.DATE, 1);
        }

        int count = 0;
        //todo read schedule of date c and return if it is has classes, currently it only returns today or tomorrow
        while(subjectTable.get(temp.toString()) == null && count < 365){
            temp.change(Calendar.DATE, 1);
            count++;
        }

        LogUtil.d("focus_time",temp.toString());

        return temp;
    }

    //append or remove number buttons to fit the new month length
    public void updateTitleBar(){
        View root = getView();
        TextView m = (TextView) root.findViewById(R.id.schedule_month);
        m.setText(month_text());

        RadioGroup rg = (RadioGroup) root.findViewById(R.id.date_radio_group);
        LinearLayout daywk = (LinearLayout) root.findViewById(R.id.days_wk);

        if(browsingTime.month_length() > last_date_length){
            for(int i = last_date_length; i < browsingTime.month_length(); i++){
                RadioButton rb = (RadioButton) mActivity.getLayoutInflater().inflate(R.layout.date_button, rg, false);
                rb.setOnClickListener(DATE);
                rb.setText(Integer.toString(i+1));
                rb.setTag(Integer.valueOf(i+1));
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

        }
        daywk.removeAllViews();
        for(int i=0; i < browsingTime.month_length(); i++){
            TextView dwk = (TextView) mActivity.getLayoutInflater().inflate(R.layout.date_wk, null);
            dwk.setText(CastratedDate.dayInWeek(browsingTime.year, browsingTime.month, i+1).substring(0,1));
            dwk.setLayoutParams(wk_params);
            daywk.addView(dwk);
        }
        //if(browsingTime.date == browsingTime.month_length())
        ((RadioButton)rg.getChildAt(browsingTime.date-1)).setChecked(true);
        autoscroll();
        last_date_length = browsingTime.month_length();
        for(int i = 0; i < rg.getChildCount(); i++){
            if(((RadioButton)rg.getChildAt(i)).isChecked())
                rg.getChildAt(i).callOnClick();
        }

        setCalendarToMonth();
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

    public void setCalendarToMonth(){
        int year = browsingTime.year;
        int month = browsingTime.month;
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, 1);
        calendar.set(Calendar.DATE, calendar.getActualMaximum(Calendar.DATE));
        calendar.set(Calendar.HOUR_OF_DAY, 23);//not sure this is needed

        long endOfMonth = calendar.getTimeInMillis();

        //may need to reinitialize calendar, not sure
        calendar = Calendar.getInstance();
        calendar.set(year, month, 1);
        calendar.set(Calendar.DATE, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        long startOfMonth = calendar.getTimeInMillis();

        cal.removeAllViews();
        expcal = (CalendarView)mActivity.getLayoutInflater().inflate(R.layout.mycalendar, null);
        expcal.setOnDateChangeListener(new CalendarView.OnDateChangeListener(){
            @Override
            public void onSelectedDayChange(CalendarView view, int year, int month, int dayOfMonth){
                if(month!=browsingTime.month){
                    browsingTime.set(Calendar.YEAR, year);
                    browsingTime.set(Calendar.MONTH, month);
                    browsingTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    updateTitleBar();
                }
                else{
                    browsingTime.set(Calendar.YEAR, year);
                    browsingTime.set(Calendar.MONTH, month);
                    browsingTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    autoscroll();
                }
                load();
            }
        });
        cal.addView(expcal);
        expcal.setMaxDate(endOfMonth);
        expcal.setMinDate(startOfMonth);
    }

    public void setCalendarToDay(){
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, browsingTime.year);
        calendar.set(Calendar.MONTH, browsingTime.month);
        calendar.set(Calendar.DAY_OF_MONTH, browsingTime.date);
        long date = calendar.getTimeInMillis();
        expcal.setDate(date);
    }

    private String month_text(){return browsingTime.month_name();}

    public HashMap<String, Subject[]> getSchedule() throws Exception{
        HashMap<String, Subject[]> schedule = new HashMap<>(6);
        HashMap<String, Integer> pairs             = getDateDayPairs();
        HashMap<Integer, Subject[]> weeklySchedule = readWeeklySchedule();
        for(Map.Entry<String, Integer> keyValuePair : pairs.entrySet()){
            String date = keyValuePair.getKey();
            Integer day = keyValuePair.getValue();
            if(day != -1) schedule.put(date, weeklySchedule.get(day));
            else schedule.put(date, null);
        }
        LogUtil.d("SCHEDULE", "got schedule");
        return schedule;
    }

    public HashMap<String, Integer> getDateDayPairs()throws AVException, ParseException, ClassNotFoundException{
        HashMap<String, Integer> pairs;
        try{
            pairs = readDateDayPairs();
            LogUtil.d("CALENDAR", "date-day pairs read success");
        }catch(IOException e) {
            LogUtil.d("CALENDAR", "read failed; using default");
            pairs = defaultDateDayPairs("2018-08-27");
            LogUtil.d("CALENDAR", "date-day pairs defaulted");
        }
        return pairs;
    }

    public HashMap<String, Integer> defaultDateDayPairs(String startOfYear) throws ParseException, AVException {
        HashMap<String, Integer> pairs = new HashMap<>(0);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar c = Calendar.getInstance();
        c.setTime(sdf.parse(startOfYear));
        for(int i = 0; i < 365; i++){
            String date = sdf.format(c.getTime());
            pairs.put(date, -1);
            c.add(Calendar.DATE, 1);
        }
        return pairs;
    }

    public HashMap<String, Integer> readDateDayPairs() throws IOException, ClassNotFoundException{
        FileInputStream f = mActivity.openFileInput("date_day.dat");
        ObjectInputStream s = new ObjectInputStream(f);
        LogUtil.d("CALENDAR", "reading date-day pairs");
        HashMap<String, Integer> dateDay = (HashMap<String, Integer>)s.readObject();
        s.close();
        return dateDay;
    }

    public HashMap<Integer, Subject[]> readWeeklySchedule() throws IOException{
        FileInputStream f = mActivity.openFileInput("week_schedule.dat");
        BufferedReader in = new BufferedReader(new InputStreamReader(f));
        HashMap<Integer, Subject[]> schedule = new HashMap<>(0);
        LogUtil.d("SCHEDULE", "reading weekly schedule");
        String line;
        int dayInCycle = 1;
        while((line = in.readLine())!=null){
            StringTokenizer tizer = new StringTokenizer(line, "?");
            //todo change period number according to school
            Subject[] dailySchedule = new Subject[tizer.countTokens()/3];
            for(int period = 0; period < dailySchedule.length; period ++){
                String name = tizer.nextToken();
                String teacher = tizer.nextToken();
                String room = tizer.nextToken();
                Subject subject = new Subject(name, teacher, room);
                dailySchedule[period] = subject;
            }
            schedule.put(dayInCycle, dailySchedule);
            dayInCycle ++;
        }
        in.close();
        LogUtil.d("SCHEDULE", "successfully read weekly schedule");
        return schedule;
    }
}
