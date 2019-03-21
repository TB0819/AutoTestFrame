package com.frame.config;

public enum ContentTypeEnum {
    BODY("application/json;charset=utf-8"),
    FORM("application/x-www-form-urlencoded;application/json;charset=utf-8");

    private String value;

    private ContentTypeEnum(String value){
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
