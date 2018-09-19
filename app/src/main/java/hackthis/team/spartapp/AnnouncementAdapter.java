package hackthis.team.spartapp;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.DatePicker;
import android.widget.Filter;
import android.widget.TextView;
import android.widget.TimePicker;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class AnnouncementAdapter extends BaseAdapter {
    //必须存放两个String[]类型数据，一个保存原始数据，一个用来展示过滤后的数据
    private ArrayList<Announcement.Content> item;
    private ArrayList<Announcement.Content> displayItem;
    Context context;

    String alarmName;

    AlertDialog dateDialog;


    DatePicker datePicker;
    TimePicker timePicker;

    View.OnClickListener selectDate = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            infoHolder ih = (infoHolder)v.getTag();
            alarmName = ih.name;

            CastratedDate cd = new CastratedDate();
            datePicker.init(cd.year, cd.month, cd.date, null);
            timePicker.setIs24HourView(true);
            timePicker.setHour(cd.get(Calendar.HOUR_OF_DAY));
            timePicker.setMinute(cd.get(Calendar.MINUTE));
            dateDialog.show();

            /*
            Intent i = new Intent();
            //i.setAction("hackthis.team.spartapp.TIMER_ACTION");
            i.setAction(AlarmReceiver.TIMER_ACTION);
            i.putExtra("title",ih.name);
            LogUtil.d("notification",i.getAction());
            */

        }
    };

    public AnnouncementAdapter(Context context, ArrayList<Announcement.Content> item) {
        super();
        this.item = item;
        displayItem = item;
        this.context = context;

    }

    //因为要展示的是过滤后的数据，所以是displayItem的一些属性
    @Override
    public int getCount() {
        return displayItem.size();
    }

    @Override
    public Announcement.Content getItem(int position) {
        return displayItem.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        infoHolder holder;
        View itemView = null;
        if (convertView == null) {
            itemView = View.inflate(context, R.layout.announcement_item, null);
            holder = new infoHolder();
            holder.title = (TextView) itemView.findViewById(R.id.announcement_item_title);
            holder.subtitle = (TextView) itemView.findViewById(R.id.announcement_item_subtitle);
            holder.body = (JustifyTextView) itemView.findViewById(R.id.announcement_item_body);
            //用setTag的方法把ViewHolder与convertView "绑定"在一起
            itemView.setTag(holder);
        } else {
            //当不为null时，我们让itemView=converView，用getTag方法取出这个itemView对应的holder对象，就可以获取这个itemView对象中的组件
            itemView = convertView;
            holder = (infoHolder) itemView.getTag();
        }

        Announcement.Content ac = displayItem.get(position);
        holder.title.setText(ac.name);
        String subtitle = ac.club + " - " + ac.date;
        holder.subtitle.setText(subtitle);
        holder.body.setText(ac.body);
        holder.name = ac.name;

        //itemView.setOnClickListener(selectDate);

        return itemView;
    }

    class infoHolder{
        TextView title;
        TextView subtitle;
        JustifyTextView body;
        String name;
    }

    public void filter(String keyword){
        final ArrayList<Announcement.Content> newValues = new ArrayList<>(50);
        String prefixString = keyword.toLowerCase();
        for (int i = 0; i < item.size(); i++) {
            final Announcement.Content value = item.get(i);
            if (value.body.toLowerCase().contains(prefixString) || value.name.toLowerCase().contains(prefixString)
                    || value.club.toLowerCase().contains(prefixString) || value.date.toString().contains(prefixString)) {
                newValues.add(value);
            }
        }
        displayItem = newValues;
        LogUtil.d("new_values_record",newValues.toString());
        notifyDataSetChanged();
    }
}
