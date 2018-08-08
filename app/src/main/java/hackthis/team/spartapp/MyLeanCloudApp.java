package hackthis.team.spartapp;

import android.app.Application;

import com.avos.avoscloud.AVOSCloud;

public class MyLeanCloudApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // 初始化参数依次为 this, AppId, AppKey
        AVOSCloud.initialize(this,"cRxhzMwEQJ07JfRuztRYFJ5n-gzGzoHsz","kIvYOVL1hGnkS3n1kh76P8NC");
        //TODO: remove before release
        AVOSCloud.setDebugLogEnabled(true);
    }
}
