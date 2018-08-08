package hackthis.team.spartapp;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ArrayAdapter;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class Schedule extends Fragment {

    private Activity mActivity;

    public boolean isLoggedIn = true;

    private CastratedDate browsingTime; //the date for the exhibited schedule
    private CastratedDate focusTime; //the date that contains the next period (will be enlarged)
                                        //this variable sets to the next school day
    private CastratedDate realTime; //the current time

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
        Subject[] subs = {
                new Subject ("Period 1", "Mr. 1", "Room 1"), null,
                new Subject ("Period 2", "Mr. 2", "Room 2"), null,
                new Subject ("Period 3", "Mr. 3", "Room 3"), null,
                new Subject ("Period 4", "Mr. 4", "Room 4"), null,
        };

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
        mActivity = (Activity) context;

        browsingTime = new CastratedDate();
        realTime = new CastratedDate();

        date_params = new RadioGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        date_params.setMargins(15,0,15,0);
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
}
