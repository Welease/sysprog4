package tables;

import models.Operation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class OpcodeTableSingleton {
    private static OpcodeTableSingleton instance;
    private HashMap<String , Operation> opcodeTable;
    private Set<String> codes;

    private OpcodeTableSingleton() {
        opcodeTable = new HashMap<>();
        codes = new HashSet<>();
    }

    public static OpcodeTableSingleton getInstance(){
        if(instance == null){
            instance = new OpcodeTableSingleton();
        }
        return instance;
    }

    public void addCommand(String name, String binaryCode, int len) {
        if (codes.contains(binaryCode.trim().toLowerCase()))
            throw new RuntimeException();
        else
            codes.add(binaryCode);
        opcodeTable.put(name, new Operation(binaryCode, len));
    }

    public int getBinaryCode(String name) {
        return opcodeTable.get(name).getBinaryCode();
    }

    public int getLen(String name) {
        return opcodeTable.get(name).getLen();
    }

    public boolean checkCommand(String name) {
        return opcodeTable.containsKey(name);
    }

    public HashMap<String, Operation> getOpcodeTable() {
        return opcodeTable;
    }

    public static void clear() {
        instance = null;
    }
}
