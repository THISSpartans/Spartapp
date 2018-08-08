package hackthis.team.spartapp;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

public class Announcement extends Fragment {

    private Activity mActivity;

    View.OnClickListener SEARCH = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

        }
    };

    View.OnClickListener FILTER = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

        }
    };

    RadioGroup.LayoutParams date_params;

    /**
     * 取自https://blog.csdn.net/ljcitworld/article/details/77528585
     */
    @Override
    public void onStart() {
        super.onStart();
        if(getUserVisibleHint()) {
            update_list();
        }
    }

    public void update_list(){

    }

    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = (Activity) context;

    }
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.announcement, container, false);
        ImageView search = (ImageView) root.findViewById(R.id.announcement_search);
        search.setOnClickListener(SEARCH);
        ImageView filter = (ImageView) root.findViewById(R.id.announcement_filter);
        filter.setOnClickListener(FILTER);
        return root;
    }

}
