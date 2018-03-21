package ru.hse.model;

public class Step {
    private int stepId;
    private String photo;
    private String description;

    public Step(int stepId, String photo, String description) {
        this.stepId = stepId;
        this.photo = photo;
        this.description = description;
    }

    public Step() {
    }

    public int getStepId() {
        return stepId;
    }

    public void setStepId(int stepId) {
        this.stepId = stepId;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
