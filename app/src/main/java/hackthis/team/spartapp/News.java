package hackthis.team.spartapp;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVLiveQuery;
import com.avos.avoscloud.AVLiveQueryEventHandler;
import com.avos.avoscloud.AVLiveQuerySubscribeCallback;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.FindCallback;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class News extends RefreshableFragment {

    ListView list;
    ArrayList<news_element> content = new ArrayList<>();

    static private Context mActivity;

    /*
    returns if there are new elements
     */
    private boolean getStuff(){
        //todo internet, remember to do time order (recent -> far)
        AVQuery<AVObject> news = new AVQuery<>("News");
        //news.orderByDescending("updatedAt");
        news = news.orderByDescending("date"); //
        news.findInBackground(new FindCallback<AVObject>() {
            @Override
            public void done(List<AVObject> list_, AVException e) {
                if (e == null) {
                    updateNews(list_);
                    news_adapter NA = new news_adapter(mActivity, content);
                    list.setAdapter(NA);
                    NA.notifyDataSetChanged();
                } else {
                    Toast t = Toast.makeText(mActivity,
                            "Error, please connect to the internet", Toast.LENGTH_LONG);
                    t.setGravity(Gravity.CENTER, 0, 0);
                    t.show();
                }
            }
        });

        /*AVLiveQuery livenews = AVLiveQuery.initWithQuery(news);
        livenews.setEventHandler(new AVLiveQueryEventHandler() {
            public void done(AVLiveQuery.EventType eventType, AVObject avObject, List<String> updateKeyList) {
                // 事件回调，有更新后会调用此回调函数

            }
            public void onObjectCreated(AVObject avObject){
                //new item
            }
        });
        livenews.subscribeInBackground(new AVLiveQuerySubscribeCallback() {
            @Override
            public void done(AVException e) {
                if (null == e) {
                    // 订阅成功
                }
            }
        });
*/

        return true;
    }

    public void updateNews(List<AVObject> news){
        content = new ArrayList<>(news.size());
        ArrayList<String> datesIncluded = new ArrayList<>(0);
        for(AVObject newsitem: news){
            int dateInt = newsitem.getInt("date");
            String date = Integer.toString(dateInt);
            int year = Integer.parseInt(date.substring(0, 4));
            int month = Integer.parseInt(date.substring(4,6));
            int day = Integer.parseInt(date.substring(6, 8));
            String body = newsitem.getString("body");
            String title = newsitem.getString("title");
            Log.d("TITLEAV", date);
                content.add(new news_element(
                        new CastratedDate(year, month - 1, day), title, body
                ));
        }
        //remove repeated dates
        for(int i = content.size()-1; i > 0; i--){
            if(content.get(i).date.toString().equals(
                    content.get(i-1).date.toString()
            )){
                content.get(i).date = null;
                Log.d("nullified", content.get(i).title);
            }
        }
    }

    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = context;
        //refresh();
    }

    public void onStart(){
        super.onStart();
        //display();
        getStuff();
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.news_page, container, false);
        list = (ListView) root.findViewById(R.id.news_list);
        return root;
    }

    public void refresh(){}

    public void display(){
        //re-download news and call ondatachange
        if(getStuff()){
            news_adapter NA = new news_adapter(mActivity, content);
            list.setAdapter(NA);
            NA.notifyDataSetChanged();
        }
        else{
            if(content.isEmpty()){
                //todo check internet warning
            }
        }
    }

    private static class news_adapter extends BaseAdapter{

        ArrayList<news_element> item;

        public news_adapter(Context context, ArrayList<news_element> item) {
            super();
            this.item = item;
        }

        //因为要展示的是过滤后的数据，所以是displayItem的一些属性
        @Override
        public int getCount() {
            return item.size();
        }

        @Override
        public news_element getItem(int position) {
            return item.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        private class Hol{
            TextView d;
            TextView b;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View itemView = null;
            Hol holder;
            if (convertView == null) {
                itemView = View.inflate(mActivity, R.layout.news_block, null);
                holder = new Hol();
                holder.d = (TextView) itemView.findViewById(R.id.news_date);
                holder.b = (TextView) itemView.findViewById(R.id.news_body);
                //用setTag的方法把ViewHolder与convertView "绑定"在一起
                itemView.setTag(holder);
            } else {
                //当不为null时，我们让itemView=converView，用getTag方法取出这个itemView对应的holder对象，就可以获取这个itemView对象中的组件
                itemView = convertView;
                holder = (Hol) itemView.getTag();
            }

            news_element i = getItem(position);
            if(i.date != null) {
                holder.d.setText(i.date);
            }
            else{
                holder.d.setText("");
            }
            holder.b.setText(i.body);

            //itemView.setOnClickListener(selectDate);

            return itemView;
        }
    }

    private static class news_element{
        SpannableStringBuilder date;
        SpannableStringBuilder body;
        String title;

        String week_day_name[] =
                {
                        "Sun","Mon","Tue","Wed","Thu","Fri","Sat",
                };

        public news_element(@Nullable CastratedDate d, String title, String content){
            this.title = title;
            if(d == null){
                //no date text

            }
            else{
                //concatenate date string
                String str = d.get(Calendar.DATE) + "\n" + week_day_name[d.get(Calendar.DAY_OF_WEEK)-1];
                String str1 = new CastratedDate().get(Calendar.DATE) + "\n" + week_day_name[new CastratedDate().get(Calendar.DAY_OF_WEEK)-1];
                date = new SpannableStringBuilder(str);
                if(d.get(Calendar.DATE) == new CastratedDate().get(Calendar.DATE)
                        && d.get(Calendar.MONTH) == new CastratedDate().get(Calendar.MONTH)){
                    //purple date text
                    date.setSpan(new ForegroundColorSpan(mActivity.getResources().getColor(R.color.purple)),
                            0,str.length(),Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                }
                else{
                    date.setSpan(new ForegroundColorSpan(mActivity.getResources().getColor(R.color.grey)),
                            0,str.length(),Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                    //grey date text
                }
            }

            body = new SpannableStringBuilder(title + "\n" + content);
            body.setSpan(new StyleSpan(Typeface.BOLD), 0,title.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            body.setSpan(new RelativeSizeSpan(1.5f), 0,title.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            body.setSpan(new ForegroundColorSpan(mActivity.getResources().getColor(R.color.black)),
                    0,title.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);

            body.setSpan(new ForegroundColorSpan(mActivity.getResources().getColor(R.color.grey)),
                    title.length(),title.length()+content.length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
        }
    }
}
