package ru.hse.model;

import java.util.Date;
import java.util.List;

public class Receipt {
    private int recId;
    private String recName;
    private int userId;
    private Date publicationDate;
    private String photo;
    private String annotation;
    private Integer time;
    private int nutrId;
    private int catId;
    private List<Ingredient> ingredients;
    private List<Step> steps;
    private String token;

    public Receipt(int recId, String recName, int userId, Date publicationDate, String photo, String annotation, Integer time, int nutrId, int catId, List<Ingredient> ingredients, List<Step> steps, String token) {
        this.recId = recId;
        this.recName = recName;
        this.userId = userId;
        this.publicationDate = publicationDate;
        this.photo = photo;
        this.annotation = annotation;
        this.time = time;
        this.nutrId = nutrId;
        this.catId = catId;
        this.ingredients = ingredients;
        this.steps = steps;
        this.token = token;
    }

    public Receipt() {
    }

    public int getRecId() {
        return recId;
    }

    public void setRecId(int recId) {
        this.recId = recId;
    }

    public String getRecName() {
        return recName;
    }

    public void setRecName(String recName) {
        this.recName = recName;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public Date getPublicationDate() {
        return publicationDate;
    }

    public void setPublicationDate(Date publicationDate) {
        this.publicationDate = publicationDate;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getAnnotation() {
        return annotation;
    }

    public void setAnnotation(String annotation) {
        this.annotation = annotation;
    }

    public Integer getTime() {
        return time;
    }

    public void setTime(Integer time) {
        this.time = time;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public int getNutrId() {
        return nutrId;
    }

    public void setNutrId(int nutrId) {
        this.nutrId = nutrId;
    }

    public int getCatId() {
        return catId;
    }

    public void setCatId(int catId) {
        this.catId = catId;
    }

    public List<Ingredient> getIngredients() {
        return ingredients;
    }

    public void setIngredients(List<Ingredient> ingredients) {
        this.ingredients = ingredients;
    }

    public List<Step> getSteps() {
        return steps;
    }

    public void setSteps(List<Step> steps) {
        this.steps = steps;
    }
}
