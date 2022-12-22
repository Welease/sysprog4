package models;


import java.util.ArrayList;

public class SymbolicName {
    private String addressName;
    private ArrayList<String> addressArrayList;

    public SymbolicName(String addressName) {
        this.addressName = addressName;
        addressArrayList = new ArrayList<>();
    }

    public String getAddressName() {
        return addressName;
    }

    public void setAddressName(String addressName) {
        this.addressName = addressName;
    }

    public void addAddressCounter(String addressCounter) {
        addressArrayList.add(addressCounter);
    }

    public ArrayList<String> getAddressArrayList() {return addressArrayList;}

    public void clearAddressCounter() {
        addressArrayList.clear();
    }
}
