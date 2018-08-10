package hackthis.team.spartapp;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.FindCallback;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Announcement extends Fragment {

    private Activity mActivity;

    View.OnClickListener SEARCH = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            EditText et = (EditText) getView().findViewById(R.id.announcement_search_text);
            if(!et.getText().toString().equals("") || !et.getText().toString().equals(" ")){
                filter_keyword(et.getText().toString());
            }
        }
    };

    View.OnClickListener FILTER = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent f = new Intent(mActivity, FilterActivity.class);
            startActivity(f);
        }
    };

    ListView list;
    ArrayList<Content> announcements;

    /**
     * 取自https://blog.csdn.net/ljcitworld/article/details/77528585
     */
    @Override
    public void onStart() {
        super.onStart();
        if(getUserVisibleHint()) {
            update_list();
        }
    }

    public void update_list(){
        Log.d("filter_activity","update called");
        AVQuery<AVObject> query = new AVQuery<>("Announcements");

        SharedPreferences sp = mActivity.getSharedPreferences("clubs",Context.MODE_PRIVATE);
        Set<String> subscribed_names = sp.getStringSet("subscribed",null);
        if(subscribed_names == null){
            subscribed_names = new HashSet<>(Arrays.asList("Student Council", "{Hack,THIS}"));
            sp.edit().putStringSet("subscribed", subscribed_names).apply();
        }
        ArrayList<String> club_names = new ArrayList<>(subscribed_names);

        query.whereContainedIn("clubName",club_names);
        Toast.makeText(mActivity, "loading...",Toast.LENGTH_SHORT).show();
        Log.d("announcement_adapter","update called");
        query.findInBackground(new FindCallback<AVObject>() {
            @Override
            public void done(List<AVObject> list, AVException e) {
                if(e == null){
                    announcements = new ArrayList<>(50);
                    for(int i = 0; i < list.size(); i++)
                        announcements.add(new Content(list.get(i)));
                    Log.d("announcement_adapter",announcements.toString());
                    EditText et = (EditText) getView().findViewById(R.id.announcement_search_text);
                    if(!et.getText().toString().equals("") || !et.getText().toString().equals(" ")){
                        filter_keyword(et.getText().toString());
                    }
                    else{
                        filter_keyword(null);
                    }
                }
                else{
                    Toast t = Toast.makeText(mActivity,
                            "Error occured when loading announcements, please connect to the internet", Toast.LENGTH_LONG);
                    t.setGravity(Gravity.CENTER, 0,0);
                    t.show();
                }
            }
        });
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
        ImageView search = (ImageView) root.findViewById(R.id.announcement_search);
        search.setOnClickListener(SEARCH);
        ImageView filter = (ImageView) root.findViewById(R.id.announcement_filter);
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
