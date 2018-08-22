package hackthis.team.spartapp;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.RadioButton;

public class MainActivity extends Activity {

    Fragment schedule = null, announcement = null, post = null, current;//schedule程序开始时初始化，其余第一次navigate时初始化
    FragmentTransaction transaction = getFragmentManager().beginTransaction();

    SharedPreferences sp;

    //switch between activities
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_schedule:
                    switchContent(current, schedule);
                    return true;
                case R.id.navigation_announcement:
                    if(announcement == null){
                        announcement = new Announcement();
                    }
                    switchContent(current, announcement);
                    return true;
                case R.id.navigation_post:
                    if(post == null){
                        post = new Post();
                    }
                    switchContent(current, post);
                    return true;
            }
            return false;
        }
    };

    View.OnClickListener save_grade_level = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            NumberPicker np = (NumberPicker) findViewById(R.id.grade_picker);
            int grade = np.getValue();
            sp = getSharedPreferences("clubs",Context.MODE_PRIVATE);
            sp.edit().putInt("grade",grade).apply();
            RadioButton THIS = (RadioButton) findViewById(R.id.radio_this);
            RadioButton ISB = (RadioButton) findViewById(R.id.radio_isb);
            if(THIS.isChecked()){
                sp.edit().putString("school","THIS").apply();
            }
            else{
                sp.edit().putString("school","ISB").apply();
            }
            init_main();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_ACTION_BAR);

        sp = getSharedPreferences("app", Context.MODE_PRIVATE);
        boolean first = sp.getBoolean("first_launch",true);

        //todo remove this
        sp.edit().putBoolean("first_launch",true).apply();

        if(first){
            setContentView(R.layout.walkthrough);
            ImageView yes = (ImageView) findViewById(R.id.yes);
            yes.setOnClickListener(save_grade_level);
            NumberPicker np = (NumberPicker) findViewById(R.id.grade_picker);
            np.setMaxValue(12);
            np.setMinValue(1);
            np.setValue(9);
        }

        else {
            init_main();
        }
    }

    public void init_main(){
        //todo determine if the user already logged in, if true do follows:

        setContentView(R.layout.activity_main);

        //初始化schedule
        if (schedule == null) {
            schedule = new Schedule();
        }
        current = schedule;

        //加到主界面
        transaction.add(R.id.fragment_container, schedule).commit();

        //初始化选择栏
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        //todo if false, do follows
        //Intent login = new Intent(MainActivity.this, LoginActivity.class)
        //login.startActivity();
    }

    //https://blog.csdn.net/caroline_wendy/article/details/48492135
    public void switchContent(Fragment from, Fragment to) {
        if (current != to) {
            current = to;
            transaction = getFragmentManager().beginTransaction();
            if (!to.isAdded()) {    // 先判断是否被add过
                transaction.hide(from).add(R.id.fragment_container, to).commit(); // 隐藏当前的fragment，add下一个到Activity中
            } else {
                transaction.hide(from).show(to).commit(); // 隐藏当前的fragment，显示下一个
            }
        }
    }
}

