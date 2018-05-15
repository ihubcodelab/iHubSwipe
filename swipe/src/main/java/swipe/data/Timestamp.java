package swipe.data;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.Objects;

public class Timestamp {

    private String start;
    private String end;
    public static final DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("MM-dd-yyyy hh:mm a");

    public Timestamp(String start, String end){
        this.start = start;
        this.end = end;
    }

    public Timestamp(String start){
        this.start = start;
        this.end = "";
    }

    public Timestamp(){
        start = "";
        end = "";
    }

    /**
     * returns true if this timestamp indicates that the user was here during the given hour
     * @param hour hour in 24hr format (start at zero)
     * @return true if the hour falls in the range
     */
    public boolean hereAtHour(int hour){
        int startHour = Integer.parseInt(start.substring(11,13));
        if (start.substring(17).toLowerCase().equals("pm")){
            startHour+=12;
        }
        int endHour = Integer.parseInt(end.substring(11,13));
        if (end.substring(17).toLowerCase().equals("pm")){
            startHour+=12;
        }
        if ((startHour<=hour && hour<=endHour)){
            return true;
        } else {
            return false;
        }
    }

    /**
     * Check for unreasonable entries (like supre late sign out)
     * @return
     */
    public boolean isValid(){
        //TODO: Finish Stuff
        return true;
    }

    public static Timestamp Now(){
        return new Timestamp(getCurrentTime());
    }

    public static String getCurrentTime(){
        return dateTimeFormatter.print(DateTime.now());
    }

    public String getStart() {
        return start;
    }

    public String getEnd() {
        return end;
    }

    public void setEnd(String end) {
        this.end = end;
    }


    /**
     * used in analytics functions, uses start time as basis for finding month
     * @return month of timestamp initialization
     */
    public int getMonth(){
        return Integer.parseInt(start.substring(0,2));
    }

    /**
     * see getMonth description
     * @return year as int
     */
    public int getYear(){
        return Integer.parseInt(start.substring(6,10));
    }


    @Override
    public String toString() {
        return "Timestamp{" +
                "start='" + start +
                ", end='" + end  +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Timestamp timestamp = (Timestamp) o;
        return Objects.equals(start, timestamp.start) &&
                Objects.equals(end, timestamp.end);
    }

    @Override
    public int hashCode() {

        return Objects.hash(start, end);
    }

    /**
     * @return Returns the duration between the start and end times.
     */
    public Duration getTimeLength(){
        if(end == null || start == null || start.isEmpty() || end.isEmpty()){
            return new Duration(0,0);
        }
        return new Duration(dateTimeFormatter.parseDateTime(start), dateTimeFormatter.parseDateTime(end));
    }

}
