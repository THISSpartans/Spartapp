package hackthis.team.spartapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.DeleteCallback;
import com.avos.avoscloud.FindCallback;
import com.avos.avoscloud.SaveCallback;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class Lang2019 extends Activity{

    boolean rep = true;

    TextView r, d;
    WebView manifest;
    HelloWebViewClient maniClient;
    ImageView img;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_ACTION_BAR);
        setContentView(R.layout.langelection);

        //todo fill these
        //if not from THIS -> handleError("only THIS students can vote")
        SharedPreferences prefs = this.getSharedPreferences("clubs", this.MODE_PRIVATE);
        SharedPreferences ver = this.getSharedPreferences("verified", this.MODE_PRIVATE);
        String school = prefs.getString("school", "THIS");
        String occ = prefs.getString("occupation", "student");
        String id = ver.getString("account", "none");
        if(!school.equals("THIS")){ handleError("Only THIS students can vote"); return; }
        else if(!occ.equals("student")) { handleError("Only students can vote"); return; }
        else if(id.equals("none")) { handleError("You must log in to vote"); return; }
        //it not a student -> handleError("only Students can vote")
        //if already voted -> handleError("you can only vote once")

        //todo only select the grade representative nominees for the student's grade, not all

        r = findViewById(R.id.langelection_rep);
        r.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rep = true;
                update();
            }
        });

        d = findViewById(R.id.langelection_dem);
        d.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rep = false;
                update();
            }
        });

        manifest = findViewById(R.id.langelection_manifest);
        manifest.setHorizontalScrollBarEnabled(false);
        maniClient = new HelloWebViewClient();
        manifest.setWebViewClient(maniClient);
        img = findViewById(R.id.langelection_img);

        findViewById(R.id.langelection_vote).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                printSelections();
            }
        });
        update();
    }

    public void update(){
        if (rep) {
            r.setTextColor(getResources().getColor(R.color.black));
            d.setTextColor(getResources().getColor(R.color.grey));
            img.setImageResource(R.drawable.langelection_rep);
            maniClient.shouldOverrideUrlLoading(manifest,"https://thisprogrammingclub.github.io/rep2019");

        }
        else{
            r.setTextColor(getResources().getColor(R.color.grey));
            d.setTextColor(getResources().getColor(R.color.black));
            img.setImageResource(R.drawable.stuco_kimberlyliu);
            maniClient.shouldOverrideUrlLoading(manifest,"https://thisprogrammingclub.github.io/dem2019");
        }
    }

    //https://stackoverflow.com/questions/5800985/how-to-display-web-site-in-android-webview
    private class HelloWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }
    }

    public void handleError(String err){
        setContentView(R.layout.activity_election_page_after);
        TextView message = findViewById(R.id.election_page_error_message);
        message.setText(err);
        Button ret = findViewById(R.id.returnbtn);
        ret.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    public void vote(){
        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        SharedPreferences ver = this.getSharedPreferences("verified", this.MODE_PRIVATE);
        final String id = ver.getString("account", "none");

        final Activity act = this;
        AVQuery<AVObject> query = new AVQuery<>("Langelection2019");
        query.whereEqualTo("studentID",id);
        query.findInBackground(new FindCallback<AVObject>() {
            @Override
            public void done(List<AVObject> list, AVException e) {
                if(e==null) {
                        AVObject.deleteAllInBackground(list, new DeleteCallback() {
                            @Override
                            public void done(AVException e) {
                                AVObject message = new AVObject("Langelection2019");
                                message.put("studentID", id);
                                message.put("party", rep);
                                message.saveInBackground(new SaveCallback() {
                                    @Override
                                    public void done(AVException e) {
                                        if (e == null) {
                                            Toast.makeText(act, "Vote confirmed",
                                                    Toast.LENGTH_SHORT).show();
                                            finish();
                                        } else {
                                            Toast.makeText(act, "An error has occured while sending.",
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }
                        });
                }
                else{
                    e.printStackTrace();
                    Toast.makeText(act, "An error has occured while sending.",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });



    }

    public void printSelections(){
        StringBuilder nameList = new StringBuilder();

        nameList.append("Vote for: ").append(rep?
        "Republican candidate Joanna He":"Democratic candidate Kimberly Liu").append(" ?");

        AlertDialog print = new AlertDialog.Builder(Lang2019.this)
                .setPositiveButton("yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        vote();
                    }})
                .setNegativeButton("no", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                    }})
                .setMessage(nameList+"\n\n\nWARNING: This will overwrite your previous votes!")
                .setCancelable(true)
                .setTitle("Confirmation")
                .create();
        print.show();
    }

}
