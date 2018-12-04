package hackthis.team.spartapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.RadioButton;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.GetCallback;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.StringTokenizer;

public class MainActivity extends Activity {

    RefreshableFragment schedule = null, announcement = null, post = null, current, services, news = null;//schedule程序开始时初始化，其余第一次navigate时初始化
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
                /*case R.id.navigation_announcement:
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
                    */

                case R.id.navigation_news:
                    if(news == null){
                        news = new News();
                    }
                    updateClickNum();
                    switchContent(current, news);
                    return true;

                case R.id.navigation_services:
                    if(services == null){
                        services = new Services();
                    }
                    switchContent(current, services);
                    return true;
            }
            return false;
        }
    };

    View.OnClickListener save_user_stats = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //NumberPicker np = (NumberPicker) findViewById(R.id.grade_picker);
            //int grade = np.getValue();
            sp = getSharedPreferences("clubs",Context.MODE_PRIVATE);
            //sp.edit().putInt("grade",grade).apply();
            RadioButton THIS = (RadioButton) findViewById(R.id.radio_this);
            //RadioButton ISB = (RadioButton) findViewById(R.id.radio_isb);
            if(THIS.isChecked()){
                sp.edit().putString("school","THIS").apply();
            }
            else{
                sp.edit().putString("school","ISB").apply();
            }

            RadioButton STU = (RadioButton) findViewById(R.id.radio_student);
            //RadioButton TEA = (RadioButton) findViewById(R.id.radio_teacher);
            if(STU.isChecked()){
                sp.edit().putString("occupation","student").apply();
            }
            else{
                sp.edit().putString("occupation","teacher").apply();
                LogUtil.d("HTML", "rest");
            }
            sp = getSharedPreferences("app", Context.MODE_PRIVATE);
            sp.edit().putBoolean("first_launch",false).apply();
            init_main();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_ACTION_BAR);

        sp = getSharedPreferences("app", Context.MODE_PRIVATE);
        boolean first = sp.getBoolean("first_launch",true);

        LogUtil.d("thing",Boolean.toString(first));

        if(first){
            setContentView(R.layout.walkthrough);
            ImageView yes = (ImageView) findViewById(R.id.yes);
            yes.setOnClickListener(save_user_stats);
            /*NumberPicker np = (NumberPicker) findViewById(R.id.grade_picker);
            np.setMaxValue(12);
            np.setMinValue(1);
            np.setValue(9);

            RadioButton TEA = (RadioButton) findViewById(R.id.radio_teacher);
            final LinearLayout GL = (LinearLayout) findViewById(R.id.grade_level);
            TEA.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if(isChecked)
                        GL.setVisibility(View.GONE);
                    else
                        GL.setVisibility(View.VISIBLE);
                }
            });
            */

        }

        else {
            init_main();
        }


        //get access to internet
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .detectDiskReads().detectDiskWrites().detectNetwork()
                .penaltyLog().build());
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                .detectLeakedSqlLiteObjects().detectLeakedClosableObjects()
                .penaltyLog().penaltyDeath().build());

        LogUtil.d("VER","test");
        AVQuery versionQuery = new AVQuery("AndroidVersionInfo");
        versionQuery.getInBackground("5adf2f749f545433342866ec", new GetCallback() {
                @Override
                public void done(AVObject avObject, AVException e) {
                    if(e==null) {
                        String versionName = avObject.getString("version_name");
                        int versionCode = avObject.getInt("version_code");

                        //dialogue for version name
                        try {
                            PackageInfo pInfo = getApplicationContext().getPackageManager().getPackageInfo(getPackageName(), 0);
                            int localVersionCode = pInfo.versionCode;
                            LogUtil.d("VER", "local version code is " + localVersionCode);

                            if (localVersionCode < versionCode) {
                                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(MainActivity.this);
                                alertBuilder.setPositiveButton("yes", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://thisprogrammingclub.github.io/spartapp_android.apk"));
                                        startActivity(browserIntent);
                                    }
                                }).setNegativeButton("no", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {

                                    }
                                }).setMessage("A new version (" + versionName + ") is available. Do you want to download it?")
                                        .setCancelable(true);
                                LogUtil.d("VER", "connected and obtained version " + versionName + " with code " + versionCode);
                                AlertDialog dialog = alertBuilder.create();
                                dialog.show();
                            }

                        } catch (PackageManager.NameNotFoundException E) {
                            E.printStackTrace();
                        }
                    }
                }
        });
    }

    public void init_main(){
        boolean schExist = true;
        try {
            readWeeklySchedule();
        }
        catch(Exception e){
            schExist = false;
        }
        //todo determine if the user already logged in, if true do follows:
        if(schExist) {
            setContentView(R.layout.activity_main);

            //初始化schedule
            if (schedule == null) {
                schedule = new Schedule();
            }
            current = schedule;

            //加到主界面
            transaction.add(R.id.fragment_container, current).commit();

            //初始化选择栏
            BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
            navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        }
        else {
            LogUtil.d("ERR", "IOE");
            //todo if false, do follows
            Intent login = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(login);
        }
    }



    //https://blog.csdn.net/caroline_wendy/article/details/48492135
    public void switchContent(RefreshableFragment from, RefreshableFragment to) {
        if (current != to) {
            current = to;
            transaction = getFragmentManager().beginTransaction();
            if (!to.isAdded()) {    // 先判断是否被add过
                transaction.hide(from).add(R.id.fragment_container, to).commit(); // 隐藏当前的fragment，add下一个到Activity中
            } else {
                transaction.hide(from).show(to).commit(); // 隐藏当前的fragment，显示下一个
            }
        }
        to.refresh();
    }

    public HashMap<Integer, Subject[]> readWeeklySchedule() throws Exception{
        LogUtil.d("HTML_IN", "called");
        FileInputStream f = this.openFileInput("week_schedule.dat");
        LogUtil.d("HTML_IN", "found");
        BufferedReader in = new BufferedReader(new InputStreamReader(f));
        LogUtil.d("HTML_IN", "buffer on");
        HashMap<Integer, Subject[]> schedule = new HashMap<>(0);
        String line;
        int dayInCycle = 1;
        while((line = in.readLine())!=null){
            StringTokenizer tizer = new StringTokenizer(line, "?");
            Subject[] daySchedule = new Subject[tizer.countTokens()/3];
            for(int period = 0; period < daySchedule.length; period ++){
                String name = tizer.nextToken();
                String teacher = tizer.nextToken();
                String room = tizer.nextToken();
                Subject subject = new Subject(name, teacher, room);
                LogUtil.d("HTML_IN",subject.name() + "," + subject.teacher() + "," + subject.room() + ",");
                daySchedule[period] = subject;
            }
            schedule.put(dayInCycle, daySchedule);
            dayInCycle ++;
        }
        in.close();
        LogUtil.d("HTML_IN", "done reading schedule");
        return schedule;
    }

    private void updateClickNum(){
        LogUtil.d("newscontent","clicked");
        AVQuery<AVObject> q = new AVQuery<>("NewsViews");
        q.getInBackground("5c053423808ca40072d3a1e0", new GetCallback<AVObject>() {
            @Override
            public void done(AVObject avObject, AVException e) {
                if(e == null){
                    LogUtil.d("newscontent","got");
                    int i = avObject.getInt("numOfViews");
                    AVObject n = AVObject.createWithoutData("NewsViews","5c053423808ca40072d3a1e0");
                    n.put("numOfViews",i+1);
                    n.saveInBackground();
                }
                else{
                    LogUtil.d("newscontent",e.getMessage());
                }
            }
        });
    }

}

