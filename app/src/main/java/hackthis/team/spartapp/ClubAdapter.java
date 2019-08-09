package hackthis.team.spartapp;

import android.content.ClipData;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;

import cn.leancloud.AVOSCloud;

import java.util.List;

public class ClubAdapter extends BaseAdapter implements View.OnClickListener{

    List<Club> data;
    private ListView root;
    //定义一个数据源的引用
    private Context context;

    //取自 https://blog.csdn.net/jxnk25/article/details/50358231

    public ClubAdapter(Context c, ListView r, List<Club> items){
        data = items;
        context = c;
        root = r;
    }

    /**
     * 获取当前子view的id（就是listview中的每一个条目的位置）
     * @param position
     * @return   返回当前id
     */
    @Override
    public long getItemId(int position) {
        return position;
    }

    /**
     * 获取当前子view对应的值
     * @param position  当前子view（条目）的id（位置）
     * @return   返回当前对应的值 该值为object类型
     */
    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    /**
     * 定义coverView的Recyler(缓存)，该类名自定义的
     */
    class ViewHodler{
        TextView name;
        int id;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        Club cp = (Club) getItem(position); // 获取当前项的实例

        //布局生成器(抽象类)
        LayoutInflater layoutInflater=LayoutInflater.from(this.context);
        //声明缓存
        ViewHodler viewHodler=null;
        //重新创建布局及缓存
        convertView=layoutInflater.inflate(R.layout.club_list_item,parent,false);
        //产生缓存
        viewHodler=new ViewHodler();
        viewHodler.name=(TextView)convertView.findViewById(R.id.club_selector_name);
        viewHodler.id = position;
        convertView.setTag(viewHodler);
        //为缓存的布局ViewHodler控件设置新的数据
        Club currItem=data.get(position);
        viewHodler.name.setText(currItem.name);
        if(currItem.checked){
            convertView.setBackground(context.getResources().getDrawable(R.drawable.club_shape_selected));
            viewHodler.name.setTextColor(context.getResources().getColor(R.color.white));
        }
        else{
            convertView.setBackground(context.getResources().getDrawable(R.drawable.club_shape));
            viewHodler.name.setTextColor(context.getResources().getColor(R.color.black));
        }
        convertView.setOnClickListener(this);
        return convertView;
    }

    /**
     * 获取数据中要在listview中显示的条目
     * @return  返回数据的条目
     */
    @Override
    public int getCount() {
        return this.data!=null?this.data.size():0;
    }

    @Override
    public void onClick(View v){
        ViewHodler vh = (ViewHodler) v.getTag();
        data.get(vh.id).checked = !data.get(vh.id).checked;
        this.notifyDataSetChanged();
    }
}
