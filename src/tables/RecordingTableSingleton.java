package tables;

import models.RecordBody;

import java.util.HashMap;

public class RecordingTableSingleton {
    private static RecordingTableSingleton instance;
    private HashMap<String , RecordBody> recordTable;

    private RecordingTableSingleton() {
        recordTable = new HashMap<>();
    }

    public static RecordingTableSingleton getInstance() {
        if(instance == null){
            instance = new RecordingTableSingleton();
        }
        return instance;
    }

    public void addRecord(String type, String address, String value) {
        recordTable.put(type + address.toUpperCase(), new RecordBody(value));
    }

    public void addRecord(String type, String address, String len, String value) {
        recordTable.put(type + address.toUpperCase(), new RecordBody(len, value));
    }

    public HashMap<String , RecordBody> getRecordingTable() {
        return this.recordTable;
    }

    public static void clear() {
        instance = null;
    }
}
