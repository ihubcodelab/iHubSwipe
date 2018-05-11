package swipe.data;

import com.google.gson.Gson;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import org.hildan.fxgson.FxGson;
import swipe.util.FileManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Collection;

public class PersonModel {

    /*An observable version of the values in personMap. Changes in the personMap are reflected in this list and vice-versa.*/
    private ObservableList<Person> observablePersonList;

    public PersonModel(){
        observablePersonList = FXCollections.observableArrayList(param -> new Observable[]{
                param.idProperty(),
                param.nameProperty(),
                param.shopCertificationProperty(),
                param.strikesProperty(),
                param.timesVisitedProperty(),
                param.labCertificationProperty(),
                param.emailProperty(),
                param.notesProperty(),
                param.signedWaiverProperty()
        });
        observablePersonList.addListener((ListChangeListener<Person>) c -> System.out.println(""));
    }
    
    public Boolean add(Person p){
        return this.observablePersonList.add(p);
    }

    public Person getByID(String key){
        //Stream<Person> stream = observablePersonList.stream().filter(p -> p.getId().equals(key));
        //System.out.println(stream);
        Person output = observablePersonList.stream().filter(p -> p.getId().equals(key)).findFirst().orElse(null);
        return output;
    }
    
    public Boolean remove(Person person){
        Person toDelete =  this.observablePersonList.stream()
                .filter(p -> p.getId().equals(person.getId()))
                .findFirst().orElse(null);
        return this.observablePersonList.remove(toDelete);
    }

    public Boolean contains(Person person){
        return observablePersonList.stream().anyMatch(p -> p.getId().equals(person.getId()));
    }


    /**
     * Loads person objects from files into their Gson model and adds them to the map.
     * @param files Collection of files to add.
     */
    public void loadFromFiles(Collection<File> files){
        for(File f : files){
            if(!f.isHidden() && f.exists()){
                try(BufferedReader br = new BufferedReader(new FileReader(f))){
                    Gson gson = FxGson.create();
                    Person person = gson.fromJson(br, Person.class);
                    //System.out.println("Person is: " + person);
                    FileManager.validateUpToDateJson(person);
                    add(person);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
    }

    public ObservableList<Person> getObservableList(){
        return observablePersonList;
    }
}
