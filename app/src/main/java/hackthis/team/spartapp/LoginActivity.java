package hackthis.team.spartapp;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;

import android.os.AsyncTask;

import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.webkit.ValueCallback;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;

import org.apache.commons.lang3.StringEscapeUtils;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity{

    /**
     * A dummy authentication store containing known user names and passwords.
     * TODO: remove after connecting to a real authentication system.
     */
    private static final String[] DUMMY_CREDENTIALS = new String[]{
            "foo@example.com:hello", "bar@example.com:world"
    };
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //TODO：this does not delete the title bar for some reason, replace
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_login);
        // Set up the login form.
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        SharedPreferences prefs = this.getSharedPreferences("hackthis.team.spartapp", Context.MODE_PRIVATE);
        prefs.edit().putString("account", email).apply();
        prefs.edit().putString("password", password).apply();

        try {
            openWebView(email, password);
        }
        catch(Exception e){
            Log.d("ERR", "open webview failed");
        }
        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.

            //(deleted progress spinner 2018.8.8)
            mAuthTask = new UserLoginTask(email, password);
            mAuthTask.execute((Void) null);
        }
    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return true;
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }

    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mEmail;
        private final String mPassword;

        UserLoginTask(String email, String password) {
            mEmail = email;
            mPassword = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.

            try {
                // Simulate network access.
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                return false;
            }

            for (String credential : DUMMY_CREDENTIALS) {
                String[] pieces = credential.split(":");
                if (pieces[0].equals(mEmail)) {
                    // Account exists, return true if the password matches.
                    return pieces[1].equals(mPassword);
                }
            }

            // TODO: register the new account here.
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;

            if (success) {
                finish();
            } else {
                mPasswordView.setError(getString(R.string.error_incorrect_password));
                mPasswordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
        }
    }

    public void openWebView(String account, String password) throws InterruptedException, AVException, ParseException {

        Log.d("Demo", "webview starting");
        //fetch 6-day schedule
        final String account_ = account;
        final String password_ = password;
        final WebView webView = new WebView(this.getApplicationContext());

        final boolean[] spellCasted = {false};
        final boolean[] timeout = {false};

        final Timer timer = new Timer();
        TimerTask tt = new TimerTask(){
            public void run(){
                Log.d("TIME_", "timeout");
                timeout[0] = true;
                //ls.errorTextInternet();
                timer.purge();
                timer.cancel();
                Log.d("TIME_", "timer purged and cancelled");
            }
        };
        timer.schedule(tt, 10000, 1);

        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                if(!spellCasted[0]){
                    Log.d("HTML", url + " onpagefinished()");
                    webView.evaluateJavascript("document.getElementById('fieldAccount').value='"+account_+"'", null);
                    webView.evaluateJavascript("document.getElementById('fieldPassword').value='"+password_+"'", null);
                    webView.evaluateJavascript("document.getElementById('btn-enter').click();", null);
                    spellCasted[0] = true;
                }
                else if(!timeout[0]){
                    webView.evaluateJavascript(
                            "(function() { return ('<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>'); })();",
                            new ValueCallback<String>() {
                                @Override
                                public void onReceiveValue(String html_) {
                                    try{
                                        //internet works, fetch calendar on this thread
                                        AVQuery query = new AVQuery("UpdateCalendar");
                                        List<AVObject> qList = query.find();
                                        String startOfYear = qList.get(0).getString("yearStart");
                                        HashMap<String, Integer> dateDay;

                                        //write day list
                                        dateDay = fetchDateDayPairs(startOfYear);
                                        writeDateDayPairs(dateDay);

                                        String html = StringEscapeUtils.unescapeJava(html_);
                                        //Log.d("HTML", html);
                                        // if(!timedOut[0])
                                        writeWeeklySchedule(html);

                                    }
                                    catch(Exception e){
                                        Log.d("HTML", "escape failed");
                                    }
                                }
                            });
                }
            }
        });

        webView.loadUrl("https://power.this.edu.cn/public/home.html");
        Log.d("HTML", "load url");

    }

    public HashMap<String, Integer> fetchDateDayPairs(String startOfYear) throws ParseException, AVException {
        HashMap<String, Integer> dateDay = new HashMap<>(0);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar c = Calendar.getInstance();
        c.setTime(sdf.parse(startOfYear));
        List<AVObject> schoolDays = null;
        schoolDays = getWkDayList();
        for(AVObject schoolDay : schoolDays){
            String time = sdf.format(c.getTime());
            Integer day = -1;
            day = schoolDay.getInt("dayInCycle");
            dateDay.put(time, day);
            c.add(Calendar.DATE, 1);
            Log.d("WKD_", time  + " " + day);
        }
        return dateDay;
    }

    public void writeDateDayPairs(HashMap<String, Integer> dateDay){
        //File file = new File("DateDayPairs.dat");
        //Log.d("WKD", new Boolean(file.canWrite()).toString());
        try {
            FileOutputStream f = this.openFileOutput("date_day.dat", this.MODE_PRIVATE);
            ObjectOutputStream s = new ObjectOutputStream(f);
            s.writeObject(dateDay);
            s.close();
            Log.d("WKD", "output success");
        }
        catch(FileNotFoundException e){Log.d("WKD", "witchcraft");}
        catch(IOException e){Log.d("WKD", "unknown powers");}
    }

    public List<AVObject> getWkDayList()throws AVException {
      /*  AVQuery calendar = new AVQuery("DayCycle");
        calendar.limit(1000);
        List<AVObject> schoolDays = calendar.find();
        schoolDays = QSDateHelper(schoolDays);
        Log.d("WKD_", new Integer(schoolDays.size()).toString());
        return schoolDays;
*/
        AVQuery calendar = new AVQuery("Calendar");
        List<AVObject> schoolDays = calendar.find();
        List<AVObject> dayCycle = new ArrayList<AVObject>(0);

        schoolDays = QSDateHelper(schoolDays);
        int schoolDayCount = -1;
        int days = -1;
        for(AVObject schoolDay : schoolDays){
            for(Boolean isDay : (List<Boolean>)schoolDay.getList("weeklyCalendar")) {
                AVObject schoolday = new AVObject("Void");
                schoolday.put("daysSince", days);
                if (isDay){
                    schoolday.put("dayInCycle", schoolDayCount % 6 + 1);
                    schoolDayCount ++;
                }
                else {
                    schoolday.put("dayInCycle", -1);
                }
                days++;
                //schoolday.saveInBackground();
                dayCycle.add(schoolday);
                //Log.d("WKD", new Boolean(isDay).toString() + " " + new Integer(schoolDayCount%6+1));
            }
        }
        return dayCycle;
    }

    public static void triggerRebirth(Context context) {
        Intent mStartActivity = new Intent(context, MainActivity.class);
        Log.d("EASTER", "how long will this go on...");
        int mPendingIntentId = 123456;
        PendingIntent mPendingIntent = PendingIntent.getActivity(context, mPendingIntentId, mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager mgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);

        Log.d("HTML", "triggering rebirth");

        System.exit(0);
    }

    public void writeWeeklySchedule(String html) throws Exception{
        HashMap<Integer, Subject[]> schedule = fetchSchedule(html);
        Log.d("HTML", html);
        Log.d("HTML_OUT", "called");
        if(schedule.get(1)==null){
            Log.d("HTML_OUT", "schedule.get(1) returned null" );
            //ls.errorTextLogin();
            return;
        }

        FileOutputStream f = this.openFileOutput("week_schedule.dat", Context.MODE_PRIVATE);
        Log.d("HTML_OUT", "output found");
        PrintWriter out = new PrintWriter(f);
        for(int i = 1; i < 7; i ++) {
            Subject[] day = schedule.get(i);
            for (Subject subject : day) {
                if(subject != null) {
                    out.write(subject.name() + "?" + subject.teacher() + "?" + subject.room() + "?");
                    Log.d("HTML_OUT", subject.name() + "?" + subject.teacher() + "?" + subject.room() + "?");
                }
                else{
                    out.write("Study Hall?None?Library?");
                    Log.d("HTML_OUT", "Study Hall?None?Library?");
                }
            }
            out.write("\n");
        }

        out.close();
        Log.d("HTML_OUT", "output success");

        triggerRebirth(this.getApplicationContext());
    }

    public HashMap<Integer, Subject[]> fetchSchedule(String html) throws IOException, InterruptedException {

        String pageSource = html;

        HashMap<Integer, Subject[]> schedule = new HashMap<Integer, Subject[]>(0);

        int inx = 0;
        String afterLastCcid = pageSource;

        Log.d("HTML", "parsing...\n");
        Log.d("HTML", new Integer(pageSource.indexOf("ccid")).toString());
        while( (inx = afterLastCcid.indexOf("ccid")) != -1 ) {

            afterLastCcid = afterLastCcid.substring(inx+4);

            //extract period info
            int inxOfTd = afterLastCcid.indexOf("<td>");
            int inxOfEndTd = afterLastCcid.indexOf("</td>");
            String periodSeq = afterLastCcid.substring(inxOfTd+4, inxOfEndTd);

            //extract class info
            int inxStart = afterLastCcid.indexOf("<td");
            for(int i = 0; i < 15; i ++)
                inxStart += afterLastCcid.substring(inxStart+1).indexOf("<td") + 1;
            int inxOfQuote = afterLastCcid.substring(inxStart).indexOf(";")+inxStart;
            String className = afterLastCcid.substring(inxStart + 17, inxOfQuote-5);
            Log.d("BUH", className);
            int InxOfAnd;
            if((InxOfAnd = className.indexOf("&")) != -1)
                className = className.substring(0, InxOfAnd) + className.substring(InxOfAnd+6);

            //extract teacher info
            int inxOfDetails = afterLastCcid.indexOf("Details about");
            int inxOfClsBtn = afterLastCcid.indexOf("class=\"button mini");
            String teacherName = afterLastCcid.substring(inxOfDetails + 14, inxOfClsBtn - 2);

            //extract room info
            int inxOfRm = afterLastCcid.indexOf("Rm:");
            String roomNum = afterLastCcid.substring(inxOfRm + 3, inxOfRm + 8);

            //System.out.println(periodSeq + " " + className + " " + teacherName + " " + roomNum);

            //parse period
            //Log.d("HTML", "still parsing1");

            String periodInfo = periodSeq;
            while(true) {
                //Log.d("HTML", "still parsing2");
                String days = periodInfo.substring(periodInfo.indexOf("(")+1, periodInfo.indexOf(")"));
                Log.w("HTML", days);
                for(int i = 0; i * 2 < days.length(); i ++) {
                    int dayNum = days.charAt(i*2) - 48;
                    Subject period = new Subject(className, teacherName, roomNum);

                    int pN, pC, pNe, pCe;
                    try {
                        pN = Integer.parseInt(periodInfo.substring(0, 1));
                        pC = periodInfo.substring(1, 2).equals("A") ? 0 : 1;
                        pNe = Integer.parseInt(periodInfo.substring(3, 4));
                        pCe = periodInfo.substring(4, 5).equals("A") ? 0 : 1;
                    }
                    catch(NumberFormatException e) {
                        break;
                    }

                    if(!schedule.containsKey(new Integer(dayNum))){
                        schedule.put(new Integer(dayNum), new Subject[8]);
                    }
                    schedule.get(new Integer(dayNum))[(pN-1)*2 + pC] = period;
                    schedule.get(new Integer(dayNum))[(pNe-1)*2 + pCe] = period;
                }

                int endInx = periodInfo.indexOf(")");
                //Log.w("HTML", new Integer(endInx).toString());
                try {
                    periodInfo = periodInfo.substring(endInx + 2);
                }
                catch(StringIndexOutOfBoundsException e) {
                    break;
                }
            }

        }
        Log.d("HTML", "done parsing");
        return schedule;
    }

    public List<AVObject> QSDateHelper(List<AVObject> arr){
        QuickSortDate(arr, 0, arr.size()-1);
        for(AVObject i : arr)
            Log.d("SORT","sorted " +i.getInt("daysSince"));
        return arr;
    }

    public void QuickSortDate(List<AVObject> arr, int low, int high){
        for(int k = low; k <= high; k++){
        }
        if(arr==null || high-low <1 || high<=low){
            return;
        }
        AVObject midValue = arr.get(low);
        int i = low, j = high;
        while(true){
            while(i<j && arr.get(j).getInt("daysSince")>=midValue.getInt("daysSince")){
                j--;
            }
            while(i<j && arr.get(i).getInt("daysSince")<=midValue.getInt("daysSince")){
                i++;
            }
            if(i<j){
                AVObject temp = arr.get(i);
                arr.set(i, arr.get(j));
                arr.set(j, temp);
            }
            else{
                AVObject temp = arr.get(low);
                arr.set(low, arr.get(i));
                arr.set(i, temp);
                break;
            }
        }
        QuickSortDate(arr, low, i-1);
        QuickSortDate(arr, i+1, high);
    }

    class AVObjectComparator implements Comparator<AVObject> {
        @Override
        public int compare(AVObject obj1, AVObject obj2){
            return obj1.getInt("daysSince") - obj2.getInt("daysSince");
        }

    }
}

