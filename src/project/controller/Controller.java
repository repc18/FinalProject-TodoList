package project.controller;

import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.jdesktop.swingx.JXDatePicker;

import javax.swing.*;
import java.util.Date;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Updates.set;

public class Controller {
    private static MongoCollection<Document> listCollection;

    // This function is called to open the connection with the database
    private static void connectWithMongoDB() {
        MongoClient mongoClient = new MongoClient("localhost", 27017);
        System.out.println("Database connected");
        MongoDatabase todoDatabase = mongoClient.getDatabase("todo");
        listCollection = todoDatabase.getCollection("list");
    }

    // This function is called when user want to add a new task
    public static void addTaskIntoDatabase(JTextField newTaskField, JXDatePicker datePicker, JComboBox<String> priorityComboBox){
        connectWithMongoDB();
        String taskName = newTaskField.getText();
        Date taskDate = datePicker.getDate();
        String taskPriority = (String) priorityComboBox.getSelectedItem();
        System.out.println("List Collection: " + listCollection.getDocumentClass());
        System.out.println("Task: " + taskName);
        System.out.println("Task date: " + taskDate);
        System.out.println("Priority: " + taskPriority);
        Document taskDocument = new Document("_id", new ObjectId());
        taskDocument.append("task", taskName);
        taskDocument.append("date", taskDate);
        taskDocument.append("priority", taskPriority);
        listCollection.insertOne(taskDocument);
        System.out.println("Task added to the database");
    }

    // This function is called to load the table
    public static FindIterable<Document> readFromDatabase() {
        connectWithMongoDB();
        FindIterable<Document> iterTask = listCollection.find();
        System.out.println("Table loaded!");
        return iterTask;
    }

    // This function called when user want to update details of the task
    public static UpdateResult updateTaskDetail(String fieldName, Object input, String whereName, Date whereDate, String wherePriority) {
        connectWithMongoDB();
        Bson filter = and(eq("task", whereName), eq("date", whereDate), eq("priority", wherePriority));
        Bson updateOperation = set(fieldName, input);
        return listCollection.updateOne(filter, updateOperation);
    }

    // This function called when the task is finished
    public static DeleteResult deleteTask(String whereName, Date whereDate, String wherePriority) {
        connectWithMongoDB();
        Bson filter = and(eq("task", whereName), eq("date", whereDate), eq("priority", wherePriority));
        return listCollection.deleteOne(filter);
    }
}

