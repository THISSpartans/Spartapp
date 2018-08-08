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
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

public class Announcement extends Fragment {

    private Activity mActivity;

    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = (Activity) context;
    }
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.announcement, container, false);

        return root;
    }

}
