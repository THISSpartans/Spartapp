package hackthis.team.spartapp;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.Toast;

import cn.leancloud.AVException;
import cn.leancloud.AVInstallation;
import cn.leancloud.AVOSCloud;
import cn.leancloud.AVObject;
import cn.leancloud.AVQuery;
import cn.leancloud.callback.GetCallback;
import cn.leancloud.push.PushService;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.StringTokenizer;

public class MainActivity extends Activity {

    RefreshableFragment schedule = null, announcement = null, post = null, current, services, news = null;//schedule程序开始时初始化，其余第一次navigate时初始化
    FragmentTransaction transaction = getFragmentManager().beginTransaction();

    SharedPreferences sp;

    final int RequestCode = 114514;

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
                PushService.subscribe(MainActivity.this,"tsinghua",MainActivity.class);
                PushService.unsubscribe(MainActivity.this,"isb");
            }
            else{
                sp.edit().putString("school","ISB").apply();
                PushService.subscribe(MainActivity.this,"isb",MainActivity.class);
                PushService.unsubscribe(MainActivity.this,"tsinghua");
            }

            AVInstallation.getCurrentInstallation().saveInBackground();

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

    private BroadcastReceiver ApkInstallReceiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() != null && intent.getAction().equals(DownloadManager.ACTION_DOWNLOAD_COMPLETE)) {
                //https://stackoverflow.com/questions/26210048/how-to-receive-status-of-download-manager-intent-until-download-success-or-faile/38737503#38737503
                Toast.makeText(context,"downloaded", Toast.LENGTH_SHORT).show();
                long completeDownloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                installApk(context, completeDownloadId);
                LogUtil.d("DownloadManager",Long.toString(completeDownloadId));
            }
        }
    };

        /*
        ---------------------
        作者：Chiclaim
        来源：CSDN
        原文：https://blog.csdn.net/johnny901114/article/details/51472600
        版权声明：本文为博主原创文章，转载请附上博文链接！*/
        private void installApk(Context context, long downloadApkId) {
            DownloadManager dManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
            Intent install = new Intent(Intent.ACTION_VIEW);
            try {
                Uri downloadFileUri = dManager.getUriForDownloadedFile(downloadApkId);
                if (downloadFileUri != null) {
                    LogUtil.d("DownloadManager", downloadFileUri.toString());
                    install.setDataAndType(downloadFileUri, "application/vnd.android.package-archive");
                    install.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    //Toast.makeText(MainActivity.this, "jier",Toast.LENGTH_LONG).show();
                    context.startActivity(install);
                } else {
                    LogUtil.e("DownloadManager", "download error");
                }
            }
            catch(Exception E){
                E.printStackTrace();
            }
        }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_ACTION_BAR);

        registerReceiver(ApkInstallReceiver,new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

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

            sp = getSharedPreferences("clubs",Context.MODE_PRIVATE);
            String school = sp.getString("school","none");
            if(!school.equals("none")){
                switch(school){
                    case "THIS":{
                        PushService.subscribe(MainActivity.this,"tsinghua",MainActivity.class);
                        PushService.unsubscribe(MainActivity.this,"isb");
                        break;
                    }
                    case "ISB":{
                        PushService.subscribe(MainActivity.this,"isb",MainActivity.class);
                        PushService.unsubscribe(MainActivity.this,"tsinghua");
                        break;
                    }
                }
            }
            AVInstallation.getCurrentInstallation().saveInBackground();
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
        AVQuery<AVObject> versionQuery = new AVQuery<AVObject>("AndroidVersionInfo");

        versionQuery.getInBackground("5adf2f749f545433342866ec").subscribe(new Observer<AVObject>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(AVObject avObject) {
                String versionName = avObject.getString("version_name");
                int versionCode = avObject.getInt("version_code");
                String description = avObject.getString("description");

                //dialogue for version name
                try {
                    PackageInfo pInfo = getApplicationContext().getPackageManager().getPackageInfo(getPackageName(), 0);
                    int localVersionCode = pInfo.versionCode;
                    LogUtil.d("VER", "local version code is " + localVersionCode);

                    if (versionCode > localVersionCode) {
                        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(MainActivity.this);
                        alertBuilder.setPositiveButton("update", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                download();
                            }
                        }).setNegativeButton("ignore", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                            }
                        }).setMessage(description)
                                .setCancelable(true);
                        AlertDialog dialog = alertBuilder.create();
                        dialog.show();
                    }

                } catch (PackageManager.NameNotFoundException E) {
                    E.printStackTrace();
                }
            }

            @Override
            public void onError(Throwable e) {
                LogUtil.d("VER","cannot get online version");
                e.printStackTrace();
            }

            @Override
            public void onComplete() {

            }
        });
    }

    //https://stackoverflow.com/questions/21477493/android-download-manager-completed
    @Override
    public void onStop() {
        //not sure what this does
        //gave me receiver not registered error
        try {
            unregisterReceiver(ApkInstallReceiver);
        }catch(Exception e){

        }
        super.onStop();
    }


    //https://stackoverflow.com/questions/40514335/download-manager-android-permissions-error-write-external-storage
    public  boolean haveStoragePermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                LogUtil.e("Permission error","You have permission");
                return true;
            } else {

                LogUtil.e("Permission error","You have asked for permission");
                return false;
            }
        }
        else { //you dont need to worry about these stuff below api level 23
            LogUtil.e("Permission error","You already have the permission");
            return true;
        }
    }

    public void download(){
        if(haveStoragePermission()) {
            DownloadManager.Request req = new DownloadManager.Request(Uri.parse("https://thisprogrammingclub.github.io/spartapp_android.apk"));
            req.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "spartapp_update.apk");
            // 设置一些基本显示信息
            req.setTitle("Spartapp");
            req.setDescription("New Version");
            req.setMimeType("application/vnd.android.package-archive");
        /*
        ---------------------
                作者：Chiclaim
        来源：CSDN
        原文：https://blog.csdn.net/johnny901114/article/details/51472600
        版权声明：本文为博主原创文章，转载请附上博文链接！
        */
            req.setVisibleInDownloadsUi(true);
            req.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);

            long downloadID = dm.enqueue(req);
        }
        else{
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    RequestCode);


        }
    }

    //https://developer.android.com/training/permissions/requesting?hl=zh-cn
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case RequestCode: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    download();

                } else {
                    startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse("https://www.spartapp.org")));
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    public void init_main(){
        boolean schExist = true;
        //todo: remove the following before release
        /*
        try {
            readWeeklySchedule();
        }
        catch(Exception e){
            schExist = false;
        }
        */
        //determine if the user already logged in, if true do follows:
        if(schExist) {
            setContentView(R.layout.activity_main);

            Bundle extras = getIntent().getExtras();
            String mode = extras==null?null:extras.getString("mode");

            if(current!=null)transaction.hide(current);

            //initialize bottom navigation (part1) (see below for part2)
            BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);


            //main screen (the functional ones)
            if(mode != null) {
                if (mode.equals("news")) {
                    //launch with the news tab chosen
                    //news need to be refreshed in case of new info
                    if(news==null)
                        news = new News();
                    else
                        news.refresh();
                    current = news;
                    navigation.setSelectedItemId(R.id.navigation_news);
                    LogUtil.d("spartapp", "launching in news mode");
                }
                else if (mode.equals("services")) {
                    //launch with the service tab chosen
                    services = services==null?new Services():services;
                    current = services;
                    navigation.setSelectedItemId(R.id.navigation_services);
                    LogUtil.d("spartapp", "launching in services mode");
                }
                else {
                    //default: use the schedule tab to begin
                    schedule = schedule==null?new Schedule():schedule;
                    current = schedule;
                    navigation.setSelectedItemId(R.id.navigation_schedule);
                    LogUtil.d("spartapp", "launching in schedule mode");
                }
            }
            else {
                //todo switch back to schedule?
                //default: use the schedule tab to begin
                news = news==null?new News():news;
                current = news;
                navigation.setSelectedItemId(R.id.navigation_news);
                LogUtil.d("spartapp", "launching in news mode");
            }

            if(current == null)
                LogUtil.d("spartapp","theres nothing!");

            //initialize navigation bar (part 2)
            navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

            //clear & find the fragment container
            FrameLayout container_dummy = (FrameLayout)findViewById(R.id.fragment_container);
            container_dummy.removeAllViewsInLayout();

            //add current tab to fragment container
            transaction.add(R.id.fragment_container, current).commit();


        }
        else {
            LogUtil.d("ERR", "IOE");
            Intent login = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(login);
        }
    }



    //https://blog.csdn.net/caroline_wendy/article/details/48492135
    public void switchContent(RefreshableFragment from, RefreshableFragment to) {
        if (current != to) {
            current = to;
            transaction = getFragmentManager().beginTransaction();
            if (!to.isAdded()) {    // see if the tab already exists in the background
                transaction.hide(from).add(R.id.fragment_container, to).commit(); // hide the 'from' tab, add the 'to' tab: clarify: show != add, see 'show' below
            } else {
                transaction.hide(from).show(to).commit(); // hide the 'from' tab, show the 'to' tab
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
        q.getInBackground("5c053423808ca40072d3a1e0").subscribe(new Observer<AVObject>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(AVObject avObject) {
                LogUtil.d("newscontent","got");
                int i = avObject.getInt("numOfViews");
                AVObject n = AVObject.createWithoutData("NewsViews","5c053423808ca40072d3a1e0");
                n.put("numOfViews",i+1);
                n.saveInBackground();
            }

            @Override
            public void onError(Throwable e) {
                LogUtil.d("newscontent",e.getMessage());
            }

            @Override
            public void onComplete() {

            }
        });
    }

}

