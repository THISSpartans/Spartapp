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
                "ceramics", "easternart","foundationsofart", "foundationsofdigitalart",
                "sculpture", "painting", "artportfolio", "westernart", "arthistory","arthl","artsl","artexperience"
        )));
        pd.add(new periodData(R.drawable.course_beginningguitar, R.color.music, Arrays.asList(
                "beginningguitar"
        )));
        pd.add(new periodData(R.drawable.course_biology, R.color.biology, Arrays.asList(
                "biology", "bioethics", "biotechnology"
        )));
        pd.add(new periodData(R.drawable.course_chinese, R.color.chinese, Arrays.asList(
                "chinese","mandarin"
        )));
        pd.add(new periodData(R.drawable.course_choir, R.color.choir, Arrays.asList(
                "choir"
        )));
        pd.add(new periodData(R.drawable.course_computer, R.color.computer, Arrays.asList(
                "computer", "desktoppublishing","photoshop",
                "appdevelopment", "robotics", "webdesign",
                "programming","appdesign","computational","compprogram"
        )));
        pd.add(new periodData(R.drawable.course_filmstudies, R.color.filmstudies, Arrays.asList(
                "filmstudy","digital"
        )));
        pd.add(new periodData(R.drawable.course_french, R.color.french, Arrays.asList(
                "french"
        )));
        pd.add(new periodData(R.drawable.course_highschoolenrichment, R.color.highschoolenrichment, Arrays.asList(
                "highschoolenrichment","mentoring","tutortraining","effectivestudyskills","aide",
                "hsmscrossover","seniorprivileges","apprenticeship","creativity,activity"
        )));
        pd.add(new periodData(R.drawable.course_linguistics, R.color.linguistics, Arrays.asList(
                "linguistics"
        )));
        pd.add(new periodData(R.drawable.course_math, R.color.math, Arrays.asList("algebra","math"
        )));
        pd.add(new periodData(R.drawable.course_calculus, R.color.math, Arrays.asList("calculus")));
        pd.add(new periodData(R.drawable.course_geometry,R.color.math,Arrays.asList("geometry")));
        pd.add(new periodData(R.drawable.course_music, R.color.music, Arrays.asList(
                "band","music","concert"
        )));
        pd.add(new periodData(R.drawable.course_spanish, R.color.spanish, Arrays.asList(
                "spanish", "bilingualtranslation"
        )));
        pd.add(new periodData(R.drawable.course_steam, R.color.steam, Arrays.asList(
                "steam","robotics","engineeringdesign","andengineering","engineeringforsust","b.e.a.d."
        )));
        pd.add(new periodData(R.drawable.course_chemistry, R.color.chemistry, Arrays.asList(
                "chemistry"
        )));
        pd.add(new periodData(R.drawable.course_economics, R.color.economics, Arrays.asList(
                "economics"
        )));
        pd.add(new periodData(R.drawable.course_english, R.color.english, Arrays.asList("english","langarts","englishlanguage",
                "langsupport", "englishliterature")));
        pd.add(new periodData(R.drawable.course_history, R.color.history, Arrays.asList(
                "history","pearlharbor"
        )));
        pd.add(new periodData(R.drawable.course_socialstudy, R.color.socialstudy, Arrays.asList(
                "crimesagainsthumanity","criminology"
        )));
        pd.add(new periodData(R.drawable.course_physics, R.color.physics, Arrays.asList("physics",
                "earthandspacescience", "earthscience", "lifescience",
                "physicalscience"
        )));
        pd.add(new periodData(R.drawable.course_piano, R.color.piano, Arrays.asList(
                "piano"
        )));
        pd.add(new periodData(R.drawable.course_studyhall, R.color.studyhall, Arrays.asList(
                "studyhall","studyperiod","studysem","text","guidedstudy","tlcstudy"
        )));
        pd.add(new periodData(R.drawable.course_theater, R.color.theater, Arrays.asList(
                "advacting", "classicalacting", "theater6","cinema,","acting"
        )));
        pd.add(new periodData(R.drawable.course_genderstudies, R.color.genderstudies, Arrays.asList(
                "genderstudiesi"
        )));
        pd.add(new periodData(R.drawable.course_esl, R.color.els, Arrays.asList(
                "langsupport","esol","eal10","eal11","eal9","ealiii"
        )));
        pd.add(new periodData(R.drawable.course_health, R.color.health, Arrays.asList(
                "health7", "health8", "health9", "health10"
        )));
        pd.add(new periodData(R.drawable.course_fitness, R.color.fitness, Arrays.asList(
                "fitness"
        )));
        pd.add(new periodData(R.drawable.course_sport, R.color.sport, Arrays.asList(
                "outdooreducation", "pe6", "pe7", "pe8", "pe9", "sportsmanagement","p.e.","sportsexer",
                "strengthtraining", "ultimatesports"
        )));
        pd.add(new periodData(R.drawable.course_business,R.color.chemistry,Arrays.asList(
                "business","entrepreneurship","investment","stockmarket","finance","enterprise"
        )));
        pd.add(new periodData(R.drawable.course_korean,R.color.sport,Arrays.asList("korean")));
        pd.add(new periodData(R.drawable.course_japanese,R.color.highschoolenrichment,Arrays.asList("japanese")));
        pd.add(new periodData(R.drawable.course_latin,R.color.grey,Arrays.asList("latin")));
        pd.add(new periodData(R.drawable.course_arabic,R.color.fitness,Arrays.asList("arabic")));
        pd.add(new periodData(R.drawable.course_psychology,R.color.biology,Arrays.asList(
                "psychology","sociology"
        )));
        pd.add(new periodData(R.drawable.course_design,R.color.highschoolenrichment,Arrays.asList(
                "publicationdesign","graphicdesign","designer","c.a.d.","three-dimensional"
        )));
        pd.add(new periodData(R.drawable.course_law,R.color.steam, Arrays.asList(
                "globalpolitics","comparativegovernments","comparativepolitics","americanlaw","9/11","prisons","forensic"
        )));
        pd.add(new periodData(R.drawable.course_asia,R.color.chinese, Arrays.asList(
                "asiaandtheworld","asianstudies","asia&theworld"
        )));
        pd.add(new periodData(R.drawable.course_anatomy,R.color.sport, Arrays.asList(
                "anatomy","zoology"
        )));
        pd.add(new periodData(R.drawable.course_geography,R.color.chemistry,Arrays.asList(
                "geography", "climatechange","peopleplacesandpublication"
        )));
        pd.add(new periodData(R.drawable.course_literature, R.color.steam, Arrays.asList(
                "languagealiterature","literaryanalysis","apliterature"
        )));
        pd.add(new periodData(R.drawable.course_medicine, R.color.piano, Arrays.asList(
                "globalhealth","medicine","medicalproblem"
        )));
        pd.add(new periodData(R.drawable.course_finnish,R.color.piano, Arrays.asList("finnish")));
        pd.add(new periodData(R.drawable.course_writing,R.color.theater, Arrays.asList(
            "extendedessay","creativewriting","onlinejournalism","globalvoice","fieldwork","fieldstudies"
        )));
        pd.add(new periodData(R.drawable.course_religion,R.color.economics,Arrays.asList("religions")));
        pd.add(new periodData(R.drawable.course_science, R.color.chemistry, Arrays.asList("science")));
        pd.add(new periodData(R.drawable.course_game, R.color.computer, Arrays.asList("gametheory")));
        pd.add(new periodData(R.drawable.course_ib, R.color.chemistry, Arrays.asList("coreclass")));
        pd.add(new periodData(R.drawable.course_online, R.color.none, Arrays.asList(
                "vhssem","goasem","goayearlong"
        )));
        pd.add(new periodData(R.drawable.course_passionproject, R.color.chemistry, Arrays.asList("passionproject")));
        pd.add(new periodData(R.drawable.course_theoryofknowledge, R.color.computer,Arrays.asList("theoryofknowledge")));
        pd.add(new periodData(R.drawable.course_astronomy, R.color.english, Arrays.asList("astronomy")));
        pd.add(new periodData(R.drawable.course_middleeast, R.color.socialstudy, Arrays.asList("middleeast")));
        pd.add(new periodData(R.drawable.course_easternandwesternthought, R.color.chemistry, Arrays.asList("easternandwestern")));
        pd.add(new periodData(R.drawable.course_seminar, R.color.english, Arrays.asList("seminarap")));
        pd.add(new periodData(R.drawable.course_environmental, R.color.choir, Arrays.asList("environmental")));
        pd.add(new periodData(R.drawable.course_empty, R.color.grey, Arrays.asList(
                "--"
        )));
        pd.add(new periodData(R.drawable.course_researchskills, R.color.red, Arrays.asList("research")));
        pd.add(new periodData(R.drawable.course_humanities, R.color.blue, Arrays.asList("currentaffairs", "digitalethnography", "foundationsofmodernchina", "humanities", "philosophy")));
    }

    public ClassPeriod(Subject s, int p){
        sub = s;
        period = p;
        focus = false;

        String root = sub.name.toLowerCase().replace(" ","");

        boolean found = false;
        //search here
        for(periodData i : pd){
            if(i.search(root)){
                colorID = i.colorID;
                backgroundID = i.imageID;
                found = true;
                break;
            }
        }
        if(!found){
            colorID = pd.get(pd.size()-1).colorID;
            backgroundID = pd.get(pd.size()-1).imageID;
        }
    }

    static class periodData{
        int imageID;
        int colorID;
        List<String> tags;

        public periodData(int image, int color, List<String> t){
            imageID = image;
            colorID = color;
            tags = t;
        }
        public boolean search(String source){
                for(String t : tags){
                    if(source.contains(t))
                        return true;
                }
            return false;
        }
    }

}


