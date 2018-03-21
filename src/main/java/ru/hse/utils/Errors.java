package ru.hse.utils;

public enum Errors {
    INVALID_TOKEN("invalid token"),
    SQL_INSERT_ERROR("error while inserting data in db"),
    UNKNOWN_ERROR("error occurred");

    private String msg;
    Errors(String msg){
        this.msg = msg;
    }

    public String getMsg() {
        return msg;
    }
}
