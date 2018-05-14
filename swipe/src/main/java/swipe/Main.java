package swipe;

import swipe.awsapi.AWSCRUD;
import swipe.data.Constants;
import swipe.data.PersonModel;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import swipe.util.FileManager;
import swipe.view.AddController;
import swipe.view.DatePickerController;
import swipe.view.MainController;
import swipe.view.CertController;

import java.util.ConcurrentModificationException;
import java.util.Map;

public class Main extends Application {

    //Must be updated at each release iteration.
    private static final String VERSION = "1.4.4";

    @Override
    public void start(Stage primaryStage) throws Exception{
        setupFiles();
        PersonModel directoryModel = new PersonModel();
        PersonModel checkinModel = new PersonModel();
        directoryModel.loadFromFiles(Constants.directoryFiles);

        //Set up resources and model structure
        FXMLLoader mainLoader = new FXMLLoader(getClass().getResource("/signin.fxml"));
        Parent root = mainLoader.load();
        MainController mainController = mainLoader.getController();
        mainController.initModel(checkinModel, directoryModel);

        FXMLLoader addLoader = new FXMLLoader(getClass().getResource("/entry.fxml"));
        Parent addRoot = addLoader.load();
        //.load() MUST be called before getting the controller.
        AddController addController = addLoader.getController();
        addController.setRoot(addRoot);
        addController.setupStage();
        addController.initParentController(mainController);

        FXMLLoader datePickerLoader = new FXMLLoader(getClass().getResource("/analytic_date_picker.fxml"));
        Parent dpRoot = datePickerLoader.load();
        DatePickerController datePickerController = datePickerLoader.getController();
        datePickerController.setRoot(dpRoot);
        datePickerController.setupStage();
        datePickerController.initParentController(mainController);

        FXMLLoader certLoader = new FXMLLoader(getClass().getResource("/certification.fxml"));
        Parent cRoot = certLoader.load();
        CertController certController = certLoader.getController();
        certController.setRoot(cRoot);
        certController.setupStage();
        certController.initParentController(mainController);

        mainController.initControllers(addController, datePickerController, certController);

        primaryStage.setTitle("Innovation Hub Analytics "  + getVersion());
        primaryStage.setScene(new Scene(root, 1060  , 650));
        primaryStage.setOnCloseRequest(event -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Would you like to sign out all users?", ButtonType.YES, ButtonType.NO);
            alert.showAndWait();
            if (alert.getResult() == ButtonType.YES) {
                try {
                    mainController.signOutAll();
                }catch (ConcurrentModificationException e){
                    e.printStackTrace();
                    Platform.exit();
                }
                Platform.exit();
            }else{
                event.consume();
            }
        });
        setUserAgentStylesheet(STYLESHEET_CASPIAN);
        primaryStage.show();
    }

    private void setupFiles(){
        FileManager.setupFolders();
    }

    public static void main(String[] args) {
        launch(args);
    }

    /**
     * DO NOT CHANGE SIGNATURE. USED BY Main UPDATER BY REFLECTION
     * @return Returns version of this jar.
     */
    public static String getVersion(){
        return VERSION;
    }
}
