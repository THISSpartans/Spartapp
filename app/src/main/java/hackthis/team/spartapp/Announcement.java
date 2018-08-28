package hackthis.team.spartapp;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVInstallation;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.FindCallback;
import com.avos.avoscloud.PushService;

import java.util.ArrayList;
import java.util.Arrays;
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

    String search_root;

    int grade;

    ListView list;
    ArrayList<Content> announcements;

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

            AVQuery<AVObject> query2 = new AVQuery<>("Announcements" + search_root);
            query2.whereEqualTo("gradeLevel", grade);
            Log.d("announcement_adapter", "update called");
            AVQuery<AVObject> combined_query = AVQuery.and(Arrays.asList(query, query2));
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
        AnnouncementAdapter adapter = new AnnouncementAdapter(mActivity, announcements);
        list.setAdapter(adapter);
        if(keyword != null){
            adapter.filter(keyword);
        }
    }

    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = (Activity) context;
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
