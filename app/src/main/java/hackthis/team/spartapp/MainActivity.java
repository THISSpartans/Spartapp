package hackthis.team.spartapp;

import android.app.Activity;
import android.app.ActivityGroup;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.Window;
import android.widget.TextView;

public class MainActivity extends Activity {

    Fragment schedule = null, announcement = null, post = null, current;//schedule程序开始时初始化，其余第一次navigate时初始化
    FragmentTransaction transaction = getFragmentManager().beginTransaction();

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_ACTION_BAR);

        //todo determine if the user already logged in, if true do follows:

        setContentView(R.layout.activity_main);

        //初始化schedule
        if(schedule == null) {
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

