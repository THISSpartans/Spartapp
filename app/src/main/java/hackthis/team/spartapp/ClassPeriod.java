package hackthis.team.spartapp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ClassPeriod {
    /**
     * contains subject and its specific adapter-related stuff (color ID, background ID, period number,
     */
    Subject sub; //content subject
    int colorID; //id of color
    int backgroundID; //id of background drawable
    int period; //0 ~ 7, 1 per half-period (0=1a, 1=1b, etc.)
    boolean focus; //if the adapted list item should be enlarged

    static ArrayList<periodData> pd;

    static
    {
        pd = new ArrayList<>(40);
        pd.add(new periodData(R.drawable.course_art, R.color.art, Arrays.asList(
                "ceramics", "easternart6", "easternart7", "easternart8",
                "easternarti", "easternartii", "foundationsofart", "foundationsofdigitalart",
                "introtosculpture", "paintingi", "paintingii", "paintingiii", "theartportfolio",
                "westernart6", "westernart7", "westernart8"
        )));
        pd.add(new periodData(R.drawable.course_beginningguitar, R.color.music, Arrays.asList(
                "beginningguitar"
        )));
        pd.add(new periodData(R.drawable.course_biology, R.color.biology, Arrays.asList(
                "biology"
        )));
        pd.add(new periodData(R.drawable.course_chinese, R.color.chinese, Arrays.asList(
                "apchinese", "chinese6a", "chinese6b", "chinese7a", "chinese7b", "chinese8a",
                "chinese8b", "chinese9a", "chinese9b", "chinese10a", "chinese10b", "chinese11a",
                "chinese11b", "chinese12a", "chinese12b"
        )));
        pd.add(new periodData(R.drawable.course_choir, R.color.choir, Arrays.asList(
                "choir"
        )));
        pd.add(new periodData(R.drawable.course_computer, R.color.computer, Arrays.asList(
                "apcomputersciencea", "desktoppublishing", "digitalphotography", "digitalvideo",
                "grade6computerfoundations", "grade7computerfoundations", "grade8computerfoundations",
                "introtocomputerscience", "mobileappdesign", "roboticsi", "roboticsii", "webdesign"
        )));
        pd.add(new periodData(R.drawable.course_filmstudies, R.color.filmstudies, Arrays.asList(
                "filmstudyi", "filmstudyii"
        )));
        pd.add(new periodData(R.drawable.course_french, R.color.french, Arrays.asList(
                "frenchi", "frenchii", "frenchiii", "frenchiv"
        )));
        pd.add(new periodData(R.drawable.course_highschoolenrichment, R.color.highschoolenrichment, Arrays.asList(
                "highschoolenrichment"
        )));
        pd.add(new periodData(R.drawable.course_linguistics, R.color.linguistics, Arrays.asList(
                "linguistics"
        )));
        pd.add(new periodData(R.drawable.course_math, R.color.math, Arrays.asList(
                "advalgebraii", "advgeometry", "algebrai", "algebraii", "algebraiii", "algebraii/trignometry",
                "apcalculusab", "apcalculusbc","apstatistics", "appliedmath","calculus","geometry", "introtolinearalgebra",
                "math6", "math7", "precalculus"
        )));
        pd.add(new periodData(R.drawable.course_music, R.color.music, Arrays.asList(
                "advband8", "band", "beginninginstrumentalmusic", "instrumentalmusicband6",
                "instrumentalmusicband7", "vocalmusic6", "vocalmusic7"
        )));
        pd.add(new periodData(R.drawable.course_spanish, R.color.spanish, Arrays.asList(
                "spanishi", "spanishii", "spanishiii", "spanishiv", "bilingualtranslation"
        )));
        pd.add(new periodData(R.drawable.course_steam, R.color.steam, Arrays.asList(
                "steam", "steami", "steamii", "steamiii", "steamday"
        )));
        pd.add(new periodData(R.drawable.course_chemistry, R.color.chemistry, Arrays.asList(
                "apchemistry", "chemistry"
        )));
        pd.add(new periodData(R.drawable.course_economics, R.color.economics, Arrays.asList(
                "apmacroeconomics", "economics"
        )));
        pd.add(new periodData(R.drawable.course_english, R.color.english, Arrays.asList("english",
                "langarts6", "langarts7", "langarts8", "english9", "english10", "englih11", "englih12",
                "apenglishlanguage&composition", "aplang", "apenglishlanguage"
        )));
        pd.add(new periodData(R.drawable.course_history, R.color.history, Arrays.asList(
                "ancientworldhistory7", "apushistory", "apworldhistory", "arthistorymethods",
                "chinesehistory6", "chinesehistory7", "chinesehistory8", "chinesehistoryi",
                "chinesehistoryii", "Geography 6","medievalworldhistory8", "modernworldhistory",
                "ushistory"
        )));
        pd.add(new periodData(R.drawable.course_socialstudy, R.color.socialstudy, Arrays.asList(
                "currentaffairs", "digitalethnography", "foundationsofmodernchina", "humanities", "philosophy"
        )));
        pd.add(new periodData(R.drawable.course_physics, R.color.physics, Arrays.asList(
                "apphysicsi", "apphysicsii", "earthandspacescience", "earthscience6", "lifescience7",
                "physicalscience8"
        )));
        pd.add(new periodData(R.drawable.course_piano, R.color.piano, Arrays.asList(
                "pianoi", "pianoii"
        )));
        pd.add(new periodData(R.drawable.course_studyhall, R.color.studyhall, Arrays.asList(
                "studyhall"
        )));
        pd.add(new periodData(R.drawable.course_theater, R.color.theater, Arrays.asList(
                "advacting", "classicalacting", "theater6", "theater7", "theater8"
        )));
        pd.add(new periodData(R.drawable.course_genderstudies, R.color.genderstudies, Arrays.asList(
                "genderstudiesi"
        )));
        pd.add(new periodData(R.drawable.course_history, R.color.els, Arrays.asList(
                "langsupport6", "langsupport7", "langsupport8", "langsupport9", "langsupport10"
        )));
        pd.add(new periodData(R.drawable.course_health, R.color.health, Arrays.asList(
                "health7", "health8", "health9", "health10"
        )));
        pd.add(new periodData(R.drawable.course_fitness, R.color.fitness, Arrays.asList(
                "advfitness"
        )));
        pd.add(new periodData(R.drawable.course_sport, R.color.sport, Arrays.asList(
                "outdooreducation", "pe6", "pe7", "pe8", "pe9", "sportsmanagement",
                "strengthtraining", "ultimatesports"
        )));
        pd.add(new periodData(R.drawable.course_none, R.color.grey, Arrays.asList(
                "--"
        )));
    }

    public ClassPeriod(Subject s, int p){
        sub = s;
        period = p;
        focus = false;
        //todo search for corresponding color and background
        colorID = R.color.purple;
        backgroundID = 0;

        String temp = s.name.toLowerCase().replace(" ","");

        boolean found = false;

        for(periodData i : pd){
            if(i.courses.contains(temp)) {
                colorID = i.colorID;
                backgroundID = i.imageID;
                found = true;
                break;
            }
        }

        if(!found){
            colorID = pd.get(0).colorID;
            backgroundID = pd.get(0).imageID;
        }
    }

}

class periodData{
    int imageID;
    int colorID;
    List<String> courses;

    public periodData(int image, int color, List<String> c){
        imageID = image;
        colorID = color;
        courses = c;
    }
}
