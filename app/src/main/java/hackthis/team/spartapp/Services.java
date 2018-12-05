package hackthis.team.spartapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

public class Services extends RefreshableFragment {

    Context mActivity;

    FrameLayout election;

    View.OnClickListener toElection = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Toast.makeText(mActivity,"Election is over!",Toast.LENGTH_SHORT).show();
                //startActivity(new Intent(mActivity, ElectionPage.class));
        }
    };

    @Override
    public void onStart() {
        super.onStart();
    }

    public void refresh(){

    }

    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = (Activity) context;
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.services, container, false);
        election = root.findViewById(R.id.services_election);
        election.setOnClickListener(toElection);
        return root;
    }
}
