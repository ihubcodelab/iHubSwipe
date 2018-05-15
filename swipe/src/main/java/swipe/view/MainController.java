package swipe.view;
import swipe.awsapi.AWSCRUD;
import swipe.data.Constants;
import swipe.data.Person;
import swipe.data.PersonModel;
import swipe.data.Timestamp;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import swipe.util.AnalyticUtil;
import swipe.util.FileManager;
import swipe.util.LogManager;
import swipe.util.WebUtil;

import java.net.URL;
import java.util.*;

/**
 * TODO: Add description of class here
 */
public class MainController implements Initializable {

    @FXML
    TableView<Person> CheckinTable;
    @FXML
    TableView<Person> DirectoryTable;
    @FXML
    TableColumn<Person, String> CIDColumn;
    @FXML
    TableColumn<Person, String> CNameColumn;
    @FXML
    TableColumn<Person, String> CTimestampColumn;
    @FXML
    TableColumn<Person, String> CCertificationsColumn;
    @FXML
    TableColumn<Person, String> CNotesColumn;
    @FXML
    TableColumn<Person, String> CStrikesColumn;
    @FXML
    TableColumn<Person, String> CVisitColumn;
    @FXML
    TableColumn<Person, String> DIDColumn;
    @FXML
    TableColumn<Person, String> DNameColumn;
    @FXML
    TableColumn<Person, String> DEmailColumn;
    @FXML
    TableColumn<Person, String> CEmailColumn;
    @FXML
    TableColumn<Person, String> DCertificationsColumn;
    @FXML
    TableColumn<Person, String> DNotesColumn;
    @FXML
    TableColumn<Person, String> DStrikesColumn;
    @FXML
    TableColumn<Person, String> DVisitColumn;
    @FXML
    TableColumn<Person, String> DlabCertColumn;
    @FXML
    TableColumn<Person, String> ClabCertColumn;
    @FXML
    TableColumn<Person, String> DshopCertColumn;
    @FXML
    TableColumn<Person, String> CshopCertColumn;
    @FXML
    TableColumn<Person, String> CWaiverColumn;
    @FXML
    TableColumn<Person, String> DWaiverColumn;


    @FXML
    Button signInButton;
    @FXML
    TextField idField;
    @FXML
    TextField searchField;
    @FXML
    Tab directoryTab;
    @FXML
    Tab checkedInTab;
    @FXML
    TabPane tabPane;
    @FXML
    Tab logTab;
    @FXML
    MenuItem openFolderMenuButton;
    @FXML
    MenuItem addMenuItem;
    @FXML
    MenuItem deleteMenuItem;
    @FXML
    MenuItem editMenuItem;
    @FXML
    MenuItem forceSignInOutMenuItem;
    @FXML
    MenuItem addCertMenuItem;
    @FXML
    MenuItem conversionButton; //this is the button in 'Edit' labelled 'Debug'
    @FXML
    MenuItem dlDirectoryButton;
    @FXML
    MenuItem exportDirectory;
    @FXML
    MenuItem aboutButton;
    @FXML
    MenuItem exportAnalytics;
    @FXML
    RadioMenuItem generalLayout;
    @FXML
    RadioMenuItem fablabLayout;
    @FXML
    RadioMenuItem visCodelabLayout;
    @FXML
    TextArea logTextArea;

    private Person selectedPerson;

    private PersonModel checkInModel;
    protected PersonModel directoryModel;
    private AddController addController;
    private DatePickerController datePickerController;
    private CertController certController;

    public CertController getCertController() {
        return certController;
    }
    public AddController getAddController() {
        return addController;
    }

    public void initModel(PersonModel checkInModel, PersonModel directoryModel){
        this.checkInModel = checkInModel;
        this.directoryModel = directoryModel;
        CheckinTable.setItems(checkInModel.getObservableList());
        DirectoryTable.setItems(directoryModel.getObservableList());
    }

    public void initControllers(AddController addController, DatePickerController datePickerController, CertController certController){
        if(this.addController != null || this.datePickerController != null || this.certController != null){
            throw new IllegalStateException("Controllers already initialized");
        }
        this.addController = addController;
        this.datePickerController = datePickerController;
        this.certController = certController;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        //associates cells in the tables with Person object attributes
        CIDColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        CNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        CEmailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        CCertificationsColumn.setCellValueFactory(new PropertyValueFactory<>("certifications"));
        CTimestampColumn.setCellValueFactory(new PropertyValueFactory<>("timestamp"));
        CNotesColumn.setCellValueFactory(new PropertyValueFactory<>("notes"));
        CStrikesColumn.setCellValueFactory(new PropertyValueFactory<>("strikes"));
        CVisitColumn.setCellValueFactory(new PropertyValueFactory<>("timesVisited"));
        DIDColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        DNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        DCertificationsColumn.setCellValueFactory(new PropertyValueFactory<>("labCertification"));
        ClabCertColumn.setCellValueFactory(new PropertyValueFactory<>("labCertification"));
        DlabCertColumn.setCellValueFactory(new PropertyValueFactory<>("labCertification"));
        CshopCertColumn.setCellValueFactory(new PropertyValueFactory<>("shopCertification"));
        DshopCertColumn.setCellValueFactory(new PropertyValueFactory<>("shopCertification"));
        DWaiverColumn.setCellValueFactory(new PropertyValueFactory<>("signedWaiver"));
        CWaiverColumn.setCellValueFactory(new PropertyValueFactory<>("signedWaiver"));
        DEmailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        DNotesColumn.setCellValueFactory(new PropertyValueFactory<>("notes"));
        DStrikesColumn.setCellValueFactory(new PropertyValueFactory<>("strikes"));
        DVisitColumn.setCellValueFactory(new PropertyValueFactory<>("timesVisited"));

        setupCellFactories();
        //link up button presses etc to actions
        signInButton.setOnAction(event -> handleSwipe(false));
        idField.setOnAction(event -> handleSwipe(false));
        searchField.setOnKeyTyped(event -> focusSearch());
        searchField.setOnAction(event -> AWSCRUD.searchName(searchField.getText()));
        openFolderMenuButton.setOnAction(event -> FileManager.openFolderExplorer());
        addMenuItem.setOnAction(event -> openAddWindow("", false));
        editMenuItem.setOnAction(event-> editSelected());
        editMenuItem.setOnAction(event-> editSelected());
        deleteMenuItem.setOnAction(event -> deleteSelected());
        forceSignInOutMenuItem.setOnAction(event -> forceSignInOut());
        addCertMenuItem.setOnAction(event -> manageCerts());
        logTab.setOnSelectionChanged(event -> {
            Platform.runLater(()->logTextArea.setScrollTop(Double.MAX_VALUE));
            refocusIdField(true);
        });
        conversionButton.setOnAction(event -> AWSCRUD.uploadDirectory(directoryModel));
        dlDirectoryButton.setOnAction(event -> AWSCRUD.downloadDirectory());
        exportDirectory.setOnAction(event ->  FileManager.getDirectoryAsCSV(directoryModel.getObservableList(), null));
        exportAnalytics.setOnAction(event -> AnalyticUtil.exportAnalytics(directoryModel.getObservableList()));
        aboutButton.setOnAction(event -> WebUtil.openWebpage(Constants.aboutLink));
        logTextArea.setText(Constants.logContents);

        CheckinTable.setOnMouseClicked(event -> doubleClickCheck(event));
        DirectoryTable.setOnMouseClicked(event -> doubleClickCheck(event));

        ToggleGroup layoutGroup = new ToggleGroup();
        generalLayout.setToggleGroup(layoutGroup);
        fablabLayout.setToggleGroup(layoutGroup);
        visCodelabLayout.setToggleGroup(layoutGroup);
        fablabLayout.setSelected(true);

        generalLayout.setOnAction(this::changeLayout);
        fablabLayout.setOnAction(this::changeLayout);
        visCodelabLayout.setOnAction(this::changeLayout);
        CIDColumn.setVisible(false);
        DIDColumn.setVisible(false);
        Platform.runLater(() -> idField.requestFocus());
    }

    private void doubleClickCheck(MouseEvent event) {
        if (event.getClickCount()==2){
            //we got a double click
            editSelected();
        }
    }


    private void setupCellFactories() {
        DshopCertColumn.setCellFactory(column -> new TableCell<Person, String>() {
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if(item == null || empty){
                    setText(null);
                    setStyle("");
                }else {
                    setText(getString());
                    //this is a testing thing right now
                    //can change css for the cells and other stuff
                    if (getString().toLowerCase().equals("red")) {
                        setStyle("-fx-background-color: #C14242");
                    } else if (getString().toLowerCase().equals("yellow")) {
                        setStyle("-fx-background-color: #E8E868");
                    } else if (getString().toLowerCase().equals("green")) {
                        setStyle("-fx-background-color: #4BE64B");
                    }else{
                        setStyle("");
                    }
                }
            }
            private String getString() {
                return getItem() == null ? "" : getItem();
            }
        });
        DWaiverColumn.setCellFactory(column -> new TableCell<Person, String>() {
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if(item == null || empty){
                    setText(null);
                    setStyle("");
                }else {
                    setText(getString());
                    if (getString().toLowerCase().equals("no")) {
                        setStyle("-fx-text-fill: #C14242");
                    }
                }
            }
            private String getString() {
                return getItem() == null ? "" : getItem();
            }
        });
        CWaiverColumn.setCellFactory(DWaiverColumn.getCellFactory());
        CshopCertColumn.setCellFactory(DshopCertColumn.getCellFactory());
        CCertificationsColumn.setCellFactory(DshopCertColumn.getCellFactory());
        DCertificationsColumn.setCellFactory(DshopCertColumn.getCellFactory());
    }

    private void focusSearch() {
        for(int i = 0; i < DirectoryTable.getItems().size(); i++){
            if(DNameColumn.getCellData(i).toLowerCase().contains(searchField.getText().toLowerCase())){
                tabPane.getSelectionModel().select(directoryTab);
                DirectoryTable.getFocusModel().focus(i);
                DirectoryTable.scrollTo(i);
                break;
            }
        }
    }

    private void deleteSelected() {
        TableView<Person> tableView = getFocusedTableView();
        if(tableView != null) {
            int index = tableView.getSelectionModel().getFocusedIndex();
            selectedPerson = tableView.getItems().get(index);
            if(selectedPerson != null) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Delete " + selectedPerson.getName() + " from directory and check in permanently?");
                alert.showAndWait();
                if (alert.getResult() == ButtonType.OK) {
                    checkInModel.remove(selectedPerson);
                    directoryModel.remove(selectedPerson);
                    FileManager.deleteDirectoryFile(selectedPerson);
                    LogManager.appendLogWithTimeStamp(selectedPerson.getName() + " with ID: " + selectedPerson.getId() + " was deleted from the directory.");
                }
            }
        }
    }

    private void editSelected() {
        TableView<Person> tableView = getFocusedTableView();
        if(tableView != null){
            int index = tableView.getSelectionModel().getFocusedIndex();
            selectedPerson = getSelectedPerson();
            openAddWindow(selectedPerson.getId(), true);
        }
    }

    private TableView<Person> getFocusedTableView(){
        TableView<Person> tableView = null;
        if(DirectoryTable.isFocused()){
            tableView = DirectoryTable;
        }else if(CheckinTable.isFocused()){
            tableView = CheckinTable;
        }
        return tableView;
    }


    /**
     * gets com.fla.data from id field as it's populated by the card swipe
     * @param wasForced used when Manual SignIn/Out is used
     */
    private void handleSwipe(boolean wasForced){
        String cardInput = idField.getText();
        try {
            cardInput = parseSwipe(cardInput);
            //see if person is already checked in
            if(checkInModel.getByID(cardInput)!=null){
                signOut(checkInModel.getByID(cardInput), wasForced);
                refocusIdField(true);
                return;
            } else {
                //lookup on AWS
                Person person = AWSCRUD.read(cardInput);
                if (person!=null){
                    signIn(person, wasForced);
                    refocusIdField(true);
                    return;
                } else {
                    openAddWindow(cardInput, false);
                    return;
                }
            }
        } catch (SwipeException e) {
            //problem reading card (probably just a bad swipe)
            System.out.println("error with this swipe input: " + e.getSwipe());
            Alert alert = new Alert(Alert.AlertType.ERROR, "Error reading card. Try putting a post it over the mag stripe and try again (sounds crazy but should work).");
            idField.clear();
            alert.showAndWait();
        }

    }

    /**
     * cleans input from card swipe, assumes an OU student ID is being swiped
     * @param input String from input feild
     * @return just the student ID number
     * @throws SwipeException if there was a an error reading the card, should be handled by handleSwipe
     */
    private String parseSwipe(String input) throws SwipeException{
        if(input.charAt(0)==';' || input.charAt(0)=='%'){
            if(input.charAt(1)!='E'){
                input = input.substring(6,15);
                return input;
            }
            //error reading card
            throw new SwipeException(input);
        }
        //if the input doesn't have the semicolon
        //then it's not a swipe, it's a user input
        return input;

    }

    private void signOut(Person p, boolean forced){
        if(!p.getTimeStampHistory().isEmpty()) p.getTimeStampHistory().get(p.getTimeStampHistory().size() - 1).setEnd(Timestamp.getCurrentTime());
        FileManager.saveDirectoryJsonFile(p);
        AWSCRUD.create(p);
        Boolean deleteOutcome  = checkInModel.remove(p);
        System.out.println("deleted from checkinmodel? " + deleteOutcome);
        LogManager.appendLogWithTimeStamp(forced ? p.getName() + " was signed out(MANUAL) with " + "ID: " + p.getId() : p.getName() + " was signed out with " + "ID: " + p.getId());
        idField.clear();
    }

    public void signIn(Person p, boolean forced){
        p.incrementTimesVisited();
        checkInModel.add(p);
        p.setTimestampProperty(Timestamp.getCurrentTime());
        p.getTimeStampHistory().add(Timestamp.Now());
        LogManager.appendLogWithTimeStamp(forced ? p.getName() + " was signed in(MANUAL) with " + "ID: " + p.getId() : p.getName() + " was signed in with " + "ID: " + p.getId());
        invalidateViews();
        idField.clear();
    }

    private void openAddWindow(String input, boolean isEditMode){
        addController.open(input, getSelectedPerson(), isEditMode);
    }

    public void signOutAll(){
        //Stop concurrent modifications
        List<Person> list = new ArrayList<>(checkInModel.getObservableList());
        for (Person p :list) {
            signOut(p, false);
        }
    }

    private Person getSelectedPerson(){
        TableView<Person> tableView = getFocusedTableView();
        Person person = null;
        if(tableView != null && tableView.getItems().size() > 0) {
            person = tableView.getItems().get(tableView.getSelectionModel().getFocusedIndex());
        }
        return person;
    }

    /**
     * Called when user click the "Manage Certifications" button in the "Edit" menu
     * Brings up dialog to add cert
     */
    private void manageCerts() {
        selectedPerson = getSelectedPerson();
        if (selectedPerson == null) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Please select a person to edit certifications for.");
            alert.show();
        } else {
            certController.open(selectedPerson);
        }
    }

    private void forceSignInOut() {
        TableView<Person> tableView = getFocusedTableView();
        if(tableView != null) {
            Person person = tableView.getItems().get(tableView.getSelectionModel().getFocusedIndex());
            if (checkInModel.contains(person)){
                signOut(person, true);
            } else {
                signIn(person, true);
            }
            refocusIdField(true);
        }
    }

    public void refocusIdField(boolean runLater){
        updateLogDisplay();
        if(runLater){
            Platform.runLater(()-> idField.requestFocus());
            idField.clear();
            return;
        }
        idField.requestFocus();
        idField.clear();
    }

    public void invalidateViews(){
        DirectoryTable.refresh();
        CheckinTable.refresh();
        updateLogDisplay();
    }

    private void updateLogDisplay(){
        logTextArea.setText(Constants.logContents);
    }

    private void changeLayout(Event event){
        //Default to codelab settings - all true
        boolean strikes = true;
        boolean certs = true; //Whole certs tab
        boolean timestamp = true;
        boolean notes = true;
        boolean visits = true;
        boolean labCert = true;
        boolean shopCert = true;
        if(event.getSource().equals(generalLayout)){
            strikes = false;
            certs = false;
            timestamp = false;
            notes = false;
            visits = false;
            labCert = false;
            shopCert = false;
        }else if(event.getSource().equals(visCodelabLayout)){
            labCert = false;
            shopCert = false;
        }
        DCertificationsColumn.setVisible(certs);
        CCertificationsColumn.setVisible(certs);
        DStrikesColumn.setVisible(strikes);
        CStrikesColumn.setVisible(strikes);
        CTimestampColumn.setVisible(timestamp);
        DNotesColumn.setVisible(notes);
        CNotesColumn.setVisible(notes);
        CVisitColumn.setVisible(visits);
        DVisitColumn.setVisible(visits);
        ClabCertColumn.setVisible(labCert);
        DlabCertColumn.setVisible(labCert);
        DshopCertColumn.setVisible(shopCert);
        CshopCertColumn.setVisible(shopCert);
    }
}


class SwipeException extends Exception{
    private String swipe;
    public SwipeException(String swipe){
        this.swipe = swipe;
    }
    public String getSwipe(){
        return swipe;
    }
}
