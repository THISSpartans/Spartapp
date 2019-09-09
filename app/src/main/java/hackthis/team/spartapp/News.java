package hackthis.team.spartapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import cn.leancloud.AVException;
import cn.leancloud.AVFile;
import cn.leancloud.AVOSCloud;
import cn.leancloud.AVObject;
import cn.leancloud.AVQuery;
import cn.leancloud.callback.FindCallback;
import cn.leancloud.convertor.ObserverBuilder;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

public class News extends RefreshableFragment {

    ListView list;
    ArrayList<news_element> content = new ArrayList<>();

    private Context mActivity;

    /*
    returns if there are new elements
     */
    private boolean getStuff() {
        AVQuery<AVObject> news = new AVQuery<>("News_Complex");
        //news.orderByDescending("updatedAt");
        news = news.orderByDescending("createdAt");
        news.findInBackground().subscribe(new Observer<List<AVObject>>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(List<AVObject> avObjects) {
                updateNews(avObjects);
                news_adapter NA = new news_adapter(mActivity, content);
                list.setAdapter(NA);
                NA.notifyDataSetChanged();
                LogUtil.d("news", "news download complete: " + avObjects.size() + " items fetched");
            }

            @Override
            public void onError(Throwable e) {
                Toast t = Toast.makeText(mActivity,
                        "Error, please connect to the internet", Toast.LENGTH_LONG);
                t.setGravity(Gravity.CENTER, 0, 0);
                t.show();
            }

            @Override
            public void onComplete() {

            }
        });

        return true;
    }

    public void updateNews(List<AVObject> news) {
        LogUtil.d("news", "updatenews() called with " + news.size() + " items:\n" + news.toString());
        content = new ArrayList<>(news.size());

        int lastMonth=0, lastDate=0;

        for (int i = 0; i < news.size(); i++) {
            try {
                //leancloud uses xxxx-xx-xx-abcdeaoepfjopae as time, pick first four digits as year, 5~6 as month, etc.
                String dateStr = news.get(i).getCreatedAt();
                int date = Integer.parseInt(dateStr.substring(8, 10));
                int month = Integer.parseInt(dateStr.substring(5, 7));

                String body = news.get(i).getString("Description");
                String title = news.get(i).getString("Title");
                AVFile attached = news.get(i).getAVFile("Attached");
                String url = news.get(i).getString("url");
                String author = news.get(i).getString("author");

                LogUtil.d("news", title + " " + url + " " + date);

                content.add(new news_element(
                        (i <= 0 || (lastMonth != month || lastDate != date)), month, date, title, body, author, attached, url
                ));

                lastMonth = month;
                lastDate = date;
            } catch (Exception e1) {
                String e1str = e1.getLocalizedMessage();
                LogUtil.d("news", e1str);
            }
        }
    }

    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = context;
        //refresh();
    }

    public void onStart() {
        super.onStart();
        //display();
        getStuff();
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.news_page, container, false);
        list = root.findViewById(R.id.news_list);
        return root;
    }

    public void refresh() {
        getStuff();
    }

    public void display() {
        //re-download news and call ondatachange
        news_adapter NA = new news_adapter(mActivity, content);
        list.setAdapter(NA);
        NA.notifyDataSetChanged();

    }

    private class news_adapter extends BaseAdapter {

        ArrayList<news_element> item;

        private news_adapter(Context context, ArrayList<news_element> item) {
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

        private class Hol {
            TextView d;
            TextView b;
            ImageView image;
            String url;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View itemView;
            Hol holder;
            if (convertView == null) {
                itemView = View.inflate(mActivity, R.layout.news_block, null);
                holder = new Hol();
                holder.d = itemView.findViewById(R.id.news_date);
                holder.b = itemView.findViewById(R.id.news_body);
                holder.image = itemView.findViewById(R.id.news_image);
                //用setTag的方法把ViewHolder与convertView "绑定"在一起
                itemView.setTag(holder);

                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(((Hol)(v.getTag())).url != null) {
                            Intent readNews = new Intent(mActivity, NewsActivity.class);
                            readNews.putExtra("url", ((Hol) v.getTag()).url);
                            mActivity.startActivity(readNews);

                            LogUtil.d("news","shit's called from" + v.toString());
                        }
                        else{
                            Toast t = Toast.makeText(mActivity, "no more to show",Toast.LENGTH_SHORT);
                            t.show();
                        }
                    }
                });

            } else {
                //当不为null时，我们让itemView=convertView，用getTag方法取出这个itemView对应的holder对象，就可以获取这个itemView对象中的组件
                itemView = convertView;
                holder = (Hol) itemView.getTag();
            }

            news_element i = getItem(position);
            if (i.date != null) {
                holder.d.setText(i.date);
            } else {
                holder.d.setText("");
            }
            holder.b.setText(i.body);
            holder.url = i.url;
            if(i.attach!=null) {
                LogUtil.d("news_browser","looking at '"+i.title+"' url: "+i.attach.getUrl());
                Picasso.get().load(i.attach.getUrl())
                        .error(R.drawable.wifi_disconnected)
                        .into(holder.image);
            }
            else {
                LogUtil.d("news_browser","looking at '"+i.title+"'without url");
                holder.image.setImageDrawable(null);
            }
            return itemView;
        }
    }



    private class news_element {
        SpannableStringBuilder date;
        SpannableStringBuilder body;
        String title;

        String[] week_day_name =
                {
                        "Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat",
                };

        String[] month_short =
                {
                        "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sept", "Oct", "Nov", "Dec"
                };
        AVFile attach;
        String url;

        private news_element(boolean includeDate, int m,int d, String title, @Nullable String content, @Nullable String author, @Nullable AVFile attach, @Nullable String url) {
            this.title = title;

            this.attach = attach;
            this.url = url;
            if (includeDate) {
                //concatenate date string
                String str = month_short[m - 1] + ". " + d;
                date = new SpannableStringBuilder(str);

                if (d == new GregorianCalendar().get(Calendar.DAY_OF_MONTH)
                        && m == (new GregorianCalendar().get(Calendar.MONTH) + 1)) {//the first month is 0
                    //purple date text
                    date.setSpan(new ForegroundColorSpan(mActivity.getResources().getColor(R.color.purple)),
                            0, str.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                } else {
                    date.setSpan(new ForegroundColorSpan(mActivity.getResources().getColor(R.color.grey)),
                            0, str.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                    //grey date text
                    //}
                }
            }

                body = new SpannableStringBuilder(title + "\n" + (author==null?"":author) + "\n    "+ (content==null?"":content));
                //body.setSpan(new StyleSpan(Typeface.BOLD), 0, title.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                body.setSpan(new RelativeSizeSpan(1.3f), 0, title.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                body.setSpan(new ForegroundColorSpan(mActivity.getResources().getColor(R.color.black)),
                        0, title.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);

                body.setSpan(new ForegroundColorSpan(mActivity.getResources().getColor(R.color.grey)),
                        title.length(), title.length() + ((content==null)?0:content.length()), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);

        }
        public String toString(){
            return title;
        }
    }
}
