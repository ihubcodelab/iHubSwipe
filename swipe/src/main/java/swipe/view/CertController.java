package swipe.view;


import swipe.data.Certification;
import swipe.data.Person;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.time.LocalDate;
import swipe.util.FileManager;
import swipe.util.LogManager;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * TODO: Add description of class here
 */
public class CertController implements Initializable{

    private Stage stage;
    private Parent root;
    private MainController parentController;
    private Person person;

    @FXML
    Button doneButton;
    @FXML
    Button addButton;
    @FXML
    Button removeButton;
    @FXML
    DatePicker expirationPicker;
    @FXML
    ComboBox labCert;
    @FXML
    ChoiceBox shopLevel;
    @FXML
    DatePicker shopExpPicker;
    @FXML
    Button addShopButton;
    @FXML
    TableView<Certification> certTable;
    @FXML
    TableColumn<Certification, String> nameCol;
    @FXML
    TableColumn<Certification, String> expCol;
    @FXML
    TableColumn<Certification, Boolean> typeCol;

    private ObservableList<Certification> observableCerts;
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MM-dd-yyyy");


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        //associates cells in the com.fla.view with Certification object attributes
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        expCol.setCellValueFactory(new PropertyValueFactory<>("expiration"));
        typeCol.setCellValueFactory(new PropertyValueFactory<>("isShopCert"));
        setupCellFactories();
        shopLevel.setItems(FXCollections.observableArrayList(
                "Green", "Yellow", "Red"));
        //These are the certifications that will show up as defaults
        //User can add their own too
        labCert.setItems(FXCollections.observableArrayList(
                "Laser Cutter", "3D Printer", "Embroidery", "CNC"
        ));
        //preset date pickers to the next august
        setDatePickersToNextAugust();

        labCert.setEditable(true);

        //do item.setOnAction(event -> function()); here
        doneButton.setOnAction(event -> close());
        addButton.setOnAction(event -> addCert(false));
        addShopButton.setOnAction(event -> addCert(true));
        removeButton.setOnAction(event -> removeSelectedCert());

    }

    public void setRoot(Parent root){
        this.root = root;
    }

    public void initParentController(MainController mainController){
        if(parentController != null) throw new IllegalStateException("Parent MainController already initialized.");
        this.parentController = mainController;
    }

    private void initTables(){
        certTable.setItems(observableCerts);
    }

    public void setupStage(){
        stage = new Stage();
        stage.setTitle("Manage Certifications");
        stage.setScene(new Scene(root,661  , 371));
        stage.setResizable(false);
        stage.setOnCloseRequest(event -> close());
    }

    /**
     * FabLab told me they want certs to expire at the start of every school year
     * So we'll set the default values to whatever the next August is from the current date
     *
     */
    private void setDatePickersToNextAugust(){
        LocalDate now = LocalDate.now();
        int yearShift = 0;
        if (now.getMonthValue()>=9){
            yearShift++;
        }
        expirationPicker.setValue(LocalDate.of(now.getYear() + yearShift,9,17));
        shopExpPicker.setValue(LocalDate.of(now.getYear() + yearShift,9,17));

    }

    public void open(Person person) {
        this.person = person;
        observableCerts = FXCollections.observableArrayList(param -> new Observable[]{
                param.nameProperty(),
                param.expProperty(),
                param.isShopCertProperty()
        });
        ArrayList<Certification> certs;
        if (person!=null){
            certs = person.getCertifications();
        } else {
            certs = new ArrayList<Certification>();
        }
        observableCerts.addAll(certs);
        initTables();
        stage.show();
    }



    private void addCert(boolean shop){
        Certification newCert = new Certification();
        String newExp;
        newCert.setShopCert(shop);
        if (!shop){
            newExp = expirationPicker.getValue().format(dtf);
            newCert.setCertName(labCert.getValue().toString());
        } else {
            newExp = shopExpPicker.getValue().format(dtf);
            newCert.setCertName(shopLevel.getValue().toString());
        }
        newCert.setExpiration(newExp);
        observableCerts.add(newCert);
    }

    /**
     * here is where we can adjust custom styling for different column based on
     * what values the cells have
     */
    private void setupCellFactories() {
        expCol.setCellFactory(column -> new TableCell<Certification, String>() {
           protected void updateItem(String item, boolean empty) {
               super.updateItem(item, empty);
               if(item == null || empty){
                   setText(null);
                   setStyle("");
               } else {
                   setText(getString());
                   Certification cert = new Certification("",getString());
                   //this is a testing thing right now
                   //can change css for the cells and other stuff
                   if (cert.isExpired()) {
                       setStyle("-fx-background-color: #C14242");
                   }
               }
           }
            private String getString() {
                return getItem() == null ? "" : getItem();
            }
        });
        typeCol.setCellFactory(column -> new TableCell<Certification, Boolean>() {
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if(item == null || empty){
                    setText(null);
                    setStyle("");
                } else {
                    if (getString().equals("true")){
                        setText("Shop");
                    } else {
                        setText("Lab");

                    }
                }
            }
            private String getString() {
                return getItem() == null ? "" : getItem()+"";
            }
        });

    }

    private void removeSelectedCert() {
       observableCerts.remove(certTable.getSelectionModel().getFocusedIndex());
    }

    private void invalidateViews(){
        certTable.refresh();
    }

    private void close(){
        //do item.setValue(null); for everything
        LogManager.appendLogWithTimeStamp(person.getName() + " with ID: " + person.getId() + " had their Certifications updated");
        FileManager.deleteDirectoryFile(person);


        List<Certification> certs = certTable.getItems();
        person.setCertifications(new ArrayList<Certification>(certs));

        parentController.getAddController().setCerts(new ArrayList<Certification>(certs));

        FileManager.saveDirectoryJsonFile(person);
        parentController.invalidateViews();
        stage.close();
    }
}
