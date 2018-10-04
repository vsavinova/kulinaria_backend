package ru.hse.model;

import java.sql.Blob;
import java.sql.Date;

public class UserInfo {
    private int userId;
    private String userName;
    private Blob photo;
    private Date registrationDate;
    private String bio;
    private String token;
    private int receiptsCount;

    public UserInfo(int userId, String userName, Blob photo, Date registrationDate, String bio, String token, int receiptsCount) {
        this.userId = userId;
        this.userName = userName;
        this.photo = photo;
        this.registrationDate = registrationDate;
        this.bio = bio;
        this.token = token;
        this.receiptsCount = receiptsCount;
    }

    public UserInfo() {
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Blob getPhoto() {
        return photo;
    }

    public void setPhoto(Blob photo) {
        this.photo = photo;
    }

    public Date getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(Date registrationDate) {
        this.registrationDate = registrationDate;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public int getReceiptsCount() {
        return receiptsCount;
    }

    public void setReceiptsCount(int receiptsCount) {
        this.receiptsCount = receiptsCount;
    }
}
