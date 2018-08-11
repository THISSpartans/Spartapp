package hackthis.team.spartapp;

public class Club {
    String name;
    boolean checked;
    private String channel = null;

    Club(String n){name = n; checked = false;}

    public String getCorrespondingChannel(){
        char[] str = name.toCharArray();
        if(channel == null) {
            StringBuilder sb = new StringBuilder();
            for (char i : str) {
                if (((int) i) <= ((int) 'z') && ((int) i) >= ((int) 'a')) {
                    sb.append(i);
                } else if (((int) i) <= ((int) 'Z') && ((int) i) >= ((int) 'A')) {
                    sb.append(i);
                }
            }
            channel = sb.toString();
        }
        return channel;
    }

    @Override
    public String toString() {
        return name+" "+checked;
    }
}
