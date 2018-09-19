package hackthis.team.spartapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVInstallation;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVPush;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.FindCallback;
import com.avos.avoscloud.SaveCallback;
import com.avos.avoscloud.SendCallback;

import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class Post extends RefreshableFragment {

    private Activity mActivity;
    Club club;
    String title, body;
    SharedPreferences sp_post = null;
    SharedPreferences sp;
    Date time;

    EditText ti;
    EditText bo;
    TextView grade_text;

    int to_grade;
    ArrayList<Boolean> to_grade_list;

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
            String cl = sp_post.getString("name",null);
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

    View.OnClickListener grade_selector = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent i = new Intent(mActivity, PostGradeActivity.class);
            startActivity(i);
        }
    };

    public void send_announcement(){

        String cl = sp_post.getString("name",null);
        club = new Club(cl);

        sp = mActivity.getSharedPreferences("clubs",Context.MODE_PRIVATE);

        String search_root = sp.getString("school","");
        if(search_root.equals("THIS")){
            search_root = "";
        }
        else{
            search_root = "_"+search_root;
        }

        AVObject message = new AVObject("Announcements"+search_root);
        message.put("clubName", cl);
        message.put("included", true);
        message.put("announcementTitle",title);
        message.put("announcementBody",body);
        message.put("channels", Arrays.asList(club.getCorrespondingChannel()));
        ArrayList<Integer> grades_list = new ArrayList<>(12);
        for(int i = 0; i < to_grade_list.size(); i++){
            if(to_grade_list.get(i))
                grades_list.add(i+1);
        }
        message.put("gradeLevel",grades_list);
        message.saveInBackground(new SaveCallback() {
            @Override
            public void done(AVException e) {
                if(e==null){
                    Toast.makeText(mActivity, "Announcement sent",
                            Toast.LENGTH_SHORT).show();
                    ti.setText("");
                    bo.setText("");
                    sp_post.edit().putString("title",null).putString("body",null).apply();

                    JSONObject object = new JSONObject();
                    try {
                        object.put("alert", title);
                    }
                    catch (Exception exc) {
                        exc.printStackTrace();
                    }

                    AVQuery pushQuery = AVInstallation.getQuery();
                    pushQuery.whereEqualTo("channels", club.getCorrespondingChannel());
                    AVPush push = new AVPush();
                    push.setQuery(pushQuery);
                    push.setChannel(club.getCorrespondingChannel());
                    push.setData(object);
                    push.setPushToAndroid(true);
                    push.setPushToIOS(true);
                    push.setPushToWindowsPhone(true);
                    push.sendInBackground(new SendCallback() {
                        @Override
                        public void done(AVException e) {
                            if (e == null) {
                                LogUtil.d("push_log","push complete");
                            }   else {
                                e.printStackTrace();
                            }
                        }
                    });
                }
                else{
                    sp_post.edit().putString("title",title).putString("body",body).apply();
                    Toast.makeText(mActivity, "An error has occured while sending.\nDraft saved.",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void refresh(){
        return;
    }

    @Override
    public void onDetach(){

        if(ti.getText() != null && ti.getText().length() > 0) sp_post.edit().putString("title",ti.getText().toString()).apply();
        if(bo.getText() != null && bo.getText().length() > 0) sp_post.edit().putString("body",bo.getText().toString()).apply();
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
        grade_text = (TextView) getView().findViewById(R.id.post_grade_text);

        if(getUserVisibleHint()) {
            String club = sp_post.getString("name",null);
            TextView club_name = (TextView)getView().findViewById(R.id.post_club);
            to_grade = sp_post.getInt("grades",4095);
            to_grade_list = new ArrayList<>(12);
            LogUtil.d("post",Integer.toBinaryString(to_grade));
            char[] temp = Integer.toBinaryString(to_grade).toCharArray();
            for(int i = temp.length-1; i >= 0; i--){
                to_grade_list.add(temp[i] == '1');
            }
            LogUtil.d("post",to_grade_list.toString());
            if(club == null){
                Toast.makeText(mActivity, "Please sign in before sending!", Toast.LENGTH_SHORT).show();
                club_name.setText("<--Sign in");
            }
            else
                club_name.setText(club);
            title = sp_post.getString("title",null);
            body = sp_post.getString("body",null);
            if(title != null) ti.setText(title);
            if(body != null) bo.setText(body);
            grade_text.setText("to "+print_grades());
        }
    }

    public String print_grades(){
        boolean allSelected = true;
        String str = "grade ";
        for(int i = 0; i < to_grade_list.size(); i++){
            if(!to_grade_list.get(i))
                allSelected = false;
            else
            str = str + (i+1) + " ";
        }
        return allSelected && to_grade_list.size() > 1? "all grades" : str;
    }

    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = (Activity) context;
        sp_post = mActivity.getSharedPreferences("post", Context.MODE_PRIVATE);
    }
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root;
        root = inflater.inflate(R.layout.post_edit, container, false);
        ImageView group_icon = (ImageView) root.findViewById(R.id.post_switch);
        group_icon.setOnClickListener(switch_group);
        ImageView send_icon = (ImageView) root.findViewById(R.id.post_send);
        send_icon.setOnClickListener(send);
        RelativeLayout grade_select = (RelativeLayout) root.findViewById(R.id.post_grade);
        grade_select.setOnClickListener(grade_selector);
        return root;
    }
}
