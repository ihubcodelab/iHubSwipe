package swipe.view;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.DatePicker;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class DatePickerController implements Initializable {

    private Stage stage;
    private Parent root;
    private MainController parentController;

    @FXML
    DatePicker datePicker1;
    @FXML
    DatePicker datePicker2;
    @FXML
    DatePicker datePicker3;
    @FXML
    DatePicker datePicker4;
    @FXML
    Button okButton;
    @FXML
    Button cancelButton;
    @FXML
    CheckBox includeAggregate;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cancelButton.setOnAction(event -> close());
        okButton.setOnAction(event -> requestData());
    }

    private void requestData() {
        //TODO Implement range queries here


    }

    public void setRoot(Parent root){
        this.root = root;
    }
    public void initParentController(MainController mainController){
        if(parentController != null) throw new IllegalStateException("Parent MainController already initialized.");
        this.parentController = mainController;
    }

    public void setupStage(){
        stage = new Stage();
        stage.setTitle("Analytics Date Selection");
        stage.setScene(new Scene(root,235  , 165));
        stage.setResizable(false);
        stage.setOnCloseRequest(event -> close());
    }

    public void open() {
        stage.show();
    }

    private void close(){
        datePicker1.setValue(null);
        datePicker2.setValue(null);
        datePicker3.setValue(null);
        datePicker4.setValue(null);
        stage.close();
    }
}
