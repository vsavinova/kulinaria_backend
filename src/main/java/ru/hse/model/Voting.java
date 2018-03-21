package ru.hse.model;

import java.util.Date;

public class Voting {
    private int votingId;
    private int userId;
    private int recId;
    private int stars;
    private Date votingDate;
    private String token;

    public Voting(int votingId, int userId, int recId, int stars, Date votingDate, String token) {
        this.votingId = votingId;
        this.userId = userId;
        this.recId = recId;
        this.stars = stars;
        this.votingDate = votingDate;
        this.token = token;
    }

    public Voting(){

    }

    public int getVotingId() {
        return votingId;
    }

    public void setVotingId(int votingId) {
        this.votingId = votingId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getRecId() {
        return recId;
    }

    public void setRecId(int recId) {
        this.recId = recId;
    }

    public int getStars() {
        return stars;
    }

    public void setStars(int stars) {
        this.stars = stars;
    }

    public Date getVotingDate() {
        return votingDate;
    }

    public void setVotingDate(Date votingDate) {
        this.votingDate = votingDate;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
