package ru.hse.model;

public class Ingredient {
    private int ingrId;
    private String ignName;
    private double count;
    private String unit;

    public Ingredient(int ingrId, String ignName, double count, String unit) {
        this.ingrId = ingrId;
        this.ignName = ignName;
        this.count = count;
        this.unit = unit;
    }

    public Ingredient() {
    }

    public int getIngrId() {
        return ingrId;
    }

    public void setIngrId(int ingrId) {
        this.ingrId = ingrId;
    }

    public String getIgnName() {
        return ignName;
    }

    public void setIgnName(String ignName) {
        this.ignName = ignName;
    }

    public double getCount() {
        return count;
    }

    public void setCount(double count) {
        this.count = count;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }
}
