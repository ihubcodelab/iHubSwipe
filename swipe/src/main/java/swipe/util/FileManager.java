package swipe.util;

import com.google.gson.Gson;
import swipe.data.Constants;
import swipe.data.Person;
import swipe.data.Timestamp;
import javafx.scene.control.Alert;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hildan.fxgson.FxGson;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import java.awt.*;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;

public final class FileManager {

    private static String OSDirectoryPath;
    private static String OSLogPath;
    private static String OSAnalyticsPath;
    private static Log log = LogFactory.getLog(FileManager.class);

    public static void setupFolders() {
        System.out.println("Searching for resource folder");
        Constants.mainFolder = new File(getFilePath());
        Constants.directoryFolder = new File(Constants.mainFolder.toString() + OSDirectoryPath);
        Constants.logFolder = new File(Constants.mainFolder.toString() + OSLogPath);
        Constants.analyticsFolder = new File(Constants.mainFolder.toString() + OSAnalyticsPath);
        if(!Constants.mainFolder.exists()) {
            System.out.println("Making main folder");
            if(!Constants.mainFolder.mkdir()){
                System.out.println("Failed creating main folder");
                System.exit(1);
            }
        }
        if(!Constants.directoryFolder.exists()) {
            System.out.println("Making directory folder");
            if(!Constants.directoryFolder.mkdir()){
                System.out.println("Failed making directory folder");
                System.exit(1);
            }
        }
        if(!Constants.logFolder.exists()) {
            System.out.println("Making log folder");
            if(!Constants.logFolder.mkdir()){
                System.out.println("Failed making log folder");
                System.exit(1);
            }
        }
        if(!Constants.analyticsFolder.exists()) {
            System.out.println("Making analytics folder");
            if(!Constants.analyticsFolder.mkdir()){
                System.out.println("Failed making analytics folder");
                System.exit(1);
            }
        }

        List<File> list;
        if(Constants.directoryFolder.listFiles() == null) {
            list = Arrays.asList(new File[0]);
            log.debug("Array returned null. Setting contents of zero.");
        }else{
            list = Arrays.asList((Constants.directoryFolder.listFiles()));
        }
        Constants.directoryFiles = new ArrayList<>();
        Constants.directoryFiles.addAll(list);
        setupLogger();
    }

    private static void setupLogger(){
        System.out.println("Seaching for log file");
        Path path = Paths.get(Constants.logFolder.toString(), "Log_File.txt");
        Constants.logFile = new File(path.toString());
        if(!Constants.logFile.exists()){
            try {
                if(Constants.logFile.createNewFile()) {
                    System.out.println("Created log file");
                }else{
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Log file failed creating");
                    alert.showAndWait();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else{
            System.out.println("Log file found");
        }
        LogManager.readFileIntoContents();
    }


    private static String getFilePath(){
        String FileFolder = new File(".").getAbsolutePath() + File.separator + "FabLabAnalytics";
        System.out.println(FileFolder);
        OSDirectoryPath = File.separator + "directory";
        OSLogPath = File.separator + "log";
        OSAnalyticsPath = File.separator + "analytics";
        return FileFolder;
    }

    public static void saveDirectoryJsonFile(Person person) {
        if (person.getTimestamp()==null){
            person.setTimestampProperty("");
        }
        Gson gson = FxGson.create();
        Path path = Paths.get(Constants.directoryFolder.toString(), person.getName().replace(" ", "_")+person.getId()+".json");
        deleteFile(path);
        try {
            if(person.getTimeStampHistory() == null){
                person.setTimeStampHistory(new ArrayList<>());
            }
            System.out.println(person.toString());
            FileUtils.writeStringToFile(path.toFile(), gson.toJson(person), Charset.defaultCharset());
        } catch (Exception e) {
            System.out.println(person.toString());
            e.printStackTrace();
        }
    }

    private static boolean deleteFile(Path path){
        try{
            if(path != null && path.toFile().exists()){
                Files.delete(path);
            }else{
                System.out.println("File does not exist");
                System.out.println(path);
            }
        } catch (IOException e) {
            System.out.println(e.toString());
            return false;
        }
        return true;
    }

    public static boolean deleteDirectoryFile(Person selectedPerson) {
        Path path = Paths.get(Constants.directoryFolder.toString(), selectedPerson.getName().replace(" ", "_")+selectedPerson.getId()+ ".json");
        return deleteFile(path);
    }

    public static void openFolderExplorer() {
        try {
            Desktop.getDesktop().open(Constants.mainFolder);
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    public static void getDirectoryAsCSV(Collection<Person> collection, Collection<Timestamp> dataQueries){
        String CSVContents = "Aggregate com.fla.data gathered from 9-17-17 to " + Timestamp.getCurrentTime()+ "\n";
        CSVContents += "Total Time Collected:" + AnalyticUtil.getTotalTimeSpentInText(collection) + "," +
                "Total Number of People: " +  collection.size() + "\n";
        CSVContents += "Total Number of Visits: " + AnalyticUtil.getVisitCount(collection) + "," +"Average Time Per Visit: " + AnalyticUtil.getAverageTimePerVisit(collection)+ "," +
                "Average Time Per Person: " + AnalyticUtil.getAverageTimeSpent(collection) + "\n";
        String visitorHeaders = "ID, Name, Email, Certifications, Shop Certification, Strikes, Notes, Visit Count" + "\n";
        CSVContents += visitorHeaders;
        StringBuilder CSVContentsBuilder = new StringBuilder(CSVContents);
        for(Person p : collection){
            String row = p.getId() + "," +p.getName() + "," + p.getEmail() + "," + p.getCertifications() +
                    "," + p.getStrikes() + "," + p.getNotes() + "," + p.getTimesVisited() + "\n";
            CSVContentsBuilder.append(row);
        }
        CSVContents = CSVContentsBuilder.toString();

        DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("MM-dd-yyyy");
        Path path = Paths.get(Constants.analyticsFolder.toString(), "Directory" + dateTimeFormatter.print(DateTime.now()) + ".csv");
        try {
            FileUtils.writeStringToFile(path.toFile(), CSVContents, Charset.defaultCharset());
            Desktop.getDesktop().open(Constants.analyticsFolder);
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "File generated!");
            alert.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Failed creating file." + e.getMessage());
            alert.showAndWait();
        }
    }

    /**
     * Used to bring older versions of the json database up to date.
     * @param person Person object to be validated
     */
    public static void validateUpToDateJson(Person person){
        if(person != null) {
            if ((person.strikesProperty() == null || person.getStrikes() == null)) {
                person.setStrikesProperty("0");
            }
            if(person.timesVisitedProperty() == null || person.getTimesVisited() == null){
                person.setTimesVisitedProperty("0");
            }
            if(person.getTimeStampHistory() == null){
                person.setTimeStampHistory(new ArrayList<>());
            }
            if(person.getCertifications() == null || person.shopCertificationProperty() == null){
                person.setCertifications(new ArrayList<>());
            }
            if(person.signedWaiverProperty() == null || person.getSignedWaiver() == null){
                person.setSignedWaiver("No");
            }
        }
    }

    public static void saveHashMapToCSV(String filename, String keyName, String valName, HashMap<String, Integer> input){
        String CSVContents = keyName + ", " + valName + "\n";

        StringBuilder CSVContentsBuilder = new StringBuilder(CSVContents);
        for(String key:input.keySet()){
            CSVContentsBuilder.append(key + ", "+ input.get(key).toString() + "\n");
        }
        CSVContents = CSVContentsBuilder.toString();

        Path path = Paths.get(Constants.analyticsFolder.toString(), filename + ".csv");
        try {
            FileUtils.writeStringToFile(path.toFile(), CSVContents, Charset.defaultCharset());
            Desktop.getDesktop().open(Constants.analyticsFolder);
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Failed creating file." + e.getMessage());
            alert.showAndWait();
        }
    }
}