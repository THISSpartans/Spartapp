package hackthis.team.spartapp;

import android.util.Log;

public class LogUtil {
    public static void d(String tag, String thing){
        if(BuildConfig.DEBUG)
            Log.d(tag, thing);
    }
    public static void v(String tag, String thing){
        if(BuildConfig.DEBUG)
            Log.v(tag, thing);
    }
    public static void e(String tag, String thing)
    {
        Log.e(tag, thing);
    }
    public static void i(String tag, String thing){
        Log.i(tag, thing);
    }
}
