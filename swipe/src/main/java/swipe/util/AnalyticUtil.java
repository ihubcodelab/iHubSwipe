package swipe.util;

import swipe.data.Person;
import swipe.data.Timestamp;
import java.util.Collection;

/**
 * Provides a set of classes for specific com.fla.data points like time spent, unique visitors, average time, etc.
 */
public class AnalyticUtil {

    public static final int DAY_IN_MILLIS = 86400000; //86 million
    public static final int HOUR_IN_MILLIS = 3600000; //3.6 million
    public static final int MINUTE_IN_MILLIS = 60000; // 60 thousand
    /**
     * Fetches the total time spent in the lab from all persons in the directory. An O(P*T) operation that should be used sparingly.
     * @param collection Collection of person objects
     * @return Returns the total time spent from all visits in the collection of persons in milliseconds.
     */
    public static long getTotalTimeSpent(Collection<Person> collection){
        long millis = 0;
        for(Person p : collection){
             millis += getTotalTimeSpent(p);
        }
        return millis;
    }

    public static long getTotalTimeSpent(Person p){
        long millis = 0;
        for( Timestamp t : p.getTimeStampHistory()){
            millis += t.getTimeLength().getMillis();
        }
        return millis;
    }

    public static long getTotalTimeSpentForMonth(int month, int year, Collection<Person> collection){
        long millis = 0;
        for (Person p : collection){
            for( Timestamp t : p.timeStampsForMonth(month, year)){
                millis += t.getTimeLength().getMillis();
            }
        }
        return millis;
    }

    /**
     * gets average visit length for a particular month
     * maybe should be collapsed into another function, lot of copied code
     * @param month
     * @param year
     * @param collection
     * @return
     */
    public static long getAvgVisitTimeForMonth(int month, int year, Collection<Person> collection){
        long millis = 0;
        int count = 0;
        for (Person p : collection){
            for( Timestamp t : p.timeStampsForMonth(month, year)){
                millis += t.getTimeLength().getMillis();
                count++;
            }
        }
        return millis/count;
    }

    public static long getTotalTimeSpentForMonth(int month, int year, Person p){
        long millis = 0;

        for( Timestamp t : p.timeStampsForMonth(month, year)){
            millis += t.getTimeLength().getMillis();
        }

        return millis;
    }

    public static int getNumVisitorsForMonth(int month, int year, Collection<Person> collection){
        int output = 0;
        for (Person p: collection){
            if (!(p.timeStampsForMonth(month, year).isEmpty())){
                output++;
            }
        }
        return output;
    }

    /**
     * Expensive operation, should only be used for collecting analytics on demand.
     * @param personCollection Collection of person objects
     * @return Returns a string formatted with the number of days, hours, and minutes.
     */
    public static String getTotalTimeSpentInText(Collection<Person> personCollection){
        int days, hours, minutes;
        long millis = getTotalTimeSpent(personCollection);
        days =(int) (millis / DAY_IN_MILLIS);
        millis = millis % DAY_IN_MILLIS;
        hours = (int)(millis / HOUR_IN_MILLIS);
        millis = millis % HOUR_IN_MILLIS;
        minutes = (int) (millis/MINUTE_IN_MILLIS);
        return String.format("Days: %1$d Hours: %2$d Minutes: %3$d", days, hours, minutes);
    }

    public static String getTotalTimeSpentInText(long millis){
        int days, hours, minutes;
        days =(int) (millis / DAY_IN_MILLIS);
        millis = millis % DAY_IN_MILLIS;
        hours = (int)(millis / HOUR_IN_MILLIS);
        millis = millis % HOUR_IN_MILLIS;
        minutes = (int) (millis/MINUTE_IN_MILLIS);
        return String.format("Days: %1$d Hours: %2$d Minutes: %3$d", days, hours, minutes);
    }

    public static String getAverageTimeSpent(Collection<Person> personCollection){
        long millis = 0;
        for(Person p : personCollection){
            millis += getTotalTimeSpent(p);
        }
        return getTotalTimeSpentInText(millis / personCollection.size());
    }

    public static int getVisitCount(Collection<Person> personCollection){
        int visits = 0;
        for(Person p : personCollection){
            visits += Integer.valueOf(p.getTimesVisited());
        }
        return visits;
    }

    public static String getAverageTimePerVisit(Collection<Person> collection){
        long visits = getVisitCount(collection);
        long time = getTotalTimeSpent(collection);
        return getTotalTimeSpentInText(time/visits);
    }

    public static void exportAnalytics(Collection<Person> people){
        int currentYear = Timestamp.Now().getYear();
        int currentMonth = Timestamp.Now().getMonth();
        LogManager.appendLog(String.format("ANALYTICS FOR %1$d", currentYear));
        LogManager.appendLog("MONTH - UNIQUE VISITORS - AVG VISIT TIME - TOTAL TIME SPENT BY VISITORS");
        //starting with January, compile information for each month
        int days, hours, minutes, numVisitors, avgMinutes;
        long millis;
        for (int month=1; month<=currentMonth; month++){
            numVisitors = getNumVisitorsForMonth(month,currentYear,people);
            millis = getTotalTimeSpentForMonth(month,currentYear,people);
            days =(int) (millis / DAY_IN_MILLIS);
            millis = millis % DAY_IN_MILLIS;
            hours = (int)(millis / HOUR_IN_MILLIS);
            millis = millis % HOUR_IN_MILLIS;
            minutes = (int) (millis/MINUTE_IN_MILLIS);
            avgMinutes =  (int) getAvgVisitTimeForMonth(month, currentYear, people)/MINUTE_IN_MILLIS;
            String output = String.format("%4$d/%5$d  -   %6$d Visitors    -    %7$d Minutes   -    %1$d Days, %2$d Hours and %3$d Minutes"
                    , days, hours, minutes, month, currentYear, numVisitors, avgMinutes);
            LogManager.appendLog(output);
        }
    }
}
