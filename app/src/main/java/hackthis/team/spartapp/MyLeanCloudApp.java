package hackthis.team.spartapp;

import android.app.Application;

import cn.leancloud.AVInstallation;
import cn.leancloud.AVOSCloud;
import cn.leancloud.AVObject;
import cn.leancloud.push.PushService;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

//created following instructions on Leancloud : see their documentations on setting up
public class MyLeanCloudApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        PushService.setDefaultChannelId(this, "Spartapp");

        // 初始化参数依次为 this, AppId, AppKey
        AVOSCloud.initialize(this,"cRxhzMwEQJ07JfRuztRYFJ5n-gzGzoHsz","kIvYOVL1hGnkS3n1kh76P8NC");

        AVInstallation.getCurrentInstallation().saveInBackground().subscribe(new Observer<AVObject>() {
            @Override
            public void onSubscribe(Disposable d) {
            }
            @Override
            public void onNext(AVObject avObject) {
                // 关联 installationId 到用户表等操作。
                String installationId = AVInstallation.getCurrentInstallation().getInstallationId();
                System.out.println("保存成功：" + installationId );
            }
            @Override
            public void onError(Throwable e) {
                System.out.println("保存失败，错误信息：" + e.getMessage());
            }
            @Override
            public void onComplete() {
            }
        });

        // 设置默认打开的 Activity
        PushService.setDefaultPushCallback(this, MainActivity.class);
    }
}
