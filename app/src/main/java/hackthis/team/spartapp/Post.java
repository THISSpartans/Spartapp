package hackthis.team.spartapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.FindCallback;
import com.avos.avoscloud.SaveCallback;

import java.util.Date;
import java.util.List;

public class Post extends Fragment {

    private Activity mActivity;
    Club club;
    String title, body;
    SharedPreferences sp = null;
    Date time;

    EditText ti;
    EditText bo;

    View.OnClickListener switch_group = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent i = new Intent(mActivity, PostLoginActivity.class);
            startActivity(i);
        }
    };

    View.OnClickListener send = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String cl = sp.getString("name",null);
            if(cl == null){
                Toast t = Toast.makeText(mActivity, "Please sign in before sending!", Toast.LENGTH_SHORT);
                t.setGravity(Gravity.CENTER,0,0);
                t.show();
                return;
            }
            title = ti.getText().toString();
            body = bo.getText().toString();
            if(title.length() < 1){
                Toast t = Toast.makeText(mActivity, "Please write a title!", Toast.LENGTH_SHORT);
                t.setGravity(Gravity.CENTER,0,0);
                t.show();
                return;
            }
            if(body.length() < 1){
                Toast t = Toast.makeText(mActivity, "Please write some contents!", Toast.LENGTH_SHORT);
                t.setGravity(Gravity.CENTER,0,0);
                t.show();
                return;
            }

            AlertDialog dialog = new AlertDialog.Builder(mActivity)
                    .setTitle("Confirmation")
                    .setMessage("Send this announcement to subscribers of "+cl+"?")
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            send_announcement();
                        }
                    }).create();
            dialog.show();

        }
    };

    public void send_announcement(){

        String cl = sp.getString("name",null);
        club = new Club(cl);

        AVObject message = new AVObject("Announcements");
        message.put("clubName", cl);
        message.put("included", true);
        message.put("announcementTitle",title);
        message.put("announcementBody",body);
        message.saveInBackground(new SaveCallback() {
            @Override
            public void done(AVException e) {
                if(e==null){
                    Toast.makeText(mActivity, "Announcement sent",
                            Toast.LENGTH_SHORT).show();
                    ti.setText("");
                    bo.setText("");
                    sp.edit().putString("title",null).putString("body",null).apply();
                }
                else{
                    sp.edit().putString("title",title).putString("body",body).apply();
                    Toast.makeText(mActivity, "An error has occured while sending.\nDraft saved.",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onDetach(){

        if(ti.getText() != null && ti.getText().length() > 0) sp.edit().putString("name",ti.getText().toString()).apply();
        if(bo.getText() != null && bo.getText().length() > 0) sp.edit().putString("body",bo.getText().toString()).apply();
        super.onDetach();
    }

    /**
     * 取自https://blog.csdn.net/ljcitworld/article/details/77528585
     */
    @Override
    public void onStart() {
        super.onStart();


        ti = (EditText) getView().findViewById(R.id.post_title);
        bo = (EditText) getView().findViewById(R.id.post_body);

        if(getUserVisibleHint()) {
            String club = sp.getString("name",null);
            TextView club_name = (TextView)getView().findViewById(R.id.post_club);
            if(club == null){
                Toast.makeText(mActivity, "Please sign in before sending!", Toast.LENGTH_SHORT).show();
                club_name.setText("<--Sign in");
            }
            else
                club_name.setText(club);
            title = sp.getString("title",null);
            body = sp.getString("body",null);
            if(title != null) ti.setText(title);
            if(body != null) bo.setText(body);
        }
    }

    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = (Activity) context;
        sp = mActivity.getSharedPreferences("post", Context.MODE_PRIVATE);
    }
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root;
        root = inflater.inflate(R.layout.post_edit, container, false);
        ImageView group_icon = (ImageView) root.findViewById(R.id.post_switch);
        group_icon.setOnClickListener(switch_group);
        ImageView send_icon = (ImageView) root.findViewById(R.id.post_send);
        send_icon.setOnClickListener(send);

        return root;
    }
}
