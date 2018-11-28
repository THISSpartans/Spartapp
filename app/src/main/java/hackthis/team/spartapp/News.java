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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

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

        content = new ArrayList<>(3);
        content.add(new news_element(
                new CastratedDate(), "Title","cyka blyat idinahui, p90 rush b, vodka"
        ));
        content.add(new news_element(
                new CastratedDate(), "Title","cyka blyat idinahui, p90 rush b, vodka"
        ));
        content.add(new news_element(
                new CastratedDate(), "Title","cyka blyat idinahui, p90 rush b, vodka"
        ));

        //remove repeated dates
        for(int i = content.size()-1; i > 0; i--){
            if(content.get(i).date.toString().equals(
                    content.get(i-1).date.toString()
            )){
                content.get(i).date = null;
            }
        }

        return true;
    }

    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = context;
        //refresh();
    }

    public void onStart(){
        super.onStart();
        display();
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
            holder.b.setText(i.body);

            //itemView.setOnClickListener(selectDate);

            return itemView;
        }
    }

    private static class news_element{
        SpannableStringBuilder date;
        SpannableStringBuilder body;

        String week_day_name[] =
                {
                        "Sun","Mon","Tue","Wed","Thu","Fri","Sat",
                };

        public news_element(@Nullable CastratedDate d, String title, String content){
            if(d == null){
                //no date text
            }
            else{

                //concatenate date string
                String str = d.date + "\n" + week_day_name[d.get(Calendar.DAY_OF_WEEK)-1];

                if(d.equals(new CastratedDate())){
                    //purple date text
                    date = new SpannableStringBuilder(str);
                    date.setSpan(new ForegroundColorSpan(mActivity.getResources().getColor(R.color.purple)),
                            0,str.length(),Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                }
                else{
                    date = new SpannableStringBuilder(str);
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
