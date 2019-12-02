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
import android.widget.Toast;

import cn.leancloud.AVException;
import cn.leancloud.AVOSCloud;
import cn.leancloud.AVObject;
import cn.leancloud.AVQuery;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

import org.apache.commons.lang3.StringEscapeUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

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

    //school schedule settings
    private int cycleLen;

    private final boolean[] threadCompleted = {false};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
                    LogUtil.d("HTML", "end then clicked");
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
                LogUtil.d("HTML", "end then clicked");
            }
        });

        SharedPreferences p = this.getSharedPreferences("clubs", Context.MODE_PRIVATE);
        String schl = p.getString("school", "THIS");
        if(schl.equals("THIS")) cycleLen = 6;
        else cycleLen = 8;

        SharedPreferences sp = getSharedPreferences("login", Context.MODE_PRIVATE);
        Boolean loginFailed = sp.getBoolean("failed", false);
        if(loginFailed){
            mEmailView.setError("Failed to download schedule; Please try again");
        };
        SharedPreferences prefs = this.getSharedPreferences("hackthis.team.spartapp", Context.MODE_PRIVATE);
        String username = prefs.getString("account", "");
        String password = prefs.getString("password", "");
        mEmailView.setText(username);
        mPasswordView.setText(password);

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
            //The following code doesnt do anything and causes a crash in some cases,
            //much like myself.
                //mAuthTask = new UserLoginTask(email, password);
                //mAuthTask.execute((Void) null);
        }
        try {
            openWebView(email, password);
            LogUtil.d("HTML", "out of openweb");
        }
        catch(Exception e){
            LogUtil.d("ERR", "open webview failed");
        }
        LogUtil.d("HTML", "reached the end");
    }


    private boolean isEmailValid(String email) {
        return true;
    }

    private boolean isPasswordValid(String password) {
        return !(password==null);
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

    public void openWebView(String account_, String password_) throws InterruptedException, AVException, ParseException {
        final WebView webView = new WebView(this.getApplicationContext());
        LogUtil.d("SCHEDULE", "webview starting");
        webView.getSettings().setUserAgentString("Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:25.0) Gecko/20100101 Firefox/25.0");
        final String account = account_;
        final String password = password_;
        final boolean[] pastLoginPage = {false};
        final boolean[] timeout = {false};
        SharedPreferences sp = getSharedPreferences("clubs", Context.MODE_PRIVATE);
        final String occ = sp.getString("occupation", "student");
        final String schl = sp.getString("school", "THIS");
        LogUtil.d("OCCUPATION", occ);
        final Context context = this;

        final Timer timer = new Timer();
        TimerTask tt = new TimerTask(){
            public void run(){
                LogUtil.d("WVTIME", "time's up");
                timeout[0] = true;
                timer.purge();
                timer.cancel();
                LogUtil.d("WVTIME", "timer purged and cancelled; re-login");
                Intent login = new Intent(context, LoginActivity.class);
                SharedPreferences sp = getSharedPreferences("login", Context.MODE_PRIVATE);
                sp.edit().putBoolean("failed",true).apply();
                startActivity(login);
            }
        };
        //timer.schedule(tt, 10000, 1);

        final String url_ = occ.equals("student")?(
                schl.equals("THIS")?"https://power.this.edu.cn/public/home.html":
                    "https://sis.isb.bj.edu.cn/public/home.html")
                :"https://power.this.edu.cn/teachers/pw.html";

        webView.getSettings().setJavaScriptEnabled(true);
        //2019.03.25, decided to show webview so it looks like something is happening
        webView.setEnabled(false);
        webView.setClickable(false);
        setContentView(webView);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                if(!pastLoginPage[0]){
                    LogUtil.d("HTML", url + " page finished");
                    String usrField = (occ.equals("student"))?"fieldAccount":"fieldUsername";
                    String btnName = (occ.equals("student"))?("btn-enter-sign-in"):"btnEnter";
                    LogUtil.d("HTML", usrField);
                    webView.evaluateJavascript("document.getElementById('"+usrField+"').value='"+account+"'", null);
                    webView.evaluateJavascript("document.getElementById('fieldPassword').value='"+password+"'", null);
                    webView.evaluateJavascript("document.getElementById('"+btnName+"').click();", null);
                    pastLoginPage[0] = true;
                    webView.evaluateJavascript(
                            "(function() { return ('<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>'); })();",
                            new ValueCallback<String>() {
                                @Override
                                public void onReceiveValue(String html_) {
                                    try{
                                        //Log.d("HTML", html_);
                                        //internet works, fetch calendar on this thread
                                        if(html_.contains("Grades and Attendance")||html_.contains("Current Classes")) {
                                            LogUtil.d("HTML", html_);
                                            output(html_, account, password, occ, schl, context);
                                        }
                                    }
                                    catch(Exception e){
                                        webView.loadUrl(url_);
                                    }
                                }
                            });

                }
                else if(!timeout[0]){
                    Log.d("HTML", "ligma");
                    LogUtil.d("HTML", "logged in");
                    webView.evaluateJavascript(
                            "(function() { return ('<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>'); })();",
                            new ValueCallback<String>() {
                                @Override
                                public void onReceiveValue(String html_) {
                                    try{
                                        //internet works, fetch calendar on this thread
                                        Log.d("HTML", "ligma2");
                                        if(html_.contains("Grades and Attendance")||html_.contains("Current Classes")){
                                            LogUtil.d("HTML", html_);
                                            output(html_, account, password, occ, schl, context);
                                        }
                                        else{throw new Exception();}
                                    }
                                    catch(Exception e){
                                        //pun intended
                                        LogUtil.d("HTML", "escape failed");
                                        pastLoginPage[0] = false;
                                        webView.loadUrl(url_);
                                    }
                                }
                            });
                }
            }
        });
        LogUtil.d("HTML", occ);
        webView.loadUrl(url_);
        LogUtil.d("HTML", "initiated webview operations");
    }

    public void output(String html_, String account_, String password_, String occ_, String schl_, final Context context) throws Exception{
        final String account = account_;
        final String password = password_;
        final String occ = occ_;
        final String schl = schl_;
        final Context[] ctxt_ = {context};
        Log.d("HTML", "pa");
        //String html = StringEscapeUtils.unescapeJava(html_);
        final String html = StringEscapeUtils.unescapeJava(html_);
        AVQuery query = new AVQuery("UpdateCalendar");
        //breaks here
        query.findInBackground().subscribe(new Observer() {
            public void onSubscribe(Disposable disposable) {}

            //@Override
            public void onNext(Object o) {
                List<AVObject> qlist = (List<AVObject>)o;
                String startOfYear = qlist.get(0).getString("startOfYear");
                //my lazy ass getting the time when first semester ends
                String midYear = qlist.get(1).getString("startOfYear");


                try {
                    LogUtil.d("HTML", html);
                    Object[] weeklySchedules = fetchScheduleStudent(html);
                    HashMap<Integer, Subject[]> schedule_sem1 = (HashMap<Integer, Subject[]>)weeklySchedules[0];
                    HashMap<Integer, Subject[]> schedule_sem2 = (HashMap<Integer, Subject[]>)weeklySchedules[1];
                    writeWeeklySchedule(schedule_sem1, "weeklySchedule_sem1.dat");
                    writeWeeklySchedule(schedule_sem2, "weeklySchedule_sem2.dat");
                    getCalendar(startOfYear, midYear, context);
                }
                catch(Exception e){e.printStackTrace();}
                SharedPreferences prefs = getApplicationContext().getSharedPreferences("verified", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("account", account);
                editor.putString("password", password);
                //must use commit instead of apply here
                editor.commit();
                threadCompletion(ctxt_[0]);
            }

            @Override
            public void onError(Throwable e) {}

            @Override
            public void onComplete() {}
        });

    }

    public void writeDateDayPairs(HashMap<String, Integer> pairs) throws IOException{
        FileOutputStream f = this.openFileOutput("date_day.dat", this.MODE_PRIVATE);
        ObjectOutputStream s = new ObjectOutputStream(f);
        LogUtil.d("CALENDAR", "writing date-day pairs");
        s.writeObject(pairs);
        s.close();
        LogUtil.d("CALENDAR", "wrote date-day pairs");
    }

    public void getCalendar(String startOfYear_, String midYear_, Context context) throws AVException {
        final Context[] ctxt_ = {context};
        final String startOfYear = startOfYear_;
        final String midYear = midYear_;
        AVQuery calendar = new AVQuery("Calendar");
        calendar.findInBackground().subscribe(new Observer() {
            public void onSubscribe(Disposable disposable) {}

            //@Override
            public void onNext(Object o) {
                List<AVObject> weeks_ = (List<AVObject>)o;
                AVObject[] weeks = new AVObject[53];
                for(int i = 0; i < weeks_.size(); i++){
                    AVObject week = weeks_.get(i);
                    int weekNum = (int)week.get("weekNumber");
                    weeks[weekNum-1] = week;
                }
                List<Integer> days = new ArrayList<>(0);
                LogUtil.d("CALENDAR", "downloaded weekly calendar");
                int curDay = -1;
                for(AVObject week : weeks){
                    if(week == null) break;
                    Log.d("wkorder", week.get("weekNumber").toString());
                    for(Boolean isDay : (List<Boolean>)week.getList("weeklyCalendar")) {
                        //Log.d(week.getClassName());
                        if(isDay) {
                            days.add(curDay+1);
                            curDay = (curDay+1) % cycleLen;
                        }
                        else days.add(-1);
                    }
                }
                LogUtil.d("CALENDAR", "generated daily calendar");
                HashMap<String, Integer> pairs = new HashMap<>(0);
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                Calendar c = Calendar.getInstance();
                try{c.setTime(sdf.parse(startOfYear));} catch(Exception e){e.printStackTrace();}
                LogUtil.d("CALENDAR", "pairing day cycle with calendar dates");
                int i =0;
                for(Integer day : days){
                    String date = sdf.format(c.getTime());
                    Calendar my = Calendar.getInstance();
                    try{my.setTime(sdf.parse(midYear));} catch(Exception e){e.printStackTrace();}
                    if(c.before(my))
                        pairs.put(date, day);
                    else
                        pairs.put(date, day+20); //day 1 in second semester is day 21, 2 is 22, so on
                    LogUtil.d("CALENDAR", date.toString() + " " +day.toString()+" "+i);
                    c.add(Calendar.DATE, 1);
                    i++;
                }
                LogUtil.d("CALENDAR", "paired day cycle with calendar dates");
                try{writeDateDayPairs(pairs);} catch(Exception e){e.printStackTrace();}
                threadCompletion(ctxt_[0]);
            }
            @Override
            public void onError(Throwable e) {}
            @Override
            public void onComplete() {}
        });
    }

    public void writeWeeklySchedule(HashMap<Integer, Subject[]> schedule, String filename) throws Exception{
        if(schedule.get(1)==null){
            LogUtil.d("SCHEDULE", "schedule is empty" );
            throw new Exception();
        }
        FileOutputStream f = this.openFileOutput(filename, Context.MODE_PRIVATE);
        PrintWriter out = new PrintWriter(f);
        LogUtil.d("SCHEDULE", "preparing to write week_schedule.dat" );
        for(int i = 1; i < cycleLen+1; i ++) {
            Subject[] day = schedule.get(i);
            for (Subject subject : day) {
                if(subject != null) {
                    out.write(subject.name() + "?" + subject.teacher() + "?" + subject.room() + "?");
                    LogUtil.d("SCHEDULE", subject.name() + "?" + subject.teacher() + "?" + subject.room() + "?");
                }else {
                    SharedPreferences sp = getSharedPreferences("clubs", Context.MODE_PRIVATE);
                    final String occ = sp.getString("occupation", "student");
                    final String schl = sp.getString("school", "THIS");
                    if(occ=="student"&&schl=="THIS") out.write("Study Hall?-?-?");
                    else out.write("--?-?-?");
                    LogUtil.d("SCHEDULE", "empty block");
                }
            }
            out.write("\n");
        }
        out.close();
        LogUtil.d("SCHEDULE", "wrote week_schedule.dat");
    }

    public Object[] fetchScheduleStudent(String html) throws IOException, InterruptedException {
        HashMap<Integer, Subject[]> schedule_sem1 = new HashMap<Integer, Subject[]>(0);
        HashMap<Integer, Subject[]> schedule_sem2 = new HashMap<Integer, Subject[]>(0);

        LogUtil.d("HTML", "parsing html source");
        Document doc = Jsoup.parse(html);
        LogUtil.d("HTML", "jsoup parsed");
        Element table = doc.select("table").get(0);
        LogUtil.d("HTML", "table get0ed");
        Elements rows = table.select("tr");
        LogUtil.d("HTML", "table tr selected");
        for(int dayNum = 1; dayNum <= cycleLen; dayNum++) {
            schedule_sem1.put(new Integer(dayNum), new Subject[8]);
            schedule_sem2.put(new Integer(dayNum), new Subject[8]);
        }
        for(int j=2; j<rows.size()-1; j++) {
            try {
                LogUtil.d("HTML", "parsing row");
                Element row = rows.get(j);
                Elements col = row.select("td");
                String periodInfo = col.get(0).text();
                if (periodInfo.contains("HR")) continue;
                String classInfo = col.get(15).text();
                //Log.d("BALLS", classInfo);
                String className = classInfo.substring(0, classInfo.indexOf("Details about") - 1);
                String teacherName = classInfo.substring(classInfo.indexOf("Details about") + 14,
                        classInfo.indexOf("Email") - 1);
                int rmInx = classInfo.indexOf("Rm:");
                String roomNum;
                if (rmInx > 0) roomNum = classInfo.substring(rmInx + 4);
                else roomNum = "-";
                while (true) {
                    String days = periodInfo.substring(periodInfo.indexOf("(") + 1, periodInfo.indexOf(")"));
                    for (int i = 0; i * 2 < days.length(); i++) {
                        int dayNum = days.charAt(i * 2) - 48;
                        Subject period = new Subject(className, teacherName, roomNum);
                        int pN, pC, pNe, pCe;
                        try {
                            pN = Integer.parseInt(periodInfo.substring(0, 1));
                            pC = periodInfo.substring(1, 2).equals("A") ? 0 : 1;

                            if (periodInfo.indexOf("(") > 2) {
                                pNe = Integer.parseInt(periodInfo.substring(3, 4));
                                pCe = periodInfo.substring(4, 5).equals("A") ? 0 : 1;
                            } else {
                                pNe = 0;
                                pCe = 0;
                            }
                        } catch (NumberFormatException e) {
                            break;
                        }
                        LogUtil.d("HTML", "adding row");
                        LogUtil.d("HTML", period.name());
                        //determine semester
                        if (!col.get(16).className().equals("notInSession")){
                            schedule_sem1.get(new Integer(dayNum))[(pN - 1) * 2 + pC] = period;
                            if (periodInfo.indexOf("(") > 2)
                                schedule_sem1.get(new Integer(dayNum))[(pNe - 1) * 2 + pCe] = period;
                            LogUtil.d("HTML", "added row, semester1");
                        }
                        if (!col.get(17).className().equals("notInSession")){
                            schedule_sem2.get(new Integer(dayNum))[(pN - 1) * 2 + pC] = period;
                            if (periodInfo.indexOf("(") > 2)
                                schedule_sem2.get(new Integer(dayNum))[(pNe - 1) * 2 + pCe] = period;
                            LogUtil.d("HTML", "added row, semester2");
                        }
                    }

                    int endInx = periodInfo.indexOf(")");
                    try {
                        periodInfo = periodInfo.substring(endInx + 2);
                    } catch (StringIndexOutOfBoundsException e) {
                        break;
                    }
                }
            }
            catch(Exception e){
                if(j!=2) throw new IOException();
            }
        }
        LogUtil.d("HTML", "done parsing; schedule generated");
        Object[] ret = {schedule_sem1, schedule_sem2};
        return ret;
    }

    public HashMap<Integer, Subject[]> fetchScheduleTeacher(String html){
        HashMap<Integer, Subject[]> schedule = new HashMap<Integer, Subject[]>(0);
        LogUtil.d("HTML", "parsing html source teacher");
        Document doc = Jsoup.parse(html);
        Element table = doc.select("table").get(0);
        Elements rows = table.select("tr");
        for(int dayNum = 1; dayNum <= cycleLen; dayNum++)
            schedule.put(new Integer(dayNum), new Subject[8]);

        for(int j=0; j<rows.size(); j++) {
            Element row = rows.get(j);
            Elements col = row.select("td");
            String periodInfo = col.get(0).text();
            if(periodInfo.contains("HR")) continue;
            String className = col.get(1).text();
            className = className.substring(0, className.length()-16);

            while(true) {
                String days = periodInfo.substring(periodInfo.indexOf("(")+1, periodInfo.indexOf(")"));
                for(int i = 0; i * 2 < days.length(); i ++) {
                    int dayNum = days.charAt(i*2) - 48;
                    Subject period = new Subject(className, " ", " ");
                    int pN, pC, pNe, pCe;
                    try {
                        pN = Integer.parseInt(periodInfo.substring(0, 1));
                        pC = periodInfo.substring(1, 2).equals("A") ? 0 : 1;
                        if(periodInfo.indexOf("(")>2) {
                            pNe = Integer.parseInt(periodInfo.substring(3, 4));
                            pCe = periodInfo.substring(4, 5).equals("A") ? 0 : 1;
                        }else{
                            pNe = 0;
                            pCe = 0;
                        }
                    }
                    catch(NumberFormatException e) {
                        break;
                    }

                    schedule.get(new Integer(dayNum))[(pN-1)*2 + pC] = period;
                    if(periodInfo.indexOf("(")>2)
                        schedule.get(new Integer(dayNum))[(pNe-1)*2 + pCe] = period;
                }

                int endInx = periodInfo.indexOf(")");
                try {
                    periodInfo = periodInfo.substring(endInx + 2);
                }
                catch(StringIndexOutOfBoundsException e) {
                    break;
                }
            }

        }
        return schedule;
    }

    public HashMap<Integer, Subject[]> fetchScheduleISB(String html) throws Exception{
        Document doc = Jsoup.parse(html);

        // get exp code of subject
        Elements exp = doc.select("td:contains((A))");
        exp.addAll(doc.select("td:contains((A-B))"));
        exp.addAll(doc.select("td:contains((B))"));
        ArrayList expList = new ArrayList<Integer>();
        for (Element element : exp) {
            int num = Integer.parseInt(element.text().substring(0, 1));
            expList.add(num);
        }
        if(expList.size()==0) throw new Exception();

        // get subject info
        Elements classInfo = doc.select("[align='left']");
        ArrayList<Subject> subList = new ArrayList<Subject>();

        for (Element element : classInfo) {
            String elname = between(element.text(), "", " Details");
            String elteacher = between(element.text(), "about ", " Email");

            String elroom;

            if (element.text().substring((element.text().length() - 5)).equals("Admin")
                    || element.text().substring((element.text().length() - 1)).equals("D")) {
                elroom = element.text().substring((element.text().length() - 5));
            } else {
                elroom = element.text().substring((element.text().length() - 4));
            }

            Subject subject = new Subject(elname, elteacher, elroom);
            subList.add(subject);
        }

        // stores list of subjects
        HashMap<Integer, Subject> subjects = new HashMap<>();

        for (int i = 0; i < expList.size(); i++) {

            if (!subjects.containsKey(expList.get(i))) {
                subjects.put((Integer) expList.get(i), subList.get(i));
            } else {
                String tempn = subjects.get(expList.get(i)).name;
                String tempt = subjects.get(expList.get(i)).teacher;
                String tempr = subjects.get(expList.get(i)).room;

                subjects.put((Integer) expList.get(i),
                        new Subject(tempn + " / " + subList.get(i).name,
                                tempt + " / " + subList.get(i).teacher,
                                tempr + " / " + subList.get(i).room));
            }
        }

        // stores schedule rotation
        HashMap<Integer, Subject[]> sch = new HashMap<>();

        // 8-day schedule cycle
        Subject[] even = new Subject[4];
        Subject[] odd = new Subject[4];

        for (int b = 0; b < 4; b++) {
            even[b] = subjects.get(b + 1);
            odd[b] = subjects.get(b + 5);
        }

        for (int i = 0; i < 8; i++) {
            Subject[] subs = new Subject[5];

            // 5 periods per day
            if (i % 2 == 0) {
                System.arraycopy(even, 0, subs, 0, 4);
                even = cycle(even);
            } else {
                System.arraycopy(odd, 0, subs, 0, 4);
                odd = cycle(odd);
            }
            subs[4] = subs[3];
            subs[3] = subs[2];
            subs[2] = subjects.get(9);

            sch.put(i + 1, subs);
        }

        return sch;
    }

    // cycling through schedule cycle
    private static Subject[] cycle(Subject[] arr) {
        Subject temp;

        for (int i = 0; i < arr.length - 1; i++) {
            temp = arr[arr.length - 1 - i];
            arr[arr.length - 1 - i] = arr[arr.length - 2 - i];
            arr[arr.length - 2 - i] = temp;
        }
        return arr;
    }

    // for getting class info
    private static String between(String text, String textFrom, String textTo) {
        String result = "";
        result = text.substring(text.indexOf(textFrom) + textFrom.length(), text.length());
        result = result.substring(0, result.indexOf(textTo));
        return result;
    }

    public List<AVObject> QSDateHelper(List<AVObject> arr){
        QuickSortDate(arr, 0, arr.size()-1);
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

    private void threadCompletion(Context context){
        if(threadCompleted[0]) triggerRebirth(context);
        else threadCompleted[0] = true;
    }

    public void triggerRebirth(Context context) {
        SharedPreferences sp = getSharedPreferences("login", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean("failed", false);
        //must use commit instead of apply here
        editor.commit();
        LogUtil.d("REBIRTH", "error state cleared");

        Intent mStartActivity = new Intent(context, MainActivity.class);
        int mPendingIntentId = 123456;
        PendingIntent mPendingIntent = PendingIntent.getActivity(context, mPendingIntentId, mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager mgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        mgr.set(AlarmManager.RTC, System.currentTimeMillis(), mPendingIntent);
        LogUtil.d("REBIRTH", "exiting");
        System.exit(0);
    }
}

