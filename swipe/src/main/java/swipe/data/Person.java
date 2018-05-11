package swipe.data;



import javafx.beans.property.SimpleStringProperty;
import swipe.security.SecureString;

import java.lang.reflect.Array;
import java.security.NoSuchAlgorithmException;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


public class Person {


    private SimpleStringProperty id;
    private SimpleStringProperty name;
    private SimpleStringProperty email;
    /*Lab Certification*/
    private ArrayList<Certification> certifications;
    private SimpleStringProperty notes;
    private SimpleStringProperty signedWaiver;
    private transient SimpleStringProperty timestamp;
    private SimpleStringProperty timesVisited;
    private SimpleStringProperty strikes;
    private ArrayList<Timestamp> timeStampHistory;

    public Person(){
        this.id = new SimpleStringProperty("");
        this.name = new SimpleStringProperty("");
        this.email = new SimpleStringProperty("");
        this.certifications = new ArrayList<>();
        this.notes = new SimpleStringProperty("");
        this.timestamp = new SimpleStringProperty(Timestamp.getCurrentTime());
        this.timesVisited = new SimpleStringProperty("0");
        this.strikes = new SimpleStringProperty("0");
        this.timeStampHistory = new ArrayList<>();
        this.signedWaiver = new SimpleStringProperty("No");
    }

    public Person(String ID, String name, String email, String notes, String strikes, String signedWaiver) {
        this.id = new SimpleStringProperty(ID);
        this.name = new SimpleStringProperty(name);
        this.email = new SimpleStringProperty(email);
        this.certifications = new ArrayList<>();
        this.notes = new SimpleStringProperty(notes);
        this.timestamp = new SimpleStringProperty(Timestamp.getCurrentTime());
        this.timesVisited = new SimpleStringProperty("0");
        this.strikes = new SimpleStringProperty(strikes);
        this.timeStampHistory = new ArrayList<>();
        this.signedWaiver = new SimpleStringProperty(signedWaiver);
    }

    public Person(String id, String name, String email, ArrayList<Certification> certifications, String notes, String signedWaiver, String timestamp, String timesVisited, String strikes, ArrayList<Timestamp> timeStampHistory) {
        this.id = new SimpleStringProperty(id);
        this.name = new SimpleStringProperty(name);
        this.email = new SimpleStringProperty(email);
        this.certifications = certifications;
        this.notes = new SimpleStringProperty(notes);
        this.signedWaiver = new SimpleStringProperty(signedWaiver);
        this.timestamp = new SimpleStringProperty(timestamp);
        this.timesVisited = new SimpleStringProperty(timesVisited);
        this.strikes = new SimpleStringProperty(strikes);
        this.timeStampHistory = timeStampHistory;
    }

    public void incrementTimesVisited() {
        timesVisited = new SimpleStringProperty(String.valueOf(Integer.valueOf(getTimesVisited()) + 1));
        //FileManager.saveDirectoryJsonFile(this);
    }

    public String getName() {
        return name.get();
    }
    public void setName(String name) {
        this.name.set(name);
    }

    public void setStrikes(String strikes) {
        this.strikes.set(strikes);
    }

    public void setId(String id) {
        this.id.set(id);
    }

    public SimpleStringProperty nameProperty() {
        return name;
    }

    public String getEmail() {
        return email.get();
    }
    public void setEmail(String email) {
        this.email.set(email);
    }

    public void setNotes(String notes) {
        this.notes.set(notes);
    }

    public SimpleStringProperty emailProperty() {
        return email;
    }

    public ArrayList<Certification> getCertifications() {
        return certifications;
    }

    public SimpleStringProperty labCertificationProperty(){
        if (certifications==null || certifications.isEmpty()){
            //no certs on file
            return new SimpleStringProperty("");
        } else {
            StringBuilder sb = new StringBuilder();
            List<Certification> labCerts = certifications.stream()
                    .filter(c -> !c.isShopCert() && !c.isExpired())
                    .collect(Collectors.toList());
            for (Certification c : labCerts) {
                if (labCerts.indexOf(c) == (labCerts.size() - 1)) {
                    sb.append(c.getCertName());
                } else {
                    sb.append(c.getCertName());
                    sb.append(", ");
                }

            }
            return new SimpleStringProperty(sb.toString());
        }
    }

    public SimpleStringProperty shopCertificationProperty(){
        StringBuilder sb = new StringBuilder();
        if (certifications==null || certifications.isEmpty()){
            //no certs on file
            return new SimpleStringProperty("");
        } else {
            List<Certification> labCerts = certifications.stream()
                    .filter(c -> c.isShopCert() && !c.isExpired())
                    .collect(Collectors.toList());
            for (Certification c : labCerts) {
                if (labCerts.indexOf(c) == (labCerts.size() - 1)) {
                    sb.append(c.getCertName());
                } else {
                    sb.append(c.getCertName());
                    sb.append(", ");
                }

            }
            return new SimpleStringProperty(sb.toString());
        }
    }

    public void setCertifications(ArrayList<Certification> certs){
        this.certifications = certs;
    }

    public String getNotes() {
        return notes.get();
    }

    public SimpleStringProperty notesProperty() {
        return notes;
    }


    public String getTimestamp() {
        if(timestamp==null) {
            return "";
        }
        return timestamp.get();
    }

    public SimpleStringProperty timestampProperty() {
        return timestamp;
    }

    public void setTimestampProperty(SimpleStringProperty simpleStringProperty) {
        this.timestamp = simpleStringProperty;
    }

    public void setTimestampProperty(String string) {
        this.timestamp = new SimpleStringProperty(string);
    }

    public String getId() {
        return id.get();
    }

    public SimpleStringProperty idProperty() {
        return id;
    }

    public String getTimesVisited() {
        return timesVisited.get();
    }

    public SimpleStringProperty timesVisitedProperty() {
        return timesVisited;
    }

    public void setTimesVisitedProperty(String string) {
        this.timesVisited = new SimpleStringProperty(string);
    }

    public void setStrikesProperty(String string) {
        this.strikes = new SimpleStringProperty(string);
    }

    public String getStrikes() {
        return strikes.get();
    }

    public SimpleStringProperty strikesProperty() {
        return strikes;
    }

    public ArrayList<Timestamp> getTimeStampHistory() {
        return timeStampHistory;
    }


    public void setTimeStampHistory(ArrayList<Timestamp> timeStampHistory) {
        this.timeStampHistory = timeStampHistory;
    }


    public String getSignedWaiver() {
        if (signedWaiver == null) {
            setSignedWaiver("No");
        }
        return signedWaiver.get();
    }

    public SimpleStringProperty signedWaiverProperty() {
        return signedWaiver;
    }

    public void setSignedWaiver(String signedWaiver) {
        if (this.signedWaiver == null) {
            this.signedWaiver = new SimpleStringProperty();
        }
        this.signedWaiver.set(signedWaiver);
    }

    public String getHashedID() {
        SecureString hashed = null;
        try {
            hashed = new SecureString(id.get());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return hashed.getContents();
    }

    @Override
    public String toString() {
        return "Person{" +
                " id=" + getId() +
                ", name=" + getName() +
                ", email=" + getEmail() +
                ", certifications=" + getCertifications() +
                ", notes=" + getNotes() +
                ", signedWaiver=" + getSignedWaiver() +
                ", timestamp=" + getTimestamp() +
                ", timesVisited=" + getTimesVisited() +
                ", strikes=" + getStrikes() +
                ", timeStampHistory=" + getTimeStampHistory() +
                '}';
    }

    /**
     * Sets the com.fla.data of this object to the com.fla.data from the given person.
     *
     * @param p Set of com.fla.data to set this person object to.
     */
    public void set(Person p) {
        this.id = p.id;
        this.name = p.name;
        this.email = p.email;
        this.certifications = p.certifications;
        this.notes = p.notes;
        this.timestamp = p.timestamp;
        this.timesVisited = p.timesVisited;
        this.strikes = p.strikes;
        this.timeStampHistory = p.timeStampHistory;
        this.signedWaiver = p.signedWaiver;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Person person = (Person) o;
        return Objects.equals(id, person.id) &&
                Objects.equals(name, person.name) &&
                Objects.equals(email, person.email) &&
                Objects.equals(certifications, person.certifications) &&
                Objects.equals(notes, person.notes) &&
                Objects.equals(signedWaiver, person.signedWaiver) &&
                Objects.equals(timesVisited, person.timesVisited) &&
                Objects.equals(strikes, person.strikes);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id, name, email, certifications, notes, signedWaiver, timestamp, timesVisited, strikes, timeStampHistory);
    }


    /**
     * Used in analytic functions
     * Returns only revelant timestamp entries for a given month and year
     * @param month
     * @param year
     * @return ArrayList of timestamps that are relevant
     */
    public ArrayList<Timestamp> timeStampsForMonth(Integer month, Integer year){
        ArrayList<Timestamp> output = new ArrayList<>();
        for (Timestamp ts : timeStampHistory){
            if (ts.getMonth()==month && ts.getYear()==year){
                output.add(ts);
            }
        }
        return output;
    }

}

