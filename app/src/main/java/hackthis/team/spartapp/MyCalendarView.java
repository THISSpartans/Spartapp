package hackthis.team.spartapp;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.CalendarView;

/**
 * Created by HP on 2018/11/25.
 */

public class MyCalendarView extends CalendarView {

    public MyCalendarView(Context context, AttributeSet attrs){
        super(context, attrs);
    }

    public boolean onTouchEvent(MotionEvent event){
        if(event.getAction()!=MotionEvent.ACTION_MOVE){
            super.onTouchEvent(event);
        }
        return true;
    }

}
