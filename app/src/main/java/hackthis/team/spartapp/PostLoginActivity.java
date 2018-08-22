package hackthis.team.spartapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.FindCallback;

import java.util.List;

public class PostLoginActivity extends AppCompatActivity{

    View.OnClickListener sign_in = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            SharedPreferences sp = getSharedPreferences("clubs",Context.MODE_PRIVATE);

            String search_root = sp.getString("school","");
            if(search_root.equals("THIS")){
                search_root = "";
            }
            else{
                search_root = "_"+search_root;
            }

            AVQuery<AVObject> query = new AVQuery<>("Clubs"+search_root);
            final EditText et = (EditText) findViewById(R.id.post_club_name);
            final EditText pt = (EditText) findViewById(R.id.post_club_pass);
            String club = et.getText().toString();
            query.whereMatches("name", club);
            query.findInBackground(new FindCallback<AVObject>() {
                @Override
                public void done(List<AVObject> list, AVException e) {
                    if(e==null) {
                        if (list.size() != 1) {
                            Toast t = Toast.makeText(PostLoginActivity.this, "club cannot be found", Toast.LENGTH_SHORT);
                            t.setGravity(Gravity.TOP, 0, 50);
                            t.show();
                        } else if (!list.get(0).getString("key").equals(pt.getText().toString())) {
                            Toast t = Toast.makeText(PostLoginActivity.this, "incorrect password", Toast.LENGTH_SHORT);
                            t.setGravity(Gravity.TOP, 0, 50);
                            t.show();
                        }
                        else {
                            SharedPreferences sp = PostLoginActivity.this.getSharedPreferences("post", Context.MODE_PRIVATE);
                            sp.edit().putString("name",et.getText().toString()).
                                    putString("key",pt.getText().toString()).apply();
                            finish();
                        }
                    }
                    else{
                        Toast t = Toast.makeText(PostLoginActivity.this, "error, please connect to the internet", Toast.LENGTH_SHORT);
                        t.setGravity(Gravity.TOP, 0, 50);
                        t.show();
                    }
                }
            });
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.post_login);
        Button b = (Button) findViewById(R.id.post_sign_in);
        b.setOnClickListener(sign_in);
    }

}
