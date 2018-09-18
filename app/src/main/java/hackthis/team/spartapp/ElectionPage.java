package hackthis.team.spartapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.provider.Contacts;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVFile;
import com.avos.avoscloud.AVInstallation;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVPush;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.FindCallback;
import com.avos.avoscloud.SaveCallback;
import com.avos.avoscloud.SendCallback;

import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ElectionPage extends Activity {

    ArrayList<List<Person>> titles;
    ArrayList<Title> keyList;
    List<RadioGroup> nominees;
    LinearLayout body;
    ImageView yes, no;

    ArrayList<String> shownTitles = new ArrayList<>(10);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_ACTION_BAR);
        setContentView(R.layout.activity_election_page);

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
        body = findViewById(R.id.election_page_body);
        yes = findViewById(R.id.election_page_yes);
        no = findViewById(R.id.election_page_no);

        yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                printSelections();
            }
        });

        no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        update();
    }

    public void update(){
        nominees = new ArrayList<>(10);
        titles = new ArrayList<>(10);
        keyList = new ArrayList<>(10);

        // grade 0 means the title will not be filtered by grade
        Title temp;
        temp = new Title("President",0);
        titles.add(Arrays.asList(
                new Person("Charlie\nLiu",R.drawable.stuco_charlieliu),
                new Person("Kimberly\nLiu",R.drawable.stuco_kimberlyliu),
                new Person("Claudia\nSun",R.drawable.stuco_claudiasun),
                new Person("Raymond\nZhang",R.drawable.stuco_raymondzhang)
        ));
        keyList.add(temp);
        temp = new Title("Vice President",0);
        titles.add(Arrays.asList(
                new Person("Annelise\nGuo",R.drawable.stuco_anneliseguo),
                new Person("Yifan\nRuan",R.drawable.stuco_yifanruan),
                new Person("Keven\nZhou",R.drawable.stuco_kevenzhou)
        ));
        keyList.add(temp);
        temp = new Title("Secretary",0);
        titles.add(Arrays.asList(
                new Person("Winnie\nXiao",R.drawable.stuco_winniexiao)
        ));
        keyList.add(temp);
        temp = new Title("Treasurer",0);
        titles.add(Arrays.asList(
                new Person("Joice\nChen",R.drawable.stuco_joicechen),
                new Person("Thomas\nLi",R.drawable.stuco_thomasli),
                new Person("Nicole\nZhang",R.drawable.stuco_nicolezhang)
        ));
        keyList.add(temp);
        temp = new Title("Activities Officer",0);
        titles.add(Arrays.asList(
                new Person("Leni\nGao",R.drawable.stuco_lenigao),
                new Person("Jack\nXu",R.drawable.stuco_jackxu),
                new Person("Talia\nZhao",R.drawable.stuco_taliazhao)
        ));
        keyList.add(temp);
        temp = new Title("Publishing Officer",0);
        titles.add(Arrays.asList(
                new Person("Leon\nChang",R.drawable.stuco_leonchang)
        ));
        keyList.add(temp);
        temp = new Title("Grade 9 Rep",9);
        titles.add(Arrays.asList(
                new Person("Anna\nCui",R.drawable.stuco_annacui),
                new Person("Harry\nZhang",R.drawable.stuco_harryzhang)
        ));
        keyList.add(temp);
        temp = new Title("Grade 10 Rep",10);
        titles.add(Arrays.asList(
                new Person("Stanley\nHu",R.drawable.stuco_stanleyhu),
                new Person("Jeff\nNakanishi",R.drawable.stuco_jeffnakanishi)
        ));
        keyList.add(temp);
        temp = new Title("Grade 11 Rep",11);
        titles.add(Arrays.asList(
                new Person("Catherine\nTsai",R.drawable.stuco_catherinetsai),
                new Person("Matthew\nTurner",R.drawable.group_icon)
        ));
        keyList.add(temp);
        temp = new Title("Grade 12 Rep",12);
        titles.add(Arrays.asList(
                new Person("Sherry\nTsui",R.drawable.stuco_sherrytsui),
                new Person("Leo\nFu",R.drawable.group_icon)
        ));
        keyList.add(temp);

        //body.addView(new ElectionItem(ElectionPage.this, R.drawable.yes, "becky").content);

        SharedPreferences gradeReader = getSharedPreferences("clubs", Context.MODE_PRIVATE);
        for(int i = 0; i < keyList.size(); i++) {
            Title key = keyList.get(i);
            List<Person> value = titles.get(i);
            //Log.d("electionpage",key.name+value.toString());
            boolean shown = true;
            if(key.grade!=0 &&
                    key.grade != gradeReader.getInt("grade",0)){
                shown = false;
            }

            if(shown){
                LayoutInflater inflater = getLayoutInflater();
                View root = inflater.inflate(R.layout.election_row, null);
                ((TextView)root.findViewById(R.id.election_row_title)).setText(key.name);
                RadioGroup rg = (RadioGroup)root.findViewById(R.id.election_row_radio);
                nominees.add(rg);
                for(Person j : value){
                    ElectionItem rb = new ElectionItem(ElectionPage.this, j.ImageID, j.name, rg);
                    rg.addView(rb.content);
                }
                body.addView(root);
                shownTitles.add(key.name);
            }
        }

    }

    public void printSelections(){
        final ArrayList<String> ppl = new ArrayList<>(10);
        StringBuilder nameList = new StringBuilder();
        for(int i = 0; i < shownTitles.size(); i++){
            nameList.append(shownTitles.get(i)+": ");
            for(int j = 0; j < nominees.get(i).getChildCount(); j++){
                if(((ElectionItem)nominees.get(i).getChildAt(j).getTag()).isChecked()){
                    nameList.append(((ElectionItem) nominees.get(i).getChildAt(j).getTag()).name
                            .replace('\n',' '));
                    ppl.add(((ElectionItem) nominees.get(i).getChildAt(j).getTag()).name
                            .replace('\n',' '));
                    break;
                }
            }
            nameList.append("\n");
        }
        AlertDialog print = new AlertDialog.Builder(ElectionPage.this)
                .setPositiveButton("yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                vote(ppl);
            }})
                .setNegativeButton("no", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

            }})
                .setMessage("WARNING: This will overwrite your previous votes!\n" + nameList)
                .setCancelable(true)
                .setTitle("Confirmation")
                .create();
        print.show();
    }

    public void vote(ArrayList<String> people){
        //todo somehow put the selected list online
        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            if (!c.after(sdf.parse("2018-09-20"))) {
                Toast.makeText(ElectionPage.this, "Election hasn't started",
                        Toast.LENGTH_SHORT).show();
                return;
            }
        }catch(ParseException e){}
        SharedPreferences ver = this.getSharedPreferences("verified", this.MODE_PRIVATE);
        String id = ver.getString("account", "none");

        final Activity act = this;
        AVObject message = new AVObject("StucoVotes2018");
        message.put("studentID", id);
        message.put("votedCandidates", people);
        message.saveInBackground(new SaveCallback() {
            @Override
            public void done(AVException e) {
                if(e==null){
                    Toast.makeText(act, "Votes confirmed",
                            Toast.LENGTH_SHORT).show();
                    finish();
                }
                else{
                    Toast.makeText(act, "An error has occured while sending.\nDraft saved.",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

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

    public static class Title{
        String name;
        int grade;
        public Title(String n, int g){name = n; grade = g;}
    }

    public static class Person{
        String name;
        int ImageID;
        public Person(String n, int id){name = n; ImageID = id;}
        public String toString(){return name;}
    }
}
