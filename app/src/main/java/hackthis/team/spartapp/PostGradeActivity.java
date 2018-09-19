package hackthis.team.spartapp;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class PostGradeActivity extends Activity {

    ArrayList<Boolean> grade_list;
    ListView content;
    GradeAdapter ga;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        grade_list = new ArrayList<>(12);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.post_grade);

        for(int i = 0; i < 12; i++)
            grade_list.add(false);

        TextView confirm = findViewById(R.id.post_grade_confirm);
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int sum = 0;
                int base = 1;
                for(int i = 0; i < 12; i++){
                    CheckBox cb = ((ho)(content.getChildAt(i).getTag())).check;
                    if(cb.isChecked()) {
                        sum += base;
                    }
                    base *= 2;
                }
                if(sum == 0){
                    Toast.makeText(PostGradeActivity.this, "please select some grades", Toast.LENGTH_SHORT).show();
                }
                else{
                    LogUtil.d("PostGradeActivity",""+sum);
                    finish(sum);
                }
            }
        });
        Button all = findViewById(R.id.post_grade_all);
        all.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for(int i = 0; i < grade_list.size(); i++){
                    grade_list.set(i, true);
                }
                ga.notifyDataSetChanged();
            }
        });
        Button all_un = findViewById(R.id.post_grade_all_un);
        all_un.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for(int i = 0; i < grade_list.size(); i++){
                    grade_list.set(i, false);
                }
                ga.notifyDataSetChanged();
            }
        });

        ga = new GradeAdapter(this, R.layout.post_grade_element, grade_list);

        content = findViewById(R.id.post_grade_content);
        content.setAdapter(ga);
    }

    public void finish(int grade) {
        SharedPreferences sp = getSharedPreferences("post", Context.MODE_PRIVATE);
        sp.edit().putInt("grades",grade).apply();
        super.finish();
    }

    public class GradeAdapter extends ArrayAdapter{
        public GradeAdapter(Context context, int viewID, List<Boolean> items){
            super(context, viewID, items);
        }


        @Override
        public View getView(int position, View convertView, ViewGroup parent){
            Boolean b = (Boolean) getItem(position); // 获取当前项的实例
            View view = LayoutInflater.from(getContext()).inflate(R.layout.post_grade_element, null);
            TextView tv = view.findViewById(R.id.post_grade_element_text);
            tv.setText(Integer.toString(position+1));
            ho h = new ho();
            CheckBox cb = view.findViewById(R.id.post_grade_element_check);
            cb.setChecked(b);
            h.check = cb;
            view.setTag(h);
            return view;
        }
    }


    public class ho{
        CheckBox check;
    }
}
