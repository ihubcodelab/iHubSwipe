package swipe.util;

import javafx.scene.control.Alert;
import org.apache.commons.io.FileUtils;
import swipe.awsapi.AWSCRUD;
import swipe.data.Constants;
import swipe.data.Person;
import swipe.data.Timestamp;

import java.awt.*;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.*;
import java.util.List;

/**
 * Provides a set of classes for specific points like time spent, unique visitors, average time, etc.
 */
public class AnalyticUtil {

    public static final int DAY_IN_MILLIS = 86400000; //86 million
    public static final int HOUR_IN_MILLIS = 3600000; //3.6 million
    public static final int MINUTE_IN_MILLIS = 60000; // 60 thousand
    private static final int[] DAYS = {1,2,3,4,5,6,7};
    private static final int[] HOURS = {0,1,2,3,4,5,6,7,8,9,10,11};
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
     * gets input from analytics window, filters out timestamps that aren't in the range
     * @param directory collection of all people in directory
     * @param dateStart start date from analytics window
     * @param dateEnd end date from analytics window
     * @return HashMap of filtered timestamps lists for individuals, with the key being the associated person's ID
     */
    public static HashMap<String, List<Timestamp>> filterForDateRange(Collection<Person> directory, LocalDate dateStart, LocalDate dateEnd){
        HashMap<String, List<Timestamp>> output = new HashMap<>();
        for (Person person: directory){
            ArrayList<Timestamp> tsHistory = person.getTimeStampHistory();
            tsHistory.removeIf(ts ->!(ts.startLocalDate().isAfter(dateStart) && ts.startLocalDate().isBefore(dateEnd)));
            output.put(person.getId(),tsHistory);
        }
        return output;
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
        if (count==0){
            return 0;
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
            //visits += Integer.valueOf(p.getTimesVisited()); not using because past records for Times Visited are incorrect
            visits += p.getTimeStampHistory().size();
        }
        return visits;
    }

    public static String getAverageTimePerVisit(Collection<Person> collection){
        long visits = getVisitCount(collection);
        long time = getTotalTimeSpent(collection);
        return getTotalTimeSpentInText(time/visits);
    }

    /**
     * Will get some basic stats from directory data by month for a given year
     * and will save the data to a CSV file
     * ---not currently implemented----
     * @param people
     * @param currentYear set to 0 if you just want to grab the current year
     */
    public static void statsForYearByMonth(Collection<Person> people, int currentYear){
        String CSVContents = "Month, Number of Unique Visitors, Average Visit Time, Total Time Spent By Visitors\n";
        StringBuilder CSVCOntentsBuilder = new StringBuilder(CSVContents);
        int currentMonth;
        if (currentYear==0){
            currentYear = Timestamp.Now().getYear();
            currentMonth = Timestamp.Now().getMonth();
        } else {
            currentMonth = 12; //if we aren't looking at the current year, then we should analyze the whole year
        }

        LogManager.appendLog(String.format("ANALYTICS FOR %1$d", currentYear));
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
            String output = String.format("%4$d/%5$d, %6$d, %7$d Minutes, %1$d Days %2$d Hours and %3$d Minutes\n"
                    , days, hours, minutes, month, currentYear, numVisitors, avgMinutes);
            CSVCOntentsBuilder.append(output);
        }
        CSVContents = CSVCOntentsBuilder.toString();
        String filename = currentYear+"overallanalytics";
        Path path = Paths.get(Constants.analyticsFolder.toString(), filename + ".csv");
        try {
            FileUtils.writeStringToFile(path.toFile(), CSVContents, Charset.defaultCharset());
            Desktop.getDesktop().open(Constants.analyticsFolder);
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "First set done! This next one will take a while!");
            alert.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Failed creating file." + e.getMessage());
            alert.showAndWait();
        }
    }

    public static int[] avgVisitTime(Collection<List<Timestamp>> timestamps, boolean sortByDay, boolean sortByHour){
        int[] output;
        if(sortByDay && sortByHour){
            return dailyHourlyAvgVisitTime(timestamps);
        } else if (sortByDay) {
            return dailyAvgVisitTime(timestamps);
        } else if (sortByHour) {
            return hourlyAvgVisitTime(timestamps);
        } else {
            output = new int[1];
            output[0] = overallAvgVisitTime(timestamps);
            return output;
        }
    }

    public static int overallAvgVisitTime(Collection<List<Timestamp>> timestamps){
        int totalTimestampCount = 0;
        long totalTimeInMillis = 0;
        for (List<Timestamp> tsHistory :timestamps){
            for(Timestamp ts: tsHistory){
                if(ts.isValid()){
                    totalTimeInMillis += ts.getTimeLength().getMillis();
                    totalTimestampCount++;
                }
            }
        }
        if (totalTimestampCount>0){
            return  (int) ((totalTimeInMillis/totalTimestampCount)/MINUTE_IN_MILLIS);
        } else {
            return 0;
        }
    }

    public static int[] dailyAvgVisitTime(Collection<List<Timestamp>> timestamps){
        int[] output = new int[7];
        for (int day: DAYS){
            int totalTimestampCount = 0;
            long totalTimeInMillis = 0;
            for (List<Timestamp> tsHistory :timestamps){
                for(Timestamp ts: tsHistory){
                    if(ts.isValid() && ts.hereOnDay(day)){
                        totalTimeInMillis += ts.getTimeLength().getMillis();
                        totalTimestampCount++;
                    }
                }
            }
            if(totalTimestampCount>0){
                output[day-1] = (int) ((totalTimeInMillis/totalTimestampCount)/MINUTE_IN_MILLIS);
            } else {
                output[day-1] = 0;
            }
        }

        return output;
    }

    public static int[] hourlyAvgVisitTime(Collection<List<Timestamp>> timestamps){
        int[] output = new int[12];
        for (int hour: HOURS){
            int totalTimestampCount = 0;
            long totalTimeInMillis = 0;
            for (List<Timestamp> tsHistory :timestamps){
                for(Timestamp ts: tsHistory){
                    if(ts.isValid() && ts.hereAtHour(hour+9)){
                        totalTimeInMillis += ts.getTimeLength().getMillis();
                        totalTimestampCount++;
                    }
                }
            }
            if(totalTimestampCount>0){
                output[hour] = (int) ((totalTimeInMillis/totalTimestampCount)/MINUTE_IN_MILLIS);
            } else {
                output[hour] = 0;
            }
        }

        return output;
    }

    public static int[] dailyHourlyAvgVisitTime(Collection<List<Timestamp>> timestamps){
        int[] output = new int[84];
        for (int day: DAYS){
            for (int hour: HOURS){
                int totalTimestampCount = 0;
                long totalTimeInMillis = 0;
                for (List<Timestamp> tsHistory :timestamps){
                    for(Timestamp ts: tsHistory){
                        if(ts.isValid() && ts.hereAtHour(hour+9) && ts.hereOnDay(day)){
                            totalTimeInMillis += ts.getTimeLength().getMillis();
                            totalTimestampCount++;
                        }
                    }
                }
                if(totalTimestampCount>0){
                    output[hour + ((day-1) * 12)] = (int) ((totalTimeInMillis/totalTimestampCount)/MINUTE_IN_MILLIS);
                } else {
                    output[hour + ((day-1) * 12)] = 0;
                }

            }
        }


        return output;
    }

    /**
     * gets the info for student year break down, using custom StudentInfo class defined at the bottom of this file
     * AnalyticsController does not currently allow for doing anything but overall analytics for this,
     * could be implemented if needed in the future
     * @param directory
     * @param sortByDay
     * @param sortByHour
     * @return
     */
    public static StudentInfo[] getStudentStandingInfo(HashMap<String, List<Timestamp>> directory, boolean sortByDay, boolean sortByHour){
        StudentInfo[] output;
        if(sortByDay && sortByHour){
            return null;
        } else if (sortByDay) {
            return null;
        } else if (sortByHour) {
            return null;
        } else {
            output = new StudentInfo[1];
            output[0] = getTotalStudentStandingInfo(directory);
            return output;
        }
    }

    public static StudentInfo getTotalStudentStandingInfo(HashMap<String, List<Timestamp>> directory){
        int freshmen = 0;
        int sophomores = 0;
        int juniors = 0;
        int seniors = 0;
        int gradStudents = 0;
        int other = 0;
        for(String id : directory.keySet()){
            Map<String, Object> info = AWSCRUD.retrieveStudentInfo(id);
            if (info!=null){
                String year = (String) info.get("year");
                switch (year){
                    case "Freshman": freshmen++;
                        break;
                    case "Sophomore": sophomores++;
                        break;
                    case "Junior": juniors++;
                        break;
                    case "Senior": seniors++;
                        break;
                    case "Graduate": gradStudents++;
                        break;
                    default: other++;
                        break;

                }
            }
        }
        StudentInfo output = new StudentInfo(freshmen,sophomores,juniors,seniors,gradStudents,other);
        return output;
    }

    /**
     * counts visitor numbers for each hour tha that the Hub is open for
     * i.e. 9am to 9pm
     * @param timestamps collection of people's timestamps histories to use in analysis
     * @return array of 12 ints with each representing and hour long interval
     */
    public static int[] hourlyVisitors(Collection<List<Timestamp>> timestamps){
        //make an array to hold counts for hourly stats
        int[] hourlyCounts = new int[12]; //representing 9am to 9pm
        //loop through collection
        //loop through timestamps of user, add to hourly counts
        for (List<Timestamp> tsHistory : timestamps) {
            for (Timestamp ts : tsHistory)
                if (ts.isValid()) {
                    for(int hour = 0; hour<12; hour++){
                        if(ts.hereAtHour(hour+9)){
                            hourlyCounts[hour] += 1;
                        }
                    }
                }
        }
        return hourlyCounts;

    }

    /**
     * similar to hourlyStats method, sums up counts by day for visitors
     * NOTE: Don't think it is getting unique visitors, might be padding the stats
     * @param timestamps
     * @return int array with each number indicating count for that day of the week (order is M T W T F S S)
     */
    public static int[] dailyVisitors(Collection<List<Timestamp>> timestamps){
        //array for storing day info
        int[] dailyCounts = new int[7];
        for (List<Timestamp> tsHistory : timestamps) {
            for (Timestamp ts : tsHistory)
                if (ts.isValid()) {
                    for(int day = 1; day<8; day++){
                        if(ts.hereOnDay(day)){
                            dailyCounts[day-1] += 1;
                        }
                    }
                }
        }
        return dailyCounts;
    }

    public static int[] dailyAvgTime(Collection<List<Timestamp>> timestamps){
        //array for storing day info
        int[] dailyCounts = new int[7];
        int personCount = 0;
        for (List<Timestamp> tsHistory : timestamps) {
            for (Timestamp ts : tsHistory) {
                if (ts.isValid()) {
                    for (int day = 1; day < 8; day++) {
                        if (ts.hereOnDay(day)) {
                            dailyCounts[day - 1] += ts.getTimeLength().getStandardMinutes();
                        }
                    }
                }
            }
            personCount++;
        }

        return dailyCounts;
    }

    /**
     * This is called by the Analytics Controller to get stats for unique visitors based on the passed parameters from the controller's checkboxes
      * @param timestamps
     * @param sortByDay
     * @param sortByHour
     * @return an int array of an appropriate size for the given parameters
     */
    public static int[] numUniqueVisitors(Collection<List<Timestamp>> timestamps, boolean sortByDay, boolean sortByHour){
        if(sortByDay && sortByHour) {
            return dailyHourlyUniqueVisitors(timestamps);
        } else if (sortByDay) {
            return dailyUniqueVisitors(timestamps);
        } else if (sortByHour) {
            return hourlyUniqueVisitors(timestamps);
        } else {
            int[] output = {timestamps.size()};
            return output;
        }
    }

    /**
     * calculates unique visitors sorted by hour block
     * if a record stretches over multiple blocks, it will defer to the earlier block
     * @param timestamps
     * @return
     */
    private static int[] hourlyUniqueVisitors(Collection<List<Timestamp>> timestamps){
        int[] output = new int[12];
        for (int hour : HOURS){
            for (List<Timestamp> tsHistory : timestamps){
                if(wasHereAtHour(tsHistory,hour+9)){
                    output[hour] += 1;
                    continue;
                }
            }
        }
        return output;
    }

    /**
     * calculates unique visitors sorted by day of the week
     * @param timestamps
     * @return
     */
    private static int[] dailyUniqueVisitors(Collection<List<Timestamp>> timestamps){
        int[] output = new int[7];
        for(int day: DAYS){
            for (List<Timestamp> tsHistory : timestamps){
                if(wasHereAtDay(tsHistory,day)){
                    output[day-1] += 1;
                    continue;
                }
            }
        }
        return output;
    }

    /**
     * calculates unique visitors sorting by both day of week and hour block
     * @param timestamps
     * @return
     */
    private static int[] dailyHourlyUniqueVisitors(Collection<List<Timestamp>> timestamps){
        int[] output = new int[84];
        for (int day: DAYS){
            for (int hour: HOURS){
                for (List<Timestamp> tsHistory: timestamps){
                    if(wasHereAtHourDay(tsHistory, day, hour+9)){
                        output[hour + ((day-1) * 12)] += 1;
                        continue;
                    }
                }
            }
        }
        return output;
    }

    /**
     * checks to see if any record in the provided timestamp history meets the given day/hour time requirement
     * @param tsHistory
     * @param day
     * @param hour
     * @return true if one of the entries in the list is valid
     */
    private static boolean wasHereAtHourDay(List<Timestamp> tsHistory, int day, int hour){
        for(Timestamp ts : tsHistory){
           if(ts.isValid() && ts.hereAtHour(hour) && ts.hereOnDay(day)) {
                return true;
           }
        }
        return false;
    }

    private static boolean wasHereAtHour(List<Timestamp> tsHistory, int hour){
        for(Timestamp ts : tsHistory){
            if(ts.isValid() && ts.hereAtHour(hour)) {
                return true;
            }
        }
        return false;
    }

    private static boolean wasHereAtDay(List<Timestamp> tsHistory, int day){
        for(Timestamp ts : tsHistory){
            if(ts.isValid() && ts.hereOnDay(day)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Will get number of visitors for the given sort parameters (daily and hourly)
     * @param timestamps
     * @param sortByDay
     * @param sortByHour
     * @return an appropriately sized int array with visitor numbers
     */
    public static int[] numVisitors(Collection<List<Timestamp>> timestamps, boolean sortByDay, boolean sortByHour) {
        if (sortByDay && sortByHour){
            int[] dailyHourlyCounts = new int[84];
            for (List<Timestamp> tsHistory : timestamps) {
                for (Timestamp ts : tsHistory)
                    if (ts.isValid()) {
                        for(int day = 1; day<8; day++){
                            if(ts.hereOnDay(day)){
                                for(int hour = 0; hour<12; hour++){
                                    if(ts.hereAtHour(hour+9)){
                                        dailyHourlyCounts[hour+((day-1)*12)] += 1;
                                    }
                                }
                            }
                        }
                    }
            }
            return dailyHourlyCounts;
        } else if (sortByDay) {
            return dailyVisitors(timestamps);
        } else if (sortByHour) {
            return hourlyVisitors(timestamps);
        } else {
            //just get data unsorted for all peoples
            int[] visitors = {0};
            for (List<Timestamp> tsHistory: timestamps){
                for(Timestamp ts: tsHistory){
                    if (ts.isValid()) visitors[0]++;
                }
            }
            return visitors;
        }

    }

    /**
     * Custom class used in returning year information for student records
     */
    public static class StudentInfo{
        int freshmenCount;
        int sophomoreCount;
        int juniorCount;
        int seniorCount;
        int gradCount;
        int otherCount;
        public StudentInfo(int freshmenCount, int sophomoreCount, int juniorCount, int seniorCount, int gradCount, int otherCount){
            this.freshmenCount = freshmenCount;
            this.sophomoreCount = sophomoreCount;
            this.juniorCount = juniorCount;
            this.seniorCount = seniorCount;
            this.gradCount = gradCount;
            this.otherCount = otherCount;
        }

        @Override
        public String toString() {
            return freshmenCount + ", " + sophomoreCount + ", " + juniorCount + ", " + seniorCount + ", " + gradCount + ", " + otherCount;
        }

    }

}


