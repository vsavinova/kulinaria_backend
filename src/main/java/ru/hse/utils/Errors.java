package ru.hse.utils;

public enum Errors {
    INVALID_TOKEN("invalid token"),
    SQL_INSERT_ERROR("error while inserting data in db"),
    SQL_GET_INFO_ERROR("error while getting data from db"),
    AUTH_ERROR("authentication failed"),
    UNKNOWN_ERROR("error occurred");

    private String msg;
    Errors(String msg){
        this.msg = msg;
    }

    public String getMsg() {
        return msg;
    }
}
