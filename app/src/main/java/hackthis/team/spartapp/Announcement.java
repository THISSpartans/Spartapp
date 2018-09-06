package hackthis.team.spartapp;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Fragment;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVInstallation;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.FindCallback;
import com.avos.avoscloud.PushService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Announcement extends RefreshableFragment {

    private Activity mActivity;

    View.OnClickListener SEARCH = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            EditText et = (EditText) getView().findViewById(R.id.announcement_search_text);

        }
    };

    View.OnClickListener FILTER = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent f = new Intent(mActivity, FilterActivity.class);
            startActivity(f);
        }
    };

    EditText search_text;
    AlertDialog dateDialog;
    TimePicker timePicker;
    DatePicker datePicker;

    String search_root;

    int grade;

    ListView list;
    ArrayList<Content> announcements;

    Intent noteIntent;

    boolean isStudent = true;

    /**
     * 取自https://blog.csdn.net/ljcitworld/article/details/77528585
     */
    @Override
    public void onStart() {
        super.onStart();
        update_list();
    }

    //取自https://blog.csdn.net/u013278099/article/details/72869175
    public void refresh(){
        update_list();
    }

    public void update_list(){

        Log.d("filter_activity","update called");

        if(getActivity()!= null) {

            SharedPreferences sp = getActivity().getSharedPreferences("clubs", Context.MODE_PRIVATE);

            grade = sp.getInt("grade", 9);

            search_root = sp.getString("school", "");
            isStudent = sp.getString("occupation","student").equals("student");
            if (search_root.equals("THIS")) {
                search_root = "";
            } else {
                search_root = "_" + search_root;
            }

            //todo add default subscriptions for ISB (?)
            Set<String> subscribed_names = sp.getStringSet("subscribed", null);
            if (subscribed_names == null) {
                subscribed_names = search_root.equals("") ? new HashSet<>(Arrays.asList("Student Council", "{Hack,THIS}"))
                        : new HashSet<>(Arrays.asList("test_org"));
                sp.edit().putStringSet("subscribed", subscribed_names).apply();
                PushService.subscribe(mActivity, "StudentCouncil", MainActivity.class);
                PushService.subscribe(mActivity, "HackTHIS", MainActivity.class);
                AVInstallation.getCurrentInstallation().saveInBackground();
            }


            ArrayList<String> club_names = new ArrayList<>(subscribed_names);

            AVQuery<AVObject> query = new AVQuery<>("Announcements" + search_root);

            query.whereContainedIn("clubName", club_names);
            AVQuery<AVObject> combined_query;
            if(isStudent) {
                AVQuery<AVObject> query2 = new AVQuery<>("Announcements" + search_root);
                query2.whereEqualTo("gradeLevel", grade);
                Log.d("announcement_adapter", "update called");
                combined_query = AVQuery.and(Arrays.asList(query, query2));
            }
            else
                combined_query = query;
            combined_query.orderByDescending("updatedAt");
            combined_query.findInBackground(new FindCallback<AVObject>() {
                @Override
                public void done(List<AVObject> list, AVException e) {
                    if (e == null) {
                        announcements = new ArrayList<>(50);
                        for (int i = 0; i < list.size(); i++)
                            announcements.add(new Content(list.get(i)));
                        Log.d("announcement_adapter", announcements.toString());
                        if (!search_text.getText().toString().equals("") || !search_text.getText().toString().equals(" ")) {
                            filter_keyword(search_text.getText().toString());
                        } else {
                            filter_keyword(null);
                        }
                    } else {
                        Toast t = Toast.makeText(mActivity,
                                "Error, please connect to the internet", Toast.LENGTH_LONG);
                        t.setGravity(Gravity.CENTER, 0, 0);
                        t.show();
                    }
                }
            });
        }
    }

    public void filter_keyword(String keyword){
        AnnouncementAdapter adapter = new AnnouncementAdapter(getActivity(), announcements);
        list.setAdapter(adapter);
        if(keyword != null){
            adapter.filter(keyword);
        }
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {
                CastratedDate cd = new CastratedDate();
                datePicker.updateDate(cd.year, cd.month, cd.date);
                timePicker.setHour(cd.get(Calendar.HOUR_OF_DAY));
                timePicker.setMinute(cd.get(Calendar.MINUTE));
                dateDialog.show();
                AnnouncementAdapter.infoHolder ih = (AnnouncementAdapter.infoHolder)view.getTag();
                noteIntent = new Intent(mActivity, AlarmReceiver.class);
                noteIntent.putExtra("title", ih.name);
                //mActivity.sendBroadcast(intent);
            }
        });
    }

    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = (Activity) context;

        final View dialogView = View.inflate(context, R.layout.date_time_picker, null);
        dateDialog = new AlertDialog.Builder(context).
                setTitle("Set Alarm Time").create();

        datePicker = dialogView.findViewById(R.id.date_picker);
        timePicker = dialogView.findViewById(R.id.time_picker);

        dialogView.findViewById(R.id.date_time_set).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Calendar calendar = new GregorianCalendar(datePicker.getYear(),
                        datePicker.getMonth(),
                        datePicker.getDayOfMonth(),
                        timePicker.getCurrentHour(),
                        timePicker.getCurrentMinute());
                long time = calendar.getTimeInMillis();

                AlarmManager alarmManager = (AlarmManager) mActivity.getSystemService(Context.ALARM_SERVICE);

                PendingIntent pi = PendingIntent.getBroadcast(mActivity, 0, noteIntent, 0);
                alarmManager.set(AlarmManager.RTC, time, pi);
                dateDialog.hide();
                Log.d("alarm","alarm set after"+Long.toString(time-(new Date()).getTime())+" "+noteIntent.getStringExtra("title"));
            }});

        dialogView.findViewById(R.id.date_time_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dateDialog.hide();
            }
        });
        dateDialog.setView(dialogView);

    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.announcement, container, false);
        ImageView filter = (ImageView) root.findViewById(R.id.announcement_filter);
        search_text = (EditText) root.findViewById(R.id.announcement_search_text);
        search_text.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(!search_text.getText().toString().equals("") || !search_text.getText().toString().equals(" ")){
                    filter_keyword(search_text.getText().toString());
                }
            }
        });
        final SwipeRefreshLayout srl = (SwipeRefreshLayout) root.findViewById(R.id.announcement_swipe_refresher);
        srl.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                update_list();
                list.smoothScrollToPosition(0);
                srl.setRefreshing(false);
            }
        });
        filter.setOnClickListener(FILTER);
        list = (ListView)root.findViewById(R.id.announcement_list);
        return root;
    }

    public static class Content{
        String name;
        String club;
        CastratedDate date;
        String body;
        Content(AVObject obj){
            name = obj.getString("announcementTitle");
            club = obj.getString("clubName");
            date = new CastratedDate(obj.getDate("updatedAt"));
            body = obj.getString("announcementBody");
        }
        public String toString(){return name;}
    }

}
