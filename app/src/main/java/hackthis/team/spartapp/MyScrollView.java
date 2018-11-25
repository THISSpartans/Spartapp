package hackthis.team.spartapp;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ScrollView;

/**
 * Created by HP on 2018/11/24.
 */

public class MyScrollView extends ScrollView {

    private Runnable scrollerTask;
    private int initialPosition;

    private int newCheck = 1;
    private static final String TAG = "MyScrollView";

    public interface OnScrollStoppedListener{
        void onScrollStopped();
    }

    public OnScrollStoppedListener onScrollStoppedListener;

    public MyScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);

        scrollerTask = new Runnable() {

            public void run() {

                int newPosition = getScrollY();
                if(initialPosition - newPosition == 0){//has stopped

                    if(onScrollStoppedListener!=null){

                        onScrollStoppedListener.onScrollStopped();
                    }
                }else{
                    initialPosition = getScrollY();
                    MyScrollView.this.postDelayed(scrollerTask, newCheck);
                }
            }
        };
    }

}