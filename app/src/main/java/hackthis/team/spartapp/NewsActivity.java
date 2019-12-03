package hackthis.team.spartapp;

import android.app.Activity;
import android.app.FragmentContainer;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.just.agentweb.AgentWeb;

public class NewsActivity extends Activity {

    Activity context;

    private View.OnClickListener back = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent bacc = new Intent(context, MainActivity.class);
            bacc.putExtra("mode","news");
            startActivity(bacc);
        }
    };

    //view components
    FrameLayout content;
    AgentWeb mAgentWeb;
    ImageButton backbutton;

    //error constants
    static final int NO_URL = 114514;
    static final String[] error_message =
            {
                    "No Online Content",
                    "Invalid Internet Connection"
            };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = NewsActivity.this;

        setContentView(R.layout.news_viewer);

        //get url from news object
        Bundle extras = getIntent().getExtras();
        String URL;
        content = findViewById(R.id.news_viewer_content);
        backbutton = findViewById(R.id.news_viewer_back_button);
        backbutton.setOnClickListener(back);
        if (extras != null && (URL = extras.getString("url"))!=null) {
            //agentweb plugin webviewer, see: https://github.com/Justson/AgentWeb/blob/master/README-ENGLISH.md
            //                              and https://github.com/Justson/AgentWeb
            mAgentWeb = AgentWeb.with(this)//传入Activity or Fragment
                    .setAgentWebParent(content, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))//Incoming AgentWeb parent control, if the parent control is RelativeLayout, then the second parameter needs to be passed RelativeLayout.LayoutParams, the first parameter and the second parameter should correspond.
                    .useDefaultIndicator()// use the default onProgress bar
                    //.setReceivedTitleCallback(mCallback) //Set the Web page title callback
                    .additionalHttpHeader("abc","def")
                    .createAgentWeb()//
                    .ready()
                    .go(URL);
            LogUtil.d("news",URL);
        }
        else {
            displayError(NO_URL);
        }

    }

    //when something wrong happens
    public void displayError(int error_code){
        LogUtil.d("news",Integer.toString(error_code));
        switch (error_code){
            case(NO_URL):{
                //just quit the thing...
                Toast.makeText(getApplicationContext(), "no webpage attached",Toast.LENGTH_SHORT).show();
                backbutton.callOnClick();
            }
            default:{

            }
        }
    }

    //please refer to the fragment lifecycle for the functions below

    @Override
    protected void onPause () {
        if(mAgentWeb!=null) mAgentWeb.getWebLifeCycle().onPause();
        super.onPause();

    }

    @Override
    protected void onResume () {
        if(mAgentWeb!=null) mAgentWeb.getWebLifeCycle().onResume();
        super.onResume();
    }

    @Override
    protected void onDestroy(){
        if(mAgentWeb!=null) mAgentWeb.getWebLifeCycle().onDestroy();
        super.onDestroy();
    }

}
