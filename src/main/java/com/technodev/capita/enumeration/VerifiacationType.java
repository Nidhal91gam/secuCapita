package com.technodev.capita.enumeration;

public enum VerifiacationType {
    ACCOUNT("ACCOUNT"),
    PASSWORD("PASSWORD");

    private final String type;

    VerifiacationType(String type) {
        this.type = type;
    }

    public String getType(){
        return this.type.toLowerCase();
    }
}
