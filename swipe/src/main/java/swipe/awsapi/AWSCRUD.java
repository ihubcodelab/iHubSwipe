package swipe.awsapi;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.document.*;
import com.amazonaws.services.dynamodbv2.document.spec.GetItemSpec;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import com.amazonaws.services.dynamodbv2.document.spec.ScanSpec;
import com.google.gson.Gson;
import org.hildan.fxgson.FxGson;
import swipe.data.Person;
import swipe.data.PersonModel;
import swipe.data.Timestamp;
import swipe.util.FileManager;
import swipe.util.LogManager;

import java.util.*;

public class AWSCRUD {
    static private String tableName = "Ihub_persons";
    static private String studentInfoTable = "studentInfo";
    static private final String EMPTY_STRING = "҂҂҂";


    static public boolean create(Person person){
        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard()
                .withRegion(Regions.US_EAST_2)
                .build();

        Gson gson = FxGson.create();

        DynamoDB dynamoDB = new DynamoDB(client);

        Table table = dynamoDB.getTable(tableName);




        try {
            System.out.println("Adding a new item...");
            //need to redo person's timeStampHistory
            ArrayList<Timestamp> newTSHistory = new ArrayList<Timestamp>();
            for (Timestamp ts: person.getTimeStampHistory()){
                if (ts.getEnd().equals("")){
                    ts.setEnd(ts.getStart());
                }
                newTSHistory.add(ts);
            }
            person.setTimeStampHistory(newTSHistory);

            Item item = new Item()
                    .withPrimaryKey("id", person.getId())
                    .withString("name", person.getName())
                    .withString("email",(person.getEmail().equals("")) ? EMPTY_STRING : person.getEmail())
                    .withJSON("certifications", gson.toJson(person.getCertifications()))
                    .withString("notes", (person.getNotes().equals("")) ? EMPTY_STRING : person.getNotes())
                    .withString("timesVisited", person.getTimesVisited())
                    .withString("strikes", (person.getStrikes().equals("")) ? "0" : person.getStrikes())
                    .withString("signedWaiver", person.getSignedWaiver())
                    .withJSON("timeStampHistory", gson.toJson(person.getTimeStampHistory()));
            PutItemOutcome outcome = table.putItem(item);
            System.out.println("PutItem succeeded:\n" + outcome.getPutItemResult());
            LogManager.appendLogWithTimeStamp(person.getName() + " was successfully sent to AWS with id:  " + person.getId());


        }
        catch (Exception e) {
            LogManager.appendLogWithTimeStamp("AWS Put Failure for id: " + person.getId());
            LogManager.appendLogWithTimeStamp("Error: " + e.getMessage());
            System.err.println("Unable to add item: " + person.getName());
            System.out.println(person);
            System.err.println(e.getMessage());
        }

        return false;
    }

    static public Person read(String id){
        Gson gson = FxGson.create();
        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard()
                .withRegion(Regions.US_EAST_2)
                .build();
        DynamoDB dynamoDB = new DynamoDB(client);
        Table table = dynamoDB.getTable(tableName);
        GetItemSpec spec = new GetItemSpec().withPrimaryKey("id",id);
        try {
            Item outcome = table.getItem(spec);
            Person output = gson.fromJson(outcome.toJSON(), Person.class);
            //This EMPTY_STRING stuff is here because DynamoDB won't allow you to upload empty strings
            if (output.getNotes().equals(EMPTY_STRING)){
                output.setNotes("");
            }
            if (output.getEmail().equals(EMPTY_STRING)){
                output.setEmail("");
            }
            output.setTimestampProperty(Timestamp.getCurrentTime());
            LogManager.appendLogWithTimeStamp(output.getName() + " was retrieved from AWS with id:  " + output.getId());
            return output;
        } catch (Exception e){
            LogManager.appendLogWithTimeStamp("AWS Retrieval Failure for id: " + id);
            LogManager.appendLogWithTimeStamp("Error: " + e.getMessage());
            System.err.println("Unable to retrieve person with id: " + id);
            System.err.println(e.getMessage());
            return null;
        }
    }


    static public boolean deleteVisitorWithID(String id) {
        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard()
                .withRegion(Regions.US_EAST_2)
                .build();
        DynamoDB dynamoDB = new DynamoDB(client);
        Table table = dynamoDB.getTable(tableName);
        try {
            DeleteItemOutcome outcome = table.deleteItem("id", id);
            return true;
        } catch (Exception e){
            System.err.println("Unable to delete person with ID: " + id);
            System.err.println(e.getMessage());
            return false;
        }
    }

    static public boolean uploadDirectory(PersonModel directory) {
        for (Person p: directory.getObservableList()) {
            try {
                create(p);
                LogManager.appendLogWithTimeStamp(p.getName() + " with ID: " + p.getId() + " uploaded to AWS");
            } catch (Exception e) {
                LogManager.appendLogWithTimeStamp(p.getName() + " with ID: " + p.getId() + " had an error in uploading to AWS");
                return false;
            }
        }

        return true;
    }

    static public void downloadDirectory() {
        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard()
                .withRegion(Regions.US_EAST_2)
                .build();
        DynamoDB dynamoDB = new DynamoDB(client);
        Table table = dynamoDB.getTable(tableName);
        ScanSpec scanSpec = new ScanSpec();
        Gson gson = FxGson.create();
        try{
            ItemCollection<ScanOutcome> items = table.scan(scanSpec);
            Iterator<Item> iter = items.iterator();
            while (iter.hasNext()) {
                Item item = iter.next();
                Person p = gson.fromJson(item.toJSON(), Person.class);
                if (p.getNotes().equals(EMPTY_STRING)){
                    p.setNotes("");
                }
                if (p.getEmail().equals(EMPTY_STRING)){
                    p.setEmail("");
                }
                FileManager.saveDirectoryJsonFile(p);
            }
        } catch (Exception e){
            LogManager.appendLogWithTimeStamp("Error in downloading from AWS: " + e.getMessage());
        }

    }

    static public Map<String, Object> retrieveStudentInfo(String id){
        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard()
                .withRegion(Regions.US_EAST_2)
                .build();
        DynamoDB dynamoDB = new DynamoDB(client);
        Table table = dynamoDB.getTable(studentInfoTable);

        HashMap<String, String> nameMap = new HashMap<>();
        nameMap.put("#id", "id");

        HashMap<String, Object> valueMap = new HashMap<>();
        valueMap.put(":id", id);

        QuerySpec querySpec = new QuerySpec()
                .withKeyConditionExpression("#id = :id")
                .withNameMap(nameMap)
                .withValueMap(valueMap);

        ItemCollection<QueryOutcome> items;
        Iterator<Item> iterator;
        Item item;

        try {
            items = table.query(querySpec);
            iterator = items.iterator();
            while (iterator.hasNext()){
                item = iterator.next();
                return item.asMap();
            }
        } catch (Exception e){
            LogManager.appendLogWithTimeStamp("error retrieving student info for id: " + id +" "+ e.getMessage());
            return null;
        }
        return null;
    }


    /**
     * Not currently very functional, needs exact name.
     * Look into creating a GSI on the table over the name attribute
     * @param searchTerm
     * @return
     */
    static public List<Person> searchName(String searchTerm){
        ArrayList<Person> output = new ArrayList<Person>();
        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard()
                .withRegion(Regions.US_EAST_2)
                .build();
        DynamoDB dynamoDB = new DynamoDB(client);
        Table table = dynamoDB.getTable(tableName);

        Gson gson = FxGson.create();

        //nameMap provides name substitution. some words are reserved in dynamodb
        //(like 'year') so this addresses that
        HashMap<String, String> nameMap = new HashMap<>();
        nameMap.put("#nm", "name");

        //you can't have literal values in DynamoDB, so you need this valueMap
        HashMap<String, Object> valueMap = new HashMap<>();
        valueMap.put(":st", searchTerm);

        QuerySpec querySpec = new QuerySpec()
                .withKeyConditionExpression("#nm = :st")
                .withNameMap(nameMap)
                .withValueMap(valueMap);

        ItemCollection<QueryOutcome> items = null;
        Iterator<Item> iterator = null;
        Item item = null;

        try {
            items = table.query(querySpec);
            iterator = items.iterator();
            while (iterator.hasNext()){
                item = iterator.next();
                Person p = gson.fromJson(item.toJSON(), Person.class);
                if (p.getNotes().equals(EMPTY_STRING)){
                    p.setNotes("");
                }
                if (p.getEmail().equals(EMPTY_STRING)){
                    p.setEmail("");
                }
                System.out.println("SEARCH RESULTS");
                System.out.println(p);
                output.add(p);
            }
        } catch (Exception e){
            LogManager.appendLogWithTimeStamp("error doing search for "+searchTerm+": " + e.getMessage());
            return null;
        }

        return output;
    }


}
