package hackthis.team.spartapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Checkable;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ElectionItem implements Checkable {

    private boolean checked;

    public RelativeLayout content;
    public TextView mText;
    public ImageView mImage;

    public RadioGroup parent;

    String name;

    public static float SOLID = 1.0f, TRANS = 0.6f;

    public ElectionItem(Context context, int picID, String n, RadioGroup p){
        checked = false;

        parent = p;

        name = n;

        LayoutInflater inflater = LayoutInflater.from(context);
        content = (RelativeLayout)inflater.inflate(R.layout.election_item, p, false);
        mImage = (ImageView) content.findViewById(R.id.election_item_profile);
        mText = (TextView) content.findViewById(R.id.election_item_name);
        content.setTag(this);

        mImage.setImageResource(picID);
        mText.setText(name);

        content.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*if(!isChecked()) {
                    for (int i = 0; i < parent.getChildCount(); i++) {
                        if(((ElectionItem)parent.getChildAt(i).getTag()).isChecked())
                            ((ElectionItem)parent.getChildAt(i).getTag()).toggle();
                    }
                }*/
                toggle();
            }
        });

        //setBackgroundResource(R.drawable.button_background);

        content.setAlpha(TRANS);
    }

    public boolean isChecked(){return checked;}

    public void setChecked(boolean b){checked = b; toggleAlpha();}

    public void toggle(){checked = !checked;toggleAlpha();}

    public void toggleAlpha (){
        content.setAlpha(checked? SOLID : TRANS);
    }
}
