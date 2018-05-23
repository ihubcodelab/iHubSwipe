package swipe.view;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.DatePicker;
import javafx.stage.Stage;
import org.apache.commons.io.FileUtils;
import swipe.data.Constants;
import swipe.data.Person;
import swipe.data.Timestamp;
import swipe.util.AnalyticUtil;
import swipe.util.FileManager;

import javax.print.DocFlavor;
import java.awt.*;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;

public class AnalyticsController implements Initializable {

    @FXML
    Button okButton;
    @FXML
    Button cancelButton;
    @FXML
    CheckBox hourCheckBox;
    @FXML
    CheckBox dayCheckBox;
    @FXML
    CheckBox departmentCheckBox;
    @FXML
    CheckBox avgVisitTimeCheckBox;
    @FXML
    CheckBox numVisitorsCheckBox;
    @FXML
    CheckBox numUniqueCheckBox;
    @FXML
    CheckBox studentInfoCheckBox;
    @FXML
    DatePicker datePicker1;
    @FXML
    DatePicker datePicker2;

    private Stage stage;
    private Parent root;
    private Collection<Person> directory;
    private HashMap<String, List<Timestamp>> filteredDirectory;
    private MainController parentController;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        //set default values for the datePickers here
        datePicker1.setValue(LocalDate.of(2017,9, 1));
        datePicker2.setValue(LocalDate.now());
        okButton.setOnAction(event -> runAnalytics());
        cancelButton.setOnAction(event -> close());
    }

    public void initParentController(MainController mainController){
        if(parentController != null) throw new IllegalStateException("Parent MainController already initialized.");
        this.parentController = mainController;

    }

    public void open(Collection<Person> directory){
        this.directory = directory;
        stage.show();
    }

    private void close(){
        //reset date pickers
        stage.close();
    }

    private void filter() {
        filteredDirectory = AnalyticUtil.filterForDateRange(directory, datePicker1.getValue(), datePicker2.getValue());

    }

    private void runAnalytics() {
        //set filteredDirectory to have timestamp collection filtered for date range
        filter();
        //get info from checkBoxes
        boolean sortByHour = hourCheckBox.isSelected();
        boolean sortByDay = dayCheckBox.isSelected();
        boolean sortByDept = departmentCheckBox.isSelected();

        boolean getAvgVisitTime = avgVisitTimeCheckBox.isSelected();
        boolean getNumVisitors = numVisitorsCheckBox.isSelected();
        boolean getNumUnique = numUniqueCheckBox.isSelected();
        boolean getStudentInfo = studentInfoCheckBox.isSelected();
        String filename = getRangeForFilename();
        int personCount = 0;

        String CSVData =
                ((sortByDay && sortByHour) ? "Day of Week, Hour Block" :
                        (sortByDay ? "Day of Week" : (sortByHour ? "Hour Block" : filename)))
                +(getAvgVisitTime ? ", Avg. Visit Time (min)" : "")
                +(getNumVisitors ? ", Num. Visitors" : "")
                +(getNumUnique ? ", Num. Unique Visitors" : "")
                +(getStudentInfo ? ", Student Info" : "")
                +"\n";
        StringBuilder CSVBuilder = new StringBuilder(CSVData);
        int[] visitorCounts = AnalyticUtil.numVisitors(filteredDirectory.values(), sortByDay, sortByHour);


        // may want to refactor this code when/if marking by department happens
        if (sortByDay && sortByHour){

            for (int i = 0; i< visitorCounts.length; i++){
                CSVBuilder.append(dayToString(i/12)+", "+hourToString(i%12)
                        +(getAvgVisitTime ? ", " +visitorCounts[i] : "")
                        +(getNumVisitors ? ", " +visitorCounts[i] : "")
                        +(getNumUnique ? ", " +visitorCounts[i] : "")
                        +(getStudentInfo ? ", " +visitorCounts[i] : "")
                        +"\n");
            }
            CSVData = CSVBuilder.toString();
            FileManager.saveAnalyticsStringAsCSV(filename, CSVData);
        } else if (sortByDay) {
            CSVData = "Day Of Week, Num. Visitors\n";
            CSVBuilder = new StringBuilder(CSVData);
            for (int i = 0; i< visitorCounts.length; i++){
                CSVBuilder.append(dayToString(i)+", "+visitorCounts[i]+"\n");
            }
            CSVData = CSVBuilder.toString();
            FileManager.saveAnalyticsStringAsCSV(filename, CSVData);
        } else if (sortByHour) {
            CSVData = "Hour Block, Num. Visitors\n";
            CSVBuilder = new StringBuilder(CSVData);
            for (int i = 0; i< visitorCounts.length; i++){
                CSVBuilder.append(hourToString(i)+", "+visitorCounts[i]+"\n");
            }
            CSVData = CSVBuilder.toString();
            FileManager.saveAnalyticsStringAsCSV(filename, CSVData);
        } else {
            //JUST SHOW AGGREGATE DATA
            CSVData +=
                    (getAvgVisitTime ? ", " +visitorCounts[0] : "")
                    +(getNumVisitors ? ", " +visitorCounts[0] : "")
                    +(getNumUnique ? ", " +visitorCounts[0] : "")
                    +(getStudentInfo ? ", " +visitorCounts[0] : "")
                    +"\n";
            FileManager.saveAnalyticsStringAsCSV(filename, CSVData);
        }
        stage.close();
    }

    public void setupStage(){
        stage = new Stage();
        stage.setTitle("Analytics Tool");
        //Bailey hardcoded the values below like a total jerk ugh
        stage.setScene(new Scene(root,305  , 347));
        stage.setResizable(false);
        stage.setOnCloseRequest(event -> close());
    }

    public void setRoot(Parent root){
        this.root = root;
    }

    /**
     * maps day values to Strings representing the days
     * @param day from 0-6 representing monday, tuesday, ... sunday
     * @return text name of provided day
     */
    public String dayToString(int day){
        String[] daysOfWeek = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday","Saturday", "Sunday"};
        return daysOfWeek[day];
    }

    /**
     * similar to dayToString, but returns time strings for the given hour
     * @param hour
     * @return
     */
    public String hourToString(int hour){
        String[] hourBlocks = {"9-10AM","10-11AM", "11AM-12PM", "12-1PM", "1-2PM",
                "2-3PM", "3-4PM", "4-5PM","5-6PM","6-7PM", "7-8PM", "8-9PM"};
        return hourBlocks[hour];
    }

    private String getRangeForFilename(){
        return datePicker1.getValue().toString() +" to "+ datePicker2.getValue().toString();
    }
}
