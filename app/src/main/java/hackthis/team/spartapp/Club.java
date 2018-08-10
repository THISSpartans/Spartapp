package hackthis.team.spartapp;

public class Club {
    String name;
    boolean checked;

    Club(String n){name = n; checked = false;}

    public String getCorrespondingChannel(){
        return name;
    }

    @Override
    public String toString() {
        return name+" "+checked;
    }
}
