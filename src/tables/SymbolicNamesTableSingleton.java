package tables;

import exceptions.UnknownCommandException;
import models.RecordBody;
import models.SymbolicName;

import java.util.ArrayList;
import java.util.HashMap;

public class SymbolicNamesTableSingleton {
    private static SymbolicNamesTableSingleton instance;
    private HashMap<String, SymbolicName> symbolicNames;

    private SymbolicNamesTableSingleton() {
        symbolicNames = new HashMap<>();
    }

    public static SymbolicNamesTableSingleton getInstance(){
        if(instance == null){
            instance = new SymbolicNamesTableSingleton();
        }
        return instance;
    }

    public void addNewName(String name, String address) throws UnknownCommandException {
        address = address.toUpperCase();
        if (address.length() != 6){
            address = "0".repeat(Math.max(0, 6 - address.length())) + address;
        }

        if (symbolicNames.containsKey(name)) {
            SymbolicName symbolicName = symbolicNames.get(name);
            if (!"".equals(symbolicName.getAddressName()))
                throw new UnknownCommandException("Повторение метки.");

            symbolicName.setAddressName(address);

            //Ищем в объектном модуле вхождения адресов и меняем
            RecordingTableSingleton record = RecordingTableSingleton.getInstance();
            HashMap<String , RecordBody> hashMap = record.getRecordingTable();
            ArrayList<String> addresses = symbolicName.getAddressArrayList();
            for (int i = 0; i < addresses.size(); i++) {
                RecordBody body = hashMap.get("T" + addresses.get(i));
                String str = body.getBody().substring(0, body.getBody().indexOf("#"));
                hashMap.put("T" + addresses.get(i), new RecordBody(str + address));
            }

            symbolicName.clearAddressCounter();
        }
        else {
            symbolicNames.put(name, new SymbolicName(address));
        }
    }

    public void addAddressToName(String name, String address) throws UnknownCommandException {
        address = address.toUpperCase();
        if (address.length() != 6){
            address = "0".repeat(Math.max(0, 6 - address.length())) + address;
        }

        if (symbolicNames.containsKey(name)) {
            SymbolicName symbolicName = symbolicNames.get(name);
            if (!"".equals(symbolicName.getAddressName()))
                return;

            symbolicName.addAddressCounter(address);
        }
        else {
            SymbolicName symbolicName = new SymbolicName("");
            symbolicName.addAddressCounter(address);
            symbolicNames.put(name, symbolicName);
        }
    }

    public HashMap<String , SymbolicName> getSymbolicNames() {
        return symbolicNames;
    }

    public static void clear() {
        instance = null;
    }
}
