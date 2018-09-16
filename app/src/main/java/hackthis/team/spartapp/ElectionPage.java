package hackthis.team.spartapp;

import android.app.Activity;
import android.app.AlertDialog;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVFile;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.FindCallback;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ElectionPage extends Activity {

    HashMap<Title, List<Person>> titles;
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
        titles = new HashMap<>(10);

        // grade 0 means the title will not be filtered by grade
        /*titles.put(new Title("bird",0), Arrays.asList(
                new Person("becky\nbeckyson",R.drawable.yes),
                new Person("ron\nronson",R.drawable.no),
                new Person("ben\nbenson",R.drawable.group_icon)
        ));

        titles.put(new Title("heavier",0), Arrays.asList(
                new Person("feathers\nbeckyson",R.drawable.yes),
                new Person("steel\nronson",R.drawable.no),
                new Person("yo mama\nbenson",R.drawable.group_icon),
                new Person("thing\nronson",R.drawable.no),
                new Person("thing2\nronson",R.drawable.no)
        ));*/

        //body.addView(new ElectionItem(ElectionPage.this, R.drawable.yes, "becky").content);

        SharedPreferences gradeReader = getSharedPreferences("clubs",MODE_PRIVATE);
        for(Map.Entry<Title, List<Person>> i : titles.entrySet()) {
            Title key = i.getKey();
            List<Person> value = i.getValue();
            Log.d("electionpage",key.name+value.toString());
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
                    //todo inflate whatever
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
                            .replace('\n',' ')+"\n");
                    ppl.add(((ElectionItem) nominees.get(i).getChildAt(j).getTag()).name
                            .replace('\n',' '));
                    break;
                }
            }
        }
        AlertDialog print = new AlertDialog.Builder(ElectionPage.this)
                .setPositiveButton("yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                vote(ppl);
            }})
                .setNegativeButton("no", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

            }})
                .setMessage("WARNING: You can only vote once!\n" + nameList)
                .setCancelable(true)
                .setTitle("Confirmation")
                .create();
        print.show();
    }

    public void vote(ArrayList<String> people){
        //todo somehow put the selected list online
        Toast.makeText(ElectionPage.this,"Election hasn't started",
                Toast.LENGTH_SHORT).show();
    }

    public void handleError(String err){
        setContentView(R.layout.activity_election_page_after);
        TextView message = findViewById(R.id.election_page_error_message);
        message.setText(err);
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
