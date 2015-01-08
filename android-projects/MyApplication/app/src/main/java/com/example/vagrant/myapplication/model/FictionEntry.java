package com.example.vagrant.myapplication.model;

public class FictionEntry {
    private String name;
    private String updateDate;

    public FictionEntry(String name, String updateDate) {
        this.name = name;
        this.updateDate = updateDate;
    }

    public String getName() {
        return name;
    }

    public String getUpdateDate() {
        return updateDate;
    }

    @Override
    public String toString() {
        return name + "   " + updateDate;
    }
}
