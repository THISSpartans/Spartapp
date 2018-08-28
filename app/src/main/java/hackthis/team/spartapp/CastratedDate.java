package hackthis.team.spartapp;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class CastratedDate {
    int year, month, date;

    final static int[] max_date = {31,28,31,30,31,30,31,31,30,31,30,31};
    final static String[] monthName = {
            "January",
            "February",
            "March",
            "April",
            "May",
            "June",
            "July",
            "August",
            "September",
            "October",
            "November",
            "December"
    };

    private GregorianCalendar cal;

    CastratedDate () {
        cal = new GregorianCalendar();
        cal.setTime(new Date());
        //returns actual value
        year = cal.get(Calendar.YEAR);
        //first month is 0
        month = cal.get(Calendar.MONTH);
        //first day is 1
        date = cal.get(Calendar.DATE);
    }

    CastratedDate (int year, int month, int date) {
        cal = new GregorianCalendar(year, month, date);
        //returns actual value
        year = cal.get(Calendar.YEAR);
        //first month is 0
        month = cal.get(Calendar.MONTH);
        //first day is 1
        date = cal.get(Calendar.DATE);
    }

    CastratedDate (Date date_source){
        cal = new GregorianCalendar();
        cal.setTime(date_source);
        year = cal.get(Calendar.YEAR);
        //first month is 0
        month = cal.get(Calendar.MONTH);
        //first day is 1
        date = cal.get(Calendar.DATE);
    }

    /**
     * @param field Calendar constant used to get GregorianCalendar values
     */
    public int get(int field){
        return cal.get(field);
    }

    /**
     * @param field Calendar constant used to set GregorianCalendar values
     */
    public void change(int field, int amount){
        cal.add(field, amount);
        //returns actual value
        year = cal.get(Calendar.YEAR);
        //first month is 0
        month = cal.get(Calendar.MONTH);
        //first day is 1
        date = cal.get(Calendar.DATE);
    }

    /**
     * @param field Calendar constant used to set GregorianCalendar values
     */
    public void set(int field, int amount){
        cal.set(field, amount);
        //returns actual value
        year = cal.get(Calendar.YEAR);
        //first month is 0
        month = cal.get(Calendar.MONTH);
        //first day is 1
        date = cal.get(Calendar.DATE);
    }

    public int month_length(){
        return max_date[month + (month == 1 && !is365() ? 1 : 0)];
    }

    public String month_name(){
        return monthName[month];
    }

    public boolean is365(){
        if(month % 4 == 0){
            if(month % 100 == 0){
                if(month % 400 == 0){
                    return true;
                }
                return false;
            }
            return true;
        }
        return false;
    }

    public static int getHourMinute(){
        Calendar c = Calendar.getInstance();
        return c.get(Calendar.HOUR_OF_DAY)*100 + c.get(Calendar.MINUTE);
    }

    public boolean equals(CastratedDate obj){
        return (obj.year == year) && (obj.month == month) && (obj.date == date);
    }

    public CastratedDate clone (){
        return new CastratedDate(this.year, this.month, this.date);
    }

    public String toString(){
        return year+"-"+(month<10?"0"+Integer.toString(month):Integer.toString(month))+"-"+(date<10?"0"+Integer.toString(date):Integer.toString(date));
    }
}
