package swipe.data;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.time.LocalDate;
import java.util.Objects;

public class Timestamp {

    private String start;
    private String end;
    public static final DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("MM-dd-yyyy hh:mm a");
    public static final int MAX_REASONABLE_VISIT_TIME = 6; //in hours

    public Timestamp(String start, String end){
        this.start = start;
        this.end = end;
    }

    public Timestamp(String start){
        this.start = start;
        this.end = "";
    }

    public LocalDate startLocalDate(){
        return LocalDate.of(getYear(), getMonth(), getDayOfMonth());
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
        int startHour = Integer.parseInt(start.substring(11, 13));
        if (start.substring(17).toLowerCase().equals("pm")){
            startHour+=12;
        }
        int endHour;
        if(end.equals("")){
            endHour = startHour;
        } else {
            endHour = Integer.parseInt(end.substring(11, 13));
            if (end.substring(17).toLowerCase().equals("pm")){
                endHour+=12;
            }
        }

        if ((startHour<=hour && hour<=endHour)){
            return true;
        } else {
            return false;
        }
    }

    /**
     * Checks if timestamp occurred during certain day of the week
     * @param day 1-7 indicating Monday -> Sunday
     * @return
     */
    public boolean hereOnDay(int day) {
        if (dateTimeFormatter.parseDateTime(start).dayOfWeek().get()==day){
            return true;
        }
        return false;
    }

    /**
     * used to determine if a timestamp contains 'valid' data
     * examples of 'invalid' data:
     * - Got logged out after we closed - indicates that they were not logged out when they actually left,
     *   just when someone closed the software
     * - Has a duration greater than a reasonable visit time (set at top of file)
     * @return true if timestamp seems like ok data
     */
    public boolean isValid(){
        if (this.getTimeLength().getStandardHours()>=MAX_REASONABLE_VISIT_TIME){
            return false;
        }
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

    public int getDayOfMonth(){
        return dateTimeFormatter.parseDateTime(start).getDayOfMonth();
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
