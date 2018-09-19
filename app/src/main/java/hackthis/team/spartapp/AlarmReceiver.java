package hackthis.team.spartapp;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

public class AlarmReceiver extends BroadcastReceiver {

    /**
     作者：LeiHolmes
     链接：https://juejin.im/post/59f7f53c5188257ad639db3e
     */

    private NotificationManager m_notificationMgr = null;
    private static final int NOTIFICATION_FLAG = 3;
    public static final String TIMER_ACTION = "AlarmReceiver_TimerAction";

    public AlarmReceiver(){

    }

    @Override
    public void onReceive(Context context, Intent intent) {
        m_notificationMgr = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        String title = intent.getStringExtra("title");
        LogUtil.d("notification","TRIGGEREDDDDDDDDDDDDD");
            Intent intent1 = new Intent(context, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent1, 0);
            Notification notify;
            if(Build.VERSION.SDK_INT < 26) {
                notify = new Notification.Builder(context)
                        .setSmallIcon(R.drawable.app_mini) // 设置状态栏中的小图片，尺寸一般建议在24×24
                        .setTicker(title) // 设置显示的提示文字
                        .setContentTitle(title) // 设置显示的标题
                        .setContentIntent(pendingIntent) // 关联PendingIntent
                        .build();
                notify.flags = Notification.FLAG_AUTO_CANCEL;
            }
            else{
                notify = new Notification.Builder(context)
                        .setSmallIcon(R.drawable.app_mini) // 设置状态栏中的小图片，尺寸一般建议在24×24
                        .setTicker(title) // 设置显示的提示文字
                        .setContentTitle(title) // 设置显示的标题
                        .setContentIntent(pendingIntent) // 关联PendingIntent
                        .setChannelId("Spartapp")
                        .build();
                notify.flags = Notification.FLAG_AUTO_CANCEL;
            }
            m_notificationMgr.notify(420, notify);
    }

}
