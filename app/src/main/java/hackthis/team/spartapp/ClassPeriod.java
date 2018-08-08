package hackthis.team.spartapp;

public class ClassPeriod {
    /**
     * contains subject and its specific adapter-related stuff (color ID, background ID, period number,
     */
    Subject sub; //content subject
    int colorID; //id of color
    int backgroundID; //id of background drawable
    int period; //0 ~ 7, 1 per half-period (0=1a, 1=1b, etc.)
    boolean focus; //if the adapted list item should be enlarged

    public ClassPeriod(Subject s, int p){
        sub = s;
        period = p;
        focus = false;
        //todo search for corresponding color and background
        colorID = R.color.purple;
        backgroundID = R.drawable.course_art;
    }


}
