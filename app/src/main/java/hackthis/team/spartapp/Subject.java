package hackthis.team.spartapp;

public class Subject {
    /**
     * includes only basic components
     */

    String name, teacher, room;

    Subject(String n, String t, String r){
        name = n;
        teacher = t;
        room = r;
    }

    public String name(){ return name; }
    public String teacher() { return teacher; }
    public String room() { return room; }

    public boolean equals(Object obj){
        return ((Subject) obj).name.equals(this.name);
    }

    public String toString(){
        return name();
    }

}
