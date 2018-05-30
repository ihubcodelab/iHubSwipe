package swipe.view;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.DatePicker;
import javafx.stage.Stage;
import swipe.data.Person;
import swipe.data.Timestamp;
import swipe.util.AnalyticUtil;
import swipe.util.FileManager;
import java.net.URL;
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
        //Don't let people do student demographics and daily/hourly, could change in the future
        studentInfoCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if(studentInfoCheckBox.isSelected()){
                hourCheckBox.setSelected(false);
                dayCheckBox.setSelected(false);
            }
        });
        hourCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if(hourCheckBox.isSelected()) studentInfoCheckBox.setSelected(false);
        });
        dayCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if(dayCheckBox.isSelected())studentInfoCheckBox.setSelected(false);
        });


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

        AnalyticUtil.StudentInfo[] studentInfos;
        //get info from checkBoxes
        boolean sortByHour = hourCheckBox.isSelected();
        boolean sortByDay = dayCheckBox.isSelected();
        boolean sortByDept = departmentCheckBox.isSelected();

        boolean getAvgVisitTime = avgVisitTimeCheckBox.isSelected();
        boolean getNumVisitors = numVisitorsCheckBox.isSelected();
        boolean getNumUnique = numUniqueCheckBox.isSelected();
        boolean getStudentInfo = studentInfoCheckBox.isSelected();
        String filename = getRangeForFilename() + ((sortByDay && sortByHour) ? "dailyhourly" :
                (sortByDay ? "daily" : (sortByHour ? "hourly" : "overall")));
        String CSVData =
                ((sortByDay && sortByHour) ? "Day of Week, Hour Block" :
                        (sortByDay ? "Day of Week" : (sortByHour ? "Hour Block" : filename)))
                +(getAvgVisitTime ? ", Avg. Visit Time (min)" : "")
                +(getNumVisitors ? ", Num. Visitors" : "")
                +(getNumUnique ? ", Num. Unique Visitors" : "")
                +(getStudentInfo ? ", Freshmen, Sophomores, Juniors, Seniors, Grad Students, Other" : "")
                +"\n";
        StringBuilder CSVBuilder = new StringBuilder(CSVData);
        int[] visitorCounts = AnalyticUtil.numVisitors(filteredDirectory.values(), sortByDay, sortByHour);
        int[] uniqueVisitorCounts = AnalyticUtil.numUniqueVisitors(filteredDirectory.values(), sortByDay, sortByHour);
        if(getStudentInfo){
            studentInfos = AnalyticUtil.getStudentStandingInfo(filteredDirectory, sortByDay,sortByHour);
        } else {
            studentInfos = new AnalyticUtil.StudentInfo[1];
        }

        int[] avgVisitTimes = AnalyticUtil.avgVisitTime(filteredDirectory.values(), sortByDay, sortByHour);

        for (int i = 0; i< visitorCounts.length; i++){
            CSVBuilder.append(((sortByDay && sortByHour) ? (dayToString(i/12)+", "+hourToString(i%12)) :
                            (sortByDay ? dayToString(i) : (sortByHour ? hourToString(i) : "")))
                    +(getAvgVisitTime ? ", " +avgVisitTimes[i] : "")
                    +(getNumVisitors ? ", " +visitorCounts[i] : "")
                    +(getNumUnique ? ", " +uniqueVisitorCounts[i] : "")
                    +(getStudentInfo ? ", " +studentInfos[i] : "")
                    +"\n");
        }
        CSVData = CSVBuilder.toString();
        FileManager.saveAnalyticsStringAsCSV(filename, CSVData);
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
