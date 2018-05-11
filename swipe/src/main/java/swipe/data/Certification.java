package swipe.data;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleBooleanProperty;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class Certification {
    private SimpleStringProperty certName;
    private SimpleStringProperty expiration;
    private SimpleBooleanProperty isShopCert;
    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("MM-dd-yyyy");

    public Certification(){
        this.certName = new SimpleStringProperty();
        this.expiration = new SimpleStringProperty();
        this.isShopCert = new SimpleBooleanProperty(false);
    }

    public Certification(String certName) {
        this.certName = new SimpleStringProperty(certName);
        this.expiration = new SimpleStringProperty(getYearFromNow());
        this.isShopCert = new SimpleBooleanProperty(false);

    }
    public Certification(String certName, String expiration) {
        this.certName = new SimpleStringProperty(certName);
        this.expiration = new SimpleStringProperty(expiration);
        this.isShopCert = new SimpleBooleanProperty(false);
    }

    public Certification(String certName, String expiration, boolean isShopCert) {
        this.certName = new SimpleStringProperty(certName);
        this.expiration = new SimpleStringProperty(expiration);
        this.isShopCert = new SimpleBooleanProperty(isShopCert);
    }


    private static String getYearFromNow(){
        String now = dateTimeFormatter.print(DateTime.now());
        Integer year = Integer.parseInt(now.substring(6,10));
        year += 1;
        return now.substring(0,6) + year.toString();
    }

    public boolean isShopCert() {
        return this.isShopCert.get();
    }

    public void setShopCert(boolean shopCert) {
        isShopCert = new SimpleBooleanProperty(shopCert);
    }

    public String getCertName() {
        return certName.get();
    }

    public SimpleStringProperty nameProperty() {
        return certName;
    }

    public void setCertName(String certName) {
        this.certName = new SimpleStringProperty(certName);
    }

    public String getExpiration() {
        return expiration.get();
    }

    public SimpleStringProperty expProperty() {
        return expiration;
    }

    public SimpleBooleanProperty isShopCertProperty() {
        return isShopCert;
    }

    public void setExpiration(String expiration) {
        this.expiration = new SimpleStringProperty(expiration);
    }

    public boolean isExpired(){
        return dateTimeFormatter.parseDateTime(expiration.get()).isBeforeNow();
    }



    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public String toString() {
        return getCertName() + ": " + getExpiration() + " (Shop?: "+ isShopCert()+")";
    }
}
