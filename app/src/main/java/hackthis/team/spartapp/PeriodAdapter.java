package hackthis.team.spartapp;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class PeriodAdapter extends ArrayAdapter {

    public PeriodAdapter(Context context, int viewID, List<ClassPeriod> items){
        super(context, viewID, items);
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        ClassPeriod cp = (ClassPeriod) getItem(position); // 获取当前项的实例
        View view;

        if(cp.focus) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.period_large, null);//实例化一个对象
            TextView title = (TextView) view.findViewById(R.id.period_large_title);
            title.setText(cp.sub.name);
            title.setTypeface(null, Typeface.BOLD);
            TextView description = (TextView) view.findViewById(R.id.period_large_description);
            description.setText(cp.sub.teacher+"\n"+cp.sub.room);
            ImageView background = (ImageView) view.findViewById(R.id.period_large_background);
            if(cp.backgroundID != 0)
                background.setImageResource(cp.backgroundID);
        }
        else{
            view = LayoutInflater.from(getContext()).inflate(R.layout.period_small, null);
            TextView title = (TextView) view.findViewById(R.id.period_small_title);
            title.setText(cp.sub.name);
            title.setTypeface(null, Typeface.BOLD);
            ImageView background = (ImageView) view.findViewById(R.id.period_small_background);
            background.setColorFilter(getContext().getResources().getColor(cp.colorID));
        }
        return view;
    }
}
